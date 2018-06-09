package com.ljb.taglayout;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

/**
 * Author      :ljb
 * Date        :2018/6/2
 * Description : 流式布局的Adapter
 */
public abstract  class TagAdapter {
    /**
     *  有多少个条目
     * @return
     */
    public abstract int getContent();

    /**
     * getView通过position
     * @param position
     * @param parent
     * @return
     */
    public abstract View getView(int position, ViewGroup parent);
    public void unregisterDataSetObserver(DataSetObserver observer){

    }

    public void registerDataSetObserver(DataSetObserver observer){

    }
}
