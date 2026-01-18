package com.example.sos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    TextInputEditText etName, etEmail, etAge, etGender, etMobile, etEmergencyContact,
            etCity, etBloodType, etAllergies, etMedicalConditions, etMedicines, etEmergencyNotes;
    MaterialButton btnSaveProfile, btnLogout, btnEditMessage;
    MaterialToolbar topAppBar;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etAge = findViewById(R.id.etAge);
        etGender = findViewById(R.id.etGender);
        etMobile = findViewById(R.id.etMobile);
        etEmergencyContact = findViewById(R.id.etEmergencyContact);
        etCity = findViewById(R.id.etCity);
        etBloodType = findViewById(R.id.etBloodType);
        etAllergies = findViewById(R.id.etAllergies);
        etMedicalConditions = findViewById(R.id.etMedicalConditions);
        etMedicines = findViewById(R.id.etMedicines);
        etEmergencyNotes = findViewById(R.id.etEmergencyNotes);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditMessage = findViewById(R.id.btnEditMessage);
        topAppBar = findViewById(R.id.topAppBar);

        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        loadUserProfile();

        btnSaveProfile.setOnClickListener(v -> saveUserProfile());

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnEditMessage.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditMessageActivity.class));
        });
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            etEmail.setText(user.getEmail());
            if (user.getDisplayName() != null) {
                etName.setText(user.getDisplayName());
            }

            db.collection("users").document(user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if (document.contains("name"))
                                    etName.setText(document.getString("name"));
                                if (document.contains("age"))
                                    etAge.setText(document.getString("age"));
                                if (document.contains("gender"))
                                    etGender.setText(document.getString("gender"));
                                if (document.contains("mobile"))
                                    etMobile.setText(document.getString("mobile"));
                                if (document.contains("emergencyContact"))
                                    etEmergencyContact.setText(document.getString("emergencyContact"));
                                if (document.contains("city"))
                                    etCity.setText(document.getString("city"));
                                if (document.contains("bloodType"))
                                    etBloodType.setText(document.getString("bloodType"));
                                if (document.contains("allergies"))
                                    etAllergies.setText(document.getString("allergies"));
                                if (document.contains("medicalConditions"))
                                    etMedicalConditions.setText(document.getString("medicalConditions"));
                                if (document.contains("medicines"))
                                    etMedicines.setText(document.getString("medicines"));
                                if (document.contains("emergencyNotes"))
                                    etEmergencyNotes.setText(document.getString("emergencyNotes"));
                            }
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage()
                                    : "Unknown Error";
                            Toast.makeText(ProfileActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void saveUserProfile() {
        String name = etName.getText().toString();
        String age = etAge.getText().toString();
        String gender = etGender.getText().toString();
        String mobile = etMobile.getText().toString();
        String emergencyContact = etEmergencyContact.getText().toString();
        String city = etCity.getText().toString();
        String bloodType = etBloodType.getText().toString();
        String allergies = etAllergies.getText().toString();
        String medicalConditions = etMedicalConditions.getText().toString();
        String medicines = etMedicines.getText().toString();
        String notes = etEmergencyNotes.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", user.getEmail());
            userData.put("age", age);
            userData.put("gender", gender);
            userData.put("mobile", mobile);
            userData.put("emergencyContact", emergencyContact);
            userData.put("city", city);
            userData.put("bloodType", bloodType);
            userData.put("allergies", allergies);
            userData.put("medicalConditions", medicalConditions);
            userData.put("medicines", medicines);
            userData.put("emergencyNotes", notes);

            db.collection("users").document(user.getUid())
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener(
                            aVoid -> Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast
                            .makeText(ProfileActivity.this, "Error saving profile", Toast.LENGTH_SHORT).show());
        }
    }
}
