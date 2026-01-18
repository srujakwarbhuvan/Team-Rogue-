package com.example.sos;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import android.os.Handler;
import android.os.Looper;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import java.util.ArrayList;
import android.os.Bundle;

public class ServiceMine extends Service implements SensorEventListener {

    boolean isRunning = false;
    private Vibrator vibrator;
    DatabaseHelper db;
    FusedLocationProviderClient fusedLocationClient;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime;
    private long lastMovementTime;
    private static final int SHAKE_THRESHOLD = 90;
    private static final int MOVEMENT_THRESHOLD_MIN = 8;
    private static final int MOVEMENT_THRESHOLD_MAX = 12;
    private static final long INACTIVITY_LIMIT = 1 * 60 * 1000; // 1 Minute for testing

    private LocationCallback locationCallback;
    private Handler inactivityHandler = new Handler(Looper.getMainLooper());
    private Runnable inactivityRunnable;
    SmsManager manager = SmsManager.getDefault();
    String myLocation;

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        lastMovementTime = System.currentTimeMillis();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        myLocation = "https://maps.google.com/maps?q=" + location.getLatitude() + ","
                                + location.getLongitude();
                    }
                }
            }
        };
        startLocationUpdates();
        startInactivityCheck();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions,
            // int[] grantResults)
            // to handle the case where the user grants the permission. See the
            // documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // Get the current location of user
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationToken() {
            @Override
            public boolean isCancellationRequested() {
                return false;
            }

            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    myLocation = "https://maps.google.com/maps?q=" + location.getLatitude() + ","
                            + location.getLongitude();
                } else {
                    myLocation = "Unable to Find Location :(";
                }
            }
        });

        initializeVoiceListening();
        listenForNearbyAlerts();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(15000)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void startInactivityCheck() {
        inactivityRunnable = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastMovementTime) > INACTIVITY_LIMIT) {
                    triggerInactivityAlert();
                }
                inactivityHandler.postDelayed(this, 10000); // Check frequently for demo
            }
        };
        inactivityHandler.post(inactivityRunnable);
    }

    private void triggerInactivityAlert() {
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        lastMovementTime = System.currentTimeMillis();
    }

    MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private int originalVolume;
    private Handler volumeHandler = new Handler(Looper.getMainLooper());
    private Runnable volumeRunnable;

    // Safe Route Fields
    boolean isRouteMode = false;
    java.util.ArrayList<SimpleLocation> routePath; // Store full path using generic model
    private final Handler liveLocationHandler = new Handler(Looper.getMainLooper());
    private Runnable liveLocationRunnable;
    private static final long LIVE_LOCATION_INTERVAL = 30 * 1000; // Check every 30 seconds
    private static final double MAX_DEVIATION_METERS = 200.0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }

        String action = (intent != null) ? intent.getAction() : null;

        if (action != null && action.equalsIgnoreCase("STOP")) {
            stopServiceCommon();
        } else if (action != null && action.equalsIgnoreCase("START_ROUTE")) {
            // Receive the full list of points
            routePath = intent.getParcelableArrayListExtra("route_points");

            if (routePath != null && !routePath.isEmpty()) {
                isRouteMode = true;
                startLiveLocationUpdates();
                startForegroundNotification("Safe Route Active", "Monitoring your path...");
                isRunning = true;
            } else {
                startForegroundNotification("Safe Route Error", "No route data received.");
            }
            return START_NOT_STICKY;
        } else if (action != null && action.equalsIgnoreCase("NEARBY_HELP")) {
            triggerNearbyAlert("COMMUNITY_HELP");
            return START_STICKY;
        } else {
            startForegroundNotification("Protected", "Shake to SOS Active");
            isRunning = true;
            startListening();
            return START_STICKY;
        }
        return START_STICKY;
    }

    private void startLiveLocationUpdates() {
        liveLocationRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRouteMode && myLocation != null) {
                    // Only check for deviation, don't spam "I am Safe" messages
                    // sendLiveLocationToContacts();
                    checkRouteDeviation();
                }
                liveLocationHandler.postDelayed(this, LIVE_LOCATION_INTERVAL);
            }
        };
        liveLocationHandler.post(liveLocationRunnable);
    }

    private void checkRouteDeviation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && routePath != null) {
                if (!isLocationOnPath(location, routePath)) {
                    // DEVIATION DETECTED!
                    triggerSOS();
                    // Notify specifically about deviation
                    if (!db.fetchData().isEmpty()) {
                        manager.sendTextMessage(db.fetchData().get(0).getNumber(), null,
                                "ALERT: Route Deviation Detected! I am off track.", null, null);
                    }
                }
            }
        });
    }

    // Check if user is within tolerance of ANY point on the path
    private boolean isLocationOnPath(Location location, java.util.ArrayList<SimpleLocation> polyline) {
        for (SimpleLocation point : polyline) {
            float[] results = new float[1];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(), point.latitude, point.longitude,
                    results);
            if (results[0] < MAX_DEVIATION_METERS) {
                return true; // We are close enough to at least one point
            }
        }
        return false;
    }

    private void sendLiveLocationToContacts() {
        db = new DatabaseHelper(ServiceMine.this);
        ArrayList<ContactModel> list = db.fetchData();
        for (ContactModel c : list) {
            manager.sendTextMessage(c.getNumber(), null,
                    "Safe Route Update: I am safe. My current location: " + myLocation,
                    null, null);
        }
    }

    private void startForegroundNotification(String title, String text) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationChannel channel = new NotificationChannel("MYID", "CHANNELFOREGROUND",
                NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        m.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, "MYID")
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.siren)
                .setContentIntent(pendingIntent)
                .build();
        this.startForeground(115, notification);
    }

    // Voice Detection Fields
    private android.speech.SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private boolean isVoiceDetectionOn = true; // Enabled by default for SHAKTI

    private void initializeVoiceListening() {
        if (android.speech.SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizerIntent = new Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "en-US");
            speechRecognizerIntent.putExtra(android.speech.RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    this.getPackageName());

            speechRecognizer.setRecognitionListener(new android.speech.RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                }

                @Override
                public void onBeginningOfSpeech() {
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                    // Restart listening
                    if (isVoiceDetectionOn && isRunning) {
                        startListening();
                    }
                }

                @Override
                public void onError(int error) {
                    // Restart listening on error (silence, etc.)
                    if (isVoiceDetectionOn && isRunning) {
                        startListening();
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results
                            .getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null) {
                        for (String result : matches) {
                            String cmd = result.toLowerCase(java.util.Locale.ROOT);
                            boolean keywordMatched = false;

                            // 1. Check Personal Contact Keywords
                            db = new DatabaseHelper(ServiceMine.this);
                            ArrayList<ContactModel> list = db.fetchData();
                            for (ContactModel c : list) {
                                if (c.getKeyword() != null && !c.getKeyword().isEmpty()
                                        && cmd.contains(c.getKeyword().toLowerCase())) {
                                    triggerSpecificContactSOS(c);
                                    keywordMatched = true;
                                }
                            }

                            if (keywordMatched)
                                continue;

                            // 2. Check Helpline Keywords
                            ArrayList<HelplineModel> helplines = db.fetchHelplineData();
                            for (HelplineModel h : helplines) {
                                if (h.getKeyword() != null && !h.getKeyword().isEmpty()
                                        && cmd.contains(h.getKeyword().toLowerCase())) {
                                    triggerHelplineSOS(h);
                                    keywordMatched = true;
                                }
                            }

                            if (keywordMatched)
                                continue;

                            // 3. Authority-Specific Triggers (Legacy/Fallback)
                            if (cmd.contains("help") || cmd.contains("bachao") || cmd.contains("nahi")
                                    || cmd.contains("mummy") || cmd.contains("save me") || cmd.contains("emergency")) {
                                triggerSOS(); // General SOS
                            }
                        }
                    }
                    if (isVoiceDetectionOn && isRunning) {
                        startListening();
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            });
        }
    }

    private void startListening() {
        if (speechRecognizer != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        speechRecognizer.startListening(speechRecognizerIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private boolean isSOSActive = false; // Prevent multiple triggers

    private void triggerSpecificContactSOS(ContactModel contact) {
        if (isSOSActive)
            return; // Already active
        isSOSActive = true;

        startSiren();
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        // 1. Launch Camera to take photos automatically
        Intent cameraIntent = new Intent(this, CameraActivity.class);
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        cameraIntent.putExtra("AUTO_START", true);
        startActivity(cameraIntent);

        // 2. Send SMS ONLY to the specific contact
        myLocation = (myLocation == null) ? "Checking Location..." : myLocation;
        manager.sendTextMessage(contact.getNumber(), null,
                "PERSONAL SOS: I called out '" + contact.getKeyword() + "'! I need help! My location: " + myLocation,
                null, null);

        // 3a. Trigger Nearby Help Alert
        triggerNearbyAlert("SOS_SPECIFIC");

        // 4. Start Live Tracking
        isRouteMode = true;
        if (liveLocationHandler != null && liveLocationRunnable != null) {
            liveLocationHandler.removeCallbacks(liveLocationRunnable);
        }
        startLiveLocationUpdates();
    }

    private void triggerHelplineSOS(HelplineModel helpline) {
        if (isSOSActive)
            return;
        isSOSActive = true;

        startSiren();
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        // 1. Launch Camera to take photos automatically
        Intent cameraIntent = new Intent(this, CameraActivity.class);
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        cameraIntent.putExtra("AUTO_START", true);
        startActivity(cameraIntent);

        // 2. Send SMS to the specific helpline number with Custom Message
        myLocation = (myLocation == null) ? "Checking Location..." : myLocation;
        String messageToSend = helpline.getCustomMessage();
        if (messageToSend == null || messageToSend.trim().isEmpty()) {
            messageToSend = "EMERGENCY: I need help! My location: " + myLocation;
        } else {
            messageToSend += "\n\nLocation: " + myLocation;
        }

        try {
            manager.sendTextMessage(helpline.getNumber(), null, messageToSend, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3a. Trigger Nearby Help Alert
        triggerNearbyAlert("HELPLINE_SOS");

        // 4. Start Live Tracking
        isRouteMode = true;
        if (liveLocationHandler != null && liveLocationRunnable != null) {
            liveLocationHandler.removeCallbacks(liveLocationRunnable);
        }
        startLiveLocationUpdates();

        // Also call them
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + helpline.getNumber()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (ActivityCompat.checkSelfPermission(ServiceMine.this,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent);
        }
    }

    private void triggerSOS() {
        if (isSOSActive)
            return; // Already active, don't re-trigger
        isSOSActive = true;

        startSiren();
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        // 1. Launch Camera to take photos automatically
        Intent cameraIntent = new Intent(this, CameraActivity.class);
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        cameraIntent.putExtra("AUTO_START", true);
        startActivity(cameraIntent);

        // 2. Send SMS to ALL contacts
        myLocation = (myLocation == null) ? "Checking Location..." : myLocation;
        db = new DatabaseHelper(ServiceMine.this);
        ArrayList<ContactModel> list = db.fetchData();
        for (ContactModel c : list) {
            manager.sendTextMessage(c.getNumber(), null,
                    "VOICE ALERT: I screamed HELP! " + "My location: " + myLocation,
                    null, null);
        }

        // 3a. Trigger Nearby Help Alert
        triggerNearbyAlert("SOS");

        // 3. Start Continuous Live Tracking
        isRouteMode = true;
        if (liveLocationHandler != null && liveLocationRunnable != null) {
            liveLocationHandler.removeCallbacks(liveLocationRunnable);
        }
        startLiveLocationUpdates();
    }

    private void triggerAuthorityEmergency(String emergencyType) {
        if (isSOSActive)
            return;
        isSOSActive = true;

        startSiren();
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        // Launch Camera for evidence
        Intent cameraIntent = new Intent(this, CameraActivity.class);
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        cameraIntent.putExtra("AUTO_START", true);
        startActivity(cameraIntent);

        // Fetch user profile from Firebase
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance()
                .getCurrentUser();
        if (user != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        EmergencyConfig.UserProfile profile = new EmergencyConfig.UserProfile();
                        if (document.exists()) {
                            profile.name = document.getString("name");
                            profile.age = document.getString("age");
                            profile.gender = document.getString("gender");
                            profile.mobile = document.getString("mobile");
                            profile.emergencyContact = document.getString("emergencyContact");
                            profile.city = document.getString("city");
                            profile.bloodType = document.getString("bloodType");
                            profile.allergies = document.getString("allergies");
                            profile.medicalConditions = document.getString("medicalConditions");
                            profile.medicines = document.getString("medicines");
                        }

                        sendAuthorityAlert(emergencyType, profile);
                    })
                    .addOnFailureListener(e -> {
                        // Fallback with minimal info
                        EmergencyConfig.UserProfile profile = new EmergencyConfig.UserProfile();
                        profile.name = user.getDisplayName() != null ? user.getDisplayName() : "User";
                        sendAuthorityAlert(emergencyType, profile);
                    });
        }

        // Start live tracking
        isRouteMode = true;
        if (liveLocationHandler != null && liveLocationRunnable != null) {
            liveLocationHandler.removeCallbacks(liveLocationRunnable);
        }
        startLiveLocationUpdates();
    }

    private void sendAuthorityAlert(String emergencyType, EmergencyConfig.UserProfile profile) {
        String location = (myLocation != null) ? myLocation : "Getting Location...";
        String message = EmergencyConfig.formatEmergencyMessage(emergencyType, profile, location);
        String emergencyNumber = EmergencyConfig.getEmergencyNumber(emergencyType);

        // Send SMS to emergency contacts
        db = new DatabaseHelper(ServiceMine.this);
        ArrayList<ContactModel> list = db.fetchData();
        for (ContactModel c : list) {
            manager.sendTextMessage(c.getNumber(), null, message, null, null);
        }

        // For certain emergencies, also call the authority
        if (emergencyType.equals(EmergencyConfig.TYPE_AMBULANCE) ||
                emergencyType.equals(EmergencyConfig.TYPE_ACCIDENT) ||
                emergencyType.equals(EmergencyConfig.TYPE_FIRE)) {

            // Auto-call emergency number
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(android.net.Uri.parse("tel:" + emergencyNumber));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(callIntent);
            }
        }

        // Trigger Nearby Help Alert
        triggerNearbyAlert(emergencyType);
    }

    private void triggerNearbyAlert(String type) {
        Location location = null;
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(loc -> {
                if (loc != null) {
                    uploadAlertToFirestore(loc, type);
                }
            });
        }
    }

    private void uploadAlertToFirestore(Location location, String type) {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance()
                .getCurrentUser();
        if (user == null)
            return;

        java.util.Map<String, Object> alert = new java.util.HashMap<>();
        alert.put("userId", user.getUid());
        alert.put("type", type);
        alert.put("latitude", location.getLatitude());
        alert.put("longitude", location.getLongitude());
        alert.put("timestamp", System.currentTimeMillis());
        alert.put("status", "ACTIVE");

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("active_alerts")
                .document(user.getUid())
                .set(alert);
    }

    private void listenForNearbyAlerts() {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance()
                .getCurrentUser();
        if (user == null)
            return;

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("active_alerts")
                .whereEqualTo("status", "ACTIVE")
                .whereGreaterThan("timestamp", System.currentTimeMillis() - 3600000) // Last 1 hour
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null)
                        return;

                    for (com.google.firebase.firestore.DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            String userId = dc.getDocument().getString("userId");
                            if (userId != null && !userId.equals(user.getUid())) {
                                double lat = dc.getDocument().getDouble("latitude");
                                double lng = dc.getDocument().getDouble("longitude");
                                String type = dc.getDocument().getString("type");

                                checkDistanceAndAlert(lat, lng, type);
                            }
                        }
                    }
                });
    }

    private void checkDistanceAndAlert(double lat, double lng, String type) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(), lat, lng, results);
                float distanceInMeters = results[0];

                if (distanceInMeters <= 1000) { // 1km radius
                    showNearbyAlertNotification(distanceInMeters, type, lat, lng);
                }
            }
        });
    }

    private void showNearbyAlertNotification(float distance, String type, double lat, double lng) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("http://maps.google.com/maps?daddr=" + lat + "," + lng));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationChannel channel = new NotificationChannel("NEARBY_ALERT", "Nearby Alerts",
                NotificationManager.IMPORTANCE_HIGH);
        NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        m.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, "NEARBY_ALERT")
                .setContentTitle("🚨 SOMEONE NEARBY NEEDS HELP!")
                .setContentText("Distance: " + (int) distance + "m. Emergency: " + type)
                .setSmallIcon(R.drawable.siren)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        m.notify((int) System.currentTimeMillis(), notification);

        // Vibrate to alert user
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void stopServiceCommon() {
        if (isRunning) {
            // Stop Location Updates
            if (locationCallback != null)
                fusedLocationClient.removeLocationUpdates(locationCallback);

            // Stop Sensor Listener (Shake Detection)
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }

            // Stop Inactivity Check
            if (inactivityHandler != null && inactivityRunnable != null) {
                inactivityHandler.removeCallbacks(inactivityRunnable);
            }

            // Stop Live Location Tracking
            if (liveLocationHandler != null && liveLocationRunnable != null) {
                liveLocationHandler.removeCallbacks(liveLocationRunnable);
            }

            // Stop Voice Recognition
            if (speechRecognizer != null) {
                speechRecognizer.stopListening();
                speechRecognizer.destroy();
                speechRecognizer = null;
            }

            // Stop Siren & Volume Lock
            stopSiren();

            // Stop Foreground Notification
            stopForeground(true);
            stopSelf();

            // Reset Flags
            isRunning = false;
            isSOSActive = false;
            isRouteMode = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Gravity is ~9.8. If accel is significantly different or changing, it's
            // moving.
            double acceleration = Math.sqrt(x * x + y * y + z * z);

            if (acceleration > MOVEMENT_THRESHOLD_MAX || acceleration < MOVEMENT_THRESHOLD_MIN) {
                lastMovementTime = currentTime; // Reset inactivity timer
            }

            if ((currentTime - lastShakeTime) > 5000) { // Add a time gap between two shakes
                if (acceleration > SHAKE_THRESHOLD) {

                    lastShakeTime = currentTime;
                    lastMovementTime = currentTime; // Mobile is definitely moving

                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));

                    // Activate Siren
                    startSiren();

                    String locationToSend = (myLocation != null) ? myLocation : "Getting Location...";

                    db = new DatabaseHelper(ServiceMine.this);
                    ArrayList<ContactModel> list = db.fetchData();
                    SharedPreferences sp = getSharedPreferences("message", MODE_PRIVATE);
                    String msg = sp.getString("msg", null);
                    if (msg != null) {
                        for (ContactModel c : list) {
                            String message = "Hey, " + c.getName() + " " + msg + "\n\nHere are my coordinates :\n"
                                    + locationToSend;
                            manager.sendTextMessage(c.getNumber(), null, message, null, null);
                        }
                    } else {
                        for (ContactModel c : list) {
                            manager.sendTextMessage(c.getNumber(), null,
                                    "Hey, " + c.getName()
                                            + " I am in DANGER, i need help. Please urgently reach me out. "
                                            + "\n\nHere are my coordinates :\n" + locationToSend,
                                    null, null);
                        }
                    }

                    lastShakeTime = currentTime;
                }
            }
        }
    }

    private void startSiren() {
        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }

        if (audioManager != null) {
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

            // Continuous Volume Max Enforcement
            volumeRunnable = new Runnable() {
                @Override
                public void run() {
                    if (audioManager != null) {
                        // Force Music Stream to Max
                        int maxMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxMusic, 0);

                        // Force Alarm Stream to Max
                        int maxAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarm, 0);
                    }
                    volumeHandler.postDelayed(this, 100); // Check every 100ms
                }
            };
            volumeHandler.post(volumeRunnable);
        }

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
            }
        }

        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    private void stopSiren() {
        // Stop Volume Enforcement
        if (volumeHandler != null && volumeRunnable != null) {
            volumeHandler.removeCallbacks(volumeRunnable);
        }

        // Restore Original Volume
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
        }

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the sensor listener when the service is destroyed
        sensorManager.unregisterListener(this);
        stopSiren();
    }
}