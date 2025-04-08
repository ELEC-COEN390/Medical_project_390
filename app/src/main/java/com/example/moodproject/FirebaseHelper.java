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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FirebaseHelper {

    private static FirebaseHelper instance;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Interface for callbacks
    public interface PreferencesCallback {
        void onPreferencesLoaded(UserPreferences preferences);
        void onError(String errorMessage);
    }

    public interface TypeCallback {
        void onTypesLoaded(UserType Type);
        void onError(String errorMessage);
    }

    // Interface for emotion data callbacks
    public interface EmotionDataCallback {
        void onEmotionDataLoaded(Map<String, int[]> emotionData);
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
                                new UserPreferences(false, false, false, false, false, false);

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
                        UserType type = dataSnapshot.exists() ?
                                dataSnapshot.getValue(UserType.class) :
                                new UserType();

                        callback.onTypesLoaded(type);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError(databaseError.getMessage());
                    }
                });
    }

    // Get emotion data for a specific date
    public void getEmotionData(Calendar date, EmotionDataCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not authenticated");
            return;
        }

        // Format date as YYYY-MM-DD for Firebase path
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = dateFormat.format(date.getTime());

        mDatabase.child("emotion_data").child(userId).child(dateString)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, int[]> emotionData = new HashMap<>();

                        // Initialize with empty arrays
                        String[] emotions = {"neutral", "calm", "happy", "sad",
                                "angry", "fearful", "disgust", "surprised"};
                        for (String emotion : emotions) {
                            emotionData.put(emotion, new int[24]);
                        }

                        // If data exists, fill in the values
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot emotionSnapshot : dataSnapshot.getChildren()) {
                                String emotion = emotionSnapshot.getKey();
                                if (emotionData.containsKey(emotion)) {
                                    for (DataSnapshot hourSnapshot : emotionSnapshot.getChildren()) {
                                        try {
                                            int hour = Integer.parseInt(hourSnapshot.getKey());
                                            Long intensityLong = hourSnapshot.getValue(Long.class);
                                            int intensity = intensityLong != null ? intensityLong.intValue() : 0;

                                            if (hour >= 0 && hour < 24) {
                                                emotionData.get(emotion)[hour] = intensity;
                                            }
                                        } catch (NumberFormatException e) {
                                            // Skip this entry if hour is not a valid number
                                            continue;
                                        }
                                    }
                                }
                            }
                        }

                        callback.onEmotionDataLoaded(emotionData);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError(databaseError.getMessage());
                    }
                });
    }

    // Save emotion data for a specific date
    public Task<Void> saveEmotionData(Calendar date, String emotion, int hour, int intensity) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        // Format date as YYYY-MM-DD for Firebase path
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = dateFormat.format(date.getTime());

        // If intensity is 0, remove the data
        if (intensity == 0) {
            return mDatabase.child("emotion_data").child(userId)
                    .child(dateString).child(emotion).child(String.valueOf(hour)).removeValue();
        } else {
            return mDatabase.child("emotion_data").child(userId)
                    .child(dateString).child(emotion).child(String.valueOf(hour)).setValue(intensity);
        }
    }

    public Task<Void> saveUserType(UserType type) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

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