package vn.humg.hai.event_ticket_booking_app.controller;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import java.util.HashMap;
import java.util.Map;

public class ConfigController {
    private static ConfigController instance;
    private final FirebaseRemoteConfig mFirebaseRemoteConfig;

    // Keys
    public static final String KEY_QR_RELEASE_THRESHOLD = "qr_release_threshold_hours";

    private ConfigController() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        init();
    }

    public static synchronized ConfigController getInstance() {
        if (instance == null) {
            instance = new ConfigController();
        }
        return instance;
    }

    private void init() {
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // 1 hour
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        // Set default values
        Map<String, Object> defaultValues = new HashMap<>();
        defaultValues.put(KEY_QR_RELEASE_THRESHOLD, 24L);
        mFirebaseRemoteConfig.setDefaultsAsync(defaultValues);
    }

    public void fetchAndActivate() {
        mFirebaseRemoteConfig.fetchAndActivate();
    }

    public long getQrReleaseThresholdHours() {
        long value = mFirebaseRemoteConfig.getLong(KEY_QR_RELEASE_THRESHOLD);
        return value > 0 ? value : 24; // Fallback to 24 if invalid
    }

    public long getQrReleaseThresholdMs() {
        return getQrReleaseThresholdHours() * 60 * 60 * 1000;
    }
}
