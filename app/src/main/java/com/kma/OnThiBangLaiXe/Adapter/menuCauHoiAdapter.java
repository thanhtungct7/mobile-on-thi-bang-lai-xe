package com.kma.OnThiBangLaiXe.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.kma.OnThiBangLaiXe.Model.CauTraLoi;
import com.kma.OnThiBangLaiXe.Model.DanhSach;
import com.kma.OnThiBangLaiXe.R;
import com.kma.OnThiBangLaiXe.ThiThuActivity;

import java.util.List;

public class menuCauHoiAdapter extends RecyclerView.Adapter<menuCauHoiAdapter.ViewHolder> {

    private List<CauTraLoi> dsCauTraLoi;
    private Context context;
    private int activePosition = 0;

    public menuCauHoiAdapter(List<CauTraLoi> dsCauTraLoi, Context context) {
        this.dsCauTraLoi = dsCauTraLoi;
        this.context = context;
    }

    public void setActivePosition(int newPos) {
        int oldPos = activePosition;
        activePosition = newPos;
        notifyItemChanged(oldPos);
        notifyItemChanged(newPos);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cau_tra_loi, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CauHoi ch = null;
        CauTraLoi ctl = dsCauTraLoi.get(position);

        for (CauHoi cauHoi : DanhSach.getDsCauHoi()) {
            if (cauHoi.getMaCH() == ctl.getMaCH()) { ch = cauHoi; break; }
        }

        holder.btnCauHoi.setText(String.valueOf(position + 1));
        Log.d("Test", String.valueOf(position + 1));

        boolean answered = ctl.getDapAnChon() != null;
        boolean critical = ch != null && ch.getMaLoaiCH() == 1;
        boolean active = position == activePosition;

        if (answered) {
            holder.btnCauHoi.setBackground(AppCompatResources.getDrawable(context,
                    active ? R.drawable.q_dot_answered_active_bg : R.drawable.q_dot_answered_bg));
            holder.btnCauHoi.setTextColor(context.getColor(R.color.white));
        } else if (critical) {
            holder.btnCauHoi.setBackground(AppCompatResources.getDrawable(context,
                    active ? R.drawable.q_dot_critical_active_bg : R.drawable.q_dot_critical_bg));
            holder.btnCauHoi.setTextColor(context.getColor(R.color.white));
        } else {
            holder.btnCauHoi.setBackground(AppCompatResources.getDrawable(context,
                    active ? R.drawable.q_dot_default_active_bg : R.drawable.q_dot_default_bg));
            holder.btnCauHoi.setTextColor(context.getColor(R.color.muted));
        }

        holder.btnCauHoi.setOnClickListener(v -> ThiThuActivity.vp.setCurrentItem(position, false));
    }

    @Override
    public int getItemViewType(int position) { return position; }

    @Override
    public int getItemCount() { return dsCauTraLoi.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView btnCauHoi;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            btnCauHoi = itemView.findViewById(R.id.btnCauHoi);
        }
    }
}
