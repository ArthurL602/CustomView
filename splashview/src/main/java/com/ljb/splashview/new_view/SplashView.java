package com.ljb.splashview.new_view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;

import com.ljb.splashview.R;


/**
 * Author      :ljb
 * Date        :2019/10/5
 * Description :
 */
public class SplashView extends View {


    private final long ROTATION_ANIMATOR_DURATION = 1400;
    private int mWidth, mHeight, mCenterX, mCenterY;

    //旋转圆半径
    private int mRotationRadius;
    private int mFianalRotationRadius;
    // 小圆半径
    private int mCircleRadius;

    private int[] mCircleColors;

    private Paint mPaint;

    private SplashDrawState mSplashDrawState;
    private float mRotationAngle = 0;

    private float mDiagonal = 0;//对角线

    public SplashView(Context context) {
        this(context, null);
    }

    public SplashView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SplashView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        init(context, attrs);
        post(new Runnable() {
            @Override
            public void run() {
            }
        });
        setLayerType(LAYER_TYPE_SOFTWARE,null);
    }

    private void init(Context context, AttributeSet attrs) {

        mCircleColors = getResources().getIntArray(R.array.splash_circle_colors);
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mWidth = getWidth();
            mHeight = getHeight();
            mRotationRadius = mWidth / 2 / 2;
            mCircleRadius = mRotationRadius / 10;

            mCenterX = mWidth / 2;
            mCenterY = mHeight / 2;

            mFianalRotationRadius = mRotationRadius;

            mDiagonal = (float) Math.sqrt(mWidth * mWidth + mHeight * mHeight) / 2;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mSplashDrawState == null) {
            mSplashDrawState = new RotationState();
        }
        mSplashDrawState.onDraw(canvas);
    }

    private void drawCircles(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        int rotate = 360 / mCircleColors.length;
        for (int i = 0; i < mCircleColors.length; i++) {
            canvas.save();
            canvas.rotate(rotate * i + mRotationAngle, getWidth() / 2, getHeight() / 2);
            int cX = mCenterX;
            int cy = mCenterY + mRotationRadius;
            mPaint.setColor(mCircleColors[i]);
            canvas.drawCircle(cX, cy, mCircleRadius, mPaint);
            canvas.restore();
        }
    }

    public void end() {
        if (mSplashDrawState instanceof RotationState) {
            RotationState rotationState = (RotationState) mSplashDrawState;
            rotationState.endAnimator();
        }
        mSplashDrawState = new GatherState();
    }

    /**
     * 旋转动画
     */
    public class RotationState extends SplashDrawState {


        private final ValueAnimator mAnimator;

        public RotationState() {
            mAnimator = ValueAnimator.ofFloat(0, 360);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mRotationAngle = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.setDuration(ROTATION_ANIMATOR_DURATION);
            mAnimator.setRepeatCount(-1);
            mAnimator.start();

        }

        @Override
        public void onDraw(Canvas canvas) {
            drawCircles(canvas);
        }


        public void endAnimator() {
            if (mAnimator != null && mAnimator.isRunning()) {
                mAnimator.cancel();
            }
        }
    }

    /**
     * 聚集动画
     */
    public class GatherState extends SplashDrawState {

        private final ValueAnimator mAnimator;

        public GatherState() {
            mAnimator = ValueAnimator.ofInt(mFianalRotationRadius, 0);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mRotationRadius = (int) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mAnimator.setInterpolator(new AnticipateInterpolator(3f));
            mAnimator.setDuration(ROTATION_ANIMATOR_DURATION / 2);
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSplashDrawState = new SpreadState();
                }
            });
            mAnimator.start();

        }

        @Override
        public void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);
            drawCircles(canvas);
        }
    }

    /**
     * 扩散动画
     */
    public class SpreadState extends SplashDrawState {
        private final ValueAnimator mAnimator;
        private float mSpreadRadius = 0;

        public SpreadState() {

            mAnimator = ValueAnimator.ofFloat(0, mDiagonal);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mSpreadRadius = (Float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.setDuration(ROTATION_ANIMATOR_DURATION / 2);
            mAnimator.start();
        }

        @Override
        public void onDraw(Canvas canvas) {
            float strokeWidth =mDiagonal - mSpreadRadius;
            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(strokeWidth);
            float radius = mSpreadRadius + strokeWidth / 2;
            canvas.drawCircle(mCenterX,mCenterY,radius,mPaint);
//            drawFun2(canvas);
        }

        private void drawFun2(Canvas canvas) {
            canvas.save();
            canvas.drawColor(Color.WHITE);
            mPaint.setColor(Color.TRANSPARENT);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawCircle(mCenterX, mCenterY, mSpreadRadius, mPaint);
            mPaint.setXfermode(null);
            canvas.restore();
        }
    }

    public abstract class SplashDrawState {
        public abstract void onDraw(Canvas canvas);
    }
}
