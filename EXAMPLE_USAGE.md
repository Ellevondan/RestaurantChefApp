# Example Usage of Chef Priority System

## Complete Integration Example

Here's how to integrate the priority scheduler into your MainActivity:

```java
package com.miun.restaurantchefapp;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.miun.restaurantchefapp.models.Dish;
import com.miun.restaurantchefapp.models.OrderBundle;
import com.miun.restaurantchefapp.models.PrioritizedDish;
import com.miun.restaurantchefapp.scheduling.DishPriorityScheduler;
import com.miun.restaurantchefapp.utils.OrderParser;

import java.time.LocalDateTime;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<OrderBundle> orderBundles;
    private Handler updateHandler;
    private static final int UPDATE_INTERVAL = 60000; // Update every minute

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize update handler
        updateHandler = new Handler();

        // Load orders from backend (this is just example, you'll use your API)
        loadOrdersFromBackend();

        // Start periodic updates
        startPriorityUpdates();
    }

    private void loadOrdersFromBackend() {
        // This is example JSON - replace with actual API call to Payara server
        String jsonResponse = getExampleOrderJson();

        try {
            // Parse JSON using Gson - that's it!
            orderBundles = GsonHelper.parseOrderBundles(jsonResponse);
            updatePriorityDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePriorityDisplay() {
        // Calculate current priority list
        List<PrioritizedDish> priorityList =
            DishPriorityScheduler.calculatePriorityList(orderBundles);

        // Display in UI (you'll create a RecyclerView for this)
        displayPriorityList(priorityList);
    }

    private void displayPriorityList(List<PrioritizedDish> priorityList) {
        LocalDateTime now = LocalDateTime.now();

        // Example: Display top 10 dishes in priority order
        for (int i = 0; i < Math.min(10, priorityList.size()); i++) {
            PrioritizedDish pd = priorityList.get(i);
            Dish dish = pd.getDish();

            String status = DishPriorityScheduler.getStartTimingStatus(dish, now);
            String color = DishPriorityScheduler.getTimingStatusColor(dish, now);

            // TODO: Update your RecyclerView adapter with this data
            System.out.println((i+1) + ". " + dish.getName() +
                             " (x" + dish.getQuantity() + ") - " + status);
        }
    }

    private void startPriorityUpdates() {
        updateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updatePriorityDisplay();
                updateHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        }, UPDATE_INTERVAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateHandler.removeCallbacksAndMessages(null);
    }

    private String getExampleOrderJson() {
        // Example JSON with activeTime and idleTime added
        return "[{" +
            "\"ID\":67," +
            "\"groupID\":42," +
            "\"id\":67," +
            "\"isDone\":false," +
            "\"orders\":[" +
                "{\"comments\":\"\",\"id\":101,\"name\":\"Pizza Margherita\"," +
                "\"orderedAt\":\"2025-12-11T12:34:56\",\"quantity\":2," +
                "\"selectedAllergens\":\"Gluten,Dairy\"," +
                "\"specialInstructions\":\"Extra cheese, cut into squares\"," +
                "\"activeTime\":5,\"idleTime\":15}," +
                "{\"comments\":\"\",\"id\":102,\"name\":\"Caesar Salad\"," +
                "\"orderedAt\":\"2025-12-11T12:36:10\",\"quantity\":1," +
                "\"selectedAllergens\":\"Egg,Dairy\"," +
                "\"specialInstructions\":\"Dressing on the side\"," +
                "\"activeTime\":8,\"idleTime\":0}," +
                "{\"comments\":\"\",\"id\":103,\"name\":\"Vegan Burger\"," +
                "\"orderedAt\":\"2025-12-11T12:37:45\",\"quantity\":3," +
                "\"selectedAllergens\":\"Soy,Gluten\"," +
                "\"specialInstructions\":\"No mayo, extra lettuce\"," +
                "\"activeTime\":7,\"idleTime\":10}," +
                "{\"comments\":\"\",\"id\":104,\"name\":\"Chocolate Cake\"," +
                "\"orderedAt\":\"2025-12-11T12:40:00\",\"quantity\":1," +
                "\"selectedAllergens\":\"Egg,Dairy,Gluten\"," +
                "\"specialInstructions\":\"Birthday message on top\"," +
                "\"activeTime\":10,\"idleTime\":0}" +
            "]" +
        "}]";
    }
}
```

## RecyclerView Adapter Example

Create an adapter to display the priority list:

