package com.ljb.qqslidingmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;

/**
 * Author      :ljb
 * Date        :2019/10/2
 * Description :
 */
public class QQSlidingViewV2 extends HorizontalScrollView {

    private View mMenuView;
    private int mMenuWidth;
    private int mMenuRightMargin = 100;
    private View mContentView;
    private View mShadowView;
    private int mShadowColor = Color.parseColor("#55000000");
    private int mMinTouch = 150;
    private GestureDetector mGestureDetector;

    private boolean mMenuIsOpen = false;

    private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            int distant = (int) (e2.getX() - e1.getX());
            if (Math.abs(velocityY) < Math.abs(velocityX) && Math.abs(distant) > mMinTouch) {
                if (!mMenuIsOpen && velocityX > 0) {
                    openMenu();
                }
                if (mMenuIsOpen && velocityX < 0) {
                    closeMenu();
                }
                return true;
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    };


    public QQSlidingViewV2(Context context) {
        this(context, null);
    }

    public QQSlidingViewV2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QQSlidingViewV2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initAttrs(context, attrs);
        mGestureDetector = new GestureDetector(context, mSimpleOnGestureListener);
        mMenuWidth = getScreenWidth(context) - mMenuRightMargin;
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.QQSlidingViewV2);
        if (typedArray != null) {
            mMenuRightMargin = typedArray.getDimensionPixelSize(R.styleable.QQSlidingViewV2_qqMenuRightMargin, mMenuRightMargin);
            typedArray.recycle();
        }
    }

    private int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ViewGroup container = (ViewGroup) getChildAt(0);
        if (container == null || container.getChildCount() != 2) {
            throw new RuntimeException("error");
        }
        // 菜单页
        mMenuView = container.getChildAt(0);
        ViewGroup.LayoutParams menuLayoutParams = mMenuView.getLayoutParams();
        menuLayoutParams.width = mMenuWidth;
        mMenuView.setLayoutParams(menuLayoutParams);
        // 内容页
        mContentView = container.getChildAt(1);
        container.removeView(mContentView);

        ViewGroup.LayoutParams contentLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentLayoutParams.width = getScreenWidth(getContext());
        FrameLayout contentContainer = new FrameLayout(getContext());
        // 添加内容页
        contentContainer.addView(mContentView);
        // 添加阴影页
        mShadowView = new View(getContext());
        mShadowView.setBackgroundColor(mShadowColor);
        mShadowView.setAlpha(0.0f);
        contentContainer.addView(mShadowView);
        // 添加contentContainer
        container.addView(contentContainer, contentLayoutParams);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        scrollTo(mMenuWidth, 0);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        float scale = 1.0f * l / mMenuWidth;
        float alphaScale = 1 - scale;
        if (mShadowView != null) {
            mShadowView.setAlpha(alphaScale);//控制阴影
        }

        ViewCompat.setTranslationX(mMenuView, 0.5f * l);// 形成抽屉效果
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (ev.getX() > mMenuWidth && mMenuIsOpen) {
                closeMenu();
                return false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mGestureDetector.onTouchEvent(ev)) {
            return true;
        }

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            int scrollX = getScrollX();
            if (scrollX < mMenuWidth / 2) {
                openMenu();
            } else {
                closeMenu();
            }
            return true;
        }

        return super.onTouchEvent(ev);
    }

    private void openMenu() {
        smoothScrollTo(0, 0);
        mMenuIsOpen = true;
    }

    private void closeMenu() {
        smoothScrollTo(mMenuWidth, 0);
        mMenuIsOpen = false;
    }
}
