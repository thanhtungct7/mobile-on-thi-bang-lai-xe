package com.kma.OnThiBangLaiXe.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.kma.OnThiBangLaiXe.Custom.MyDB;
import com.kma.OnThiBangLaiXe.Custom.MySharedPreferences;
import com.kma.OnThiBangLaiXe.Model.DanhSach;
import com.kma.OnThiBangLaiXe.R;
import com.google.android.material.textfield.TextInputLayout;

/*
 * Mô tả file:
 * Fragment đại diện cho từng trang trong luồng giới thiệu ban đầu.
 * File này nhận ảnh/tiêu đề/nội dung qua arguments; ở trang cuối cho phép chọn loại bằng
 * và lưu lựa chọn để khởi tạo dữ liệu học phù hợp.
 */
public class OnboardingFragment extends Fragment {

    private static final String ARG_IMG     = "img";
    private static final String ARG_TITLE   = "title";
    private static final String ARG_CONTENT = "content";
    private static final String ARG_LAST    = "last";

    public Button btnNext;

    // Required no-arg constructor for FragmentManager recreation
    public OnboardingFragment() {}

    public static OnboardingFragment newInstance(int img, String title, String content) {
        OnboardingFragment f = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_IMG, img);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CONTENT, content);
        args.putBoolean(ARG_LAST, false);
        f.setArguments(args);
        return f;
    }

    public static OnboardingFragment newInstanceLast(int img, String title, String content) {
        OnboardingFragment f = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_IMG, img);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CONTENT, content);
        args.putBoolean(ARG_LAST, true);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_onboarding, container, false);

        Bundle args = getArguments();
        int img         = args != null ? args.getInt(ARG_IMG, 0)          : 0;
        String title    = args != null ? args.getString(ARG_TITLE, "")    : "";
        String content  = args != null ? args.getString(ARG_CONTENT, "")  : "";
        boolean isLast  = args != null && args.getBoolean(ARG_LAST, false);

        ImageView imgV   = mView.findViewById(R.id.img);
        TextView titleV  = mView.findViewById(R.id.title);
        TextView contentV = mView.findViewById(R.id.conntent);

        imgV.setImageResource(img);
        titleV.setText(title);
        contentV.setText(content);

        if (isLast) {
            btnNext = mView.findViewById(R.id.btn_next);
            AutoCompleteTextView autoCompleteTextView = mView.findViewById(R.id.auto_complete_txt);
            TextInputLayout textInputLayout = mView.findViewById(R.id.TextInputLayout);
            textInputLayout.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.VISIBLE);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    mView.getContext(), R.layout.list_item_loai_bang, DanhSach.getDsBang());
            autoCompleteTextView.setAdapter(adapter);

            MyDB myDB = new MyDB(requireContext());
            btnNext.setOnClickListener(v -> {
                String selected = autoCompleteTextView.getText().toString();
                if (selected.isEmpty()) {
                    Toast.makeText(mView.getContext(), "Vui lòng chọn loại bằng", Toast.LENGTH_SHORT).show();
                    return;
                }
                MySharedPreferences prefs = new MySharedPreferences(mView.getContext());
                if (selected.equals("A1"))      prefs.puttIntValue("LOAI_GPLX", 1);
                else if (selected.equals("B1")) prefs.puttIntValue("LOAI_GPLX", 2);
                DanhSach.setLoaiBang(prefs.getIntValue("LOAI_GPLX"));
                myDB.kiemTraPhienBan();
            });
        }

        return mView;
    }
}
