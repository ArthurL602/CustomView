package com.ljb.autoscrolltextview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;

public class AutoTextView extends android.support.v7.widget.AppCompatTextView {
    private Paint mPaint;
    private int mViewWidth;
    private String mText;
    private boolean mIsTextLengthOver = false;
    private int mInitY;
    private int mTextLength;

    private int distance;
    private int REFRESH_DELAY = 16;//移动间隔时间
    private int INCREMENT = 1;//每次移动距离

    private int mSpace = 20;//间距，距离上一个文本末尾的距离
    private int mShadowWidth = 20;
    private int mInitX = 0;


    private RectF mLeftRectF;
    private LinearGradient mLeftGradient;
    private Paint mPaintShadow;
    private RectF mRightRectF;
    private LinearGradient mRightGradient;

    private boolean mIsRunning = true;
    private boolean isDrawShadow =true;
    private Runnable mRunnable;

    public boolean isRunning() {
        return mIsRunning;
    }

    public AutoTextView(Context context) {
        this(context, null);
    }

    public AutoTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint = getPaint();
        setSingleLine();
        mPaint.setColor(getPaint().getColor());
    }


    public void setText(String text) {
        super.setText(text);
        stopMarquee();
        getContent();
        someCompute();
        startMarquee();
    }

    private void getContent() {
        mText = getText().toString();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        getContent();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        someCompute();
        // left
        mLeftRectF = new RectF(0,0,mShadowWidth,getHeight());
        mLeftGradient = new LinearGradient(mLeftRectF.left,0, mLeftRectF.right,0,0x90ffffff, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        mPaintShadow = new Paint();


        //right
        mRightRectF = new RectF(getWidth() - mShadowWidth ,0,getWidth(),getBottom());
        mRightGradient = new LinearGradient(mRightRectF.left,0, mRightRectF.right,0, Color.TRANSPARENT,0x90ffffff, Shader.TileMode.CLAMP);
    }


    public void startMarquee(){
        if(!mIsRunning){
            mIsRunning = true;
            distance = mInitX;
            invalidate();
        }
    }

    public void stopMarquee(){
        if(mIsRunning){
            removePost();
            mIsRunning = false;
            distance = mInitX;
        }
    }

    private void someCompute() {
        mTextLength = (int) mPaint.measureText(mText);
        if (mTextLength > mViewWidth) { //use default marquee
            mIsTextLengthOver = true;
            mInitX = 0;
        } else {
            mInitX= mViewWidth / 2 - mTextLength / 2;
            distance = mInitX;
            mIsTextLengthOver = false;
        }
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        mInitY = (int) (getHeight() / 2 - (fontMetrics.descent + fontMetrics.ascent) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mIsTextLengthOver) {
            drawTextLengthOver(canvas);
        } else {
            drawTextLength(canvas);
        }
        if(mIsRunning){
            postDelayed();
        }
        if(isDrawShadow){
            // 绘制两边阴影
            drawShadow(canvas);
        }
    }

    private void drawTextLength(Canvas canvas) {
        if (distance <= -mViewWidth) {
            distance = 0;
        }
        if (distance >= -mTextLength) {//绘制左进
            canvas.drawText(mText, 0, mText.length(), distance, mInitY, mPaint);
        }
        if (distance < 0) {//绘制右出
            canvas.drawText(mText, 0, mText.length(), mViewWidth + distance, mInitY, mPaint);
        }
    }

    private void drawTextLengthOver(Canvas canvas) {
        int disparity = mTextLength - mViewWidth;
        if (distance <= -mTextLength - mSpace) {
            distance = 0;
        }
        if (distance >= -mTextLength) {//绘制左进
            canvas.drawText(mText, 0, mText.length(), distance, mInitY, mPaint);
        }
        if (distance <= -disparity - mSpace) { // 绘制从右边出来的
            float x = mTextLength + mSpace - mViewWidth + distance + mViewWidth;
            canvas.drawText(mText, 0, mText.length(), x, mInitY, mPaint);
        }
    }

    private void drawShadow(Canvas canvas) {
        // 绘制两边阴影
        //left
        mPaintShadow.setShader(mLeftGradient);
        canvas.drawRect(mLeftRectF, mPaintShadow);
        // right
        mPaintShadow.setShader(mRightGradient);
        canvas.drawRect(mRightRectF, mPaintShadow);
    }

    private void postDelayed() {
        if(mRunnable == null){
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    distance -= INCREMENT;
                    invalidate();
                }
            };
        }
        postDelayed(mRunnable, REFRESH_DELAY);
    }

    private void removePost(){
        if(mRunnable == null) return;
        removeCallbacks(mRunnable);
        mRunnable=null;
    }

    @Override
    protected void onDetachedFromWindow() {
        stopMarquee();
        super.onDetachedFromWindow();
    }
}
