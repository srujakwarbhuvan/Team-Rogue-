package com.example.sos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AlertPopupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make it visible even if locked (Keyguard)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                
        setContentView(R.layout.activity_alert_popup);

        TextView titleView = findViewById(R.id.alertTitle);
        TextView messageView = findViewById(R.id.alertMessage);
        Button actionButton = findViewById(R.id.actionButton);
        Button dismissButton = findViewById(R.id.dismissButton);

        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        float distance = intent.getFloatExtra("distance", 0);
        double lat = intent.getDoubleExtra("lat", 0);
        double lng = intent.getDoubleExtra("lng", 0);

        titleView.setText("🚨 EMERGENCY NEARBY!");
        messageView.setText("Someone needs help " + (int)distance + " meters away!\nType: " + type);

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + lat + "," + lng));
                mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mapIntent);
                finish();
            }
        });

        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        // Play critical alert sound
        playAlertSound();
    }
    
    private android.media.MediaPlayer mediaPlayer;
    
    private void playAlertSound() {
        try {
            android.net.Uri alert = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM);
            if (alert == null) {
                alert = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION);
            }
            mediaPlayer = new android.media.MediaPlayer();
            mediaPlayer.setDataSource(this, alert);
            mediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_ALARM);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
