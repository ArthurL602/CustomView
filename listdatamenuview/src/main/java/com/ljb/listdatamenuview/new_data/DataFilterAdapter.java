package com.ljb.listdatamenuview.new_data;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ljb.listdatamenuview.R;


/**
 * Author      :ljb
 * Date        :2019/10/3
 * Description :
 */
public class DataFilterAdapter extends BaseDataFilterAdapter {
    private String[] mTitles = {"直播", "视频", "推荐", "新闻"};

    private int[] mLayoutIds = {
            R.layout.layout_data_filter_menu_01,
            R.layout.layout_data_filter_menu_02,
            R.layout.layout_data_filter_menu_03,
            R.layout.layout_data_filter_menu_04
    };

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public View getTabView(int position, ViewGroup parent) {
        TextView tabView = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_data_filter_tab, parent, false);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tabView.getLayoutParams();
        layoutParams.width = 0;
        layoutParams.weight = 1;
        tabView.setText(mTitles[position]);
        return tabView;
    }

    @Override
    public View getMenuView(int position, ViewGroup parent) {
        TextView menuView = (TextView) LayoutInflater.from(parent.getContext()).inflate(mLayoutIds[position], parent, false);
        menuView.setText(mTitles[position]);
        menuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyCloseMenu();
            }
        });
        return menuView;
    }

    @Override
    public void openMenu(View tabView) {
        TextView tabTv = (TextView) tabView;
        tabTv.setTextColor(Color.parseColor("#ffff0000"));
    }

    @Override
    public void closeMenu(View tabView) {
        TextView tabTv = (TextView) tabView;
        tabTv.setTextColor(Color.parseColor("#ff000000"));
    }
}
