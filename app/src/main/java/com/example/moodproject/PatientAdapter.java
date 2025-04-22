package com.example.moodproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private final List<Patient> patientList;
    private OnPatientClickListener onPatientClickListener;

    // Interface for click listener
    public interface OnPatientClickListener {

        // Implement the OnPatientClickListener interface method
        void onPatientClick(Patient patient, int position);
    }

    // Constructor with click listener
    public PatientAdapter(List<Patient> patientList, OnPatientClickListener listener) {
        this.patientList = patientList;
        this.onPatientClickListener = listener;
    }

    // Constructor without click listener for backward compatibility
    public PatientAdapter(List<Patient> patientList) {
        this.patientList = patientList;
    }

    // Set click listener after initialization if needed
    public void setOnPatientClickListener(OnPatientClickListener listener) {
        this.onPatientClickListener = listener;
    }

    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textId, textAge;
        View itemView;

        public PatientViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            textName = itemView.findViewById(R.id.textPatientName);
            textId = itemView.findViewById(R.id.textPatientID);
            textAge = itemView.findViewById(R.id.textPatientAge);
        }
    }

    @Override
    public PatientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);
        holder.textName.setText(patient.getEmail());
        holder.textId.setText("ID: " + patient.getId());
        holder.textAge.setText("Email: " + patient.getEmail());

        // Set click listener for the entire item view
        holder.itemView.setOnClickListener(v -> {
            if (onPatientClickListener != null) {
                onPatientClickListener.onPatientClick(patient, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }
}