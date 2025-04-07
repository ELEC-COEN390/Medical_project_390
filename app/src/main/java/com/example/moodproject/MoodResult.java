package com.example.moodproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodResult extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseDatabase database;
    private ImageView moodDisplay;
    private TextView moodResult;
    private TextView moodAccuracy;
    private Button backButton;
    private ImageView mood1;
    private ImageView mood2;
    private ImageView mood3;

    // Map to store mood accuracies
    private Map<String, Double> moodAccuracies = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mood_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        moodDisplay = findViewById(R.id.mooddisplay);
        moodResult = findViewById(R.id.moodresult);
        moodAccuracy = findViewById(R.id.moodaccuracy);
        backButton = findViewById(R.id.button);

        mood1 = findViewById(R.id.mood1);
        mood2 = findViewById(R.id.mood2);
        mood3 = findViewById(R.id.mood3);

        // Process the mood detection results
        processDetectionResults();

        // Get the top three moods with their accuracies
        List<String> top3Moods = getTopThreeMoods();

        if (!top3Moods.isEmpty()) {
            String mainMood = top3Moods.get(0);
            moodResult.setText(mainMood);
            moodDisplay.setImageResource(getMoodGif(mainMood));
            moodAccuracy.setText(getAccuracy(mainMood) + "%");

            mood1.setImageResource(getMoodImage(top3Moods.get(0)));
            mood2.setImageResource(getMoodImage(top3Moods.get(1)));
            mood3.setImageResource(getMoodImage(top3Moods.get(2)));
        }

        // Set click listeners for mood selection
        mood1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mood = top3Moods.get(0);
                moodResult.setText(mood);
                moodDisplay.setImageResource(getMoodGif(mood));
                moodAccuracy.setText(getAccuracy(mood) + "%");
            }
        });

        mood2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mood = top3Moods.get(1);
                moodResult.setText(mood);
                moodDisplay.setImageResource(getMoodGif(mood));
                moodAccuracy.setText(getAccuracy(mood) + "%");
            }
        });

        mood3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mood = top3Moods.get(2);
                moodResult.setText(mood);
                moodDisplay.setImageResource(getMoodGif(mood));
                moodAccuracy.setText(getAccuracy(mood) + "%");
            }
        });

        // Set back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to the previous screen
            }
        });
    }

    /**
     * Process the mood detection results from your algorithm.
     * This is where you would integrate with your deep learning model.
     */
    private void processDetectionResults() {
        // TODO: Replace this with actual integration with your mood detection algorithm

        // Example: These would come from your mood detection model
        moodAccuracies.put("happy", 85.7);
        moodAccuracies.put("sad", 75.2);
        moodAccuracies.put("fearful", 62.9);
        moodAccuracies.put("angry", 45.3);
        moodAccuracies.put("surprised", 38.1);
        moodAccuracies.put("neutral", 32.5);
        moodAccuracies.put("disgusted", 28.9);
        moodAccuracies.put("calm", 21.4);
    }

    /**
     * Returns the top three moods based on accuracy values.
     * @return List of the top three mood names
     */
    private List<String> getTopThreeMoods() {
        // For a real implementation, sort the moodAccuracies map by value (descending)
        // and take the top 3 keys

        // This is a simplified example - in a real app, sort the map by values
        List<String> sortedMoods = new ArrayList<>(moodAccuracies.keySet());
        sortedMoods.sort((m1, m2) -> Double.compare(moodAccuracies.get(m2), moodAccuracies.get(m1)));

        // Take only the top 3
        List<String> top3 = new ArrayList<>();
        for (int i = 0; i < Math.min(3, sortedMoods.size()); i++) {
            top3.add(sortedMoods.get(i));
        }

        // If we have fewer than 3 moods, fill with default values
        while (top3.size() < 3) {
            top3.add("neutral");
        }

        return top3;
    }

    /**
     * Gets the accuracy for a given mood.
     * @param mood The mood name
     * @return The accuracy as an integer percentage
     */
    private String getAccuracy(String mood) {
        Double accuracy = moodAccuracies.get(mood.toLowerCase());
        if (accuracy == null) {
            return "0";
        }
        return String.format("%.1f", accuracy);
    }

    /**
     * Returns the appropriate image resource for the mood.
     */
    private int getMoodImage(String mood) {
        switch (mood.toLowerCase()) {
            case "happy":
                return R.drawable.slightly_smiling_face_1f642;
            case "sad":
                return R.drawable.crying_face_1f622;
            case "fearful":
                return R.drawable.fearful_face_1f628;
            case "angry":
                return R.drawable.pouting_face_1f621;
            case "surprised":
                return R.drawable.hushed_face_1f62f;
            case "neutral":
                return R.drawable.neutral_face_1f610;
            case "disgusted":
                return R.drawable.nauseated_face_1f922;
            case "calm":
                return R.drawable.relieved_face_1f60c;
            default:
                return R.drawable.neutral_face_1f610;
        }
    }

    /**
     * Returns the appropriate GIF resource for the mood.
     */
    private int getMoodGif(String mood) {
        switch (mood.toLowerCase()) {
            case "happy":
                return R.drawable.slightly_smiling_face_1f642gif;
            case "sad":
                return R.drawable.crying_face_1f622gif;
            case "fearful":
                return R.drawable.fearful_face_1f628gif;
            case "angry":
                return R.drawable.pouting_face_1f621gif;
            case "surprised":
                return R.drawable.hushed_face_1f62fgif;
            case "neutral":
                return R.drawable.neutral_face_1f610gif;
            case "disgusted":
                return R.drawable.nauseated_face_1f922gif;
            case "calm":
                return R.drawable.relieved_face_1f60cgif;
            default:
                return R.drawable.neutral_face_1f610gif;
        }
    }
}