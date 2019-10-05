package com.ljb.mylibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Author      :ljb
 * Date        :2019/10/5
 * Description :
 */
public class RedPackageView extends View {

    private Bitmap mRedPackageBit;
    private Bitmap mProgressBgBit;
    private float mProgressLeft;
    private float mProgressTop;
    private Paint mPaint;
    private Paint mBobblePaint;


    private float mMaxProgress ;
    private float mCurrentProgress;
    private ValueAnimator mProgressAnimator;
    // 颜色渐变
    private int mProgressStarColor = Color.parseColor("#FDA501");
    private int mProgressEndColor = Color.parseColor("#FFEF74");

    private int mBobbleNums = 8;

    private float mBobbleRadius;

    private Bitmap mBobbleBit1;
    private Bitmap mBobbleBit2;

    private Bitmap [] mBobbleBit;
    private float mCenterX;
    private float mCenterY;

    private float mCurrentBobbleProgress = -1;
    private ValueAnimator mExpandAnimator;

    public void setMaxProgress(int maxProgress) {
        mMaxProgress = maxProgress;
    }

    private  void setCurrentProgress(float currentProgress) {
        mCurrentProgress = currentProgress;
        invalidate();
    }

    public RedPackageView(Context context) {
        this(context,null);
    }

    public RedPackageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RedPackageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mRedPackageBit = BitmapFactory.decodeResource(getResources(), R.drawable.icon_game_red_package_normal);
        mProgressBgBit = BitmapFactory.decodeResource(getResources(),R.drawable.icon_game_red_package_pb_bg);

        mBobbleBit1 = BitmapFactory.decodeResource(getResources(),R.drawable.icon_red_package_bomb_1);
        mBobbleBit2 = BitmapFactory.decodeResource(getResources(),R.drawable.icon_red_package_bomb_2);
        mBobbleBit = new Bitmap[]{mBobbleBit1,mBobbleBit2};

        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);

        mBobblePaint = new Paint();
        mBobblePaint.setDither(true);
        mBobblePaint.setAntiAlias(true);


        mBobbleRadius = mProgressBgBit.getHeight() * 0.7f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = (int) (mRedPackageBit.getWidth() * 1.1f);
        setMeasuredDimension(size,size);
    }


    public void startAnimator(float from ,float to){
        if(mProgressAnimator == null){
            mProgressAnimator = ValueAnimator.ofFloat(from,to);
            mProgressAnimator.setDuration(600);
            mProgressAnimator.setInterpolator(new AccelerateInterpolator());
            mProgressAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // 开始扩展动画
                    executeExpandAnimator();
                }
            });
            mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    setCurrentProgress(value);
                }
            });
        }
        if(!mProgressAnimator.isRunning()){
            mProgressAnimator.start();
        }
    }

    /**
     * 执行扩展动画
     */
    private void executeExpandAnimator() {
        if(mExpandAnimator == null){
            mExpandAnimator = ValueAnimator.ofFloat(0f,1f);
            mExpandAnimator.setDuration(480);
            mExpandAnimator.setRepeatCount(2);
            mExpandAnimator.setInterpolator(new DecelerateInterpolator());
            mExpandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    mCurrentBobbleProgress = value;
                    invalidate();
                }
            });
            mExpandAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCurrentBobbleProgress=-1;
                    //开始缩放
                    executeScaleAnimator();
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    mCurrentBobbleProgress=-1;
                }
            });
        }
        if(!mExpandAnimator .isRunning()){
            mExpandAnimator.start();
        }
    }

    private void executeScaleAnimator() {
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(this,"scaleX",1.0f,0.8F,1.0F,1.0f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(this,"scaleY",1.0f,0.8F,1.0F,1.0f);
        AnimatorSet animatorSet = new AnimatorSet();
        scaleXAnimator.setRepeatCount(2);
        scaleYAnimator.setRepeatCount(2);
        animatorSet.setDuration(600);
        animatorSet.playTogether(scaleXAnimator,scaleYAnimator);
        animatorSet.start();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制红包
        drawRedPackage(canvas);
        // 绘制红包下面的进度
        drawProgress(canvas);
        // 绘制second progress
        drawSecondProgress(canvas);
        // 绘制爆炸效果
        drawBobbleEffect(canvas);
    }

    private void drawBobbleEffect(Canvas canvas) {
        if(mCurrentBobbleProgress<0) return;
        float perAngle = (float) (2 * Math.PI / mBobbleNums * 1.0f);
        mBobblePaint.setAlpha((int) (300 - 255 *mCurrentBobbleProgress));
        for (int i = 0; i < mBobbleNums; i++) {
            Bitmap currentBit= mBobbleBit[i%2];
            float  angle = perAngle * i;
            float left = (float) (mCenterX + mBobbleRadius*mCurrentBobbleProgress * Math.cos(angle));
            float top = (float) (mCenterY - mBobbleRadius*mCurrentBobbleProgress * Math.sin(angle));
            canvas.drawBitmap(currentBit,left,top,mBobblePaint);
        }
    }

    private void drawSecondProgress(Canvas canvas) {
        if(mMaxProgress == 0 || mCurrentProgress ==0) return;
        float percent = mCurrentProgress / mMaxProgress ;
        float left = mProgressLeft +mProgressBgBit.getHeight() * 0.32f;
        float top = mProgressTop + mProgressBgBit.getHeight() * 0.36f;
        float right =  left + percent * mProgressBgBit.getWidth() * 0.77f;
        float bottom = top + mProgressBgBit.getHeight() * 0.29f;
        LinearGradient gradient = getGradient(right);
        mPaint.setShader(gradient);
        RectF rectF = new RectF(left,top,right,bottom);
        float roundCorner=mProgressBgBit.getHeight() * 0.2f;
        canvas.drawRoundRect(rectF,roundCorner,roundCorner,mPaint);
        mCenterX = right - mBobbleBit1.getWidth()/2;
        mCenterY = top  - (bottom - top)/1.5f ;
    }

    private LinearGradient getGradient(float end) {
        LinearGradient  gradient = new LinearGradient(0,
                    0,
                    end,
                    0,
                    new int [] {mProgressStarColor,mProgressEndColor},
                    new float [] {0f,1f},
                    Shader.TileMode.CLAMP);
        return gradient;
    }

    private void drawProgress(Canvas canvas) {
        mProgressLeft = (mRedPackageBit.getWidth() - mProgressBgBit.getWidth()) / 2 * 1.6f;
        mProgressTop = mRedPackageBit.getHeight() * 0.7f;
        canvas.drawBitmap(mProgressBgBit, mProgressLeft, mProgressTop,null);
    }

    private void drawRedPackage(Canvas canvas) {
        canvas.drawBitmap(mRedPackageBit,0,0,null);
    }
}
