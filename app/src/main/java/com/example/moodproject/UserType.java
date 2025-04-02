package com.example.moodproject;

// UserType class that matches Firebase rules (string-based)
public class UserType {
    private String type;

    // Required empty constructor for Firebase
    public UserType() {
        // Default value
        this.type = "patient";
    }

    public UserType(String type) {
        this.type = type.toLowerCase();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type.toLowerCase();
    }

    public boolean isDoctor() {
        return "doctor".equals(type);
    }

    public boolean isPatient() {
        return "patient".equals(type);
    }
}
