package com.ljb.lockpatternview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;


import com.ljb.lockpatternview.entity.Circle;

import java.util.ArrayList;

/**
 * Author      :    ljb
 * Date        :    2018/2/7
 * Description :    九宫格自定义View
 */

public class LockPatternView extends View {

    private boolean mIsInit = false;
    //canvas 的宽高
    private int width;
    private int height;
    //画笔
    private Paint mLinePaint;
    private Paint mPressedPaint;
    private Paint mNormalPaint;
    private Paint mErrorPaint;
    private Paint mArrowPaint;
    //颜色
    public static int OUTTER_PRESSED_COLOR = 0xff8cbad8;
    public static int INNER_PRESSED_COLOR = 0xff0596f6;
    public static int OUTTER_NORMAL_COLOR = 0xffd9d9d9;
    public static int INNER_NORMAL_COLOR = 0xff929292;
    public static int OUTTER_ERROR_COLOR = 0xff901032;
    public static int INNER_ERROR_COLOR = 0xffea0945;
    //外圆半径
    private int mDotRadius;
    //储存圆的圆心位置以及状态的二维数组
    private Circle[][] mCircles;
    //按下的时候，是否按在一个点上
    private boolean mIsTouchPoint = false;
    // 选中的所有点
    private ArrayList<Circle> mSelectedPoint = new ArrayList<>();
    //手指按的坐标
    private float mX;
    private float mY;
    //选择错误
    private boolean mIsError;

    private LockPatternListener mLockPatternListener;
    private String mDefaultLock = "012543678";
    //是否要外圆
    private boolean mUseOutDot;


    public void setLockPatternListener(LockPatternListener lockPatternListener) {
        mLockPatternListener = lockPatternListener;
    }

    public LockPatternView(Context context) {
        this(context, null);
    }

    public LockPatternView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockPatternView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    /**
     * 实例化自定义属性
     *
     * @param context
     * @param attrs
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LockPatternView);
        OUTTER_NORMAL_COLOR = ta.getColor(R.styleable.LockPatternView_out_normal_color, OUTTER_NORMAL_COLOR);
        OUTTER_PRESSED_COLOR = ta.getColor(R.styleable.LockPatternView_out_pressed_color, OUTTER_PRESSED_COLOR);
        OUTTER_ERROR_COLOR = ta.getColor(R.styleable.LockPatternView_out_error_color, OUTTER_ERROR_COLOR);

        INNER_NORMAL_COLOR = ta.getColor(R.styleable.LockPatternView_inner_normal_color, INNER_NORMAL_COLOR);
        INNER_PRESSED_COLOR = ta.getColor(R.styleable.LockPatternView_inner_pressed_color, INNER_PRESSED_COLOR);
        INNER_ERROR_COLOR = ta.getColor(R.styleable.LockPatternView_inner_error_color, INNER_ERROR_COLOR);

        mDotRadius = (int) ta.getDimension(R.styleable.LockPatternView_dotRadius, 0);

        mDefaultLock = ta.getString(R.styleable.LockPatternView_defaultLock);

        mUseOutDot=ta.getBoolean(R.styleable.LockPatternView_use_out_dot,true);

        if (TextUtils.isEmpty(mDefaultLock)) {
            mDefaultLock = "012543678";
        }

        ta.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        if (!mIsInit) {
            initDot();
            initPaint();
            mIsInit=true;
        }

    }

    /**
     * 实例化画笔
     */
    private void initPaint() {
        //线的画笔
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(INNER_PRESSED_COLOR);
        mLinePaint.setStrokeWidth(mDotRadius / 10);
        mLinePaint.setStyle(Paint.Style.STROKE);
        //按下的画笔
        mPressedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPressedPaint.setStrokeWidth(mDotRadius / 6);
        mPressedPaint.setStyle(Paint.Style.STROKE);
        //错误的画笔
        mErrorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mErrorPaint.setStrokeWidth(mDotRadius / 6);
        mErrorPaint.setStyle(Paint.Style.STROKE);
        //默认的画笔
        mNormalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNormalPaint.setStrokeWidth(mDotRadius / 9);
        mNormalPaint.setStyle(Paint.Style.STROKE);
        //箭头的画笔
        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPaint.setStyle(Paint.Style.FILL);
        mArrowPaint.setColor(INNER_PRESSED_COLOR);
    }


