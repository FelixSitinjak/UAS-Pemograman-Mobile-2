package com.felix.scanmakanan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AnalysisResultActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URI = "image_uri";
    private static final String TAG = "AnalysisResultActivity";
    private static final String GEMINI_API_KEY = "AIzaSyDd1QTiifVX8061hpz_S_8vMo6l3vP8dKU";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";

    private static final int MAX_QUOTA_RETRY = 3;
    private static final int MAX_NETWORK_RETRY = 2;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Uri lastImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_analysis_result);

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
        TextView resultSubtitle = findViewById(R.id.result_subtitle);
        TextView tvAiDetailIntro = findViewById(R.id.tv_ai_detail_intro);
        TextView tvFoodOrDrinkName = findViewById(R.id.tv_food_or_drink_name);
        TextView tvCalories = findViewById(R.id.tv_calories);
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        
        String uriString = getIntent().getStringExtra(EXTRA_IMAGE_URI);
        
        if (uriString != null && !uriString.isEmpty()) {
            Uri uri = Uri.parse(uriString);
            lastImageUri = uri;
            foodImage.setImageURI(uri);
            
            // Start analysis with loading state
            resultSubtitle.setText("Sedang menganalisis gambar...");
            if (tvAiDetailIntro != null) {
                tvAiDetailIntro.setText("Sedang memproses analisis AI...");
            }
            tvFoodOrDrinkName.setText("Nama: Menunggu hasil analisis...");
            tvCalories.setText("Kalori: Menunggu Hasil Analisis Kalori");
            progressBar.setVisibility(android.view.View.VISIBLE);
            
            analyzeImageWithGemini(uri, 0, 0);
        }

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save).setOnClickListener(v -> {
            // TODO: Implement save functionality
            Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show();
        });
    }

    private void analyzeImageWithGemini(Uri imageUri, int attempt, int networkAttempt) {
        Log.d(TAG, "Starting analysis. Attempt: " + attempt + ", Network attempt: " + networkAttempt);
        
        new Thread(() -> {
            try {
                Log.d(TAG, "Encoding image to base64...");
                String base64Image = encodeImageToBase64(imageUri);
                Log.d(TAG, "Image encoded successfully");
                
                Log.d(TAG, "Calling Gemini API...");
                String response = callGeminiAPI(base64Image);
                Log.d(TAG, "API call successful");
                
                runOnUiThread(() -> {
                    ProgressBar progressBar = findViewById(R.id.progress_bar);
                    TextView resultSubtitle = findViewById(R.id.result_subtitle);
                    TextView tvAiDetailIntro = findViewById(R.id.tv_ai_detail_intro);
                    TextView tvFoodOrDrinkName = findViewById(R.id.tv_food_or_drink_name);
                    TextView tvCalories = findViewById(R.id.tv_calories);
                    
                    progressBar.setVisibility(android.view.View.GONE);
                    
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray candidates = jsonResponse.getJSONArray("candidates");
                        if (candidates.length() > 0) {
                            JSONObject candidate = candidates.getJSONObject(0);
                            JSONObject content = candidate.getJSONObject("content");
                            JSONArray parts = content.getJSONArray("parts");
                            if (parts.length() > 0) {
                                String analysisText = parts.getJSONObject(0).getString("text");
                                Log.d(TAG, "Analysis text received: " + analysisText);
                                
                                resultSubtitle.setText("Analisis selesai!");
                                if (tvAiDetailIntro != null) {
                                    tvAiDetailIntro.setText("Berikut adalah analisis detail mengenai produk dalam gambar tersebut:");
                                }
                                String[] parsed = parseNameAndCalories(analysisText);
                                Log.d(TAG, "Parsed results: Name=" + parsed[0] + ", Calories=" + parsed[1]);
                                tvFoodOrDrinkName.setText(parsed[0]);
                                tvCalories.setText(parsed[1]);
                            } else {
                                Log.w(TAG, "No parts found in response");
                                resultSubtitle.setText("Analisis gagal");
                                if (tvAiDetailIntro != null) {
                                    tvAiDetailIntro.setText("Tidak ada hasil analisis yang diterima dari AI.");
                                }
                                tvFoodOrDrinkName.setText("Nama: Tidak ada hasil analisis yang diterima dari AI.");
                                tvCalories.setText("Kalori: -");
                            }
                        } else {
                            Log.w(TAG, "No candidates found in response");
                            resultSubtitle.setText("Analisis gagal");
                            if (tvAiDetailIntro != null) {
                                tvAiDetailIntro.setText("Tidak ada kandidat hasil yang ditemukan dari AI.");
                            }
                            tvFoodOrDrinkName.setText("Nama: Tidak ada kandidat hasil yang ditemukan dari AI.");
                            tvCalories.setText("Kalori: -");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse Gemini response", e);
                        resultSubtitle.setText("Terjadi kesalahan");
                        if (tvAiDetailIntro != null) {
                            tvAiDetailIntro.setText("Terjadi kesalahan saat memproses hasil analisis.");
                        }
                        tvFoodOrDrinkName.setText("Nama: Gagal memproses hasil analisis.");
                        tvCalories.setText("Kalori: -");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Gemini analysis failed", e);
                runOnUiThread(() -> {
                    ProgressBar progressBar = findViewById(R.id.progress_bar);
                    TextView resultSubtitle = findViewById(R.id.result_subtitle);
                    TextView tvAiDetailIntro = findViewById(R.id.tv_ai_detail_intro);
                    TextView tvFoodOrDrinkName = findViewById(R.id.tv_food_or_drink_name);
                    TextView tvCalories = findViewById(R.id.tv_calories);
                    
                    progressBar.setVisibility(android.view.View.GONE);
                    if (isQuotaError(e) && attempt < MAX_QUOTA_RETRY && lastImageUri != null) {
                        int retrySeconds = getRetryDelaySeconds(e);
                        resultSubtitle.setText("Mencoba menghubungkan AI...");
                        tvFoodOrDrinkName.setText("Nama: -");
                        tvCalories.setText("Kalori: -");
                        scheduleRetry(retrySeconds, attempt + 1, networkAttempt);
                    } else if (isNetworkError(e) && networkAttempt < MAX_NETWORK_RETRY && lastImageUri != null) {
                        // Network retry logic
                        resultSubtitle.setText("Koneksi bermasalah, mencoba lagi...");
                        tvFoodOrDrinkName.setText("Nama: Menghubungkan ulang...");
                        tvCalories.setText("Kalori: Menghubungkan ulang...");
                        scheduleNetworkRetry(2, attempt, networkAttempt + 1);
                    } else {
                        resultSubtitle.setText("Analisis gagal");
                        String friendly = getUserFriendlyErrorMessage(e);
                        if (tvAiDetailIntro != null) {
                            tvAiDetailIntro.setText(friendly);
                        }
                        tvFoodOrDrinkName.setText("Nama: -");
                        tvCalories.setText("Kalori: -");
                    }
                });
            }
        }).start();
    }

    private boolean isNetworkError(Throwable error) {
        String msg = error != null ? error.getMessage() : null;
        if (msg == null) msg = "";
        String lower = msg.toLowerCase();
        return msg.contains("timeout")
                || msg.contains("connection")
                || msg.contains("network")
                || msg.contains("unreachable")
                || msg.contains("unknownhost")
                || msg.contains("connect")
                || lower.contains("timeout")
                || lower.contains("connection")
                || lower.contains("network")
                || lower.contains("unreachable")
                || lower.contains("unknownhost")
                || lower.contains("connect");
    }

    private boolean isQuotaError(Throwable error) {
        String msg = error != null ? error.getMessage() : null;
        if (msg == null) msg = "";
        String lower = msg.toLowerCase();
        return msg.contains("HTTP 429")
                || lower.contains("resource_exhausted")
                || lower.contains("quota")
                || lower.contains("rate");
    }

    private int getRetryDelaySeconds(Throwable error) {
        String msg = error != null ? error.getMessage() : null;
        String extracted = extractRetryInfoSeconds(msg);
        if (extracted != null) {
            try {
                return Math.max(1, Integer.parseInt(extracted));
            } catch (Exception ignored) {
                return 5;
            }
        }
        return 5;
    }

    private void scheduleRetry(int seconds, int nextAttempt, int networkAttempt) {
        TextView tvAiDetailIntro = findViewById(R.id.tv_ai_detail_intro);
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(android.view.View.GONE);

        if (tvAiDetailIntro != null) {
            tvAiDetailIntro.setText("Kuota API AI sedang habis. Coba lagi dalam " + seconds + " detik.");
        }

        final int[] remaining = new int[]{seconds};
        Runnable tick = new Runnable() {
            @Override
            public void run() {
                remaining[0] = remaining[0] - 1;
                if (remaining[0] > 0) {
                    if (tvAiDetailIntro != null) {
                        tvAiDetailIntro.setText("Kuota API AI sedang habis. Coba lagi dalam " + remaining[0] + " detik.");
                    }
                    mainHandler.postDelayed(this, 1000);
                } else {
                    ProgressBar pb = findViewById(R.id.progress_bar);
                    TextView resultSubtitle = findViewById(R.id.result_subtitle);
                    TextView intro = findViewById(R.id.tv_ai_detail_intro);
                    TextView tvFoodOrDrinkName = findViewById(R.id.tv_food_or_drink_name);
                    TextView tvCalories = findViewById(R.id.tv_calories);

                    resultSubtitle.setText("Sedang menganalisis gambar...");
                    if (intro != null) {
                        intro.setText("Sedang memproses analisis AI...");
                    }
                    tvFoodOrDrinkName.setText("Nama: Menunggu hasil analisis...");
                    tvCalories.setText("Kalori: Menunggu Hasil Analisis Kalori");
                    pb.setVisibility(android.view.View.VISIBLE);
                    analyzeImageWithGemini(lastImageUri, nextAttempt, networkAttempt);
                }
            }
        };

        mainHandler.postDelayed(tick, 1000);
    }

    private void scheduleNetworkRetry(int seconds, int attempt, int nextNetworkAttempt) {
        TextView tvAiDetailIntro = findViewById(R.id.tv_ai_detail_intro);
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(android.view.View.GONE);

        if (tvAiDetailIntro != null) {
            tvAiDetailIntro.setText("Koneksi bermasalah. Mencoba lagi dalam " + seconds + " detik...");
        }

        final int[] remaining = new int[]{seconds};
        Runnable tick = new Runnable() {
            @Override
            public void run() {
                remaining[0] = remaining[0] - 1;
                if (remaining[0] > 0) {
                    if (tvAiDetailIntro != null) {
                        tvAiDetailIntro.setText("Koneksi bermasalah. Mencoba lagi dalam " + remaining[0] + " detik...");
                    }
                    mainHandler.postDelayed(this, 1000);
                } else {
                    ProgressBar pb = findViewById(R.id.progress_bar);
                    TextView resultSubtitle = findViewById(R.id.result_subtitle);
                    TextView intro = findViewById(R.id.tv_ai_detail_intro);
                    TextView tvFoodOrDrinkName = findViewById(R.id.tv_food_or_drink_name);
                    TextView tvCalories = findViewById(R.id.tv_calories);

                    resultSubtitle.setText("Menghubungkan ulang ke AI...");
                    if (intro != null) {
                        intro.setText("Sedang mencoba koneksi ulang...");
                    }
                    tvFoodOrDrinkName.setText("Nama: Menunggu hasil analisis...");
                    tvCalories.setText("Kalori: Menunggu Hasil Analisis Kalori");
                    pb.setVisibility(android.view.View.VISIBLE);
                    analyzeImageWithGemini(lastImageUri, attempt, nextNetworkAttempt);
                }
            }
        };

        mainHandler.postDelayed(tick, 1000);
    }

    private String getUserFriendlyErrorMessage(Throwable error) {
        String msg = error != null ? error.getMessage() : null;
        if (msg == null) msg = "";

        // Most common: quota exceeded / rate limit (HTTP 429)
        if (msg.contains("HTTP 429")
                || msg.toLowerCase().contains("resource_exhausted")
                || msg.toLowerCase().contains("quota")
                || msg.toLowerCase().contains("rate")) {
            String retryInfo = extractRetryInfoSeconds(msg);
            if (retryInfo != null) {
                return "Kuota API AI sedang habis. Coba lagi dalam " + retryInfo + " detik.";
            }
            return "Kuota API AI sedang habis. Silakan coba lagi beberapa saat lagi.";
        }

        // Network / generic errors
        if (msg.toLowerCase().contains("timeout")) {
            return "Koneksi timeout. Mencoba menghubungkan ulang...";
        }
        if (msg.toLowerCase().contains("unable to resolve host") || msg.toLowerCase().contains("unknownhost")) {
            return "Tidak bisa terhubung ke server. Mencoba lagi...";
        }
        if (msg.toLowerCase().contains("connection") || msg.toLowerCase().contains("network")) {
            return "Masalah koneksi internet. Mencoba menghubungkan ulang...";
        }

        return "Gagal menganalisis gambar. Silakan coba lagi.";
    }

    private String extractRetryInfoSeconds(String message) {
        if (message == null) return null;

        // Example: "Please retry in 47.1668s"
        Matcher m1 = Pattern.compile("(?i)retry\\s+in\\s+([0-9]+(?:\\.[0-9]+)?)s").matcher(message);
        if (m1.find()) {
            try {
                double secs = Double.parseDouble(m1.group(1));
                return String.valueOf(Math.max(1, (int) Math.round(secs)));
            } catch (Exception ignored) {
                return null;
            }
        }

        // Example: "retryDelay":"47s"
        Matcher m2 = Pattern.compile("(?i)\\\"retryDelay\\\"\\s*:\\s*\\\"(\\d+)s\\\"").matcher(message);
        if (m2.find()) {
            return m2.group(1);
        }

        return null;
    }

    private String[] parseNameAndCalories(String analysisText) {
        String name = "-";
        String calories = "-";

        if (analysisText != null) {
            // Parse format baru: "Nama: [nama]"
            Pattern namePattern = Pattern.compile("(?i)nama\\s*:\\s*(.+?)(?:\\n|$)");
            Matcher nameMatcher = namePattern.matcher(analysisText);
            if (nameMatcher.find()) {
                String candidate = nameMatcher.group(1).trim();
                if (!candidate.isEmpty()) {
                    name = candidate;
                }
            }
            
            // Fallback untuk format lama
            if ("-".equals(name)) {
                Pattern oldNamePattern = Pattern.compile("(?i)nama\\s+makanan/?minuman\\s*:\\s*(.+?)(?:\\n|$)");
                Matcher oldNameMatcher = oldNamePattern.matcher(analysisText);
                if (oldNameMatcher.find()) {
                    String candidate = oldNameMatcher.group(1).trim();
                    if (!candidate.isEmpty()) {
                        name = candidate;
                    }
                }
            }

            // Parse format baru: "Kalori: [angka] kkal"
            Pattern caloriesPattern = Pattern.compile("(?i)kalori\\s*:\\s*(\\d+(?:[\\.,]\\d+)?)\\s*(kkal|kcal)?");
            Matcher caloriesMatcher = caloriesPattern.matcher(analysisText);
            if (caloriesMatcher.find()) {
                calories = caloriesMatcher.group(1).replace(",", ".") + " kkal";
            }

            // Jika masih tidak ketemu, coba cara lama
            if ("-".equals(calories)) {
                Pattern fallbackPattern = Pattern.compile("(?i)(\\d+(?:[\\.,]\\d+)?)\\s*(kkal|kcal)");
                Matcher fallbackMatcher = fallbackPattern.matcher(analysisText);
                if (fallbackMatcher.find()) {
                    calories = fallbackMatcher.group(1).replace(",", ".") + " kkal";
                }
            }

            // Fallback terakhir: ambil baris pertama untuk nama
            if ("-".equals(name)) {
                String[] lines = analysisText.split("\\r?\\n");
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.toLowerCase().contains("kalori")) {
                        name = trimmed;
                        break;
                    }
                }
            }
        }

        return new String[]{"Nama: " + name, "Kalori: " + calories};
    }

    private String encodeImageToBase64(Uri imageUri) throws IOException {
        // Optimized for Gemini API compatibility
        final int maxDim = 384; // Further reduced for better compatibility
        final int jpegQuality = 60; // Lower quality for smaller file size

        Log.d(TAG, "Starting image encoding for URI: " + imageUri.toString());

        InputStream boundsStream = getContentResolver().openInputStream(imageUri);
        if (boundsStream == null) {
            throw new IOException("Cannot open image input stream");
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(boundsStream, null, bounds);
        try {
            boundsStream.close();
        } catch (IOException ignored) {
        }

        Log.d(TAG, "Original image dimensions: " + bounds.outWidth + "x" + bounds.outHeight);

        int srcW = Math.max(1, bounds.outWidth);
        int srcH = Math.max(1, bounds.outHeight);
        int inSampleSize = 1;
        while ((srcW / inSampleSize) > maxDim || (srcH / inSampleSize) > maxDim) {
            inSampleSize *= 2;
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = inSampleSize;
        opts.inPreferredConfig = Bitmap.Config.RGB_565; // Use RGB_565 for smaller memory footprint

        InputStream decodeStream = getContentResolver().openInputStream(imageUri);
        if (decodeStream == null) {
            throw new IOException("Cannot open image input stream");
        }
        Bitmap bitmap = BitmapFactory.decodeStream(decodeStream, null, opts);
        try {
            decodeStream.close();
        } catch (IOException ignored) {
        }

        if (bitmap == null) {
            throw new IOException("Failed to decode image");
        }

        Log.d(TAG, "Decoded bitmap dimensions: " + bitmap.getWidth() + "x" + bitmap.getHeight());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, baos);
        bitmap.recycle();

        if (!compressed) {
            throw new IOException("Failed to compress image");
        }

        byte[] imageBytes = baos.toByteArray();
        String base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        
        Log.d(TAG, "Image encoded successfully. Size: " + (imageBytes.length / 1024) + " KB");
        Log.d(TAG, "Base64 string length: " + base64String.length());

        return base64String;
    }

    private String callGeminiAPI(String base64Image) throws IOException {
        URL url = new URL(GEMINI_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-goog-api-key", GEMINI_API_KEY);
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Keep-Alive", "timeout=30");
        connection.setRequestProperty("User-Agent", "ScanMakanan-Android/1.0");
        connection.setDoOutput(true);
        connection.setConnectTimeout(15_000);
        connection.setReadTimeout(30_000);
        connection.setUseCaches(false);
        
        String jsonInputString = String.format(
            "{\n" +
            "    \"contents\": [\n" +
            "      {\n" +
            "        \"parts\": [\n" +
            "          {\n" +
            "            \"text\": \"What food or drink is in this image? Provide:\\nName: [specific name]\\nCalories: [number] kcal\\n\\nAnswer in exactly 2 lines.\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"inline_data\": {\n" +
            "              \"mime_type\": \"image/jpeg\",\n" +
            "              \"data\": \"%s\"\n" +
            "            }\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  }",
            base64Image.replace("\n", "")
        );
        
        Log.d(TAG, "Starting Gemini API call...");
        Log.d(TAG, "Request JSON length: " + jsonInputString.length());

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
            Log.d(TAG, "Request sent successfully");
        }
        
        int responseCode = connection.getResponseCode();
        Log.d(TAG, "Response code: " + responseCode);
        
        InputStream inputStream = (responseCode >= 200 && responseCode < 300) 
            ? connection.getInputStream() 
            : connection.getErrorStream();
            
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseString = response.toString();
            Log.d(TAG, "Response length: " + responseString.length());
            Log.d(TAG, "Response: " + responseString.substring(0, Math.min(200, responseString.length())));
            
            if (responseCode >= 200 && responseCode < 300) {
                return responseString;
            } else {
                Log.e(TAG, "API Error Response: " + responseString);
                throw new IOException("HTTP " + responseCode + ": " + responseString);
            }
        }
    }

    private void updateAnalysisResult(String analysisText) {
        TextView resultSubtitle = findViewById(R.id.result_subtitle);
        resultSubtitle.setText(analysisText);
    }
}
