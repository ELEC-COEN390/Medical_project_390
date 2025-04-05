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

    public void signOut() {
        mAuth.signOut();
    }
}