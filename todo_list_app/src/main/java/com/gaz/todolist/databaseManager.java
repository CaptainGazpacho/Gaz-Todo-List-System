package com.gaz.todolist;

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class databaseManager {

    /**
     * Creates a new database file if it does not already exist
     */
    public static void createDatabase() {
        try {
            File myObj = new File("todo_list_app\\sql\\todo_list.db");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace(); 
        }
    }

    /**
     * Connects to Database and saves the list
     * @param todo
     */
    public static void saveToDatabase(todoList todo) {
        String url = "jdbc:sqlite:todo_list_app\\sql\\todo_list.db";

        String mergeQuery = """
                INSERT OR REPLACE INTO TODO_LIST
                    (ACCOUNT, TASK_ID, RANK, TASK, DEADLINE, SCHEDULED_TIME, MANUAL, RECURRING, SIZE, STATUS)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                PreparedStatement mergeStmt = conn.prepareStatement(mergeQuery);

                for(todoItem item:todo.inputList) {
                    mergeStmt.setString(1, item.getAccount());
                    mergeStmt.setString(2, item.getTaskID());
                    mergeStmt.setInt(3, item.getRank());
                    mergeStmt.setString(4, item.getTask());
                    mergeStmt.setString(5, item.getDeadline());
                    mergeStmt.setString(6, item.getScheduledTime());
                    mergeStmt.setString(7, item.getManual());
                    mergeStmt.setString(8, item.getRecurring());
                    mergeStmt.setInt(9, item.getSize());
                    mergeStmt.setInt(10, item.getStatus());

                    mergeStmt.execute();
                }

                todo = null;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
