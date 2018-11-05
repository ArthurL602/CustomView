package com.meloon.alipayview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Author      :ljb
 * Date        :2018/11/5
 * Description : 仿阿里巴巴支付成功效果
 */
public class AliPlayView extends View {

    private int mWidth = 200; // 宽
    private int mHeight = 200; // 高

    private float mFirstXRatio = 0.5f;
    private float mFirstYRatio = 0;
    private float mCenterXRatio = 0f;
    private float mCenterYRatio = 0.5f;
    private float mLastXRatio = 0.5f;
    private float mLastYRatio = 0.3f;
    private int color = Color.BLUE;
    private int x, y, radius;// x y 中心坐标，radius: 半径
    private int mPathWidth = 4;

    private Path mPath; // 圆 路径

    private int duration = 3000;


    private Paint mPaint;

    private PathMeasure mPathMeasure;
    private float mFraction;

    private Path mDst;

    private boolean hasNext;// 是否切换到下一个
    private ValueAnimator mAnimator;

    public AliPlayView(Context context) {
        this(context, null);
    }

    public AliPlayView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AliPlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        initAttrs(context, attrs);
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mPathWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(color);

        x = mWidth / 2;
        y = mHeight / 2;
        radius = Math.min(mWidth, mWidth) / 2 - 20;
        mPath = new Path();
        mPath.addCircle(x, y, radius, Path.Direction.CW); // 添加一个圆

        mPath.moveTo(x - mFirstXRatio * radius, y + mFirstYRatio * radius);
        mPath.lineTo(x - mCenterXRatio * radius, y + radius * mCenterYRatio);
        mPath.lineTo(x + mLastXRatio * radius, y - radius * mLastYRatio);

        mPathMeasure = new PathMeasure(mPath, false);

        mDst = new Path();

        mAnimator = ValueAnimator.ofFloat(0f, 2f);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFraction = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        mAnimator.setDuration(duration);
        mAnimator.start();
    }

    public void startAnimation() {
        mAnimator.start();
    }

    /**
     * 初始化自定义属性
     *
     * @param context
     * @param attrs
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AliPlayView);
        if (ta != null) {
            mWidth = (int) ta.getDimension(R.styleable.AliPlayView_view_width, mWidth);
            mHeight = (int) ta.getDimension(R.styleable.AliPlayView_view_height, mHeight);

            mFirstXRatio = ta.getFloat(R.styleable.AliPlayView_first_point_x_ratio, mFirstXRatio);
            mFirstYRatio = ta.getFloat(R.styleable.AliPlayView_first_point_y_ratio, mFirstYRatio);

            mCenterXRatio = ta.getFloat(R.styleable.AliPlayView_center_point_x_ratio, mCenterXRatio);
            mCenterYRatio = ta.getFloat(R.styleable.AliPlayView_center_point_y_ratio, mCenterYRatio);

            mLastXRatio = ta.getFloat(R.styleable.AliPlayView_last_point_x_ratio, mLastXRatio);
            mLastYRatio = ta.getFloat(R.styleable.AliPlayView_last_point_y_ratio, mLastYRatio);

            color = ta.getColor(R.styleable.AliPlayView_path_color, color);
            mPathWidth = (int) ta.getDimension(R.styleable.AliPlayView_pathWidth, mPathWidth);

            duration = ta.getInt(R.styleable.AliPlayView_anim_duration,duration);
            ta.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mFraction < 1) {
            float stop = mFraction * mPathMeasure.getLength();
            mPathMeasure.getSegment(0, stop, mDst, true);
        } else {
            if (!hasNext) {
                hasNext = true;
                float stop = mFraction * mPathMeasure.getLength();
                mPathMeasure.getSegment(0, stop, mDst, true);
                mPathMeasure.nextContour();
            } else {
                float stop = (mFraction - 1) * mPathMeasure.getLength();
                mPathMeasure.getSegment(0, stop, mDst, true);
            }
        }
        canvas.drawPath(mDst, mPaint);
    }
}
