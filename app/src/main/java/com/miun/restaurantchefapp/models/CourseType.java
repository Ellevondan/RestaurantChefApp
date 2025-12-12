package com.miun.restaurantchefapp.models;

/**
 * Represents the type of course and its priority in meal sequence.
 * Lower priority value = higher priority (made first)
 */
public enum CourseType {
    RUSH(0, "Rush Order"),     // Highest priority - make ASAP, no timing needed
    APPETIZER(1, "Appetizer"),
    MAIN(2, "Main Course"),
    DESSERT(3, "Dessert"),
    UNKNOWN(99, "Unknown");    // Fallback for unrecognized types

    private final int priority;
    private final String displayName;

    CourseType(int priority, String displayName) {
        this.priority = priority;
        this.displayName = displayName;
    }

    public int getPriority() {
        return priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parse modifiedType string from backend to CourseType enum
     * Case-insensitive matching
     */
    public static CourseType fromString(String modifiedType) {
        if (modifiedType == null || modifiedType.isEmpty()) {
            return UNKNOWN;
        }

        String normalized = modifiedType.trim().toUpperCase();

        // Handle variations
        switch (normalized) {
            case "RUSH":
            case "URGENT":
            case "PRIORITY":
            case "ASAP":
                return RUSH;

            case "APPETIZER":
            case "APPETIZERS":
            case "STARTER":
            case "STARTERS":
                return APPETIZER;

            case "MAIN":
            case "MAINS":  // Backend sends "mains"
            case "MAIN COURSE":
            case "MAIN_COURSE":
            case "ENTREE":
                return MAIN;

            case "DESSERT":
            case "DESSERTS":
            case "SWEET":
            case "SWEETS":
                return DESSERT;

            default:
                return UNKNOWN;
        }
    }

    /**
     * Returns true if this course type needs timing coordination
     * (i.e., all dishes should finish together)
     * RUSH orders bypass timing - just make them ASAP
     */
    public boolean needsTimingCoordination() {
        return this != RUSH;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
