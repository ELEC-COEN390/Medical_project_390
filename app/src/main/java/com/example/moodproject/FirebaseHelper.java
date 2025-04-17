package com.example.moodproject;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {

    private static FirebaseHelper instance;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Reference to doctor_assignments node in Firebase
    private static final String DOCTOR_ASSIGNMENTS = "doctor_assignments";

    // Reference to unmatched_patients node in Firebase
    private static final String UNMATCHED_PATIENTS = "unmatched_patients";

    // Interface for callbacks
    public interface PreferencesCallback {
        void onPreferencesLoaded(UserPreferences preferences);
        void onError(String errorMessage);

    }

    public interface TypeCallback{
        void onTypesLoaded(UserType Type);
        void onError(String errorMessage);
    }

    public interface NameCallback{
        void onNameLoaded(String name);
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

    // Add this interface to FirebaseHelper.java
    public interface EmotionDataCallback {
        void onEmotionsLoaded(Map<String, Float> emotions);
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

    public Task<Void> saveUserName(String name){
        return mDatabase.child("users").child(getCurrentUserId()).child("name").setValue(name);
    }

    public void getUserName(NameCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        mDatabase.child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Check if the value is a string
                            if (snapshot.getValue() instanceof String) {
                                String nameStr = snapshot.getValue(String.class);
                                callback.onNameLoaded(nameStr);
                            } else {
                                callback.onNameLoaded("");
                            }
                        } else {
                            callback.onNameLoaded("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public void getPredictions(EmotionDataCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        mDatabase.child("prediction").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            try {
                                // Try to convert the data to a Map<String, Float>
                                Map<String, Float> emotions = new HashMap<>();

                                for (DataSnapshot emotionSnapshot : snapshot.getChildren()) {
                                    String emotion = emotionSnapshot.getKey();
                                    Float value = emotionSnapshot.getValue(Float.class);
                                    if (emotion != null && value != null) {
                                        emotions.put(emotion, value);
                                    }
                                }

                                callback.onEmotionsLoaded(emotions);
                            } catch (Exception e) {
                                callback.onError("Error parsing emotion data: " + e.getMessage());
                            }
                        } else {
                            callback.onEmotionsLoaded(new HashMap<>());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public Task<Void> savePredictions(Map<String, Float> emotions){

        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        return mDatabase.child("prediction").child(userId).setValue(emotions);
    }

    public Task<Void> saveUserPreferences(UserPreferences preferences) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        return mDatabase.child("user_preferences").child(userId).setValue(preferences);
    }

    /**
     * Fetch all patients assigned to the current doctor
     */
    public void getMatchedPatients(PatientsListCallback callback) {
        String doctorId = getCurrentUserId();
        if (doctorId == null) {
            callback.onError("User not authenticated");
            return;
        }

        // Query doctor_assignments where doctorId equals current user's ID
        Query query = mDatabase.child(DOCTOR_ASSIGNMENTS).orderByChild("doctorId").equalTo(doctorId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<DoctorAssignment> patients = new ArrayList<>();

                for (DataSnapshot assignmentSnapshot : dataSnapshot.getChildren()) {
                    DoctorAssignment assignment = assignmentSnapshot.getValue(DoctorAssignment.class);
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

    /**
     * Fetch all unmatched patients (patients without assigned doctors)
     */
    public void getUnmatchedPatients(PatientsListCallback callback) {
        String doctorId = getCurrentUserId();
        if (doctorId == null) {
            callback.onError("User not authenticated");
            return;
        }

        // Query unmatched_patients node for all patients without doctors
        mDatabase.child(UNMATCHED_PATIENTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<DoctorAssignment> unmatchedPatients = new ArrayList<>();

                for (DataSnapshot patientSnapshot : dataSnapshot.getChildren()) {
                    DoctorAssignment patient = new DoctorAssignment();
                    patient.setPatientId(patientSnapshot.getKey());

                    // Check if patient data includes email
                    if (patientSnapshot.hasChild("email")) {
                        patient.setPatientEmail(patientSnapshot.child("email").getValue(String.class));
                    }

                    unmatchedPatients.add(patient);
                }

                callback.onPatientsLoaded(unmatchedPatients);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    /**
     * Assign a patient to the current doctor
     */
    public Task<Void> assignPatientToDoctor(String patientId, String patientEmail) {
        String doctorId = getCurrentUserId();
        if (doctorId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        // Get doctor email for the assignment
        final String[] doctorEmail = {""};
        mDatabase.child("users").child(doctorId).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    doctorEmail[0] = dataSnapshot.getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        // Create a new assignment
        String assignmentKey = mDatabase.child(DOCTOR_ASSIGNMENTS).push().getKey();
        DoctorAssignment assignment = new DoctorAssignment(
                doctorId,
                patientId,
                doctorEmail[0],
                patientEmail
        );

        // Add to assignments and remove from unmatched
        mDatabase.child(DOCTOR_ASSIGNMENTS).child(assignmentKey).setValue(assignment);
        return mDatabase.child(UNMATCHED_PATIENTS).child(patientId).removeValue();
    }

    public Task<Void> addToUnmatchedPatients() {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("User not authenticated");
        }

        String patientId = user.getUid();
        String patientEmail = user.getEmail();

        // Create a new entry in the unmatched_patients node
        return mDatabase.child("unmatched_patients").child(patientId)
                .child("email").setValue(patientEmail);
    }

    public Task<Void> saveUserTypeAndHandlePatient(UserType type) {
        // First save the user type
        Task<Void> saveTypeTask = saveUserType(type);

        // If the user is a patient, add them to unmatched patients list
        if (type.getType().equals("patient")) {
            return saveTypeTask.continueWithTask(task -> {
                // Only proceed if the first task was successful
                if (task.isSuccessful()) {
                    return addToUnmatchedPatients();
                } else {
                    // If there was an error saving the user type, propagate that error
                    throw task.getException();
                }
            });
        } else {
            // For doctors, just return the original task
            return saveTypeTask;
        }
    }

    public void signOut() {
        mAuth.signOut();
    }
}