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
     */
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

        // 1. Bind Table Number (using GroupID as Table ID for this demo)
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

        // Add timing status from Scheduler logic
        String timingStatus = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            timingStatus = DishPriorityScheduler.getStartTimingStatus(dish, LocalDateTime.now());
        }
        instructions.append("• Timing: ").append(timingStatus);

        holder.tvInstructions.setText(instructions.toString());

        // 5. Dynamic Styling based on Scheduler Priority/Timing
        String statusColor = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            statusColor = DishPriorityScheduler.getTimingStatusColor(dish, LocalDateTime.now());
        }
        applyStatusColor(holder, statusColor);

        // 6. Handle Done Button
        holder.btnDone.setOnClickListener(v -> {
            // In a real app, this would notify the backend
            dish.setDone(true);
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                prioritizedDishes.remove(currentPos);
                notifyItemRemoved(currentPos);
            }
        });
    }

    private void applyStatusColor(OrderViewHolder holder, String status) {
        // Customize colors based on the status returned by the Scheduler
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