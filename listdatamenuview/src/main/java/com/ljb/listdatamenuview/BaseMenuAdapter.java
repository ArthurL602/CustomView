package com.ljb.listdatamenuview;

import android.view.View;
import android.view.ViewGroup;

/**
 * Author      :ljb
 * Date        :2018/6/6
 * Description : 筛选菜单的adapter
 */
public abstract class BaseMenuAdapter {
    /*获得有多少个菜单*/
    public abstract int getCount();

    /*获取菜单TabView*/
    public abstract View getView(int position, ViewGroup parent);

    /*获取菜单内容*/
    public abstract View getMenuView(int position, ViewGroup parent);

    /**
     * 菜单打开
     *
     * @param tabView
     */
    public void menuOpen(View tabView) {

    }

    /**
     * 菜单关闭
     *
     * @param tabView
     */
    public void menuClose(View tabView) {

    }
}
