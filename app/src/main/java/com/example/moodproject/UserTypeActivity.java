package com.example.moodproject;

import android.content.Intent;
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

import android.media.MediaPlayer;

public class UserTypeActivity extends AppCompatActivity {

    private CardView doctorCard;
    private CardView patientCard;
    private VideoView videoBackground;

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

        // Initialize VideoView
        videoBackground = findViewById(R.id.videoBackground);
        setupVideoBackground();

        doctorCard.setOnClickListener(v -> {
            saveUserTypeToDatabase("Doctor");
            Intent intent = new Intent(UserTypeActivity.this, Dashboard.class);
            startActivity(intent);
            finish();
        });

        patientCard.setOnClickListener(v -> {
            saveUserTypeToDatabase("Patient");
            Intent intent = new Intent(UserTypeActivity.this, PreferencesActivity.class);
            startActivity(intent);
            finish();
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
            // Set the video path - make sure 'wave.mp4' is in your raw folder
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.wave;
            videoBackground.setVideoURI(Uri.parse(videoPath));

            // Set video properties
            videoBackground.setOnPreparedListener(mp -> {
                // Loop the video
                mp.setLooping(true);

                // Mute the video
                mp.setVolume(0.0f, 0.0f);

                // Scale the video to fill the view
                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            });

            // Start playing the video
            videoBackground.start();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing video background", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        makeFullScreen();
        // Resume video playback when activity comes back to foreground
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

    private void saveUserTypeToDatabase(String type) {
        // TODO: Replace with actual Firebase logic
        Toast.makeText(this, "Selected: " + type, Toast.LENGTH_SHORT).show();

        // Example: Navigate or store in Firebase
        // You can also call FirebaseAuth.getInstance().getCurrentUser().getUid()
        // and store the user type in Realtime Database or Firestore here.
    }
}