package com.kma.OnThiBangLaiXe.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kma.OnThiBangLaiXe.ChiTietKetQuaActivity;
import com.kma.OnThiBangLaiXe.DBHandler;
import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.kma.OnThiBangLaiXe.Model.CauTraLoi;
import com.kma.OnThiBangLaiXe.R;

import java.util.List;

public class CauHoiResultAdapter extends RecyclerView.Adapter<CauHoiResultAdapter.ViewHolder> {

    private final List<CauTraLoi> dsCTL;
    private final List<CauTraLoi> fullList;
    private final Context context;
    private final DBHandler db;

    public CauHoiResultAdapter(List<CauTraLoi> dsCTL, List<CauTraLoi> fullList, Context context) {
        this.dsCTL = dsCTL;
        this.fullList = fullList;
        this.context = context;
        db = new DBHandler(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cau_hoi_result, parent, false);

        // Make items square (5 columns)
        DisplayMetrics dm = parent.getContext().getResources().getDisplayMetrics();
        int availableWidth = parent.getMeasuredWidth();
        if (availableWidth <= 0) {
            int sidePad = (int) (16 * dm.density); // 8dp each side of the FrameLayout container
            availableWidth = dm.widthPixels - sidePad * 2;
        }
        int cellSize = availableWidth / 5;
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(cellSize, cellSize);
        v.setLayoutParams(lp);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CauTraLoi ctl = dsCTL.get(position);
        if (ctl == null) return;

        CauHoi ch = db.getCauHoiByID(ctl.getMaCH());

        // Show original question number in the exam
        int originalPos = fullList.indexOf(ctl);
        holder.cau.setText(String.valueOf(originalPos >= 0 ? originalPos + 1 : position + 1));

        // Điểm liệt badge
        if (ch.getMaLoaiCH() == 1) {
            holder.imageCauHoiLiet.setVisibility(View.VISIBLE);
        } else {
            holder.imageCauHoiLiet.setVisibility(View.GONE);
        }

        // Bubble color based on answer status
        String dapAn = ctl.getDapAnChon();
        if (dapAn == null || dapAn.equals("0")) {
            holder.cau.setBackground(ContextCompat.getDrawable(context, R.drawable.bubble_skip_bg));
            holder.cau.setTextColor(ContextCompat.getColor(context, R.color.muted));
        } else if (dapAn.equals(ch.getDapAnDung())) {
            holder.cau.setBackground(ContextCompat.getDrawable(context, R.drawable.bubble_correct_bg));
            holder.cau.setTextColor(ContextCompat.getColor(context, R.color.result_pass_fg));
        } else {
            holder.cau.setBackground(ContextCompat.getDrawable(context, R.drawable.bubble_wrong_bg));
            holder.cau.setTextColor(ContextCompat.getColor(context, R.color.result_fail_fg));
        }

        // Tap to view detail — ViTri is position in full exam list
        holder.itemView.setOnClickListener(view -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;
            CauTraLoi clicked = dsCTL.get(adapterPos);
            int viTriGoc = fullList.indexOf(clicked);
            Intent intent = new Intent(context, ChiTietKetQuaActivity.class);
            intent.putExtra("MaDeThi", clicked.getMaDeThi());
            intent.putExtra("ViTri", viTriGoc >= 0 ? viTriGoc : adapterPos);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemViewType(int position) { return position; }

    @Override
    public int getItemCount() {
        return dsCTL != null ? dsCTL.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView cau, noidung;
        final ImageView image, imageBB, imageCauHoiLiet;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cau             = itemView.findViewById(R.id.txtCau);
            image           = itemView.findViewById(R.id.img);
            noidung         = itemView.findViewById(R.id.txtContent);
            imageBB         = itemView.findViewById(R.id.imgBB);
            imageCauHoiLiet = itemView.findViewById(R.id.img_cauhoidiemliet);
        }
    }
}