    /**
     * 实例化点
     */
    private void initDot() {
        int index = 0;
        mCircles = new Circle[3][3];
        int offsetX = 0;
        int offsetY = 0;
        if (height > width) {//竖屏
            offsetY = (height - width) / 2;
        } else {//横屏
            offsetX = (width - height) / 2;
            //这里的目的是取height和width的最小值
            //因为后面需要用这两者的最小值来计算圆的半径和圆心坐标值
            width = height;
        }
        float squareWidth = width / 3;
        if(mDotRadius==0){
            mDotRadius = width / 8;
        }

        for (int i = 0; i < mCircles.length; i++) {
            // 同一行，Y轴相等
            int y = (int) (offsetY + squareWidth / 2 + squareWidth * i);
            Circle[] circles = mCircles[i];
            for (int j = 0; j < circles.length; j++) {
                int x = (int) (offsetX + squareWidth / 2 + squareWidth * j);
                mCircles[i][j] = new Circle(x, y, index);
                index++;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        showDot(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mX = event.getX();
        mY = event.getY();
        //判断手指是否按在九宫格上
        Circle circle = checkInRound(mX, mY);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (circle != null && !mIsError) {
                    mIsTouchPoint = true;
                    //改变当前点的状态
                    circle.setStatus(Circle.STATUS_PRESSED);
                    mSelectedPoint.add(circle);

                }
                break;
            case MotionEvent.ACTION_MOVE:
                //按下的时候一定要在一个点上，不断触摸的时候不断去判断新的点
                if (mIsTouchPoint && !mIsError) {
                    if (circle != null) {
                        //判断是否已经按下过这个点
                        if (!mSelectedPoint.contains(circle)) {
                            mSelectedPoint.add(circle);
                        }
                        circle.setStatus(Circle.STATUS_PRESSED);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mIsTouchPoint = false;
                //回调密码获取监听
                if (mSelectedPoint.size() >= 1 && !mIsError) {
                    String lock = getLockData();
                    if (mLockPatternListener != null) {
                        mLockPatternListener.lock(lock);
                    } else {
                        if (!lock.equals(mDefaultLock)) {
                            showSelectError();
                        } else {
                            //清空选择
                            clearSelectPoints();
                            Toast.makeText(getContext(), "密码正确", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
        invalidate();
        return true;
    }

    /**
     * 获取锁的数据
     *
     * @return
     */
    private String getLockData() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mSelectedPoint.size(); i++) {
            sb.append(mSelectedPoint.get(i).getIndex());
        }
        return sb.toString();
    }

    /**
     * 显示选择错误
     */
    public void showSelectError() {
        for (Circle circle : mSelectedPoint) {
            mIsError = true;
            circle.setStatus(Circle.STATUS_ERROR);
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                clearSelectPoints();
                mIsError = false;
                invalidate();
            }
        }, 1000);

    }

    /**
     * 清空选择
     */
    public void clearSelectPoints() {
        for (Circle circle : mSelectedPoint) {
            circle.setStatus(Circle.STATUS_NORAML);
        }
        mSelectedPoint.clear();
    }

    /**
     * 判断手指是否按在九宫格上
     * 判断准则： 点到圆心的距离<半径
     *
     * @param x
     * @param y
     * @return 返回按下的点
     */
    private Circle checkInRound(float x, float y) {
        for (int i = 0; i < mCircles.length; i++) {
            for (int j = 0; j < mCircles[i].length; j++) {
                Circle circle = mCircles[i][j];
                //计算按下的点到九宫格圆心的距离
                double distance = Math.sqrt(Math.pow(x - circle.getCenterX(), 2) + Math.pow(y - circle.getCenterY(),
                        2));
                if (distance < mDotRadius) {
                    return circle;
                }
            }
        }
        return null;
    }

    /**
     * 显示点
     * @param canvas
     */
    private void showDot(Canvas canvas) {
        for (int i = 0; i < mCircles.length; i++)
            for (int j = 0; j < mCircles[i].length; j++) {
                Circle circle = mCircles[i][j];
                //绘制正常状态下的圆
                if (circle.getStatus() == Circle.STATUS_NORAML) {
                    if(mUseOutDot){
                        // 绘制外圆
                        mNormalPaint.setColor(OUTTER_NORMAL_COLOR);
                        mNormalPaint.setStyle(Paint.Style.STROKE);
                        canvas.drawCircle(circle.getCenterX(), circle.getCenterY(), mDotRadius, mNormalPaint);
                    }

                    //绘制内圆
                    mNormalPaint.setStyle(Paint.Style.FILL);
                    mNormalPaint.setColor(INNER_NORMAL_COLOR);
                    canvas.drawCircle(circle.getCenterX(), circle.getCenterY(), mDotRadius / 6, mNormalPaint);
                }
                //绘制按下状态的圆
                if (circle.getStatus() == Circle.STATUS_PRESSED) {
                    if(mUseOutDot){
                        // 绘制外圆
                        mPressedPaint.setStyle(Paint.Style.STROKE);
                        mPressedPaint.setColor(OUTTER_PRESSED_COLOR);
                        canvas.drawCircle(circle.getCenterX(), circle.getCenterY(), mDotRadius, mPressedPaint);
                    }
                    //绘制内圆
                    mPressedPaint.setStyle(Paint.Style.FILL);
                    mPressedPaint.setColor(INNER_PRESSED_COLOR);
                    canvas.drawCircle(circle.getCenterX(), circle.getCenterY(), mDotRadius / 6, mPressedPaint);
                }
                //绘制异常状态下的圆
                if (circle.getStatus() == Circle.STATUS_ERROR) {
                    if(mUseOutDot){
                        // 绘制外圆
                        mErrorPaint.setStyle(Paint.Style.STROKE);
                        mErrorPaint.setColor(OUTTER_ERROR_COLOR);
                        canvas.drawCircle(circle.getCenterX(), circle.getCenterY(), mDotRadius, mErrorPaint);
                    }

                    //绘制内圆
                    mErrorPaint.setStyle(Paint.Style.FILL);
                    mErrorPaint.setColor(INNER_ERROR_COLOR);
                    canvas.drawCircle(circle.getCenterX(), circle.getCenterY(), mDotRadius / 6, mErrorPaint);
                }
                //绘制两个点之间的连心和箭头
                drawLine(canvas);
            }
    }

    /**
     * 绘制两个点之间的连线个箭头
     *
     * @param canvas
     */
    private void drawLine(Canvas canvas) {
        if (mSelectedPoint.size() > 0) {
            Circle lastCircle = mSelectedPoint.get(0);
            if (mIsError) {
                //如果选择错误，则设置画线条的绘制错误的颜色
                mLinePaint.setColor(INNER_ERROR_COLOR);
                mArrowPaint.setColor(INNER_ERROR_COLOR);
            } else {
                mLinePaint.setColor(INNER_PRESSED_COLOR);
                mArrowPaint.setColor(INNER_PRESSED_COLOR);
            }
            for (int i = 1; i < mSelectedPoint.size(); i++) {
                //两个点之间绘制一条线
                drawLine(lastCircle, mSelectedPoint.get(i), canvas, mLinePaint);
                //两个点之间绘制一个箭头
                drawArrow(canvas, mArrowPaint, lastCircle, mSelectedPoint.get(i), mDotRadius / 3.0, 38);
                lastCircle = mSelectedPoint.get(i);
            }
            if (mIsTouchPoint) {
                drawLine(lastCircle, new Circle(mX, mY, -1), canvas, mLinePaint);
            }
        }

    }

    /**
     * 绘制三角形
     * 详细计算看说明
     * @param canvas
     * @param arrowPaint
     * @param start
     * @param last
     * @param arrowHeight
     * @param angle
     */
    private void drawArrow(Canvas canvas, Paint arrowPaint, Circle start, Circle last, double arrowHeight, int angle) {
        float dx = last.getCenterX() - start.getCenterX();
        float dy = last.getCenterY() - start.getCenterY();
        //计算两个点的距离
        double distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        //计算两个点的夹角
        float sinA = (float) (dy / distance);
        float cosA = (float) (dx / distance);
        //三角形顶部夹角的一半
        double tanB = Math.tan(Math.toRadians(angle));

        int h = (int) (distance - arrowHeight - mDotRadius * 1.1);
        int l = (int) (arrowHeight * tanB);

        float x0 = start.getCenterX() + h * cosA;
        float y0 = start.getCenterY() + h * sinA;
        float x1 = x0 + l * sinA;
        float y1 = y0 - l * cosA;
        float x2 = x0 - l * sinA;
        float y2 = y0 + l * cosA;
        float x3 = (float) (x0+arrowHeight*cosA);
        float y3 = (float) (y0+arrowHeight*sinA);
        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.close();
        canvas.drawPath(path, arrowPaint);
    }

    /**
     * 绘制线
     *
     * @param start
     * @param end
     * @param canvas
     * @param linePaint
     */
    private void drawLine(Circle start, Circle end, Canvas canvas, Paint linePaint) {
        float dx = end.getCenterX() - start.getCenterX();
        float dy = end.getCenterY() - start.getCenterY();

        //计算两个点之间的距离
        double pointDistance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        float rx = (float) (dx / pointDistance * (mDotRadius / 6.0));
        float ry = (float) (dy / pointDistance * (mDotRadius / 6.0));
        canvas.drawLine(start.getCenterX() + rx, start.getCenterY() + ry, end.getCenterX() - rx, end.getCenterY() -
                ry, linePaint);
    }

    /**
     * 解锁监听器
     */
    public interface LockPatternListener {
        void lock(String lock);
    }
}
