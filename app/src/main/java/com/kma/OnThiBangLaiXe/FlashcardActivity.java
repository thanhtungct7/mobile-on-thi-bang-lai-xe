package com.kma.OnThiBangLaiXe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.kma.OnThiBangLaiXe.Model.BienBao;
import com.kma.OnThiBangLaiXe.Model.DanhSach;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FlashcardActivity extends AppCompatActivity {

    private List<BienBao> dsBienBao;
    private int currentIndex = 0;
    private boolean isFlipped = false;
    private boolean isAnimating = false;

    private MaterialCardView flashCardFront, flashCardBack;
    private ImageView ivSign;
    private TextView txtCounter;
    private TextView txtMaBBBack;
    private TextView txtTieuDeBack;
    private TextView txtNoiDungBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        Toolbar toolbar = findViewById(R.id.toolbarBack);
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText("Flashcard Biển báo");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        flashCardFront = findViewById(R.id.flashCardFront);
        flashCardBack = findViewById(R.id.flashCardBack);
        ivSign = findViewById(R.id.ivSign);
        txtCounter = findViewById(R.id.txtFlashcardCounter);
        txtMaBBBack = findViewById(R.id.txtMaBBBack);
        txtTieuDeBack = findViewById(R.id.txtTieuDeBack);
        txtNoiDungBack = findViewById(R.id.txtNoiDungBack);
        Button btnPrev = findViewById(R.id.btnPrev);
        Button btnNext = findViewById(R.id.btnNext);
        ScrollView backFace = findViewById(R.id.backFace);

        float scale = getResources().getDisplayMetrics().density;
        flashCardFront.setCameraDistance(8000 * scale);
        flashCardBack.setCameraDistance(8000 * scale);

        DBHandler db = new DBHandler(this);
        if (DanhSach.getDsBienBao().isEmpty()) {
            DanhSach.setDsBienBao(db.docBienBao());
        }
        dsBienBao = new ArrayList<>(DanhSach.getDsBienBao());

        showCard(0);

        flashCardFront.setOnClickListener(v -> flipCard());

        // GestureDetector để phân biệt tap đơn và scroll trên mặt sau
        GestureDetector gestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        flipCard();
                        return true;
                    }
                });
        backFace.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false; // cho phép ScrollView vẫn xử lý scroll
        });

        btnPrev.setOnClickListener(v -> navigate(-1));
        btnNext.setOnClickListener(v -> navigate(1));
    }

    private void showCard(int index) {
        currentIndex = index;
        txtCounter.setText((index + 1) + " / " + dsBienBao.size());

        BienBao bb = dsBienBao.get(index);
        txtMaBBBack.setText(bb.getMaBB());
        txtTieuDeBack.setText(bb.getTieuDe());
        txtNoiDungBack.setText(bb.getNoidung() != null && !bb.getNoidung().equals("null") ? bb.getNoidung() : "");

        try {
            InputStream is = getAssets().open("bien_bao/" + bb.getHinhAnh());
            Bitmap b = BitmapFactory.decodeStream(is);
            ivSign.setImageBitmap(b);
            is.close();
        } catch (Exception e) {
            ivSign.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        isFlipped = false;
        flashCardFront.setRotationY(0f);
        flashCardFront.setVisibility(android.view.View.VISIBLE);
        flashCardBack.setRotationY(180f);
        flashCardBack.setVisibility(android.view.View.GONE);
    }

    private void flipCard() {
        if (isAnimating) return;
        isAnimating = true;

        MaterialCardView fromCard = isFlipped ? flashCardBack : flashCardFront;
        MaterialCardView toCard = isFlipped ? flashCardFront : flashCardBack;

        // toCard bắt đầu ở phía sau (180° hoặc -180° tùy chiều lật)
        float toStartAngle = isFlipped ? -180f : 180f;
        float fromEndAngle = isFlipped ? 180f : -180f;

        toCard.setRotationY(toStartAngle);
        toCard.setVisibility(android.view.View.VISIBLE);

        ObjectAnimator fromAnim = ObjectAnimator.ofFloat(fromCard, "rotationY", 0f, fromEndAngle);
        ObjectAnimator toAnim = ObjectAnimator.ofFloat(toCard, "rotationY", toStartAngle, 0f);

        // Ẩn mặt đang quay đi khi qua ngưỡng 90°
        fromAnim.addUpdateListener(anim -> {
            float val = (float) anim.getAnimatedValue();
            if (Math.abs(val) >= 90f) {
                fromCard.setVisibility(android.view.View.INVISIBLE);
            }
        });

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(fromAnim, toAnim);
        animSet.setDuration(350);
        animSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fromCard.setVisibility(android.view.View.GONE);
                isFlipped = !isFlipped;
                isAnimating = false;
            }
        });
        animSet.start();
    }

    private void navigate(int direction) {
        if (isAnimating) return;
        int newIndex = currentIndex + direction;
        if (newIndex < 0 || newIndex >= dsBienBao.size()) return;
        showCard(newIndex);
    }
}
