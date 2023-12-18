package com.example.finalyearproject_android.DietPlan;

public class ModelFood {
    private String name, calories;

    public ModelFood() {
    }

    public ModelFood(String name, String calories) {
        this.name = name;
        this.calories = calories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }
}
