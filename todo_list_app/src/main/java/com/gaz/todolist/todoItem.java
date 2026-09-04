package com.gaz.todolist;

import java.util.Comparator;

import java.time.LocalDateTime;

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