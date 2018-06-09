package com.ljb.passworddialog.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Author      :ljb
 * Date        :2018/3/28
 * Description :
 */

public class GridLayoutItemDecoration extends RecyclerView.ItemDecoration {

    private int mSpanSize;
    private int ceiCount;
    private Paint mPaint;
    private int mSize;

    public GridLayoutItemDecoration(int spanSize, int dataSize) {
        mSpanSize = spanSize;
        mSize = dataSize;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(2);
        ceiCount = (int) Math.ceil((dataSize - 1) / spanSize);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            if (i % mSpanSize != mSpanSize - 1) {//绘制右边线
                drawVerticalLine(child, c);
            }
            if (Math.ceil(i / mSpanSize) != ceiCount) {//绘制下边线
                drawHorizatonLine(child, c);
            }
        }

    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            if (i % mSpanSize != mSpanSize - 1) {//绘制右边线
                offsetVertical( outRect);
            }
            if (Math.ceil(i / mSpanSize) != ceiCount) {//绘制下边线
                offsetHorization(outRect);
            }
        }
    }

    private void offsetHorization(Rect outRect) {
        outRect.bottom=2;
    }

    private void offsetVertical( Rect outRect) {
        outRect.right = 2;
    }


    /**
     * 绘制下边线
     *
     * @param child
     * @param c
     */
    private void drawHorizatonLine(View child, Canvas c) {
        int bottom = child.getBottom();
        int left = child.getLeft();
        int right = child.getRight();
        c.drawLine(left, bottom, right, bottom, mPaint);
    }

    /**
     * 绘制竖线
     *
     * @param child
     * @param c
     */
    private void drawVerticalLine(View child, Canvas c) {
        int right = child.getRight();
        int top = child.getTop();
        int bottom = child.getBottom();
        c.drawLine(right, top, right, bottom, mPaint);
    }
}
