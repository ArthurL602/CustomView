package com.xiaoma.ljb.seekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * @Author: LiangJingBo.
 * @Date: 2019/04/29
 * @Describe:
 */

public class LSeekbar extends View {
    /******************CONSTANT***********************/
    private final static int MIN_THUMB_HEIGHT_WIDTH = 30;
    private final static int DEFAULT_WIDTH = 300;
    private final static int DEFAULT_MAX = 100;

    /********************DATA**************************/
    private Drawable mThumbDrawable;
    private Drawable mSeekDrawable;
    private int mHeight;
    private float percent = 0;
    private int lastX;
    private int mThumbHeight;
    private int mWidth;
    private int mThumbWidth;
    private int mSeekBarWidth;
    private int mSeekBarHeight;
    private Paint mProgressPaint;
    private int range = 26;
    private int mMax = DEFAULT_MAX;
    private int fillet = 30;//圆角
    private boolean mContains;//首次摁下的点是否在Thumb范围内
    private boolean isScrollEnable = true;//是否能够滑动，默认是可以的
    private int defaultPadding = 8;//默认上下padding值
    private int mSeekbar_bg_resId;
    private int mThumb_resId;
    private int mProgress_color;
    private int mBgHeight;

    /************************Listener*********************************/
    private OnSeekBarChangeListener mSeekBarChangeListener;



    public LSeekbar(Context context) {
        this(context, null);
    }

