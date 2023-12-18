package com.example.finalyearproject_android.AIAssistant;

public class ModelMessage {
    private String id, text, timeStamp;
    private boolean userMessage;

    public ModelMessage() {
    }

    public boolean isUserMessage() {
        return userMessage;
    }

    public void setUserMessage(boolean userMessage) {
        this.userMessage = userMessage;
    }

    public ModelMessage(String id, String text, String timeStamp, boolean userMessage) {
        this.id = id;
        this.text = text;
        this.timeStamp = timeStamp;
        this.userMessage = userMessage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
