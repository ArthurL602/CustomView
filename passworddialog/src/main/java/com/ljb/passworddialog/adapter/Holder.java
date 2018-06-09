package com.ljb.passworddialog.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

import com.ljb.passworddialog.R;
/**
 * Author      :ljb
 * Date        :2018/3/28
 * Description :
 */

public class Holder extends RecyclerView.ViewHolder {
    private SparseArray<View> mViews;


    public Holder(View itemView) {
        super(itemView);
        mViews = new SparseArray<>();
    }

    public <V> V getView(int viewId) {
        View child = mViews.get(viewId);
        if (child == null) {
            child = itemView.findViewById(R.id.tv_keyboard_num);
            mViews.put(viewId, child);
        }
        return (V) child;
    }

}
