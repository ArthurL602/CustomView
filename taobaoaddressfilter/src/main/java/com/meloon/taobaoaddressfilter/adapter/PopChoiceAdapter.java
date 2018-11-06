package com.meloon.taobaoaddressfilter.adapter;

import android.widget.TextView;

import com.meloon.taobaoaddressfilter.R;

import java.util.List;

/**
 * Author      :meloon
 * Date        :2018/10/31
 * Description :
 */
public class PopChoiceAdapter extends BaseRvAdapter<String> {
    public PopChoiceAdapter(List<String> datas) {
        super(R.layout.item_rv_choice, datas);
    }

    @Override
    public void bindView(BaseRvHolder holder, int position, String data) {
        TextView tv = holder.getView(R.id.tv_choice);
        tv.setText(data);
    }
}
