package com.example;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Comparator;

import java.time.LocalDateTime;
import java.sql.Connection;
import java.sql.DriverManager;
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
        connectToDatabase();

        todoList newList = new todoList();
        newList.addItem("Finish project", LocalDateTime.of(2026, 4, 5, 17, 0), true, false, scale.LARGE);
        newList.addItem("Buy groceries", LocalDateTime.of(2026, 4, 9, 12, 0), true, false, scale.MEDIUM);
        newList.addItem("Call mom", LocalDateTime.of(2026, 4, 2, 18, 0), true, false, scale.SMALL);
        newList.addItem("Pay bills", LocalDateTime.of(2026, 4, 6, 9, 0), true, false, scale.MEDIUM);
        newList.addItem("Schedule dentist appointment", LocalDateTime.of(2026, 4, 5, 14, 0), true, false, scale.SMALL);

        for (todoItem item : newList.manualDoList.stream().filter(i -> i.scheduledTime.toLocalDate() != null && i.scheduledTime.toLocalDate().isEqual(LocalDateTime.now().toLocalDate())).collect(Collectors.toCollection(ArrayList::new))) {
            System.out.println("Task: " + item.task + ", \nDeadline: " + item.deadline + ", \nManual: " + item.mmanual + ", \nRecurring: " + item.recurring + ", \nUrgency: " + item.urgency + ", \nSize: " + item.size + ", \nScheduled Time: " + item.scheduledTime + "\n\n");
        }

    }

    public static void connectToDatabase() {
        String url = "jdbc:sqlite:todo_list.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("Connected to the database!");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

/**
 * This is the class definition for a to-do list
 */
class todoList {
    public ArrayList<todoItem> inputList = new ArrayList<todoItem>();

    public ArrayList<todoItem> manualDoList = new ArrayList<todoItem>();
    public ArrayList<todoItem> planDoList = new ArrayList<todoItem>();
    public ArrayList<todoItem> delegateDoList = new ArrayList<todoItem>();
    public ArrayList<todoItem> holdDoList = new ArrayList<todoItem>();
    
    public ArrayList<todoItem> recurringDoList = new ArrayList<todoItem>();

    public ArrayList<todoItem> completeDoList = new ArrayList<todoItem>();

    /**
     * This function adds a new item to the input list and refreshes the sort
     * @param task
     * @param deadline
     * @param mmanual
     * @param recurring
     * @param size
     */
    public void addItem(String task, LocalDateTime deadline, Boolean mmanual, Boolean recurring, scale size) {
        inputList.add(new todoItem(task, deadline, mmanual, recurring, size));
        this.refreshLists();
    }

    /**
     * This function refreshes the list after any updates were made, such as the addition of a new item or the completion of an existing one
     */
    public void refreshLists() {
        this.sortList();

        manualDoList = inputList.stream()
            .filter(item -> item.mmanual && item.urgency && !item.isComplete && !item.recurring)
            .collect(Collectors.toCollection(ArrayList::new));

        planDoList = inputList.stream()
            .filter(item -> item.mmanual && !item.urgency && !item.isComplete && !item.recurring)
            .collect(Collectors.toCollection(ArrayList::new));

        delegateDoList = inputList.stream()
            .filter(item -> !item.mmanual && item.urgency && !item.isComplete && !item.recurring)
            .collect(Collectors.toCollection(ArrayList::new));

        holdDoList = inputList.stream()
            .filter(item -> !item.mmanual && !item.urgency && !item.isComplete && !item.recurring)
            .collect(Collectors.toCollection(ArrayList::new));

        recurringDoList = inputList.stream()
            .filter(item -> item.recurring)
            .collect(Collectors.toCollection(ArrayList::new));

        completeDoList = inputList.stream()
            .filter(item -> item.isComplete)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * This function sorts the input list based on size and then by deadline, with larger items being higher priority and earlier deadlines being higher priority within those subsets.
     * It also automatically schedules tasks
     */
    public void sortList() {
        ArrayList<todoItem> largeItems = new ArrayList<todoItem>();
        ArrayList<todoItem> mediumItems = new ArrayList<todoItem>();
        ArrayList<todoItem> smallItems = new ArrayList<todoItem>();

        largeItems = this.inputList.stream()
            .filter(item -> item.size == scale.LARGE)
            .collect(Collectors.toCollection(ArrayList::new));

        mediumItems = this.inputList.stream()
            .filter(item -> item.size == scale.MEDIUM)
            .collect(Collectors.toCollection(ArrayList::new));

        smallItems = this.inputList.stream()
            .filter(item -> item.size == scale.SMALL)
            .collect(Collectors.toCollection(ArrayList::new));

        Collections.sort(largeItems, new deadlineComparator());

        largeItems.get(0).scheduledTime = LocalDateTime.now();

        for(int i = 1; i < largeItems.size(); i++) {
            if(largeItems.get(i).deadline == largeItems.get(i-1).deadline) {
                largeItems.get(i).scheduledTime = LocalDateTime.now();
            } else {
                largeItems.get(i).scheduledTime = largeItems.get(i-1).scheduledTime.plusDays(1);
            }
        }

        Collections.sort(mediumItems, new deadlineComparator());

        for(int i = 0; i < mediumItems.size(); i++) {
            if(i < 3) {
                mediumItems.get(i).scheduledTime = LocalDateTime.now();
            } else if (mediumItems.get(i).deadline == mediumItems.get(i-1).deadline) {
                mediumItems.get(i).scheduledTime = LocalDateTime.now();
            } else {
                mediumItems.get(i).scheduledTime = mediumItems.get(i-1).scheduledTime.plusDays(1);
            }
        }

        Collections.sort(smallItems, new deadlineComparator());

        for(int i = 0; i < smallItems.size(); i++) {
            if(i < 5) {
                smallItems.get(i).scheduledTime = LocalDateTime.now();
            } else if (smallItems.get(i).deadline == smallItems.get(i-1).deadline) {
                smallItems.get(i).scheduledTime = LocalDateTime.now();
            } else {
                smallItems.get(i).scheduledTime = smallItems.get(i-1).scheduledTime.plusDays(1);
            }
        }

        this.inputList.clear();
        this.inputList.addAll(largeItems);
        this.inputList.addAll(mediumItems);
        this.inputList.addAll(smallItems);

        largeItems = null;
        mediumItems = null;
        smallItems = null;
    }
}

/**
 * This is the class definition for a to-do item
 */
 class todoItem {
    String task = "";
    LocalDateTime deadline = null;
    LocalDateTime scheduledTime = null;
    Boolean mmanual = null;
    Boolean recurring = null;
    Boolean urgency = null;
    Boolean isComplete = false;
    scale size = null;

    /**
     * This construucts a new to-do item with the given parameters
     * @param task
     * @param deadline
     * @param mmanual
     * @param recurring
     * @param size
     */
    public todoItem(String task, LocalDateTime deadline, Boolean mmanual, Boolean recurring, scale size) {
        this.task = task;
        this.deadline = deadline;
        this.scheduledTime = null;
        this.mmanual = mmanual;
        this.recurring = recurring;
        this.urgency = (deadline.isBefore(LocalDateTime.now().plusDays(7))) ? true : false;
        this.size = size;
    }

    /**
     * This function marks a task as complete which causes it to be moved to the completeDoList and no longer appear in the other lists.
     */
    public void markComplete() {
        this.isComplete = true;
    }
}

/**
 * This comparator is used to sort items in a to-do list by their deadline, with the earliest deadlines first.
 */
class deadlineComparator implements Comparator<todoItem> {
    @Override
    public int compare(todoItem o1, todoItem o2) {
        return o1.deadline.compareTo(o2.deadline);
    }
}

/**
 * These are the scales of a task:
 * 
 * SMALL == Use or maintenance of an existing system or process (e.g. A system service request)
 * MEDIUM == Change in the functionality of an existing system or process (e.g. A system change request)
 * LARGE == Creation of a new system or process (e.g. An IMS project request)
 */
enum scale {
    SMALL,
    MEDIUM,
    LARGE;
}