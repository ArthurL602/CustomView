package com.ljb.musicwaveview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Author      :ljb
 * Date        :2019/10/13
 * Description :
 */
public class MusicWaveView extends View {

    private int mWidth;
    private int mHeight;


    private int mWaveSize = 30; // 横向波形数
    private float mCeilPadding;
    private int mCeilRealSize;
    private int mMaxCeilSize; // 纵向最大方块数
    private float mCeilPaddingRate = 0.1f;

    private List<WaveBean> mWaveBeans;

    private Paint mPaint;

    private Handler mHandler;
    private Random mRandom;

    private boolean mIsStop = false;

    private int time_delay = 100;

    public MusicWaveView(Context context) {
        this(context, null);
    }

    public MusicWaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public boolean isStop() {
        return mIsStop;
    }

    private void init(Context context, AttributeSet attrs) {
        initAttrs(context, attrs);

        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.FILL);

        mHandler = new Handler(Looper.myLooper());
        mRandom = new Random();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MusicWaveView);
        if (typedArray != null) {
            time_delay = typedArray.getInt(R.styleable.MusicWaveView_time_delay, time_delay);
            mWaveSize = typedArray.getInt(R.styleable.MusicWaveView_waveSize, mWaveSize);
            mCeilPaddingRate = typedArray.getFloat(R.styleable.MusicWaveView_ceilPaddingRate, mCeilPaddingRate);
            typedArray.recycle();
        }
        if (mCeilPaddingRate > 1.0f) {
            mCeilPaddingRate = 0.1f;
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = calculateSize(widthMeasureSpec, getScreenWidth());
        int height = calculateSize(heightMeasureSpec, getScreenHeight());
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        int ceilSize = mWidth / mWaveSize;
        mCeilPadding = ceilSize * mCeilPaddingRate;
        mCeilRealSize = (int) ((mWidth - (mWaveSize - 1) * mCeilPadding) / mWaveSize);
        mMaxCeilSize = mHeight / 2 / ceilSize;
        initData(mWaveSize);
    }

    private void initData(int waveSize) {
        mWaveBeans = new ArrayList<>(waveSize);
        for (int i = 0; i < waveSize; i++) {
            WaveBean waveBean = new WaveBean();
            //设置纵行个数
            int size = getWaveCeilSize(i);
            waveBean.setWaveSize(size);
            //设置是递增还是递减
            boolean isUp = i % 2 == 0;
            waveBean.setUp(isUp);
            mWaveBeans.add(waveBean);
        }

    }

    public void stopAnim() {
        mIsStop = true;
        mHandler.removeCallbacksAndMessages(null);
    }

    public void startAnim() {
        mIsStop = false;
        refreshAnim();
    }

    private int getWaveCeilSize(int index) {
        int size = mRandom.nextInt(mMaxCeilSize + 1);
        if (size == 0) {
            size = getWaveCeilSize(index);
        }
        if (index >= 1) {
            int preSize = mWaveBeans.get(index - 1).getWaveSize();
            if (preSize == size) {
                size = getWaveCeilSize(index);
            }
        }
        return size;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        drawWaves(canvas);
        drawWavesReflection(canvas);
        refreshAnim();
    }

    private void refreshAnim() {
        if (mIsStop) return;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshData();
                invalidate();
            }
        }, time_delay);
    }

    private void drawWavesReflection(Canvas canvas) {
        canvas.save();
        canvas.translate(0, mHeight / 2 + mCeilPadding);
        for (int i = 0; i < mWaveBeans.size(); i++) { // 绘制横向
            for (int j = 0; j < mWaveBeans.get(i).getWaveSize(); j++) {// 绘制纵向
                mPaint.setAlpha(60 - j * 8 > 20 ? 60 - j * 8 : 20);
                int left = (int) (i * (mCeilRealSize + mCeilPadding));
                int top = (int) (j * (mCeilRealSize + mCeilPadding));
                int right = left + mCeilRealSize;
                int bottom = top + mCeilRealSize;
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
        canvas.restore();
    }

    private void drawWaves(Canvas canvas) {
        canvas.save();
        canvas.translate(0, mHeight / 2);
        canvas.scale(1, -1);
        mPaint.setAlpha(255);
        for (int i = 0; i < mWaveBeans.size(); i++) { // 绘制横向
            for (int j = 0; j < mWaveBeans.get(i).getWaveSize(); j++) {// 绘制纵向
                int left = (int) (i * (mCeilRealSize + mCeilPadding));
                int top = (int) (j * (mCeilRealSize + mCeilPadding));
                int right = left + mCeilRealSize;
                int bottom = top + mCeilRealSize;
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
        canvas.restore();
    }

    private void refreshData() {
        for (int i = 0; i < mWaveBeans.size(); i++) {
            WaveBean waveBean = mWaveBeans.get(i);
            waveBean.operation();
        }
    }

    private int calculateSize(int measureSpec, int defaultSize) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = defaultSize;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(size, result);
            }
        }
        return result;
    }

    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    private int getScreenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    private class WaveBean {
        private int mWaveSize;
        private boolean mIsUp;

        public WaveBean() {
        }

        public WaveBean(int waveSize, boolean isUp) {
            mWaveSize = waveSize;
            mIsUp = isUp;
        }

        public int getWaveSize() {
            return mWaveSize;
        }

        public void setWaveSize(int waveSize) {
            mWaveSize = waveSize;
        }

        public boolean isUp() {
            return mIsUp;
        }

        public void setUp(boolean up) {
            mIsUp = up;
        }

        public void operation() {
            if (mIsUp) {
                if (mWaveSize < mMaxCeilSize) {
                    mWaveSize++;
                } else {
                    mIsUp = false;
                    mWaveSize--;
                }
            } else {
                if (mWaveSize > 1) {
                    mWaveSize--;
                } else {
                    mWaveSize++;
                    mIsUp = true;
                }
            }
        }
    }
}
