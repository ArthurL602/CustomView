package com.ljb.huashuloadingview.new_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Author      :ljb
 * Date        :2019/10/4
 * Description :
 */
public class CircleView extends View {

    private Paint mPaint;

    private float mCenterX, mCenterY, mRadius;

    private int mDefaultSize = 20;
    private int mColor = Color.RED;

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);

        mDefaultSize = dp2px(mDefaultSize);
    }

    private int dp2px(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getContext().getResources().getDisplayMetrics());
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = calculateWidth(widthMeasureSpec);
        int height = calculateHeight(heightMeasureSpec);
        setMeasuredDimension(Math.min(width, height), Math.min(width, height));
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mCenterX = mCenterY = mRadius = getWidth() / 2;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
    }

    public void exchangeColor(int color) {
        mColor = color;
        mPaint.setColor(color);
        postInvalidate();
    }

    public int getColor() {
        return mColor;
    }

    private int calculateHeight(int heightMeasureSpec) {
        int height;
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            height = size;
        } else {
            height = mDefaultSize;
        }
        return height;
    }

    private int calculateWidth(int widthMeasureSpec) {
        int width;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            width = size;
        } else {
            width = mDefaultSize;
        }
        return width;
    }
}
