package com.kma.OnThiBangLaiXe.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kma.OnThiBangLaiXe.Adapter.TheLoaiCauHoiAdapter;
import com.kma.OnThiBangLaiXe.BienBaoActivity;
import com.kma.OnThiBangLaiXe.CauHoiActivity;
import com.kma.OnThiBangLaiXe.DBHandler;
import com.kma.OnThiBangLaiXe.DeThiActivity;
import com.kma.OnThiBangLaiXe.Interface.RecyclerViewInterface;
import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.kma.OnThiBangLaiXe.Model.DanhSach;
import com.kma.OnThiBangLaiXe.Model.DeThi;
import com.kma.OnThiBangLaiXe.Model.LoaiCauHoi;
import com.kma.OnThiBangLaiXe.R;
import com.kma.OnThiBangLaiXe.WebActivity;

import java.util.ArrayList;
import java.util.List;

/*
 * Mô tả file:
 * Fragment màn hình ôn tập chính.
 * File này hiển thị tiến độ học, các chế độ luyện tập như đề thi/600 câu/biển báo/mẹo thi
 * và danh sách chủ đề câu hỏi để người dùng chọn nội dung ôn tập.
 */
public class StudyFragment extends Fragment implements RecyclerViewInterface {

    private TextView    txtSubtitle;
    private TextView    txtProgressCount;
    private ProgressBar pbOverall;
    private TextView    txtProgressMeta;
    private RecyclerView rvTheLoaiCauHoi;

    private View cardExam;
    private View card600Cau;
    private View cardBienBao;
    private View cardMeoThi;
    private TextView txtModeExamSub;

    private DBHandler db;

    public static TheLoaiCauHoiAdapter tlchAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_study, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = new DBHandler(requireContext());

        txtSubtitle      = view.findViewById(R.id.txtSubtitle);
        txtProgressCount = view.findViewById(R.id.txtProgressCount);
        pbOverall        = view.findViewById(R.id.pbTheLoaiCauHoi);
        txtProgressMeta  = view.findViewById(R.id.txtProgressMeta);
        rvTheLoaiCauHoi  = view.findViewById(R.id.rvTheLoaiCauHoi);

        cardExam      = view.findViewById(R.id.cardExam);
        card600Cau    = view.findViewById(R.id.card600Cau);
        cardBienBao   = view.findViewById(R.id.cardBienBao);
        cardMeoThi    = view.findViewById(R.id.cardMeoThi);
        txtModeExamSub = view.findViewById(R.id.txtModeExamSub);

        List<LoaiCauHoi> topics = new ArrayList<>();
        topics.add(new LoaiCauHoi(1, "ico_fire",          "Câu hỏi điểm liệt",     true));
        topics.add(new LoaiCauHoi(2, "ico_trafficligh",   "Khái niệm và quy tắc",  false));
        topics.add(new LoaiCauHoi(3, "ico_traffic_signs", "Biển báo",               false));
        topics.add(new LoaiCauHoi(4, "ico_car",           "Tình huống giao thông",  false));
        topics.add(new LoaiCauHoi(5, "ico_truck",         "Kỹ thuật lái xe",        false));
        topics.add(new LoaiCauHoi(6, "ico_sahinh",        "Cấu tạo phương tiện",    false));
        topics.add(new LoaiCauHoi(7, "ico_account",       "Văn hóa và đạo đức",     false));

        tlchAdapter = new TheLoaiCauHoiAdapter(topics, requireContext(), this);
        rvTheLoaiCauHoi.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTheLoaiCauHoi.setAdapter(tlchAdapter);

        cardExam.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), DeThiActivity.class)));

        card600Cau.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CauHoiActivity.class);
            intent.putExtra("MaLoaiCH", 0);
            startActivity(intent);
        });

        cardBienBao.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), BienBaoActivity.class)));

        cardMeoThi.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), WebActivity.class);
            intent.putExtra("URL", "file:///android_asset/html/tips600.html");
            intent.putExtra("Name", "Mẹo thi");
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshProgress();
        if (tlchAdapter != null) tlchAdapter.notifyDataSetChanged();
    }

    public void refreshProgress() {
        List<CauHoi> all = DanhSach.getDsCauHoi();
        int total = all.size();
        int answered = 0;
        for (CauHoi ch : all) {
            if (ch.getDaTraLoiDung() != 0) answered++;
        }
        int pct = total == 0 ? 0 : (int) ((answered / (float) total) * 100);
        pbOverall.setMax(100);
        pbOverall.setProgress(pct);
        txtProgressCount.setText(answered + " / " + total);
        txtProgressMeta.setText(pct + "% hoàn thành · còn " + (total - answered) + " câu cần ôn");

        List<DeThi> allDeThi = DanhSach.getDsDeThi();
        int totalDe = allDeThi.size();
        int cauMoiDe = DanhSach.getLoaiBang() == 2 ? 30 : 25;

        if (txtModeExamSub != null) {
            txtModeExamSub.setText(totalDe + " đề · " + cauMoiDe + " câu · 19 phút");
        }

        txtSubtitle.setText(total + " câu · 7 chủ đề · " + totalDe + " đề thi");
    }

    @Override
    public void onItemClick(int position) { /* navigation handled inside adapter */ }
}
