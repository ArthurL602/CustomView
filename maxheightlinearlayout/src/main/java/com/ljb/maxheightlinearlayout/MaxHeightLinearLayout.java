package com.ljb.maxheightlinearlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Author      :ljb
 * Date        :2018/10/27
 * Description : 自定义可以设置最大高度得LinearLayout
 */
public class MaxHeightLinearLayout extends LinearLayout {

    private boolean mMaxHeightEnable=true;
    private float mRatio;

    public MaxHeightLinearLayout(Context context) {
        this(context, null);
    }

    public MaxHeightLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaxHeightLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context,attrs);
    }

    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightLinearLayout);
        if(typedArray!=null){
            mMaxHeightEnable = typedArray .getBoolean(R.styleable.MaxHeightLinearLayout_maxHeightEnable,true);
            mRatio = typedArray.getFloat(R.styleable.MaxHeightLinearLayout_maxHeiRatio,0.5f);
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mMaxHeightEnable){
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            heightSize = heightSize >= getScreenHeight() * mRatio ? (int) (getScreenHeight() * mRatio) : heightSize;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private float getScreenHeight() {
        return getContext().getResources().getDisplayMetrics().heightPixels;
    }
}
