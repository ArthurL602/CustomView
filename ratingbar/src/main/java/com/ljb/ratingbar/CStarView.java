package com.ljb.ratingbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Author      :ljb
 * Date        :2019/9/28
 * Description :
 */
public class CStarView extends View {

    private Drawable mDefaultStar;
    private Drawable mSelectedStar;

    private int mStarNum = 5;
    private int mSelectedNum = 0;
    private int mTmpSelectedNum = -1;

    private int mStarPadding = 0;

    private int mStarWidth;
    private int mStarHeight;
    private int mDefaultRes = R.drawable.star_normal;
    private int mSelectedRes = R.drawable.star_selected;

    public CStarView(Context context) {
        this(context, null);
    }

    public CStarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CStarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        mDefaultStar = context.getDrawable(mDefaultRes);
        mSelectedStar = context.getDrawable(mSelectedRes);

    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CStarView);
        if (ta != null) {
            mStarNum = ta.getInt(R.styleable.CStarView_starNum, mStarNum);
            mStarHeight = ta.getDimensionPixelSize(R.styleable.CStarView_starHeight, mStarHeight);
            mStarWidth = ta.getDimensionPixelSize(R.styleable.CStarView_starWidth, mStarWidth);
            mStarPadding = ta.getDimensionPixelSize(R.styleable.CStarView_starPadding, mStarPadding);
            mDefaultRes = ta.getResourceId(R.styleable.CStarView_defaultStar, R.drawable.star_normal);
            mSelectedRes = ta.getResourceId(R.styleable.CStarView_selectedStar, R.drawable.star_selected);
            ta.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int startPadding = mStarPadding * (mStarNum - 1);
        int width = getMeasuredWidth(startPadding, widthMeasureSpec);
        int height = getMeasuredHeight( heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int getMeasuredWidth(int startPadding, int widthMeasureSpec) {
        int width;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
            mStarWidth = (width - getPaddingLeft() - getPaddingRight() - startPadding) / mStarNum;
        } else {
            mStarWidth = mStarWidth <= 0 ? Math.max(mDefaultStar.getIntrinsicWidth(), mSelectedStar.getIntrinsicWidth()) : mStarWidth;

            width = mStarWidth * mStarNum + +getPaddingLeft() + getPaddingRight() + startPadding;
        }
        return width;
    }

    private int getMeasuredHeight( int heightMeasureSpec) {
        int height;
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
            mStarHeight = height - getPaddingTop() - getPaddingBottom();
        } else {
            mStarHeight = mStarHeight <= 0 ? Math.max(mDefaultStar.getIntrinsicHeight(), mSelectedStar.getIntrinsicHeight()) : mStarHeight;
            height = mStarHeight + getPaddingTop() + getPaddingBottom();
        }
        return height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawDefaultStar(canvas);
        drawSelectedStar(canvas);
    }

    private void drawDefaultStar(Canvas canvas) {
        drawStart(canvas, mDefaultStar, mStarNum);
    }

    private void drawSelectedStar(Canvas canvas) {
        if (mSelectedNum < 0) return;
        drawStart(canvas, mSelectedStar, mSelectedNum);
    }

    private void drawStart(Canvas canvas, Drawable startDrawable, int num) {
        int starWidth = mStarWidth;
        int starHeight = mStarHeight;

        int left, top, right, bottom;
        for (int i = 0; i < num; i++) {
            if (i == 0) {
                left = i * starWidth + getPaddingLeft();
            } else {
                left = i * starWidth + getPaddingLeft() + mStarPadding * i;
            }

            top = getPaddingTop();
            right = left + starWidth;
            bottom = starHeight + top;
            startDrawable.setBounds(left, top, right, bottom);
            startDrawable.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int downX = (int) event.getX();
                int selectedNum;
                if (downX < getPaddingLeft()) {
                    selectedNum = 0;
                } else {
                    downX = downX - getPaddingLeft();
                    selectedNum = downX / (mStarWidth + mStarPadding) + 1;
                }
                if (mTmpSelectedNum != selectedNum && selectedNum <= mStarNum) {
                    mSelectedNum = selectedNum;
                    mTmpSelectedNum = selectedNum;
                    if (selectedNum > mStarNum) {
                        mSelectedNum = mStarNum;
                        mTmpSelectedNum = mStarNum;
                    }
                    invalidate();
                }
                break;
        }
        return true;
    }
}
