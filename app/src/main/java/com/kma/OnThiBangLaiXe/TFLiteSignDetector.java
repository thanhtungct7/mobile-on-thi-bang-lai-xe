package com.kma.OnThiBangLaiXe;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/*
 * Mô tả file:
 * Lớp chạy mô hình TensorFlow Lite để nhận diện biển báo từ ảnh.
 * File này load model/labels từ assets, tiền xử lý bitmap đầu vào,
 * chạy inference và trả về danh sách nhãn biển báo theo độ tin cậy giảm dần.
 */
public class TFLiteSignDetector {

    private static final int   INPUT_SIZE     = 640;
    private static final float CONF_THRESHOLD = 0.35f;
    private static final String MODEL_FILE    = "best.tflite";
    private static final String LABELS_FILE   = "labels.txt";

    public static class Detection {
        public final String label;
        public final float  confidence;

        Detection(String label, float confidence) {
            this.label      = label;
            this.confidence = confidence;
        }
    }

    private final Interpreter  interpreter;
    private final List<String> labels;

    public TFLiteSignDetector(Context ctx) throws IOException {
        interpreter = new Interpreter(loadModel(ctx));
        labels      = loadLabels(ctx);
    }

    // ─── Inference ────────────────────────────────────────────────────────────

    public Detection detect(Bitmap bitmap) {
        List<Detection> all = detectAll(bitmap);
        return all.isEmpty() ? null : all.get(0);
    }

    public List<Detection> detectAll(Bitmap bitmap) {
        ByteBuffer input = preprocess(bitmap);

        int[] shape = interpreter.getOutputTensor(0).shape();
        int dim1 = shape[1];
        int dim2 = shape[2];

        boolean transposed = (dim1 == 8400);
        int numAnchors  = transposed ? dim1 : dim2;
        int numFeatures = transposed ? dim2 : dim1;
        int numClasses  = numFeatures - 4;

        float[][][] output = new float[1][dim1][dim2];
        interpreter.run(input, output);

        // Mỗi anchor: lấy class có score cao nhất
        List<Detection> candidates = new ArrayList<>();
        for (int a = 0; a < numAnchors; a++) {
            float bestScore = CONF_THRESHOLD;
            int   bestClass = -1;
            for (int c = 0; c < numClasses; c++) {
                float score = transposed ? output[0][a][4 + c] : output[0][4 + c][a];
                if (score > bestScore) {
                    bestScore = score;
                    bestClass = c;
                }
            }
            if (bestClass >= 0 && bestClass < labels.size()) {
                candidates.add(new Detection(labels.get(bestClass), bestScore));
            }
        }

        // Gộp các detection cùng label: giữ cái confidence cao nhất mỗi class
        java.util.Map<String, Detection> best = new java.util.LinkedHashMap<>();
        for (Detection d : candidates) {
            Detection prev = best.get(d.label);
            if (prev == null || d.confidence > prev.confidence) {
                best.put(d.label, d);
            }
        }

        List<Detection> result = new ArrayList<>(best.values());
        result.sort((a2, b) -> Float.compare(b.confidence, a2.confidence));
        return result;
    }

    public void close() {
        interpreter.close();
    }

    // ─── Preprocessing ────────────────────────────────────────────────────────

    private ByteBuffer preprocess(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);

        ByteBuffer buf = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4);
        buf.order(ByteOrder.nativeOrder());

        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        resized.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);
        if (resized != bitmap) resized.recycle();

        for (int px : pixels) {
            buf.putFloat(((px >> 16) & 0xFF) / 255.0f); // R
            buf.putFloat(((px >>  8) & 0xFF) / 255.0f); // G
            buf.putFloat(( px        & 0xFF) / 255.0f); // B
        }
        return buf;
    }

    // ─── Asset loaders ────────────────────────────────────────────────────────

    private static MappedByteBuffer loadModel(Context ctx) throws IOException {
        AssetFileDescriptor fd = ctx.getAssets().openFd(MODEL_FILE);
        try (FileInputStream fis = new FileInputStream(fd.getFileDescriptor())) {
            return fis.getChannel().map(
                    FileChannel.MapMode.READ_ONLY,
                    fd.getStartOffset(),
                    fd.getDeclaredLength());
        }
    }

    private static List<String> loadLabels(Context ctx) throws IOException {
        List<String> list = new ArrayList<>();
        try (InputStream is = ctx.getAssets().open(LABELS_FILE)) {
            byte[] bytes = new byte[is.available()];
            //noinspection ResultOfMethodCallIgnored
            is.read(bytes);
            for (String line : new String(bytes).split("\n")) {
                String t = line.trim();
                if (!t.isEmpty()) list.add(t);
            }
        }
        return list;
    }
}
