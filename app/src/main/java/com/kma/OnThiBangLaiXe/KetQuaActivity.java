package com.kma.OnThiBangLaiXe;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.kma.OnThiBangLaiXe.Fragment.ResultFragment;
import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.kma.OnThiBangLaiXe.Model.CauTraLoi;
import com.kma.OnThiBangLaiXe.Model.DanhSach;

import java.util.ArrayList;
import java.util.List;

/*
 * Mô tả file:
 * Activity tổng hợp kết quả sau khi làm đề thi.
 * File này tính số câu đúng/sai/bỏ qua, xác định đạt hoặc không đạt,
 * hiển thị thời gian làm bài, bộ lọc câu hỏi và danh sách câu trong ResultFragment.
 */
public class KetQuaActivity extends AppCompatActivity {
    DBHandler db;
    int type = 0, maDeThi;

    LinearLayout btnAll, btnTrue, btnFalse, btnNull;
    TextView txtCountAll, txtCountTrue, txtCountFalse, txtCountNull;
    public TextView ThiLai, txtlyDo, txtKetQua, txtScore, txtScoreNum, txtScoreDenom, txtThoiGianLam, txtThoiGianConLai;
    ReadinessRingView ringScore;

    List<CauTraLoi> dsCTL;

    public int getType() { return type; }

    void init() {
        btnAll   = findViewById(R.id.btnAll);
        btnTrue  = findViewById(R.id.btnTrue);
        btnFalse = findViewById(R.id.btnFalse);
        btnNull  = findViewById(R.id.btnNull);

        txtCountAll   = findViewById(R.id.txtCountAll);
        txtCountTrue  = findViewById(R.id.txtCountTrue);
        txtCountFalse = findViewById(R.id.txtCountFalse);
        txtCountNull  = findViewById(R.id.txtCountNull);

        ThiLai            = findViewById(R.id.txtThiLai);
        txtlyDo           = findViewById(R.id.txtLyDo);
        txtKetQua         = findViewById(R.id.txtResult);
        txtScore          = findViewById(R.id.txtScore);
        txtScoreNum       = findViewById(R.id.txtScoreNum);
        txtScoreDenom     = findViewById(R.id.txtScoreDenom);
        txtThoiGianLam    = findViewById(R.id.txtThoiGianLam);
        txtThoiGianConLai = findViewById(R.id.txtThoiGianConLai);
        ringScore         = findViewById(R.id.ringScore);

        ThiLai.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ket_qua);

        Intent intent = getIntent();
        maDeThi = intent.getIntExtra("MaDeThi", 1);
        long tongThoiGianMacDinh = DanhSach.getLoaiBang() == 2 ? 1_200_000L : 1_140_000L;
        long tongThoiGian = intent.getLongExtra("TongThoiGian", tongThoiGianMacDinh);
        boolean coDuLieuThoiGian = intent.hasExtra("ThoiGianConLai");
        long thoiGianConLaiMs = coDuLieuThoiGian
                ? intent.getLongExtra("ThoiGianConLai", 0)
                : 0;
        thoiGianConLaiMs = Math.max(0L, Math.min(thoiGianConLaiMs, tongThoiGian));
        long thoiGianLamMs = Math.max(0L, tongThoiGian - thoiGianConLaiMs);

        db = new DBHandler(this);

        Toolbar toolbarBack = findViewById(R.id.toolbarBack);
        toolbarBack.setNavigationOnClickListener(view -> onBackPressed());

        init();

        // --- Count answers ---
        int soCauDung = 0, soCauSai = 0, soCauChuaTraLoi = 0;
        boolean saiDiemLiet = false;
        dsCTL = new ArrayList<>();

        if (maDeThi == 0) {
            for (CauTraLoi ctl : DanhSach.getDsCauTraLoi()) {
                if (ctl.getMaDeThi() == maDeThi) dsCTL.add(ctl);
            }
        } else {
            dsCTL = db.getListCauTraLoiByMaDeThi(maDeThi);
        }

        for (CauTraLoi ctl : dsCTL) {
            String dapAnChon = ctl.getDapAnChon();
            CauHoi cauHoi = db.getCauHoiByID(ctl.getMaCH());
            boolean coKetQuaThi = dapAnChon != null && !dapAnChon.equals("null");
            boolean boQua = !coKetQuaThi || dapAnChon.equals("0");
            boolean traLoiDung = cauHoi != null && cauHoi.getDapAnDung().equals(dapAnChon);

            if (boQua)
                soCauChuaTraLoi++;
            else if (traLoiDung)
                soCauDung++;
            else
                soCauSai++;

            if (coKetQuaThi && cauHoi != null && cauHoi.getMaLoaiCH() == 1 && !traLoiDung) {
                saiDiemLiet = true;
            }
        }

        int loaiBang  = DanhSach.getLoaiBang();
        int nguongDat = (loaiBang == 2) ? 27 : 22;
        int tongCau   = (loaiBang == 2) ? 30 : 25;

        int total = dsCTL.size();

        // --- Update counts ---
        txtCountAll.setText(String.valueOf(total));
        txtCountTrue.setText(String.valueOf(soCauDung));
        txtCountFalse.setText(String.valueOf(soCauSai));
        txtCountNull.setText(String.valueOf(soCauChuaTraLoi));

