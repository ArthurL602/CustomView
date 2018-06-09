package com.ljb.indexbaritemdecoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;


import com.ljb.indexbaritemdecoration.entity.TitleBean;

import java.util.List;

/**
 * Author      :ljb
 * Date        :2018/2/23
 * Description :
 */

public class TitleItemDecoration<T extends TitleBean> extends RecyclerView.ItemDecoration {
    //标题文字大小
    private int mTitleFontSize = 16;
    //标题高度
    private int mTitleHeight = 30;
    private Paint mPaint;
    private Context mContext;
    private List<T> mDatas;

    public TitleItemDecoration(Context context, List<T> datas) {
        super();
        mDatas = datas;

        mContext = context;
        mTitleFontSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTitleFontSize, mContext
                .getResources().getDisplayMetrics());
        mTitleHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mTitleHeight, mContext
                .getResources().getDisplayMetrics());
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mPaint.setTextSize(mTitleFontSize);
    }

    /**
     * 绘制title,布置每个item的时候都会回调次方法
     *
     * @param c
     * @param parent
     * @param state
     */
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        //计算title的宽高位置
        int left = parent.getPaddingLeft();
        int right = parent.getRight() - parent.getPaddingRight();
        // 获取子Item的个数
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            //获取item的位置
            int pos = params.getViewLayoutPosition();
            String tag = mDatas.get(pos).getTag();
            if (pos > -1) {
                //第一个item一定时候title的
                if (pos == 0) {
                    drawTitleArea(c, tag, params, left, right, child);
                } else if (mDatas.get(pos).getTag() != null && !mDatas.get(pos).getTag().equals(mDatas.get(pos - 1)
                        .getTag())) {
                    //如果当前item的 tag不等于上一个item的tag，则也有title
                    drawTitleArea(c, tag, params, left, right, child);
                }
            }
        }
    }

    /**
     * 绘制Title区域
     *
     * @param c
     * @param tag
     * @param params
     * @param left
     * @param right
     * @param child
     */
    private void drawTitleArea(Canvas c, String tag, RecyclerView.LayoutParams params, int left, int right, View
            child) {
        int top = child.getTop() - params.topMargin - mTitleHeight;
        int bottom = top + mTitleHeight;
        //设置title区域背景颜色
        mPaint.setColor(Color.GRAY);
        c.drawRect(left, top, right, bottom, mPaint);
        mPaint.setColor(Color.BLACK);
        Rect bounds = new Rect();
        mPaint.getTextBounds(tag, 0, tag.length(), bounds);
        //绘制文字
        c.drawText(tag, child.getPaddingLeft(), top + (mTitleHeight / 2 + bounds.height() / 2), mPaint);
    }

    /**
     * 绘制悬浮区域
     *
     * @param c
     * @param parent
     * @param state
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        int left = parent.getPaddingLeft();
        int right = parent.getRight() - parent.getPaddingRight();
        int top = parent.getTop();
        int bottom = top + mTitleHeight;
        int pos = ((LinearLayoutManager) parent.getLayoutManager()).findFirstVisibleItemPosition();
        String tag = mDatas.get(pos).getTag();
        //出现一个奇怪的bug，有时候child为空，所以将 child = parent.getChildAt(i)。-》 parent.findViewHolderForLayoutPosition(pos).itemView
        View child = parent.findViewHolderForLayoutPosition(pos).itemView;
        mPaint.setColor(Color.GRAY);
        c.drawRect(left, top, right, bottom, mPaint);//绘制矩形区域
        mPaint.setColor(Color.BLACK);
        Rect rect = new Rect();
        mPaint.getTextBounds(tag, 0, tag.length(), rect);
        c.drawText(tag, child.getPaddingLeft(), bottom - mTitleHeight / 2 +rect.height() / 2, mPaint);

    }

    /**
     * 设置装饰偏移量
     *
     * @param outRect
     * @param view
     * @param parent
     * @param state
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        int pos = params.getViewLayoutPosition();
        if (pos > -1) {
            //第一个item是有title的所以需要偏移
            if (pos == 0) {
                outRect.set(0, mTitleHeight, 0, 0);
            } else if (mDatas.get(pos).getTag() != null && !mDatas.get(pos).getTag().equals(mDatas.get(pos - 1)
                    .getTag())) {
                //如果当前item的tag与上一个item的tag不相等，也是需要偏移的
                outRect.set(0, mTitleHeight, 0, 0);
            }
        }
    }
}
