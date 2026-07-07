package com.kma.OnThiBangLaiXe;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

/*
 * Mô tả file:
 * Activity hiển thị nội dung HTML trong WebView.
 * File này nhận URL và tên trang qua Intent, bật JavaScript/DOM storage,
 * load nội dung mẹo thi hoặc chính sách và xử lý nút quay lại trên toolbar.
 */
public class WebActivity extends AppCompatActivity {
    WebView wb;
    Toolbar toolbarBack;
    WebSettings myWebSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        wb = findViewById(R.id.wbMeo);
        myWebSettings = wb.getSettings();
        myWebSettings.setJavaScriptEnabled(true);
        myWebSettings.setDomStorageEnabled(true);
        wb.loadUrl(Objects.requireNonNull(getIntent().getStringExtra("URL")));
        wb.setWebViewClient(new WebViewClient());
        toolbarBack = findViewById(R.id.toolbarBack);
        TextView txtWeb = findViewById(R.id.txtWeb);
        txtWeb.setText(getIntent().getStringExtra("Name"));
        toolbarBack.setNavigationOnClickListener(view -> onBackPressed());
    }
}
