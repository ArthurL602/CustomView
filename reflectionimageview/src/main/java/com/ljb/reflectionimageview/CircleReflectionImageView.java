package com.ljb.reflectionimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Author      :ljb
 * Date        :2019/10/7
 * Description :
 */
public class CircleReflectionImageView extends AppCompatImageView {


    private int mReflectionPadding = 10;
    private Drawable mCircleBgDrawable;
    private Drawable mCircleCenterDrawable;
    private Path mPath;
    private Drawable mOriginalDrawable;
    private int mCenterCircleColor = Color.parseColor("#3B539A");
    ;
    private Paint mPaint;

    public CircleReflectionImageView(Context context) {
        this(context, null);
    }

    public CircleReflectionImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleReflectionImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mCircleBgDrawable = ContextCompat.getDrawable(context, R.drawable.img_music_album);
        mCircleCenterDrawable = ContextCompat.getDrawable(context, R.drawable.img_music_album_light);
        mPath = new Path();
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleReflectionImageView);
        if (typedArray != null) {
            mReflectionPadding = typedArray.getDimensionPixelSize(R.styleable.CircleReflectionImageView_centerPadding, mReflectionPadding);
            typedArray.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mOriginalDrawable = getDrawable();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mReflectionPadding == 0) {//
            super.onDraw(canvas);
        } else {
            // 绘制原图
            drawOriginal(canvas, 255);
            // 绘制倒影
            drawReflection2(canvas);
            // 绘制阴影
            drawShadow(canvas);
        }
    }

    private void drawOriginal(Canvas canvas, int alpha) {
        float offsetDistance = getWidth() * 0.06f;
        float targetSize = getWidth() - 2 * offsetDistance;
        float innerCircleX = getWidth() * 0.5f;
        float innerCircleY = getWidth() * 0.5f;
        float innerCircleRadius = targetSize / 2;
        // 绘制大圆背景
        mCircleBgDrawable.setBounds(0, 0, getWidth(), getWidth());
        mCircleBgDrawable.setAlpha(alpha);
        mCircleBgDrawable.setDither(true);
        mCircleBgDrawable.draw(canvas);
        // 绘制中间层图片
        canvas.save();
        mPath.reset();
        mPath.addCircle(innerCircleX, innerCircleY, innerCircleRadius, Path.Direction.CW);
        mOriginalDrawable.setAlpha(alpha);
        mOriginalDrawable.setBounds((int) offsetDistance, (int) offsetDistance, (int) (offsetDistance + targetSize), (int) (offsetDistance + targetSize));
        canvas.clipPath(mPath);
        mOriginalDrawable.setDither(true);
        mOriginalDrawable.draw(canvas);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(mCenterCircleColor);
        canvas.drawPath(mPath, paint);
        canvas.restore();
        // 绘制最外层小圆
        mCircleCenterDrawable.setBounds(0, 0, getWidth(), getWidth());
        mCircleCenterDrawable.setAlpha(alpha);
        mCircleCenterDrawable.setDither(true);
        mCircleCenterDrawable.draw(canvas);
    }

    private void drawShadow(Canvas canvas) {
        canvas.save();
        canvas.translate(0, getWidth() + mReflectionPadding);
        RectF rectF = new RectF(0, 0, getWidth(), getHeight() - getWidth() - mReflectionPadding);
        Paint paint = getPaint();
        canvas.drawRect(rectF, paint);
        canvas.restore();
    }

    private Paint getPaint() {
        if (mPaint == null) {
            LinearGradient gradient = new LinearGradient(0, 0, 0, getHeight() - getWidth() - mReflectionPadding * 1.5f, 0x60ffffff, Color.TRANSPARENT, Shader.TileMode.CLAMP);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            mPaint.setShader(gradient);
        }
        return mPaint;
    }

    private void drawReflection2(Canvas canvas) {
        canvas.save();
        canvas.scale(1, -1);
        canvas.translate(0, -getWidth() * 2 - 10);
        drawOriginal(canvas, 180);
        canvas.restore();
    }


}
