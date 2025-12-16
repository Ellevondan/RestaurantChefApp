package com.miun.restaurantchefapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miun.restaurantchefapp.models.Dish;
import com.miun.restaurantchefapp.models.OrderBundle;
import com.miun.restaurantchefapp.models.PrioritizedDish;
import com.miun.restaurantchefapp.scheduling.DishPriorityScheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private KitchenOrderAdapter adapter;
    private List<OrderBundle> activeBundles; // Store the raw bundles here

    // Handler for periodic updates (Ref: EXAMPLE_USAGE.md)
    private final Handler updateHandler = new Handler(Looper.getMainLooper());
    private static final int UPDATE_INTERVAL = 60000; // Update every 1 minute

    // Runnable to recalculate priorities
    private final Runnable priorityUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updatePriorityList();
            // Schedule next update
            updateHandler.postDelayed(this, UPDATE_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        // 1. Setup RecyclerView
        recyclerView = findViewById(R.id.recycler_view_orders);

        // This makes the list scroll horizontally (left-to-right)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // 2. Initialize Data
        activeBundles = generateMockOrderBundles();

        // 3. Initial Calculation & Adapter Setup
        List<PrioritizedDish> initialList = DishPriorityScheduler.calculatePriorityList(activeBundles);
        adapter = new KitchenOrderAdapter(initialList);
        recyclerView.setAdapter(adapter);

        // 4. Start Periodic Updates
        startPriorityUpdates();
    }

    private void updatePriorityList() {
        if (activeBundles != null && adapter != null) {
            // Recalculate priorities based on the new current time
            List<PrioritizedDish> updatedList = DishPriorityScheduler.calculatePriorityList(activeBundles);

            // Update the adapter
            adapter.updateData(updatedList);
        }
    }

    private void startPriorityUpdates() {
        // Run immediately first, then schedule
        updateHandler.post(priorityUpdateRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop updates when activity is destroyed to prevent memory leaks
        updateHandler.removeCallbacks(priorityUpdateRunnable);
    }

    /**
     * Helper to generate mock data for demonstration
     */
    private List<OrderBundle> generateMockOrderBundles() {
        List<OrderBundle> bundles = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // --- Bundle 1: Main Course (Needs to start soon) ---
        OrderBundle b1 = new OrderBundle();
        b1.setGroupID(12);
        b1.setModifyType("MAIN");

        Dish d1 = new Dish();
        d1.setName("Grilled Steak");
        d1.setSpecialInstructions("Medium Rare");
        d1.setActiveTime(15);
        d1.setWaitingTime(5);
        d1.setOrderedAt(now.minusMinutes(10));

        List<Dish> orders1 = new ArrayList<>();
        orders1.add(d1);
        b1.setOrders(orders1);
        bundles.add(b1);

        // --- Bundle 2: Rush Order (Should be first!) ---
        OrderBundle b2 = new OrderBundle();
        b2.setGroupID(5);
        b2.setModifyType("RUSH");

        Dish d2 = new Dish();
        d2.setName("Quick Soup");
        d2.setActiveTime(5);
        d2.setWaitingTime(0);
        d2.setOrderedAt(now.minusMinutes(2));

        List<Dish> orders2 = new ArrayList<>();
        orders2.add(d2);
        b2.setOrders(orders2);
        bundles.add(b2);

        return bundles;
    }
}