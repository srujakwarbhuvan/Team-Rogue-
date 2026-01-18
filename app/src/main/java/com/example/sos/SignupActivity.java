package com.example.sos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupActivity extends AppCompatActivity {

    TextInputEditText nameEt, emailEt, passwordEt;
    MaterialButton btnSignup;
    TextView tvLogin;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        nameEt = findViewById(R.id.nameEt);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        btnSignup = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEt.getText().toString();
                String email = emailEt.getText().toString();
                String password = passwordEt.getText().toString();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(SignupActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Update user profile with name
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                                if (mAuth.getCurrentUser() != null) {
                                    mAuth.getCurrentUser().updateProfile(profileUpdates);
                                }

                                Toast.makeText(SignupActivity.this, "Account Created!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignupActivity.this, HomeActivity.class));
                                finishAffinity(); // Close all previous activities
                            } else {
                                Toast.makeText(SignupActivity.this,
                                        "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
            }
        });

        tvLogin.setOnClickListener(v -> {
            finish(); // Go back to Login Activity
        });
    }
}
