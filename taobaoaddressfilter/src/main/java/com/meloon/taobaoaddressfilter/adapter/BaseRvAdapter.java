package com.meloon.taobaoaddressfilter.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Author      :meloon
 * Date        :2018/7/2
 * Description :
 */

public abstract class BaseRvAdapter<T> extends RecyclerView.Adapter<BaseRvHolder> {
    private List<T> mDatas;
    private int mLayoutId;
    private OnItemClickListener mItemClickListener;

    public List<T> getDatas() {
        return mDatas;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public BaseRvAdapter(int layoutId, List<T> datas) {
        mDatas = datas;
        mLayoutId = layoutId;
    }

    @Override
    public BaseRvHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        BaseRvHolder holder = new BaseRvHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final BaseRvHolder holder, final int position) {
        T t = mDatas.get(position);
        if (t != null) {
            bindView(holder, position, t);
        }
        if (mItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(position, holder);
                }
            });
        }
    }

    public abstract void bindView(BaseRvHolder holder, int position, T data);

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public void addAllData(List<T> datas) {
        clearData();
        mDatas.addAll(datas);
    }
    public void clearData(){
        mDatas.clear();
    }

    public interface OnItemClickListener {
        void onItemClick(int position, BaseRvHolder holder);
    }
}
