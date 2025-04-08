package com.example.moodproject;

import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class History extends AppCompatActivity {

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
        MaterialButton btnAddEmotion = findViewById(R.id.btn_add_emotion);

        // Initialize date handling
        currentDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        updateDateDisplay();

        // Set up navigation buttons
        findViewById(R.id.btn_previous_day).setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, -1);
            updateDateDisplay();
            loadEmotionData();
        });

        findViewById(R.id.btn_next_day).setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
            updateDateDisplay();
            loadEmotionData();
        });

        // Set up add emotion button
        btnAddEmotion.setOnClickListener(v -> showAddEmotionDialog());

        // Initialize emotion data
        loadEmotionData();

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
     * Load emotion data from Firebase or initialize with empty data
     */
    private void loadEmotionData() {
        // Initialize with empty data first
        emotionData = new HashMap<>();
        for (String emotion : emotions) {
            emotionData.put(emotion, new int[24]);
        }

        // Load from Firebase
        FirebaseHelper.getInstance().getEmotionData(currentDate, new FirebaseHelper.EmotionDataCallback() {
            @Override
            public void onEmotionDataLoaded(Map<String, int[]> data) {
                emotionData = data;
                refreshEmotionData();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(History.this, "Error loading data: " + errorMessage, Toast.LENGTH_SHORT).show();
                refreshEmotionData(); // Refresh with the empty data we initialized above
            }
        });
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
                hourBlock.setOnClickListener(v ->
                        showEditEmotionDialog(finalEmotion, finalHour, intensities[finalHour]));
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
     * Show dialog to add a new emotion
     */
    private void showAddEmotionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Record New Emotion");

        // Inflate custom layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_emotion, null);
        builder.setView(view);

        // Get references to views
        Slider timeSlider = view.findViewById(R.id.time_slider);
        TextView timeValueText = view.findViewById(R.id.time_value);

        // Set up time slider
        timeSlider.setValue(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        updateTimeText(timeValueText, (int) timeSlider.getValue());

        timeSlider.addOnChangeListener((slider, value, fromUser) ->
                updateTimeText(timeValueText, (int) value));

        // Set dialog buttons
        builder.setPositiveButton("Next", (dialog, which) -> {
            int hour = (int) timeSlider.getValue();
            showEmotionSelectionDialog(hour);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    /**
     * Show dialog to select which emotion to record
     */
    private void showEmotionSelectionDialog(int hour) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Emotion");

        // Format emotion names for display
        String[] displayEmotions = new String[emotions.length];
        for (int i = 0; i < emotions.length; i++) {
            displayEmotions[i] = emotions[i].substring(0, 1).toUpperCase() +
                    emotions[i].substring(1);
        }

        builder.setItems(displayEmotions, (dialog, which) -> {
            String selectedEmotion = emotions[which];
            showIntensitySelectionDialog(selectedEmotion, hour);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    /**
     * Show dialog to select emotion intensity
     */
    private void showIntensitySelectionDialog(String emotion, int hour) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Intensity");

        // Find the color for this emotion
        int colorIndex = 0;
        for (int i = 0; i < emotions.length; i++) {
            if (emotions[i].equals(emotion)) {
                colorIndex = i;
                break;
            }
        }

        // Inflate custom layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_intensity, null);
        builder.setView(view);

        // Set up intensity slider
        Slider intensitySlider = view.findViewById(R.id.intensity_slider);
        TextView intensityLabel = view.findViewById(R.id.intensity_label);
        View intensityPreview = view.findViewById(R.id.intensity_preview);

        // Initially set to current value if there is one
        intensitySlider.setValue(emotionData.get(emotion)[hour]);

        int finalColorIndex = colorIndex;
        updateIntensityUI(intensityLabel, intensityPreview, (int) intensitySlider.getValue(),
                ContextCompat.getColor(this, emotionColors[finalColorIndex]));

        intensitySlider.addOnChangeListener((slider, value, fromUser) -> {
            updateIntensityUI(intensityLabel, intensityPreview, (int) value,
                    ContextCompat.getColor(this, emotionColors[finalColorIndex]));
        });

        // Set dialog buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            int intensity = (int) intensitySlider.getValue();

            // Save to Firebase
            FirebaseHelper.getInstance().saveEmotionData(currentDate, emotion, hour, intensity)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update local data
                            emotionData.get(emotion)[hour] = intensity;
                            refreshEmotionData();

                            String formattedTime = String.format(Locale.getDefault(), "%d:00 %s",
                                    hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour),
                                    hour >= 12 ? "PM" : "AM");

                            String intensityText = getIntensityText(intensity);
                            String emotionDisplay = emotion.substring(0, 1).toUpperCase() + emotion.substring(1);

                            if (intensity > 0) {
                                Toast.makeText(History.this,
                                        intensityText + " " + emotionDisplay + " recorded at " + formattedTime,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(History.this,
                                        emotionDisplay + " cleared at " + formattedTime,
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(History.this,
                                    "Failed to save data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    /**
     * Show dialog to edit an existing emotion entry
     */
    private void showEditEmotionDialog(String emotion, int hour, int currentIntensity) {
        String emotionDisplay = emotion.substring(0, 1).toUpperCase() + emotion.substring(1);
        String formattedTime = String.format(Locale.getDefault(), "%d:00 %s",
                hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour),
                hour >= 12 ? "PM" : "AM");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(emotionDisplay + " at " + formattedTime);

        String[] options;
        if (currentIntensity > 0) {
            options = new String[]{"Edit intensity", "Clear"};
        } else {
            options = new String[]{"Add " + emotionDisplay.toLowerCase()};
        }

        builder.setItems(options, (dialog, which) -> {
            if (currentIntensity > 0 && which == 0) {
                // Edit intensity
                showIntensitySelectionDialog(emotion, hour);
            } else if (currentIntensity > 0 && which == 1) {
                // Clear
                FirebaseHelper.getInstance().saveEmotionData(currentDate, emotion, hour, 0)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                emotionData.get(emotion)[hour] = 0;
                                refreshEmotionData();
                                Toast.makeText(History.this,
                                        emotionDisplay + " cleared at " + formattedTime,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(History.this,
                                        "Failed to clear data",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else if (currentIntensity == 0) {
                // Add emotion
                showIntensitySelectionDialog(emotion, hour);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
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

    /**
     * Get text representation of intensity value
     */
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
}