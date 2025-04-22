package com.example.moodproject;

import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class historyActivity extends AppCompatActivity {

    private TextView tvCurrentDate;
    private Calendar currentDate;
    private SimpleDateFormat dateFormat;

    // Map to store emotion data
    private Map<String, int[]> emotionData;

    // List of emotions
    private final String[] emotions = {
            "neutral", "calm", "happy", "sad",
            "angry", "fearful", "disgust", "surprised"
    };

    // Corresponding colors for each emotion
    private final int[] emotionColors = {
            R.color.gray, R.color.teal, R.color.yellow, R.color.blue,
            R.color.red, R.color.purple, R.color.green, R.color.orange
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize UI components
        tvCurrentDate = findViewById(R.id.tv_current_date);

        // Enable full screen mode
        makeFullScreen();

        // Initialize date handling
        currentDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        updateDateDisplay();

        // Set up navigation buttons
        findViewById(R.id.btn_previous_day).setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, -1);
            updateDateDisplay();
            refreshEmotionData();
        });

        findViewById(R.id.btn_next_day).setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
            updateDateDisplay();
            refreshEmotionData();
        });

        // Initialize emotion data
        initializeEmotionData();

        // Load initial emotion UI
        refreshEmotionData();

        // Set up synchronized scrolling
        setupSynchronizedScrolling();
    }

    private void setupSynchronizedScrolling() {
        // Get references to all scroll views

        SynchronizedScrollView timeHeaderScroll = findViewById(R.id.time_header_scroll);
        SynchronizedScrollView neutralScroll = findViewById(R.id.neutral_scroll_view);
        SynchronizedScrollView calmScroll = findViewById(R.id.calm_scroll_view);
        SynchronizedScrollView happyScroll = findViewById(R.id.happy_scroll_view);
        SynchronizedScrollView sadScroll = findViewById(R.id.sad_scroll_view);
        SynchronizedScrollView angryScroll = findViewById(R.id.angry_scroll_view);
        SynchronizedScrollView fearfulScroll = findViewById(R.id.fearful_scroll_view);
        SynchronizedScrollView disgustScroll = findViewById(R.id.disgust_scroll_view);
        SynchronizedScrollView surprisedScroll = findViewById(R.id.surprised_scroll_view);

        // Create list of all scroll views
        List<SynchronizedScrollView> allScrollViews = new ArrayList<>();
        allScrollViews.add(timeHeaderScroll);
        allScrollViews.add(neutralScroll);
        allScrollViews.add(calmScroll);
        allScrollViews.add(happyScroll);
        allScrollViews.add(sadScroll);
        allScrollViews.add(angryScroll);
        allScrollViews.add(fearfulScroll);
        allScrollViews.add(disgustScroll);
        allScrollViews.add(surprisedScroll);

        // Set all scroll views to synchronize with each other
        for (SynchronizedScrollView scrollView : allScrollViews) {
            scrollView.setSynchronizedScrollViews(allScrollViews);
        }
    }

    /**
     * Initialize sample emotion data for demonstration purposes
     */
    private void initializeEmotionData() {
        emotionData = new HashMap<>();

        // For each emotion, create an array of 24 intensity values (0-3)
        // 0 = none, 1 = low, 2 = medium, 3 = high
        for (String emotion : emotions) {
            int[] intensities = new int[24];
            emotionData.put(emotion, intensities);
        }

        // Add some sample data
        // Joy in the morning
        // Add some sample data
// Happy in the morning
        emotionData.get("happy")[8] = 2;
        emotionData.get("happy")[9] = 3;
        emotionData.get("happy")[10] = 2;

// Angry in the afternoon
        emotionData.get("angry")[14] = 1;
        emotionData.get("angry")[15] = 2;

// Sad in the evening
        emotionData.get("sad")[20] = 2;
        emotionData.get("sad")[21] = 3;

// Fearful at night
        emotionData.get("fearful")[2] = 2;

// Surprised in the afternoon
        emotionData.get("surprised")[13] = 3;

// Calm throughout the day
        emotionData.get("calm")[7] = 1;
        emotionData.get("calm")[12] = 2;
        emotionData.get("neutral")[17] = 2;
    }

    /**
     * Update the date display text
     */
    private void updateDateDisplay() {
        tvCurrentDate.setText(dateFormat.format(currentDate.getTime()));
    }

    /**
     * Refresh all emotion timelines based on current data
     */
    private void refreshEmotionData() {
        for (int i = 0; i < emotions.length; i++) {
            String emotion = emotions[i];
            LinearLayout timelineLayout = findViewById(getResources().getIdentifier(
                    emotion + "_timeline", "id", getPackageName()));

            // Clear existing views
            timelineLayout.removeAllViews();

            // Get intensity data for this emotion
            int[] intensities = emotionData.get(emotion);

            // Create 24 hour blocks
            for (int hour = 0; hour < 24; hour++) {
                View hourBlock = createHourBlock(intensities[hour], emotionColors[i]);
                timelineLayout.addView(hourBlock);

                // Make the block clickable to edit
                final int finalHour = hour;
                final String finalEmotion = emotion;

            }
        }
    }

    /**
     * Create a visual block representing one hour's emotion intensity
     */
    private View createHourBlock(int intensity, int colorResId) {
        View hourBlock = new View(this);

        // Get the screen density scale
        float density = getResources().getDisplayMetrics().density;
// Convert 40dp to pixels
        int hourBlockWidthPx = (int) (40 * density);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                hourBlockWidthPx,
                ViewGroup.LayoutParams.MATCH_PARENT);

        params.setMargins(1, 0, 1, 0);
        hourBlock.setLayoutParams(params);
        // Set the color based on intensity
        int color = ContextCompat.getColor(this, colorResId);

        if (intensity == 0) {
            // No emotion
            hourBlock.setBackgroundResource(R.drawable.hour_block_empty);
        } else {
            // Create a drawable with the emotion color at appropriate opacity
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);

            float alpha = 0.3f;
            switch (intensity) {
                case 1: // Low
                    alpha = 0.3f;
                    break;
                case 2: // Medium
                    alpha = 0.6f;
                    break;
                case 3: // High
                    alpha = 1.0f;
                    break;
            }

            // Apply the alpha to the color
            int alphaInt = (int) (alpha * 255);
            int colorWithAlpha = (color & 0x00FFFFFF) | (alphaInt << 24);

            drawable.setColor(colorWithAlpha);
            drawable.setStroke(1, color);
            hourBlock.setBackground(drawable);
        }

        return hourBlock;
    }



    /**
     * Update the time text based on slider value
     */
    private void updateTimeText(TextView textView, int hour) {
        String formattedTime = String.format(Locale.getDefault(), "%d:00 %s",
                hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour),
                hour >= 12 ? "PM" : "AM");
        textView.setText(formattedTime);
    }

    /**
     * Update intensity UI elements based on selected intensity
     */
    private void updateIntensityUI(TextView textView, View previewView, int intensity, int color) {
        String intensityText = getIntensityText(intensity);
        textView.setText(intensityText);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);

        if (intensity == 0) {
            // No emotion
            drawable.setColor(ContextCompat.getColor(this, R.color.background_dark));
            drawable.setStroke(1, ContextCompat.getColor(this, R.color.white_30));
        } else {
            // Apply the appropriate alpha
            float alpha = 0.3f;
            switch (intensity) {
                case 1: // Low
                    alpha = 0.3f;
                    break;
                case 2: // Medium
                    alpha = 0.6f;
                    break;
                case 3: // High
                    alpha = 1.0f;
                    break;
            }

            // Apply the alpha to the color
            int alphaInt = (int) (alpha * 255);
            int colorWithAlpha = (color & 0x00FFFFFF) | (alphaInt << 24);

            drawable.setColor(colorWithAlpha);
            drawable.setStroke(1, color);
        }

        previewView.setBackground(drawable);
    }

    private String getIntensityText(int intensity) {
        switch (intensity) {
            case 0:
                return "None";
            case 1:
                return "Low";
            case 2:
                return "Medium";
            case 3:
                return "High";
            default:
                return "Unknown";
        }
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
}