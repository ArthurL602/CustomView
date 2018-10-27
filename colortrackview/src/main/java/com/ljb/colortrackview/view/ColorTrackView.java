package com.ljb.colortrackview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.ljb.colortrackview.R;

import static com.ljb.colortrackview.view.ColorTrackView.Direction.LEFT_TO_RIGHT;


/**
 * Author      :ljb
 * Date        :2018/2/23
 * Description : 颜色轨迹自定义view(玩转字体变色)
 */


public class ColorTrackView extends android.support.v7.widget.AppCompatTextView {
    //不变色的画笔
    private Paint mOriginalPaint;
    //变色的画笔
    private Paint mChangePaint;
    //当前进度
    private float mCurrentProgress = 0.0f;
    //文字宽度
    private int mWidth;
    //文字高度
    private int mHeight;
    private Paint.FontMetrics mMetrics;
    //绘制的文字
    private String mText;

    public enum Direction {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT
    }

    private Direction mDirection = LEFT_TO_RIGHT;

    public ColorTrackView(Context context) {
        this(context, null);
    }

    public ColorTrackView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorTrackView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint(context, attrs);
    }

    public void setChangeColor(int color) {
        mChangePaint.setColor(color);
    }

    public void setOriginalColor(int color) {
        mOriginalPaint.setColor(color);
    }

    /**
     * 实例化画笔
     *
     * @param context
     * @param attrs
     */
    private void initPaint(Context context, AttributeSet attrs) {

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ColorTrackView);
        if (ta != null) {
            int originalColor = ta.getColor(R.styleable.ColorTrackView_originalColor, getTextColors().getDefaultColor
                    ());
            int changeColor = ta.getColor(R.styleable.ColorTrackView_changeColor, getTextColors().getDefaultColor());
            ta.recycle();

            mOriginalPaint = getPaintByColor(originalColor);
            mChangePaint = getPaintByColor(changeColor);
        } else {
            mOriginalPaint = getPaintByColor(getTextColors().getDefaultColor());
            mChangePaint = getPaintByColor(getTextColors().getDefaultColor());
        }

    }

    /**
     * 根据画笔颜色创建画笔
     *
     * @param color
     * @return
     */
    private Paint getPaintByColor(int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setDither(true);
        paint.setColor(color);
        paint.setTextSize(getTextSize());
        return paint;
    }

    public Direction getDirection() {
        return mDirection;
    }

    /**
     * 绘制文字，展示不同颜色
     * 一个文字两种颜色（利用clipRect()的api）
     * 左边用一个画笔去画，右边用一个画笔去画，不断的改变中间值
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        //根据进度吧中间值计算出来
        int middle = (int) (mCurrentProgress * getWidth());
        if (mDirection == LEFT_TO_RIGHT) {//从左到右
            //不变色
            drawText(canvas, mOriginalPaint, middle, getWidth());
            //绘制变色的文字
            drawText(canvas, mChangePaint, 0, middle);
        } else {//从右到左
            //不变色的
            drawText(canvas, mOriginalPaint, 0, getWidth() - middle);
            //绘制变色的文字
            drawText(canvas, mChangePaint, getWidth() - middle, getWidth());
        }

    }

    public void setCurrentProgress(float currentProgress) {
        mCurrentProgress = currentProgress;
        invalidate();
    }

    public void setDirection(Direction direction) {
        mDirection = direction;
    }

    /**
     * 绘制文字
     *
     * @param canvas 画板
     * @param paint  画笔
     * @param start  起始位置
     * @param end    结束位置
     */
    private void drawText(Canvas canvas, Paint paint, int start, int end) {
        canvas.save();
        mText = getText().toString();
        paint.setTextSize(getTextSize());
        mWidth = (int) mOriginalPaint.measureText(mText);
        mMetrics = mOriginalPaint.getFontMetrics();
        mHeight = (int) (Math.abs(mMetrics.ascent) - mMetrics.descent);
        canvas.clipRect(start, 0, end, getHeight());
        int x = getWidth() / 2 - mWidth / 2;
        int y = getHeight() / 2 + mHeight / 2;
        canvas.drawText(mText, x, y, paint);
        canvas.restore();
    }
}
