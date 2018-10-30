package com.meloon.dragview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Author      :meloon
 * Date        :2018/10/30
 * Description : 自定义拖拽View
 */
public class DragView extends View{
    public DragView(Context context) {
        super(context);
    }

    public DragView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DragView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int lastX;
    private int lastY;
    private int left;
    private int top;
    private int right;
    private int bottom;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (x - lastX);
                int dy = (y - lastY);
                //移动中动态设置位置
                left = getLeft() + dx;
                top = getTop() + dy;
                right = getRight() + dx;
                bottom = getBottom() + dy;
                //不超出屏幕范围
                if (left < 0) {
                    left = 0;
                    right = left + getWidth();
                }
                if (right > getScreenWidth()) {
                    right = (int) getScreenWidth();
                    left = right - getWidth();
                }
                if (top < 0) {
                    top = 0;
                    bottom = top + getHeight();
                }
                if (bottom > getParentHeight()) {
                    bottom = getParentHeight();
                    top = bottom - getHeight();
                }
                //left：控件的左边相对父控件的左边的距离，top：上边相对父控件的上边，right：右边相对父控件左边，botton：下边相对父控件上边
                layout(left, top, right, bottom);
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(120,120);

                float currX = getX()+getWidth()/2;
                if (currX >= getScreenWidth() / 2) {
                    right = (int) getScreenWidth();
                    left = right - getWidth();
                    layout(left, top, right, bottom);
                    lp.width = getWidth();
                    lp.height = getHeight();
                    lp.setMargins(left, top, 0, 0);
                    setLayoutParams(lp);
                } else {
                    left = 0;
                    right = left + getWidth();
                    layout(left, top, right, bottom);
                    lp.width = getWidth();
                    lp.height = getHeight();
                    lp.setMargins(0, top, 0, 0);
                    setLayoutParams(lp);
                }
                break;
        }
        return true;
    }

    private float getScreenWidth() {
        return getContext().getResources().getDisplayMetrics().widthPixels;
    }

    private int getParentHeight() {
        ViewGroup group = (ViewGroup) getParent();
        return group.getMeasuredHeight();
    }
}
