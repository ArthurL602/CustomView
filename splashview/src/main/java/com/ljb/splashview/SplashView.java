package com.ljb.splashview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;



/**
 * Author      :ljb
 * Date        :2018/6/10
 * Description :
 */
public class SplashView extends View {
    /*当前大圆的角度*/
    private float mCurrentRotationAngle;

    private int ROTATION_ANIMATION_TIME = 1400;
    // 小圆的颜色列表
    private int[] mCircleColors;
    // 小圆半径
    private int mCircleRadius;
    /* 大圆半径*/
    private int mRotationRadius;

    private Paint mPaint;
    private int mCenterX, mCenterY;

    /*当前状态所画动画*/
    private SplashState mSplashState;

    private double mMaxRadius;
    private int mSplashColor = Color.WHITE;

    public SplashView(Context context) {
        this(context, null);
    }

    public SplashView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SplashView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCircleColors = context.getResources().getIntArray(R.array.splash_circle_colors);
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRotationRadius = getMeasuredWidth() / 4;
        mCircleRadius = mRotationRadius / 8;
        mCenterX = getWidth() / 2;
        mCenterY = getHeight() / 2;
        mMaxRadius = Math.sqrt(Math.pow(mCenterX, 2) + Math.pow(mCenterY, 2));
    }

    private void release() {
        ViewGroup parent = (ViewGroup) getParent();
        parent.removeView(this);
        parent.removeAllViews();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (mSplashState == null) { // 旋转动画
            mSplashState = new RotationState();
        }
        mSplashState.draw(canvas);
    }

    /**
     * 绘制圆形
     *
     * @param canvas        画布
     * @param radius        大圆半径
     * @param rotationAngle 大圆旋转角度
     */
    private void drawCircle(Canvas canvas, int radius, float rotationAngle) {
        // 画一个背景 白色
        canvas.drawColor(mSplashColor);
        // 绘制多个圆
        //  每份角度
        double percentAngle = Math.PI * 2 / mCircleColors.length;
        for (int i = 0; i < mCircleColors.length; i++) {
            mPaint.setColor(mCircleColors[i]);
            // 当前角度  = 每份角度 * i + 大圆旋转角度
            double currAngle = i * percentAngle + rotationAngle;
            int cx = (int) (radius * Math.cos(currAngle)) + mCenterX;
            int cy = (int) (radius * Math.sin(currAngle)) + mCenterY;
            canvas.drawCircle(cx, cy, mCircleRadius, mPaint);
        }
    }

    /**
     * 消失
     */
    public void disapper() {
        // 开始聚合动画
        // 取消动画
        if (mSplashState instanceof RotationState) {
            ((RotationState) mSplashState).cancel();
        }
        mSplashState = new MergeState();
    }

    public abstract class SplashState {
        public abstract void draw(Canvas canvas);
    }


    /**
     * 旋转动画
     */
    public class RotationState extends SplashState {
        private ValueAnimator mAnimator;

        public RotationState() {
            mAnimator = ValueAnimator.ofFloat(0f, (float) (2 * Math.PI));
            mAnimator.setDuration(ROTATION_ANIMATION_TIME);
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCurrentRotationAngle = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            // 不断反复执行
            mAnimator.setRepeatCount(-1);
            mAnimator.start();
        }

        @Override
        public void draw(Canvas canvas) {
            drawCircle(canvas, mRotationRadius, mCurrentRotationAngle);
        }

        public void cancel() {
            mAnimator.cancel();
        }
    }



    /**
     * 聚合动画
     */
    public class MergeState extends SplashState {

        private float mRotationRadius;
        private final ValueAnimator mAnimator;

        public MergeState() {
            mAnimator = ValueAnimator.ofFloat(SplashView.this.mRotationRadius, 0);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mRotationRadius = (float) animation.getAnimatedValue(); // 最大半径 --> 0
                    invalidate();
                }
            });
            mAnimator.setInterpolator(new AnticipateInterpolator(6f));
            mAnimator.setDuration(ROTATION_ANIMATION_TIME / 2);
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    cancel();
                    mSplashState = new ExpandState();
                }
            });
            mAnimator.start();
        }

        @Override
        public void draw(Canvas canvas) {
            drawCircle(canvas, (int) mRotationRadius, mCurrentRotationAngle);
        }

        public void cancel() {
            mAnimator.cancel();
        }
    }

    /**
     * 展开动画
     */
    public class ExpandState extends SplashState {

        private float mHoleRadius;
        private final Paint mPaint;
        private final ValueAnimator mAnimator;

        public ExpandState() {
            mPaint = new Paint();
            mPaint.setColor(Color.RED);
            mAnimator = ValueAnimator.ofFloat(0f, (float) mMaxRadius);
            mAnimator.setDuration(ROTATION_ANIMATION_TIME / 2);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mHoleRadius = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mAnimator.cancel();
                    release();
                }
            });
            mAnimator.start();
        }

        @Override
        public void draw(Canvas canvas) {
            int strokeWidth = (int) (mMaxRadius - mHoleRadius);
            mPaint.setColor(mSplashColor);
            mPaint.setStrokeWidth(strokeWidth);
            mPaint.setStyle(Paint.Style.STROKE);
            int radius = (int) (strokeWidth / 2 + mHoleRadius);
            canvas.drawCircle(mCenterX, mCenterY, radius, mPaint);
        }
    }
}
