package com.ljb.taglayout.new_flow;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;


import com.ljb.taglayout.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Author      :ljb
 * Date        :2019/9/22
 * Description :
 */
public class FlowLayout extends ViewGroup {

    private List<List<View>> mAllViews = new ArrayList<>();
    private List<Integer> mLineHeights = new ArrayList<>();
    //获取系统的属性
    private int[] ll = new int[android.R.attr.maxLines];
    private int maxLines = -1;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        if (ta != null) {
            maxLines = ta.getInt(R.styleable.FlowLayout_max_lines, -1);
            ta.recycle();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mAllViews.clear();
        mLineHeights.clear();
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);

        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int lineWidth = 0;
        int lineHeight = 0;
        int height = 0;
        //计算空间高度，由子空间来决定空间的高度
        int childCount = getChildCount();

        List<View> views = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() == GONE) continue;
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
            //获取子View的宽高
            MarginLayoutParams childLp = (MarginLayoutParams) childView.getLayoutParams();
            int childWidth = childView.getMeasuredWidth() + childLp.leftMargin + childLp.rightMargin;
            int childHeight = childView.getMeasuredHeight() + childLp.topMargin + childLp.bottomMargin;

            if (lineWidth + childWidth + getPaddingLeft() + getPaddingRight() > sizeWidth) {
                //换行,换行后lineWidth 就等于当前控件的宽度,lineHeight就等于当前控件的高度
                height += lineHeight;
                mLineHeights.add(lineHeight);
                mAllViews.add(views);
                views = new ArrayList<>();
                views.add(childView);

                lineWidth = childWidth;
                lineHeight = childHeight;
            } else {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);

                views.add(childView);
            }

            if (i == childCount - 1) {//最后一行
                height += lineHeight;
                mLineHeights.add(lineHeight);
                mAllViews.add(views);
            }
        }

        //  maxHeight 矫正
        if (maxLines != -1 && maxLines < mLineHeights.size()) {
            height = getMaxLinesHeight();
        }
        if (modeHeight == MeasureSpec.EXACTLY) {
            height = sizeHeight;
        } else if (modeHeight == MeasureSpec.AT_MOST) {
            height = Math.min(sizeHeight, height);
            height += (getPaddingBottom() + getPaddingTop());
        } else {
            height += (getPaddingBottom() + getPaddingTop());
        }
        setMeasuredDimension(sizeWidth, height);

    }

    private int getMaxLinesHeight() {
        int height = 0;
        for (int i = 0; i < maxLines; i++) {
            height += mLineHeights.get(i);
        }
        return height;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int lineNums = mAllViews.size();
        int left = getPaddingLeft(), top = getPaddingTop();
        for (int i = 0; i < lineNums; i++) {
            List<View> views = mAllViews.get(i);
            int lineHeight = mLineHeights.get(i);
            for (int j = 0; j < views.size(); j++) {
                View childView = views.get(j);
                MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
                // top left bottom right
                int tc = top + layoutParams.topMargin;
                int lc = left + layoutParams.leftMargin;
                int bc = tc + childView.getMeasuredHeight();
                int rc = lc + childView.getMeasuredWidth();

                childView.layout(lc, tc, rc, bc);

                left += childView.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
            }
            left = getPaddingLeft();
            top += lineHeight;
        }
    }

    // child addView没有设置layoutParams
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    // inflater 写在布局文件里面的
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    // child addView设置了layoutParams
    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    // child addView设置了layoutParams
    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }
}
