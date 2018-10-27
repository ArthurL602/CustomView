package com.ljb.qqstepview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * Author      :ljb
 * Date        :2018/2/11
 * Description : QQ步数计数器
 */

public class QQStepView extends View {

    //颜色
    private int mOuterColor = Color.BLUE;
    private int mInnerColor = Color.RED;
    private int mStepTextColor = Color.RED;
    //圆弧宽度
    private int mBorderWidth = 20;
    // 文本大小
    private int mStepTextSize;
    private Paint mArcPaint;
    private Paint mTextPaint;
    //最大步数
    private int mMaxStep = 100;
    //目标步数
    private int mTargetStep;
    //当前步数
    private int mCurrentStep;
    
    //插值器回调接口
    private InterpolaterListener mInterpolaterListener;
    private int mDuration = 1000;

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public void setInterpolaterListener(InterpolaterListener interpolaterListener) {
        mInterpolaterListener = interpolaterListener;
    }

    public QQStepView(Context context) {
        this(context, null);
    }

    public QQStepView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QQStepView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBorderWidth = (int) dp2px(6);
        mStepTextSize = (int) sp2px(20);
        initAttr(context, attrs);
        init();
    }

    private void init() {
        mArcPaint = new Paint();
        mArcPaint.setStrokeWidth(mBorderWidth);
        mArcPaint.setStrokeJoin(Paint.Join.ROUND);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint();
        mTextPaint.setColor(mStepTextColor);
        mTextPaint.setTextSize(mStepTextSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //设置onMeasure() 应对 wrap_content 和宽度和高度不一致

        int width = getDefault_Size(widthMeasureSpec);
        int height = getDefault_Size(heightMeasureSpec);
        //取两者最小值，确保是个正方形
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);

    }

    /**
     * 设置目标步数
     * @param targetStep
     */
    public void setTargetStep(int targetStep) {
        mTargetStep = targetStep;
    }

    /**
     * 设置最大步数
     * @param maxStep
     */
    public void setMaxStep(int maxStep) {
        mMaxStep = maxStep;
    }

    /**
     * 获取默认大小
     *
     * @param measureSpec
     * @return
     */

    private int getDefault_Size(int measureSpec) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            //默认大小
            int defaultSize = (int) dp2px(40);
            result = defaultSize;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(defaultSize, size);
            }
        }
        return result;
    }

    /**
     * 实例化自定义属性
     *
     * @param context
     * @param attrs
     */
    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.QQStepView);
        mOuterColor = ta.getColor(R.styleable.QQStepView_outerColor, mOuterColor);
        mInnerColor = ta.getColor(R.styleable.QQStepView_innerColor, mInnerColor);
        mStepTextColor = ta.getColor(R.styleable.QQStepView_stepTextColor, mStepTextColor);

        mStepTextSize = (int) ta.getDimension(R.styleable.QQStepView_stepTextSize, mStepTextSize);
        mBorderWidth = (int) ta.getDimension(R.styleable.QQStepView_borderWidth, mBorderWidth);

        ta.recycle();
    }

    private boolean mAnimStart;

    @Override
    protected void onDraw(Canvas canvas) {
        RectF rectF = new RectF(mBorderWidth, mBorderWidth, getWidth() - mBorderWidth, getHeight() - mBorderWidth);
        //绘制外圆弧
        mArcPaint.setColor(mOuterColor);
        canvas.drawArc(rectF, 135, 270, false, mArcPaint);
        //绘制内圆弧
        if (mMaxStep == 0) {
            return;
        }
        mArcPaint.setColor(mInnerColor);
        float percent = mCurrentStep / (mMaxStep * 1.0f);
        canvas.drawArc(rectF, 135, percent * 270, false, mArcPaint);

        //绘制文字
        String text = mCurrentStep + "";
        int width = (int) (getWidth() / 2 - mTextPaint.measureText(text) / 2);
        Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
        int height = (int) (getHeight() / 2 + (Math.abs(metrics.ascent) - metrics.descent) / 2);

        canvas.drawText(text, width, height, mTextPaint);
        //开始动画
        if (!mAnimStart) {
            mAnimStart=true;
            setProgress();
        }
    }

    /**
     * 设置进度
     */
    private void setProgress() {
        ValueAnimator animator = ValueAnimator.ofInt(0, mTargetStep);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentStep = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        if (mInterpolaterListener != null) {
            animator.setInterpolator(mInterpolaterListener.getInterpolater());
        }
        animator.setDuration(mDuration);
        animator.start();
    }

    private float dp2px(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private float sp2px(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }

    public interface InterpolaterListener {
        Interpolator getInterpolater();
    }
}
