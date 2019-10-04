package com.ljb.huashuloadingview.new_view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import com.ljb.huashuloadingview.R;


/**
 * Author      :ljb
 * Date        :2019/10/4
 * Description : 加载自定义View
 */
public class LoadingView2 extends RelativeLayout {

    private static final long ANIMATOR_DURATION = 350;
    private int mCircleRadius = 20;
    private CircleView mLeftView;
    private CircleView mRightView;
    private CircleView mMiddleView;

    private int mTranslationDistance = 80;
    private ObjectAnimator mLeftTranslationAnimator;
    private ObjectAnimator mRightTranslationAnimator;
    private AnimatorSet mAnimatorSet;

    private int mLeftColor = Color.BLUE;
    private int mMiddleColor = Color.RED;
    private int mRightColor = Color.GREEN;


    public LoadingView2(Context context) {
        this(context, null);
    }

    public LoadingView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mCircleRadius = dp2px(mCircleRadius);
        mTranslationDistance = dp2px(mTranslationDistance);
        initAttrs(context, attrs);

        setBackgroundColor(Color.WHITE);
        initCircle(context);
        post(new Runnable() {
            @Override
            public void run() {
                startAnimator();
            }
        });
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingView2);
        if (typedArray != null) {
            mLeftColor = typedArray.getColor(R.styleable.LoadingView2_leftColor, mLeftColor);
            mMiddleColor = typedArray.getColor(R.styleable.LoadingView2_middleColor, mMiddleColor);
            mRightColor = typedArray.getColor(R.styleable.LoadingView2_rightColor, mRightColor);
            mTranslationDistance = typedArray.getDimensionPixelSize(R.styleable.LoadingView2_translationDistance, mTranslationDistance);
            mCircleRadius = typedArray.getDimensionPixelSize(R.styleable.LoadingView2_circleRadius, mCircleRadius);

            typedArray.recycle();
        }
    }

    private void startAnimator() {
        mLeftTranslationAnimator = ObjectAnimator.ofFloat(mLeftView, "translationX", 0, -mTranslationDistance);
        mRightTranslationAnimator = ObjectAnimator.ofFloat(mRightView, "translationX", 0, mTranslationDistance);
        mLeftTranslationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        mLeftTranslationAnimator.setRepeatMode(ObjectAnimator.REVERSE);

        mRightTranslationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        mRightTranslationAnimator.setRepeatMode(ObjectAnimator.REVERSE);

        mRightTranslationAnimator.addListener(new AnimatorListenerAdapter() {
            int count = 0;

            @Override
            public void onAnimationRepeat(Animator animation) {
                count++;
                if (count == 2) {
                    count = 0;
                    int leftColor = mLeftView.getColor();
                    int middleColor = mMiddleView.getColor();
                    int rightColor = mRightView.getColor();

                    mLeftView.exchangeColor(rightColor);
                    mMiddleView.exchangeColor(leftColor);
                    mRightView.exchangeColor(middleColor);
                }
            }
        });
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(mLeftTranslationAnimator, mRightTranslationAnimator);
        mAnimatorSet.setDuration(ANIMATOR_DURATION);
        mAnimatorSet.setInterpolator(new DecelerateInterpolator());

        mAnimatorSet.start();

    }

    public void releaseAnimators() {
        releaseAnimator(mLeftTranslationAnimator);
        releaseAnimator(mRightTranslationAnimator);
        releaseAnimatorSet(mAnimatorSet);
    }

    private void releaseAnimatorSet(AnimatorSet animatorSet) {
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.end();
        }
    }

    private void releaseAnimator(ObjectAnimator animator) {
        if (animator != null && animator.isRunning()) {
            animator.end();
        }
    }


    private void initCircle(Context context) {
        mLeftView = getCircleView(context);
        mRightView = getCircleView(context);
        mMiddleView = getCircleView(context);
        addView(mLeftView);
        addView(mRightView);
        addView(mMiddleView);
        mLeftView.exchangeColor(Color.BLUE);
        mRightView.exchangeColor(Color.RED);
        mMiddleView.exchangeColor(Color.GREEN);
    }

    private CircleView getCircleView(Context context) {
        CircleView circleView = new CircleView(context);
        LayoutParams params = new LayoutParams(dp2px(mCircleRadius), dp2px(mCircleRadius));
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        circleView.setLayoutParams(params);
        return circleView;
    }

    private int dp2px(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getContext().getResources().getDisplayMetrics());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releaseAnimators();
    }

}
