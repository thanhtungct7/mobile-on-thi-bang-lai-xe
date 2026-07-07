package com.kma.OnThiBangLaiXe;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.RemoteViews;

import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.kma.OnThiBangLaiXe.Model.DanhSach;

import java.io.File;
import java.util.List;
import java.util.Random;

/*
 * Mô tả file:
 * AppWidgetProvider cho widget câu hỏi ngẫu nhiên.
 * File này chọn câu hỏi từ database, hiển thị đáp án trên RemoteViews,
 * xử lý đổi câu/trả lời A-B-C-D và lưu trạng thái từng widget bằng SharedPreferences.
 */
public class QuestionWidgetProvider extends AppWidgetProvider {

    static final String ACTION_REFRESH  = "com.kma.OnThiBangLaiXe.WIDGET_REFRESH";
    static final String ACTION_ANSWER_A = "com.kma.OnThiBangLaiXe.WIDGET_ANSWER_A";
    static final String ACTION_ANSWER_B = "com.kma.OnThiBangLaiXe.WIDGET_ANSWER_B";
    static final String ACTION_ANSWER_C = "com.kma.OnThiBangLaiXe.WIDGET_ANSWER_C";
    static final String ACTION_ANSWER_D = "com.kma.OnThiBangLaiXe.WIDGET_ANSWER_D";

    private static final String PREFS  = "MY_SHARED_PREFERENCES";
    private static final String KEY_LB = "LOAI_GPLX";

