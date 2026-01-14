package com.miun.restaurantchefapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miun.restaurantchefapp.models.OrderBundle;
import com.miun.restaurantchefapp.models.PrioritizedDish;
import com.miun.restaurantchefapp.network.ApiRepository;
import com.miun.restaurantchefapp.network.OrderPollingManager;
import com.miun.restaurantchefapp.scheduling.DishPriorityScheduler;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private KitchenOrderAdapter adapter;
    private List<OrderBundle> activeBundles;

    // API components
    private ApiRepository repository;
    private OrderPollingManager pollingManager;

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

        // 2. Initialize API components
        repository = new ApiRepository();
        pollingManager = new OrderPollingManager(repository);

        // 3. Setup adapter with empty list initially
        adapter = new KitchenOrderAdapter(List.of());
        recyclerView.setAdapter(adapter);

        // 4. Start fetching orders from API
        startFetchingOrders();

        // 5. Start periodic priority updates
        startPriorityUpdates();
    }

    private void startFetchingOrders() {
        pollingManager.startPolling(new OrderPollingManager.OrderUpdateListener() {
            @Override
            public void onOrdersUpdated(List<OrderBundle> orders) {
                activeBundles = orders;
                updatePriorityList();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this,
                        "Fel vid hämtning: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
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
        pollingManager.stopPolling(); // Stoppa API-polling
    }
}