//The Adapter is the engine that powers the RecyclerView
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
        // Create a copy of the list to avoid external modification issues
        this.prioritizedDishes = new ArrayList<>(prioritizedDishes);
    }

    /**
     * Updates the list of dishes and notifies the recycler view.
     * Call this when the priority schedule updates (e.g. every minute).
     * A helper method we added to swap the data list safely and tell
     * the UI to repaint (notifyDataSetChanged).
     */
    public void updateData(List<PrioritizedDish> newDishes) {
        this.prioritizedDishes.clear();
        this.prioritizedDishes.addAll(newDishes);
        notifyDataSetChanged();
    }

    //loads the XML layout for a single row and creates a Java object to hold references to it.
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kitchen_order, parent, false);
        return new OrderViewHolder(view);
    }

    /**
    *the most critical loop. It runs every time a row needs to appear on screen.
    *1-It takes a PrioritizedDish object from your list.
    *2-It finds the corresponding UI widgets (using the View Holder).
    *3-It sets the text (setText) and changes colors (setBackgroundColor).
    */
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        PrioritizedDish item = prioritizedDishes.get(position);
        Dish dish = item.getDish();

        // 1. Bind Table Number
        holder.tvTableNumber.setText("TABLE " + item.getParentBundle().getGroupID());

        // 2. Bind Course Type
        holder.tvCourseType.setText(item.getParentBundle().getCourseType().getDisplayName().toUpperCase());

        // 3. Bind Dish Name
        holder.tvDishName.setText(dish.getName());

        // 4. Bind Instructions (Combine allergens and special instructions)
        StringBuilder instructions = new StringBuilder();
        if (dish.getSpecialInstructions() != null && !dish.getSpecialInstructions().isEmpty()) {
            instructions.append("• ").append(dish.getSpecialInstructions()).append("\n");
        }
        if (dish.getSelectedAllergens() != null && !dish.getSelectedAllergens().isEmpty()) {
            instructions.append("• Allergies: ").append(dish.getSelectedAllergens()).append("\n");
        }

        // Add timing status safely
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String timingStatus = DishPriorityScheduler.getStartTimingStatus(dish, LocalDateTime.now());
            if (timingStatus != null) {
                instructions.append("• Timing: ").append(timingStatus);
            }
        }

        holder.tvInstructions.setText(instructions.toString());

        // 5. Dynamic Styling (Fixes potential NullPointerException)
        String statusColor = "normal"; // Default value
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String calculatedColor = DishPriorityScheduler.getTimingStatusColor(dish, LocalDateTime.now());
            if (calculatedColor != null) {
                statusColor = calculatedColor;
            }
        }
        applyStatusColor(holder, statusColor);

        // 6. Handle Done Button
        holder.btnDone.setOnClickListener(v -> {
            dish.setDone(true);
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                prioritizedDishes.remove(currentPos);
                notifyItemRemoved(currentPos);
            }
        });
    }

    private void applyStatusColor(OrderViewHolder holder, String status) {
        // Safe check for null, though we set a default above
        if (status == null) status = "normal";

        switch (status) {
            case "urgent":
                holder.headerContainer.setBackgroundColor(Color.parseColor("#D32F2F")); // Red
                break;
            case "soon":
                holder.headerContainer.setBackgroundColor(Color.parseColor("#FBC02D")); // Yellow
                break;
            default:
                holder.headerContainer.setBackgroundColor(Color.parseColor("#800880")); // Default Purple
                break;
        }
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