package com.ljb.viewpagerindicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Author      :ljb
 * Date        :2018/6/22
 * Description : 自定义ViewPagerIndicator
 */
public class ViewPagerIndicator extends HorizontalScrollView {
    private IVpIndicatorAdapter mAdapter;
    private Paint mPaint;
    private Path mPath;
    /*三角形宽高*/
    private int mTriangleWidth, mTriangleHeight;
    /*初始化偏移量，手指移动时的偏移量*/
    private int mInitTranslationX, mTranslationX;
    private float RAIDO_TRIANGLE_WIDTH = 0.2f;
    private int mTabCouont = 4;
    private ViewPager mViewPager;
    private LinearLayout mRoot;
    private float mLastPositon;

    public ViewPagerIndicator(Context context) {
        this(context, null);
    }

    public ViewPagerIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewPagerIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setPathEffect(new CornerPathEffect(3));

    }

    /**
     * 设置适配器
     *
     * @param adapter
     */
    public void setAdapter(IVpIndicatorAdapter adapter) {
        // 添加根布局
        addRootView();
        mAdapter = adapter;
        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            final View view = mAdapter.getView(i);
            view.post(new Runnable() {
                @Override
                public void run() {
                    int width = getWidth() / mTabCouont;
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams
                            .MATCH_PARENT);
                    lp.width = width;
                    view.setLayoutParams(lp);
                    mRoot.addView(view);
                }
            });
            final int index = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mViewPager != null) {
                        mViewPager.setCurrentItem(index);
                    }

                }
            });
        }
        post(new Runnable() {
            @Override
            public void run() {
                hightLightTextViewColor(0);
            }
        });
    }

    /**
     * 添加根布局
     */
    private void addRootView() {
        removeAllViews();
        mRoot = new LinearLayout(getContext());
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mRoot.setLayoutParams(lp);
        addView(mRoot);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // 1. 绘制三角形
        canvas.save();
        // 1.1 平移画布，绘制 三角形
        canvas.translate(mInitTranslationX + mTranslationX, getHeight() + 2);
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 计算三角形宽度 : title宽度 1/6
        mTriangleWidth = (int) (w / mTabCouont * RAIDO_TRIANGLE_WIDTH);
        mTriangleHeight = mTriangleWidth / 4;
        // 计算三角形初始化位置
        mInitTranslationX = w / mTabCouont / 2 - mTriangleWidth / 2;
        initTriangle();
    }

    /**
     * 初始化三角形
     */
    private void initTriangle() {
        if (mPath == null) {
            mPath = new Path();
        }
        mPath.reset();
        mPath.moveTo(0, 0);
        mPath.lineTo(mTriangleWidth, 0);
        mPath.lineTo(mTriangleWidth / 2, -mTriangleHeight);
        mPath.close();
    }

    /**
     * 三角形跟随手指滑动
     *
     * @param position
     * @param positionOffset
     */
    public void onPageScrolled(int position, float positionOffset) {
        float currentPosition = position + positionOffset;
        boolean leftToRight = false;
        if (currentPosition > mLastPositon) {
            leftToRight = true;
        }
        // 2. 三角形跟随手机滑动
        // 2.1 tab的宽度
        int tabWidth = getWidth() / mTabCouont;
        // 2.2 计算平移偏移量
        mTranslationX = (int) (positionOffset * tabWidth + position * tabWidth);
        // 3. 容器跟随滑动，当tab处于移动到最后一个时
        if (mTabCouont != 1) {
            // 3.1 当tab不是最后第二个且大于可见tab数-1时
            // mTabCount - 2: 滑动到当前屏幕可见tab位置的倒数第二个tab位置，之所以减2是因为position是从0开始的，需要多减一个1
            if (position >= mTabCouont - 2 && positionOffset > 0 && mRoot.getChildCount() > mTabCouont) {
                // (position - (mTabCouont - 2))：为了保持视觉上tab一直处于这个位置，需要偏移的tab个数
                // 这个个数等于当前position - 用户所指定的滑动到屏幕上个哪个位置的tab，就需要滑动容器
                int scrollTo = (int) (positionOffset * tabWidth + (position - (mTabCouont - 2)) * tabWidth);
                if (leftToRight && position != mRoot.getChildCount() - 2) {
                    // 偏移量* tabWidth
                    scrollTo(scrollTo, 0);
                } else {
                    scrollTo(scrollTo, 0);
                }
            }
        } else {
            scrollTo((int) positionOffset * tabWidth + position * tabWidth, 0);
        }
        mLastPositon = currentPosition;
        invalidate();
    }

    private void resetTextColor() {
        for (int i = 0; i < mRoot.getChildCount(); i++) {
            View view = mRoot.getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(Color.WHITE);
            }

        }
    }

    /**
     * 字体高亮
     *
     * @param position
     */
    private void hightLightTextViewColor(int position) {
        resetTextColor();
        View view = mRoot.getChildAt(position);
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(Color.RED);
        }
    }

    /**
     * 绑定ViewPager
     *
     * @param viewPager
     */
    public void bindViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                ViewPagerIndicator.this.onPageScrolled(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                hightLightTextViewColor(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
