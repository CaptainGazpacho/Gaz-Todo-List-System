package com.gaz.todolist;

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class databaseManager {

    /**
     * Creates a new database file if it does not already exist
     */
    public static void createDatabase() {
        try {
            File myObj = new File("todo_list_app\\sql\\todo_list.db");
            if (myObj.createNewFile()) {
                //System.out.println("File created: " + myObj.getName());
            } else {
                //System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace(); 
        }

        String url = "jdbc:sqlite:todo_list_app\\sql\\todo_list.db";

        String createTodoListTableQuery = """
                CREATE TABLE IF NOT EXISTS TODO_LIST (
	                "ACCOUNT"	TEXT,
	                "TASK_ID"	TEXT PRIMARY KEY,
	                "RANK"	INTEGER,
	                "TASK"	TEXT,
	                "DEADLINE"	INTEGER,
	                "SCHEDULED_TIME"	TEXT,
	                "MANUAL"	TEXT,
	                "RECURRING"	TEXT,
	                "SIZE"	INTEGER,
	                "STATUS"	INTEGER
                );
                """;

        String createManualTodoListTableQuery = """
                CREATE TABLE IF NOT EXISTS MANUAL_TODO_LIST (
	                "ACCOUNT"	TEXT,
	                "TASK_ID"	TEXT PRIMARY KEY,
	                "RANK"	INTEGER,
	                "TASK"	TEXT,
	                "DEADLINE"	INTEGER,
	                "SCHEDULED_TIME"	TEXT,
	                "MANUAL"	TEXT,
	                "RECURRING"	TEXT,
	                "SIZE"	INTEGER,
	                "STATUS"	INTEGER
                );
                """;

        String createPlanTodoListTableQuery = """
                CREATE TABLE IF NOT EXISTS PLAN_TODO_LIST (
	                "ACCOUNT"	TEXT,
	                "TASK_ID"	TEXT PRIMARY KEY,
	                "RANK"	INTEGER,
	                "TASK"	TEXT,
	                "DEADLINE"	INTEGER,
	                "SCHEDULED_TIME"	TEXT,
	                "MANUAL"	TEXT,
	                "RECURRING"	TEXT,
	                "SIZE"	INTEGER,
	                "STATUS"	INTEGER
                );
                """;

        String createDelegateTodoListTableQuery = """
                CREATE TABLE IF NOT EXISTS DELEGATE_TODO_LIST (
	                "ACCOUNT"	TEXT,
	                "TASK_ID"	TEXT PRIMARY KEY,
	                "RANK"	INTEGER,
	                "TASK"	TEXT,
	                "DEADLINE"	INTEGER,
	                "SCHEDULED_TIME"	TEXT,
	                "MANUAL"	TEXT,
	                "RECURRING"	TEXT,
	                "SIZE"	INTEGER,
	                "STATUS"	INTEGER
                );
                """;

        String createHoldTodoListTableQuery = """
                CREATE TABLE IF NOT EXISTS HOLD_TODO_LIST (
	                "ACCOUNT"	TEXT,
	                "TASK_ID"	TEXT PRIMARY KEY,
	                "RANK"	INTEGER,
	                "TASK"	TEXT,
	                "DEADLINE"	INTEGER,
	                "SCHEDULED_TIME"	TEXT,
	                "MANUAL"	TEXT,
	                "RECURRING"	TEXT,
	                "SIZE"	INTEGER,
	                "STATUS"	INTEGER
                );
                """;

        String createRecurringTodoListTableQuery = """
                CREATE TABLE IF NOT EXISTS RECURRING_TODO_LIST (
	                "ACCOUNT"	TEXT,
	                "TASK_ID"	TEXT PRIMARY KEY,
	                "RANK"	INTEGER,
	                "TASK"	TEXT,
	                "DEADLINE"	INTEGER,
	                "SCHEDULED_TIME"	TEXT,
	                "MANUAL"	TEXT,
	                "RECURRING"	TEXT,
	                "SIZE"	INTEGER,
	                "STATUS"	INTEGER
                );
                """;

        String createCompleteTodoListTableQuery = """
                CREATE TABLE IF NOT EXISTS COMPLETE_TODO_LIST (
	                "ACCOUNT"	TEXT,
	                "TASK_ID"	TEXT PRIMARY KEY,
	                "RANK"	INTEGER,
	                "TASK"	TEXT,
	                "DEADLINE"	INTEGER,
	                "SCHEDULED_TIME"	TEXT,
	                "MANUAL"	TEXT,
	                "RECURRING"	TEXT,
	                "SIZE"	INTEGER,
	                "STATUS"	INTEGER
                );
                """;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                PreparedStatement createTodoListStmt = conn.prepareStatement(createTodoListTableQuery);
                createTodoListStmt.execute();

                PreparedStatement createManualTodoListStmt = conn.prepareStatement(createManualTodoListTableQuery);
                createManualTodoListStmt.execute();

                PreparedStatement createPlanTodoListStmt = conn.prepareStatement(createPlanTodoListTableQuery);
                createPlanTodoListStmt.execute();

                PreparedStatement createDelegateTodoListStmt = conn.prepareStatement(createDelegateTodoListTableQuery);
                createDelegateTodoListStmt.execute();

                PreparedStatement createHoldTodoListStmt = conn.prepareStatement(createHoldTodoListTableQuery);
                createHoldTodoListStmt.execute();

                PreparedStatement createRecurringTodoListStmt = conn.prepareStatement(createRecurringTodoListTableQuery);
                createRecurringTodoListStmt.execute();

                PreparedStatement createCompleteTodoListStmt = conn.prepareStatement(createCompleteTodoListTableQuery);
                createCompleteTodoListStmt.execute();

                conn.close();

            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Connects to Database and saves the list
     * @param listName
     * @param todo
     */
    public static void saveToDatabase(String listName, ArrayList<todoItem> todo) {
        createDatabase();

        String table = " " + listName;

        String url = "jdbc:sqlite:todo_list_app\\sql\\todo_list.db";

        String mergeQuery = """
                INSERT OR REPLACE INTO """ + table + """
                    (ACCOUNT, TASK_ID, RANK, TASK, DEADLINE, SCHEDULED_TIME, MANUAL, RECURRING, SIZE, STATUS)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                PreparedStatement mergeStmt = conn.prepareStatement(mergeQuery);

                for(todoItem item:todo) {
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
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
