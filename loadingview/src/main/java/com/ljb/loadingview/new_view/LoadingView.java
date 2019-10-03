package com.ljb.loadingview.new_view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.ljb.loadingview.R;


/**
 * Author      :ljb
 * Date        :2019/10/3
 * Description :
 */
public class LoadingView extends LinearLayout {

    private static final long ANIMATOR_DURATION = 600;
    private int mMarginBottom = 80;

    private ShapeView mShapeView;
    private View mShadowView;
    private AnimatorSet mUpAnimatorSet;
    private AnimatorSet mFallAnimatorSet;
    private ObjectAnimator mRotationAnimator1;
    private ObjectAnimator mRotationAnimator2;

    private boolean mAnimatorIsEnd = false;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mMarginBottom = dp2px(context, mMarginBottom);

        // 添加布局
        inflate(context, R.layout.ui_loading_view_2, this);
        //初始化控件
        initView();
        // 开始动画
        post(new Runnable() {
            @Override
            public void run() {
                startFallAnimator();
            }
        });
    }


    private void startFallAnimator() {
        if (mAnimatorIsEnd) return;
        if (mFallAnimatorSet == null) {
            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(mShapeView, "translationY", 0, mMarginBottom);
            ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(mShadowView, "scaleX", 0.3f, 1.0f);

            mFallAnimatorSet = new AnimatorSet();
            mFallAnimatorSet.playTogether(translationAnimator, scaleAnimator);
            mFallAnimatorSet.setDuration(ANIMATOR_DURATION);
            mFallAnimatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            mFallAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mShapeView.exchange();
                    startUpAnimator();
                }

            });
        }
        mFallAnimatorSet.start();
    }

    private void startUpAnimator() {
        if (mAnimatorIsEnd) return;
        if (mUpAnimatorSet == null) {
            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(mShapeView, "translationY", mMarginBottom, 0);
            ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(mShadowView, "scaleX", 1.0f, 0.3f);

            mUpAnimatorSet = new AnimatorSet();
            mUpAnimatorSet.playTogether(translationAnimator, scaleAnimator);
            mUpAnimatorSet.setDuration(ANIMATOR_DURATION);
            mUpAnimatorSet.setInterpolator(new DecelerateInterpolator());
            mUpAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    startFallAnimator();
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    // 开始旋转
                    startRotation();
                }
            });
        }
        mUpAnimatorSet.start();
    }

    private void startRotation() {
        switch (mShapeView.getShape()) {
            case ShapeView.Shape.ROUND:
            case ShapeView.Shape.SQUARE:
                if (mRotationAnimator1 == null) {
                    mRotationAnimator1 = ObjectAnimator.ofFloat(mShapeView, "rotation", 0, 180);
                    mRotationAnimator1.setDuration(ANIMATOR_DURATION);
                }
                mRotationAnimator1.start();
                break;
            case ShapeView.Shape.TRIANGLE:
                if (mRotationAnimator2 == null) {
                    mRotationAnimator2 = ObjectAnimator.ofFloat(mShapeView, "rotation", 0, 120);
                    mRotationAnimator2.setDuration(ANIMATOR_DURATION);
                }
                mRotationAnimator2.start();
                break;
        }

    }

    private void initView() {
        mShapeView = findViewById(R.id.shape_loading_view);
        mShadowView = findViewById(R.id.shadow_view);
    }

    private int dp2px(Context context, int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE || visibility == INVISIBLE) {
            releaseAnimator();
        }
    }

    private void releaseAnimator() {
        mAnimatorIsEnd = true;
        endAnimator(mRotationAnimator1);
        endAnimator(mRotationAnimator2);
        endAnimator(mFallAnimatorSet);
        endAnimator(mUpAnimatorSet);
    }

    private void endAnimator(AnimatorSet animatorSet) {
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.end();
        }
    }

    private void endAnimator(ObjectAnimator animator) {
        if (animator != null && animator.isRunning()) {
            animator.end();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releaseAnimator();
    }
}
