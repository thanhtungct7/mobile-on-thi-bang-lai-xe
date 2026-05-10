package com.kma.OnThiBangLaiXe.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kma.OnThiBangLaiXe.DBHandler;
import com.kma.OnThiBangLaiXe.KetQuaActivity;
import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.kma.OnThiBangLaiXe.Model.CauTraLoi;
import com.kma.OnThiBangLaiXe.Model.DeThi;
import com.kma.OnThiBangLaiXe.R;

import java.util.ArrayList;
import java.util.List;

public class ResultDeThiAdapter extends RecyclerView.Adapter<ResultDeThiAdapter.ViewHolder> {

    public static final int FILTER_ALL = 0;
    public static final int FILTER_DAU = 1;
    public static final int FILTER_ROT = 2;

    private static int passThreshold(DeThi dt) {
        return dt.getMaLoaiBang() == 2 ? 27 : 22;
    }

    private final List<DeThi> allList;
    private List<DeThi>       displayList;
    private final Context     context;
    private final DBHandler   db;

    public ResultDeThiAdapter(List<DeThi> list, Context context) {
        this.context = context;
        this.db = new DBHandler(context);
        this.allList = new ArrayList<>();
        for (DeThi dt : list) {
            if (isCompleted(dt)) this.allList.add(dt);
        }
        this.displayList = new ArrayList<>(this.allList);
    }

    public void setFilter(int filter) {
        displayList = new ArrayList<>();
        for (DeThi dt : allList) {
            switch (filter) {
                case FILTER_DAU:
                    if (isDau(dt)) displayList.add(dt);
                    break;
                case FILTER_ROT:
                    if (!isDau(dt)) displayList.add(dt);
                    break;
                default:
                    displayList.add(dt);
            }
        }
        notifyDataSetChanged();
    }

    public int getTotalCompleted() { return allList.size(); }

    public int getTotalPass() {
        int n = 0;
        for (DeThi dt : allList) if (isDau(dt)) n++;
        return n;
    }

    public int getTotalFail() { return allList.size() - getTotalPass(); }

    private boolean isCompleted(DeThi dt) {
        for (CauTraLoi ctl : db.getListCauTraLoiByMaDeThi(dt.getMaDeThi())) {
            String ans = ctl.getDapAnChon();
            if (ans != null && !ans.equals("null") && !ans.equals("0")) return true;
        }
        return false;
    }

    private boolean isDau(DeThi dt) {
        int[] r = computeResults(dt);
        return r[0] >= passThreshold(dt) && r[2] == 0;
    }

    /** Returns [cauDung, cauSai, diemLietSai] */
    private int[] computeResults(DeThi dt) {
        int cauDung = 0, cauSai = 0, diemLietSai = 0;
        for (CauTraLoi ctl : db.getListCauTraLoiByMaDeThi(dt.getMaDeThi())) {
            String ans = ctl.getDapAnChon();
            if (ans == null || ans.equals("null") || ans.equals("0")) continue;
            CauHoi ch = db.getCauHoiByID(ctl.getMaCH());
            if (ch == null) continue;
            if (ch.getDapAnDung().equals(ans)) {
                cauDung++;
            } else {
                cauSai++;
                if (ch.getMaLoaiCH() == 1) diemLietSai++;
            }
        }
        return new int[]{cauDung, cauSai, diemLietSai};
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeThi dt = displayList.get(position);
        holder.divider.setVisibility(position == 0 ? View.GONE : View.VISIBLE);

        int[] r = computeResults(dt);
        int cauDung = r[0], cauSai = r[1];
        boolean saiDiemLiet = r[2] > 0;
        boolean dau = cauDung >= passThreshold(dt) && !saiDiemLiet;
        int total = cauDung + cauSai;

        holder.txtTitle.setText(dt.getTenDeThi());

        if (dau) {
            holder.iconBg.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.result_pass_icon_bg));
            holder.ivIcon.setImageResource(R.drawable.ic_check);
            holder.ivIcon.setColorFilter(
                    ContextCompat.getColor(context, R.color.result_pass_fg));
            holder.tvBadge.setText("ĐẬU");
            holder.tvBadge.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.pass_badge));
            holder.tvBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.result_pass_fg));
            holder.txtScore.setTextColor(
                    ContextCompat.getColor(context, R.color.result_pass_fg));
            holder.txtReason.setVisibility(View.GONE);
        } else {
            holder.iconBg.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.result_fail_icon_bg));
            holder.ivIcon.setImageResource(R.drawable.ic_x_close);
            holder.ivIcon.setColorFilter(
                    ContextCompat.getColor(context, R.color.result_fail_fg));
            holder.tvBadge.setText("RỚT");
            holder.tvBadge.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.fail_badge));
            holder.tvBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.result_fail_fg));
            holder.txtScore.setTextColor(
                    ContextCompat.getColor(context, R.color.result_fail_fg));
            String reason = saiDiemLiet
                    ? "Sai câu điểm liệt"
                    : "Không đủ " + passThreshold(dt) + " câu đúng";
            holder.txtReason.setText(reason);
            holder.txtReason.setVisibility(View.VISIBLE);
        }

        holder.txtScore.setText(String.valueOf(cauDung));
        holder.txtDenom.setText("/ " + total + " câu đúng");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, KetQuaActivity.class);
            intent.putExtra("MaDeThi", dt.getMaDeThi());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return displayList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View         divider;
        final LinearLayout iconBg;
        final ImageView    ivIcon;
        final TextView     txtTitle;
        final TextView     tvBadge;
        final TextView     txtReason;
        final TextView     txtScore;
        final TextView     txtDenom;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            divider   = itemView.findViewById(R.id.resultDivider);
            iconBg    = itemView.findViewById(R.id.resultIconBg);
            ivIcon    = itemView.findViewById(R.id.ivResultIcon);
            txtTitle  = itemView.findViewById(R.id.txtResultTitle);
            tvBadge   = itemView.findViewById(R.id.tvResultBadge);
            txtReason = itemView.findViewById(R.id.txtResultReason);
            txtScore  = itemView.findViewById(R.id.txtResultScore);
            txtDenom  = itemView.findViewById(R.id.txtResultDenom);
        }
    }
}
