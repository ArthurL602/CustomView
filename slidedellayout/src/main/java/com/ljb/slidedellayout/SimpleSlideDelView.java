package com.ljb.slidedellayout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Author      :ljb
 * Date        :2018/3/6
 * Description : 自定义侧滑删除View
 */
public class SimpleSlideDelView extends ViewGroup {

    /*侧滑菜单宽度总和(最大滑动距离)*/
    private int mMenuWidth;

    /*Item的高度*/
    private int mHeight;

    /*存储contentView(第一个View)*/
    private View mContentView;

    /*滑动判定临界值（右侧菜单宽度的40%） 手指抬起时，超过了展开，没超过收起menu*/
    private int mLimit;

    /*左滑右滑的开关,默认左滑打开菜单*/
    private boolean isLeftSwipe;

    /*右滑删除功能的开关,默认开*/
    private static boolean isSwipeEnable;

    /*防止多只手指一起滑我的flag 在每次down里判断， touch事件结束清空*/
    private static boolean isTouching;

    /*上一次的xy*/
    private PointF mLastP = new PointF();

    //判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件。
    //up-down的坐标，判断是否是滑动，如果是，则屏蔽一切点击事件
    private PointF mFirstP = new PointF();

    /*存储的是当前正在展开的View*/
    private static SimpleSlideDelView mViewCache;

    private ValueAnimator mCloseAnim;

    private ValueAnimator mExpandAnim;

    /*是否拦截事件的*/
    private boolean mInterceptEvent;

    /*表示滑动的时候，手的移动要大于这个距离才开始移动控件*/
    private float mScaleTouchSlop;

    /*是否可以两个item滑动，即当存在一个侧滑菜单后，如果滑动其他的item，则滑动的item马上展示item,并且前一个侧滑菜单还原*/
    private boolean isSlideTogether;

    public SimpleSlideDelView(Context context) {
        this(context, null);
    }

