package com.ljb.indexbaritemdecoration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;




/**
 * Author      :ljb
 * Date        :2018/2/23
 * Description : 字母索引条
 */

public class LetterSideBar extends View {

    private String[] mLetters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
            "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
    private Paint mPaint;
    //默认文字大小
    private int mTextSize = 20;
    //选中的文字颜色
    private int mSelectTextColor = Color.BLACK;
    //未选中的颜色
    private int mDefaultTextColor = Color.RED;
    //字母的高度
    private int mItemHeight;
    private String mCurrentLetter;

    private TextChangeListener mTextChangeListener;
    //上一次的划过的下标
    private int mLastIndex;
    //是否在触摸
    private boolean isTouch = false;

    public void setTextChangeListener(TextChangeListener textChangeListener) {
        mTextChangeListener = textChangeListener;
    }

    public LetterSideBar(Context context) {
        this(context, null);
    }

    public LetterSideBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LetterSideBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        initPaint();

    }

    /**
     * 初始化自定义属性
     *
     * @param context
     * @param attrs
     */
    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LetterSideBar);
        if (ta != null) {
            mTextSize = (int) ta.getDimension(R.styleable.LetterSideBar_LetterSizeBar_TextSize, mTextSize);
            mSelectTextColor = ta.getColor(R.styleable.LetterSideBar_LatterSizeBar_SelectTextColor, mSelectTextColor);
            mDefaultTextColor = ta.getColor(R.styleable.LetterSideBar_LatterSizeBar_DefaultTextColor,
                    mDefaultTextColor);
            ta.recycle();
        }
        mTextSize = sp2px(mTextSize);
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(mDefaultTextColor);

    }

    /**
     * 测量
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //计算宽度
        String text = mLetters[0];
        int textWidth = (int) mPaint.measureText(text, 0, text.length());
        int width = getPaddingLeft() + getPaddingRight() + textWidth;
        //计算高度
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    /**
     * 绘制文字
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        Paint.FontMetrics metrics = mPaint.getFontMetrics();
        int height = (int) (Math.abs(metrics.ascent) - metrics.descent);
        //计算每个字母所在区域的高度
        mItemHeight = (getHeight() - getPaddingBottom() - getPaddingTop()) / mLetters.length;
        for (int i = 0; i < mLetters.length; i++) {
            String letter = mLetters[i];
            //文字的宽度
            int width = (int) mPaint.measureText(letter);
            int x = getWidth() / 2 - width / 2;
            //计算每个字母的中心位置
            int letterHeight = mItemHeight * i + mItemHeight / 2 + getPaddingTop();
            //计算基线位置
            int y = letterHeight + height / 2;
            //判断当前字母是否需要高亮
            if (letter.equals(mCurrentLetter) && isTouch) {
                mPaint.setColor(mSelectTextColor);
            } else {
                mPaint.setColor(mDefaultTextColor);
            }
            canvas.drawText(letter, x, y, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                isTouch=true;
                //计算当前字母  获取当前的位置
                float currentY = event.getY();
                //currentY/ 字母的高度 = 当前文字的位置
                int index = (int) ((currentY - getPaddingTop()) / mItemHeight);
                //防止数组下标越界
                if (index >= mLetters.length) {
                    index = mLetters.length - 1;
                }
                //防止有负数
                if (index < 0) {
                    index = 0;
                }
                //当前绘制的字母
                mCurrentLetter = mLetters[index];
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //如果是摁下，直接回调，并且改变字体
                    changeText();
                } else {
                    //判断手指否是还在同一个字母内
                    boolean isInner = mLastIndex == index;
                    //如果是移动，判断是否再用一个字母内，如果是用一个字母，就不回调
                    if (!isInner) {
                        changeText();
                    }
                }
                mLastIndex = index;
                break;
            case MotionEvent.ACTION_UP:
                isTouch=false;
                if (mTextChangeListener != null) {
                    mTextChangeListener.onTextChange(mCurrentLetter, false);
                }
                invalidate();
                break;
        }
        return true;
    }

    /**
     * 改变字体
     */
    private void changeText() {
        if (mTextChangeListener != null) {
            mTextChangeListener.onTextChange(mCurrentLetter, true);
        }
        //重新绘制
        invalidate();
    }

    /**
     * 当前触摸的字母回调接口
     */
    public interface TextChangeListener {
        void onTextChange(String letter, boolean isTouch);

    }

    /**
     * sp转化成px
     *
     * @param textSize
     * @return
     */
    private int sp2px(int textSize) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, getResources().getDisplayMetrics
                ());
    }
}
