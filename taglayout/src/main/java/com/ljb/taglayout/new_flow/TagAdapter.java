package com.ljb.taglayout.new_flow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Author      :ljb
 * Date        :2019/9/22
 * Description :
 */
public abstract class TagAdapter {

    private OnDataChangedListener mOnDataChangedListener;

    public abstract int getItemCount();

    public abstract View createView(LayoutInflater inflater, ViewGroup parent, int position);

    public abstract void bindView(View view, int position);

    public void setOnItemClick(View v, int position) {
    }

    public void tipForSelectedMax(View v, int maxSelectedCount) {
    }

    public void onItemSelected(View v, int position) {
    }

    public void onItemUnSelected(View v, int position) {
    }

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    public void setOnDataChangedListener(OnDataChangedListener onDataChangedListener) {
        mOnDataChangedListener = onDataChangedListener;
    }


}
