package com.ljb.lovelayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Random;

/**
 * Author      :ljb
 * Date        :2019/10/4
 * Description :
 */
public class LoveLayout extends RelativeLayout {

    private static final int INIT_ANIMATOR_DURATION = 200;
    private static final long BEZIER_ANIMATOR_DURATION = 3000;
    private Random mRandom;

    private int[] mImgResIds = {
            R.drawable.pl_blue,
            R.drawable.pl_red,
            R.drawable.pl_yellow
    };

    private int mWidth, mHeight, mDrawableWidth, mDrawableHeight;

    private Interpolator[] mInterpolators;


    public LoveLayout(Context context) {
        this(context, null);
    }

    public LoveLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoveLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mRandom = new Random();
        Drawable loveDrawable = ContextCompat.getDrawable(context, mImgResIds[0]);
        mDrawableWidth = loveDrawable.getIntrinsicWidth();
        mDrawableHeight = loveDrawable.getIntrinsicHeight();

        mInterpolators = new Interpolator[]{
                new AccelerateDecelerateInterpolator(),
                new DecelerateInterpolator(),
                new AccelerateInterpolator(),
                new LinearInterpolator()
        };
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            mWidth = getWidth();
            mHeight = getHeight();
        }
    }

    public void addLove() {
        ImageView loveIv = new ImageView(getContext());
        // 添加love 控件
        addLoveView(loveIv);
        // love 出现时的动画
        executeInitAnimator(loveIv);
    }

    /**
     * 执行初始动画
     */
    private void executeInitAnimator(final ImageView loveIv) {
        AnimatorSet immediatelyAnimator = new AnimatorSet();
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(loveIv, "scaleX", 0.3f, 1f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(loveIv, "scaleY", 0.3f, 1f);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(loveIv, "alpha", 0.3f, 1f);
        immediatelyAnimator.playTogether(scaleXAnimator, alphaAnimator, scaleYAnimator);
        immediatelyAnimator.setDuration(INIT_ANIMATOR_DURATION);
        immediatelyAnimator.setInterpolator(new LinearInterpolator());
        immediatelyAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startSportAnimator(loveIv);
            }
        });
        immediatelyAnimator.start();

    }

    /**
     * 开始动的动画
     */
    private void startSportAnimator(final ImageView loveIv) {
        // 这里love控件移动路径是一个三阶贝塞尔曲线
        PointF point0 = new PointF(mWidth / 2 - mDrawableWidth / 2, mHeight - mDrawableHeight);
        PointF point1 = new PointF(mRandom.nextInt(mWidth - mDrawableWidth), mRandom.nextInt(mHeight / 2));
        PointF point2 = new PointF(mRandom.nextInt(mWidth - mDrawableWidth), mRandom.nextInt(mHeight / 2) + mHeight / 2);
        PointF point3 = new PointF(mRandom.nextInt(mWidth - mDrawableWidth), 0);
        BezierTypeEvaluator evaluator = new BezierTypeEvaluator(point1, point2);
        ValueAnimator bezierAnimator = ObjectAnimator.ofObject(evaluator, point0, point3);
        bezierAnimator.setInterpolator(mInterpolators[mRandom.nextInt(mInterpolators.length - 1)]);
        bezierAnimator.setDuration(BEZIER_ANIMATOR_DURATION);
        bezierAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF value = (PointF) animation.getAnimatedValue();
                float fraction = animation.getAnimatedFraction();
                loveIv.setX(value.x);
                loveIv.setY(value.y);
                loveIv.setAlpha(0.3f + 0.7f * (1 - fraction));
            }
        });
        bezierAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeView(loveIv);
            }
        });
        bezierAnimator.start();
    }

    /**
     * 添加Love控件
     */
    private void addLoveView(ImageView loveIv) {
        LayoutParams loveParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loveParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        loveParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        loveIv.setLayoutParams(loveParams);
        int resId = mImgResIds[mRandom.nextInt(mImgResIds.length - 1)];
        loveIv.setBackgroundResource(resId);
        addView(loveIv);
    }
}
