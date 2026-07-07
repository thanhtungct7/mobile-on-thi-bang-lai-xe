package com.kma.OnThiBangLaiXe.Interface;

/*
 * Mô tả file:
 * Interface callback dùng cho sự kiện click item trong RecyclerView.
 * File này định nghĩa phương thức onItemClick để Adapter báo vị trí item
 * được chọn về Fragment hoặc Activity đang sở hữu danh sách.
 */
public interface RecyclerViewInterface {
    void onItemClick(int postion);
}
