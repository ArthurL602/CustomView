package com.ljb.verticaldraglayout;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;

/**
 * Author      :ljb
 * Date        :2018/6/4
 * Description : 垂直拖拽列表(仿汽车之间列表)
 */

public class VerticalDragLayout extends FrameLayout {

    /*ViewDragHelper对象*/
    private ViewDragHelper mViewDragHelper;
    /*拖拽的列表View*/
    private View mDragView;
    /*菜单View*/
    private View mMenuView;
    /*菜单高度*/
    private int mMenuHeight;
    /*菜单是否打开*/
    private boolean mMenuIsOpen = false;
    private int mLastY = 0;
    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {


        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            // 只能是列表才能拖动
            if (mDragView == child) {
                return true;
            }
            return false;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            // 垂直拖动移动的位置
            if (top <= 0) {
                top = 0;
            } else if (top >= mMenuHeight) {
                top = mMenuHeight;
            }
            return top;
        }
        // 手指松开
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (releasedChild == mDragView) {
                if (mDragView.getTop() >= mMenuHeight / 2) {
                    openMenu();
                } else {
                    closeMenu();
                }
                invalidate();
            }
        }
    };

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    public VerticalDragLayout(@NonNull Context context) {
        this(context, null);
    }

    public VerticalDragLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalDragLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mViewDragHelper = ViewDragHelper.create(this, mCallback);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if (childCount != 2) {
            throw new RuntimeException("只能有两个子View");
        }
        mDragView = getChildAt(1);
        mMenuView = getChildAt(0);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mMenuHeight = mMenuView.getMeasuredHeight();
        }
    }


    /**
     * 如果ViewDragHelper接收到了MOVE事件，而没有接收到DOWN事件，会抛如下异常:
     * Ignoring pointerId=0 because ACTION_DOWN was not received for this pointer before ACTION_MOVE. It likely
     * happened because  ViewDragHelper did not receive all the events in the event stream.
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 菜单打开，全部拦截
        if (mMenuIsOpen) {
            return true;
        }
        int moveY = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 让ViewDragHelper拿个完整的事件
                mViewDragHelper.processTouchEvent(ev);
                mLastY = moveY;
                break;
            case MotionEvent.ACTION_MOVE:
                    // 如果菜单是关闭的，并且列表已经是最顶部了，并且往下滑动
                    if ((!canChildScrollUp() && (moveY - mLastY) > 0 && !mMenuIsOpen)) {
                        mLastY = moveY;
                        return true;
                    }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // ViewDragHelper处理事件
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    /**
     * 关闭菜单
     */
    private void closeMenu() {
        mViewDragHelper.settleCapturedViewAt(0, 0);
        mMenuIsOpen = false;
    }

    /**
     * 打开菜单
     */
    private void openMenu() {
        mViewDragHelper.settleCapturedViewAt(0, mMenuHeight);
        mMenuIsOpen = true;
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     * 判断View是否滚动到了最顶部,还能不能向上滚
     */
    public boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mDragView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mDragView;
                return absListView.getChildCount() > 0 && (absListView.getFirstVisiblePosition() > 0 || absListView
                        .getChildAt(0).getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mDragView, -1) || mDragView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mDragView, -1);
        }
    }
}
