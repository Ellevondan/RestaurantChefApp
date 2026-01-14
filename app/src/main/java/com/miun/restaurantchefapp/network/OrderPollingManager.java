package com.miun.restaurantchefapp.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.miun.restaurantchefapp.models.OrderBundle;
import java.util.List;

/**
 * Hanterar automatisk polling av aktiva ordrar
 * Uppdaterar orderlistor med jämna intervall
 */
public class OrderPollingManager {

    private static final String TAG = "OrderPollingManager";
    private static final long POLLING_INTERVAL_MS = 5000; // 5 sekunder

    private final ApiRepository repository;
    private final Handler handler;
    private final Runnable pollingRunnable;
    private boolean isPolling = false;
    private OrderUpdateListener listener;

    public interface OrderUpdateListener {
        void onOrdersUpdated(List<OrderBundle> orders);
        void onError(String errorMessage);
    }

    public OrderPollingManager(ApiRepository repository) {
        this.repository = repository;
        this.handler = new Handler(Looper.getMainLooper());

        this.pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPolling) {
                    fetchOrders();
                    handler.postDelayed(this, POLLING_INTERVAL_MS);
                }
            }
        };
    }

    /**
     * Startar automatisk polling
     */
    public void startPolling(OrderUpdateListener listener) {
        this.listener = listener;
        if (!isPolling) {
            isPolling = true;
            Log.d(TAG, "Startar polling av ordrar");
            handler.post(pollingRunnable);
        }
    }

    /**
     * Stoppar automatisk polling
     */
    public void stopPolling() {
        if (isPolling) {
            isPolling = false;
            handler.removeCallbacks(pollingRunnable);
            Log.d(TAG, "Stoppar polling av ordrar");
        }
    }

    /**
     * Tvingar en omedelbar uppdatering
     */
    public void forceUpdate() {
        if (listener != null) {
            fetchOrders();
        }
    }

    private void fetchOrders() {
        repository.getActiveOrders(new ApiCallback<List<OrderBundle>>() {
            @Override
            public void onSuccess(List<OrderBundle> result) {
                if (listener != null) {
                    listener.onOrdersUpdated(result);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Fel vid polling: " + errorMessage);
                if (listener != null) {
                    listener.onError(errorMessage);
                }
            }
        });
    }

    public boolean isPolling() {
        return isPolling;
    }
}