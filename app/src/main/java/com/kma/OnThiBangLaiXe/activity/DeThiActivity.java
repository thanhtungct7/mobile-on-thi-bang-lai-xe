package com.kma.OnThiBangLaiXe.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kma.OnThiBangLaiXe.Adapter.DeThiAdapter;
import com.kma.OnThiBangLaiXe.Model.DanhSach;
import com.kma.OnThiBangLaiXe.Model.DeThi;
import com.kma.OnThiBangLaiXe.R;

import java.util.List;

/*
 * Mô tả file:
 * Activity hiển thị danh sách đề thi thử.
 * File này lấy danh sách đề từ DanhSach, gắn DeThiAdapter vào RecyclerView dạng lưới
 * và xử lý nút quay lại trên toolbar.
 */
public class DeThiActivity extends AppCompatActivity {
    List<DeThi> dsDeThi;
    public static DeThiAdapter dtAdapter;

    Toolbar toolbarBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_de_thi);

        dsDeThi = DanhSach.getDsDeThi();
        toolbarBack = findViewById(R.id.toolbarBack);
        dtAdapter = new DeThiAdapter(dsDeThi, this);
        RecyclerView rv = findViewById(R.id.rvDeThi);
        rv.setLayoutManager(new GridLayoutManager(this, 3));
        rv.setAdapter(dtAdapter);

        toolbarBack.setNavigationOnClickListener(view -> onBackPressed());
    }
}
