package com.example.sos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RouteActivity extends AppCompatActivity {

    private MapView map;
    private FusedLocationProviderClient fusedLocationClient;
    private MaterialButton btnGetRoute, btnStartRoute, btnOpenMaps;
    private TextInputEditText etSource, etDestination;
    private android.widget.ProgressBar progressBar;
    private GeoPoint currentLatLong;
    private GeoPoint destinationLatLong;
    private ArrayList<SimpleLocation> routePoints; // For Service
    private Polyline roadOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSM Configuration
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_route);

        map = findViewById(R.id.map);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);

        etSource = findViewById(R.id.etSource);
        etDestination = findViewById(R.id.etDestination);
        btnGetRoute = findViewById(R.id.btnGetRoute);
        btnStartRoute = findViewById(R.id.btnStartRoute);
        btnOpenMaps = findViewById(R.id.btnOpenMaps);
        progressBar = findViewById(R.id.progressBar);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getCurrentLocation();

        // Restore active route state if any
        restoreRouteState();

        // Handle Map Clicks for Destination
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                destinationLatLong = p;
                etDestination.setText(String.format("%.4f, %.4f", p.getLatitude(), p.getLongitude()));

                Marker endMarker = new Marker(map);
                endMarker.setPosition(p);
                endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                endMarker.setTitle("Destination");
                map.getOverlays().add(endMarker);
                map.invalidate();
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        map.getOverlays().add(new MapEventsOverlay(mReceive));

        btnGetRoute.setOnClickListener(v -> {
            if (currentLatLong != null && destinationLatLong != null) {
                fetchRoute(currentLatLong, destinationLatLong);
            } else {
                Toast.makeText(RouteActivity.this, "Please select destination on map", Toast.LENGTH_SHORT).show();
            }
        });

        btnStartRoute.setOnClickListener(v -> startSafeRouteService());
    }

    // ... (Keep existing methods getCurrentLocation, fetchRoute, parseOSRMRoute)
    // ...

    private void startSafeRouteService() {
        if (new DatabaseHelper(this).count() == 0) {
            Toast.makeText(this, "Please verify trusted contacts first!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, RegisterNumberActivity.class));
            return;
        }

        if (routePoints == null || routePoints.isEmpty())
            return;
        Toast.makeText(this, "Starting Safe Patrol...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, ServiceMine.class);
        intent.setAction("START_ROUTE");
        intent.putParcelableArrayListExtra("route_points", routePoints);
        startService(intent);

        saveRouteState(); // Persistent Save

        // Monitoring Mode
        btnStartRoute.setText("Stop Patrol");
        btnStartRoute.setOnClickListener(v -> stopSafeRouteService());

        btnGetRoute.setEnabled(false);
        etDestination.setEnabled(false);

        Toast.makeText(this, "Patrol Started. Stay on the blue line.", Toast.LENGTH_LONG).show();
        startFollowingUser();
    }

    private void stopSafeRouteService() {
        Intent intent = new Intent(this, ServiceMine.class);
        intent.setAction("STOP");
        startService(intent);

        clearRouteState(); // Persistent Clear

        btnStartRoute.setText("Start Patrol");
        btnStartRoute.setOnClickListener(v -> startSafeRouteService());

        btnGetRoute.setEnabled(true);
        etDestination.setEnabled(true);

        // Remove Route Line
        if (map.getOverlays().contains(roadOverlay)) {
            map.getOverlays().remove(roadOverlay);
            map.invalidate();
        }

        Toast.makeText(this, "Patrol Stopped.", Toast.LENGTH_SHORT).show();
    }

    private void saveRouteState() {
        if (routePoints == null)
            return;
        StringBuilder sb = new StringBuilder();
        for (SimpleLocation loc : routePoints) {
            sb.append(loc.latitude).append(",").append(loc.longitude).append(";");
        }

        android.content.SharedPreferences prefs = getSharedPreferences("SafeRoute", MODE_PRIVATE);
        prefs.edit().putString("active_route", sb.toString()).putBoolean("is_active", true).apply();
    }

    private void clearRouteState() {
        getSharedPreferences("SafeRoute", MODE_PRIVATE).edit().clear().apply();
    }

    private void restoreRouteState() {
        android.content.SharedPreferences prefs = getSharedPreferences("SafeRoute", MODE_PRIVATE);
        boolean isActive = prefs.getBoolean("is_active", false);
        if (isActive) {
            String routeStr = prefs.getString("active_route", "");
            if (!routeStr.isEmpty()) {
                routePoints = new ArrayList<>();
                ArrayList<GeoPoint> geoPoints = new ArrayList<>();
                String[] points = routeStr.split(";");
                for (String p : points) {
                    if (!p.isEmpty()) {
                        String[] latlon = p.split(",");
                        if (latlon.length == 2) {
                            double lat = Double.parseDouble(latlon[0]);
                            double lon = Double.parseDouble(latlon[1]);
                            routePoints.add(new SimpleLocation(lat, lon));
                            geoPoints.add(new GeoPoint(lat, lon));
                        }
                    }
                }

                // Draw Route
                if (roadOverlay != null)
                    map.getOverlays().remove(roadOverlay);
                roadOverlay = new Polyline();
                roadOverlay.setPoints(geoPoints);
                roadOverlay.setColor(0xFF0000FF);
                roadOverlay.setWidth(12f);
                map.getOverlays().add(0, roadOverlay);
                map.invalidate();

                // Set UI to Monitoring Mode
                btnStartRoute.setText("Stop Patrol");
                btnStartRoute.setOnClickListener(v -> stopSafeRouteService());
                btnStartRoute.setEnabled(true);
                btnGetRoute.setEnabled(false);
                etDestination.setEnabled(false);
                startFollowingUser();
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLatLong = new GeoPoint(location.getLatitude(), location.getLongitude());
                map.getController().setCenter(currentLatLong);

                Marker startMarker = new Marker(map);
                startMarker.setPosition(currentLatLong);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                startMarker.setTitle("Me");
                map.getOverlays().add(startMarker);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchRoute(GeoPoint start, GeoPoint end) {
        btnGetRoute.setVisibility(android.view.View.INVISIBLE);
        progressBar.setVisibility(android.view.View.VISIBLE);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    // OSRM Url: Use HTTP (usesCleartextTraffic=true enabled now)
                    String urlString = "http://router.project-osrm.org/route/v1/driving/" +
                            start.getLongitude() + "," + start.getLatitude() + ";" +
                            end.getLongitude() + "," + end.getLatitude() +
                            "?overview=full&geometries=geojson";

                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        return result.toString();
                    } else {
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                progressBar.setVisibility(android.view.View.GONE);
                btnGetRoute.setVisibility(android.view.View.VISIBLE);

                if (result != null) {
                    parseOSRMRoute(result);
                } else {
                    Toast.makeText(RouteActivity.this, "Route Fetch Failed: Check Internet or Try Again",
                            Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void parseOSRMRoute(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray routes = json.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);
                JSONObject geometry = route.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");

                ArrayList<GeoPoint> geoPoints = new ArrayList<>();
                routePoints = new ArrayList<>();

                for (int i = 0; i < coordinates.length(); i++) {
                    JSONArray coord = coordinates.getJSONArray(i);
                    double lon = coord.getDouble(0);
                    double lat = coord.getDouble(1);
                    geoPoints.add(new GeoPoint(lat, lon));
                    routePoints.add(new SimpleLocation(lat, lon));
                }

                if (roadOverlay != null)
                    map.getOverlays().remove(roadOverlay);
                roadOverlay = new Polyline();
                roadOverlay.setPoints(geoPoints);
                roadOverlay.setColor(0xFF0000FF);
                roadOverlay.setWidth(12f);
                map.getOverlays().add(0, roadOverlay);
                map.invalidate();

                // Zoom to bounds
                if (!geoPoints.isEmpty()) {
                    org.osmdroid.util.BoundingBox boundingBox = org.osmdroid.util.BoundingBox.fromGeoPoints(geoPoints);
                    map.zoomToBoundingBox(boundingBox, true, 50); // 50px padding
                }

                btnStartRoute.setEnabled(true);
                Toast.makeText(this, "Route Found!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No route found.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing route.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startFollowingUser() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        com.google.android.gms.location.LocationRequest request = new com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 5000).build();

        fusedLocationClient.requestLocationUpdates(request, new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    android.location.Location loc = locationResult.getLastLocation();
                    GeoPoint newLoc = new GeoPoint(loc.getLatitude(), loc.getLongitude());
                    map.getController().animateTo(newLoc);
                }
            }
        }, android.os.Looper.getMainLooper());
    }
}
