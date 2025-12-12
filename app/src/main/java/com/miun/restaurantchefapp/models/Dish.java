package com.miun.restaurantchefapp.models;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Dish {
    private int id;
    private int originalID;  // Original ID from backend
    private String name;
    private int quantity;
    private String selectedAllergens;
    private String specialInstructions;
    private String comments;

    // Custom deserializer needed for LocalDateTime
    @SerializedName("orderedAt")
    private String orderedAtString;
    private transient LocalDateTime orderedAt;  // transient = don't serialize

    // Cooking time properties (NOTE: Backend sends "waitingTime" not "idleTime"!)
    private int activeTime;   // Time for active preparation (in minutes)
    private int waitingTime;  // Time for passive cooking/waiting (in minutes)

    // Scheduling properties (transient = only used locally, not from JSON)
    private transient LocalDateTime scheduledStartTime;
    private transient LocalDateTime scheduledFinishTime;
    private transient boolean isStarted;
    private transient boolean isDone;

    public Dish() {
        this.isStarted = false;
        this.isDone = false;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOriginalID() {
        return originalID;
    }

    public void setOriginalID(int originalID) {
        this.originalID = originalID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSelectedAllergens() {
        return selectedAllergens;
    }

    public void setSelectedAllergens(String selectedAllergens) {
        this.selectedAllergens = selectedAllergens;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDateTime getOrderedAt() {
        // Lazy parse from string to LocalDateTime
        if (orderedAt == null && orderedAtString != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            this.orderedAt = LocalDateTime.parse(orderedAtString, formatter);
        }
        return orderedAt;
    }

    public void setOrderedAt(LocalDateTime orderedAt) {
        this.orderedAt = orderedAt;
    }

    public void setOrderedAt(String orderedAtString) {
        this.orderedAtString = orderedAtString;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        this.orderedAt = LocalDateTime.parse(orderedAtString, formatter);
    }

    public int getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(int activeTime) {
        this.activeTime = activeTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    /**
     * Legacy method for backward compatibility
     * @deprecated Use getWaitingTime() instead
     */
    @Deprecated
    public int getIdleTime() {
        return waitingTime;
    }

    /**
     * Legacy method for backward compatibility
     * @deprecated Use setWaitingTime() instead
     */
    @Deprecated
    public void setIdleTime(int idleTime) {
        this.waitingTime = idleTime;
    }

    public int getTotalCookTime() {
        return activeTime + waitingTime;
    }

    public LocalDateTime getScheduledStartTime() {
        return scheduledStartTime;
    }

    public void setScheduledStartTime(LocalDateTime scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }

    public LocalDateTime getScheduledFinishTime() {
        return scheduledFinishTime;
    }

    public void setScheduledFinishTime(LocalDateTime scheduledFinishTime) {
        this.scheduledFinishTime = scheduledFinishTime;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }
}
