package com.kma.OnThiBangLaiXe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private MaterialCardView flashCard;
    private LinearLayout frontFace;
    private ScrollView backFace;
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

        flashCard = findViewById(R.id.flashCard);
        frontFace = findViewById(R.id.frontFace);
        backFace = findViewById(R.id.backFace);
        ivSign = findViewById(R.id.ivSign);
        txtCounter = findViewById(R.id.txtFlashcardCounter);
        txtMaBBBack = findViewById(R.id.txtMaBBBack);
        txtTieuDeBack = findViewById(R.id.txtTieuDeBack);
        txtNoiDungBack = findViewById(R.id.txtNoiDungBack);
        Button btnPrev = findViewById(R.id.btnPrev);
        Button btnNext = findViewById(R.id.btnNext);

        float scale = getResources().getDisplayMetrics().density;
        flashCard.setCameraDistance(8000 * scale);

        DBHandler db = new DBHandler(this);
        if (DanhSach.getDsBienBao().isEmpty()) {
            DanhSach.setDsBienBao(db.docBienBao());
        }
        dsBienBao = new ArrayList<>(DanhSach.getDsBienBao());

        showCard(0);

        flashCard.setOnClickListener(v -> flipCard());
        backFace.setOnClickListener(v -> flipCard());
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
        frontFace.setVisibility(View.VISIBLE);
        backFace.setVisibility(View.GONE);
        flashCard.setRotationY(0f);
    }

    private void flipCard() {
        if (isAnimating) return;
        isAnimating = true;

        ObjectAnimator firstHalf = ObjectAnimator.ofFloat(flashCard, "rotationY", 0f, 90f);
        firstHalf.setDuration(150);
        firstHalf.setInterpolator(new AccelerateInterpolator());
        firstHalf.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isFlipped = !isFlipped;
                if (isFlipped) {
                    frontFace.setVisibility(View.GONE);
                    backFace.setVisibility(View.VISIBLE);
                } else {
                    frontFace.setVisibility(View.VISIBLE);
                    backFace.setVisibility(View.GONE);
                }
                ObjectAnimator secondHalf = ObjectAnimator.ofFloat(flashCard, "rotationY", -90f, 0f);
                secondHalf.setDuration(150);
                secondHalf.setInterpolator(new DecelerateInterpolator());
                secondHalf.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isAnimating = false;
                    }
                });
                secondHalf.start();
            }
        });
        firstHalf.start();
    }

    private void navigate(int direction) {
        if (isAnimating) return;
        int newIndex = currentIndex + direction;
        if (newIndex < 0 || newIndex >= dsBienBao.size()) return;
        showCard(newIndex);
    }
}
