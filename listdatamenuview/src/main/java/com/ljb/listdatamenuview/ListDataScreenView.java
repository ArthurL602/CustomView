package com.ljb.listdatamenuview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Author      :ljb
 * Date        :2018/6/6
 * Description : 多筛选自定义View
 */
public class ListDataScreenView extends LinearLayout {
    /*tab*/
    private LinearLayout mMenuTabLayout;
    private Context mContext;
    /*阴影View*/
    private View mShadowView;
    private int mShadowColor = Color.parseColor("#88888888");

    /*菜单Adapter*/
    private BaseMenuAdapter mAdapter;
    private int mCurrentPosition = -1;

    public static int DURATION = 350;
    /*动画是否执行*/
    private boolean mAnimatorIsExecute;
    /*存储菜单的高度*/
    private List<Integer> mMenuHeights;
    /*内容布局*/
    private ViewGroup mContentView;
    /*菜单View集合*/
    private List<View> mMenuViews;
    private boolean mOpenAnimation;

    private MenuObserver mObserver;

    public ListDataScreenView(Context context) {
        this(context, null);
    }

    public ListDataScreenView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListDataScreenView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainAttrs(context, attrs);
        init(context);
        initLayout();
        if (!mOpenAnimation) {
            DURATION = 0;
        }
    }

    private void obtainAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ListDataScreenView);
        mOpenAnimation = ta.getBoolean(R.styleable.ListDataScreenView_openAnimation, false);
        ta.recycle();
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        mContext = context;
        mMenuHeights = new ArrayList<>();
        mMenuViews = new ArrayList<>();
    }

    /**
     * 实例化布局
     */
    private void initLayout() {
        // 1. 创建LinearLayout，用来存放Tab
        mMenuTabLayout = new LinearLayout(mContext);
        mMenuTabLayout.setLayoutParams(//
                new LayoutParams(LayoutParams.MATCH_PARENT, //
                        LayoutParams.WRAP_CONTENT));
        mMenuTabLayout.setBackgroundColor(Color.WHITE);
        addView(mMenuTabLayout);
        // 2. 添加阴影
        mShadowView = new View(mContext);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                .LayoutParams.MATCH_PARENT);
        mShadowView.setLayoutParams(params);
        mShadowView.setBackgroundColor(mShadowColor);
        mShadowView.setAlpha(0f);
        mShadowView.setVisibility(GONE);
        mShadowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
            }
        });
    }

    /**
     * 绑定内容布局
     *
     * @param parent
     */
    public void bindViewGroup(ViewGroup parent) {
        if (!(parent instanceof RelativeLayout) && !(parent instanceof FrameLayout)) {
            throw new RuntimeException("请绑定RelativeLayout或者FrameLayout");
        }
        mContentView = parent;
    }

    /**
     * 设置adapter
     *
     * @param adapter
     */
    public void setAdapter(BaseMenuAdapter adapter) {
        if (mContentView == null) {
            throw new RuntimeException("请绑定内容布局");
        }
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver();
        }
        mAdapter = adapter;
        mObserver= new AdapterMenuObserver();
        mAdapter.registerDataSetObserver(mObserver);
        mMenuHeights.clear();
        mMenuViews.clear();

        int count = mAdapter.getCount();
        mContentView.removeView(mShadowView);
        mContentView.addView(mShadowView);
        for (int i = 0; i < count; i++) {
            // 获取菜单 tab
            View tabView = mAdapter.getView(i, mMenuTabLayout);
            LayoutParams layoutParams = (LayoutParams) tabView.getLayoutParams();
            layoutParams.weight = 1;
            setTabClick(tabView, i);
            mMenuTabLayout.addView(tabView);
            // 获取菜单的内容
            final View menuView = mAdapter.getMenuView(i, mContentView);
            menuView.setClickable(true);
            mContentView.addView(menuView);
            mMenuViews.add(menuView);
            menuView.post(new Runnable() {
                @Override
                public void run() {
                    int height = menuView.getMeasuredHeight();
                    menuView.setTranslationY(-height);
                    mMenuHeights.add(height);
                }
            });
        }
    }

    /**
     * tabView的点击事件
     *
     * @param tabView
     * @param position
     */
    private void setTabClick(final View tabView, final int position) {
        tabView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关闭状态下 mCurrentPosition==-1
                if (mCurrentPosition == -1) { //打开
                    openMenu(tabView, position);
                } else { // mCurrentPosition!=-1,说明不是关闭状态下
                    if (mCurrentPosition == position) { // 如果重复点击同一个tab,关闭
                        closeMenu();
                    } else { // 否则，切换
                        if (mAnimatorIsExecute) return;
                        View currentMenu = mMenuViews.get(mCurrentPosition);
                        int height = mMenuHeights.get(mCurrentPosition);
                        currentMenu.setVisibility(View.GONE);//隐藏当前菜单View
                        currentMenu.setTranslationY(-height); // 收回
                        mAdapter.menuClose(mMenuTabLayout.getChildAt(mCurrentPosition));
                        mCurrentPosition = position; //mCurrentPosition重新赋值当前的position
                        currentMenu = mMenuViews.get(mCurrentPosition);
                        currentMenu.setVisibility(View.VISIBLE); // 显示tab对应的菜单View
                        currentMenu.setTranslationY(0); //展示
                        mAdapter.menuOpen(mMenuTabLayout.getChildAt(mCurrentPosition));
                    }
                }
            }
        });
    }

    /**
     * 打开菜单
     *
     * @param tabView
     * @param position
     */
    private void openMenu(final View tabView, final int position) {
        // 防止动画错乱，如果是动画状态下，则不会执行下面代码
        if (mAnimatorIsExecute) return;
        View menuView = mMenuViews.get(position);
        menuView.setVisibility(View.VISIBLE);
        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(menuView, "translationY", -mMenuHeights.get
                (position), 0);
        translationAnimator.setDuration(DURATION);
        translationAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimatorIsExecute = false;
                mCurrentPosition = position; // mCurrentPosition赋值当前打开的菜单的position
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mAnimatorIsExecute = true;
                // 当前tab传递到外面
                mAdapter.menuOpen(tabView);
            }
        });
        translationAnimator.start();
        // 控制透明度
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mShadowView, "alpha", 0f, 1f);
        alphaAnimator.setDuration(DURATION);
        mShadowView.setVisibility(VISIBLE);
        alphaAnimator.start();

    }

    /**
     * 关闭菜单
     */
    private void closeMenu() {
        // 经测试 乱点的情况下可能导致mCurrentPosition==-1
        if (mAnimatorIsExecute || mCurrentPosition == -1) return;
        final View menuView = mMenuViews.get(mCurrentPosition);
        // 1. 开启动画，位移动画， 透明度动画
        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(menuView, "translationY", 0, -menuView
                .getMeasuredHeight());
        translationAnimator.setDuration(DURATION);
        translationAnimator.start();
        // 控制透明度
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mShadowView, "alpha", 1f, 0f);
        alphaAnimator.setDuration(DURATION);
        alphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mShadowView.setVisibility(GONE);//隐藏遮盖层
                menuView.setVisibility(GONE); // 隐藏菜单View
                mAnimatorIsExecute = false;
                mCurrentPosition = -1; // mCurrentPosition赋值 -1,表明关闭状态

            }

            @Override
            public void onAnimationStart(Animator animation) {
                mAnimatorIsExecute = true;
                mAdapter.menuClose(mMenuTabLayout.getChildAt(mCurrentPosition));
            }
        });
        alphaAnimator.start();
    }

    /**
     * 具体的观察者
     */
    private class AdapterMenuObserver extends MenuObserver {

        @Override
        public void closeMenu() {
            ListDataScreenView.this.closeMenu();
        }
    }

}
