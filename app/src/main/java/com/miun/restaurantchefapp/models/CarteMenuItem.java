package com.miun.restaurantchefapp.models;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a menu item from the carte menu API endpoint.
 * This model matches the backend CarteMenuItem structure and contains
 * all menu item details including timing information needed for scheduling.
 */
public class CarteMenuItem {
    // MenuItem fields
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private Double price;

    @SerializedName("isVegan")
    private Boolean isVegan;

    @SerializedName("isGlutenFree")
    private Boolean isGlutenFree;

    @SerializedName("allergens")
    private String allergens;

    // CarteAttributes fields - These are critical for scheduling
    @SerializedName("carteAttributesId")
    private Long carteAttributesId;

    @SerializedName("activeTime")
    private Integer activeTime;

    @SerializedName("waitingTime")
    private Integer waitingTime;

    @SerializedName("isMeat")
    private Boolean isMeat;

    @SerializedName("isDessert")
    private Boolean isDessert;

    @SerializedName("canSubstitute")
    private Boolean canSubstitute;

    @SerializedName("isAppetizer")
    private Boolean isAppetizer;

    @SerializedName("isHuvud")
    private Boolean isHuvud;

    // Constructors
    public CarteMenuItem() {
    }

    // Getters - MenuItem fields
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public Boolean getIsVegan() {
        return isVegan;
    }

    public Boolean getIsGlutenFree() {
        return isGlutenFree;
    }

    public String getAllergens() {
        return allergens;
    }

    // Getters - CarteAttributes fields
    public Long getCarteAttributesId() {
        return carteAttributesId;
    }

    public Integer getActiveTime() {
        return activeTime;
    }

    public Integer getWaitingTime() {
        return waitingTime;
    }

    public Boolean getIsMeat() {
        return isMeat;
    }

    public Boolean getIsDessert() {
        return isDessert;
    }

    public Boolean getCanSubstitute() {
        return canSubstitute;
    }

    public Boolean getIsAppetizer() {
        return isAppetizer;
    }

    public Boolean getIsHuvud() {
        return isHuvud;
    }

    // Setters - MenuItem fields
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setIsVegan(Boolean isVegan) {
        this.isVegan = isVegan;
    }

    public void setIsGlutenFree(Boolean isGlutenFree) {
        this.isGlutenFree = isGlutenFree;
    }

    public void setAllergens(String allergens) {
        this.allergens = allergens;
    }

    // Setters - CarteAttributes fields
    public void setCarteAttributesId(Long carteAttributesId) {
        this.carteAttributesId = carteAttributesId;
    }

    public void setActiveTime(Integer activeTime) {
        this.activeTime = activeTime;
    }

    public void setWaitingTime(Integer waitingTime) {
        this.waitingTime = waitingTime;
    }

    public void setIsMeat(Boolean isMeat) {
        this.isMeat = isMeat;
    }

    public void setIsDessert(Boolean isDessert) {
        this.isDessert = isDessert;
    }

    public void setCanSubstitute(Boolean canSubstitute) {
        this.canSubstitute = canSubstitute;
    }

    public void setIsAppetizer(Boolean isAppetizer) {
        this.isAppetizer = isAppetizer;
    }

    public void setIsHuvud(Boolean isHuvud) {
        this.isHuvud = isHuvud;
    }

    // Convenience methods
    public Integer getTotalTime() {
        if (activeTime != null && waitingTime != null) {
            return activeTime + waitingTime;
        }
        return null;
    }

    public boolean hasCarteAttributes() {
        return carteAttributesId != null;
    }

    public boolean hasTimingInformation() {
        return activeTime != null && waitingTime != null;
    }

    @Override
    public String toString() {
        return "CarteMenuItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", activeTime=" + activeTime +
                ", waitingTime=" + waitingTime +
                '}';
    }
}
