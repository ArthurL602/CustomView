package com.meloon.taobaoaddressfilter.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.meloon.taobaoaddressfilter.R;
import com.meloon.taobaoaddressfilter.bean.CcbFilterBean;

import java.util.List;


/**
 * Author      :meloon
 * Date        :2018/10/29
 * Description :
 */
public class CcbPopFilterAdapter extends BaseRvAdapter<CcbFilterBean> {

    private OnItemChildClickListener mListener;
    private int clickColor = Color.parseColor("#FFEB5B0E");

    public void setListener(OnItemChildClickListener listener) {
        mListener = listener;
    }

    public CcbPopFilterAdapter(List<CcbFilterBean> datas) {
        super(R.layout.item_rv_timeline, datas);
    }

    @Override
    public void bindView(BaseRvHolder holder, final int position, CcbFilterBean data) {
        TextView textView = holder.getView(R.id.tv_address);
        textView.setText(data.getAddress());
        holder.getView(R.id.v_last_dot).setVisibility(View.VISIBLE);
        if (position == 0) {
            holder.getView(R.id.v_first_dot).setVisibility(View.INVISIBLE);
        }
        if (position == getItemCount() - 1) {
            holder.getView(R.id.v_last_dot).setVisibility(View.INVISIBLE);
        }

        if (data.isSelect()) {
            holder.getView(R.id.iv_dot).setBackgroundResource(R.drawable.dot_timeline_orangle_select);
        } else {
            holder.getView(R.id.iv_dot).setBackgroundResource(R.drawable.dot_timeline_orangle_default);
        }
        if (data.isFocus()) {
            textView.setTextColor(clickColor);
        } else {
            textView.setTextColor(Color.BLACK);
        }

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null ) {
                    mListener.onItemChildClick(v, position);
                }
            }
        });
    }

    /**
     * 重置焦点
     */
    public void resetFocus() {
        for (int i = 0; i < getDatas().size(); i++) {
            getDatas().get(i).setFocus(false);
        }
    }




    /**
     * 重置选择
     */
    public void resetSeclet() {
        for (int i = 0; i < getDatas().size(); i++) {
            getDatas().get(i).setSelect(false);
        }
    }


    public interface OnItemChildClickListener {
        void onItemChildClick(View view, int position);
    }
}
