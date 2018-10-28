package com.ljb.listdatamenuview;

import android.view.View;
import android.view.ViewGroup;

/**
 * Author      :ljb
 * Date        :2018/6/6
 * Description : 筛选菜单的adapter
 */
public abstract class BaseMenuAdapter {
    private MenuObserver mMenuObserver ;

    /*获得有多少个菜单*/
    public abstract int getCount();

    /*添加菜单TabView*/
    public abstract View addTabView(int position, ViewGroup parent);

    /*获取菜单内容*/
    public abstract View getMenuView(int position, ViewGroup parent);
    /*菜单是否能点击*/
    public abstract boolean tabClickEnable();
    /**
     * 打开菜单
     * @param tabView 菜单View
     * @param openPosition 打开的position
     */
    public void menuOpen(View tabView,int openPosition) {

    }

    /**
     *  关闭菜单
     * @param tabView 菜单View
     * @param closePosition 关闭的position
     */
    public void menuClose(View tabView,int closePosition) {

    }

    public void registerDataSetObserver(MenuObserver observer) {
        mMenuObserver=observer;
    }

    public void unregisterDataSetObserver() {
        mMenuObserver = null;
    }

    public void closeMenu(){
        if(mMenuObserver!=null){
            mMenuObserver.closeMenu();
        }
    }
}
