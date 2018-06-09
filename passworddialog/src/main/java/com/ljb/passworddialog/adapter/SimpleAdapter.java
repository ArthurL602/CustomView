package com.ljb.passworddialog.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.ljb.passworddialog.R;

import java.util.List;

/**
 * Author      :ljb
 * Date        :2018/3/28
 * Description :
 */

public class SimpleAdapter extends RecyclerView.Adapter<Holder> {
    private List<String> mDatas;

    private OnItemClickListener mItemClickListener;

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public SimpleAdapter(List<String> datas) {
        mDatas = datas;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_item_keyboard, parent, false);
        int screenWidth = parent.getResources().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = screenWidth / 6;
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final Holder holder, final int position) {
        TextView tv = holder.getView(R.id.tv_keyboard_num);
        if (mDatas.get(position).equals("00")) {
            tv.setText("删除");
        } else if (mDatas.get(position).equals("01")) {
            tv.setText("确认");
        } else {
            tv.setText(mDatas.get(position));
        }
        if (mItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.Onclick(position, holder);
                }
            });
        }
       holder.itemView.setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View v, MotionEvent event) {
               switch(event.getAction()){
                   case MotionEvent.ACTION_DOWN:
                       holder.itemView.setFocusable(true);
                   break;
                   case MotionEvent.ACTION_UP:
                       holder.itemView.setFocusable(false);
                       break;
               }
               return false;
           }
       });

    }


    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public interface OnItemClickListener {
        void Onclick(int position, Holder holder);
    }
}
