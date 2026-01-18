package com.example.sos;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class HowToUseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HowToUseAdapter adapter;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_use);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("How to Use SHAKTI");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<GuideSection> sections = createGuideSections();
        adapter = new HowToUseAdapter(sections);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<GuideSection> createGuideSections() {
        List<GuideSection> sections = new ArrayList<>();

        // Getting Started
        sections.add(new GuideSection(
                "🚀 Getting Started",
                "Essential first steps to activate SHAKTI protection",
                new String[] {
                        "1. Complete Your Profile (Mandatory)\n   • Fill personal information (Name, Age, Gender, City)\n   • Add contact details (Mobile, Emergency Contact)\n   • Enter health information (Blood Group, Allergies, Medical Conditions)\n   • This data is used ONLY during emergencies",
                        "2. Add Emergency Contacts\n   • Add at least 1 contact (maximum 5)\n   • These contacts receive all emergency alerts\n   • Use trusted family members or friends",
                        "3. Grant Permissions\n   • Location - For GPS tracking\n   • SMS - To send emergency alerts\n   • Phone - For auto-calling authorities\n   • Camera - For evidence capture\n   • Microphone - For voice detection\n   • All permissions are essential for safety features",
                        "4. Start Monitoring Service\n   • Tap 'Start Monitoring' on main screen\n   • Service runs in background 24/7\n   • One-time activation - works automatically"
                }));

        // Voice Commands
        sections.add(new GuideSection(
                "🎙️ Voice-Activated Emergency",
                "Hands-free help when you need it most",
                new String[] {
                        "How It Works:\n   • App continuously listens for emergency keywords\n   • Works even when phone is locked or in pocket\n   • No need to touch phone - just speak clearly",
                        "Emergency Keywords:\n\n   🚓 'Police' - Police Emergency\n   • Sends SMS with your personal data\n   • Includes location and timestamp\n   • Camera auto-starts for evidence\n\n   🩺 'Doctor' or 'Medical' - Medical Emergency\n   • Sends SMS with FULL health profile\n   • Blood group, allergies, conditions, medicines\n   • Critical for paramedics\n\n   🚑 'Ambulance' or 'Accident' - Ambulance Call\n   • AUTO-CALLS ambulance immediately\n   • Sends SMS with blood group\n   • Camera and tracking activated\n\n   🔥 'Fire' - Fire Emergency\n   • AUTO-CALLS fire department\n   • Sends location alert\n   • Immediate response\n\n   🌊 'Flood' or 'Disaster' - Disaster Alert\n   • Calls disaster management\n   • Location-based alert\n\n   🆘 'Help', 'Bachao', 'Emergency' - General SOS\n   • Sends alert to all contacts\n   • Camera and tracking start",
                        "Important Rules:\n   ✓ Speak clearly and loudly\n   ✓ Use exact keywords listed above\n   ✓ Works in noisy environments\n   ✓ Bilingual support (English + Hindi)\n   ✗ Don't use for testing - use Test Mode only"
                }));

        // Profile Management
        sections.add(new GuideSection(
                "👤 Profile Management",
                "Your safety information - keep it updated",
                new String[] {
                        "Personal Information:\n   • Full Name - Used in all emergency alerts\n   • Age - Important for medical response\n   • Gender - For appropriate assistance\n   • City/State - Local authority coordination",
                        "Contact Information:\n   • Mobile Number - Your contact for authorities\n   • Emergency Contact - Backup contact person\n   • Email - Account identification (cannot edit)",
                        "Health Information:\n   • Blood Group - CRITICAL for medical emergencies\n   • Allergies - Prevents dangerous medications\n   • Medical Conditions - Informs paramedics\n   • Regular Medicines - Current treatment info\n   • Emergency Notes - Any special instructions",
                        "Privacy & Security:\n   ✓ All data stored securely in Firebase\n   ✓ Used ONLY during emergencies\n   ✓ No sharing with third parties\n   ✓ Edit anytime from Profile section\n   ✓ Data encrypted and protected"
                }));

        // Evidence Camera
        sections.add(new GuideSection(
                "📸 Evidence Recording",
                "Automatic visual documentation for safety",
                new String[] {
                        "How It Works:\n   • Camera launches automatically during SOS\n   • Captures photos every 30 seconds\n   • Continues until you stop monitoring\n   • Photos saved in secure private storage",
                        "Manual Recording:\n   • Tap 'Record Evidence' card on home screen\n   • Tap 'Start Monitor' to begin capture\n   • Tap 'Stop Monitor' when safe\n   • View captured photos in gallery",
                        "Viewing Evidence:\n   • Tap 'View Evidence Gallery' button\n   • Browse all captured photos in grid\n   • Tap photo to see details\n   • Photos stored privately (not in phone gallery)",
                        "Important Notes:\n   ✓ Photos are evidence - don't delete\n   ✓ Secure storage prevents tampering\n   ✓ Space-efficient (photos, not video)\n   ✓ Works in low light conditions\n   ✗ May not work if phone is locked (Android security)"
                }));

        // Safe Route
        sections.add(new GuideSection(
                "🗺️ Safe Route Tracking",
                "Live location monitoring during travel",
                new String[] {
                        "Setting Up Route:\n   1. Tap 'Safe Route' card on home screen\n   2. Map opens showing your current location\n   3. Tap destination on map\n   4. Blue line shows your planned route\n   5. Tap 'Start Safe Route' to begin",
                        "During Journey:\n   • Location sent to contacts every 2 minutes\n   • SMS includes live GPS link\n   • Contacts can track you in real-time\n   • Continues until you reach destination",
                        "Google Maps Integration:\n   • Tap 'Open in Google Maps' for navigation\n   • Get turn-by-turn directions\n   • Route monitoring continues in background",
                        "Best Practices:\n   ✓ Use for late-night travel\n   ✓ Share with trusted contacts\n   ✓ Ensure phone is charged\n   ✓ Keep mobile data/GPS on\n   ✗ Don't stop tracking until safe"
                }));

        // Emergency Contacts
        sections.add(new GuideSection(
                "📞 Emergency Contacts",
                "Your safety network - choose wisely",
                new String[] {
                        "Adding Contacts:\n   • Tap 'Emergency Contacts' on home\n   • Add from phone contacts or manually\n   • Minimum 1, Maximum 5 contacts\n   • Choose people who can help quickly",
                        "Who to Add:\n   ✓ Family members (parents, spouse, siblings)\n   ✓ Close friends in same city\n   ✓ Neighbors or colleagues\n   ✓ People who answer calls promptly\n   ✗ Avoid adding too many (causes confusion)",
                        "Managing Contacts:\n   • Long press to delete contact\n   • Update numbers if changed\n   • Test by sending test message\n   • Keep list current and relevant",
                        "What They Receive:\n   • SMS with your location (Google Maps link)\n   • Emergency type (Police/Medical/Fire etc.)\n   • Your health profile (for medical emergencies)\n   • Timestamp of alert\n   • Updates every 2 minutes during tracking"
                }));

        // Siren & Alerts
        sections.add(new GuideSection(
                "🚨 Siren & Alerts",
                "Loud alarm to deter attackers and attract help",
                new String[] {
                        "Loud Siren:\n   • Tap 'Loud Siren' card for instant alarm\n   • Very loud sound to scare attackers\n   • Attracts attention from nearby people\n   • Tap again to stop siren",
                        "Automatic Activation:\n   • Siren starts automatically during SOS\n   • Triggered by voice commands\n   • Triggered by shake detection\n   • Cannot be stopped by attacker (biometric lock)",
                        "Smart Guard:\n   • Toggle 'Voice & Scream Alert' on home\n   • Enables continuous voice monitoring\n   • Detects emergency keywords 24/7\n   • Low battery consumption",
                        "Safety Tips:\n   ✓ Test siren volume in safe environment\n   ✓ Ensure phone volume is maximum\n   ✓ Use in public places for best effect\n   ✗ Don't use for pranks or false alarms"
                }));

        // Biometric Security
        sections.add(new GuideSection(
                "🔐 Biometric Security",
                "Prevent attackers from disabling protection",
                new String[] {
                        "How It Works:\n   • Fingerprint or Face ID required to stop monitoring\n   • Prevents unauthorized service stop\n   • Attacker cannot disable your protection\n   • Only you can stop the service",
                        "Stopping Monitoring:\n   1. Tap 'Stop Monitoring' button\n   2. Biometric prompt appears\n   3. Use fingerprint or face unlock\n   4. Service stops only after authentication",
                        "Fallback Options:\n   • If biometric fails, use PIN/Password\n   • Registered fingerprints work\n   • Face unlock (if device supports)\n   • Pattern lock (device dependent)",
                        "Security Best Practices:\n   ✓ Register multiple fingerprints\n   ✓ Keep biometric data updated\n   ✓ Don't share unlock methods\n   ✓ Test authentication before emergency\n   ✗ Never disable biometric lock"
                }));

        // Testing Mode
        sections.add(new GuideSection(
                "🧪 Testing Mode (Developers)",
                "Safe testing without false alarms",
                new String[] {
                        "Current Status:\n   • Testing Mode: ACTIVE\n   • Test Number: +919226144288\n   • All SMS/calls go to test number only\n   • [TEST MODE] prefix in all messages",
                        "What This Means:\n   ✓ Safe to test all features\n   ✓ No false alarms to authorities\n   ✓ No emergency calls to real numbers\n   ✓ Perfect for development and demo",
                        "Testing Checklist:\n   □ Fill complete profile\n   □ Add emergency contacts\n   □ Test each voice command\n   □ Verify SMS received at test number\n   □ Check auto-call functionality\n   □ Test evidence camera\n   □ Verify live tracking",
                        "Production Mode:\n   • For real deployment, testing mode must be disabled\n   • Contact developer to switch to production\n   • Real emergency numbers will be used\n   • Remove [TEST MODE] prefix from messages"
                }));

        // Important Rules
        sections.add(new GuideSection(
                "⚠️ Important Rules & Guidelines",
                "Read carefully before using SHAKTI",
                new String[] {
                        "DO's:\n   ✓ Keep profile information updated\n   ✓ Test features in safe environment first\n   ✓ Keep phone charged (minimum 20%)\n   ✓ Enable location services always\n   ✓ Grant all required permissions\n   ✓ Add trusted emergency contacts\n   ✓ Use voice commands clearly\n   ✓ Keep monitoring service active",
                        "DON'Ts:\n   ✗ Don't use for pranks or false alarms\n   ✗ Don't share your profile password\n   ✗ Don't disable location services\n   ✗ Don't ignore permission requests\n   ✗ Don't add unreliable contacts\n   ✗ Don't test with real emergency numbers\n   ✗ Don't stop monitoring in unsafe areas",
                        "Legal & Ethical:\n   • Use only for genuine emergencies\n   • False alarms waste resources\n   • Misuse may have legal consequences\n   • Evidence photos are for safety only\n   • Respect privacy of others\n   • Follow local laws and regulations",
                        "Battery & Performance:\n   • Background service uses minimal battery\n   • Voice detection is optimized\n   • Location tracking is efficient\n   • Close unused apps for better performance\n   • Charge phone regularly"
                }));

        // Troubleshooting
        sections.add(new GuideSection(
                "🔧 Troubleshooting",
                "Common issues and solutions",
                new String[] {
                        "Voice Not Detecting:\n   • Check microphone permission granted\n   • Ensure 'Voice & Scream Alert' is ON\n   • Speak clearly and loudly\n   • Try different keywords\n   • Restart app if needed",
                        "SMS Not Sending:\n   • Verify SMS permission granted\n   • Check emergency contacts added\n   • Ensure phone has network signal\n   • Check SMS balance (if prepaid)\n   • Try manual SMS test",
                        "Camera Not Opening:\n   • Grant camera permission\n   • Check storage space available\n   • Clear app cache if needed\n   • Restart app\n   • Update app if available",
                        "Location Not Working:\n   • Enable GPS/Location services\n   • Grant location permission\n   • Check internet connection\n   • Try outdoor location (better GPS)\n   • Restart location services",
                        "App Crashes:\n   • Update to latest version\n   • Clear app cache and data\n   • Reinstall app if persistent\n   • Check device compatibility\n   • Contact support with error details"
                }));

        return sections;
    }

    public static class GuideSection {
        public String title;
        public String description;
        public String[] content;

        public GuideSection(String title, String description, String[] content) {
            this.title = title;
            this.description = description;
            this.content = content;
        }
    }
}
