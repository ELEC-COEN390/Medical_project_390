package com.example.moodproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.EditText;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AlertDialog;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DoctorDashboard extends AppCompatActivity
 {

    TextView textViewDoctorName;
    Button buttonRefresh;
    RecyclerView recyclerViewPatients;
    FloatingActionButton fabAddPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctordashboard_activity);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Optional: Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            WindowInsetsCompat bars = insets;
            v.setPadding(
                    bars.getSystemGestureInsets().left,
                    bars.getSystemGestureInsets().top,
                    bars.getSystemGestureInsets().right,
                    bars.getSystemGestureInsets().bottom
            );
            return insets;
        });

        // Initialize UI components
        textViewDoctorName = findViewById(R.id.textViewDoctorName);
        buttonRefresh = findViewById(R.id.buttonRefresh);
        recyclerViewPatients = findViewById(R.id.recyclerViewPatients);
        fabAddPatient = findViewById(R.id.fabAddPatient);

        // Welcome message
        textViewDoctorName.setText("Welcome, Dr. Smith");

        // Refresh patient list
        buttonRefresh.setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing patient list...", Toast.LENGTH_SHORT).show();
            // TODO: Fetch unmatched patients from Firebase
        });

        // FAB opens popup menu
        fabAddPatient.setOnClickListener(v -> showCustomActionDialog());

    }


    private void showPopupMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.doctordashboard_add_patient, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_add_patient) {
                Toast.makeText(this, "Add Patient selected", Toast.LENGTH_SHORT).show();
                // TODO: Launch add patient screen or dialog
                return true;
            } else if (id == R.id.menu_scan_qr) {
                Toast.makeText(this, "Scan QR Code selected", Toast.LENGTH_SHORT).show();
                // TODO: Launch QR scanner
                return true;
            }
            return false;
        });

        popup.show();
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

}