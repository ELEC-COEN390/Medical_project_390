package com.example.moodproject;

import java.util.Date;

public class DoctorAssignment {
    private String doctorId;
    private String patientId;
    private String doctorName;
    private String patientName;



    // Required empty constructor for Firebase
    public DoctorAssignment() {
    }

    public DoctorAssignment(String doctorId, String patientId, String doctorName, String patientName) {
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.doctorName = doctorName;
        this.patientName = patientName;
    }

    // Getters and setters
    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

}