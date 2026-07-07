package com.kma.OnThiBangLaiXe;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.kma.OnThiBangLaiXe.Fragment.ResultsFragment;
import com.kma.OnThiBangLaiXe.R;
import com.kma.OnThiBangLaiXe.Adapter.CauTraLoiAdapter;
import com.kma.OnThiBangLaiXe.Adapter.menuCauHoiAdapter;
import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.kma.OnThiBangLaiXe.Model.CauTraLoi;
import com.kma.OnThiBangLaiXe.Model.DanhSach;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

/*
 * Mô tả file:
 * Activity thực hiện bài thi thử.
 * File này tạo danh sách câu trả lời cho đề thi, quản lý ViewPager2 và menu câu hỏi,
 * chạy bộ đếm thời gian, xử lý nộp bài và lưu kết quả để chuyển sang màn hình kết quả.
 */
public class ThiThuActivity extends AppCompatActivity {
    public static ViewPager2 vp;
    public static RecyclerView rvCauHoi;
    private CountDownTimer countDownTimer;
    private long time;
    private long totalTime;
    TextView txtTitle,txtNopBai;
    private int maDeThi;
    TabLayout tabLayout;
    Toolbar toolbarBack;
    Button btnNavBack, btnNavForward;
    private DBHandler db;
    public static List<CauTraLoi> dsCauTraLoi;
    public static CauTraLoiAdapter ctlApdater;
    public static menuCauHoiAdapter menuAdapter;
    @Override
    protected void onStart() {
        super.onStart();
        if (countDownTimer == null) startTime();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cau_hoi);
        txtTitle = findViewById(R.id.txtTitle);
        txtNopBai=findViewById(R.id.txtThiLai);
        txtNopBai.setVisibility(View.VISIBLE);
        txtNopBai.setText("Nộp bài");
        btnNavBack = findViewById(R.id.btnNavBack);
        btnNavForward = findViewById(R.id.btnNavForward);
        toolbarBack =findViewById(R.id.toolbarBack);
        totalTime = DanhSach.getLoaiBang() == 2 ? 1_200_000L : 1_140_000L;
        time = totalTime;
        // Mã loại câu hỏi
        maDeThi = getIntent().getIntExtra("MaDeThi", 0);
        db=new DBHandler(this);
        vp = findViewById(R.id.vp);
        rvCauHoi = findViewById(R.id.rvCauHoi);
        dsCauTraLoi=new ArrayList<>();
        for (CauTraLoi ctl:DanhSach.getDsCauTraLoi())
        {
            if(ctl.getMaDeThi()==maDeThi)
                dsCauTraLoi.add(new CauTraLoi(ctl.getMaDeThi(),ctl.getMaCH(),null));
        }
        toolbarBack.setNavigationOnClickListener(view -> ThiThuActivity.this.onBackPressed());
        // Thêm vòng lặp hoặc phương thức để lấy ds câu hỏi của loại câu hỏi này ra

        ctlApdater = new CauTraLoiAdapter(dsCauTraLoi, this, false);
        vp.setAdapter(ctlApdater);
        tabLayout = findViewById(R.id.tab_layout);

        menuAdapter = new menuCauHoiAdapter(dsCauTraLoi, this);
        rvCauHoi.setAdapter(menuAdapter);
        rvCauHoi.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        btnNavBack.setOnClickListener(v -> {
            if (vp.getCurrentItem() > 0) vp.setCurrentItem(vp.getCurrentItem() - 1, true);
        });
        btnNavForward.setOnClickListener(v -> {
            if (vp.getCurrentItem() < dsCauTraLoi.size() - 1)
                vp.setCurrentItem(vp.getCurrentItem() + 1, true);
        });

