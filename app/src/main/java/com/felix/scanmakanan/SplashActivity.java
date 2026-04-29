package com.felix.scanmakanan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_TIMEOUT_MS = 2200;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean navigated = false;

    private TextView regionText;
    private ImageView regionLogo;

    private FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<String> requestLocationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted != null && granted) {
                    fetchLocationAndUpdateUi();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        regionText = findViewById(R.id.region);
        regionLogo = findViewById(R.id.region_logo);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        handler.postDelayed(this::navigateToMainIfNeeded, SPLASH_TIMEOUT_MS);

        if (hasLocationPermission()) {
            fetchLocationAndUpdateUi();
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void fetchLocationAndUpdateUi() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    updateRegionFromLocation(location);
                }
            });
        } catch (SecurityException ignored) {
        }
    }

    private void updateRegionFromLocation(Location location) {
        try {
            Geocoder geocoder = new Geocoder(this, new Locale("id", "ID"));
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String region = null;
                if (address.getAdminArea() != null && !address.getAdminArea().isEmpty()) {
                    region = address.getAdminArea();
                } else if (address.getSubAdminArea() != null && !address.getSubAdminArea().isEmpty()) {
                    region = address.getSubAdminArea();
                } else if (address.getLocality() != null && !address.getLocality().isEmpty()) {
                    region = address.getLocality();
                }

                if (region != null && !region.isEmpty()) {
                    regionText.setText(region);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void navigateToMainIfNeeded() {
        if (navigated) return;
        navigated = true;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
