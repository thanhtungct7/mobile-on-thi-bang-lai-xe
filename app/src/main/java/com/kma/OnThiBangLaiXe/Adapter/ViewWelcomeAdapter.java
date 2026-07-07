package com.kma.OnThiBangLaiXe.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;


/*
 * Mô tả file:
 * Adapter quản lý các Fragment trong màn hình onboarding/welcome.
 * File này cung cấp từng Fragment cho ViewPager và trả về tổng số trang giới thiệu.
 */
public class ViewWelcomeAdapter extends FragmentStatePagerAdapter {
    List<Fragment> listFrm;
    public ViewWelcomeAdapter(@NonNull FragmentManager fm, int behavior,List<Fragment> listFrm) {
        super(fm, behavior);
        this.listFrm=listFrm;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        return listFrm.get(position);
    }

    @Override
    public int getCount() {
        return listFrm.size();
    }
}
