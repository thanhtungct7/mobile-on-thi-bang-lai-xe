package com.kma.OnThiBangLaiXe;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.kma.OnThiBangLaiXe.Fragment.DashboardFragment;
import com.kma.OnThiBangLaiXe.Fragment.ProfileFragment;
import com.kma.OnThiBangLaiXe.Fragment.ResultsFragment;
import com.kma.OnThiBangLaiXe.Fragment.StudyFragment;
import com.kma.OnThiBangLaiXe.Model.DanhSach;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kma.OnThiBangLaiXe.activity.SignDetectActivity;

/*
 * Mô tả file:
 * Activity chính của ứng dụng sau khi khởi động xong.
 * File này load dữ liệu từ database vào DanhSach, quản lý bottom navigation,
 * giữ các Fragment chính và mở màn hình nhận diện biển báo khi người dùng chọn tab camera.
 */
public class MainActivity extends AppCompatActivity {

    private DBHandler dbHandler;
    private BottomNavigationView bottomNav;

    private DashboardFragment dashboardFragment;
    private StudyFragment studyFragment;
    private ResultsFragment resultsFragment;
    private ProfileFragment profileFragment;
    private Fragment          activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHandler = new DBHandler(this);
        loadDBToDanhSach();

        bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            dashboardFragment = new DashboardFragment();
            studyFragment     = new StudyFragment();
            resultsFragment   = new ResultsFragment();
            profileFragment   = new ProfileFragment();

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
                    .add(R.id.fragment_container, resultsFragment, "results").hide(resultsFragment)
                    .add(R.id.fragment_container, studyFragment,   "study").hide(studyFragment)
                    .add(R.id.fragment_container, dashboardFragment, "dashboard")
                    .commit();
            activeFragment = dashboardFragment;
        } else {
            FragmentManager fm = getSupportFragmentManager();
            dashboardFragment = (DashboardFragment) fm.findFragmentByTag("dashboard");
            studyFragment     = (StudyFragment)     fm.findFragmentByTag("study");
            resultsFragment   = (ResultsFragment)   fm.findFragmentByTag("results");
            profileFragment   = (ProfileFragment)   fm.findFragmentByTag("profile");
            // Restore active by checked item
            activeFragment = dashboardFragment;
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_detect) {
                startActivity(new Intent(this, SignDetectActivity.class));
                return false; // không highlight item này
            }
            Fragment next;
            if      (id == R.id.nav_dashboard) next = dashboardFragment;
            else if (id == R.id.nav_study)     next = studyFragment;
            else if (id == R.id.nav_results)   next = resultsFragment;
            else if (id == R.id.nav_profile)   next = profileFragment;
            else return false;

            if (next == activeFragment) return true;
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(next)
                    .commit();
            activeFragment = next;
            return true;
        });
    }

    private void loadDBToDanhSach() {
        if (dbHandler.isBienBaoEmpty()) {
            dbHandler.loadBienBaoFromAssets();
        }
        DanhSach.setDsCauHoi(dbHandler.docCauHoi());
        DanhSach.setDsDeThi(dbHandler.docDeThi());
        DanhSach.setDsCauTraLoi(dbHandler.docCauTraLoi());
        // BienBao & LoaiBienBao được load lazy trong BienBaoActivity
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_MODERATE) {
            DanhSach.clearBienBao();
        }
    }

    public void dialog(String title, String content) {
        View custom = LayoutInflater.from(this).inflate(R.layout.custom_alter_dailog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(custom);
        Button btnOk    = custom.findViewById(R.id.btn_ok);
        TextView tvTitle   = custom.findViewById(R.id.tv_title);
        TextView tvContent = custom.findViewById(R.id.tv_content);
        tvTitle.setText(title);
        tvContent.setText(content);
        AlertDialog d = builder.create();
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        d.show();
        btnOk.setOnClickListener(v -> d.cancel());
    }
}
