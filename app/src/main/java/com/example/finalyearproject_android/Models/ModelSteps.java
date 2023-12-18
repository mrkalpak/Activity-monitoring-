package com.example.finalyearproject_android.Models;

public class ModelSteps {
    private String date, steps;

    public ModelSteps() {
    }

    public ModelSteps(String date, String steps) {
        this.date = date;
        this.steps = steps;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }
}
