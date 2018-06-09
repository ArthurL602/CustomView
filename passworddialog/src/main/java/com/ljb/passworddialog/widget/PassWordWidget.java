package com.ljb.passworddialog.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Author      :ljb
 * Date        :2018/3/28
 * Description :
 */

public class PassWordWidget extends View {
    /*屏幕宽高*/
    private int mScreenWidht;
    private int width;
    private int height;
    private Paint mPaint;
    private Paint mCirclePaint;
    /*边框宽度*/
    private int strokeWidth = 4;
    /*每个小密码框的大小*/
    private int itemSize;
    private int count = 0;
    private int mRadius = 0;

    public PassWordWidget(Context context) {
        this(context, null);
    }

    public PassWordWidget(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PassWordWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScreenWidht = context.getResources().getDisplayMetrics().widthPixels;
        init();
    }

    private void init() {
        strokeWidth = dp2px(strokeWidth);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(strokeWidth);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        itemSize = w / 6;
        mRadius = height / 2 - 10;
    }

    private int dp2px(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getdefSize(widthMeasureSpec);
        height = width / 8;
        setMeasuredDimension(width, height);
    }

    /**
     * 获取宽高
     *
     * @param measureSpec
     * @return
     */
    private int getdefSize(int measureSpec) {
        int result = 0;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = mScreenWidht;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(mScreenWidht, size);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw边框
        drawRect(canvas);
        //drawLine
        drawLines(canvas);
        // 绘制密码圆点
        drawPswCircle(canvas);
    }

    /**
     * 绘制密码圆点
     * @param canvas
     */
    private void drawPswCircle(Canvas canvas) {
        for (int i = 0; i < count; i++) {
            int x = itemSize / 2 + i * itemSize;
            int y = height / 2;
            canvas.drawCircle(x, y, mRadius, mCirclePaint);

        }
    }

    /**
     * 绘制密码边框
     *
     * @param canvas
     */
    private void drawLines(Canvas canvas) {
        mPaint.setStrokeWidth(4);
        for (int i = 0; i < 5; i++) {
            canvas.drawLine(itemSize * (i + 1), 0, itemSize * (i + 1), height, mPaint);
        }
    }

    /**
     * 绘制边框
     *
     * @param canvas
     */
    private void drawRect(Canvas canvas) {
        mPaint.setStrokeWidth(strokeWidth);
        Rect rect = new Rect(0, 0, width, height);
        canvas.drawRect(rect, mPaint);

    }

    public void upDataCount(int count ){
        this.count = count;
        invalidate();

    }
}
