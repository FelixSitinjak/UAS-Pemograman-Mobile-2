package com.felix.scanmakanan;

import android.content.Intent;
import android.content.ClipData;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private Uri pendingCameraUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Analisis Kalori Makanan Dan Minuman");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success != null && success && pendingCameraUri != null) {
                openAnalysis(pendingCameraUri);
            }
        });

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                openAnalysis(uri);
            }
        });

        findViewById(R.id.action_take).setOnClickListener(v -> {
            pendingCameraUri = createTempImageUri();
            if (pendingCameraUri != null) {
                takePictureLauncher.launch(pendingCameraUri);
            }
        });

        findViewById(R.id.action_pick).setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        findViewById(R.id.card_take_photo).setOnClickListener(v -> {
            pendingCameraUri = createTempImageUri();
            if (pendingCameraUri != null) {
                takePictureLauncher.launch(pendingCameraUri);
            }
        });

        findViewById(R.id.card_preview).setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void openAnalysis(Uri uri) {
        Intent intent = new Intent(this, AnalysisActivity.class);
        intent.putExtra(AnalysisActivity.EXTRA_IMAGE_URI, uri.toString());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setClipData(ClipData.newRawUri("image", uri));
        startActivity(intent);
    }

    private Uri createTempImageUri() {
        try {
            File imagesDir = new File(getCacheDir(), "images");
            if (!imagesDir.exists()) {
                boolean created = imagesDir.mkdirs();
                if (!created) {
                    return null;
                }
            }
            File image = File.createTempFile("camera_", ".jpg", imagesDir);
            return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", image);
        } catch (Exception e) {
            return null;
        }
    }
}