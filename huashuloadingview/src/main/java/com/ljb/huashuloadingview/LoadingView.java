package com.ljb.huashuloadingview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;



/**
 * Author      :ljb
 * Date        :2018/6/9
 * Description : 花束直播加载效果
 */
public class LoadingView extends RelativeLayout {

    private CircleView mLeftView, mMiddleView, mRightView;
    private int mDuration = 400;
    private float mTranslate = 100;
    private boolean mIsStop;
    private int mLeftColor = Color.RED;
    private int mMiddleColor = Color.GREEN;
    private int mRightColor = Color.BLUE;
    private int mRadius = 40;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.WHITE);
        obtainAttrs(context, attrs);
        initView(context);
        post(new Runnable() {
            @Override
            public void run() {
                executeAnimation();
            }
        });


    }

    private void obtainAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);
        mTranslate = ta.getDimension(R.styleable.LoadingView_translation, mTranslate);
        mLeftColor = ta.getColor(R.styleable.LoadingView_leftCircleColor, mLeftColor);
        mMiddleColor = ta.getColor(R.styleable.LoadingView_middleCircleColor, mMiddleColor);
        mRightColor = ta.getColor(R.styleable.LoadingView_rightCircleColor, mRightColor);
        mDuration = ta.getInt(R.styleable.LoadingView_animDuration, mDuration);
        mRadius = (int) ta.getDimension(R.styleable.LoadingView_circleRadius, mRadius);
        ta.recycle();
    }

    private void executeAnimation() {
        expandAnimation();
    }

    /**
     * 展开动画
     */
    private void expandAnimation() {
        if (mIsStop) return;
        // 左边的动画
        ObjectAnimator leftAnimator = ObjectAnimator.ofFloat(mLeftView, "translationX", 0, -mTranslate);
        // 右边的动画
        ObjectAnimator rightAnimator = ObjectAnimator.ofFloat(mRightView, "translationX", 0, mTranslate);

        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(new DecelerateInterpolator());
        set.playTogether(leftAnimator, rightAnimator);
        set.setDuration(mDuration);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                innerAnimation();
            }
        });
        set.start();
    }

    /**
     * 往内动画
     */
    private void innerAnimation() {
        if (mIsStop) return;
        // 左边的动画
        ObjectAnimator leftAnimator = ObjectAnimator.ofFloat(mLeftView, "translationX", -mTranslate, 0);
        // 右边的动画
        ObjectAnimator rightAnimator = ObjectAnimator.ofFloat(mRightView, "translationX", mTranslate, 0);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(leftAnimator, rightAnimator);
        set.setDuration(mDuration);
        set.setInterpolator(new AccelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                int leftColor = mLeftView.getColor();
                int rightColor = mRightView.getColor();
                int middleColor = mMiddleView.getColor();
                mMiddleView.exchangeColor(leftColor);
                mRightView.exchangeColor(middleColor);
                mLeftView.exchangeColor(rightColor);
                expandAnimation();
            }
        });
        set.start();
    }

    private void initView(Context context) {
        // 添加三个圆形
        mLeftView = getCircle(context);
        mLeftView.exchangeColor(mLeftColor);
        mMiddleView = getCircle(context);
        mMiddleView.exchangeColor(mMiddleColor);
        mRightView = getCircle(context);
        mRightView.exchangeColor(mRightColor);
        addView(mLeftView);
        addView(mRightView);
        addView(mMiddleView);
    }


    private CircleView getCircle(Context context) {
        CircleView circleView = new CircleView(context);
        LayoutParams params = new LayoutParams(mRadius, mRadius);

        params.addRule(CENTER_IN_PARENT);
        circleView.setLayoutParams(params);
        return circleView;
    }

    /**
     * 释放资源
     */
    public void release() {
        mLeftView.clearAnimation();
        mRightView.clearAnimation();
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
            removeAllViews();
        }
        mIsStop = true;
    }
}
