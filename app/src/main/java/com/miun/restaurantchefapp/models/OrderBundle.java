package com.miun.restaurantchefapp.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderBundle {
    private int ID;
    private int groupID;
    private int id;  // Duplicate ID field from backend
    private boolean isDone;
    private List<Dish> orders;
    private String modifyType;  // NOTE: Backend sends "modifyType" not "modifiedType"!

    // Scheduling properties (transient = only used locally, not from JSON)
    private transient LocalDateTime bundleTargetFinishTime;
    private transient LocalDateTime earliestOrderTime;
    private transient CourseType courseType;  // Parsed from modifyType

    public OrderBundle() {
        this.orders = new ArrayList<>();
        this.isDone = false;
    }

    // Getters and setters
    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public List<Dish> getOrders() {
        return orders;
    }

    public void setOrders(List<Dish> orders) {
        this.orders = orders;
    }

    public LocalDateTime getBundleTargetFinishTime() {
        return bundleTargetFinishTime;
    }

    public void setBundleTargetFinishTime(LocalDateTime bundleTargetFinishTime) {
        this.bundleTargetFinishTime = bundleTargetFinishTime;
    }

    public LocalDateTime getEarliestOrderTime() {
        return earliestOrderTime;
    }

    public void setEarliestOrderTime(LocalDateTime earliestOrderTime) {
        this.earliestOrderTime = earliestOrderTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModifyType() {
        return modifyType;
    }

    public void setModifyType(String modifyType) {
        this.modifyType = modifyType;
    }

    /**
     * Gets the course type (parsed from modifyType string)
     * Lazy parsing on first access
     */
    public CourseType getCourseType() {
        if (courseType == null) {
            courseType = CourseType.fromString(modifyType);
        }
        return courseType;
    }

    /**
     * Gets the maximum total cook time among all dishes in this bundle
     */
    public int getMaxTotalCookTime() {
        int maxTime = 0;
        for (Dish dish : orders) {
            int totalTime = dish.getTotalCookTime();
            if (totalTime > maxTime) {
                maxTime = totalTime;
            }
        }
        return maxTime;
    }

    /**
     * Calculates completion progress (0.0 to 1.0)
     */
    public double getCompletionProgress() {
        if (orders.isEmpty()) {
            return 0.0;
        }
        int completedCount = 0;
        for (Dish dish : orders) {
            if (dish.isDone()) {
                completedCount++;
            }
        }
        return (double) completedCount / orders.size();
    }

    /**
     * Checks if all dishes in the bundle are done
     */
    public boolean allDishesComplete() {
        if (orders.isEmpty()) {
            return false;
        }
        for (Dish dish : orders) {
            if (!dish.isDone()) {
                return false;
            }
        }
        return true;
    }
}
