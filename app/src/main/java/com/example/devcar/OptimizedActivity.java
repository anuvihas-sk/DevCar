package com.example.devcar;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class OptimizedActivity extends AppCompatActivity {

    private static final int WRITE_SETTINGS_PERMISSION_REQUEST_CODE = 100;
    private TextView screenBrightnessStatusTextView, screenTimeoutStatusTextView;
    private Button proceedButton, wifiButton, bluetoothButton, gpsButton, darkmodeButton;
    private LottieAnimationView optimizationAnimationView;
    private TextView optimizationMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optimized);

        // Initialize Views
        screenBrightnessStatusTextView = findViewById(R.id.screen_brightness_status);
        screenTimeoutStatusTextView = findViewById(R.id.screen_timeout_status);
        proceedButton = findViewById(R.id.proceed_button);
        wifiButton = findViewById(R.id.wifi_button);
        bluetoothButton = findViewById(R.id.bluetooth_button);
        gpsButton = findViewById(R.id.gps_button);
        darkmodeButton = findViewById(R.id.darkmode_button);
        optimizationAnimationView = findViewById(R.id.optimizationAnimationView);
        optimizationMessageTextView = findViewById(R.id.optimizationMessageTextView);

        // Request permission to change system settings
        requestWriteSettingsPermission();

        // Get the passed data from MainActivity
        int screenBrightness = getIntent().getIntExtra("screenBrightness", 35);  // Default to 35 if no value is passed
        int screenTimeout = getIntent().getIntExtra("screenTimeout", 10000);  // Default to 10 seconds if no value is passed

        // Apply the changes when the activity starts
        changeScreenBrightness(screenBrightness);
        changeScreenTimeout(screenTimeout);

        // Proceed button triggers the optimization animation
        proceedButton.setOnClickListener(v -> startOptimizationAnimation());

        // Buttons for quick settings navigation
        wifiButton.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));
        bluetoothButton.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)));
        gpsButton.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));

        // Check for Android 10 or higher to handle dark mode settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            darkmodeButton.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS)));
        } else {
            darkmodeButton.setVisibility(View.GONE); // Hide dark mode button if not supported
        }
    }

    private void startOptimizationAnimation() {
        // Hide unnecessary UI elements and show animation
        findViewById(R.id.activity_optimized_layout).setBackgroundColor(Color.TRANSPARENT);
        setVisibility(View.INVISIBLE);
        optimizationAnimationView.setVisibility(View.VISIBLE);
        optimizationMessageTextView.setVisibility(View.GONE);
        optimizationAnimationView.playAnimation();

        // Listen to animation end to show success message and navigate to MainActivity
        optimizationAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                optimizationMessageTextView.setText("Battery has been optimized");
                optimizationMessageTextView.setVisibility(View.VISIBLE);
                optimizationMessageTextView.setTextColor(Color.WHITE);
                optimizationMessageTextView.setTextSize(22);
                optimizationMessageTextView.setTypeface(null, Typeface.BOLD);

                // Wait for 2 seconds before navigating to MainActivity
                new Handler().postDelayed(() -> {
                    setVisibility(View.VISIBLE);
                    navigateToMainActivity();
                }, 2000);
            }

            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });
    }

    private void setVisibility(int visibility) {
        // Set visibility for all relevant UI elements
        wifiButton.setVisibility(visibility);
        bluetoothButton.setVisibility(visibility);
        gpsButton.setVisibility(visibility);
        proceedButton.setVisibility(visibility);
        darkmodeButton.setVisibility(visibility);
        screenBrightnessStatusTextView.setVisibility(visibility);
        screenTimeoutStatusTextView.setVisibility(visibility);
        findViewById(R.id.brightness_card).setVisibility(visibility);
        findViewById(R.id.timeout_card).setVisibility(visibility);
    }

    private void navigateToMainActivity() {
        // Navigate back to MainActivity after optimization
        Intent intent = new Intent(OptimizedActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close current activity to prevent going back to it
    }

    private void changeScreenBrightness(int brightnessValue) {
        if (!canChangeBrightness()) {
            requestWriteSettingsPermission();
            return;
        }

        try {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightnessValue);
            screenBrightnessStatusTextView.setText("Brightness: " + brightnessValue);
            screenBrightnessStatusTextView.setTextColor(Color.GREEN);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to change brightness", Toast.LENGTH_SHORT).show();
        }
    }

    private void changeScreenTimeout(int timeoutValue) {
        try {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, timeoutValue);
            screenTimeoutStatusTextView.setText("Timeout: " + (timeoutValue / 1000) + "s");
            screenTimeoutStatusTextView.setTextColor(Color.GREEN);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to change timeout", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean canChangeBrightness() {
        return Settings.System.canWrite(this);
    }

    private void requestWriteSettingsPermission() {
        if (!canChangeBrightness()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            startActivityForResult(intent, WRITE_SETTINGS_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WRITE_SETTINGS_PERMISSION_REQUEST_CODE && Settings.System.canWrite(this)) {
            // Permission granted, apply brightness and timeout changes
        }
    }
}
