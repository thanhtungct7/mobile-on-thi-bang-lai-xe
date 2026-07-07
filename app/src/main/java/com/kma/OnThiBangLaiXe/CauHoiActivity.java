package com.kma.OnThiBangLaiXe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.kma.OnThiBangLaiXe.Adapter.CauHoiAdapter;
import com.kma.OnThiBangLaiXe.Fragment.StudyFragment;
import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.kma.OnThiBangLaiXe.Model.DanhSach;

import java.util.ArrayList;
import java.util.List;

/*
 * Mô tả file:
 * Activity hiển thị các câu hỏi ôn tập bằng ViewPager2.
 * File này lọc câu hỏi theo mã loại câu hỏi hoặc chỉ các câu đã làm sai,
 * gắn CauHoiAdapter và xử lý nút điều hướng câu trước/câu sau.
 */
public class CauHoiActivity extends AppCompatActivity {
    public ViewPager2 vp;
    TextView txtTitle;
    Button btnNavBack, btnNavForward;
    Toolbar toolbarBack;
    List<CauHoi> dsCauHoi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cau_hoi);
        txtTitle = findViewById(R.id.txtTitle);
        toolbarBack = findViewById(R.id.toolbarBack);
        findViewById(R.id.navPanel).setVisibility(View.GONE);

        int maLoaiCH = getIntent().getIntExtra("MaLoaiCH", 0);
        boolean chiHienThiSai = getIntent().getBooleanExtra("ChiHienThiSai", false);
        txtTitle.setText(chiHienThiSai ? "Câu sai" : "Câu hỏi ôn thi");
        btnNavBack = findViewById(R.id.btnNavBack);
        btnNavForward = findViewById(R.id.btnNavForward);
        vp = findViewById(R.id.vp);
        DBHandler db = new DBHandler(this);
        DanhSach.setDsCauHoi(db.docCauHoi());
        dsCauHoi = new ArrayList<>();
        for (CauHoi a : DanhSach.getDsCauHoi()) {
            if (maLoaiCH != 0 && a.getMaLoaiCH() != maLoaiCH) continue;
            if (chiHienThiSai && a.getDaTraLoiDung() != 2) continue;
            dsCauHoi.add(a);
        }


        vp.setAdapter(new CauHoiAdapter(dsCauHoi, this));
        toolbarBack.setNavigationOnClickListener(view -> onBackPressed());

        btnNavBack.setOnClickListener(v -> {
            if (vp.getCurrentItem() > 0) vp.setCurrentItem(vp.getCurrentItem() - 1, true);
        });
        btnNavForward.setOnClickListener(v -> {
            if (vp.getCurrentItem() < dsCauHoi.size() - 1)
                vp.setCurrentItem(vp.getCurrentItem() + 1, true);
        });
    }

    @Override
    public void onBackPressed() {
        // StudyFragment.onResume() and DashboardFragment.onResume() refresh their own state
        if (StudyFragment.tlchAdapter != null) StudyFragment.tlchAdapter.notifyDataSetChanged();
        super.onBackPressed();
    }
}
