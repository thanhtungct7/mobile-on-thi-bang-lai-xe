package com.kma.OnThiBangLaiXe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.kma.OnThiBangLaiXe.Adapter.CauTraLoiAdapter;
import com.kma.OnThiBangLaiXe.Model.CauTraLoi;
import com.kma.OnThiBangLaiXe.Model.DanhSach;
import com.kma.OnThiBangLaiXe.Fragment.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Mô tả file:
 * Activity hiển thị chi tiết từng câu trong kết quả bài thi.
 * File này nhận mã đề, vị trí câu và danh sách mã câu hỏi qua Intent,
 * gắn CauTraLoiAdapter ở chế độ xem lại để người dùng duyệt đáp án đúng/sai.
 */
public class ChiTietKetQuaActivity extends AppCompatActivity {
    public ViewPager2 vp;
    TextView txtTitle;
    Button btnNavBack, btnNavForward;
    Toolbar toolbarBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cau_hoi);
        txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText("Chi tiết kết quả");
        toolbarBack =findViewById(R.id.toolbarBack);
        btnNavBack = findViewById(R.id.btnNavBack);
        btnNavForward = findViewById(R.id.btnNavForward);
        findViewById(R.id.navPanel).setVisibility(View.GONE);
        vp = findViewById(R.id.vp);

        DBHandler db = new DBHandler(this);
        DanhSach.setDsCauHoi(db.docCauHoi());
        List<CauTraLoi> dsCauTraLoi = new ArrayList<>();

        int maDeThi = getIntent().getIntExtra("MaDeThi", 0);
        int viTri = getIntent().getIntExtra("ViTri", 0);
        int[] maCHList = getIntent().getIntArrayExtra("MaCHList");

        List<CauTraLoi> allCTL = db.getListCauTraLoiByMaDeThi(maDeThi);

        if (maCHList != null && maCHList.length > 0) {
            Map<Integer, CauTraLoi> ctlMap = new HashMap<>();
            for (CauTraLoi ctl : allCTL) ctlMap.put(ctl.getMaCH(), ctl);
            for (int maCH : maCHList) {
                CauTraLoi ctl = ctlMap.get(maCH);
                if (ctl != null) dsCauTraLoi.add(ctl);
            }
        } else {
            dsCauTraLoi.addAll(allCTL);
        }

        vp.setAdapter(new CauTraLoiAdapter(dsCauTraLoi, this, true));
        toolbarBack.setNavigationOnClickListener(view -> onBackPressed() );

        btnNavBack.setOnClickListener(v -> {
            if (vp.getCurrentItem() > 0) vp.setCurrentItem(vp.getCurrentItem() - 1, true);
        });

        btnNavForward.setOnClickListener(v -> {
            if (vp.getCurrentItem() < dsCauTraLoi.size() - 1)
                vp.setCurrentItem(vp.getCurrentItem() + 1, true);
        });

        if (dsCauTraLoi.isEmpty()) {
            btnNavBack.setEnabled(false);
            btnNavForward.setEnabled(false);
            return;
        }
        int safePosition = Math.max(0, Math.min(viTri, dsCauTraLoi.size() - 1));
        vp.setCurrentItem(safePosition, false);
    }

    @Override
    public void onBackPressed() {
        if (StudyFragment.tlchAdapter != null) StudyFragment.tlchAdapter.notifyDataSetChanged();
        super.onBackPressed();
    }
}
