package com.kma.OnThiBangLaiXe.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kma.OnThiBangLaiXe.R;

import java.util.List;

/*
 * Mô tả file:
 * Adapter hiển thị các chủ đề người dùng đang yếu trên dashboard.
 * File này gán tên chủ đề, số câu cần ôn lại, độ chính xác và phát sự kiện
 * khi người dùng chọn một chủ đề để luyện lại.
 */
public class WeakAreaAdapter extends RecyclerView.Adapter<WeakAreaAdapter.VH> {

    public interface OnItemClickListener {
        void onWeakAreaClick(int categoryId, String categoryName);
    }

    public static class WeakArea {
        public final String categoryName;
        public final int categoryId;
        public final int wrongCount;
        public final int accuracyPercent;
        public final int iconResId;

        public WeakArea(String categoryName, int categoryId, int wrongCount, int accuracyPercent, int iconResId) {
            this.categoryName    = categoryName;
            this.categoryId      = categoryId;
            this.wrongCount      = wrongCount;
            this.accuracyPercent = accuracyPercent;
            this.iconResId       = iconResId;
        }
    }

    private final List<WeakArea> items;
    private final OnItemClickListener listener;

    public WeakAreaAdapter(List<WeakArea> items, OnItemClickListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weak_area, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        WeakArea item = items.get(pos);
        h.name.setText(item.categoryName);
        h.wrong.setText(item.wrongCount + " câu cần ôn lại");
        h.accuracy.setText(item.accuracyPercent + "%");

        if (pos == 0) {
            h.itemView.setPadding(
                    h.itemView.getPaddingLeft(), 4,
                    h.itemView.getPaddingRight(), 11);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onWeakAreaClick(item.categoryId, item.categoryName);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name, wrong, accuracy;
        VH(@NonNull View v) {
            super(v);
            name     = v.findViewById(R.id.txtCategoryName);
            wrong    = v.findViewById(R.id.txtWrongCount);
            accuracy = v.findViewById(R.id.txtAccuracy);
        }
    }
}
