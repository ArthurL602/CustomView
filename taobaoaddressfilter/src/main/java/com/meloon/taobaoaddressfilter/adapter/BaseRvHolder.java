package com.meloon.taobaoaddressfilter.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Author      :meloon
 * Date        :2018/7/2
 * Description :
 */

public class BaseRvHolder extends RecyclerView.ViewHolder {
    private SparseArray<View> mViews;

    public BaseRvHolder(View itemView) {
        super(itemView);
        mViews = new SparseArray<>();
    }


    public <V extends View> V getView(int resId) {
        View view = mViews.get(resId);
        if (view == null) {
            view = itemView.findViewById(resId);
            mViews.put(resId, view);
        }
        return (V) view;
    }

    public BaseRvHolder setText(String msg, int viewId) {
        TextView tv = getView(viewId);
        tv.setText(msg);
        return this;
    }

    public BaseRvHolder setImageView(int resId,int viewId){
        ImageView  iv = getView(viewId);
        iv.setImageResource(resId);
        return this;
    }

}
