package com.example.sos;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.DynamicColors;

public class HomeActivity extends AppCompatActivity {

    MaterialCardView registerContact, editMessage, helpline, showContact, btnSosService, btnSafeRoute,
            btnNearby;
    android.widget.TextView tvGreeting;

    MediaPlayer mediaPlayer;
    boolean isSirenPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivitiesIfAvailable(getApplication());
        setContentView(R.layout.activity_home);

        editMessage = findViewById(R.id.editMessage);
        btnSosService = findViewById(R.id.btnSosService);
        helpline = findViewById(R.id.helpline);
        showContact = findViewById(R.id.showContact);
        btnSafeRoute = findViewById(R.id.btnSafeRoute);
        btnNearby = findViewById(R.id.btnNearby);

        editMessage.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        btnSosService.setOnClickListener(v -> {
            if (new DatabaseHelper(this).count() == 0) {
                Toast.makeText(this, "Please verify trusted contacts first!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, RegisterNumberActivity.class));
                return;
            }
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        helpline.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, SosCall.class);
            startActivity(intent);
        });

        showContact.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, ShowContact.class);
            startActivity(intent);
        });

        btnSafeRoute.setOnClickListener(v -> {
            if (new DatabaseHelper(this).count() == 0) {
                Toast.makeText(this, "Please verify trusted contacts first!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, RegisterNumberActivity.class));
                return;
            }
            Intent intent = new Intent(HomeActivity.this, RouteActivity.class);
            startActivity(intent);
        });

        btnNearby.setOnClickListener(v -> {
            if (new DatabaseHelper(this).count() == 0) {
                Toast.makeText(this, "Please verify trusted contacts first!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, RegisterNumberActivity.class));
                return;
            }
            Toast.makeText(HomeActivity.this, "Requesting Nearby Help...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, ServiceMine.class);
            intent.setAction("NEARBY_HELP");
            startService(intent);
        });

        MaterialCardView btnHowToUse = findViewById(R.id.btnHowToUse);
        if (btnHowToUse != null) {
            btnHowToUse.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, HowToUseActivity.class);
                startActivity(intent);
            });
        }

        tvGreeting = findViewById(R.id.tvGreeting);
        updateGreeting();

        // Toggle Voice Detection Service

        // New Gallery Button logic
        MaterialCardView btnGallery = findViewById(R.id.btnGallery);
        if (btnGallery != null) {
            btnGallery.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, GalleryActivity.class);
                startActivity(intent);
            });
        }
    }

    // Siren logic removed

    private void updateGreeting() {
        if (tvGreeting != null) {
            String greeting = "Hello, Guardian";
            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance()
                    .getCurrentUser();
            if (user != null) {
                if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                    greeting = "Hello, " + user.getDisplayName();
                } else if (user.getEmail() != null) {
                    greeting = "Hello, " + user.getEmail().split("@")[0];
                }
            }
            tvGreeting.setText(greeting);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}