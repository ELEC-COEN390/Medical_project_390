package com.example.moodproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DoctorDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TextView textViewDoctorName;
    Button buttonRefresh;
    RecyclerView recyclerViewPatients;
    FloatingActionButton fabAddPatient;

    private FirebaseAuth mAuth;
    // Navigation Drawer components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctordashboard_activity);

        // Initialize UI components
        textViewDoctorName = findViewById(R.id.textViewDoctorName);
        buttonRefresh = findViewById(R.id.buttonRefresh);
        recyclerViewPatients = findViewById(R.id.recyclerViewPatients);
        fabAddPatient = findViewById(R.id.fabAddPatient);

        // Set up toolbar and navigation drawer
        setupNavigationDrawer();

        // Welcome message
        textViewDoctorName.setText("Welcome, Dr. Smith");

        // Refresh patient list
        buttonRefresh.setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing patient list...", Toast.LENGTH_SHORT).show();
            // TODO: Fetch unmatched patients from Firebase
        });

        // FAB opens popup menu
        fabAddPatient.setOnClickListener(v -> showCustomActionDialog());

        // Optional: Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contentContainer), (v, insets) -> {
            WindowInsetsCompat bars = insets;
            v.setPadding(
                    bars.getSystemGestureInsets().left,
                    bars.getSystemGestureInsets().top,
                    bars.getSystemGestureInsets().right,
                    bars.getSystemGestureInsets().bottom
            );
            return insets;
        });
    }

    private void setupNavigationDrawer() {
        // Initialize components
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Setup the toggle button
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup navigation item click listener
        navigationView.setNavigationItemSelectedListener(this);
    }

    // Handle navigation item clicks
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on home screen, just close drawer
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
            // Launch settings activity
            // Intent intent = new Intent(this, SettingsActivity.class);
            // startActivity(intent);
        } else if (id == R.id.nav_history) {
            Toast.makeText(this, "Recording History", Toast.LENGTH_SHORT).show();
            // Launch history activity
            // Intent intent = new Intent(this, HistoryActivity.class);
            // startActivity(intent);
        } else if (id == R.id.nav_wifi_settings) {
            Toast.makeText(this, "WiFi Settings", Toast.LENGTH_SHORT).show();
            // Open WiFi settings
            startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
        } else if (id == R.id.nav_connection) {
            Toast.makeText(this, "Connection Settings", Toast.LENGTH_SHORT).show();
            // Show connection dialog or activity
        } else if (id == R.id.nav_about) {
            Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
            // Show about dialog
            showAboutDialog();
        } else if (id == R.id.nav_logout) {
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
            // Show help dialog or activity

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();

            finish();
        }

        // Close the drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showCustomActionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_action, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        Button btnAdd = dialogView.findViewById(R.id.buttonAddPatient);
        Button btnScan = dialogView.findViewById(R.id.buttonScanQR);

        btnAdd.setOnClickListener(v -> {
            dialog.dismiss();
            showAddPatientDialog(); // call the input dialog you already built
        });

        btnScan.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Scan QR Code selected", Toast.LENGTH_SHORT).show();
            String qrData = "Patient ID: 12345\nName: John Doe\nAge: 30"; // Replace with real patient data if needed
            showQRDisplayDialog(qrData);
        });

        dialog.show();
    }

    private void showAddPatientDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_patient, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        EditText editName = dialogView.findViewById(R.id.editPatientName);
        EditText editId = dialogView.findViewById(R.id.editPatientId);
        EditText editAge = dialogView.findViewById(R.id.editPatientAge);
        Button btnCancel = dialogView.findViewById(R.id.buttonCancel);
        Button btnSave = dialogView.findViewById(R.id.buttonSave);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String id = editId.getText().toString().trim();
            String age = editAge.getText().toString().trim();

            if (name.isEmpty() || id.isEmpty() || age.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Combine data into one string (or just use ID)
            String qrData = "Patient ID: " + id + "\nName: " + name + "\nAge: " + age;

            ImageView qrImage = dialogView.findViewById(R.id.imageViewQRCode);

            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400);
                qrImage.setImageBitmap(bitmap);
                qrImage.setVisibility(View.VISIBLE); // Show the QR after generation
            } catch (WriterException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error generating QR", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showQRDisplayDialog(String qrData) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_show_qr, null);
        ImageView qrImage = dialogView.findViewById(R.id.imageViewGeneratedQR);

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 500, 500);
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating QR", Toast.LENGTH_SHORT).show();
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialog.show();
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About Doctor Dashboard");
        builder.setMessage("Version 1.0\n\nThis application allows doctors to manage patient records and generate QR codes for patient identification.");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    // Handle back button press
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}