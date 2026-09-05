package com.gaz.todolist;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.TimerTask;
import java.util.Timer;

import java.time.LocalDateTime;

import io.github.cdimascio.dotenv.Dotenv;

public class backendInit {
    public static void init() {
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
        if (dotenv.get("AUTOSAVE").equals("true")) {
            Timer timer = new Timer();

            timer.schedule( new TimerTask() {
            public void run() {
                databaseManager.saveToDatabase("TODO_LIST", newList.inputList);
                databaseManager.saveToDatabase("MANUAL_TODO_LIST", newList.manualDoList);
                databaseManager.saveToDatabase("PLAN_TODO_LIST", newList.planDoList);
                databaseManager.saveToDatabase("DELEGATE_TODO_LIST", newList.delegateDoList);
                databaseManager.saveToDatabase("HOLD_TODO_LIST", newList.holdDoList);
                databaseManager.saveToDatabase("RECURRING_TODO_LIST", newList.recurringDoList);
                databaseManager.saveToDatabase("COMPLETE_TODO_LIST", newList.completeDoList);
                }
            }, 0, 60*1000*30); // 30 minutes

            // Terminates the timer on shutdown and saves to database
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    timer.cancel();
                    databaseManager.saveToDatabase("TODO_LIST", newList.inputList);
                    databaseManager.saveToDatabase("MANUAL_TODO_LIST", newList.manualDoList);
                    databaseManager.saveToDatabase("PLAN_TODO_LIST", newList.planDoList);
                    databaseManager.saveToDatabase("DELEGATE_TODO_LIST", newList.delegateDoList);
                    databaseManager.saveToDatabase("HOLD_TODO_LIST", newList.holdDoList);
                    databaseManager.saveToDatabase("RECURRING_TODO_LIST", newList.recurringDoList);
                    databaseManager.saveToDatabase("COMPLETE_TODO_LIST", newList.completeDoList);
                }
            }, "Shutdown-thread"));
        } else {
            // Saves to database on shutdown if autosave is not enabled
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    databaseManager.saveToDatabase("TODO_LIST", newList.inputList);
                    databaseManager.saveToDatabase("MANUAL_TODO_LIST", newList.manualDoList);
                    databaseManager.saveToDatabase("PLAN_TODO_LIST", newList.planDoList);
                    databaseManager.saveToDatabase("DELEGATE_TODO_LIST", newList.delegateDoList);
                    databaseManager.saveToDatabase("HOLD_TODO_LIST", newList.holdDoList);
                    databaseManager.saveToDatabase("RECURRING_TODO_LIST", newList.recurringDoList);
                    databaseManager.saveToDatabase("COMPLETE_TODO_LIST", newList.completeDoList);
                }
            }, "Shutdown-thread"));
        }
    }
}
