package com.ljb.taglayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Author      :ljb
 * Date        :2018/6/1
 * Description : 自定义流式布局
 */
public class TagLayout extends ViewGroup {

    // 集合套集合的方式存储不同行的View
    private List<List<View>> mChildViews = new ArrayList<>();

    private TagAdapter mTagAdapter;

    public TagLayout(Context context) {
        super(context);
    }

    public TagLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 可能多次调用onMeasure()，所以需要清空下
        mChildViews.clear();
        // 计算宽度
        int width = MeasureSpec.getSize(widthMeasureSpec);
        // 计算高度
        int height = getPaddingTop() + getPaddingBottom();
        int childCount = getChildCount();
        // 一行的宽度
        int lineWidth = getPaddingLeft();
        // 用来存储同一行的View的集合
        ArrayList<View> childViews = new ArrayList<>();
        mChildViews.add(childViews);
        // 最大高度
        int maxHeight = 0;
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() == GONE) {
                continue;
            }
            // 计算子View的宽/高
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams params = (MarginLayoutParams) childView.getLayoutParams();
            if (lineWidth + childView.getMeasuredWidth() + params.leftMargin + params.rightMargin > width) {
                // 换行
                // 加上一行中最大的高度
                height += maxHeight;
                // lineWidth重置
                lineWidth = childView.getMeasuredWidth() + getPaddingLeft() + params.leftMargin + params.rightMargin;
                childViews = new ArrayList<>();
                mChildViews.add(childViews);
            } else {
                lineWidth += childView.getMeasuredWidth() + params.leftMargin + params.rightMargin;
                // 最后一行的高度
                maxHeight = Math.max(childView.getMeasuredHeight() + params.topMargin + params.bottomMargin, maxHeight);
            }
            childViews.add(childView);
        }
        // 加上最后一行的高度
        height += maxHeight;
        setMeasuredDimension(width, height);
    }

    /**
     * 摆放
     *
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left, top = getPaddingTop(), right, bottom;
        for (List<View> childViews : mChildViews) {
            left = getPaddingLeft();
            int maxHeight = 0;
            for (View childView : childViews) { // 同一行的View摆放
                if (childView.getVisibility() == GONE) {
                    continue;
                }
                MarginLayoutParams params = (MarginLayoutParams) childView.getLayoutParams();
                // 加上左边距
                left += params.leftMargin;
                int childTop = top + params.topMargin;
                bottom = childTop + childView.getMeasuredHeight();
                right = left + childView.getMeasuredWidth();
                childView.layout(left, childTop, right, bottom);
                left += childView.getMeasuredWidth() + params.rightMargin;
                int childHeight = childView.getMeasuredHeight() + params.bottomMargin + params.topMargin;
                // 计算同一行中最大的高度
                maxHeight = Math.max(childHeight, maxHeight);
            }
            top += maxHeight;
        }
    }

    /**
     * 设置adapter
     *
     * @param adapter
     */
    public void setAdapter(TagAdapter adapter) {
        if (adapter == null) {
            throw new NullPointerException("TagAdapter not null");
        }
        mTagAdapter = null;
        mTagAdapter = adapter;
        // 清空所有子View
        removeAllViews();
        int childCount = mTagAdapter.getContent();
        for (int i = 0; i < childCount; i++) {
            View view = mTagAdapter.getView(i, this);
            addView(view);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }
}
