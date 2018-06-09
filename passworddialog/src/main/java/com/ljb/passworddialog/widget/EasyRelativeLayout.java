package com.ljb.passworddialog.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Author      :ljb
 * Date        :2018/3/29
 * Description :
 */

public class EasyRelativeLayout extends RelativeLayout {
    private static boolean isClick;

    public EasyRelativeLayout(Context context) {
        super(context);
    }

    public EasyRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EasyRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isClick) {
                    isClick = true;
                } else {
                    return false;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isClick = false;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
