package com.kma.OnThiBangLaiXe.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kma.OnThiBangLaiXe.DBHandler;
import com.kma.OnThiBangLaiXe.activity.KetQuaActivity;
import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.kma.OnThiBangLaiXe.Model.CauTraLoi;
import com.kma.OnThiBangLaiXe.Model.DanhSach;
import com.kma.OnThiBangLaiXe.Model.DeThi;
import com.kma.OnThiBangLaiXe.R;
import com.kma.OnThiBangLaiXe.activity.ThiThuActivity;

import java.util.ArrayList;
import java.util.List;

/*
 * Mô tả file:
 * Adapter hiển thị danh sách đề thi thử.
 * File này tính số câu đúng/sai, xác định trạng thái đề mới/đậu/rớt,
 * và điều hướng người dùng tới màn hình thi hoặc màn hình kết quả tương ứng.
 */
public class DeThiAdapter extends RecyclerView.Adapter<DeThiAdapter.ViewHolder> {

    private final List<DeThi> dsDeThi;
    private final Context context;
    private final DBHandler db;

    public DeThiAdapter(List<DeThi> dsDeThi, Context context) {
        this.dsDeThi = dsDeThi;
        this.context = context;
        db = new DBHandler(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_de_thi, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeThi de = dsDeThi.get(position);
        boolean isRandom = de.getMaDeThi() == 0;

        if (isRandom) {
            holder.txtLabel.setText("Ngẫu nhiên");
            holder.txtDeThiNum.setText("?");
        } else {
            holder.txtLabel.setText("Đề thi số");
            holder.txtDeThiNum.setText(String.valueOf(position));
        }

        int loaiBang = DanhSach.getLoaiBang();
        int nguongDat = (loaiBang == 2) ? 27 : 22;

        int soCauDung = 0, soCauSai = 0;
        boolean attempted = false;
        boolean saiDiemLiet = false;
        List<CauTraLoi> dsCTL = db.getListCauTraLoiByMaDeThi(de.getMaDeThi());
        for (CauTraLoi ctl : dsCTL) {
            String chon = ctl.getDapAnChon();
            if (chon == null || chon.equals("null")) continue;
            attempted = true;

            CauHoi cauHoi = db.getCauHoiByID(ctl.getMaCH());
            if (cauHoi == null) continue;

            boolean boQua = chon.equals("0");
            boolean traLoiDung = cauHoi.getDapAnDung().equals(chon);
            if (boQua) {
                if (cauHoi.getMaLoaiCH() == 1) saiDiemLiet = true;
                continue;
            }

            if (traLoiDung) {
                soCauDung++;
            } else {
                soCauSai++;
                if (cauHoi.getMaLoaiCH() == 1) saiDiemLiet = true;
            }
        }

        boolean passed = attempted && soCauDung >= nguongDat && !saiDiemLiet;

        if (!attempted) {
            holder.itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.card_de_thi_new));
            holder.txtDeThiNum.setTextColor(ContextCompat.getColor(context, R.color.fg));
            holder.layoutResult.setVisibility(View.GONE);
        } else if (passed) {
            holder.itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.card_de_thi_pass));
            holder.txtDeThiNum.setTextColor(ContextCompat.getColor(context, R.color.result_pass_fg));
            holder.layoutResult.setVisibility(View.VISIBLE);
            holder.txtSoCauDung.setText(String.valueOf(soCauDung));
            holder.txtSoCauSai.setText(String.valueOf(soCauSai));
        } else {
            holder.itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.card_de_thi_fail));
            holder.txtDeThiNum.setTextColor(ContextCompat.getColor(context, R.color.result_fail_fg));
            holder.layoutResult.setVisibility(View.VISIBLE);
            holder.txtSoCauDung.setText(String.valueOf(soCauDung));
            holder.txtSoCauSai.setText(String.valueOf(soCauSai));
        }

        final boolean attemptedFinal = attempted;
        final int soCauDungFinal = soCauDung;
        final int soCauSaiFinal = soCauSai;
        holder.itemView.setOnClickListener(view -> {
            if (attemptedFinal || soCauDungFinal > 0 || soCauSaiFinal > 0) {
                Intent intent = new Intent(context, KetQuaActivity.class);
                intent.putExtra("MaDeThi", de.getMaDeThi());
                context.startActivity(intent);
            } else {
                if (isRandom) {
                    List<CauTraLoi> temp = new ArrayList<>();
                    for (CauTraLoi ctl : DanhSach.getDsCauTraLoi()) {
                        if (ctl.getMaDeThi() == 0) temp.add(ctl);
                    }
                    DanhSach.getDsCauTraLoi().removeAll(temp);
                    db.RandomQuizz();
                }
                Intent intent = new Intent(context, ThiThuActivity.class);
                intent.putExtra("MaDeThi", de.getMaDeThi());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dsDeThi != null ? dsDeThi.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtLabel, txtDeThiNum, txtSoCauDung, txtSoCauSai;
        final LinearLayout layoutResult;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtLabel      = itemView.findViewById(R.id.txtLabel);
            txtDeThiNum   = itemView.findViewById(R.id.txt_DeThi);
            txtSoCauDung  = itemView.findViewById(R.id.txtSoCauDung);
            txtSoCauSai   = itemView.findViewById(R.id.txtSoCauSai);
            layoutResult  = itemView.findViewById(R.id.layoutResult);
        }
    }
}
