package com.example.moodproject;

public class Patient{
    private String id;
    private String email;


    public Patient(String id, String email) {
        this.id = id;
        this.email = email;
    }

    public String getId() { return id; }
    public String getEmail() { return email; }


}
