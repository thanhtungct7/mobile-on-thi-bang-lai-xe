package com.kma.OnThiBangLaiXe.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.kma.OnThiBangLaiXe.CauLuuActivity;
import com.kma.OnThiBangLaiXe.CauSaiActivity;
import com.kma.OnThiBangLaiXe.DBHandler;
import com.kma.OnThiBangLaiXe.HaySaiActivity;
import com.kma.OnThiBangLaiXe.MainActivity;
import com.kma.OnThiBangLaiXe.Model.DanhSach;
import com.kma.OnThiBangLaiXe.R;
import com.kma.OnThiBangLaiXe.SplashActivity;
import com.kma.OnThiBangLaiXe.WebActivity;

/*
 * Mô tả file:
 * Fragment màn hình hồ sơ và thiết lập cá nhân.
 * File này hiển thị loại bằng đang học, số câu đã lưu/câu sai/câu hay sai,
 * mở các màn hình liên quan và xử lý đổi loại giấy phép lái xe.
 */
public class ProfileFragment extends Fragment {

    private TextView txtLoaiBang, txtLuu, txtCauSai, txtHaySai;
    private LinearLayout lo_save, loCauSai, lo_HaySai;
    private DBHandler dbHandler;
    private int luu, sai, haysai;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        dbHandler = new DBHandler(requireContext());

        txtLoaiBang = view.findViewById(R.id.txtLoaiBang);
        txtLuu      = view.findViewById(R.id.txtLuu);
        txtCauSai   = view.findViewById(R.id.txtCauSai);
        txtHaySai   = view.findViewById(R.id.txtHaySai);
        lo_save     = view.findViewById(R.id.lo_save);
        loCauSai    = view.findViewById(R.id.loCauSai);
        lo_HaySai   = view.findViewById(R.id.lo_HaySai);

        view.findViewById(R.id.btnChangeLoaiBang).setOnClickListener(v -> confirmChangeLoaiBang());

        view.findViewById(R.id.btnHoTro).setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:0855985761")));
        });

        view.findViewById(R.id.btnChinhSach).setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), WebActivity.class);
            i.putExtra("URL", "file:///android_asset/html/ChinhSach.html");
            i.putExtra("Name", "Chính sách");
            startActivity(i);
        });

        lo_save.setOnClickListener(v -> {
            if (luu != 0) startActivity(new Intent(requireContext(), CauLuuActivity.class));
            else ((MainActivity) requireActivity()).dialog("Thông báo", "Bạn chưa có câu hỏi nào đã lưu!");
        });

        loCauSai.setOnClickListener(v -> {
            if (sai != 0) startActivity(new Intent(requireContext(), CauSaiActivity.class));
            else ((MainActivity) requireActivity()).dialog("Thông báo", "Bạn chưa có câu sai nào!");
        });

        lo_HaySai.setOnClickListener(v -> {
            if (haysai != 0) startActivity(new Intent(requireContext(), HaySaiActivity.class));
            else ((MainActivity) requireActivity()).dialog("Thông báo", "Bạn chưa có câu hỏi!");
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        txtLoaiBang.setText("Bằng " + DanhSach.getLoaiBang());
        luu    = dbHandler.docCauHoiLuu().size();
        sai    = dbHandler.docCauHoiSai().size();
        haysai = dbHandler.docCauHaySai().size();
        txtLuu.setText(luu + "");
        txtCauSai.setText(sai + "");
        txtHaySai.setText(haysai + "");
    }

    private void confirmChangeLoaiBang() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Thông báo")
                .setMessage("Bạn có chắc chắn muốn đổi loại bằng và khởi động lại ứng dụng không?")
                .setPositiveButton("Có", (d, i) -> {
                    Intent intent = new Intent(requireContext(), SplashActivity.class);
                    intent.putExtra("reset", true);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Không", null)
                .show();
    }
}
