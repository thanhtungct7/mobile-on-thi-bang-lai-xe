package com.kma.OnThiBangLaiXe.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kma.OnThiBangLaiXe.activity.ChiTietBienBaoActivity;
import com.kma.OnThiBangLaiXe.Model.BienBao;
import com.kma.OnThiBangLaiXe.R;

import java.io.InputStream;
import java.util.List;

/*
 * Mô tả file:
 * Adapter hiển thị danh sách biển báo trong RecyclerView.
 * File này load ảnh biển báo từ assets/bien_bao, gán mã/tên/nội dung biển báo
 * lên từng item và mở màn hình ChiTietBienBaoActivity khi người dùng chọn một biển báo.
 */
public class BienBaoAdapter extends RecyclerView.Adapter<BienBaoAdapter.ViewHolder>
{
    private List<BienBao> dsBienBao;
    private Context context;

    public BienBaoAdapter(List<BienBao> dsBienBao, Context context) {
        this.dsBienBao = dsBienBao;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_bien_bao, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        BienBao bb = dsBienBao.get(position);

        try {
            InputStream is = context.getAssets().open("bien_bao/" + bb.getHinhAnh());
            Bitmap b = BitmapFactory.decodeStream(is);
            is.close();
            holder.ivBienBao.setImageBitmap(b);
        } catch (Exception e) {
            holder.ivBienBao.setImageResource(R.drawable.ico_exam);
        }

        holder.txtMaBienBao.setText(bb.getMaBB());
        holder.txtTieuDeBienBao.setText(bb.getTieuDe());
        holder.txtNoiDungBienBao.setText(bb.getNoidung());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChiTietBienBaoActivity.class);
            intent.putExtra("MaBB", bb.getMaBB());
            intent.putExtra("TieuDe", bb.getTieuDe());
            intent.putExtra("NoiDung", bb.getNoidung());
            intent.putExtra("HinhAnh", bb.getHinhAnh());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return dsBienBao.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        private final ImageView ivBienBao;
        private final TextView txtMaBienBao;
        private final TextView txtTieuDeBienBao;
        private final TextView txtNoiDungBienBao;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBienBao = itemView.findViewById(R.id.ivBienBao);
            txtMaBienBao = itemView.findViewById(R.id.txtMaBienBao);
            txtTieuDeBienBao = itemView.findViewById(R.id.txtTieuDeBienBao);
            txtNoiDungBienBao = itemView.findViewById(R.id.txtNoiDungBienBao);
        }
    }
}
