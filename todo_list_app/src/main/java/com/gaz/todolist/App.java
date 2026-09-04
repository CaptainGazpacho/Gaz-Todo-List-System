package com.gaz.todolist;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.TimerTask;
import java.util.Timer;

import java.time.LocalDateTime;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Author: Gaz
 * Date: 2026-03-31
 * Description: A simple to-do list application that automatically sorts and schedules tasks based on certain criteria to maximize user efficiency and clarity.
 */

public class App 
{
    public static void main( String[] args )
    {

       databaseManager.createDatabase();

        Dotenv dotenv = Dotenv.load();
        String account = dotenv.get("ACCOUNT");

        todoList newList = new todoList();
        newList.addItem(account, "Finish project", LocalDateTime.of(2026, 4, 5, 17, 0), true, false, scale.LARGE);
        newList.addItem(account, "Buy groceries", LocalDateTime.of(2026, 4, 9, 12, 0), true, false, scale.MEDIUM);
        newList.addItem(account, "Call mom", LocalDateTime.of(2026, 4, 2, 18, 0), true, false, scale.SMALL);
        newList.addItem(account, "Pay bills", LocalDateTime.of(2026, 4, 6, 9, 0), true, false, scale.MEDIUM);
        newList.addItem(account, "Schedule dentist appointment", LocalDateTime.of(2026, 4, 5, 14, 0), true, false, scale.SMALL);

        for (todoItem item : newList.manualDoList.stream().filter(i -> i.scheduledTime.toLocalDate() != null && i.scheduledTime.toLocalDate().isEqual(LocalDateTime.now().toLocalDate())).collect(Collectors.toCollection(ArrayList::new))) {
            System.out.println("Task: " + item.task + ", \nDeadline: " + item.deadline + ", \nManual: " + item.mmanual + ", \nRecurring: " + item.recurring + ", \nUrgency: " + item.urgency + ", \nSize: " + item.size + ", \nScheduled Time: " + item.scheduledTime + "\n\n");
        }

        // Creates a timer to autosave
        Timer timer = new Timer();

        timer.schedule( new TimerTask() {
            public void run() {
                databaseManager.saveToDatabase(newList); 
            }
        }, 0, 60*5000);

        // Terminates the timer on shutdown and saves to database
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                timer.cancel();
                databaseManager.saveToDatabase(newList);
            }
        }, "Shutdown-thread"));

    }
}

