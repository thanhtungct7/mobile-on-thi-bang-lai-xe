package com.kma.OnThiBangLaiXe.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kma.OnThiBangLaiXe.Adapter.DeThiAdapter;
import com.kma.OnThiBangLaiXe.Adapter.ResultDeThiAdapter;
import com.kma.OnThiBangLaiXe.Model.DanhSach;
import com.kma.OnThiBangLaiXe.R;

/*
 * Mô tả file:
 * Fragment màn hình kết quả thi thử.
 * File này hiển thị thống kê tổng số đề đã làm, số đề đậu/rớt,
 * quản lý bộ lọc kết quả và danh sách lịch sử đề thi đã hoàn thành.
 */
public class ResultsFragment extends Fragment {

    // Kept for ThiThuActivity.ketThuc() null-check compatibility
    public static DeThiAdapter dtAdapter = null;

    private ResultDeThiAdapter resultAdapter;
    private TextView txtSubtitle, txtStatTotal, txtStatPass, txtStatFail;
    private TextView chipAll, chipDau, chipRot;
    private TextView activeChip;
    private RecyclerView rvKetQua;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        txtSubtitle  = view.findViewById(R.id.txtResultsSubtitle);
        txtStatTotal = view.findViewById(R.id.txtStatTotal);
        txtStatPass  = view.findViewById(R.id.txtStatPass);
        txtStatFail  = view.findViewById(R.id.txtStatFail);
        rvKetQua     = view.findViewById(R.id.rvKetQua);
        chipAll      = view.findViewById(R.id.chipAll);
        chipDau      = view.findViewById(R.id.chipDau);
        chipRot      = view.findViewById(R.id.chipRot);

        resultAdapter = new ResultDeThiAdapter(DanhSach.getDsDeThi(), requireContext());
        rvKetQua.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvKetQua.setAdapter(resultAdapter);

        activeChip = chipAll;
        bindChipEvents();
        refreshStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Rebuild the adapter so new exam results appear after returning from ThiThuActivity
        if (rvKetQua != null) {
            resultAdapter = new ResultDeThiAdapter(DanhSach.getDsDeThi(), requireContext());
            rvKetQua.setAdapter(resultAdapter);
            activeChip = chipAll;
            resetChipUI();
            refreshStats();
        }
    }

    private void refreshStats() {
        if (resultAdapter == null) return;
        int total = resultAdapter.getTotalCompleted();
        int pass  = resultAdapter.getTotalPass();
        int fail  = resultAdapter.getTotalFail();
        txtSubtitle.setText(total + " đề đã làm");
        txtStatTotal.setText(String.valueOf(total));
        txtStatPass.setText(String.valueOf(pass));
        txtStatFail.setText(String.valueOf(fail));
    }

    private void resetChipUI() {
        chipAll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.chip_active));
        chipAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.surface));
        chipDau.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.chip_default));
        chipDau.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted));
        chipRot.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.chip_default));
        chipRot.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted));
    }

    private void bindChipEvents() {
        View.OnClickListener chipListener = v -> {
            if (activeChip != null) {
                activeChip.setBackground(
                        ContextCompat.getDrawable(requireContext(), R.drawable.chip_default));
                activeChip.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.muted));
            }
            activeChip = (TextView) v;
            activeChip.setBackground(
                    ContextCompat.getDrawable(requireContext(), R.drawable.chip_active));
            activeChip.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.surface));

            int id = v.getId();
            int filter;
            if (id == R.id.chipDau)      filter = ResultDeThiAdapter.FILTER_DAU;
            else if (id == R.id.chipRot) filter = ResultDeThiAdapter.FILTER_ROT;
            else                         filter = ResultDeThiAdapter.FILTER_ALL;

            resultAdapter.setFilter(filter);
        };

        chipAll.setOnClickListener(chipListener);
        chipDau.setOnClickListener(chipListener);
        chipRot.setOnClickListener(chipListener);
    }
}
