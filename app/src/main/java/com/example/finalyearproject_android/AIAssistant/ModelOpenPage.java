package com.example.finalyearproject_android.AIAssistant;

public class ModelOpenPage {
    private String page;
    private boolean isActivity;
    private Object activity;

    public ModelOpenPage(String page, boolean isActivity, Object activity) {
        this.page = page;
        this.isActivity = isActivity;
        this.activity = activity;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public boolean isActivity() {
        return isActivity;
    }

    public void setActivity(boolean activity) {
        isActivity = activity;
    }

    public Object getActivity() {
        return activity;
    }

    public void setActivity(Object activity) {
        this.activity = activity;
    }
}
