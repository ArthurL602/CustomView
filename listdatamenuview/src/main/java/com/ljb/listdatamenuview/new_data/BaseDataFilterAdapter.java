package com.ljb.listdatamenuview.new_data;

import android.view.View;
import android.view.ViewGroup;

/**
 * Author      :ljb
 * Date        :2019/10/3
 * Description : 数据筛选 适配器
 */
public abstract class BaseDataFilterAdapter {
    private MenuObserver mObserver;


    public void registerObserever(MenuObserver observer) {
        mObserver = observer;
    }

    public void unRegisterObserver(MenuObserver observer) {
        mObserver = null;
    }

    public void notifyCloseMenu() {
        if (mObserver == null) return;
        mObserver.closeMenu();
    }

    public abstract int getCount();

    public abstract View getTabView(int position, ViewGroup parent);

    public abstract View getMenuView(int position, ViewGroup parent);

    public abstract void openMenu(View tabView);

    public abstract void closeMenu(View tabView);
}
