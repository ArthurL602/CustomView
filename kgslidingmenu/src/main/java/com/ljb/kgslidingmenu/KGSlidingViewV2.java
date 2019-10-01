package com.ljb.kgslidingmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

/**
 * Author      :ljb
 * Date        :2019/10/1
 * Description :
 */
public class KGSlidingViewV2 extends HorizontalScrollView {
    private int mMenuRightMargin = 100;
    private int mMenuWidth;
    private View mMenuView;
    private View mContentView;

    private GestureDetector mGestureDetector;

    private boolean isOpen = false;

    private int mMinTouch = 150;

    private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int distance = (int) (e2.getX() - e1.getX());

            if (Math.abs(distance) < mMinTouch || Math.abs(velocityY) >= Math.abs(velocityX)) {
                return super.onFling(e1, e2, velocityX, velocityY);
            }

            if (isOpen && velocityX < 0) {
                closeMenu();
                return true;
            }

            if (!isOpen && velocityX > 0) {
                openMenu();
                return true;
            }


            return super.onFling(e1, e2, velocityX, velocityY);
        }
    };


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (isOpen) {
                int downX = (int) ev.getX();
                if (downX > mMenuWidth) {
                    closeMenu();
                    return false;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public KGSlidingViewV2(Context context) {
        this(context, null);
    }

    public KGSlidingViewV2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KGSlidingViewV2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
        mGestureDetector = new GestureDetector(context, mSimpleOnGestureListener);
    }

    private void init(Context context, AttributeSet attrs) {
        initAttrs(context, attrs);
        mMenuWidth = getScreenWidth() - mMenuRightMargin;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ViewGroup container = (ViewGroup) getChildAt(0);

        if (container == null || container.getChildCount() != 2) {
            throw new RuntimeException("error");
        }
        mMenuView = container.getChildAt(0);
        ViewGroup.LayoutParams menuParams = mMenuView.getLayoutParams();
        menuParams.width = mMenuWidth;
        mMenuView.setLayoutParams(menuParams);

        mContentView = container.getChildAt(1);
        ViewGroup.LayoutParams contentParams = mContentView.getLayoutParams();
        contentParams.width = getScreenWidth();
        mContentView.setLayoutParams(contentParams);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        scrollTo(mMenuWidth, 0);
        isOpen = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mGestureDetector.onTouchEvent(ev)) {
            return true;
        }
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            int scrollX = getScrollX();
            if (scrollX > mMenuWidth / 2) {
                // close  menu
                closeMenu();
            } else {
                // open menu
                openMenu();
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        float scale = 1.0f * l / mMenuWidth;
        // 缩放右边内容界面
        float rightScale = 0.7f + scale * 0.3f;
        ViewCompat.setPivotX(mContentView, 0);
        ViewCompat.setPivotY(mContentView, mContentView.getMeasuredHeight() / 2);
        ViewCompat.setScaleX(mContentView, rightScale);
        ViewCompat.setScaleY(mContentView, rightScale);

        // 缩放左边菜单界面
        // 1. 控制透明度
        float leftAlpha = 0.5f + (1 - scale) * 0.5f;
        ViewCompat.setAlpha(mMenuView, leftAlpha);

        // 2. 控制缩放度
        float leftScale = 0.5f + (1 - scale) * 0.5f;
        ViewCompat.setScaleX(mMenuView, leftScale);
        ViewCompat.setScaleY(mMenuView, leftScale);

        // 3. 控制位移
        ViewCompat.setTranslationX(mMenuView, l * 0.25f);

    }

    private void openMenu() {
        smoothScrollTo(0, 0);
        isOpen = true;
    }

    private void closeMenu() {
        smoothScrollTo(mMenuWidth, 0);
        isOpen = false;

    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.KGSlidingViewV2);
        if (typedArray != null) {
            mMenuRightMargin = typedArray.getDimensionPixelSize(R.styleable.KGSlidingViewV2_menuRightMargin, mMenuRightMargin);
            typedArray.recycle();
        }
    }


    private int getScreenWidth() {
        return getContext().getResources().getDisplayMetrics().widthPixels;
    }
}
