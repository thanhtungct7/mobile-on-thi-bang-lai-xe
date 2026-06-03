package com.kma.OnThiBangLaiXe;

import static com.kma.OnThiBangLaiXe.Model.DanhSach.getDsBienBao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.kma.OnThiBangLaiXe.Adapter.LoaiBienBaoAdapter;
import com.kma.OnThiBangLaiXe.Model.DanhSach;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class BienBaoActivity extends AppCompatActivity {
    DBHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbar toolbarBienBao;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bien_bao);
        db=new DBHandler(this);
        if (DanhSach.getDsBienBao().isEmpty()) {
            DanhSach.setDsBienBao(db.docBienBao());
            DanhSach.setDsLoaiBienBao(db.docLoaiBienBao());
        }
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        toolbarBienBao = findViewById(R.id.toolbarBienBao);
        toolbarBienBao.setNavigationOnClickListener(view -> onBackPressed());

        TextView btnFlashcard = findViewById(R.id.btnFlashcard);
        btnFlashcard.setOnClickListener(v ->
                startActivity(new Intent(this, FlashcardActivity.class)));

        ViewPager2 vp = findViewById(R.id.vp);
        LoaiBienBaoAdapter lbbAdapter = new LoaiBienBaoAdapter(DanhSach.getDsLoaiBienBao(), this, getDsBienBao());
        vp.setAdapter(lbbAdapter);

        new TabLayoutMediator(tabLayout, vp, (tab, position)
                -> tab.setText(DanhSach.getDsLoaiBienBao().get(position).getTenLoaiBB())).attach();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DanhSach.clearBienBao();
    }
}