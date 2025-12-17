package com.miun.restaurantchefapp;

import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.miun.restaurantchefapp.models.Dish;
import com.miun.restaurantchefapp.models.PrioritizedDish;
import com.miun.restaurantchefapp.scheduling.DishPriorityScheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class KitchenOrderAdapter extends RecyclerView.Adapter<KitchenOrderAdapter.OrderViewHolder> {

    private final List<PrioritizedDish> prioritizedDishes;

    public KitchenOrderAdapter(List<PrioritizedDish> prioritizedDishes) {
        this.prioritizedDishes = new ArrayList<>(prioritizedDishes);
    }

    public void updateData(List<PrioritizedDish> newDishes) {
        this.prioritizedDishes.clear();
        this.prioritizedDishes.addAll(newDishes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kitchen_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        PrioritizedDish item = prioritizedDishes.get(position);
        Dish dish = item.getDish();
        int tableId = item.getParentBundle().getGroupID();

        // 1. Bind Header Info (Table & Type)
        holder.tvTableNumber.setText("TABLE " + tableId);
        holder.tvCourseType.setText(item.getParentBundle().getCourseType().getDisplayName().toUpperCase());

        // 2. Set Header Color based on Table ID (Critique #1)
        applyTableColor(holder, tableId);

        // 3. Bind Dish Name
        holder.tvDishName.setText(dish.getName());

        // 4. Bind Extra Info (Allergens, Instructions, Timing)
        StringBuilder details = new StringBuilder();

        // Timing (Critique #3 - keeping timing info as it's useful "extra info")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String timingStatus = DishPriorityScheduler.getStartTimingStatus(dish, LocalDateTime.now());
            if (timingStatus != null) {
                details.append("⏱️ ").append(timingStatus).append("\n");
            }
        }

        // Special Instructions
        if (dish.getSpecialInstructions() != null && !dish.getSpecialInstructions().isEmpty()) {
            details.append("📝 ").append(dish.getSpecialInstructions()).append("\n");
        }

        // Allergens
        if (dish.getSelectedAllergens() != null && !dish.getSelectedAllergens().isEmpty()) {
            details.append("⚠️ Allergies: ").append(dish.getSelectedAllergens()).append("\n");
        }

        holder.tvInstructions.setText(details.toString());

        // 5. Handle Done Button
        holder.btnDone.setOnClickListener(v -> {
            dish.setDone(true);
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                prioritizedDishes.remove(currentPos);
                notifyItemRemoved(currentPos);
            }
        });
    }

    /**
     * Generates a unique-ish color for a given table ID so all items
     * for that table look visually distinct.
     */
    private void applyTableColor(OrderViewHolder holder, int tableId) {
        // Use a Golden Angle approximation to generate distinct colors for numbers 1, 2, 3...
        // This ensures Table 1 is always one color, Table 2 another, etc.
        float hue = (tableId * 137.508f) % 360;
        int color = Color.HSVToColor(new float[]{hue, 0.6f, 0.85f});

        holder.headerContainer.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return prioritizedDishes.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTableNumber, tvCourseType, tvDishName, tvInstructions;
        LinearLayout headerContainer;
        Button btnDone;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTableNumber = itemView.findViewById(R.id.tv_table_number);
            tvCourseType = itemView.findViewById(R.id.tv_course_type);
            tvDishName = itemView.findViewById(R.id.tv_dish_name);
            tvInstructions = itemView.findViewById(R.id.tv_instructions);
            headerContainer = itemView.findViewById(R.id.header_container);
            btnDone = itemView.findViewById(R.id.btn_done);
        }
    }
}