package com.ljb.dragbubble;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;

import com.ljb.dragbubble.R;

/**
 * Author      :ljb
 * Date        :2018/1/31
 * Description : 拖拽气泡
 */

public class DragBubble extends View {
    //气泡的状态
    private int mState;
    //默认，无法拖拽
    private static final int STATE_DEFAULT = 0x00;
    //拖拽
    private static final int STATE_DRAG = 0x01;
    //移动
    private static final int STATE_MOVE = 0x02;
    // 消失
    private static final int STATE_DISMISS = 0x03;

    // 拖拽圆和固定圆的坐标值
    private PointF mFixationPoint, mDragPoint;
    // 拖拽圆的半径
    private float mDragCircleRadius = 20;
    //固定圆的半径
    private float mFixationCircleRadius;
    // 固定圆的最大半径
    private float mFixationCircleRadiusMax = 20;
    //固定圆的最小半径
    private float mFixationCircleRadiusMin = 6;
    //画笔
    private Paint mPaint;
    private Paint mTextPaint;
    //两个圆的圆心距离
    private double mDistance;
    // 气泡爆炸的图片id
    private int[] mExplosionDrawables = {R.drawable.explosion_one, R.drawable.explosion_two, R.drawable
            .explosion_three, R.drawable.explosion_four, R.drawable.explosion_five};
    private Bitmap[] mExplosionBitmap;
    //当前爆炸图片索引
    private int mCurrentExplosionIndex;
    //控制点坐标
    private float mControlX;
    private float mControlY;
    //爆炸动画是否开始
    private boolean mIsExplosionAnimStart;
    private String mDefaultText = "1";
    private float mDefaultTextSize = 20;
    //字体颜色
    private int mDefaultTextColor;
    private int mWidth;
    private int mHeight;
    private OnBubbleStateListener mOnBubbleStateListener;

    public void setOnBubbleStateListener(OnBubbleStateListener onBubbleStateListener) {
        mOnBubbleStateListener = onBubbleStateListener;
    }

    public DragBubble(Context context) {
        this(context, null);
    }

    public DragBubble(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragBubble(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initAttrs(context, attrs, defStyleAttr);
    }

    /**
     * 设置字体颜色
     *
     * @param defaultText
     */
    public void setDefaultText(String defaultText) {
        mDefaultText = defaultText;
        invalidate();
    }

    private int mCircleColor;

    /**
     * 实例化自定义属性
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DragBubble, defStyleAttr, 0);
        mDragCircleRadius = ta.getDimension(R.styleable.DragBubble_dragCircleRadius, mDragCircleRadius);
        mCircleColor = ta.getColor(R.styleable.DragBubble_dragCircleColor, Color.RED);
        mDefaultText = ta.getString(R.styleable.DragBubble_text);
        mDefaultTextSize = ta.getDimension(R.styleable.DragBubble_textSize, mDefaultTextSize);
        mDefaultTextColor = ta.getColor(R.styleable.DragBubble_textColor, Color.WHITE);
        ta.recycle();

        mPaint = new Paint();
        mPaint.setColor(mCircleColor);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        mTextPaint = new Paint();
        mTextPaint.setColor(mDefaultTextColor);
        mTextPaint.setTextSize(mDefaultTextSize);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float cx = w / 2;
        float cy = h / 2;
        mFixationPoint.set(cx, cy);
        mDragPoint.set(cx, cy);
    }

    private void init() {
        mFixationPoint = new PointF();
        mDragPoint = new PointF();

        mFixationCircleRadiusMax = dip2px((int) mFixationCircleRadiusMax);
        mDragCircleRadius = dip2px((int) mDragCircleRadius);
        mFixationCircleRadiusMin = dip2px((int) mFixationCircleRadiusMin);
        mDefaultTextSize = dip2px((int) mDefaultTextSize);

        mExplosionBitmap = new Bitmap[mExplosionDrawables.length];
        for (int i = 0; i < mExplosionDrawables.length; i++) {
            mExplosionBitmap[i] = BitmapFactory.decodeResource(getResources(), mExplosionDrawables[i]);
        }
    }

    /**
     * dp转化成像素px
     *
     * @param value
     * @return
     */
    private int dip2px(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mState != STATE_DISMISS) {
            //绘制拖拽圆
            canvas.drawCircle(mDragPoint.x, mDragPoint.y, mDragCircleRadius, mPaint);
        }
        if (mState == STATE_DRAG) {
            //绘制固定圆
            canvas.drawCircle(mFixationPoint.x, mFixationPoint.y, mFixationCircleRadius, mPaint);
            //绘制贝塞尔曲线
            Path bezierPath = getBezierPath();
            canvas.drawPath(bezierPath, mPaint);
        }
        //绘制爆炸动画
        if (mIsExplosionAnimStart && mCurrentExplosionIndex < mExplosionDrawables.length) {
            RectF rectF = new RectF(mDragPoint.x - mDragCircleRadius, mDragPoint.y - mDragCircleRadius, mDragPoint.x
                    + mDragCircleRadius, mDragPoint.y + mDragCircleRadius);
            canvas.drawBitmap(mExplosionBitmap[mCurrentExplosionIndex], null, rectF, mPaint);
        }
        //绘制消息数文本
        if (mState != STATE_DISMISS && !TextUtils.isEmpty(mDefaultText)) {
            float width = mTextPaint.measureText(mDefaultText);
            Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
            float height = (Math.abs(metrics.ascent) - metrics.descent);
            canvas.drawText(mDefaultText, mDragPoint.x - width / 2, mDragPoint.y + height / 2, mTextPaint);
        }
    }

