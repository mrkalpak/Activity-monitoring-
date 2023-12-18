package com.example.finalyearproject_android.DietSug;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject_android.DietPlan.DietDatabase;
import com.example.finalyearproject_android.R;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyView> {
    List<Food> foodData;
    Context context;

    public SearchAdapter(List<Food> foodData, Context context) {
        this.foodData = foodData;
        this.context = context;
    }

    @NonNull
    @Override
    public SearchAdapter.MyView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.food_list_adapter,parent,false);
        return new MyView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.MyView holder, int position) {
        holder.setData(foodData, position);
    }

    @Override
    public int getItemCount() {
        return foodData.size();
    }

    public class MyView extends RecyclerView.ViewHolder {
        TextView foodTitle, caloriesText, serveText;
        DietDatabase dietDatabase = new DietDatabase(context);
        public MyView(@NonNull View itemView) {
            super(itemView);

            foodTitle = itemView.findViewById(R.id.food_title);
            caloriesText = itemView.findViewById(R.id.calories_text);
            serveText = itemView.findViewById(R.id.serve_text);
        }

        public void setData(List<Food> foodData, int position) {
            foodTitle.setText(foodData.get(position).getFoodName());
            caloriesText.setText(foodData.get(position).getCalories()+"");
            serveText.setText(foodData.get(position).getServingSize());

            itemView.setOnClickListener(view -> {
              showAlert(foodData.get(position));
            });
        }

        void showAlert(Food food){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder( context);
            alertDialog.setTitle("Quantity");
            alertDialog.setMessage("Select Quantity");

            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);

            alertDialog.setPositiveButton("YES",
                    (dialog, which) -> {
                        food.setQty(Integer.parseInt(input.getText().toString()));
                        DietAddActivity.addedFood.add(food);
                        dietDatabase.addDiet(food);
                    });

            alertDialog.setNegativeButton("NO",
                    (dialog, which) -> dialog.cancel());

            alertDialog.show();



        }
    }
}
