package com.example.moodproject;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;
import android.net.Uri;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class UserTypeActivity extends AppCompatActivity {

    private CardView doctorCard;
    private CardView patientCard;
    private VideoView videoBackground;

    private FirebaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_type);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize CardViews
        doctorCard = findViewById(R.id.doctorCard);
        patientCard = findViewById(R.id.patientCard);
        videoBackground = findViewById(R.id.videoBackground);

        setupVideoBackground();

        db = FirebaseHelper.getInstance();

        doctorCard.setOnClickListener(v -> {
            saveUserTypeToDatabase("doctor");
        });

        patientCard.setOnClickListener(v -> {
            saveUserTypeToDatabase("patient");
        });
    }

    private void saveUserTypeToDatabase(String type) {
        // Show processing message
        Toast.makeText(this, "Processing your selection: " + type, Toast.LENGTH_SHORT).show();

        // Create the UserType object
        UserType userType = new UserType(type);

        // Use the correct method that handles adding patients to unmatched list
        db.saveUserTypeAndHandlePatient(userType)
                .addOnSuccessListener(aVoid -> {
                    // After successful save, navigate based on user type
                    if (type.equals("doctor")) {
                        Intent intent = new Intent(UserTypeActivity.this, DoctorDashboard.class);
                        startActivity(intent);
                    } else { // patient
                        Intent intent = new Intent(UserTypeActivity.this, PreferencesActivity.class);
                        startActivity(intent);
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Show error message if saving fails
                    Toast.makeText(UserTypeActivity.this,
                            "Error saving user type: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void makeFullScreen() {
        // Make the activity full screen
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Hide the status bar and navigation bar
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(),
                getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        // Add these flags for older Android versions
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

    private void setupVideoBackground() {
        try {
            // Path to the video file in raw folder
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/raw/wave");
            videoBackground.setVideoURI(videoUri);

            // Loop the video
            videoBackground.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                mp.setVolume(0, 0); // Mute the video
            });

            // Handle video completion
            videoBackground.setOnCompletionListener(mp -> {
                videoBackground.start(); // Restart the video when it ends
            });

            // Start playing the video
            videoBackground.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-hide system bars when returning to the activity
        makeFullScreen();

        // Resume video playback when activity comes to foreground
        if (videoBackground != null && !videoBackground.isPlaying()) {
            videoBackground.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause video when activity is not visible
        if (videoBackground != null && videoBackground.isPlaying()) {
            videoBackground.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources
        if (videoBackground != null) {
            videoBackground.stopPlayback();
        }
    }
}