    /**
     * 获取贝塞尔曲线
     *
     * @return
     */
    private Path getBezierPath() {
        //计算控制点
        mControlX = (mFixationPoint.x + mDragPoint.x) / 2;
        mControlY = (mFixationPoint.y + mDragPoint.y) / 2;
        // 计算tanA;
        float dx = mDragPoint.x - mFixationPoint.x;
        float dy = mDragPoint.y - mFixationPoint.y;
        float tanA = dy / dx;
        double arcTanA = Math.atan(tanA);

        //计算四个初始点的坐标
        //p0
        float p0x = (float) (mFixationPoint.x + mFixationCircleRadius * Math.sin(arcTanA));
        float p0y = (float) (mFixationPoint.y - mFixationCircleRadius * Math.cos(arcTanA));
        //p1
        float p1x = (float) (mDragPoint.x + mDragCircleRadius * Math.sin(arcTanA));
        float p1y = (float) (mDragPoint.y - mDragCircleRadius * Math.cos(arcTanA));
        //p2
        float p2x = (float) (mDragPoint.x - mDragCircleRadius * Math.sin(arcTanA));
        float p2y = (float) (mDragPoint.y + mDragCircleRadius * Math.cos(arcTanA));
        //p3
        float p3x = (float) (mFixationPoint.x - mFixationCircleRadius * Math.sin(arcTanA));
        float p3y = (float) (mFixationPoint.y + mFixationCircleRadius * Math.cos(arcTanA));
        Path bezierPath = new Path();
        bezierPath.moveTo(p0x, p0y);
        bezierPath.quadTo(mControlX, mControlY, p1x, p1y);
        bezierPath.lineTo(p2x, p2y);
        bezierPath.quadTo(mControlX, mControlY, p3x, p3y);
        bezierPath.close();
        return bezierPath;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = getMeasuredDimension(heightMeasureSpec);
        mWidth = getMeasuredDimension(widthMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    private int getMeasuredDimension(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) (2 * mDragCircleRadius);
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(specSize, result);
            }
        }
        return result;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mState != STATE_DISMISS) {
                    float downX = event.getX();
                    float downY = event.getY();
                    //Math.hypot(double x, double y) 等价于 sqrt(x2 +y2)
                    //计算手指摁下的位置到固定圆心的距离
                    float d = (float) Math.hypot(downX - mFixationPoint.x, downY - mFixationPoint.y);
                    //判断是否在圆内
                    if (d <= mFixationCircleRadiusMax) {
                        //在圆内才能拖动
                        mState = STATE_DRAG;
                    } else {//不可拖拽
                        mState = STATE_DEFAULT;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mState != STATE_DEFAULT) {
                    float moveX = event.getX();
                    float moveY = event.getY();
                    //更新拖拽圆的圆心位置
                    updateDragCirclePoint(moveX, moveY);
                    //获取两个圆的圆心距离
                    mDistance = getDistance();
                    //计算固定圆的半径
                    mFixationCircleRadius = (int) (mFixationCircleRadiusMax - mDistance / 14);

                    if (mState == STATE_DRAG) {//可拖拽
                        //如果距离小于可黏连的最大距离,即固定圆的半径大于固定圆的最小半径
                        //如果小于固定圆的最小半径，则不再绘制固定圆和贝塞尔曲线
                        if (mFixationCircleRadius < mFixationCircleRadiusMin) {
                            mState = STATE_MOVE;//改为移动状态
                            if (mOnBubbleStateListener != null) {
                                mOnBubbleStateListener.onMove();
                            }
                        } else {
                            if (mOnBubbleStateListener != null) {
                                mOnBubbleStateListener.onDrag();
                            }
                        }

                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mState == STATE_DRAG) {//如果在拖拽的时候，即固定圆和贝塞尔曲线都在的时候
                    setBubbleRestoreAnim();
                } else if (mState == STATE_MOVE) {//如果在拖拽圆移动的时候，即固定圆和贝塞尔曲线都不存在的时候
                    if (mFixationCircleRadius >= mFixationCircleRadiusMin) {
                        //如果在固定圆最小半径范围内，则恢复原位
                        setBubbleRestoreAnim();
                    } else {
                        //设置气泡消失
                        setBubbleDismissAnim();
                    }
                }
                break;
        }
        invalidate();
        return true;
    }

    /**
     * 设置气泡消失动画
     */
    private void setBubbleDismissAnim() {
        mState = STATE_DISMISS;
        mIsExplosionAnimStart = true;
        //这里不能到mExplosionDrawables.length-1，因为最后爆炸完毕，不需要在绘制图片，所以必须
        //为mExplosionDrawables.length-1，最后index= mExplosionDrawables.length的时候，不再绘制
        //图片。
        ValueAnimator animator = ValueAnimator.ofInt(0, mExplosionDrawables.length);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentExplosionIndex = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.setDuration(500).start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIsExplosionAnimStart = false;
                if (mOnBubbleStateListener != null) {
                    mOnBubbleStateListener.onDismiss();
                }
            }
        });
    }

    /**
     * 设置气泡恢复动画
     */
    private void setBubbleRestoreAnim() {
        if (mDragPoint.x == mFixationPoint.x) {
            // 避免出现 tanA = y/0；
            mDragPoint.x += 1;
        }
        final float tanA = (mDragPoint.y - mFixationPoint.y) / (mDragPoint.x - mFixationPoint.x);
        ValueAnimator animator = ValueAnimator.ofFloat(mDragPoint.x, mFixationPoint.x);
        //设置回弹效果
        animator.setInterpolator(new BounceInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float x = (float) animation.getAnimatedValue();
                float y = mFixationPoint.y + (x - mFixationPoint.x) * tanA;
                mDragPoint.set(x, y);
                invalidate();
            }
        });
        animator.setDuration(500).start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mState = STATE_DEFAULT;
                if (mOnBubbleStateListener != null) {
                    mOnBubbleStateListener.onRestore();
                }
            }
        });

    }


    /**
     * 更新拖拽圆的圆心位置
     *
     * @param moveX
     * @param moveY
     */
    private void updateDragCirclePoint(float moveX, float moveY) {
        mDragPoint.set(moveX, moveY);
    }

    /**
     * 返回两个圆的圆心的距离
     *
     * @return
     */
    public double getDistance() {
        return Math.hypot(mDragPoint.x - mFixationPoint.x, mDragPoint.y - mFixationPoint.y);
    }

    public void reCreate() {
        mFixationPoint.set(getWidth() / 2, getHeight() / 2);
        mDragPoint.set(getWidth() / 2, getHeight() / 2);
        mState = STATE_DEFAULT;
        invalidate();
    }

    /**
     * 气泡状态的监听器
     */
    public interface OnBubbleStateListener {
        /**
         * 拖拽气泡
         */
        void onDrag();

        /**
         * 移动气泡
         */
        void onMove();

        /**
         * 气泡恢复原来位置
         */
        void onRestore();

        /**
         * 气泡消失
         */
        void onDismiss();
    }
}
