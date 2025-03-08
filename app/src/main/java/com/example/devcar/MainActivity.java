package com.example.devcar;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final int PERMISSION_REQUEST_WRITE_SETTINGS = 102;

    private Button optimizeButton;
    private int selectedBrightness = 35; // Default brightness value
    private int selectedTimeout = 10000; // Default timeout value (10 seconds)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permissions right when the app starts
        checkPermissions();

        // Battery Information
        TextView healthStatus = findViewById(R.id.health_status);
        TextView chargingStatus = findViewById(R.id.charging_status);
        TextView batteryLevel = findViewById(R.id.battery_level);
        LinearLayout batteryLevelCard = findViewById(R.id.battery_level_card);
        TextView batteryTechnology = findViewById(R.id.battery_technology);
        TextView temperature = findViewById(R.id.temperature);
        TextView voltage = findViewById(R.id.voltage);

        // Register a receiver for battery status
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (batteryStatus != null) {
            int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            healthStatus.setText(getBatteryHealthString(health));

            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            chargingStatus.setText(status == BatteryManager.BATTERY_STATUS_CHARGING ? "Charging" : "Not Charging");

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPercentage = (level * 100 / scale);
            batteryLevel.setText(batteryPercentage + "%");

            String technology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
            batteryTechnology.setText(technology != null ? technology : "Unknown");

            int temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            temperature.setText((temp / 10.0) + "°C");

            int volt = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
            voltage.setText(volt + " mV");
        }

        // Setup the optimize button functionality
        optimizeButton = findViewById(R.id.optimize_button);
        optimizeButton.setOnClickListener(view -> {
            // Change the brightness and timeout values when "Optimize" is clicked
            selectedBrightness = 10;  // Set brightness to 10 (out of 255)
            selectedTimeout = 30000;    // Set timeout to 30 seconds

            // Apply the brightness and timeout changes
            changeBrightness(selectedBrightness);
            changeScreenTimeout(selectedTimeout);

            // Pass the selected brightness and timeout to OptimizedActivity
            Intent intent = new Intent(MainActivity.this, OptimizedActivity.class);
            intent.putExtra("screenBrightness", selectedBrightness);
            intent.putExtra("screenTimeout", selectedTimeout);
            startActivity(intent);  // Start OptimizedActivity
        });
    }

    private void checkPermissions() {
        // Check for location permission at app startup
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }

        // Request write settings permission for brightness and timeout
        if (!Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            startActivityForResult(intent, PERMISSION_REQUEST_WRITE_SETTINGS);
        }
    }

    private void changeBrightness(int brightness) {
        if (Settings.System.canWrite(this)) {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        } else {
            // Show message to request permission
            Toast.makeText(this, "Permission to change system settings is required.", Toast.LENGTH_SHORT).show();
        }
    }

    private void changeScreenTimeout(int timeout) {
        if (Settings.System.canWrite(this)) {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, timeout);
        } else {
            // Show message to request permission
            Toast.makeText(this, "Permission to change system settings is required.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your operation
            } else {
                // Permission denied, show a message or disable functionality
            }
        }
    }

    private String getBatteryHealthString(int health) {
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD: return "Good";
            case BatteryManager.BATTERY_HEALTH_OVERHEAT: return "Overheat";
            case BatteryManager.BATTERY_HEALTH_DEAD: return "Dead";
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE: return "Over Voltage";
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE: return "Failure";
            default: return "Unknown";
        }
    }
}
