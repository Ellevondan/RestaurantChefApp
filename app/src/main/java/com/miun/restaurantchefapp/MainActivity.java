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

import com.miun.restaurantchefapp.models.OrderBundle;
import com.miun.restaurantchefapp.models.PrioritizedDish;
import com.miun.restaurantchefapp.scheduling.DishPriorityScheduler;
import com.miun.restaurantchefapp.utils.MockJsonResponse;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private KitchenOrderAdapter adapter;
    private List<OrderBundle> activeBundles;

    private final Handler updateHandler = new Handler(Looper.getMainLooper());
    private static final int UPDATE_INTERVAL = 60000; // Update every 1 minute

    private final Runnable priorityUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updatePriorityList();
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // 2. Initialize Data from the separate Test Data File
        activeBundles = MockJsonResponse.getMockData();

        // 3. Initial Calculation & Adapter Setup
        List<PrioritizedDish> initialList = DishPriorityScheduler.calculatePriorityList(activeBundles);
        adapter = new KitchenOrderAdapter(initialList);
        recyclerView.setAdapter(adapter);

        // 4. Start Periodic Updates
        startPriorityUpdates();
    }

    private void updatePriorityList() {
        if (activeBundles != null && adapter != null) {
            List<PrioritizedDish> updatedList = DishPriorityScheduler.calculatePriorityList(activeBundles);
            adapter.updateData(updatedList);
        }
    }

    private void startPriorityUpdates() {
        updateHandler.post(priorityUpdateRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateHandler.removeCallbacks(priorityUpdateRunnable);
    }
}