package com.kma.OnThiBangLaiXe.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kma.OnThiBangLaiXe.CauHoiActivity;
import com.kma.OnThiBangLaiXe.DBHandler;
import com.kma.OnThiBangLaiXe.Interface.RecyclerViewInterface;
import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.kma.OnThiBangLaiXe.Model.LoaiCauHoi;
import com.kma.OnThiBangLaiXe.R;

import java.util.ArrayList;
import java.util.List;

/*
 * Mô tả file:
 * Adapter hiển thị danh sách chủ đề câu hỏi ôn tập.
 * File này tính tiến độ từng chủ đề, hỗ trợ lọc điểm liệt/đang học/hoàn thành
 * và mở CauHoiActivity với đúng mã chủ đề khi người dùng chọn một mục.
 */
public class TheLoaiCauHoiAdapter extends RecyclerView.Adapter<TheLoaiCauHoiAdapter.ViewHolder> {

    public static final int FILTER_ALL         = 0;
    public static final int FILTER_DIEM_LIET   = 1;
    public static final int FILTER_DANG_HOC    = 2;
    public static final int FILTER_HOAN_THANH  = 3;

    private final RecyclerViewInterface recyclerViewInterface;
    private final List<LoaiCauHoi> allList;
    private List<LoaiCauHoi> dsLoaiCauHoi;
    private final Context context;
    private final DBHandler db;

    public TheLoaiCauHoiAdapter(List<LoaiCauHoi> list, Context context, RecyclerViewInterface iface) {
        this.allList = new ArrayList<>(list);
        this.dsLoaiCauHoi = new ArrayList<>(list);
        this.context = context;
        this.recyclerViewInterface = iface;
        this.db = new DBHandler(context);
    }

    public void setFilter(int filter) {
        dsLoaiCauHoi = new ArrayList<>();
        for (LoaiCauHoi item : allList) {
            switch (filter) {
                case FILTER_DIEM_LIET:
                    if (item.isDiemLiet()) dsLoaiCauHoi.add(item);
                    break;
                case FILTER_DANG_HOC: {
                    int[] p = computeProgress(item);
                    if (p[0] > 0 && p[0] < p[1]) dsLoaiCauHoi.add(item);
                    break;
                }
                case FILTER_HOAN_THANH: {
                    int[] p = computeProgress(item);
                    if (p[1] > 0 && p[0] == p[1]) dsLoaiCauHoi.add(item);
                    break;
                }
                default:
                    dsLoaiCauHoi.add(item);
            }
        }
        notifyDataSetChanged();
    }

    private int[] computeProgress(LoaiCauHoi item) {
        int answered = 0, total = 0;
        for (CauHoi ch : db.docCauHoi()) {
            if (ch.getMaLoaiCH() == item.getMaLoaiCH()) {
                total++;
                if (ch.getDaTraLoiDung() != 0) answered++;
            }
        }
        return new int[]{answered, total};
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_theloaicauhoi, parent, false);
        return new ViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LoaiCauHoi tlch = dsLoaiCauHoi.get(position);

        holder.divider.setVisibility(position == 0 ? View.GONE : View.VISIBLE);

        try {
            holder.ivTheLoaiCauHoi.setImageResource(context.getResources().getIdentifier(
                    tlch.getHinh(), "drawable", context.getPackageName()));
        } catch (Exception e) {
            holder.ivTheLoaiCauHoi.setImageResource(R.drawable.ico_exam);
        }

        holder.tvDiemLiet.setVisibility(tlch.isDiemLiet() ? View.VISIBLE : View.GONE);
        holder.ten.setText(tlch.getTenLoaiCauHoi());

        int[] prog = computeProgress(tlch);
        int answered = prog[0], total = prog[1];
        int pct = total == 0 ? 0 : (int) ((answered / (float) total) * 100);

        holder.soCauHoi.setText(total + " câu");

        boolean low = total > 0 && pct < 65;
        holder.pbKetQua.setProgressDrawable(ContextCompat.getDrawable(context,
                low ? R.drawable.progress_bar_low : R.drawable.progress_bar));
        holder.pbKetQua.setMax(100);
        holder.pbKetQua.setProgress(pct);
        holder.txtPct.setText(pct + "%");
        holder.txtPct.setTextColor(ContextCompat.getColor(context,
                low ? R.color.accent : R.color.muted));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CauHoiActivity.class);
            intent.putExtra("MaLoaiCH", tlch.getMaLoaiCH());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return dsLoaiCauHoi.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View       divider;
        final ImageView  ivTheLoaiCauHoi;
        final TextView   ten;
        final TextView   tvDiemLiet;
        final TextView   soCauHoi;
        final ProgressBar pbKetQua;
        final TextView   txtPct;

        ViewHolder(@NonNull View itemView, RecyclerViewInterface iface) {
            super(itemView);
            divider         = itemView.findViewById(R.id.topicDivider);
            ivTheLoaiCauHoi = itemView.findViewById(R.id.ivTheLoaiCauHoi);
            ten             = itemView.findViewById(R.id.txtTheLoaiCauHoi);
            tvDiemLiet      = itemView.findViewById(R.id.tvDiemLiet);
            soCauHoi        = itemView.findViewById(R.id.txtSoCau);
            pbKetQua        = itemView.findViewById(R.id.pbTheLoaiCauHoi);
            txtPct          = itemView.findViewById(R.id.txtPct);

            itemView.setOnClickListener(v -> {
                if (iface != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) iface.onItemClick(pos);
                }
            });
        }
    }
}
