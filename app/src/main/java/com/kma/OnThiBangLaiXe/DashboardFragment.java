package com.kma.OnThiBangLaiXe;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kma.OnThiBangLaiXe.Adapter.WeakAreaAdapter;
import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.kma.OnThiBangLaiXe.Model.DanhSach;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private ReadinessRingView readinessRing;
    private TextView txtGreeting, txtSubtitle, txtStatQuestions, txtStatAccuracy, txtStatTopics;
    private RecyclerView rvWeakAreas;
    private View cardWeakAreas, txtNoWeakAreas;
    private WeakAreaAdapter.OnItemClickListener weakAreaListener;

    // Category metadata (matches loadDBToDanhSach order in MainActivity)
    private static final int[]    CAT_IDS   = {1, 2, 3, 4, 5};
    private static final String[] CAT_NAMES = {
            "Câu hỏi điểm liệt",
            "Kỹ thuật lái xe",
            "Khái niệm và quy tắc",
            "Văn hóa và đạo đức",
            "Nghiệp vụ vận tải"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        readinessRing    = view.findViewById(R.id.readinessRing);
        txtGreeting      = view.findViewById(R.id.txtGreeting);
        txtSubtitle      = view.findViewById(R.id.txtReadinessSubtitle);
        txtStatQuestions = view.findViewById(R.id.txtStatQuestions);
        txtStatAccuracy  = view.findViewById(R.id.txtStatAccuracy);
        txtStatTopics    = view.findViewById(R.id.txtStatTopics);
        rvWeakAreas      = view.findViewById(R.id.rvWeakAreas);
        cardWeakAreas    = view.findViewById(R.id.cardWeakAreas);
        txtNoWeakAreas   = view.findViewById(R.id.txtNoWeakAreas);

        rvWeakAreas.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvWeakAreas.setNestedScrollingEnabled(false);

        view.findViewById(R.id.btnStartPractice).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CauHoiActivity.class)));

        weakAreaListener = (catId, catName) -> {
            Intent intent = new Intent(requireContext(), CauHoiActivity.class);
            intent.putExtra("MaLoaiCH", catId);
            intent.putExtra("ChiHienThiSai", true);
            startActivity(intent);
        };

        setGreeting();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        if (getView() == null) return;
        List<CauHoi> all = DanhSach.getDsCauHoi();

        int answered = 0, correct = 0;
        for (CauHoi ch : all) {
            if (ch.getDaTraLoiDung() != 0) {
                answered++;
                if (ch.getDaTraLoiDung() == 1) correct++;
            }
        }

        int accuracyPct = answered > 0 ? (int) ((correct / (float) answered) * 100) : 0;
        int topicsCovered = countTopicsCovered(all);
        int readiness = computeReadiness(all, topicsCovered);

        readinessRing.setProgress(readiness);
        txtStatQuestions.setText(answered + "");
        txtStatAccuracy.setText(accuracyPct + "%");
        txtStatTopics.setText(topicsCovered + "/" + CAT_IDS.length);

        int remaining = CAT_IDS.length - topicsCovered;
        if (remaining > 0) {
            txtSubtitle.setText("Còn " + remaining + " chủ đề cần hoàn thành\ntrước khi thi");
        } else {
            txtSubtitle.setText("Bạn đã hoàn thành tất cả chủ đề!");
        }

        buildWeakAreas(all);
    }

    private int computeReadiness(List<CauHoi> all, int topicsCovered) {
        int total = all.size();
        if (total == 0) return 0;
        int answered = 0, correct = 0;
        for (CauHoi ch : all) {
            if (ch.getDaTraLoiDung() != 0) {
                answered++;
                if (ch.getDaTraLoiDung() == 1) correct++;
            }
        }
        int progressPct  = (int) ((answered / (float) total) * 100);
        int accuracyPct  = answered > 0 ? (int) ((correct / (float) answered) * 100) : 0;
        int topicsPct    = (int) ((topicsCovered / (float) CAT_IDS.length) * 100);
        return (progressPct * 40 + accuracyPct * 40 + topicsPct * 20) / 100;
    }

    private int countTopicsCovered(List<CauHoi> all) {
        int covered = 0;
        for (int catId : CAT_IDS) {
            int catAnswered = 0;
            for (CauHoi ch : all) {
                if (ch.getMaLoaiCH() == catId && ch.getDaTraLoiDung() != 0) catAnswered++;
            }
            if (catAnswered > 0) covered++;
        }
        return covered;
    }

    private void buildWeakAreas(List<CauHoi> all) {
        List<WeakAreaAdapter.WeakArea> areas = new ArrayList<>();

        for (int i = 0; i < CAT_IDS.length; i++) {
            int catId = CAT_IDS[i];
            int wrong = 0, catCorrect = 0, catAnswered = 0;
            for (CauHoi ch : all) {
                if (ch.getMaLoaiCH() != catId) continue;
                if (ch.getDaTraLoiDung() == 1) { catCorrect++; catAnswered++; }
                else if (ch.getDaTraLoiDung() == 2) { wrong++; catAnswered++; }
            }
            if (catAnswered == 0) continue;
            int acc = (int) ((catCorrect / (float) catAnswered) * 100);
            if (acc < 80) {
                areas.add(new WeakAreaAdapter.WeakArea(CAT_NAMES[i], catId, wrong, acc, 0));
            }
        }

        // Sort worst first
        areas.sort((a, b) -> a.accuracyPercent - b.accuracyPercent);

        if (areas.isEmpty()) {
            cardWeakAreas.setVisibility(View.GONE);
            txtNoWeakAreas.setVisibility(View.VISIBLE);
        } else {
            cardWeakAreas.setVisibility(View.VISIBLE);
            txtNoWeakAreas.setVisibility(View.GONE);
            rvWeakAreas.setAdapter(new WeakAreaAdapter(areas, weakAreaListener));
        }
    }

    private void setGreeting() {
        Calendar cal = Calendar.getInstance();
        String[] days = {"Chủ nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư",
                         "Thứ Năm", "Thứ Sáu", "Thứ Bảy"};
        String day = days[cal.get(Calendar.DAY_OF_WEEK) - 1];
        String date = new SimpleDateFormat("d 'Tháng' M", new Locale("vi")).format(cal.getTime());
        txtGreeting.setText(day + " · " + date);
    }
}
