package com.gaz.todolist;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Comparator;
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
        Timer timer = new Timer();

        todoList newList = new todoList();
        newList.addItem("Finish project", LocalDateTime.of(2026, 4, 5, 17, 0), true, false, scale.LARGE);
        newList.addItem("Buy groceries", LocalDateTime.of(2026, 4, 9, 12, 0), true, false, scale.MEDIUM);
        newList.addItem("Call mom", LocalDateTime.of(2026, 4, 2, 18, 0), true, false, scale.SMALL);
        newList.addItem("Pay bills", LocalDateTime.of(2026, 4, 6, 9, 0), true, false, scale.MEDIUM);
        newList.addItem("Schedule dentist appointment", LocalDateTime.of(2026, 4, 5, 14, 0), true, false, scale.SMALL);

        for (todoItem item : newList.manualDoList.stream().filter(i -> i.scheduledTime.toLocalDate() != null && i.scheduledTime.toLocalDate().isEqual(LocalDateTime.now().toLocalDate())).collect(Collectors.toCollection(ArrayList::new))) {
            System.out.println("Task: " + item.task + ", \nDeadline: " + item.deadline + ", \nManual: " + item.mmanual + ", \nRecurring: " + item.recurring + ", \nUrgency: " + item.urgency + ", \nSize: " + item.size + ", \nScheduled Time: " + item.scheduledTime + "\n\n");
        }

        saveToDatabase(newList);

        /*// Creates a timer to autosave
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
                System.out.println("In shutdown hook");
            }
        }, "Shutdown-thread")); */

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

        for(int i = 0; i < inputList.size(); i++) {
            inputList.get(i).setRank(i + 1);
        }

        largeItems = null;
        mediumItems = null;
        smallItems = null;
    }

    /**
     * Returns a size integer for the list
     * @return int
     */
    public int getSize() {
        int size = inputList.size();
        return size;
    }
}

/**
 * This is the class definition for a to-do item
 */
 class todoItem {
    String taskID = "";
    int rank = 0;
    String task = "";
    LocalDateTime deadline = null;
    LocalDateTime scheduledTime = null;
    Boolean mmanual = null;
    Boolean recurring = null;
    Boolean urgency = null;
    Boolean isComplete = false;
    scale size = null;
    progress status = null;
    String toString = "";
    /**
     * This constructs a new to-do item with the given parameters
     * @param task
     * @param deadline
     * @param mmanual
     * @param recurring
     * @param size
     */
    public todoItem(String task, LocalDateTime deadline, Boolean mmanual, Boolean recurring, scale size) {
        this.toString = "Task: " + task + ", Deadline: " + deadline + ", Manual: " + mmanual + ", Recurring: " + recurring + ", Size: " + size;
        this.taskID = "" + this.toString.hashCode();
        this.task = task;
        this.deadline = deadline;
        this.scheduledTime = null;
        this.mmanual = mmanual;
        this.recurring = recurring;
        this.urgency = (deadline.isBefore(LocalDateTime.now().plusDays(7))) ? true : false;
        this.size = size;
        this.status = progress.NOT_STARTED;
    }

    /**
     * Set's the rank of the task
     * @param currentRank
     */
    public void setRank(int currentRank) {
        this.rank = currentRank;
    }

    /**
     * Update to in progress
     */
    public void markInProgress() {
        this.status = progress.IN_PROGRESS;
    }

    /**
     * This function marks a task as complete which causes it to be moved to the completeDoList and no longer appear in the other lists.
     */
    public void markComplete() {
        this.isComplete = true;
        this.status = progress.COMPLETED;
    }

    /**
     * Cancel a task
     */
    public void markCanceled() {
        this.isComplete = true;
        this.status = progress.CANCELED;
    }

    /**
     * Returns the Task ID
     * @return String
     */
    public String getTaskID() {
        return taskID;
    }

    /**
     * Returns the rank of the task
     * @return int
     */
    public int getRank() {
        return rank;
    }

    /**
     * Returns the task definition
     * @return String
     */
    public String getTask() {
        return task;
    }

    /**
     * Returns the deadline
     * @return String
     */
    public String getDeadline() {
        String dl = deadline.toString();
        return dl;
    }

    /**
     * Returns the scheduled time
     * @return String
     */
    public String getScheduledTime() {
        String st = scheduledTime.toString();
        return st;
    }

    /**
     * Returns the manual status
     * @return String
     */
    public String getManual() {
        String m = "" + mmanual;
        return m;
    }

    /**
     * Returns the recurring status
     * @return String
     */
    public String getRecurring() {
        String r = "" + recurring;
        return r;
    }

    /**
     * Returns the size of the task
     * @return int
     */
    public int getSize() {
        return size.ordinal();
    }

    /**
     * Returns the status of the task
     * @return int
     */
    public int getStatus() {
        return status.ordinal();
    }

    /**
     * Returns a string representation of the todo item
     * @return String
     */
    public String toString() {
        return this.toString;
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

/**
 * This is for the progress of the project:
 * 
 * NOT_STARTED == The task has not been started yet
 * IN_PROGRESS == The task is currently being worked on
 * COMPLETED == The task has been completed
 * CANCELED == The task has been canceled and will not be completed
 */
enum progress {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELED;
}