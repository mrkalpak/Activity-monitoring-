package com.example.finalyearproject_android.DietSug;

public class Food {
    private String foodName;
    private int calories;
    private String servingSize;
    private int qty;

    public Food(String foodName, int calories, String servingSize) {
        this.foodName = foodName;
        this.calories = calories;
        this.servingSize = servingSize;
    }
    public Food(String foodName, int calories, String servingSize, int qty) {
        this.foodName = foodName;
        this.calories = calories;
        this.servingSize = servingSize;
        this.qty = qty;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public String getServingSize() {
        return servingSize;
    }

    public void setServingSize(String servingSize) {
        this.servingSize = servingSize;
    }
}