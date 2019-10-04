package com.ljb.dragbubble;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
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
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

/**
 * Author      :ljb
 * Date        :2019/10/4
 * Description :
 */

public class MessageBubbleView extends View {


    private static final long REBOUND_ANIMATOR_DURATION = 250;
    private static final long EXPLOSION_ANIMATOR_DURATION = 350;
    private Paint mPaint;
    private int mMessageColor = Color.RED;

    private PointF mDragPoint;
    private PointF mFixedPoint;

    private int mDragRadius = 14;

    private int mFixedRadiusMax = 7  ;
    private int mFixedRadiusMin = 3;
    private int mFixedRadius;
    private Path mBeizerPath;
    private Bitmap mTargetBit;

    private boolean mIsExplosionState = false;
    private boolean mIsMessageDismiss = false;
    private OnMessageBubbleStateListener mOnMessageBubbleStateListener;

    private Bitmap[] mBitmaps;
    private int[] mExplosionIds = {
            R.drawable.explosion_one,
            R.drawable.explosion_two,
            R.drawable.explosion_three,
            R.drawable.explosion_four,
            R.drawable.explosion_five
    };

    private int mCurrentBitIndex = -1;

    private ValueAnimator mExplosionAnimator;
    private ValueAnimator mReboundAnimator;

    public void setOnMessageBubbleStateListener(OnMessageBubbleStateListener onMessageBubbleStateListener) {
        mOnMessageBubbleStateListener = onMessageBubbleStateListener;
    }

    public MessageBubbleView(Context context) {
        this(context, null);
    }

