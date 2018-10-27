package com.ljb.ratingbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Author      :ljb
 * Date        :2018/5/31
 * Description : 自定义RatingBar
 */

public class RatingBar extends View {

    private Bitmap mNormalBitmap, mFocusBitmap;
    // 图形数
    private int mStarNums = 5;


    private int mNormalResId;
    private int mFocusResId;
    // 图形间的边距
    private int mStarPadding;
    // 标记的图形数
    private int mFocusNum;
    private int mCacheFocusNum;
    private float mHeight;
    private float mWidth;

    public RatingBar(Context context) {
        this(context, null);
    }

    public RatingBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatingBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainAttrs(context, attrs);
        initBit();
    }

    private void initBit() {
        mNormalBitmap = BitmapFactory.decodeResource(getResources(), mNormalResId);
        mFocusBitmap = BitmapFactory.decodeResource(getResources(), mFocusResId);
    }


    private void obtainAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RatingBar);
        mNormalResId = ta.getResourceId(R.styleable.RatingBar_starNormal, 0);
        if (mNormalResId == 0) {
            throw new RuntimeException("请设置 starNormal");
        }
        mFocusResId = ta.getResourceId(R.styleable.RatingBar_starFocus, 0);
        if (mFocusResId == 0) {
            throw new RuntimeException("请设置 starFocus");
        }
        mStarNums = ta.getInt(R.styleable.RatingBar_startNums, mStarNums);
        mStarPadding = (int) ta.getDimension(R.styleable.RatingBar_starPadding, mStarPadding);
        mFocusNum = ta.getInt(R.styleable.RatingBar_focusNums, mFocusNum);
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getMeasuredWidthSize(widthMeasureSpec);
        int height = getMeasureHeightSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mNormalBitmap = Bitmap.createScaledBitmap(mNormalBitmap, (int) mWidth, (int) mHeight, false);
        mFocusBitmap = Bitmap.createScaledBitmap(mFocusBitmap, (int) mWidth, (int) mHeight, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw normal
        for (int i = 0; i < mStarNums; i++) {
            // 左内边距 + 图形的宽度 + 图形之间的间隔
            int startX = getPaddingLeft() + i * mNormalBitmap.getWidth() + i * mStarPadding;
            if (i > mFocusNum - 1) { // 绘制未选定的
                canvas.drawBitmap(mNormalBitmap, startX, getPaddingTop(), null);
            } else { // 绘制默认的
                canvas.drawBitmap(mFocusBitmap, startX, getPaddingTop(), null);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                int leftPadding = getPaddingLeft(); // 左边距
                int starPaddings = mStarPadding * (mStarNums - 1); // 图形之间的边距
                int starWidths = mStarNums * mNormalBitmap.getWidth(); // 所有是图形的宽度
                if (moveX > leftPadding + starWidths + starPaddings) return true;
                // 标亮的图形的个数
                mFocusNum = (int) ((moveX - leftPadding) / (mNormalBitmap.getWidth() + mStarPadding) + 1);
                if (mFocusNum < 0) {
                    mFocusNum = 0;
                }
        }
        // 如果个数有变化才绘制
        if (mCacheFocusNum != mFocusNum) {
            invalidate();
            mCacheFocusNum = mFocusNum;
        }
        return true;
    }

    /**
     * 计算高度
     *
     * @param heightMeasureSpec
     * @return
     */
    private int getMeasureHeightSize(int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int topPadding = getPaddingTop();
        int bottomPadding = getPaddingBottom();
        int result;
        if (mode == MeasureSpec.EXACTLY) {
            result = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            // 上内边距 + 下内边距 + 图形宽度
            result = topPadding + bottomPadding + mNormalBitmap.getHeight();
        }
        // 计算单个图形的高度
        mHeight = result - topPadding - bottomPadding;
        return result;
    }

    /**
     * 计算宽度
     *
     * @param widthMeasureSpec
     * @return
     */
    private int getMeasuredWidthSize(int widthMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int result;
        int leftPadding = getPaddingLeft();// 左内边距
        int rightPadding = getPaddingRight(); // 右内边距
        int starPadding = mStarPadding * (mStarNums - 1); // 图形间的所有间隔
        if (mode == MeasureSpec.EXACTLY) {
            result = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            // 左内边距 + 右内边距 + 图形的之间的边距+所有图形的宽度
            result = leftPadding + rightPadding + starPadding + mStarNums * mNormalBitmap.getWidth();
        }
        // 计算单个图形的宽度
        mWidth = (result - leftPadding - rightPadding - starPadding) / mStarNums;
        return result;
    }

}
