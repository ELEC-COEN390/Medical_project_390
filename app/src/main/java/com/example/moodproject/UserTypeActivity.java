package com.example.moodproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.net.Uri;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        doctorCard.setOnClickListener(v -> {
            saveUserTypeToDatabase("Doctor");
            Intent intent = new Intent(UserTypeActivity.this,DoctorDashboard.class);
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


    private void saveUserTypeToDatabase(String type) {
        // TODO: Replace with actual Firebase logic
        Toast.makeText(this, "Selected: " + type, Toast.LENGTH_SHORT).show();

        // Example: Navigate or store in Firebase
        // You can also call FirebaseAuth.getInstance().getCurrentUser().getUid()
        // and store the user type in Realtime Database or Firestore here.
    }
}