    public MessageBubbleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageBubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        mDragRadius = dp2px(mDragRadius);
        mFixedRadiusMax = dp2px(mFixedRadiusMax);
        mFixedRadiusMin = dp2px(mFixedRadiusMin);
        initAttrs(context, attrs);

        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setColor(mMessageColor);
        initBit(context);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MessageBubbleView);
        if (typedArray != null) {
            mDragRadius = typedArray.getDimensionPixelOffset(R.styleable.MessageBubbleView_dragViewRadius, mDragRadius);
            mMessageColor = typedArray.getColor(R.styleable.MessageBubbleView_dragViewColor, mMessageColor);
            typedArray.recycle();
        }
    }

    private void initBit(Context context) {
        mBitmaps = new Bitmap[mExplosionIds.length];
        for (int i = 0; i < mExplosionIds.length; i++) {
            mBitmaps[i] = BitmapFactory.decodeResource(context.getResources(), mExplosionIds[i]);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mIsExplosionState) {
            // 绘制爆炸效果
            drawExplosion(canvas);
            return;
        }
        if (mIsMessageDismiss) return;
        // 绘制能够拖动的圆
        drawDragCircle(canvas);
        // 绘制固定的圆
        drawFixedCircle(canvas);
        // 绘制贝塞尔曲线
        drawBeizer(canvas);
        // 绘制 bit
        drawTargetBit(canvas);
    }

    /**
     * 绘制爆炸效果
     */
    private void drawExplosion(Canvas canvas) {
        if (mDragPoint == null || mCurrentBitIndex == -1) return;
        Bitmap bit = mBitmaps[mCurrentBitIndex];
        RectF rectF = new RectF(mDragPoint.x - mDragRadius, mDragPoint.y - mDragRadius, mDragPoint.x
                + mDragRadius, mDragPoint.y + mDragRadius);
        canvas.drawBitmap(bit, null, rectF, null);
    }

    private void drawTargetBit(Canvas canvas) {
        if (mTargetBit == null || mDragPoint == null) return;
        float x = mDragPoint.x - mTargetBit.getWidth() / 2;
        float y = mDragPoint.y - mTargetBit.getHeight() / 2;
        canvas.drawBitmap(mTargetBit, x, y, null);
    }

    /**
     * 绘制贝塞尔曲线
     */
    private void drawBeizer(Canvas canvas) {
        if (mDragPoint == null || mFixedPoint == null) return;
        //获取两点之间的距离
        double pointDistance = getDistance(mFixedPoint.x, mFixedPoint.y, mDragPoint.x, mDragPoint.y);
        if (pointDistance == 0 || mFixedRadius < mFixedRadiusMin) return;
        float dx = mDragPoint.x - mFixedPoint.x;
        float dy = mDragPoint.y - mFixedPoint.y;


        double sinA = dy / pointDistance;
        double cosA = dx / pointDistance;
        // p0
        float p0_x = (float) (mFixedPoint.x + mFixedRadius * sinA);
        float p0_y = (float) (mFixedPoint.y - mFixedRadius * cosA);
        // p1
        float p1_x = (float) (mDragPoint.x + mDragRadius * sinA);
        float p1_y = (float) (mDragPoint.y - mDragRadius * cosA);
        // p2
        float p2_x = (float) (mDragPoint.x - mDragRadius * sinA);
        float p2_y = (float) (mDragPoint.y + mDragRadius * cosA);
        // p3
        float p3_x = (float) (mFixedPoint.x - mFixedRadius * sinA);
        float p3_y = (float) (mFixedPoint.y + mFixedRadius * cosA);
        // control point
        float controlX = (mFixedPoint.x + mDragPoint.x) / 2;
        float controlY = (mFixedPoint.y + mDragPoint.y) / 2;
        if (mBeizerPath == null) {
            mBeizerPath = new Path();
        } else {
            mBeizerPath.reset();
        }
        mBeizerPath.moveTo(p0_x, p0_y);
        mBeizerPath.quadTo(controlX, controlY, p1_x, p1_y);
        mBeizerPath.lineTo(p2_x, p2_y);
        mBeizerPath.quadTo(controlX, controlY, p3_x, p3_y);
        mBeizerPath.close();
        canvas.drawPath(mBeizerPath, mPaint);

    }

    /**
     * 绘制固定圆
     */
    private void drawFixedCircle(Canvas canvas) {
        if (mDragPoint == null || mFixedPoint == null) return;
        //获取两点之间的距离
        double pointDistance = getDistance(mFixedPoint.x, mFixedPoint.y, mDragPoint.x, mDragPoint.y);
        mFixedRadius = (int) (mFixedRadiusMax - pointDistance / 14);
        if (mFixedRadius < mFixedRadiusMin) return;
        drawCircle(canvas, mFixedPoint, mFixedRadius);
    }

    /**
     * 绘制拖拽圆
     *
     * @param canvas
     */
    private void drawDragCircle(Canvas canvas) {
        drawCircle(canvas, mDragPoint, mDragRadius);
    }

    private void drawCircle(Canvas canvas, PointF pointF, int radius) {
        if (pointF == null) return;
        float centerX = pointF.x;
        float centerY = pointF.y;
        canvas.drawCircle(centerX, centerY, radius, mPaint);
    }

  /*  @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 初始化或者重新设置PointF的坐标
                initOrResetPoint(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                // 更新DragPointF的坐标
                updateDragPoint(x, y);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        invalidate();
        return true;
    }
*/


    public void handleActionUp() {
        double pointDistance = getDistance(mFixedPoint.x, mFixedPoint.y, mDragPoint.x, mDragPoint.y);
        mFixedRadius = (int) (mFixedRadiusMax - pointDistance / 14);
        if (mFixedRadius >= mFixedRadiusMin) {
            // show 回弹效果 ，需要一个从当前拖动位置回到固定位置的 全程point
            getReboundPoint(mDragPoint, mFixedPoint);
        } else {
            // show 爆炸效果
            showExplosionEffect();
        }
    }

    //展示爆炸效果
    private void showExplosionEffect() {
        if (mExplosionAnimator == null) {
            mExplosionAnimator = ValueAnimator.ofInt(0, mExplosionIds.length - 1);
            mExplosionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    mCurrentBitIndex = value;
                    invalidate();
                }
            });
            mExplosionAnimator.setInterpolator(new LinearInterpolator());
            mExplosionAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCurrentBitIndex = -1;
                    mIsExplosionState = false;
                    mIsMessageDismiss = true;
                    if (mOnMessageBubbleStateListener != null) {
                        mOnMessageBubbleStateListener.onDismiss();
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    mIsExplosionState = true;
                }
            });
            mExplosionAnimator.setDuration(EXPLOSION_ANIMATOR_DURATION);
        }
        mExplosionAnimator.start();
    }

    /**
     * 获取回弹路径上的point
     */
    private void getReboundPoint(final PointF endPoint, final PointF startPoint) {
        if (mReboundAnimator == null) {
            mReboundAnimator = ObjectAnimator.ofFloat(1f);
            mReboundAnimator.setDuration(REBOUND_ANIMATOR_DURATION);
            mReboundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    Log.d("TAG_F", "(MessageBubbleView.java:305) onAnimationUpdate() value - > "+value);
                    float x = startPoint.x + (endPoint.x - startPoint.x) * value;
                    float y = startPoint.y + (endPoint.y - startPoint.y) * value;
                    updateDragPoint(x, y);
                }
            });
            mReboundAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // 监听进行恢复
                    if (mOnMessageBubbleStateListener != null) {
                        mOnMessageBubbleStateListener.onRestore();
                    }
                }
            });
            mReboundAnimator.setInterpolator(new BounceInterpolator());
        }
        mReboundAnimator.start();

    }

    /**
     * 计算两个点之间的距离
     */
    private double getDistance(float startX, float startY, float endX, float endY) {
        float dx = endX - startX;
        float dy = endY - startY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void updateDragPoint(float x, float y) {
        if (mDragPoint == null) {
            mDragPoint = new PointF(x, y);
        } else {
            mDragPoint.set(x, y);
        }
        invalidate();
    }

    public void initOrResetPoint(float x, float y) {
        mIsExplosionState = false;
        mIsMessageDismiss = false;
        if (mDragPoint == null) {
            mDragPoint = new PointF(x, y);
        } else {
            mDragPoint.set(x, y);
        }
        if (mFixedPoint == null) {
            mFixedPoint = new PointF(x, y);
        } else {
            mFixedPoint.set(x, y);
        }
        invalidate();
    }

    /**
     * 绑定目标View
     *
     * @param targetView
     */
    public static void attach(TextView targetView, MessageBubbleTouchListener.OnBubbleDismissListener dismissListener) {
        MessageBubbleTouchListener touchListener = new MessageBubbleTouchListener(targetView, dismissListener);
        targetView.setOnTouchListener(touchListener);
    }

    private int dp2px(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    public void setTargetBitmap(Bitmap bitmap) {
        mTargetBit = bitmap;
        invalidate();
    }


    public interface OnMessageBubbleStateListener {
        void onRestore();

        void onDismiss();
    }

}
