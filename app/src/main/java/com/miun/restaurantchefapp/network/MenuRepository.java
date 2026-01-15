package com.miun.restaurantchefapp.network;

import android.util.Log;

import com.miun.restaurantchefapp.models.CarteMenuItem;
import com.miun.restaurantchefapp.models.Dish;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for managing the menu cache.
 * Fetches the carte menu from the API and caches it for fast lookups.
 * Used to populate timing information (activeTime, waitingTime) for dishes in orders.
 */
public class MenuRepository {
    private static final String TAG = "MenuRepository";
    private static MenuRepository instance;

    private final KitchenApiService apiService;
    private final Map<Long, CarteMenuItem> menuCache;
    private boolean isMenuLoaded = false;
    private boolean isLoading = false;

    private MenuRepository() {
        this.apiService = ApiClient.getKitchenApiService();
        this.menuCache = new HashMap<>();
    }

    /**
     * Get singleton instance of MenuRepository
     */
    public static synchronized MenuRepository getInstance() {
        if (instance == null) {
            instance = new MenuRepository();
        }
        return instance;
    }

    /**
     * Check if the menu has been loaded
     */
    public boolean isMenuLoaded() {
        return isMenuLoaded;
    }

    /**
     * Get the number of items in the menu cache
     */
    public int getMenuSize() {
        return menuCache.size();
    }

    /**
     * Fetch the menu from the API and cache it
     */
    public void fetchMenu(MenuFetchCallback callback) {
        if (isLoading) {
            Log.d(TAG, "Menu fetch already in progress, skipping");
            return;
        }

        isLoading = true;
        Log.d(TAG, "Fetching menu from API...");

        Call<List<CarteMenuItem>> call = apiService.getActiveCarteMenu();
        call.enqueue(new Callback<List<CarteMenuItem>>() {
            @Override
            public void onResponse(Call<List<CarteMenuItem>> call, Response<List<CarteMenuItem>> response) {
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    List<CarteMenuItem> menuItems = response.body();
                    Log.d(TAG, "Menu fetched successfully: " + menuItems.size() + " items");

                    // Clear old cache and rebuild
                    menuCache.clear();
                    for (CarteMenuItem item : menuItems) {
                        if (item.getId() != null) {
                            menuCache.put(item.getId(), item);
                            Log.d(TAG, "Cached menu item: " + item.getName() +
                                    " (ID: " + item.getId() +
                                    ", activeTime: " + item.getActiveTime() +
                                    ", waitingTime: " + item.getWaitingTime() + ")");
                        }
                    }

                    isMenuLoaded = true;

                    if (callback != null) {
                        callback.onSuccess(menuItems.size());
                    }
                } else {
                    Log.e(TAG, "Failed to fetch menu. Response code: " + response.code());
                    if (callback != null) {
                        callback.onError("Failed to fetch menu: HTTP " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CarteMenuItem>> call, Throwable t) {
                isLoading = false;
                Log.e(TAG, "Error fetching menu: " + t.getMessage(), t);

                if (callback != null) {
                    callback.onError("Network error: " + t.getMessage());
                }
            }
        });
    }

    /**
     * Look up a menu item by its ID
     */
    public CarteMenuItem getMenuItemById(long id) {
        return menuCache.get(id);
    }

    /**
     * Populate timing information for a dish from the menu cache.
     * Uses the dish's originalID to look up the corresponding menu item.
     *
     * @param dish The dish to populate timing for
     * @return true if timing was successfully populated, false otherwise
     */
    public boolean populateDishTiming(Dish dish) {
        if (dish == null) {
            Log.w(TAG, "Cannot populate timing for null dish");
            return false;
        }

        // Check if dish already has timing information
        if (dish.getActiveTime() > 0 && dish.getWaitingTime() >= 0) {
            Log.d(TAG, "Dish '" + dish.getName() + "' already has timing information");
            return true;
        }

        // Look up menu item by originalID
        int originalID = dish.getOriginalID();
        CarteMenuItem menuItem = menuCache.get((long) originalID);

        if (menuItem == null) {
            Log.w(TAG, "No menu item found for dish '" + dish.getName() +
                    "' with originalID: " + originalID);
            return false;
        }

        // Check if menu item has timing information
        if (!menuItem.hasTimingInformation()) {
            Log.w(TAG, "Menu item '" + menuItem.getName() +
                    "' (ID: " + menuItem.getId() + ") has no timing information");
            return false;
        }

        // Populate timing from menu
        dish.setActiveTime(menuItem.getActiveTime());
        dish.setWaitingTime(menuItem.getWaitingTime());

        Log.d(TAG, "Populated timing for dish '" + dish.getName() +
                "' from menu item ID " + originalID +
                ": activeTime=" + menuItem.getActiveTime() +
                ", waitingTime=" + menuItem.getWaitingTime());

        return true;
    }

    /**
     * Populate timing information for all dishes in a list
     *
     * @param dishes List of dishes to populate timing for
     * @return Number of dishes successfully populated
     */
    public int populateDishesTiming(List<Dish> dishes) {
        if (dishes == null || dishes.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Dish dish : dishes) {
            if (populateDishTiming(dish)) {
                successCount++;
            }
        }

        Log.d(TAG, "Populated timing for " + successCount + " out of " + dishes.size() + " dishes");
        return successCount;
    }

    /**
     * Clear the menu cache
     */
    public void clearCache() {
        menuCache.clear();
        isMenuLoaded = false;
        Log.d(TAG, "Menu cache cleared");
    }

    /**
     * Callback interface for menu fetch operations
     */
    public interface MenuFetchCallback {
        void onSuccess(int itemCount);
        void onError(String errorMessage);
    }
}
