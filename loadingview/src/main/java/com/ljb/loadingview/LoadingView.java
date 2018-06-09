package com.ljb.loadingview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

/**
 * Author      :ljb
 * Date        :2018/2/11
 * Description :自定义加载动画
 */

public class LoadingView extends LinearLayout {
    //多图形自定义View
    private ShapeView mShapeView;
    //阴影
    private View mViewBg;
    private int mTranslationDistance;
    private int mDuration = 450;
    private int mAngle = 180;
    private ObjectAnimator mTranslationUp;
    private ObjectAnimator mScaleMax;
    private ObjectAnimator mRotation;
    private ObjectAnimator mTranslationDown;
    private ObjectAnimator mScaleMin;
    //是否停止动画
    private boolean mIsStop = false;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout();
    }

    private void initLayout() {
        mTranslationDistance = (int) dp2px(80);
        View view = inflate(getContext(), R.layout.ui_loading_view, this);
        mShapeView = view.findViewById(R.id.shape_view);
        mViewBg = view.findViewById(R.id.view);

        post(new Runnable() {
            @Override
            public void run() {
                //在onResume()之后调用
                startFallAnimator();
            }
        });

    }

    /**
     * 下落动画
     */
    private void startFallAnimator() {
        if (mIsStop) {
            return;
        }
        //多图形平移动画
        if (mTranslationDown == null) {
            mTranslationDown = ObjectAnimator.ofFloat(mShapeView, "translationY", 0, mTranslationDistance);
        }
        //阴影的动画
        if (mScaleMin == null) {
            mScaleMin = ObjectAnimator.ofFloat(mViewBg, "scaleX", 1.0f, 0.2f);
        }
        AnimatorSet set = new AnimatorSet();
        set.play(mTranslationDown).with(mScaleMin);
        set.setInterpolator(new AccelerateInterpolator());
        set.setDuration(mDuration).start();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mShapeView.exchange();
                startUpAnimator();
            }
        });
    }

    /**
     * 上抛动画
     */
    private void startUpAnimator() {
        if (mIsStop) return;
        //多图形平移动画
        if (mTranslationUp == null) {
            mTranslationUp = ObjectAnimator.ofFloat(mShapeView, "translationY", mTranslationDistance, 0);
        }
        //阴影的动画
        if (mScaleMax == null) {
            mScaleMax = ObjectAnimator.ofFloat(mViewBg, "scaleX", 0.2f, 1.0f);
        }
        if (mShapeView.getCurrentShape() == ShapeView.Shape.Square) {
            mAngle = 180;
        } else if (mShapeView.getCurrentShape() == ShapeView.Shape.Triangle) {
            mAngle = 180;
        }
        // 多图形旋转
        if (mRotation == null) {
            mRotation = ObjectAnimator.ofFloat(mShapeView, "rotation", -mAngle, 0);
        }
        AnimatorSet set = new AnimatorSet();
        set.play(mTranslationUp).with(mScaleMax).with(mRotation);
        set.setInterpolator(new DecelerateInterpolator());
        set.setDuration(mDuration).start();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startFallAnimator();
            }
        });
    }

    private float dp2px(int value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    /**
     * 释放资源
     */
    public void release() {
        //清理动画
        mShapeView.clearAnimation();
        mViewBg.clearAnimation();
        //把loadingView从父布局移除
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            //从父布局移除
            parent.removeView(this);
            //移除自己的子View
            removeAllViews();
        }
        mIsStop = true;
    }
}
