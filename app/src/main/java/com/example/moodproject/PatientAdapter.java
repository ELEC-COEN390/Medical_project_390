package com.example.moodproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private List<Patient> patientList;

    public PatientAdapter(List<Patient> patientList) {
        this.patientList = patientList;
    }

    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textId, textAge;

        public PatientViewHolder(View itemView) {
            super(itemView);
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
        holder.textName.setText(patient.getName());
        holder.textId.setText("ID: " + patient.getId());
        holder.textAge.setText("Age: " + patient.getAge());
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }
}
