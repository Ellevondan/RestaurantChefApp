package com.miun.restaurantchefapp.models;

/**
 * Wrapper class that pairs a Dish with its calculated priority score
 */
public class PrioritizedDish implements Comparable<PrioritizedDish> {
    private Dish dish;
    private OrderBundle parentBundle;
    private double priorityScore;

    public PrioritizedDish(Dish dish, OrderBundle parentBundle, double priorityScore) {
        this.dish = dish;
        this.parentBundle = parentBundle;
        this.priorityScore = priorityScore;
    }

    public Dish getDish() {
        return dish;
    }

    public OrderBundle getParentBundle() {
        return parentBundle;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(double priorityScore) {
        this.priorityScore = priorityScore;
    }

    /**
     * Compare by priority score (lower score = higher priority)
     */
    @Override
    public int compareTo(PrioritizedDish other) {
        return Double.compare(this.priorityScore, other.priorityScore);
    }
}
