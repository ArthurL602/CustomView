package com.meloon.taobaoaddressfilter;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Author      :meloon
 * Date        :2018/10/31
 * Description :
 */
public class MyViewPager extends ViewPager {
    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public void setCurrentItem(int item) {
        // 禁止滑动效果
        super.setCurrentItem(item,false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
