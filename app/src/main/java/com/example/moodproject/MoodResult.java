package com.example.moodproject;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodResult extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseDatabase database;

    private VideoView videoBackground;
    private ImageView moodDisplay;
    private TextView moodResult;
    private TextView moodAccuracy;
    private Button backButton;
    private ImageButton mood1;
    private ImageButton mood2;
    private ImageButton mood3;

    // Map to store mood accuracies
    private Map<String, Double> moodAccuracies = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mood_result);
        makeFullScreen();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        moodDisplay = findViewById(R.id.mooddisplay);
        moodResult = findViewById(R.id.moodresult);
        moodAccuracy = findViewById(R.id.moodaccuracy);
        backButton = findViewById(R.id.button);
        videoBackground = findViewById(R.id.videoBackground);
        mood1 = findViewById(R.id.mood1);
        mood2 = findViewById(R.id.mood2);
        mood3 = findViewById(R.id.mood3);

        setupVideoBackground();
        processDetectionResults();

        backButton.setOnClickListener(v -> finish());
    }

    private void processDetectionResults() {
        FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
        String userId = getIntent().getStringExtra("USER_ID");

        if (userId == null || userId.isEmpty()) {
            userId = firebaseHelper.getCurrentUserId();
        }

        if (userId != null && !userId.isEmpty()) {
            firebaseHelper.getPredictions(new FirebaseHelper.EmotionDataCallback() {
                @Override
                public void onEmotionsLoaded(Map<String, Float> emotions) {
                    if (emotions != null && !emotions.isEmpty()) {
                        for (Map.Entry<String, Float> entry : emotions.entrySet()) {
                            moodAccuracies.put(entry.getKey(), entry.getValue() * 100.0);
                        }
                        updateMoodUI();
                    } else {
                        fallbackToIntentExtras();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e("MoodResult", "Error loading emotion data: " + errorMessage);
                    fallbackToIntentExtras();
                }
            });
        } else {
            fallbackToIntentExtras();
        }
    }

    private void updateMoodUI() {
        List<String> top3Moods = getTopThreeMoods();

        if (!top3Moods.isEmpty()) {
            String mainMood = top3Moods.get(0);
            moodResult.setText(mainMood);
            Glide.with(this).load(getMoodGif(mainMood)).into(moodDisplay);
            moodAccuracy.setText(getAccuracy(mainMood) + "%");

            mood1.setImageResource(getMoodImage(top3Moods.get(0)));
            mood2.setImageResource(getMoodImage(top3Moods.get(1)));
            mood3.setImageResource(getMoodImage(top3Moods.get(2)));

            mood1.setOnClickListener(v -> {
                String mood = top3Moods.get(0);
                moodResult.setText(mood);
                Glide.with(MoodResult.this).load(getMoodGif(mood)).into(moodDisplay);
                moodAccuracy.setText(getAccuracy(mood) + "%");
            });

            mood2.setOnClickListener(v -> {
                String mood = top3Moods.get(1);
                moodResult.setText(mood);
                Glide.with(MoodResult.this).load(getMoodGif(mood)).into(moodDisplay);
                moodAccuracy.setText(getAccuracy(mood) + "%");
            });

            mood3.setOnClickListener(v -> {
                String mood = top3Moods.get(2);
                moodResult.setText(mood);
                Glide.with(MoodResult.this).load(getMoodGif(mood)).into(moodDisplay);
                moodAccuracy.setText(getAccuracy(mood) + "%");
            });
        }
    }

    private List<String> getTopThreeMoods() {
        List<String> sortedMoods = new ArrayList<>(moodAccuracies.keySet());
        sortedMoods.sort((m1, m2) -> Double.compare(moodAccuracies.get(m2), moodAccuracies.get(m1)));

        List<String> top3 = new ArrayList<>();
        for (int i = 0; i < Math.min(3, sortedMoods.size()); i++) {
            top3.add(sortedMoods.get(i));
        }

        while (top3.size() < 3) {
            top3.add("neutral");
        }

        return top3;
    }

    private String getAccuracy(String mood) {
        Double accuracy = moodAccuracies.get(mood.toLowerCase());
        if (accuracy == null) {
            return "0";
        }
        return String.format("%.1f", accuracy);
    }

    private int getMoodImage(String mood) {
        switch (mood.toLowerCase()) {
            case "happy": return R.drawable.slightly_smiling_face_1f642;
            case "sad": return R.drawable.crying_face_1f622;
            case "fearful": return R.drawable.fearful_face_1f628;
            case "angry": return R.drawable.pouting_face_1f621;
            case "surprised": return R.drawable.hushed_face_1f62f;
            case "neutral": return R.drawable.neutral_face_1f610;
            case "disgusted": return R.drawable.nauseated_face_1f922;
            case "calm": return R.drawable.relieved_face_1f60c;
            default: return R.drawable.neutral_face_1f610;
        }
    }

    private int getMoodGif(String mood) {
        switch (mood.toLowerCase()) {
            case "happy": return R.drawable.slightly_smiling_face_1f642gif;
            case "sad": return R.drawable.crying_face_1f622gif;
            case "fearful": return R.drawable.fearful_face_1f628gif;
            case "angry": return R.drawable.pouting_face_1f621gif;
            case "surprised": return R.drawable.hushed_face_1f62fgif;
            case "neutral": return R.drawable.neutral_face_1f610gif;
            case "disgusted": return R.drawable.nauseated_face_1f922gif;
            case "calm": return R.drawable.relieved_face_1f60cgif;
            default: return R.drawable.neutral_face_1f610gif;
        }
    }

    private void setupVideoBackground() {
        try {
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/raw/wave");
            videoBackground.setVideoURI(videoUri);

            videoBackground.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                mp.setVolume(0, 0);
                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            });

            videoBackground.start();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing video background", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoBackground != null && !videoBackground.isPlaying()) {
            videoBackground.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoBackground != null && videoBackground.isPlaying()) {
            videoBackground.pause();
        }
    }

    private void fallbackToIntentExtras() {
        Intent intent = getIntent();
        if (intent.hasExtra("DETECTED_EMOTION")) {
            String[] emotionLabels = {"angry", "calm", "disgust", "fearful", "happy", "neutral", "sad", "surprised"};
            for (String emotion : emotionLabels) {
                String key = "EMOTION_" + emotion.toUpperCase();
                if (intent.hasExtra(key)) {
                    double value = intent.getFloatExtra(key, 0f);
                    moodAccuracies.put(emotion, value);
                }
            }

            if (moodAccuracies.isEmpty()) {
                String dominantEmotion = intent.getStringExtra("DETECTED_EMOTION");
                float confidence = intent.getFloatExtra("CONFIDENCE", 50f);

                if (dominantEmotion != null && !dominantEmotion.isEmpty()) {
                    moodAccuracies.put(dominantEmotion, (double) confidence);
                    if (!dominantEmotion.equals("neutral")) {
                        moodAccuracies.put("neutral", 30.0);
                    } else {
                        moodAccuracies.put("calm", 30.0);
                    }

                    if (!dominantEmotion.equals("happy") && !moodAccuracies.containsKey("happy")) {
                        moodAccuracies.put("happy", 20.0);
                    } else if (!dominantEmotion.equals("calm") && !moodAccuracies.containsKey("calm")) {
                        moodAccuracies.put("calm", 20.0);
                    }
                }
            }
        }

        if (moodAccuracies.isEmpty()) {
            moodAccuracies.put("neutral", 50.0);
            moodAccuracies.put("happy", 30.0);
            moodAccuracies.put("calm", 20.0);
        }

        updateMoodUI();
    }
}
