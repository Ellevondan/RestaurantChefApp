package com.miun.restaurantchefapp.utils;

import android.annotation.SuppressLint;
import android.os.Build;

import com.miun.restaurantchefapp.models.Dish;
import com.miun.restaurantchefapp.models.OrderBundle;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MockJsonResponse {

    // Suppress the "NewApi" warning because we enabled Library Desugaring in build.gradle
    @SuppressLint("NewApi")
    public static List<OrderBundle> getMockData() {
        List<OrderBundle> bundles = new ArrayList<>();

        // We can now safely use LocalDateTime because of desugaring
        LocalDateTime now = LocalDateTime.now();

        // --- TABLE 1 (Rush Order) ---
        OrderBundle b1 = new OrderBundle();
        b1.setGroupID(1);
        b1.setModifyType("RUSH");

        Dish d1 = new Dish();
        d1.setName("Tap Water");
        d1.setActiveTime(1);
        d1.setWaitingTime(0);
        d1.setOrderedAt(now.minusMinutes(1)); // Ordered 1 min ago

        List<Dish> orders1 = new ArrayList<>();
        orders1.add(d1);
        b1.setOrders(orders1);
        bundles.add(b1);


        // --- TABLE 2 (Mains) ---
        OrderBundle b2 = new OrderBundle();
        b2.setGroupID(2);
        b2.setModifyType("MAIN");

        Dish d2 = new Dish();
        d2.setName("Grilled Salmon");
        d2.setSelectedAllergens("Fish");
        d2.setSpecialInstructions("Sauce on side");
        d2.setActiveTime(12);
        d2.setWaitingTime(5);
        d2.setOrderedAt(now.minusMinutes(15)); // Ordered 15 min ago

        Dish d3 = new Dish();
        d3.setName("Caesar Salad");
        d3.setSelectedAllergens("Dairy, Gluten");
        d3.setActiveTime(8);
        d3.setWaitingTime(0);
        d3.setOrderedAt(now.minusMinutes(15));

        List<Dish> orders2 = new ArrayList<>();
        orders2.add(d2);
        orders2.add(d3);
        b2.setOrders(orders2);
        bundles.add(b2);


        // --- TABLE 5 (Appetizers) ---
        OrderBundle b3 = new OrderBundle();
        b3.setGroupID(5);
        b3.setModifyType("APPETIZER");

        Dish d4 = new Dish();
        d4.setName("Garlic Bread");
        d4.setActiveTime(5);
        d4.setWaitingTime(8);
        d4.setOrderedAt(now.minusMinutes(5));

        List<Dish> orders3 = new ArrayList<>();
        orders3.add(d4);
        b3.setOrders(orders3);
        bundles.add(b3);

        return bundles;
    }
}