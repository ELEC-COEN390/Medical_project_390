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


import java.util.Arrays;
import java.util.List;



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

        List<String> top3Moods= getTopThreeMoods();
        if (!top3Moods.isEmpty()) {
            moodResult.setText(top3Moods.get(0));
        }

        String mainMood=top3Moods.get(0);
        moodResult.setText(mainMood);
        moodDisplay.setImageResource(getMoodGif(mainMood));
        mood1.setImageResource(getMoodImage(top3Moods.get(0)));
        mood2.setImageResource(getMoodImage(top3Moods.get(1)));
        mood3.setImageResource(getMoodImage(top3Moods.get(2)));


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

    }
    private List<String> getTopThreeMoods() {
        // For the algorithm
        //Side note there is a function at the bottom to get the accuracy of the mood
        //please make a link between it and this function for the deep learning so this function
        //returns the names and the other returns the accuracy
        return Arrays.asList("happy", "sad", "fearful"); //temporary example for me to use for building the code
    }
    private int getMoodImage(String mood){
        switch (mood.toLowerCase()){
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
    private int getMoodGif(String mood){
        switch (mood.toLowerCase()){
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
    private int getMoodAccuracy(String mood){
        double accuracy=getTopThreeMoods().getAccuracy();
        return (int)accuracy;
    }

}