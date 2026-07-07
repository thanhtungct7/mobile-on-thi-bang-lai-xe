package com.kma.OnThiBangLaiXe;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Mô tả file:
 * Activity nhận diện biển báo bằng camera và mô hình TFLite.
 * File này xin quyền camera, chụp ảnh bằng CameraX, chạy detector,
 * ánh xạ nhãn AI sang dữ liệu biển báo, mở màn hình chi tiết và cập nhật widget nhận diện.
 */
public class SignDetectActivity extends AppCompatActivity {

    private static final int RC_CAMERA = 101;
    static final String PREFS_DETECT = "SIGN_DETECT_PREFS";
    static final String KEY_LABEL    = "last_label";
    static final String KEY_CONF     = "last_conf";
    static final String KEY_IMG_PATH = "last_img_path";

    // Convert TFLite label (e.g. "P-102", "W-201a") → MaBB in bien_bao_assets.json
    private static String labelToMaBB(String label) {
        if (label == null) return null;
        switch (label) {
            case "P-124a": return "P124a1";
            case "P-124b": return "P124b1";
            case "P-127":  return "DP127a";
            case "R-407a": return "I407a";
            case "R-409":  return "I409";
            case "R-425":  return "R420";
            case "R-434":  return "I434a";
            default:       return label.replace("-", "");
        }
    }

    private PreviewView previewView;
    private ImageView   ivCaptured;
    private TextView    tvResult;
    private Button      btnCapture, btnRetake, btnViewDetail;

    private ImageCapture imageCapture;
    private TFLiteSignDetector detector;

    // Keyed by MaBB
    private final Map<String, String[]> bienBaoMap = new HashMap<>();

