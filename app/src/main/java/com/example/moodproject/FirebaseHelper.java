package com.example.moodproject;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {

    private static FirebaseHelper instance;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Interface for callbacks
    public interface PreferencesCallback {
        void onPreferencesLoaded(UserPreferences preferences);
        void onError(String errorMessage);
    }

    public interface TypeCallback{
        void onTypesLoaded(UserType Type);
        void onError(String errorMessage);
    }

    public interface DoctorAssignmentCallback {
        void onAssignmentComplete(boolean success);
        void onError(String errorMessage);
    }

    public interface PatientsListCallback {
        void onPatientsLoaded(List<DoctorAssignment> patients);
        void onError(String errorMessage);
    }

    public interface DoctorsListCallback {
        void onDoctorsLoaded(List<DoctorAssignment> doctors);
        void onError(String errorMessage);
    }


    private FirebaseHelper() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public void getUserPreferences(PreferencesCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        mDatabase.child("user_preferences").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserPreferences preferences = dataSnapshot.exists() ?
                                dataSnapshot.getValue(UserPreferences.class) :
                                new UserPreferences(false, false, false, false, false,false);

                        callback.onPreferencesLoaded(preferences);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError(databaseError.getMessage());
                    }
                });
    }

    public void getUserType(TypeCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        mDatabase.child("user_type").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Check if the value is a string
                            if (dataSnapshot.getValue() instanceof String) {
                                String typeStr = dataSnapshot.getValue(String.class);
                                UserType type = new UserType(typeStr);
                                callback.onTypesLoaded(type);
                            } else {
                                // Try to get it as UserType object
                                UserType type = dataSnapshot.getValue(UserType.class);
                                if (type == null) {
                                    type = new UserType();
                                }
                                callback.onTypesLoaded(type);
                            }
                        } else {
                            callback.onTypesLoaded(new UserType());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError(databaseError.getMessage());
                    }
                });
    }

    public Task<Void> saveUserType(UserType type) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        // Save as UserType object instead of just the string
        return mDatabase.child("user_type").child(userId).setValue(type);
    }

    public Task<Void> saveUserPreferences(UserPreferences preferences) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        return mDatabase.child("user_preferences").child(userId).setValue(preferences);
    }

    // Assign a doctor to a patient
    public void assignDoctorToPatient(String patientId, String patientName,
                                      String doctorId, String doctorName,
                                      DoctorAssignmentCallback callback) {
        DoctorAssignment assignment = new DoctorAssignment(doctorId, patientId, doctorName, patientName);

        // Create the relationship in both directions
        mDatabase.child("doctor_patients").child(doctorId).child(patientId).setValue(assignment)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mDatabase.child("patient_doctors").child(patientId).child(doctorId).setValue(assignment)
                                .addOnCompleteListener(innerTask -> {
                                    if (innerTask.isSuccessful()) {
                                        callback.onAssignmentComplete(true);
                                    } else {
                                        callback.onError("Failed to create patient-doctor relationship: "
                                                + innerTask.getException().getMessage());
                                    }
                                });
                    } else {
                        callback.onError("Failed to create doctor-patient relationship: "
                                + task.getException().getMessage());
                    }
                });
    }

    // Get all patients for a doctor
    public void getDoctorPatients(PatientsListCallback callback) {
        String doctorId = getCurrentUserId();
        if (doctorId == null) {
            callback.onError("User not authenticated");
            return;
        }

        mDatabase.child("doctor_patients").child(doctorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<DoctorAssignment> patients = new ArrayList<>();
                        for (DataSnapshot patientSnapshot : dataSnapshot.getChildren()) {
                            DoctorAssignment assignment = patientSnapshot.getValue(DoctorAssignment.class);
                            if (assignment != null) {
                                patients.add(assignment);
                            }
                        }
                        callback.onPatientsLoaded(patients);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError(databaseError.getMessage());
                    }
                });
    }

    // Get the doctor for a patient
    public void getPatientDoctors(DoctorsListCallback callback) {
        String patientId = getCurrentUserId();
        if (patientId == null) {
            callback.onError("User not authenticated");
            return;
        }

        mDatabase.child("patient_doctors").child(patientId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<DoctorAssignment> doctors = new ArrayList<>();
                        for (DataSnapshot doctorSnapshot : dataSnapshot.getChildren()) {
                            DoctorAssignment assignment = doctorSnapshot.getValue(DoctorAssignment.class);
                            if (assignment != null) {
                                doctors.add(assignment);
                            }
                        }
                        callback.onDoctorsLoaded(doctors);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError(databaseError.getMessage());
                    }
                });
    }

    // Remove a doctor-patient relationship
    public Task<Void> removeDoctorPatientRelationship(String patientId, String doctorId) {
        // Remove from both sides
        Task<Void> doctorSide = mDatabase.child("doctor_patients").child(doctorId).child(patientId).removeValue();
        Task<Void> patientSide = mDatabase.child("patient_doctors").child(patientId).child(doctorId).removeValue();

        // Use Tasks.whenAll() (not Task.whenAll())
        return Tasks.whenAll(doctorSide, patientSide);
    }

    public void signOut() {
        mAuth.signOut();
    }
}