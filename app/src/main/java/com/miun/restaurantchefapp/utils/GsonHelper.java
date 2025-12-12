package com.miun.restaurantchefapp.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.miun.restaurantchefapp.models.OrderBundle;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Helper class for JSON parsing with Gson
 * Provides simple methods to parse orders from backend
 */
public class GsonHelper {

    private static Gson gson;

    /**
     * Get configured Gson instance
     */
    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .setPrettyPrinting()  // Makes JSON readable (optional)
                    .create();
        }
        return gson;
    }

    /**
     * Parse JSON string into list of OrderBundles
     *
     * Usage:
     *   String json = // ... from backend
     *   List<OrderBundle> bundles = GsonHelper.parseOrderBundles(json);
     */
    public static List<OrderBundle> parseOrderBundles(String json) {
        Type listType = new TypeToken<List<OrderBundle>>(){}.getType();
        return getGson().fromJson(json, listType);
    }

    /**
     * Parse JSON string into single OrderBundle
     */
    public static OrderBundle parseOrderBundle(String json) {
        return getGson().fromJson(json, OrderBundle.class);
    }

    /**
     * Convert OrderBundle to JSON string (for sending to backend)
     */
    public static String toJson(OrderBundle bundle) {
        return getGson().toJson(bundle);
    }

    /**
     * Convert list of OrderBundles to JSON string
     */
    public static String toJson(List<OrderBundle> bundles) {
        return getGson().toJson(bundles);
    }
}