    private String currentLabel = null;
    private float  currentConf  = 0f;
    private List<TFLiteSignDetector.Detection> currentDetections = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_detect);

        Toolbar toolbar = findViewById(R.id.toolbarDetect);
        toolbar.setTitle("Nhận diện biển báo");
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> finish());

        previewView   = findViewById(R.id.previewView);
        ivCaptured    = findViewById(R.id.ivCaptured);
        tvResult      = findViewById(R.id.tvResult);
        btnCapture    = findViewById(R.id.btnCapture);
        btnRetake     = findViewById(R.id.btnRetake);
        btnViewDetail = findViewById(R.id.btnViewDetail);

        btnCapture.setOnClickListener(v -> takePhoto());
        btnRetake.setOnClickListener(v -> resetToPreview());
        btnViewDetail.setOnClickListener(v -> openDetail());

        loadBienBaoData();

        try {
            detector = new TFLiteSignDetector(this);
        } catch (Exception e) {
            Log.w("SignDetect", "TFLite model not found — place best.tflite in assets/");
            detector = null;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.CAMERA}, RC_CAMERA);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (detector != null) detector.close();
    }

    // ─── Sign DB ──────────────────────────────────────────────────────────────

    private void loadBienBaoData() {
        try (InputStream is = getAssets().open("bien_bao_assets.json")) {
            byte[] buf = new byte[is.available()];
            //noinspection ResultOfMethodCallIgnored
            is.read(buf);
            JSONObject root = new JSONObject(new String(buf));
            JSONArray arr = root.getJSONArray("BienBao");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String maBB    = o.optString("MaBB",    "");
                String tieuDe  = o.optString("TieuDe",  "");
                String noiDung = o.optString("NoiDung", "");
                String hinhAnh = o.optString("HinhAnh", "");
                if (!maBB.isEmpty()) {
                    bienBaoMap.put(maBB, new String[]{tieuDe, noiDung, hinhAnh});
                }
            }
        } catch (Exception e) {
            Log.w("SignDetect", "Could not load bien_bao_assets.json: " + e.getMessage());
        }
    }

    // ─── Camera ──────────────────────────────────────────────────────────────

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture);

            } catch (Exception e) {
                Toast.makeText(this, "Không thể mở camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        btnCapture.setEnabled(false);
        tvResult.setText("Đang xử lý...");

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        Bitmap bitmap = imageProxyToBitmap(image);
                        image.close();
                        onPhotoCaptured(bitmap);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        runOnUiThread(() -> {
                            btnCapture.setEnabled(true);
                            tvResult.setText("Chụp ảnh thất bại, thử lại");
                        });
                    }
                });
    }

    private void onPhotoCaptured(Bitmap bitmap) {
        ivCaptured.setImageBitmap(bitmap);
        ivCaptured.setVisibility(View.VISIBLE);

        String imgPath = saveBitmapToCache(bitmap);

        btnCapture.setVisibility(View.GONE);
        btnRetake.setVisibility(View.VISIBLE);
        btnViewDetail.setVisibility(View.GONE);
        currentLabel = null;
        currentConf  = 0f;
        currentDetections.clear();

        if (detector == null) {
            tvResult.setText("Model chưa sẵn sàng.\nĐặt file best.tflite vào app/src/main/assets/");
            saveDetectResult(imgPath, null, 0f);
            updateAllWidgets(bitmap, null, 0f);
            return;
        }

        tvResult.setText("Đang phân tích...");
        Bitmap copy = bitmap.copy(bitmap.getConfig(), false);

        new Thread(() -> {
            List<TFLiteSignDetector.Detection> results = detector.detectAll(copy);
            copy.recycle();
            runOnUiThread(() -> {
                currentDetections.clear();
                if (!results.isEmpty()) {
                    currentDetections.addAll(results);
                    TFLiteSignDetector.Detection top = results.get(0);
                    currentLabel = top.label;
                    currentConf  = top.confidence;

                    StringBuilder sb = new StringBuilder();
                    for (TFLiteSignDetector.Detection d : results) {
                        sb.append("• ").append(getDisplayName(d.label))
                          .append(String.format("  (%.0f%%)\n", d.confidence * 100));
                    }
                    tvResult.setText(sb.toString().trim());
                    btnViewDetail.setVisibility(View.VISIBLE);
                } else {
                    tvResult.setText("Không phát hiện biển báo.\nThử lại với góc chụp rõ hơn.");
                }
                saveDetectResult(imgPath, currentLabel, currentConf);
                updateAllWidgets(bitmap, currentLabel, currentConf);
            });
        }).start();
    }

    private void resetToPreview() {
        ivCaptured.setVisibility(View.GONE);
        ivCaptured.setImageBitmap(null);
        btnCapture.setEnabled(true);
        btnCapture.setVisibility(View.VISIBLE);
        btnRetake.setVisibility(View.GONE);
        btnViewDetail.setVisibility(View.GONE);
        tvResult.setText("Chụp ảnh để nhận diện biển báo");
        currentLabel = null;
        currentConf  = 0f;
        currentDetections.clear();
    }

    private void openDetail() {
        if (currentDetections.isEmpty()) return;

        if (currentDetections.size() == 1) {
            openDetailForDetection(currentDetections.get(0));
            return;
        }

        // Nhiều biển: cho người dùng chọn
        String[] items = new String[currentDetections.size()];
        for (int i = 0; i < currentDetections.size(); i++) {
            TFLiteSignDetector.Detection d = currentDetections.get(i);
            items[i] = String.format("%s  (%.0f%%)", getDisplayName(d.label), d.confidence * 100);
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("Chọn biển báo để xem chi tiết")
                .setItems(items, (dialog, which) ->
                        openDetailForDetection(currentDetections.get(which)))
                .show();
    }

    private void openDetailForDetection(TFLiteSignDetector.Detection d) {
        String maBB = labelToMaBB(d.label);
        String[] info = (maBB != null) ? bienBaoMap.get(maBB) : null;

        String tieuDe  = (info != null && !info[0].isEmpty()) ? info[0] : d.label;
        String noiDung = (info != null && !info[1].isEmpty()) ? info[1]
                : String.format("Biển báo nhận diện bằng AI\nĐộ chính xác: %.0f%%", d.confidence * 100);
        String hinhAnh = (info != null) ? info[2] : "";
        String maBBDisplay = (maBB != null) ? maBB : "";

        Intent intent = new Intent(this, ChiTietBienBaoActivity.class);
        intent.putExtra("TieuDe",  tieuDe);
        intent.putExtra("MaBB",    maBBDisplay);
        intent.putExtra("NoiDung", noiDung);
        intent.putExtra("HinhAnh", hinhAnh);
        startActivity(intent);
    }

    // ─── Widget update ────────────────────────────────────────────────────────

    private void saveDetectResult(String imgPath, String label, float conf) {
        getSharedPreferences(PREFS_DETECT, Context.MODE_PRIVATE).edit()
                .putString(KEY_IMG_PATH, imgPath == null ? "" : imgPath)
                .putString(KEY_LABEL,    label    == null ? "" : label)
                .putFloat(KEY_CONF,      conf)
                .apply();
    }

    private void updateAllWidgets(Bitmap fullBitmap, String label, float conf) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(this);
        ComponentName component = new ComponentName(this, SignDetectWidgetProvider.class);
        int[] ids = mgr.getAppWidgetIds(component);
        if (ids == null || ids.length == 0) return;

        Bitmap thumb = Bitmap.createScaledBitmap(fullBitmap, 320, 180, true);

        String displayLabel = (label != null) ? getDisplayName(label) : null;
        for (int id : ids) {
            RemoteViews views = SignDetectWidgetProvider.buildViews(this, id, thumb, displayLabel, conf);
            mgr.updateAppWidget(id, views);
        }
        thumb.recycle();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /** Returns the Vietnamese sign name if available, otherwise the raw label. */
    String getDisplayName(String label) {
        if (label == null) return "";
        String maBB = labelToMaBB(label);
        if (maBB != null) {
            String[] info = bienBaoMap.get(maBB);
            if (info != null && !info[1].isEmpty()) return info[1];
        }
        return label;
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int rotation = image.getImageInfo().getRotationDegrees();
        if (rotation == 0) return bmp;
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        Bitmap rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        bmp.recycle();
        return rotated;
    }

    private String saveBitmapToCache(Bitmap bitmap) {
        try {
            File cacheFile = new File(getCacheDir(), "last_detect.jpg");
            FileOutputStream out = new FileOutputStream(cacheFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.close();
            return cacheFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    // ─── Permission ───────────────────────────────────────────────────────────

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Cần quyền camera để sử dụng tính năng này",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
