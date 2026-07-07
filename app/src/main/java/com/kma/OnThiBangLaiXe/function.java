package com.kma.OnThiBangLaiXe;

/*
 * Mô tả file:
 * Model đơn giản đại diện cho một chức năng trong giao diện.
 * File này lưu tên chức năng và id icon/drawable để FunctionAdapter hiển thị
 * trong danh sách hoặc lưới chức năng.
 */
public class function {
    private String title;
    private int img;

    public function() {
    }

    public function(String title, int img) {
        this.title = title;
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

}
