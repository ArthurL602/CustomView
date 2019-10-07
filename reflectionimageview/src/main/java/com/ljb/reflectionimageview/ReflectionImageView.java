package com.ljb.reflectionimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.FloatRange;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Author      :ljb
 * Date        :2019/10/7
 * Description :
 */
public class ReflectionImageView extends AppCompatImageView {


    private @FloatRange(from = 0, to = 1)
    float mOriginalScale = 0.89f;
    private @FloatRange(from = 0, to = 1)
    float mReflectionScale = 0.1f;

    private float mOriginalHeight, mReflectionPadding, mReflectionHeigh;

    public ReflectionImageView(Context context) {
        this(context, null);
    }

    public ReflectionImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReflectionImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        initAttrs(context,attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ReflectionImageView);
        if(typedArray !=null){
            mOriginalScale= typedArray.getFloat(R.styleable.ReflectionImageView_originalScale, mOriginalScale);
            mReflectionScale= typedArray.getFloat(R.styleable.ReflectionImageView_reflectionScale, mReflectionScale);
            typedArray.recycle();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            int height = getHeight();
            mOriginalHeight = height * mOriginalScale;
            mReflectionHeigh = height * mReflectionScale;
            mReflectionPadding = height - mOriginalHeight - mReflectionHeigh;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mReflectionHeigh == 0) {//
            super.onDraw(canvas);
        } else {
            float scale = mOriginalHeight / getHeight();
            canvas.save();
            canvas.scale(1f, scale, getWidth() /2, 0);
            super.onDraw(canvas);
            canvas.restore();
            // 绘制倒影
            drawReflection(canvas);
            // 绘制阴影
            drawShadow(canvas);
        }
    }

    private void drawShadow(Canvas canvas) {
        canvas.save();
        float top = mOriginalHeight + mReflectionPadding;
        canvas.translate(0,top);
        RectF rectF = new RectF(0,0,getWidth(),mReflectionHeigh);
        Paint paint = getPaint();
        canvas.drawRect(rectF,paint);
        canvas.restore();
    }

    private Paint getPaint() {
//        LinearGradient gradient = new LinearGradient(0,0,0,mReflectionHeigh,0x90ffffff,0x30ffffff, Shader.TileMode.CLAMP);
        LinearGradient gradient = new LinearGradient(0,0,0,mReflectionHeigh,Color.TRANSPARENT,0x90ffffff, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        paint.setShader(gradient);
        return paint;
    }

    private void drawReflection(Canvas canvas) {
        canvas.save();
        canvas.scale(1f,-1f,0,getHeight());
        canvas.translate(0, mReflectionHeigh);
        Drawable drawable = getDrawable();
        drawable.draw(canvas);
        canvas.restore();
    }

}
