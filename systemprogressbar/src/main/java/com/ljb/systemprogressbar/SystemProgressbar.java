package com.ljb.systemprogressbar;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Author      :ljb
 * Date        :2018/3/9
 * Description : 仿系统的Progressbar
 */

public class SystemProgressbar extends View {

    //原路径
    private Path mSrcPath;
    //目标路径
    private Path mDestPath;
    //半径
    private int mRadius;
    private PathMeasure mPathMeasure;
    private float mPathLength;
    private float mPathPercent;
    private Paint mPaint;
    private int mDuration = 2000;
    private int mColor;
    private int mCircleWidth;

    public SystemProgressbar(Context context) {
        this(context, null);
    }

    public SystemProgressbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SystemProgressbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mSrcPath = new Path();
        mDestPath = new Path();

        //实例化自定义参数
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SystemProgressbar);
        if (ta != null) {
            mColor = ta.getColor(R.styleable.SystemProgressbar_circleColor, getResources().getColor(R.color.colorPrimary));
            mCircleWidth = (int) ta.getDimension(R.styleable.SystemProgressbar_circleWidth,0);
            ta.recycle();
        }
        mPaint = new Paint();
        mPaint.setColor(mColor);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasureSize(widthMeasureSpec);
        int height = width;
        mRadius = width / 2;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(mCircleWidth==0){
            mCircleWidth=mRadius/5;
        }
        mSrcPath.addCircle(w / 2, h / 2, mRadius - mCircleWidth/2, Path.Direction.CW);
        mPathMeasure = new PathMeasure();
        mPathMeasure.setPath(mSrcPath, false);
        //获取pathMeasure的长度
        mPathLength = mPathMeasure.getLength();

        mPaint.setStrokeWidth(mCircleWidth);
        startAnim();
    }

    /**
     * 开始动画
     */
    public void startAnim() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float percent = (float) animation.getAnimatedValue();
                mPathPercent = percent;
                invalidate();
            }
        });
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(mDuration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();

        //再加一个旋转动画以及两倍的时长，形成旋转视差
        ObjectAnimator animRotate = ObjectAnimator.ofFloat(this, View.ROTATION, 0, 360);
        animRotate.setInterpolator(new LinearInterpolator());
        animRotate.setRepeatCount(ValueAnimator.INFINITE);
        animRotate.setDuration(mDuration * 2);
        animRotate.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDestPath.reset();
        //避免  硬件加速的BUG
        mDestPath.lineTo(0,0);
        float stop = mPathLength * mPathPercent;
        float start = (float) (float) (stop - ((0.5 - Math.abs(mPathPercent - 0.5)) * mPathLength * 4));

        mPathMeasure.getSegment(start, stop, mDestPath, true);
        canvas.drawPath(mDestPath, mPaint);

    }


    /**
     * 测量宽高
     *
     * @param measureSpec
     * @return
     */
    private int getMeasureSize(int measureSpec) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = getPaddingLeft() + getWindowWidth() / 10 + getPaddingRight();
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(size, result);
            }
        }
        return result;
    }

    /**
     * 获取屏幕的宽度
     *
     * @return
     */
    private int getWindowWidth() {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }
}
