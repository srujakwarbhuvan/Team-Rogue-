package com.example.sos;

public class EmergencyConfig {
    // Testing Mode - Set to false for production
    public static final boolean TESTING_MODE = true;
    public static final String TEST_NUMBER = "+919226144288";

    // Emergency Authority Numbers (Production)
    public static final String POLICE_NUMBER = "100";
    public static final String AMBULANCE_NUMBER = "102";
    public static final String FIRE_NUMBER = "101";
    public static final String DISASTER_NUMBER = "108";

    // Emergency Types
    public static final String TYPE_POLICE = "POLICE";
    public static final String TYPE_DOCTOR = "DOCTOR";
    public static final String TYPE_AMBULANCE = "AMBULANCE";
    public static final String TYPE_ACCIDENT = "ACCIDENT";
    public static final String TYPE_FIRE = "FIRE";
    public static final String TYPE_FLOOD = "FLOOD";
    public static final String TYPE_GENERAL = "GENERAL";

    public static String getEmergencyNumber(String type) {
        if (TESTING_MODE) {
            return TEST_NUMBER;
        }

        switch (type) {
            case TYPE_POLICE:
                return POLICE_NUMBER;
            case TYPE_AMBULANCE:
            case TYPE_ACCIDENT:
            case TYPE_DOCTOR:
                return AMBULANCE_NUMBER;
            case TYPE_FIRE:
                return FIRE_NUMBER;
            case TYPE_FLOOD:
                return DISASTER_NUMBER;
            default:
                return TEST_NUMBER;
        }
    }

    public static String formatEmergencyMessage(String type, UserProfile profile, String location) {
        String prefix = TESTING_MODE ? "[TEST MODE]\n" : "";
        String timestamp = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date());

        switch (type) {
            case TYPE_POLICE:
                return prefix + "🚨 POLICE EMERGENCY\n" +
                        "Name: " + profile.name + "\n" +
                        "Age: " + profile.age + "\n" +
                        "Mobile: " + profile.mobile + "\n" +
                        "Location: " + location + "\n" +
                        "Message: I need immediate police help.\n" +
                        "Time: " + timestamp;

            case TYPE_DOCTOR:
                return prefix + "🆘 MEDICAL EMERGENCY\n" +
                        "Name: " + profile.name + "\n" +
                        "Age: " + profile.age + "\n" +
                        "Blood Group: " + profile.bloodType + "\n" +
                        "Allergies: " + profile.allergies + "\n" +
                        "Medical Conditions: " + profile.medicalConditions + "\n" +
                        "Medicines: " + profile.medicines + "\n" +
                        "Location: " + location + "\n" +
                        "Immediate medical help required.\n" +
                        "Time: " + timestamp;

            case TYPE_AMBULANCE:
            case TYPE_ACCIDENT:
                return prefix + "🚑 ACCIDENT ALERT\n" +
                        "Name: " + profile.name + "\n" +
                        "Blood Group: " + profile.bloodType + "\n" +
                        "Location: " + location + "\n" +
                        "Immediate ambulance required.\n" +
                        "Time: " + timestamp;

            case TYPE_FIRE:
                return prefix + "🔥 FIRE EMERGENCY\n" +
                        "Name: " + profile.name + "\n" +
                        "Location: " + location + "\n" +
                        "Immediate fire assistance required.\n" +
                        "Time: " + timestamp;

            case TYPE_FLOOD:
                return prefix + "🌊 FLOOD EMERGENCY\n" +
                        "Name: " + profile.name + "\n" +
                        "User trapped in flood-affected area.\n" +
                        "Location: " + location + "\n" +
                        "Time: " + timestamp;

            default:
                return prefix + "🆘 EMERGENCY ALERT\n" +
                        "Name: " + profile.name + "\n" +
                        "Location: " + location + "\n" +
                        "Immediate help required.\n" +
                        "Time: " + timestamp;
        }
    }

    public static class UserProfile {
        public String name = "";
        public String age = "";
        public String gender = "";
        public String mobile = "";
        public String emergencyContact = "";
        public String city = "";
        public String bloodType = "";
        public String allergies = "";
        public String medicalConditions = "";
        public String medicines = "";
    }
}
