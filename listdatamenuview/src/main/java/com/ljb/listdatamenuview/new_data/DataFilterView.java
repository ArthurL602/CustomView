package com.ljb.listdatamenuview.new_data;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author      :ljb
 * Date        :2019/10/3
 * Description : 数据筛选View
 */
public class DataFilterView extends LinearLayout {

    private static final long ANIMATOR_DURATION = 350;
    private LinearLayout mTabContainer;
    private FrameLayout mContentContainer;
    private FrameLayout mMenuContainer;
    private int mShadowColor = 0x88888888;
    private View mShadowView;
    private BaseDataFilterAdapter mAdapter;


    private int mCurrentPosition = -1;

    private boolean mIsAnimatorRunning = false;

    private Map<String, ObjectAnimator> mAnimatorMap;

    private AppDataMenuObserver mObserver;

    public DataFilterView(Context context) {
        this(context, null);
    }

    public DataFilterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataFilterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);
        // 初始化Tab
        initTabView(context);
        // 初始化 数据筛选Menu
        initMenuView(context);
        // 初始化点击事件
        initEvent();
    }

    /**
     * 初始化Tab
     */
    private void initTabView(Context context) {
        mTabContainer = new LinearLayout(context);
        LayoutParams tabParams = new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mTabContainer.setLayoutParams(tabParams);
        addView(mTabContainer);
    }

    /**
     * 初始化Menu
     */
    private void initMenuView(Context context) {
        mContentContainer = new FrameLayout(context);
        LayoutParams menuParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mContentContainer.setLayoutParams(menuParams);
        addView(mContentContainer);
        // 初始化遮盖层
        mShadowView = new View(context);
        mShadowView.setBackgroundColor(mShadowColor);
        mShadowView.setVisibility(GONE);
        mContentContainer.addView(mShadowView);

        mMenuContainer = new FrameLayout(context);
        mContentContainer.addView(mMenuContainer);
    }

    private void initEvent() {
        mShadowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
            }
        });
    }

    public void setAdapter(BaseDataFilterAdapter adapter) {
        if (adapter == null) {
            throw new IllegalStateException("adapter not null");
        }
        if (mAdapter != null && mObserver != null) {
            mAdapter.unRegisterObserver(mObserver);
        }
        mObserver = new AppDataMenuObserver();
        mAdapter = adapter;
        mAdapter.registerObserever(mObserver);
        mAnimatorMap = new ConcurrentHashMap<>(mAdapter.getCount());
        // 添加tab
        addTabView();
        // 添加Menu
        addMenu();
    }


    private void handleTabClick(int position, View tabView) {
        if (mCurrentPosition == -1) {
            // openMenu
            openMenu(position, tabView);
        } else {
            if (position == mCurrentPosition) {
                // 点击同一个tab，则关闭tab
                closeMenu();
            } else {
                handleSpecialOpenMenu(position);
            }
        }
    }

    /**
     * 处理特别的打开菜单的情况，即菜单打开，但是点击其他position
     */
    private void handleSpecialOpenMenu(int position) {
        if (mIsAnimatorRunning) return;
        // 隐藏当前的MenuView
        mIsAnimatorRunning = true;
        View menuView = mMenuContainer.getChildAt(mCurrentPosition);
        mAdapter.closeMenu(mTabContainer.getChildAt(mCurrentPosition));
        int menuHeight = menuView.getHeight();
        menuView.setTranslationY(-menuHeight);
        menuView.setVisibility(GONE);
        // show 当前点击的tab对应的menu
        mCurrentPosition = position;
        menuView = mMenuContainer.getChildAt(mCurrentPosition);
        mAdapter.openMenu(mTabContainer.getChildAt(mCurrentPosition));
        menuView.setVisibility(VISIBLE);
        menuView.setTranslationY(0);
        mIsAnimatorRunning = false;
    }

    private void closeMenu() {
        if (mIsAnimatorRunning) return;
        final View menuView = mMenuContainer.getChildAt(mCurrentPosition);
        if (menuView == null) return;
        int height = menuView.getHeight();
        ObjectAnimator translationAnimator = mAnimatorMap.get(menuView.hashCode() + "close");
        ObjectAnimator alphaAnimator = mAnimatorMap.get(mShadowView.hashCode() + "close");
        if (translationAnimator == null) {
            translationAnimator = ObjectAnimator.ofFloat(menuView, "translationY", 0, -height);
            translationAnimator.setDuration(ANIMATOR_DURATION);
            translationAnimator.setInterpolator(new LinearInterpolator());
        }
        if (alphaAnimator == null) {
            alphaAnimator = ObjectAnimator.ofFloat(mShadowView, "alpha", 1f, 0f);
            alphaAnimator.setDuration(ANIMATOR_DURATION);
            alphaAnimator.setInterpolator(new LinearInterpolator());
        }
        translationAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                hiddenView(menuView);
                mCurrentPosition = -1;
                mIsAnimatorRunning = false;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mAdapter.closeMenu(mTabContainer.getChildAt(mCurrentPosition));
                mIsAnimatorRunning = true;
            }
        });
        translationAnimator.start();

    }


    private void openMenu(final int position, final View tabView) {
        if (mIsAnimatorRunning) return;
        final View menuView = mMenuContainer.getChildAt(position);
        if (menuView == null) return;

        int height = menuView.getHeight();
        ObjectAnimator translationAnimator = mAnimatorMap.get(menuView.hashCode() + "open");
        ObjectAnimator alphaAnimator = mAnimatorMap.get(mShadowView.hashCode() + "open");
        if (translationAnimator == null) {
            translationAnimator = ObjectAnimator.ofFloat(menuView, "translationY", -height, 0);
            translationAnimator.setDuration(ANIMATOR_DURATION);
            translationAnimator.setInterpolator(new LinearInterpolator());
            mAnimatorMap.put(menuView.hashCode() + "open", translationAnimator);
        }
        if (alphaAnimator == null) {
            alphaAnimator = ObjectAnimator.ofFloat(mShadowView, "alpha", 0f, 1f);
            alphaAnimator.setDuration(ANIMATOR_DURATION);
            alphaAnimator.setInterpolator(new LinearInterpolator());
            mAnimatorMap.put(mShadowView.hashCode() + "open", translationAnimator);
        }
        translationAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentPosition = position;
                mIsAnimatorRunning = false;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mAdapter.openMenu(tabView);
                showView(menuView);
                mIsAnimatorRunning = true;
            }
        });
        translationAnimator.start();
    }

    private void showView(View menuView) {
        if (menuView.getVisibility() != View.VISIBLE) {
            menuView.setVisibility(VISIBLE);
        }
        if (mShadowView.getVisibility() != View.VISIBLE) {
            mShadowView.setVisibility(VISIBLE);
        }
    }

    private void hiddenView(View menuView) {
        if (menuView.getVisibility() != View.GONE) {
            menuView.setVisibility(GONE);
        }
        if (mShadowView.getVisibility() != View.GONE) {
            mShadowView.setVisibility(GONE);
        }
    }

    /**
     * 添加Tab
     */
    private void addTabView() {
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View tabView = mAdapter.getTabView(i, mTabContainer);
            final int position = i;
            mTabContainer.addView(tabView);
            tabView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleTabClick(position, v);
                }
            });
        }
    }

    /**
     * 添加Menu
     */
    private void addMenu() {
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View menuView = mAdapter.getMenuView(i, mMenuContainer);
            menuView.setClickable(true);
            mMenuContainer.addView(menuView);
            // 处理menu 的高度
            handleMenuHeight(menuView);
        }
    }

    private void handleMenuHeight(final View menuView) {
        post(new Runnable() {
            @Override
            public void run() {
                int menuHeight = menuView.getHeight();
                menuView.setTranslationY(-menuHeight);
                menuView.setVisibility(GONE);
            }
        });
    }


    private class AppDataMenuObserver extends MenuObserver {
        @Override
        public void closeMenu() {
            DataFilterView.this.closeMenu();
        }
    }

}
