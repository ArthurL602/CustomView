package com.ljb.lockpatternview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Author      :ljb
 * Date        :2019/10/3
 * Description :
 */
public class LockPatterViewV2 extends View {


    private int mCircleStrokeWidth = 10;
    private int mNormalColor = Color.BLUE;
    private int mPressColor = Color.YELLOW;
    private int mErrorColor = Color.RED;


    private Paint mInnerPaint;
    private Paint mOuterPaint;
    private Paint mLinePaint;
    private Paint mArrowPaint;

    private Point[][] mPoints = new Point[3][3];
    private List<Point> mSelectedPoints = new ArrayList<>();

    private boolean mIsDownInCircle = false;
    private int mArrowAngle = 38;
    private int mArrowHeight;
    private int mErrorDelay = 1000;

    private int mRate = 12;

    private int mRadius = 0;

    private OnLockResultListener mOnLockResultListener;

    public void setOnLockResultListener(OnLockResultListener onLockResultListener) {
        mOnLockResultListener = onLockResultListener;
    }

    public LockPatterViewV2(Context context) {
        this(context, null);
    }

    public LockPatterViewV2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockPatterViewV2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initAttrs(context, attrs);
        initPaint();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LockPatterViewV2);
        if (typedArray != null) {
            mCircleStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.LockPatterViewV2_circleStrokeWidth, mCircleStrokeWidth);
            mNormalColor = typedArray.getColor(R.styleable.LockPatterViewV2_normalColor, mNormalColor);
            mPressColor = typedArray.getColor(R.styleable.LockPatterViewV2_pressColor, mPressColor);
            mErrorColor = typedArray.getColor(R.styleable.LockPatterViewV2_errorColor, mErrorColor);
            mArrowAngle = typedArray.getInt(R.styleable.LockPatterViewV2_arrowAngle, mArrowAngle);
            mArrowHeight = typedArray.getDimensionPixelSize(R.styleable.LockPatterViewV2_arrowHeight, mArrowHeight);
            mRate = typedArray.getInt(R.styleable.LockPatterViewV2_rate, mRate);
            typedArray.recycle();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getCalculateWidth(widthMeasureSpec);
        int height = getCalculateHeight(heightMeasureSpec);
        setMeasuredDimension(Math.min(width, height), Math.min(width, height));
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            initPoints();
            mRadius = getWidth() / mRate;
            mArrowHeight = mRadius / 3;
        }
    }

    /**
     * 初始化Points
     */
    private void initPoints() {
        int width = getWidth();
        int height = width;
        for (int i = 0; i < mPoints.length; i++) {
            int y = height / 3 / 2 + i * height / 3;
            for (int j = 0; j < mPoints[i].length; j++) {
                int x = width / 3 / 2 + j * width / 3;
                int index = i * 3 + j;
                mPoints[i][j] = new Point(x, y, index);
            }
        }
    }

    private void initPaint() {
        //初始化画笔
        mInnerPaint = new Paint();
        mInnerPaint.setDither(true);
        mInnerPaint.setAntiAlias(true);
        mInnerPaint.setStrokeWidth(mCircleStrokeWidth);
        mInnerPaint.setColor(mNormalColor);
        mInnerPaint.setStyle(Paint.Style.STROKE);

        mOuterPaint = new Paint();
        mOuterPaint.setDither(true);
        mOuterPaint.setAntiAlias(true);
        mOuterPaint.setStrokeWidth(mCircleStrokeWidth);
        mOuterPaint.setColor(mNormalColor);
        mOuterPaint.setStyle(Paint.Style.STROKE);

        mLinePaint = new Paint();
        mLinePaint.setDither(true);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(mCircleStrokeWidth);
        mLinePaint.setColor(mPressColor);

        mArrowPaint = new Paint();
        mArrowPaint.setDither(true);
        mArrowPaint.setAntiAlias(true);
        mArrowPaint.setStrokeWidth(mCircleStrokeWidth);
        mArrowPaint.setColor(mPressColor);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制圆
        drawPoints(canvas);
        // 绘制线
        drawLines(canvas);
    }

    /**
     * 绘制线
     */
    private void drawLines(Canvas canvas) {
        if (!mIsDownInCircle || mSelectedPoints == null || mSelectedPoints.size() <= 0) return;
        Point lastPoint = mSelectedPoints.get(0);
        for (int i = 1; i < mSelectedPoints.size(); i++) {
            Point point = mSelectedPoints.get(i);
            //绘制线
            drawLine(lastPoint, point, canvas);
            // 绘制箭头

            drawArrow(lastPoint, point, canvas, mArrowHeight, mArrowAngle);
            lastPoint = point;
        }
        // 绘制到触摸点的距离
        boolean isInnerPoint = checkInRound(lastPoint.getCenterX(), lastPoint.getCenterY(), mDownX, mDownY, mRadius / 4);
        if (mIsDownInCircle && !isInnerPoint && !lastPoint.isErrorStatus()) {
            drawLine(lastPoint, new Point((int) mDownX, (int) mDownY, -1), canvas);
        }

    }

    /**
     * 绘制箭头
     */
    private void drawArrow(Point lastPoint, Point point, Canvas canvas, int arrowHeight, int arrowAngle) {
        if(lastPoint.isErrorStatus()){
            mArrowPaint.setColor(mErrorColor);
        }else{
            mArrowPaint.setColor(mPressColor);
        }
        double distance = getDistance(lastPoint.getCenterX(), lastPoint.getCenterY(), point.getCenterX(), point.getCenterY());
        float sin_a = (float) ((point.getCenterX() - lastPoint.getCenterX()) / distance);
        float cos_a = (float) ((point.getCenterY() - lastPoint.getCenterY()) / distance);


        double tan_a = Math.tan(Math.toRadians(arrowAngle));
        double h = (distance - arrowHeight - mRadius * 1.1);
        double l = arrowHeight * tan_a;
        double a = l * sin_a;
        double b = l * cos_a;
        double x0 = h * sin_a;
        double y0 = h * cos_a;
        float x1 = (float) (lastPoint.getCenterX() + (h + arrowHeight) * sin_a);
        float y1 = (float) (lastPoint.getCenterY() + (h + arrowHeight) * cos_a);
        float x2 = (float) (lastPoint.getCenterX() + x0 - b);
        float y2 = (float) (lastPoint.getCenterY() + y0 + a);
        float x3 = (float) (lastPoint.getCenterX() + x0 + b);
        float y3 = (float) (lastPoint.getCenterY() + y0 - a);
        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.close();

        canvas.drawPath(path, mArrowPaint);
    }

    private boolean checkInRound(int startX, int startY, float endX, float endY, int range) {
        double distance = getDistance(startX, startY, endX, endY);
        return range >= distance;
    }

    private double getDistance(int startX, int startY, float endX, float endY) {
        int dx = (int) (endX - startX);
        int dy = (int) (endY - startY);
        // 这里一点要注意精度，刚开始写的时候，没有注意精度，导致有部分线出来，其实是丢失精度导致的。
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void drawLine(Point startPoint, Point endPoint, Canvas canvas) {
        int dx = endPoint.getCenterX() - startPoint.getCenterX();
        int dy = endPoint.getCenterY() - startPoint.getCenterY();
        // 这里一点要注意精度，刚开始写的时候，没有注意精度，导致有部分线出来，其实是丢失精度导致的。
        double distance = Math.sqrt(dx * dx + dy * dy);

        float radius = mRadius / 6.0f;
        float rx = (float) (radius * (dx / distance));
        float ry = (float) (radius * (dy / distance));
        if(startPoint.isErrorStatus()){
            mLinePaint.setColor(mErrorColor);
        }else{
            mLinePaint.setColor(mPressColor);
        }
        canvas.drawLine(startPoint.getCenterX() + rx, startPoint.getCenterY() + ry, endPoint.getCenterX() - rx, endPoint.getCenterY() - ry, mLinePaint);
    }

    /**
     * 绘制点
     */
    private void drawPoints(Canvas canvas) {
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                Point point = mPoints[i][j];
                if (point != null) {
                    drawInner(point, canvas);
                    drawOuter(point, canvas);
                }
            }
        }
    }


    private float mDownX, mDownY;

    /**
     * 处理触摸事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDownX = event.getX();
        mDownY = event.getY();
        Point point;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(mIsDownInCircle) return false;
                mSelectedPoints.clear();
                mIsDownInCircle = false;
                point = calIsInnerCircle(mDownX, mDownY);
                if (point != null) {
                    mIsDownInCircle = true;
                    point.setPressStatus();
                    mSelectedPoints.add(point);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsDownInCircle) {
                    point = calIsInnerCircle(mDownX, mDownY);
                    if (point != null && !mSelectedPoints.contains(point)) {
                        point.setPressStatus();
                        mSelectedPoints.add(point);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                callbackResult();
                if (mOnLockResultListener == null) {
                    resetPoints();
                    mIsDownInCircle = false;
                }

                break;
        }
        invalidate();
        return true;
    }

    private void callbackResult() {
        if (mOnLockResultListener != null && mSelectedPoints != null && mSelectedPoints.size() > 0) {
            int[] results = new int[mSelectedPoints.size()];
            for (int i = 0; i < mSelectedPoints.size(); i++) {
                results[i] = mSelectedPoints.get(i).getIndex();
            }
            mOnLockResultListener.onResult(results);

        }
    }

    private int getCalculateHeight(int heightMeasureSpec) {
        int height;
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            height = getScreenHeight();
        }
        return height;
    }

    private int getCalculateWidth(int widthMeasureSpec) {
        int width;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            width = getScreenWidth();
        }
        return width;
    }

    private int getScreenWidth() {
        return getContext().getResources().getDisplayMetrics().widthPixels;
    }

    private int getScreenHeight() {
        return getContext().getResources().getDisplayMetrics().heightPixels;
    }


    public void executeErrorStatus() {
        if (!mIsDownInCircle || mSelectedPoints == null || mSelectedPoints.size() <= 0) return;
        for (Point selectedPoint : mSelectedPoints) {
            selectedPoint.setErrorStatus();
        }
        invalidate();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsDownInCircle = false;
                resetPoints();
                invalidate();
            }
        }, mErrorDelay);
    }

    private void resetPoints() {
        for (Point selectedPoint : mSelectedPoints) {
            selectedPoint.setNormalStatus();
        }
        mSelectedPoints.clear();
    }

    /**
     * 计算点是否在园内
     */
    private Point calIsInnerCircle(float downX, float downY) {
        int radius = mRadius;
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                int distance = (int) Math.sqrt((mPoints[i][j].getCenterX() - downX) * (mPoints[i][j].getCenterX() - downX) +
                        (mPoints[i][j].getCenterY() - downY) * (mPoints[i][j].getCenterY() - downY));
                if (distance < radius) {
                    return mPoints[i][j];
                }

            }
        }
        return null;
    }

    private void drawOuter(Point point, Canvas canvas) {
        drawPoint(point, canvas, mOuterPaint, mRadius / 6);
    }

    private void drawInner(Point point, Canvas canvas) {
        drawPoint(point, canvas, mInnerPaint, mRadius);
    }

    private void drawPoint(Point point, Canvas canvas, Paint paint, int radius) {
        if (point == null) return;
        if (point.isErrorStatus()) {
            paint.setColor(mErrorColor);
        } else if (point.isPressStatus()) {
            paint.setColor(mPressColor);
        } else {
            paint.setColor(mNormalColor);
        }
        canvas.drawCircle(point.getCenterX(), point.getCenterY(), radius, paint);
    }

    class Point {
        private int mCenterX;
        private int mCenterY;
        private int mIndex;


        public Point(int centerX, int centerY, int index) {
            mCenterX = centerX;
            mCenterY = centerY;
            mIndex = index;
        }


        public int getCenterX() {
            return mCenterX;
        }

        public int getCenterY() {
            return mCenterY;
        }

        public int getIndex() {
            return mIndex;
        }

        public int getStatus() {
            return mStatus;
        }

        private @Status
        int mStatus = Status.NORMAL;

        public void setNormalStatus() {
            mStatus = Status.NORMAL;
        }

        public void setPressStatus() {
            mStatus = Status.PRESS;
        }

        public void setErrorStatus() {
            mStatus = Status.ERROR;
        }

        public boolean isNormalStatus() {
            return mStatus == Status.NORMAL;
        }

        public boolean isPressStatus() {
            return mStatus == Status.PRESS;
        }

        public boolean isErrorStatus() {
            return mStatus == Status.ERROR;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Status.NORMAL, Status.PRESS, Status.ERROR})
    @interface Status {
        int NORMAL = 0;
        int PRESS = 1;
        int ERROR = 2;
    }

    public interface OnLockResultListener {
        void onResult(int[] result);
    }
}
