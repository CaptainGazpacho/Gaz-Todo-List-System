package com.gaz.todolist;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.TimerTask;
import java.util.Timer;

import java.time.LocalDateTime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Author: Gaz
 * Date: 2026-03-31
 * Description: A simple to-do list application that automatically sorts and schedules tasks based on certain criteria to maximize user efficiency and clarity.
 */

public class App 
{
    public static void main( String[] args )
    {
        todoList newList = new todoList();
        newList.addItem("Finish project", LocalDateTime.of(2026, 4, 5, 17, 0), true, false, scale.LARGE);
        newList.addItem("Buy groceries", LocalDateTime.of(2026, 4, 9, 12, 0), true, false, scale.MEDIUM);
        newList.addItem("Call mom", LocalDateTime.of(2026, 4, 2, 18, 0), true, false, scale.SMALL);
        newList.addItem("Pay bills", LocalDateTime.of(2026, 4, 6, 9, 0), true, false, scale.MEDIUM);
        newList.addItem("Schedule dentist appointment", LocalDateTime.of(2026, 4, 5, 14, 0), true, false, scale.SMALL);

        for (todoItem item : newList.manualDoList.stream().filter(i -> i.scheduledTime.toLocalDate() != null && i.scheduledTime.toLocalDate().isEqual(LocalDateTime.now().toLocalDate())).collect(Collectors.toCollection(ArrayList::new))) {
            System.out.println("Task: " + item.task + ", \nDeadline: " + item.deadline + ", \nManual: " + item.mmanual + ", \nRecurring: " + item.recurring + ", \nUrgency: " + item.urgency + ", \nSize: " + item.size + ", \nScheduled Time: " + item.scheduledTime + "\n\n");
        }

        // Creates a timer to autosave
        Timer timer = new Timer();

        timer.schedule( new TimerTask() {
            public void run() {
                saveToDatabase(newList); 
            }
        }, 0, 60*5000);

        // Terminates the timer on shutdown and saves to database
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                timer.cancel();
                saveToDatabase(newList);
            }
        }, "Shutdown-thread"));

    }

    /**
     * Connects to Database and saves the list
     */
    public static void saveToDatabase(todoList todo) {
        String url = "jdbc:sqlite:sql\\todo_list.db";

        String mergeQuery = """
                INSERT OR REPLACE INTO GAZ_LIST
                    (TASK_ID, RANK, TASK, DEADLINE, SCHEDULED_TIME, MANUAL, RECURRING, SIZE, STATUS)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                PreparedStatement mergeStmt = conn.prepareStatement(mergeQuery);

                for(todoItem item:todo.inputList) {
                    mergeStmt.setString(1, item.getTaskID());
                    mergeStmt.setInt(2, item.getRank());
                    mergeStmt.setString(3, item.getTask());
                    mergeStmt.setString(4, item.getDeadline());
                    mergeStmt.setString(5, item.getScheduledTime());
                    mergeStmt.setString(6, item.getManual());
                    mergeStmt.setString(7, item.getRecurring());
                    mergeStmt.setInt(8, item.getSize());
                    mergeStmt.setInt(9, item.getStatus());

                    mergeStmt.execute();
                }

                todo = null;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