        txtScoreNum.setText(String.valueOf(soCauDung));
        txtScoreDenom.setText("/" + tongCau);
        txtScore.setText("Điểm đạt là " + nguongDat + "/" + tongCau + " câu.");

        // --- Time stats ---
        if (coDuLieuThoiGian) {
            txtThoiGianLam.setText(formatTime(thoiGianLamMs));
            txtThoiGianConLai.setText(formatTime(thoiGianConLaiMs));
        } else {
            txtThoiGianLam.setText("--:--");
            txtThoiGianConLai.setText("--:--");
        }

        // --- Ring ---
        int phanTram = tongCau > 0 ? soCauDung * 100 / tongCau : 0;
        ringScore.setProgress(phanTram);

        // --- Show bubble grid ---
        senDataToFrm(dsCTL);
        setFilterActive(0);

        // --- Filter listeners ---
        btnAll.setOnClickListener(view -> { senDataToFrm(dsCTL); setFilterActive(0); });

        btnTrue.setOnClickListener(view -> {
            List<CauTraLoi> a = new ArrayList<>();
            for (CauTraLoi ctl : dsCTL) {
                if (ctl != null && ctl.getDapAnChon() != null
                        && ctl.getDapAnChon().equals(db.getCauHoiByID(ctl.getMaCH()).getDapAnDung()))
                    a.add(ctl);
            }
            senDataToFrm(a);
            setFilterActive(1);
        });

        btnFalse.setOnClickListener(view -> {
            List<CauTraLoi> a = new ArrayList<>();
            for (CauTraLoi ctl : dsCTL) {
                if (ctl != null && ctl.getDapAnChon() != null
                        && !ctl.getDapAnChon().equals(db.getCauHoiByID(ctl.getMaCH()).getDapAnDung())
                        && !ctl.getDapAnChon().equals("0"))
                    a.add(ctl);
            }
            senDataToFrm(a);
            setFilterActive(2);
        });

        btnNull.setOnClickListener(view -> {
            List<CauTraLoi> a = new ArrayList<>();
            for (CauTraLoi ctl : dsCTL) {
                if (ctl.getDapAnChon() == null || ctl.getDapAnChon().equals("0"))
                    a.add(ctl);
            }
            senDataToFrm(a);
            setFilterActive(3);
        });

        ThiLai.setOnClickListener(view -> {
            Intent intent1 = new Intent(KetQuaActivity.this, ThiThuActivity.class);
            intent1.putExtra("MaDeThi", maDeThi);
            startActivity(intent1);
            finish();
        });

        // --- Verdict & colors ---
        boolean dat = soCauDung >= nguongDat && !saiDiemLiet;
        if (dat) {
            txtlyDo.setText("");
            txtKetQua.setText("ĐẠT");
            txtKetQua.setTextColor(ContextCompat.getColor(this, R.color.result_pass_fg));
            txtScoreNum.setTextColor(ContextCompat.getColor(this, R.color.result_pass_fg));
            ringScore.setColors(
                ContextCompat.getColor(this, R.color.result_pass_border),
                ContextCompat.getColor(this, R.color.result_pass_fg),
                ContextCompat.getColor(this, R.color.result_pass_fg)
            );
        } else {
            ringScore.setColors(
                ContextCompat.getColor(this, R.color.result_fail_border),
                ContextCompat.getColor(this, R.color.result_fail_fg),
                ContextCompat.getColor(this, R.color.result_fail_fg)
            );
            txtKetQua.setText("KHÔNG ĐẠT");
            txtKetQua.setTextColor(ContextCompat.getColor(this, R.color.result_fail_fg));
            txtScoreNum.setTextColor(ContextCompat.getColor(this, R.color.result_fail_fg));
            if (saiDiemLiet) {
                txtlyDo.setText("Không trả lời đúng câu điểm liệt");
            } else {
                txtlyDo.setText("Cần đúng ≥ " + nguongDat + " câu để đạt");
            }
        }
    }

    void setFilterActive(int index) {
        LinearLayout[] tabs   = {btnAll, btnTrue, btnFalse, btnNull};
        TextView[]     counts = {txtCountAll, txtCountTrue, txtCountFalse, txtCountNull};
        int[] defaultColors   = {R.color.fg, R.color.result_pass_fg, R.color.result_fail_fg, R.color.muted};

        for (int i = 0; i < tabs.length; i++) {
            tabs[i].setBackgroundColor(Color.TRANSPARENT);
            counts[i].setTextColor(ContextCompat.getColor(this, defaultColors[i]));
            ((TextView) tabs[i].getChildAt(1)).setTextColor(ContextCompat.getColor(this, R.color.muted));
        }

        tabs[index].setBackgroundColor(ContextCompat.getColor(this, R.color.fg));
        counts[index].setTextColor(Color.WHITE);
        ((TextView) tabs[index].getChildAt(1)).setTextColor(Color.argb(200, 255, 255, 255));
    }

    private String formatTime(long ms) {
        int min = (int) (ms / 60000);
        int sec = (int) (ms % 60000 / 1000);
        return min + ":" + (sec < 10 ? "0" : "") + sec;
    }

    void senDataToFrm(List<CauTraLoi> a) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fm, new ResultFragment(a, dsCTL));
        ft.commit();
        init();
    }
}
