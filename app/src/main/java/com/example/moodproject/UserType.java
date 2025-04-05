package com.example.moodproject;

public class UserType {

    public UserType(){}
    private String type;

    public UserType(Boolean type) {
        if(!type) {
            this.type = "doctor";
        }else{
            this.type = "patient";
        }
    }

    public UserType(String type) {
        if(type.equals("doctor")) {
            this.type = "doctor";
        }else{
            this.type = "patient";
        }
    }

    public String getType() {
        return type;
    }

    public void setType(Boolean type) {
        if(!type) {
            this.type = "doctor";
        }else{
            this.type = "patient";
        }
    }

}