    public SimpleSlideDelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleSlideDelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mScaleTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        //是否支持侧滑删除功能的开关,默认开
        isSwipeEnable = true;
        //左滑右滑的开关,默认左滑打开菜单
        isLeftSwipe = true;
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SlideDelView, defStyleAttr, 0);
        int count = ta.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = ta.getIndex(i);
            //如果引用成AndroidLib 资源都不是常量，无法使用switch case
            if (attr == R.styleable.SlideDelView_swipeEnable) {
                isSwipeEnable = ta.getBoolean(attr, true);
            } else if (attr == R.styleable.SlideDelView_leftSwipe) {
                isLeftSwipe = ta.getBoolean(attr, true);
            } else if (attr == R.styleable.SlideDelView_isSlideTogether) {
                isSlideTogether = ta.getBoolean(attr, true);
            }
        }
        ta.recycle();
    }

    /* 设置侧滑功能开关*/
    public boolean isSwipeEnable() {
        return isSwipeEnable;
    }


    public boolean isLeftSwipe() {
        return isLeftSwipe;
    }

    /**
     * 设置是否开启左滑出菜单，设置false 为右滑出菜单
     *
     * @param leftSwipe
     * @return
     */
    public SimpleSlideDelView setLeftSwipe(boolean leftSwipe) {
        isLeftSwipe = leftSwipe;
        return this;
    }

    /**
     * 返回ViewCache
     *
     * @return
     */
    public static SimpleSlideDelView getViewCache() {
        return mViewCache;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /*令自己可点击，从而获取到点击事件*/
        setClickable(true);
        /*由于ViewHolder的复用机制，所以这里需要清零*/
        mMenuWidth = 0;
        mHeight = 0;
        /*适配GridLayoutManager，将第一个Item的宽度（即ContentItem）为控件宽度*/
        int contentWidth = 0;
        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            childView.setClickable(true);
            if (childView.getVisibility() != GONE) {
                //测量子View
                measureChild(childView, widthMeasureSpec, heightMeasureSpec);
                mHeight =  childView.getMeasuredHeight();
                if (i > 0) {
                    //第一个布局是内容Item，从第二个开始才是菜单
                    mMenuWidth += childView.getMeasuredWidth();
                } else {
                    mContentView = childView;
                    contentWidth = childView.getMeasuredWidth();
                }
            }
        }
        //宽度取第一个Item(Content)的宽度
        setMeasuredDimension(getPaddingLeft() + getPaddingRight() + contentWidth, mHeight + getPaddingTop() +
                getPaddingBottom());
        //滑动判断的临界值
        mLimit = mMenuWidth * 4 / 10;//滑动判断的临界值
    }

    /**
     * 设置子View的位置
     *
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingLeft();
        int right = getPaddingLeft();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() != GONE) {
                // 第一个View是内容，宽度设置为全屏
                if (i == 0) {
                    childView.layout(left, getPaddingTop(), left + childView.getMeasuredWidth(), getPaddingTop() +
                            childView.getMeasuredHeight());
                    left = left + childView.getMeasuredWidth();
                } else {
                    //判断是否是左滑删除
                    if (isLeftSwipe) {
                        childView.layout(left, getPaddingTop(), left + childView.getMeasuredWidth(), getPaddingTop()
                                + childView.getMeasuredHeight());
                        left = left + childView.getMeasuredWidth();
                    } else {
                        childView.layout(right - childView.getMeasuredWidth(), getPaddingTop(), right, getPaddingTop
                                () + childView.getMeasuredHeight());
                        right = right - childView.getMeasuredWidth();
                    }
                }
            }
        }
    }

    /**
     * @param event
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isSwipeEnable) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //每次DOWN时，默认是不拦截的
                    mInterceptEvent = false;
                    //如果有别的指头摸过了，那么就return false。这样后续的move..等事件也不会再来找这个View了。
                    if (isTouching) {
                        return false;
                    } else {
                        isTouching = true;
                    }
                    mLastP.set(event.getRawX(), event.getRawY());
                    //判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件。
                    mFirstP.set(event.getRawX(), event.getRawY());
                    //如果down，view和cacheview不一样，则立马让它还原。且把它置为null
                    // 如果down, 存在展开菜单的View，并且不是down下的View,则关闭菜单，并将cacheView置空
                    if (mViewCache != null) {
                        if (mViewCache != this) {
                            mViewCache.smoothClose();
                            //当前有侧滑菜单的View，并且点击的不是自己，就该拦截事件
                            mInterceptEvent = isSlideTogether;
                        }
                        // 如果存在一个展开的菜单View，禁止父View拦截事件和处理事件
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    //当前有侧滑菜单的View，且不是自己的，就该拦截事件,滑动也不该出现
                    if (mInterceptEvent) {
                        break;
                    }
                    //计算滑动滑动的距离
                    float gap = mLastP.x - event.getRawX();
                    //如果是侧滑滑动，则禁止父类ListView RecyclerView等垂直滑动
                    if (Math.abs(gap) > 30 || Math.abs(getScrollX()) > 30) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    //滑动使用scrollBy
                    scrollBy((int) (gap), 0);
                    //越界修正
                    if (isLeftSwipe) {//左滑
                        if (getScrollX() < 0) {
                            scrollTo(0, 0);
                        }
                        if (getScrollX() > mMenuWidth) {
                            scrollTo(mMenuWidth, 0);
                        }
                    } else {//右滑
                        if (getScrollX() < -mMenuWidth) {
                            scrollTo(-mMenuWidth, 0);
                        }
                        if (getScrollX() > 0) {
                            scrollTo(0, 0);
                        }
                    }
                    mLastP.set(event.getRawX(), event.getRawY());
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    //当前存在侧滑View,并且是自己
                    if (!mInterceptEvent) {
                        if (Math.abs(getScrollX()) > mLimit) {//否则就判断滑动距离
                            //平滑展开Menu
                            smoothExpand();
                        } else {
                            // 平滑关闭Menu
                            smoothClose();
                        }
                    }
                    //释放
                    isTouching = false;//没有手指在摸我了
                    break;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 拦截事件
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //禁止侧滑时，点击事件不受干扰。
        if (isSwipeEnable) {
            switch (ev.getAction()) {
                //长按事件和侧滑的冲突。
                case MotionEvent.ACTION_MOVE:
                    //拦截滑动时的事件
                    if (Math.abs(ev.getRawX() - mFirstP.x) > mScaleTouchSlop) {
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    Log.e("TAG", "up");
                    //为了在侧滑时，屏蔽子View的点击事件
                    //侧滑状态时，拦截子View的点击事件
                    if (isLeftSwipe) {
                        if (getScrollX() > mScaleTouchSlop) {
                            //判断是否在内容区域内
                            if (ev.getX() < getWidth() - getScrollX()) {
                                //如果点击内容外区域，缓慢的关闭侧滑菜单。
                                smoothClose();
                                return true;
                            }
                        }
                    } else {
                        if (-getScrollX() > mScaleTouchSlop) {
                            //判断是否点击到内容区域内
                            if (ev.getX() > -getScrollX()) {
                                //如果dian点击到内容区域外，则缓慢的关闭侧滑菜单，并拦截事件
                                smoothClose();
                                return true;
                            }
                        }
                    }
            }
            //如果当前已经存在一个菜单View，并且点击的不是自己，则拦截事件
            if (mInterceptEvent) {
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    //每次ViewDetach的时候，判断一下 ViewCache是不是自己，如果是自己，关闭侧滑菜单，且ViewCache设置为null，
    // 理由：1 防止内存泄漏(ViewCache是一个静态变量)
    // 2 侧滑删除后自己后，这个View被Recycler回收，复用，下一个进入屏幕的View的状态应该是普通状态，而不是展开状态。
    @Override
    protected void onDetachedFromWindow() {
        if (this == mViewCache) {
            mViewCache.quickClose();
            mViewCache = null;
        }
        super.onDetachedFromWindow();

    }

    //展开时，禁止长按
    @Override
    public boolean performLongClick() {
        if (Math.abs(getScrollX()) > mScaleTouchSlop) {
            return false;
        }
        return super.performLongClick();
    }

    /**
     * 快速关闭。
     * 用于 点击侧滑菜单上的选项,同时想让它快速关闭(删除 置顶)。
     * 这个方法在ListView里是必须调用的，
     * 在RecyclerView里，视情况而定，如果是mAdapter.notifyItemRemoved(pos)方法不用调用。
     */
    public void quickClose() {
        if (this == mViewCache) {
            //先取消展开动画
            cancelAnim();
            mViewCache.scrollTo(0, 0);//关闭
            mViewCache = null;
        }
    }


    /**
     * 平滑的开展
     */
    private void smoothExpand() {
        //展开就加入ViewCache：
        mViewCache = this;
        //侧滑菜单展开，屏蔽content长按
        if (null != mContentView) {
            mContentView.setLongClickable(false);
        }
        cancelAnim();
        mExpandAnim = ValueAnimator.ofInt(getScrollX(), isLeftSwipe ? mMenuWidth : -mMenuWidth);
        mExpandAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scrollTo((Integer) animation.getAnimatedValue(), 0);
            }
        });
        mExpandAnim.setInterpolator(new OvershootInterpolator());
        mExpandAnim.setDuration(300).start();
    }


    /**
     * 由于我们希望子View的LayoutParams是MarginLayoutParams，需要如下重写generateLayoutParams（）这个方法
     *
     * @param attrs
     * @return
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    /**
     * 平滑关闭
     */
    public void smoothClose() {
        mViewCache = null;
        //侧滑菜单展开，屏蔽content长按
        if (null != mContentView) {
            mContentView.setLongClickable(true);
        }
        cancelAnim();
        mCloseAnim = ValueAnimator.ofInt(getScrollX(), 0);
        mCloseAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scrollTo((Integer) animation.getAnimatedValue(), 0);
            }
        });
        mCloseAnim.setInterpolator(new AccelerateInterpolator());
        mCloseAnim.setDuration(300).start();

    }

    /**
     * 每次执行动画之前，应该先取消前面的动画
     */
    private void cancelAnim() {
        if (mCloseAnim != null && mCloseAnim.isRunning()) {
            mCloseAnim.cancel();
        }
        if (mExpandAnim != null && mExpandAnim.isRunning()) {
            mExpandAnim.cancel();
        }
    }
}
