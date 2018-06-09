package com.ljb.qqslidingmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;


/**
 * Author      :ljb
 * Date        :2018/6/3
 * Description : 仿QQ侧滑菜单
 */
public class QQSlidingMenu extends HorizontalScrollView {
    private float mMenuRightMargin;
    /*菜单宽度*/
    private int mMenuWidth;
    /*主内容View*/
    private View mContentView;
    /*菜单View*/
    private View mMenuView;
    /*菜单是否打开*/
    private boolean mMenuIsOpen;
    /*手势处理*/
    private GestureDetector mGestureDetector;
    /*是否拦截*/
    private boolean mIsIntercept;
    /*遮盖View*/
    private View mShadowView;
    /**
     * 解决快速滑动切换
     */
    private GestureDetector.OnGestureListener mListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // 快速滑动，打开/关闭菜单
            // 条件：（相对于主菜单的打开和关闭    ）打开的时候往左边快速滑动切换（关闭），关闭的时候往右快速滑动切换（打开）
            if (mMenuIsOpen) {
                //打开的时候往左边快速滑动切换（关闭）
                if (velocityX < 0 && Math.abs(velocityX) > Math.abs(velocityY)) {
                    closeMenu();
                    return true;
                }
            } else {
                //关闭的时候往右快速滑动切换
                if (velocityX > 0 && Math.abs(velocityX) > Math.abs(velocityY)) {
                    openMenu();
                    return true;
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    };


    public QQSlidingMenu(Context context) {
        this(context, null);
    }

    public QQSlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QQSlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainAttrs(context, attrs);
        // 计算菜单页的宽度
        mMenuWidth = (int) (getScreenWith() - mMenuRightMargin);
        mGestureDetector = new GestureDetector(getContext(), mListener);
    }

    private void obtainAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.KGSlidingMenu);
        mMenuRightMargin = ta.getDimension(R.styleable.KGSlidingMenu_menuRightMargin, dp2px(50));
        ta.recycle();
    }

    @Override
    protected void onFinishInflate() {
        // 这个方法是布局解析完毕才会回调，也就是XML解析完毕
        super.onFinishInflate();
        // 也就是LinearLayout
        ViewGroup container = (ViewGroup) getChildAt(0);
        int childCount = container.getChildCount();
        if (childCount != 2) {
            throw new RuntimeException("只能放2子View");
        }
        // 1. 菜单页是屏幕的宽度
        mMenuView = container.getChildAt(0);
        ViewGroup.LayoutParams menuParam = mMenuView.getLayoutParams();
        menuParam.width = mMenuWidth;
        mMenuView.setLayoutParams(menuParam);
        // 2. 内容页的宽度是屏幕的宽度 - 右边一小部分宽度（自定义属性）
        mContentView = container.getChildAt(1);
        ViewGroup.LayoutParams contentParam = mContentView.getLayoutParams();
        // 把内容布局单独提取出来,并构建一个ViewGroup将其包裹;
        container.removeView(mContentView);
        RelativeLayout contentContainer = new RelativeLayout(getContext());
        contentContainer.addView(mContentView);
        // 然后在外面套一层阴影;
        mShadowView = new View(getContext());
        mShadowView.setBackgroundColor(Color.parseColor("#55000000"));
        contentContainer.addView(mShadowView);
        // 最后在把容器放回原来的位置
        contentParam.width = getScreenWith();
        contentContainer.setLayoutParams(contentParam);
        container.addView(contentContainer);
        mShadowView.setAlpha(0.0f);
    }

    /**
     * 手指抬起，要么关闭，要么打开
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mIsIntercept) return true;
        if (mGestureDetector.onTouchEvent(ev)) {// 快速滑动触发了，就不要继续下面的判断
            return true;
        }
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            // 根据当前滚动的距离来判断
            int currentScrollX = getScrollX();
            if (currentScrollX > mMenuWidth / 2) { // 关闭
                closeMenu();

            } else {  // 打开
                openMenu();
            }
            // UP事件返回true，不交非HorizontalScrollView处理，因为它会覆盖我的操作，达不到我们要的效果
            return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mIsIntercept = false;
        // 菜单打开时，点击内容页需要拦截事件
        if (mMenuIsOpen) {
            float currentX = ev.getX();
            if (currentX > mMenuWidth) {
                // 关闭菜单
                closeMenu();
                mIsIntercept = true;
                // 拦截事件，但是会相应onTouchEvent()事件
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // 算一个梯度值
        float scale = 1f * l / mMenuWidth; // 变化 1-0
        float alphaScale = 1 - scale;
        mShadowView.setAlpha(alphaScale);
        // 最后一个效果，抽屉效果 l * 0.25f
        ViewCompat.setTranslationX(mMenuView, 0.6f * l);
    }

    /**
     * 打开菜单，滚动到0
     */
    private void openMenu() {
        smoothScrollTo(0, 0);
        mMenuIsOpen = true;
    }

    /**
     * 关闭菜单， 滚动到 mMenuWidth
     */
    private void closeMenu() {
        smoothScrollTo(mMenuWidth, 0);
        mMenuIsOpen = false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // 初始化进来是关闭的
        scrollTo(mMenuWidth, 0);
    }

    /**
     * dp -- > px
     *
     * @param value
     * @return
     */
    private int dp2px(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    /**
     * 获取屏幕的宽度
     *
     * @return
     */
    private int getScreenWith() {
        return getResources().getDisplayMetrics().widthPixels;
    }

}
