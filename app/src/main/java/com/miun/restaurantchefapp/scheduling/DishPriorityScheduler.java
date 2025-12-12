package com.miun.restaurantchefapp.scheduling;

import com.miun.restaurantchefapp.models.CourseType;
import com.miun.restaurantchefapp.models.Dish;
import com.miun.restaurantchefapp.models.OrderBundle;
import com.miun.restaurantchefapp.models.PrioritizedDish;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Scheduler that calculates dish priorities based on backward scheduling algorithm.
 *
 * The goal: All dishes in an order bundle should finish at the same time,
 * so guests receive their food together.
 *
 * Algorithm:
 * 1. Respect course order (appetizer → main → dessert)
 * 2. For each bundle, find the longest total cook time
 * 3. Calculate target finish time (now + longest cook time)
 * 4. For each dish, calculate when it should START (finish time - cook time)
 * 5. Prioritize dishes that need to start soonest, within their course order
 */
public class DishPriorityScheduler {

    // Configurable weights for priority calculation
    private static final double WEIGHT_COURSE_TYPE = 1000.0;   // Course order is CRITICAL
    private static final double WEIGHT_ORDER_TIME = 0.3;       // Earlier orders get priority
    private static final double WEIGHT_START_TIME = 0.5;       // Dishes that need to start soon get priority
    private static final double WEIGHT_COMPLETION = 0.2;       // Bundles closer to completion get priority

    /**
     * Schedules all dishes across all order bundles and returns them sorted by priority
     */
    public static List<PrioritizedDish> calculatePriorityList(List<OrderBundle> bundles) {
        LocalDateTime now = LocalDateTime.now();
        List<PrioritizedDish> prioritizedDishes = new ArrayList<>();

        // First pass: Calculate scheduling for each bundle
        for (OrderBundle bundle : bundles) {
            if (bundle.isDone()) {
                continue; // Skip completed bundles
            }

            calculateBundleSchedule(bundle, now);

            // Second pass: Calculate priority for each dish in the bundle
            for (Dish dish : bundle.getOrders()) {
                if (dish.isDone()) {
                    continue; // Skip completed dishes
                }

                double priority = calculateDishPriority(dish, bundle, now);
                prioritizedDishes.add(new PrioritizedDish(dish, bundle, priority));
            }
        }

        // Sort by priority (lower score = higher priority = start sooner)
        Collections.sort(prioritizedDishes);

        return prioritizedDishes;
    }

    /**
     * Calculates the schedule for all dishes in a bundle
     * Sets the scheduledStartTime and scheduledFinishTime for each dish
     */
    private static void calculateBundleSchedule(OrderBundle bundle, LocalDateTime now) {
        // Find earliest order time in bundle
        LocalDateTime earliestOrder = null;
        for (Dish dish : bundle.getOrders()) {
            if (earliestOrder == null || dish.getOrderedAt().isBefore(earliestOrder)) {
                earliestOrder = dish.getOrderedAt();
            }
        }
        bundle.setEarliestOrderTime(earliestOrder);

        // Check if this is a RUSH order - no timing coordination needed
        if (!bundle.getCourseType().needsTimingCoordination()) {
            // RUSH orders: start ALL dishes immediately
            for (Dish dish : bundle.getOrders()) {
                dish.setScheduledStartTime(now);
                dish.setScheduledFinishTime(now.plusMinutes(dish.getTotalCookTime()));
            }
            bundle.setBundleTargetFinishTime(now);
            return;
        }

        // Normal courses: use backward scheduling to coordinate finish times
        // Find the longest cook time in this bundle
        int maxCookTime = bundle.getMaxTotalCookTime();

        // Target finish time = now + longest cook time
        LocalDateTime targetFinishTime = now.plusMinutes(maxCookTime);
        bundle.setBundleTargetFinishTime(targetFinishTime);

        // Calculate start and finish time for each dish
        for (Dish dish : bundle.getOrders()) {
            int totalCookTime = dish.getTotalCookTime();
            LocalDateTime dishStartTime = targetFinishTime.minusMinutes(totalCookTime);
            LocalDateTime dishFinishTime = targetFinishTime;

            dish.setScheduledStartTime(dishStartTime);
            dish.setScheduledFinishTime(dishFinishTime);
        }
    }

    /**
     * Calculates priority score for a dish
     * Lower score = higher priority
     *
     * Priority order:
     * 1. Course type (appetizers before mains before desserts)
     * 2. Start time urgency
     * 3. Order age
     * 4. Completion progress
     */
    private static double calculateDishPriority(Dish dish, OrderBundle bundle, LocalDateTime now) {
        // Component 0: Course type priority (MOST IMPORTANT)
        // Appetizers get much lower scores than mains, which get lower than desserts
        // This ensures course order is always respected
        CourseType courseType = bundle.getCourseType();
        double courseTypeComponent = courseType.getPriority() * WEIGHT_COURSE_TYPE;

        // Component 1: Order time (how long ago was it ordered?)
        // Earlier orders get lower scores (higher priority)
        long minutesSinceOrdered = ChronoUnit.MINUTES.between(
            bundle.getEarliestOrderTime(),
            now
        );
        double orderTimeComponent = -minutesSinceOrdered * WEIGHT_ORDER_TIME;

        // Component 2: Start time urgency (how soon should this dish start?)
        // Dishes that need to start soon (or should have started already) get lower scores
        long minutesUntilStart = ChronoUnit.MINUTES.between(
            now,
            dish.getScheduledStartTime()
        );
        double startTimeComponent = minutesUntilStart * WEIGHT_START_TIME;

        // Component 3: Bundle completion progress
        // Bundles that are partially complete get priority to finish them
        double completionProgress = bundle.getCompletionProgress();
        double completionComponent = -(completionProgress * 100) * WEIGHT_COMPLETION;

        // Calculate total priority score
        double priorityScore = courseTypeComponent + orderTimeComponent + startTimeComponent + completionComponent;

        return priorityScore;
    }

    /**
     * Gets a human-readable status for when a dish should be started
     */
    public static String getStartTimingStatus(Dish dish, LocalDateTime now) {
        LocalDateTime startTime = dish.getScheduledStartTime();
        long minutesUntilStart = ChronoUnit.MINUTES.between(now, startTime);

        if (minutesUntilStart <= 0) {
            long minutesOverdue = Math.abs(minutesUntilStart);
            if (minutesOverdue == 0) {
                return "START NOW!";
            } else if (minutesOverdue == 1) {
                return "OVERDUE by 1 minute!";
            } else {
                return "OVERDUE by " + minutesOverdue + " minutes!";
            }
        } else if (minutesUntilStart <= 2) {
            return "Start in " + minutesUntilStart + " min";
        } else if (minutesUntilStart <= 10) {
            return "Start in " + minutesUntilStart + " minutes";
        } else {
            return "Start in " + minutesUntilStart + " minutes";
        }
    }

    /**
     * Gets timing status color code
     * Returns one of: "urgent", "soon", "normal"
     */
    public static String getTimingStatusColor(Dish dish, LocalDateTime now) {
        LocalDateTime startTime = dish.getScheduledStartTime();
        long minutesUntilStart = ChronoUnit.MINUTES.between(now, startTime);

        if (minutesUntilStart <= 0) {
            return "urgent";  // Red - needs to start now or overdue
        } else if (minutesUntilStart <= 5) {
            return "soon";    // Yellow - needs to start soon
        } else {
            return "normal";  // Green - plenty of time
        }
    }
}
