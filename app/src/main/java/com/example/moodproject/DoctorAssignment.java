package com.example.moodproject;

public class DoctorAssignment {
    private String doctorId;
    private String patientId;
    private String doctorEmail;
    private String patientEmail;



    // Required empty constructor for Firebase
    public DoctorAssignment() {
    }

    public DoctorAssignment(String doctorId, String patientId, String doctorEmail, String patientEmail) {
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.doctorEmail = doctorEmail;
        this.patientEmail = patientEmail;
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

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

}