```java
public class DishPriorityAdapter extends RecyclerView.Adapter<DishPriorityAdapter.ViewHolder> {

    private List<PrioritizedDish> priorityList;
    private OnDishClickListener listener;

    public interface OnDishClickListener {
        void onDishStarted(Dish dish);
        void onDishCompleted(Dish dish);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_dish_priority, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PrioritizedDish pd = priorityList.get(position);
        Dish dish = pd.getDish();
        LocalDateTime now = LocalDateTime.now();

        // Set dish info
        holder.dishName.setText(dish.getName());
        holder.quantity.setText("x" + dish.getQuantity());
        holder.bundleId.setText("Order #" + pd.getParentBundle().getGroupID());

        // Set timing status
        String status = DishPriorityScheduler.getStartTimingStatus(dish, now);
        holder.statusText.setText(status);

        // Set color based on urgency
        String colorCode = DishPriorityScheduler.getTimingStatusColor(dish, now);
        int color;
        switch (colorCode) {
            case "urgent":
                color = Color.parseColor("#FF5252"); // Red
                break;
            case "soon":
                color = Color.parseColor("#FFC107"); // Yellow
                break;
            default:
                color = Color.parseColor("#4CAF50"); // Green
        }
        holder.statusIndicator.setBackgroundColor(color);

        // Set cook times
        holder.prepTime.setText("Prep: " + dish.getActiveTime() + "min");
        holder.cookTime.setText("Cook: " + dish.getIdleTime() + "min");

        // Show special instructions if any
        if (!dish.getSpecialInstructions().isEmpty()) {
            holder.specialInstructions.setVisibility(View.VISIBLE);
            holder.specialInstructions.setText(dish.getSpecialInstructions());
        }

        // Action buttons
        holder.btnStart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDishStarted(dish);
            }
        });

        holder.btnComplete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDishCompleted(dish);
            }
        });
    }

    @Override
    public int getItemCount() {
        return priorityList != null ? priorityList.size() : 0;
    }

    public void updatePriorityList(List<PrioritizedDish> newList) {
        this.priorityList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dishName, quantity, statusText, bundleId;
        TextView prepTime, cookTime, specialInstructions;
        View statusIndicator;
        Button btnStart, btnComplete;

        ViewHolder(View itemView) {
            super(itemView);
            // Initialize views from layout
            dishName = itemView.findViewById(R.id.dish_name);
            quantity = itemView.findViewById(R.id.quantity);
            statusText = itemView.findViewById(R.id.status_text);
            bundleId = itemView.findViewById(R.id.bundle_id);
            prepTime = itemView.findViewById(R.id.prep_time);
            cookTime = itemView.findViewById(R.id.cook_time);
            specialInstructions = itemView.findViewById(R.id.special_instructions);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
            btnStart = itemView.findViewById(R.id.btn_start);
            btnComplete = itemView.findViewById(R.id.btn_complete);
        }
    }
}
```

## Backend Communication Example

Here's how to fetch orders from your Payara backend:

```java
public class OrderService {

    private static final String BASE_URL = "http://your-payara-server:8080/api";

    public void fetchOrders(OrderCallback callback) {
        // Use your preferred HTTP client (OkHttp, Retrofit, etc.)
        // Example with basic HTTP:

        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/orders/active");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse response with Gson
                List<OrderBundle> bundles =
                    GsonHelper.parseOrderBundles(response.toString());

                callback.onSuccess(bundles);

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public interface OrderCallback {
        void onSuccess(List<OrderBundle> bundles);
        void onError(Exception e);
    }
}
```

## Testing the Algorithm

Here's a test case to verify the algorithm works:

```java
@Test
public void testPriorityScheduling() {
    // Create test bundle
    OrderBundle bundle = new OrderBundle();
    bundle.setID(1);
    bundle.setGroupID(42);

    // Create dishes with different cook times
    Dish pizza = new Dish();
    pizza.setName("Pizza");
    pizza.setActiveTime(5);
    pizza.setIdleTime(15); // Total: 20 min
    pizza.setOrderedAt(LocalDateTime.now());

    Dish salad = new Dish();
    salad.setName("Salad");
    salad.setActiveTime(8);
    salad.setIdleTime(0); // Total: 8 min
    salad.setOrderedAt(LocalDateTime.now());

    bundle.getOrders().add(pizza);
    bundle.getOrders().add(salad);

    // Calculate priority
    List<OrderBundle> bundles = new ArrayList<>();
    bundles.add(bundle);

    List<PrioritizedDish> priorityList =
        DishPriorityScheduler.calculatePriorityList(bundles);

    // Pizza should be first (needs to start now)
    assertEquals("Pizza", priorityList.get(0).getDish().getName());

    // Both should finish at same time
    LocalDateTime pizzaFinish = pizza.getScheduledFinishTime();
    LocalDateTime saladFinish = salad.getScheduledFinishTime();
    assertEquals(pizzaFinish, saladFinish);
}
```

## Next Steps

1. **Create the UI Layout** - Design the RecyclerView item layout
2. **Add Real-time Updates** - Connect to Payara server WebSocket for live updates
3. **Add Action Handlers** - Implement start/complete dish functionality
4. **Add Notifications** - Alert chef when dishes need to start
5. **Add Filtering** - Allow chef to filter by bundle, dish type, etc.
