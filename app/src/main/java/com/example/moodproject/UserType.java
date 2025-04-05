package com.example.moodproject;

public class UserType {
    private String type;

    // Required empty constructor for Firebase
    public UserType() {
        this.type = "";
    }

    // Constructor with type parameter
    public UserType(String type) {
        this.type = type;
    }

    // Getter and setter
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}