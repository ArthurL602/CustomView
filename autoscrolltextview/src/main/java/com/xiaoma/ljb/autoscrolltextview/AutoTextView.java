package com.xiaoma.ljb.autoscrolltextview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * @Author: LiangJingBo.
 * @Date: 2019/05/30
 * @Describe:
 */

public class AutoTextView extends android.support.v7.widget.AppCompatTextView {
    private Paint mPaint;
    private int mViewWidth;
    private String mText;
    private boolean useDefaultMarquee = false;
    private int mInitY;
    private int mTextLength;

    private int distance;
    private int REFRESH_DELAY = 16;//移动间隔时间
    private int INCREMENT = 1;//每次移动距离

    public AutoTextView(Context context) {
        this(context, null);
    }

    public AutoTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = getPaint();
    }


    public void setText(String text) {
        super.setText(text);
        getContent();
        someCompute();
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
    }
    /**
     * 让 textview 随时以为自己是获取焦点状态
     * 自滚动需要拥有焦点,而同一个页面同一时间只能有一个控件拥有焦点,
     * 解决了原生的跑马灯经常会因为焦点的抢夺导致停止滚动的问题
     *
     * @return true
     */
    @Override
    public boolean isFocused() {
        return true;
    }

    /**
     * 避免popwindow弹出时,失去焦点导致跑马效果暂停
     *
     * @param hasWindowFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(true);
    }
    private void someCompute() {
        mTextLength = (int) mPaint.measureText(mText);
        if (mTextLength > mViewWidth) { //use default marquee
            useDefaultMarquee = true;
        } else {
            useDefaultMarquee = false;
        }
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        mInitY = (int) (getHeight() / 2 - (fontMetrics.descent + fontMetrics.ascent) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (useDefaultMarquee) {
            super.onDraw(canvas);
        } else {
            if (distance <= -mViewWidth) {
                distance = 0;
            }
            if (distance >= -mTextLength) {//绘制左进
                canvas.drawText(mText, 0, mText.length(), distance, mInitY, mPaint);
            }
            if (distance < 0) {//绘制右出
                canvas.drawText(mText, 0, mText.length(), mViewWidth + distance, mInitY, mPaint);
            }
            postDelayed();
        }

    }

    private void postDelayed() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                distance -= INCREMENT;
                invalidate();
            }
        }, REFRESH_DELAY);
    }
}
