package com.meloon.taobaoaddressfilter.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Author      :meloon
 * Date        :2018/10/31
 * Description :
 */
public class PopVpAdapter extends FragmentPagerAdapter {
    private List<Fragment> mFragments;

    public PopVpAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
