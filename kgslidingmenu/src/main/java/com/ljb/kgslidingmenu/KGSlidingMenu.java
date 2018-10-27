package com.ljb.kgslidingmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;



/**
 * Author      :ljb
 * Date        :2018/6/3
 * Description : 仿酷狗侧滑菜单
 *
 * 1. 对指定内容宽度（屏幕宽度）和内容宽度
 2. 默认是关闭的，手指抬起判断是打开状态还是关闭状态
 3. 处理快速滑动
 4. 处理内容缩放，菜单部分有位移和透明度
 5. 事件分发
 */
public class KGSlidingMenu extends HorizontalScrollView {

    private int mMenuWidth;
    private View mMenuView;
    private View mContentView;
    private GestureDetector mGestureDetector;
    //菜单是否打开
    private boolean mMenuIsOpen = false;
    // 是否拦截onTouchEvent()里面的代码
    private boolean mIsInterceptTouchEvent=false;

    public KGSlidingMenu(Context context) {
        this(context, null);
    }

    public KGSlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // 快速滑动处理打开/关闭菜单
            // 左滑 velocityX<0 右滑 velocityX>0
            if (mMenuIsOpen) {
                if (velocityX < 0 && Math.abs(velocityX)>Math.abs(velocityY)) {// 关闭
                    closeMenu();
                    return true;
                }
            } else {
                if (velocityX > 0&& Math.abs(velocityX)>Math.abs(velocityY)) {//打开
                    openMenu();
                    return true;
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    };

    public KGSlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 获取自定义属性
        initAttr(context, attrs);
        setOverScrollMode(OVER_SCROLL_NEVER);
        mGestureDetector = new GestureDetector(context, mOnGestureListener);
    }

    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.KGSlidingMenu);
        float rightMargin = 0;
        if (ta != null) {
            rightMargin = ta.getDimension(R.styleable.KGSlidingMenu_menuRightMargin, 0);
            ta.recycle();
        }
        mMenuWidth = (int) (getScreentWidth(context) - rightMargin);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 包裹View
        ViewGroup contentParent = (ViewGroup) getChildAt(0);
        int childCount = contentParent.getChildCount();
        if (childCount != 2) {
            throw new RuntimeException("只能放2子View");
        }
        // 菜单View
        mMenuView = contentParent.getChildAt(0);
        // 设置菜单宽度
        ViewGroup.LayoutParams lp1 = mMenuView.getLayoutParams();
        lp1.width = mMenuWidth;
        //
        mMenuView.setLayoutParams(lp1);

        // 内容View
        mContentView = contentParent.getChildAt(1);
        // 设置内容View的宽度
        ViewGroup.LayoutParams lp2 = mContentView.getLayoutParams();
        lp2.width = (int) getScreentWidth(getContext());
        mContentView.setLayoutParams(lp2);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // 布局摆放完毕后，再调用scrollTo()方法才能起到作用（默认关闭菜单）
        scrollTo(mMenuWidth, 0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 菜单打开时，触摸右边部分，事件不会分发到子View，并且关闭菜单
        mIsInterceptTouchEvent=false;
        if(mMenuIsOpen){
            float currentX =ev.getX();
            if(currentX>mMenuWidth){
                // 1. 关闭菜单
                closeMenu();
                // 3. 因为返回true拦截了事件，所以会回调onTouchEvent()，所以得搞个开关控制
                // 能不能调用onTouchEvent()里面的代码，否则上面的colseMenu()会有问题
                mIsInterceptTouchEvent=true;
                // 2. 返回true拦截事件，事件不会分发到子View
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);

    }

    // 处理手指抬起逻辑，打开菜单/关闭菜单，通过滚动位置进行判断
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // 拦截onTouchEvent后续代码
        if(mIsInterceptTouchEvent) return true;
        if (mGestureDetector.onTouchEvent(ev)) {
            // 如果mGestureDetecotr已经处理了滑动事件，事件就没必要继续
            // 往下传递，否则在onFling()中的实现的效果，会被后面的方法
            // 覆盖掉
            return true;
        }
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            int currentScrollX = getScrollX();
            if (currentScrollX > mMenuWidth / 2) {//关闭菜单
                closeMenu();
            } else {//打开菜单
                openMenu();
            }
            // 确保 super.onTouchEvent()不会执行
            // 如果不这么处理，smoothScrollTo()方法里调用的 mScroller.startScroll()会被
            // super.onTouchEvent(）内调用的  mScroller.fling()覆盖
            return true;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 滑动过程中，需要实现的效果：
     * 1. 右边缩放
     * 2. 左边缩放和透明度变化
     *
     * @param l
     * @param t
     * @param oldl
     * @param oldt
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // l 的变化范围 0 - mMenuWidth
        // 1. 设置右边缩放
        // 1.1 计算一个梯度值
        float scale = 1f * l / mMenuWidth;
        // 1.2 右边缩放变化范围：0.7f - 1f
        float rightScale = 0.7f + 0.3f * scale;
        // 1.3 进行缩放 PS:如果不对缩放中心点进行设置，默认是View的中心位置
        ViewCompat.setPivotX(mContentView, 0);
        ViewCompat.setPivotY(mContentView, mContentView.getMeasuredHeight() / 2);
        ViewCompat.setScaleX(mContentView, rightScale);
        ViewCompat.setScaleY(mContentView, rightScale);

        // 2. 设置左边菜单缩放和透明度
        // 2.1 设置透明度
        float alpha = 0.5f + (1 - scale) * 0.5f;
        ViewCompat.setAlpha(mMenuView, alpha);
        // 2.2 设置缩放
        float leftScale = 0.7f + (1 - scale) * 0.3f;
        ViewCompat.setScaleX(mMenuView, leftScale);
        ViewCompat.setScaleY(mMenuView, leftScale);
        // 2.3 用平移，实现抽屉效果
        ViewCompat.setTranslationX(mMenuView, l * 0.25f);

    }

    /**
     * 打开菜单，滚动到（0,0）
     */
    private void openMenu() {
        smoothScrollTo(0, 0);
        mMenuIsOpen = true;
    }

    /**
     * 关闭菜单，滚动到 （mMenuWidth，0）
     */
    private void closeMenu() {
        smoothScrollTo(mMenuWidth, 0);
        mMenuIsOpen = false;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    private float getScreentWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

}