    // Request code bases (spaced apart to avoid collision across widgets)
    private static final int RC_OPEN    = 0;
    private static final int RC_REFRESH = 1;
    private static final int RC_A       = 2;
    private static final int RC_B       = 3;
    private static final int RC_C       = 4;
    private static final int RC_D       = 5;
    private static final int RC_STRIDE  = 10; // multiply widgetId by this

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) loadNewQuestion(ctx, mgr, id);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        super.onReceive(ctx, intent);
        String action = intent.getAction();
        if (action == null) return;

        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        if (widgetId == -1) return;

        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);

        if (ACTION_REFRESH.equals(action)) {
            loadNewQuestion(ctx, mgr, widgetId);
        } else if (ACTION_ANSWER_A.equals(action)) {
            handleAnswer(ctx, mgr, widgetId, "A");
        } else if (ACTION_ANSWER_B.equals(action)) {
            handleAnswer(ctx, mgr, widgetId, "B");
        } else if (ACTION_ANSWER_C.equals(action)) {
            handleAnswer(ctx, mgr, widgetId, "C");
        } else if (ACTION_ANSWER_D.equals(action)) {
            handleAnswer(ctx, mgr, widgetId, "D");
        }
    }

    // ─── Pick a new random question and render ───────────────────────────────

    private static void loadNewQuestion(Context ctx, AppWidgetManager mgr, int widgetId) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int loaiBang = prefs.getInt(KEY_LB, 1);
        DanhSach.setLoaiBang(loaiBang);

        try {
            DBHandler db = new DBHandler(ctx);
            List<CauHoi> all = db.docCauHoi();
            if (all.isEmpty()) { renderEmpty(ctx, mgr, widgetId); return; }

            CauHoi q = all.get(new Random().nextInt(all.size()));
            saveQuestion(prefs, widgetId, q);
            render(ctx, mgr, widgetId, q, "");
        } catch (Exception e) {
            renderEmpty(ctx, mgr, widgetId);
        }
    }

    // ─── Handle user tapping an answer ───────────────────────────────────────

    private static void handleAnswer(Context ctx, AppWidgetManager mgr, int widgetId, String selected) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // Already answered — ignore repeat taps
        if (!prefs.getString("w_" + widgetId + "_sel", "").isEmpty()) return;

        prefs.edit().putString("w_" + widgetId + "_sel", selected).apply();

        // Reconstruct question from prefs (no DB call needed)
        CauHoi q = loadQuestionFromPrefs(prefs, widgetId);
        if (q == null) { renderEmpty(ctx, mgr, widgetId); return; }

        render(ctx, mgr, widgetId, q, selected);
    }

    // ─── Rendering ───────────────────────────────────────────────────────────

    private static void render(Context ctx, AppWidgetManager mgr, int widgetId, CauHoi q, String selected) {
        RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_random_question);

        views.setTextViewText(R.id.widgetQuestion, q.getNoiDung());

        // Image
        Bitmap img = loadScaledBitmap(ctx, q.getHinhAnh());
        if (img != null) {
            views.setImageViewBitmap(R.id.widgetImage, img);
            views.setViewVisibility(R.id.widgetImage, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widgetImage, View.GONE);
        }

        boolean answered = !selected.isEmpty();
        String correct = q.getDapAnDung();

        renderOpt(ctx, views, R.id.widgetOptA, "A", q.getDapAnA(), answered, selected, correct, widgetId);
        renderOpt(ctx, views, R.id.widgetOptB, "B", q.getDapAnB(), answered, selected, correct, widgetId);
        renderOpt(ctx, views, R.id.widgetOptC, "C", q.getDapAnC(), answered, selected, correct, widgetId);
        renderOpt(ctx, views, R.id.widgetOptD, "D", q.getDapAnD(), answered, selected, correct, widgetId);

        wireGlobal(ctx, widgetId, views);
        mgr.updateAppWidget(widgetId, views);
    }

    private static void renderOpt(Context ctx, RemoteViews views, int viewId,
                                  String letter, String text,
                                  boolean answered, String selected, String correct,
                                  int widgetId) {
        if (text == null || text.equals("null") || text.trim().isEmpty()) {
            views.setViewVisibility(viewId, View.GONE);
            return;
        }
        views.setViewVisibility(viewId, View.VISIBLE);
        views.setTextViewText(viewId, letter + ".  " + text);

        if (!answered) {
            // Pre-answer: neutral button, clickable
            views.setInt(viewId, "setBackgroundResource", R.drawable.widget_answer_default);
            views.setTextColor(viewId, 0xFF5C4F3D);
            views.setOnClickPendingIntent(viewId, answerPendingIntent(ctx, widgetId, letter));
        } else {
            // Post-answer: show result colors
            boolean isCorrect  = letter.equals(correct);
            boolean isSelected = letter.equals(selected);

            if (isCorrect) {
                views.setInt(viewId, "setBackgroundResource", R.drawable.widget_answer_correct);
                views.setTextColor(viewId, 0xFFFFFFFF);
            } else if (isSelected) {
                views.setInt(viewId, "setBackgroundResource", R.drawable.widget_answer_wrong);
                views.setTextColor(viewId, 0xFFFFFFFF);
            } else {
                views.setInt(viewId, "setBackgroundResource", R.drawable.widget_answer_dim);
                views.setTextColor(viewId, 0x88777368);
            }
            // After answering: tapping answer area falls through to root → opens app
        }
    }

    private static void renderEmpty(Context ctx, AppWidgetManager mgr, int widgetId) {
        RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_random_question);
        views.setTextViewText(R.id.widgetQuestion, "Mở ứng dụng để tải câu hỏi");
        views.setViewVisibility(R.id.widgetImage, View.GONE);
        views.setViewVisibility(R.id.widgetOptA, View.GONE);
        views.setViewVisibility(R.id.widgetOptB, View.GONE);
        views.setViewVisibility(R.id.widgetOptC, View.GONE);
        views.setViewVisibility(R.id.widgetOptD, View.GONE);
        wireGlobal(ctx, widgetId, views);
        mgr.updateAppWidget(widgetId, views);
    }

    // ─── Global click wiring ─────────────────────────────────────────────────

    private static void wireGlobal(Context ctx, int widgetId, RemoteViews views) {
        // Root tap → open app
        Intent openIntent = new Intent(ctx, SplashActivity.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPi = PendingIntent.getActivity(ctx,
                widgetId * RC_STRIDE + RC_OPEN, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetRoot, openPi);

        // "↺ Đổi câu" tap → new question
        Intent refreshIntent = new Intent(ctx, QuestionWidgetProvider.class);
        refreshIntent.setAction(ACTION_REFRESH);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        PendingIntent refreshPi = PendingIntent.getBroadcast(ctx,
                widgetId * RC_STRIDE + RC_REFRESH, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetBtnRefresh, refreshPi);
    }

    private static PendingIntent answerPendingIntent(Context ctx, int widgetId, String letter) {
        String action;
        int rc;
        switch (letter) {
            case "A": action = ACTION_ANSWER_A; rc = RC_A; break;
            case "B": action = ACTION_ANSWER_B; rc = RC_B; break;
            case "C": action = ACTION_ANSWER_C; rc = RC_C; break;
            default:  action = ACTION_ANSWER_D; rc = RC_D; break;
        }
        Intent intent = new Intent(ctx, QuestionWidgetProvider.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return PendingIntent.getBroadcast(ctx, widgetId * RC_STRIDE + rc, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    // ─── SharedPreferences helpers ───────────────────────────────────────────

    private static void saveQuestion(SharedPreferences prefs, int id, CauHoi q) {
        prefs.edit()
                .putString("w_" + id + "_q",   q.getNoiDung())
                .putString("w_" + id + "_oa",  nvl(q.getDapAnA()))
                .putString("w_" + id + "_ob",  nvl(q.getDapAnB()))
                .putString("w_" + id + "_oc",  nvl(q.getDapAnC()))
                .putString("w_" + id + "_od",  nvl(q.getDapAnD()))
                .putString("w_" + id + "_cor", nvl(q.getDapAnDung()))
                .putString("w_" + id + "_img", nvl(q.getHinhAnh()))
                .putString("w_" + id + "_sel", "")
                .apply();
    }

    private static CauHoi loadQuestionFromPrefs(SharedPreferences prefs, int id) {
        String q = prefs.getString("w_" + id + "_q", "");
        if (q.isEmpty()) return null;
        CauHoi ch = new CauHoi();
        ch.setNoiDung(q);
        ch.setDapAnA(prefs.getString("w_" + id + "_oa", ""));
        ch.setDapAnB(prefs.getString("w_" + id + "_ob", ""));
        ch.setDapAnC(prefs.getString("w_" + id + "_oc", ""));
        ch.setDapAnD(prefs.getString("w_" + id + "_od", ""));
        ch.setDapAnDung(prefs.getString("w_" + id + "_cor", ""));
        String img = prefs.getString("w_" + id + "_img", "");
        ch.setHinhAnh(img.isEmpty() ? null : img);
        return ch;
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    // ─── Image loading ───────────────────────────────────────────────────────

    private static Bitmap loadScaledBitmap(Context ctx, String hinhAnh) {
        if (hinhAnh == null || hinhAnh.equals("null") || hinhAnh.trim().isEmpty()) return null;
        try {
            File f = new File(new ContextWrapper(ctx).getDir("images", Context.MODE_PRIVATE), hinhAnh);
            if (!f.exists()) return null;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
            int maxPx = 600, sample = 1;
            while (opts.outWidth / (sample * 2) >= maxPx || opts.outHeight / (sample * 2) >= maxPx) {
                sample *= 2;
            }
            opts.inJustDecodeBounds = false;
            opts.inSampleSize = sample;
            return BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
        } catch (Exception e) {
            return null;
        }
    }
}
