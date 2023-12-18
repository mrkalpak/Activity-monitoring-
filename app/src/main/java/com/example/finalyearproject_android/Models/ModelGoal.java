package com.example.finalyearproject_android.Models;

public class ModelGoal {
    private String stepsGoal, caloriesGoal;

    public ModelGoal() {
    }

    public ModelGoal(String stepsGoal, String caloriesGoal) {
        this.stepsGoal = stepsGoal;
        this.caloriesGoal = caloriesGoal;
    }

    public String getStepsGoal() {
        return stepsGoal;
    }

    public void setStepsGoal(String stepsGoal) {
        this.stepsGoal = stepsGoal;
    }

    public String getCaloriesGoal() {
        return caloriesGoal;
    }

    public void setCaloriesGoal(String caloriesGoal) {
        this.caloriesGoal = caloriesGoal;
    }
}
