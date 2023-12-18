package com.example.finalyearproject_android.DietPlan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject_android.DietSug.Food;
import com.example.finalyearproject_android.DietSug.SearchAdapter;
import com.example.finalyearproject_android.R;

import java.util.List;

public class AdapterFood extends RecyclerView.Adapter<AdapterFood.ViewHolder> {
    Context context;
    List<Food> foodList;

    public AdapterFood(Context context, List<Food> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public AdapterFood.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.food_list_view,parent,false);
        return new AdapterFood.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterFood.ViewHolder holder, int position) {
        holder.setData(foodList, position);
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView foodTitle, foodCalories;
        ImageView deleteIcon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foodTitle = itemView.findViewById(R.id.food_title_text);
            foodCalories = itemView.findViewById(R.id.food_calories_total);
            deleteIcon = itemView.findViewById(R.id.food_delete_icon);
        }

        @SuppressLint("SetTextI18n")
        public void setData(List<Food> foodList, int position) {
            foodTitle.setText(foodList.get(position).getFoodName() +" x "+foodList.get(position).getQty());
            foodCalories.setText(""+(foodList.get(position).getCalories() * foodList.get(position).getQty()));
            deleteIcon.setOnClickListener(view -> {
                //food database delete
                DietDatabase d = new DietDatabase(context);
                d.removeDiet(foodList.get(position));
            });
        }
    }
}
