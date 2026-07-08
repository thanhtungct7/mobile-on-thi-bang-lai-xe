package com.kma.OnThiBangLaiXe.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.kma.OnThiBangLaiXe.Adapter.CauHoiAdapter;
import com.kma.OnThiBangLaiXe.DBHandler;
import com.kma.OnThiBangLaiXe.Fragment.StudyFragment;
import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kma.OnThiBangLaiXe.R;

import java.util.ArrayList;
import java.util.List;

/*
 * Mô tả file:
 * Activity hiển thị các câu hỏi người dùng đã trả lời sai.
 * File này lấy danh sách câu sai từ database, gắn CauHoiAdapter cho ViewPager2
 * và dùng thanh điều hướng dưới để chuyển qua lại giữa các câu.
 */
public class CauSaiActivity extends AppCompatActivity {
    public ViewPager2 vp;
    TextView txtTitle;
    BottomNavigationView bnv;
    Toolbar toolbarBack;
    List<CauHoi> dsCauHoi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cau_sai);
        txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText("Câu sai");
        toolbarBack = findViewById(R.id.toolbarBack);
        toolbarBack.setNavigationOnClickListener(view -> onBackPressed() );
        bnv = findViewById(R.id.bottomNavigationView);
        vp = findViewById(R.id.vp);
        DBHandler db = new DBHandler(this);
        dsCauHoi = new ArrayList<>();
        dsCauHoi=db.docCauHoiSai();
        vp.setAdapter(new CauHoiAdapter(dsCauHoi, this));
        toolbarBack.setNavigationOnClickListener(view -> onBackPressed() );
        Menu menu = bnv.getMenu();
        bnv.setOnNavigationItemSelectedListener(item ->
        {
            int id = item.getItemId();
            if (id == R.id.tiBack) {
                if (vp.getCurrentItem() > 0) {
                    vp.setCurrentItem(vp.getCurrentItem() - 1, true);
                }
            } else if (id == R.id.tiForward) {
                if (vp.getCurrentItem() < dsCauHoi.size() - 1) {
                    vp.setCurrentItem(vp.getCurrentItem() + 1, true);
                }
            }
            return false;
        });

        menu.setGroupCheckable(0, false, true);
    }
    @Override
    public void onBackPressed() {
        if (StudyFragment.tlchAdapter != null) StudyFragment.tlchAdapter.notifyDataSetChanged();
        super.onBackPressed();
    }
}
