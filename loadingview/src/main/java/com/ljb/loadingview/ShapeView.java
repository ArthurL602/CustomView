package com.ljb.loadingview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Author      :ljb
 * Date        :2018/2/11
 * Description : 多图形自定义View
 */

public class ShapeView extends View {
    private Shape mCurrentShape= Shape.Square;
    private Paint mPaint;

    private int mSquareColor = Color.BLUE;
    private int mTriAnangleColor = Color.RED;
    private int mCircleColor = Color.GREEN;

    public Shape getCurrentShape() {
        return mCurrentShape;
    }

    public void setCurrentShape(Shape currentShape) {
        mCurrentShape = currentShape;
    }

    public ShapeView(Context context) {
        this(context, null);
    }

    public ShapeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        switch (mCurrentShape) {
            case Circle://画圆
                mPaint.setColor(mCircleColor);
                canvas.drawCircle(width / 2, height / 2, width / 2, mPaint);
                break;
            case Square://画正方形
                mPaint.setColor(mSquareColor);
                canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
                break;
            case Triangle://画三角形
                mPaint.setColor(mTriAnangleColor);
                Path triangle = new Path();
                triangle.moveTo(0, height);
                triangle.lineTo(width, height);
                triangle.lineTo(width / 2, 0);
                triangle.close();
                canvas.drawPath(triangle, mPaint);
                break;
        }
    }

    public void exchange() {
        switch (mCurrentShape) {
            case Circle://画圆
                mCurrentShape = Shape.Square;
                break;
            case Square://画正方形
                mCurrentShape = Shape.Triangle;
                break;
            case Triangle://画三角形
                mCurrentShape = Shape.Circle;
                break;
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getDefualt_Size(widthMeasureSpec);
        int height = getDefualt_Size(heightMeasureSpec);
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    private int getDefualt_Size(int measureSpec) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = 0;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(size, 0);
            }
        }
        return result;
    }

    public enum Shape {
        Circle, Square, Triangle;
    }
}
