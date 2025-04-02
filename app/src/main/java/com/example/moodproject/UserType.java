package com.example.moodproject;

public class UserType {

    public UserType(){}
    private String type;

    public UserType(Boolean type) {
        if(!type) {
            this.type = "Doctor";
        }else{
            this.type = "Patient";
        }
    }

    public String getType() {
        return type;
    }

    public void setType(Boolean type) {
        if(!type) {
            this.type = "Doctor";
        }else{
            this.type = "Patient";
        }
    }

}