        vp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                rvCauHoi.smoothScrollToPosition(position);
                menuAdapter.setActivePosition(position);
            }
        });

        new TabLayoutMediator(tabLayout, vp, (tab, position)
                -> tab.setText("Câu " + (position + 1))).attach();

        txtNopBai.setVisibility(View.VISIBLE);
        txtNopBai.setOnClickListener(view -> nopBai());
    }

    private void ketThuc()
    {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        List<CauTraLoi> temp = new ArrayList<>();

        for (CauTraLoi ctl : DanhSach.getDsCauTraLoi())
        {
            if(ctl.getMaDeThi() == maDeThi)
            {
                if(ctl.getDapAnChon()==null||ctl.getDapAnChon().equals("null"))
                {
                    ctl.setDapAnChon("0");
                }
                temp.add(ctl);
            }
        }

        syncPracticeProgress(temp);

        if (maDeThi != 0)
        {
            db.updateCauTraLoi(temp);
            if (DeThiActivity.dtAdapter != null) DeThiActivity.dtAdapter.notifyDataSetChanged();
            if (ResultsFragment.dtAdapter != null) ResultsFragment.dtAdapter.notifyDataSetChanged();
        }

        Intent intent = new Intent(this, KetQuaActivity.class);
        intent.putExtra("MaDeThi", maDeThi);
        long thoiGianConLai = Math.max(0L, Math.min(time, totalTime));
        intent.putExtra("ThoiGianConLai", thoiGianConLai);
        intent.putExtra("TongThoiGian", totalTime);
        startActivity(intent);
        finish();
    }

    private void syncPracticeProgress(List<CauTraLoi> answers) {
        for (CauTraLoi ctl : answers) {
            CauHoi cauHoi = findCauHoi(ctl.getMaCH());
            if (cauHoi == null) continue;

            String selected = ctl.getDapAnChon();
            boolean correct = selected != null
                    && !selected.equals("null")
                    && !selected.equals("0")
                    && selected.equals(cauHoi.getDapAnDung());
            int result = correct ? 1 : 2;

            cauHoi.setDaTraLoiDung(result);
            db.updateDaTraLoi(cauHoi.getMaCH(), result);
        }
    }

    private CauHoi findCauHoi(int maCauHoi) {
        for (CauHoi cauHoi : DanhSach.getDsCauHoi()) {
            if (cauHoi.getMaCH() == maCauHoi) return cauHoi;
        }
        return null;
    }

    private void nopBai()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ThiThuActivity.this);
        alertDialog.setTitle("Thông báo");
        alertDialog.setMessage("Bạn có chắn chắn muốn nộp bài không ?");
        alertDialog.setPositiveButton("Có", (dialogInterface, i) -> ketThuc());
        alertDialog.setNegativeButton("Không", (dialogInterface, i) -> {});
        alertDialog.show();
    }

    void startTime()
    {
        countDownTimer=new CountDownTimer(time,1000) {
            @Override
            public void onTick(long l) {
                time=l;
                updateTime();
            }

            @Override
            public void onFinish() {
                countDownTimer = null;
                time = 0L;
                txtTitle.setText("Hết giờ");
                final AlertDialog.Builder alertDialog=new AlertDialog.Builder(ThiThuActivity.this);
                alertDialog.setTitle("Thông báo");
                alertDialog.setMessage("Hết giờ");
                alertDialog.setPositiveButton("Xem kết quả", (dialogInterface, i) -> ketThuc());
                alertDialog.show();
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(ThiThuActivity.this);
        alertDialog.setTitle("Thông báo");
        alertDialog.setMessage("Dữ liệu bài thi đang làm sẽ không được lưu lại, bạn có chắc chắn muốn thoát?");
        alertDialog.setPositiveButton("Có", (dialogInterface, i) -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
            finish();
        });
        alertDialog.setNegativeButton("Không", (dialogInterface, i) -> {});
        alertDialog.show();
    }

    void updateTime()
    {
        int minutes=(int)time/60000;
        int seconds=(int)time%60000/1000;
        String timeText;
        timeText=""+minutes;
        timeText+=":";
        if(seconds<10)
            timeText+="0";
        timeText+=seconds;
        txtTitle.setText(timeText);
    }
}
