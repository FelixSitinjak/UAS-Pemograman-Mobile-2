package com.felix.scanmakanan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class AnalysisActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URI = "image_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_analysis);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Analisis Kalori Makanan Dan Minuman");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView foodImage = findViewById(R.id.food_image);
        String uriString = getIntent().getStringExtra(EXTRA_IMAGE_URI);
        if (uriString != null && !uriString.isEmpty()) {
            Uri uri = Uri.parse(uriString);
            foodImage.setImageURI(uri);
        }

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        findViewById(R.id.btn_change_photo).setOnClickListener(v -> finish());
        findViewById(R.id.btn_analyze_ai).setOnClickListener(v -> {
            Intent intent = new Intent(this, AnalysisResultActivity.class);
            intent.putExtra(AnalysisResultActivity.EXTRA_IMAGE_URI, getIntent().getStringExtra(EXTRA_IMAGE_URI));
            startActivity(intent);
        });
    }
}
