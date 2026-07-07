package com.kma.OnThiBangLaiXe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/*
 * Mô tả file:
 * View tùy chỉnh dùng để vẽ vòng tròn tiến độ dạng phần trăm.
 * File này tự vẽ track, cung tiến độ và số phần trăm ở giữa,
 * đồng thời cho phép đổi màu để dùng ở dashboard hoặc màn hình kết quả.
 */
public class ReadinessRingView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arcPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint unitPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();

    private int progress = 0;

    public ReadinessRingView(Context ctx) { this(ctx, null); }
    public ReadinessRingView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        float stroke = dpToPx(7);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(stroke);
        trackPaint.setColor(0x2EFFFFFF);

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(stroke);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        arcPaint.setColor(0xEBFFFFFF);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(dpToPx(34));
        textPaint.setFakeBoldText(true);

        unitPaint.setTextAlign(Paint.Align.CENTER);
        unitPaint.setColor(0xA6FFFFFF);
        unitPaint.setTextSize(dpToPx(13));
    }

    public void setProgress(int percent) {
        progress = Math.max(0, Math.min(100, percent));
        invalidate();
    }

    public void setColors(int trackColor, int arcColor, int textColor) {
        trackPaint.setColor(trackColor);
        arcPaint.setColor(arcColor);
        textPaint.setColor(textColor);
        unitPaint.setColor(textColor & 0x99FFFFFF);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - dpToPx(7) / 2f - dpToPx(2);

        oval.set(cx - radius, cy - radius, cx + radius, cy + radius);

        canvas.drawOval(oval, trackPaint);

        float sweep = 360f * progress / 100f;
        canvas.drawArc(oval, -90f, sweep, false, arcPaint);

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = cy - (fm.descent + fm.ascent) / 2f - dpToPx(8);
        canvas.drawText(progress + "", cx, textY, textPaint);
        canvas.drawText("%", cx, textY + dpToPx(18), unitPaint);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
