package com.kma.OnThiBangLaiXe.Model;

public class LoaiCauHoi {
    private String hinh;
    private int MaLoaiCH;
    private String TenLoaiCauHoi;
    private boolean diemLiet;

    public String getHinh() {
        return hinh;
    }

    public void setHinh(String hinh) {
        this.hinh = hinh;
    }

    public int getMaLoaiCH() {
        return MaLoaiCH;
    }

    public void setMaLoaiCH(int maLoaiCH) {
        MaLoaiCH = maLoaiCH;
    }

    public String getTenLoaiCauHoi() {
        return TenLoaiCauHoi;
    }

    public void setTenLoaiCauHoi(String tenLoaiCauHoi) {
        this.TenLoaiCauHoi = tenLoaiCauHoi;
    }

    public boolean isDiemLiet() {
        return diemLiet;
    }

    public void setDiemLiet(boolean diemLiet) {
        this.diemLiet = diemLiet;
    }

    public LoaiCauHoi() {
    }

    public LoaiCauHoi(int maLoaiCH, String tenLoaiCauHoi) {
        MaLoaiCH = maLoaiCH;
        TenLoaiCauHoi = tenLoaiCauHoi;
    }

    public LoaiCauHoi(int maLoaiCH, String hinh, String TenLoaiCauHoi) {
        this.MaLoaiCH = maLoaiCH;
        this.hinh = hinh;
        this.TenLoaiCauHoi = TenLoaiCauHoi;
    }

    public LoaiCauHoi(int maLoaiCH, String hinh, String TenLoaiCauHoi, boolean diemLiet) {
        this.MaLoaiCH = maLoaiCH;
        this.hinh = hinh;
        this.TenLoaiCauHoi = TenLoaiCauHoi;
        this.diemLiet = diemLiet;
    }
}
