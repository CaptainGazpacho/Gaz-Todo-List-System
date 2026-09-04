package com.gaz.todolist;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Collections;

import java.time.LocalDateTime;

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