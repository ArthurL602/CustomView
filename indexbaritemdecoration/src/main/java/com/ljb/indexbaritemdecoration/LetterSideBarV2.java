package com.ljb.indexbaritemdecoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Author      :ljb
 * Date        :2019/10/1
 * Description :
 */
public class LetterSideBarV2 extends View {

    private String[] mLetters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};

    private Paint mOriginalPaint;

    private float mTextSize = 40;
    private float mSelectedSize = 80;
    private int mDefaultColor = Color.BLUE;
    private int mSelectedColor = Color.RED;
    private int mRoundRectColor = Color.GRAY;
    private int mItemHeight;
    private int mCurrentPosition = -1;


    private OnLetterChangedListener mOnLetterChangedListener;


    public void setOnLetterChangedListener(OnLetterChangedListener onLetterChangedListener) {
        mOnLetterChangedListener = onLetterChangedListener;
    }

    public LetterSideBarV2(Context context) {
        this(context, null);
    }

    public LetterSideBarV2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LetterSideBarV2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        mOriginalPaint = new Paint();
        mOriginalPaint.setAntiAlias(true);
        mOriginalPaint.setDither(true);
        mOriginalPaint.setTextSize(mSelectedSize);
        mOriginalPaint.setColor(mDefaultColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        for (int i = 0; i < mLetters.length; i++) {
            width = (int) Math.max(width, mOriginalPaint.measureText(mLetters[i]));
        }
        width = width + getPaddingLeft() + getPaddingRight();

        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mItemHeight = (getHeight() - getPaddingTop() - getPaddingBottom()) / mLetters.length;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawLetter(canvas);
    }



    private void drawLetter(Canvas canvas) {
        for (int i = 0; i < mLetters.length; i++) {
            String text = mLetters[i];
            int x;
            if (i == mCurrentPosition) {
                mOriginalPaint.setColor(mSelectedColor);
                mOriginalPaint.setTextSize(mSelectedSize);
                x = (int) (getWidth() / 2 - mOriginalPaint.measureText(text) / 2 );
            } else {
                mOriginalPaint.setColor(mDefaultColor);
                mOriginalPaint.setTextSize(mTextSize);
                x = (int) (getWidth() / 2 - mOriginalPaint.measureText(text) / 2);
            }
            Paint.FontMetrics fontMetrics = mOriginalPaint.getFontMetrics();
            int y = (int) (i * mItemHeight + mItemHeight / 2 + (Math.abs(fontMetrics.ascent) - fontMetrics.descent) / 2) + getPaddingTop();
            canvas.drawText(text, x, y, mOriginalPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int downY = (int) event.getY();
                int currentPosition = (downY - getPaddingTop()) / mItemHeight;
                if (currentPosition < 0) {
                    currentPosition = 0;
                }
                if (currentPosition >= mLetters.length) {
                    currentPosition = mLetters.length - 1;
                }
                if (currentPosition != mCurrentPosition) {
                    mCurrentPosition = currentPosition;
                    invalidate();
                    if (mOnLetterChangedListener != null) {
                        mOnLetterChangedListener.onTouchChanged(mCurrentPosition, mLetters[mCurrentPosition], true);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mOnLetterChangedListener != null) {
                    mOnLetterChangedListener.onTouchChanged(mCurrentPosition, mLetters[mCurrentPosition], false);
                }
                mCurrentPosition = -1;
                invalidate();
                break;
        }
        return true;
    }


    public interface OnLetterChangedListener {
        void onTouchChanged(int positon, String letter, boolean touch);
    }
}