    public LSeekbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LSeekbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
        init();
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LSeekbar, defStyleAttr, 0);
        if (typedArray != null) {
            mSeekbar_bg_resId = typedArray.getResourceId(R.styleable.LSeekbar_bg_source, R.drawable.seek_bg);//进度条背景
            mThumb_resId = typedArray.getResourceId(R.styleable.LSeekbar_thumb_source, R.drawable.thumb);// thumb
            mThumbWidth = typedArray.getDimensionPixelSize(R.styleable.LSeekbar_thumb_width, 0);
            mThumbHeight = typedArray.getDimensionPixelSize(R.styleable.LSeekbar_thumb_height, 0);
            mBgHeight = typedArray.getDimensionPixelSize(R.styleable.LSeekbar_bg_height, 0);
            mProgress_color = typedArray.getColor(R.styleable.LSeekbar_progress_color, Color.RED);
            fillet = typedArray.getDimensionPixelOffset(R.styleable.LSeekbar_fillet, 0);
            typedArray.recycle();
        }
    }

    private void init() {
        mThumbDrawable = getContext().getResources().getDrawable(mThumb_resId);
        mSeekDrawable = getContext().getResources().getDrawable(mSeekbar_bg_resId);

        mProgressPaint = new Paint();
        mProgressPaint.setStyle(Paint.Style.FILL);
        mProgressPaint.setColor(mProgress_color);
    }


    public void setSeekBarChangeListener(OnSeekBarChangeListener seekBarChangeListener) {
        mSeekBarChangeListener = seekBarChangeListener;
    }

    public void setScrollEnable(boolean scrollEnable) {
        isScrollEnable = scrollEnable;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = calculatedWidth(widthMeasureSpec);
        int height = calculatedHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int calculatedHeight(int heightMeasureSpec) {
        int result = 0;
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            int calculateSize = size + getPaddingBottom() + getPaddingTop() + defaultPadding * 2;
            if (calculateSize < MIN_THUMB_HEIGHT_WIDTH) {
                calculateSize = MIN_THUMB_HEIGHT_WIDTH;
            }
            result = calculateSize;
        } else {
            result = MIN_THUMB_HEIGHT_WIDTH;
        }
        return result;
    }

    private int calculatedWidth(int widthMeasureSpec) {
        int result = 0;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            if (size < DEFAULT_WIDTH) {
                size = DEFAULT_WIDTH;
            }
            result = size;
        } else {
            result = DEFAULT_WIDTH;
        }
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        mWidth = w;
        if (mThumbWidth == 0) {
            mThumbWidth = mHeight - getPaddingTop() - getPaddingBottom() - defaultPadding * 2;
        }
        if (mThumbHeight == 0) {
            mThumbHeight = mThumbWidth;
        }
        if (mBgHeight == 0) {
            mBgHeight = mThumbHeight / 3;
        }
        mSeekDrawable.setBounds(0, 0, mWidth - mThumbWidth - range * 2, mBgHeight);
        mSeekBarWidth = mSeekDrawable.getBounds().width();
        mSeekBarHeight = mSeekDrawable.getBounds().height();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制进度条
        drawBg(canvas);
        // 绘制一级进度条
        drawFirstProgress(canvas);
        // 绘制二级进度条
        // nothing to do
        // 绘制thumb
        drawThumb(canvas);
    }

    private void drawBg(Canvas canvas) {
        canvas.save();
        canvas.translate(mThumbWidth / 2 + range, mHeight / 2 - mSeekBarHeight / 2);
        mSeekDrawable.draw(canvas);
        canvas.restore();
    }

    // 绘制 进度
    private void drawFirstProgress(Canvas canvas) {
        canvas.save();
        canvas.translate(mThumbWidth / 2 + range, mHeight / 2 - mSeekBarHeight / 2);
        int right = (int) (percent * mSeekBarWidth * 1f);
        RectF rect = new RectF(0, 0, right, mSeekBarHeight);
        canvas.drawRoundRect(rect, fillet, fillet, mProgressPaint);
        canvas.restore();
    }

    private void drawThumb(Canvas canvas) {
        int left = (int) (percent * mSeekBarWidth * 1f) + range;
        mThumbDrawable.setBounds(left, mHeight / 2 - mThumbHeight / 2, left + mThumbWidth, mHeight / 2 + mThumbHeight / 2);
        mThumbDrawable.draw(canvas);
    }


    public void setProgress(long progress) {
        percent = (float) progress / (float) mMax;
        if (percent >= 1) {
            percent = 1;
        }
        if (percent <= 0) {
            percent = 0;
        }
        onProgressChanged(this, progress, false, percent);
        onStopTouch(this);
        postInvalidate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isScrollEnable) return false;
        if (!mContains) {
            int downX = (int) event.getX();
            int downY = (int) event.getY();
            Rect bound = createNewBounds(range);
            //构建一个新bound，扩大thumb感应范围
            mContains = bound.contains(downX, downY);
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN && !mContains) {
            return false;
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getX();
                    onStartTouch(this);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int x = (int) event.getX();
                    percent += (x - lastX) * 1f / getWidth() * 1f;
                    if (percent >= 1) {
                        percent = 1;
                    }
                    if (percent <= 0) {
                        percent = 0;
                    }
                    lastX = x;
                    invalidate();
                    onProgressChanged(this, (int) (mMax * percent), true, percent);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    onStopTouch(this);
                    mContains = false;
                    break;
            }
            return true;
        }

    }

    private Rect createNewBounds(int range) {
        Rect bounds = mThumbDrawable.getBounds();
        Rect rect = new Rect(bounds.left - range, bounds.top - range, bounds.right + range, bounds.bottom + range);
        return rect;
    }

    public interface OnSeekBarChangeListener {

        void onProgressChanged(LSeekbar seekBar, long progress, boolean fromUser, float percent);

        void onStartTouch(LSeekbar seekBar);

        void onStopTouch(LSeekbar seekBar);
    }

    private void onProgressChanged(LSeekbar seekBar, long progress, boolean fromUser, float percent) {
        if (mSeekBarChangeListener != null) {
            mSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser, percent);
        }
    }

    private void onStartTouch(LSeekbar seekBar) {
        if (mSeekBarChangeListener != null) {
            mSeekBarChangeListener.onStartTouch(seekBar);
        }
    }

    private void onStopTouch(LSeekbar seekBar) {
        if (mSeekBarChangeListener != null) {
            mSeekBarChangeListener.onStopTouch(seekBar);
        }
    }
}
