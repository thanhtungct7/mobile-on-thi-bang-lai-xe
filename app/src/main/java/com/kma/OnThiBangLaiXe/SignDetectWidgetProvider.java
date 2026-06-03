package com.kma.OnThiBangLaiXe;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.RemoteViews;

public class SignDetectWidgetProvider extends AppWidgetProvider {

    private static final int RC_OPEN = 0;
    private static final int RC_STRIDE = 10;

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) {
            RemoteViews views = buildViewsFromPrefs(ctx, id);
            mgr.updateAppWidget(id, views);
        }
    }

    // ─── Build RemoteViews ────────────────────────────────────────────────────

    /**
     * Dùng từ SignDetectActivity sau khi chụp — truyền trực tiếp bitmap + kết quả.
     */
    static RemoteViews buildViews(Context ctx, int widgetId,
                                  Bitmap thumb, String label, float conf) {
        RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_sign_detect);

        if (thumb != null) {
            views.setImageViewBitmap(R.id.widgetDetectImage, thumb);
            views.setViewVisibility(R.id.widgetDetectImage, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widgetDetectImage, View.GONE);
        }

        if (label != null && !label.isEmpty()) {
            views.setTextViewText(R.id.widgetDetectLabel, label);
            views.setViewVisibility(R.id.widgetDetectLabel, View.VISIBLE);
            String confText = String.format("Độ chính xác: %.0f%%", conf * 100);
            views.setTextViewText(R.id.widgetDetectConf, confText);
            views.setViewVisibility(R.id.widgetDetectConf, View.VISIBLE);
        } else if (thumb != null) {
            views.setTextViewText(R.id.widgetDetectLabel, "Không phát hiện biển báo");
            views.setViewVisibility(R.id.widgetDetectLabel, View.VISIBLE);
            views.setTextViewText(R.id.widgetDetectConf, "Thử lại với góc chụp rõ hơn");
            views.setViewVisibility(R.id.widgetDetectConf, View.VISIBLE);
        } else {
            views.setTextViewText(R.id.widgetDetectLabel, "Chưa có kết quả nhận diện");
            views.setViewVisibility(R.id.widgetDetectLabel, View.VISIBLE);
            views.setViewVisibility(R.id.widgetDetectConf, View.GONE);
        }

        wireCameraButton(ctx, widgetId, views);
        return views;
    }

    /**
     * Dùng khi widget khởi động lại — load kết quả từ SharedPreferences.
     */
    private static RemoteViews buildViewsFromPrefs(Context ctx, int widgetId) {
        SharedPreferences prefs = ctx.getSharedPreferences(
                SignDetectActivity.PREFS_DETECT, Context.MODE_PRIVATE);

        String imgPath = prefs.getString(SignDetectActivity.KEY_IMG_PATH, "");
        String label   = prefs.getString(SignDetectActivity.KEY_LABEL,    "");
        float  conf    = prefs.getFloat(SignDetectActivity.KEY_CONF,       0f);

        Bitmap thumb = null;
        if (!imgPath.isEmpty()) {
            Bitmap full = BitmapFactory.decodeFile(imgPath);
            if (full != null) {
                thumb = Bitmap.createScaledBitmap(full, 320, 180, true);
                full.recycle();
            }
        }

        RemoteViews views = buildViews(ctx, widgetId, thumb, label.isEmpty() ? null : label, conf);
        if (thumb != null) thumb.recycle();
        return views;
    }

    // ─── Wire camera button → launch SignDetectActivity ───────────────────────

    private static void wireCameraButton(Context ctx, int widgetId, RemoteViews views) {
        Intent intent = new Intent(ctx, SignDetectActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(ctx,
                widgetId * RC_STRIDE + RC_OPEN, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        views.setOnClickPendingIntent(R.id.widgetBtnOpenCamera, pi);
        views.setOnClickPendingIntent(R.id.widgetDetectRoot, pi);
    }
}
