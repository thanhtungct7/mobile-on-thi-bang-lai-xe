package com.kma.OnThiBangLaiXe.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kma.OnThiBangLaiXe.Adapter.CauHoiResultAdapter;
import com.kma.OnThiBangLaiXe.Model.CauTraLoi;
import com.kma.OnThiBangLaiXe.R;

import java.util.List;

/*
 * Mô tả file:
 * Fragment hiển thị lưới câu hỏi trong phần kết quả bài thi.
 * File này nhận danh sách câu trả lời, cấu hình RecyclerView dạng 5 cột
 * và dùng CauHoiResultAdapter để biểu diễn trạng thái từng câu.
 */
public class ResultFragment extends Fragment {

    private RecyclerView rv;
    private List<CauTraLoi> dsCTl;
    private List<CauTraLoi> fullList;
    private CauHoiResultAdapter adapter;
    private View mView;

    public ResultFragment(List<CauTraLoi> dsCTl, List<CauTraLoi> fullList) {
        this.dsCTl = dsCTl;
        this.fullList = fullList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_result, container, false);
        rv = mView.findViewById(R.id.rvCauHoiResult);
        adapter = new CauHoiResultAdapter(dsCTl, fullList, requireContext());
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 5));
        rv.setNestedScrollingEnabled(false);
        rv.setAdapter(adapter);
        return mView;
    }
}
