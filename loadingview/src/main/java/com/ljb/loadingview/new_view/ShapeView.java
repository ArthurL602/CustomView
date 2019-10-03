package com.ljb.loadingview.new_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author      :ljb
 * Date        :2019/10/3
 * Description :  形状自定义View :三角形、正方形、圆形
 */
public class ShapeView extends View {

    private int mDefaultWidth = 300;

    private @Shape
    int mShape = Shape.ROUND;
    private Path mRoundPath;
    private Path mSquarePath;
    private Path mTrianglePath;

    private Paint mPaint;
    private int mSquareColor = Color.BLUE;
    private int mRoundColor = Color.YELLOW;
    private int mTriangleColor = Color.RED;

    public ShapeView(Context context) {
        this(context, null);
    }

    public ShapeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = calculateWidth(widthMeasureSpec);
        int height = calculateHeight(heightMeasureSpec);
        setMeasuredDimension(Math.min(width, height), Math.min(width, height));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            RectF rect = new RectF(0, 0, getWidth(), getBottom());
            mRoundPath = new Path();
            mRoundPath.addCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, Path.Direction.CW);

            mSquarePath = new Path();
            mSquarePath.addRect(rect, Path.Direction.CW);

            mTrianglePath = new Path();
            mTrianglePath.moveTo(getWidth() / 2, 0);
            mTrianglePath.lineTo(0, getHeight());
            mTrianglePath.lineTo(getWidth(), getHeight());
            mTrianglePath.close();
        }
    }


    public void exchange() {
        switch (mShape) {
            case Shape.ROUND:
                mShape = Shape.SQUARE;
                break;
            case Shape.SQUARE:
                mShape = Shape.TRIANGLE;
                break;
            case Shape.TRIANGLE:
                mShape = Shape.ROUND;
                break;
        }
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        switch (mShape) {
            case Shape.ROUND:
                drawRound(canvas);
                break;
            case Shape.SQUARE:
                drawSquare(canvas);
                break;
            case Shape.TRIANGLE:
                drawTriangle(canvas);
                break;
        }
    }

    private void drawTriangle(Canvas canvas) {
        drawShape(canvas, mTrianglePath, mTriangleColor);
    }

    private void drawRound(Canvas canvas) {
        drawShape(canvas, mRoundPath, mRoundColor);
    }

    private void drawSquare(Canvas canvas) {
        drawShape(canvas, mSquarePath, mSquareColor);
    }

    private void drawShape(Canvas canvas, Path path, int color) {
        if (path == null) return;
        mPaint.setColor(color);
        canvas.drawPath(path, mPaint);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Shape.ROUND, Shape.SQUARE, Shape.TRIANGLE})
    @interface Shape {
        int ROUND = 0;
        int SQUARE = 1;
        int TRIANGLE = 2;
    }

    private int calculateWidth(int widthMeasureSpec) {
        int width;
        int size = MeasureSpec.getSize(widthMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            width = size;
        } else {
            width = mDefaultWidth;
        }
        return width;
    }

    private int calculateHeight(int heightMeasureSpec) {
        int height;
        int size = MeasureSpec.getSize(heightMeasureSpec);
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            height = size;
        } else {
            height = mDefaultWidth;
        }
        return height;
    }

    public int getShape() {
        return mShape;
    }
}
