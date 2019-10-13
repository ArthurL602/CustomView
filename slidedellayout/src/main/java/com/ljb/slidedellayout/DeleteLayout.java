package com.ljb.slidedellayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Author: LiangJingBo.
 * @Date: 2019/10/11
 * @Describe: 侧滑删除自定义View
 */
public class DeleteLayout extends ViewGroup {

    private View mContentView;
    private View mDelView;

    private ViewDragHelper mViewDragHelper;

    private OnResultCallback mOnResultCallback;

    private int mState = State.STATE_IDLE;

    private static DeleteLayout sOpenedDeleteLayout = null;

    private static boolean isTouching = false;
    private boolean mInterceptEvent = false;
    // 是否是向左滑动
    private boolean mIsLeftSwipe = true;

    private float mLastX;

    public void setResultCallback(OnResultCallback onResultCallback) {
        mOnResultCallback = onResultCallback;
    }

    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(@NonNull View view, int i) {
            return view == mDelView || view == mContentView;
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            if (mIsLeftSwipe) {
                left = calculateLeftSwipeResult(child, left);
            } else {
                left = calculateRightSwipeResult(child, left);
            }
            return left;
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            if (mIsLeftSwipe) {
                left = calculateLeftSwipeViewChangeResult(changedView, left);
            } else {
                left = calculateRightSwipeViewChangeResult(changedView, left);
            }
            if (changedView == mContentView) {
                mDelView.layout(left, top, left + mDelView.getWidth(), top + mDelView.getHeight());
            }
            if (changedView == mDelView) {
                mContentView.layout(left, top, left + mContentView.getWidth(), top + mContentView.getHeight());
            }
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            int left = mContentView.getLeft();//[-mDelView.getWidth(),0]
            if (!mIsLeftSwipe) {
                left = -left;
            }
            if (left > -mDelView.getWidth() * 0.5f) {
                // close
                handleSmoothClose();
            } else {
                // open
                handleSmoothOpen();
            }
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            super.onEdgeTouched(edgeFlags, pointerId);
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            super.onEdgeDragStarted(edgeFlags, pointerId);
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            return mViewDragHelper.getTouchSlop();
        }
    };

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        if (mContentView != null) {
            mContentView.setOnClickListener(l);
        }
    }

    public DeleteLayout(Context context) {
        this(context, null);
    }

    public DeleteLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeleteLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mViewDragHelper = ViewDragHelper.create(this, mCallback);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentView = getChildAt(0);
        mDelView = getChildAt(1);

        mDelView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState == State.STATE_IDLE) {
                    handleDelAnim();
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    public void handleDelAnim() {
        this.setPivotY(0);
        ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(this, "scaleY", 1.f, 0.3f);
        scaleAnimator.setInterpolator(new LinearInterpolator());
        scaleAnimator.setDuration(200);
        final int totalTranslation = mDelView.getWidth() + mContentView.getWidth();
        ValueAnimator translationAnimator = ValueAnimator.ofFloat(0f, 1f);
        translationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float translationValue = (float) animation.getAnimatedValue();
                float translationX = translationValue * totalTranslation;
                ViewCompat.setTranslationX(DeleteLayout.this, -translationX);
            }
        });
        translationAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (DeleteLayout.this.getParent() != null) {
                    ViewGroup parent = (ViewGroup) DeleteLayout.this.getParent();
                    parent.removeView(DeleteLayout.this);
                }
            }
        });
        translationAnimator.setInterpolator(new AccelerateInterpolator());
        translationAnimator.setDuration(200);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleAnimator).before(translationAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mState = State.STATE_ANIMATION;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mState = State.STATE_IDLE;
                if (mOnResultCallback != null) {
                    mOnResultCallback.onDel(DeleteLayout.this);
                }
            }
        });
        animatorSet.start();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        int width = 0, height = 0;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            width = Math.max(width, child.getMeasuredWidth());
            height = Math.max(height, child.getMeasuredHeight());
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int delLeft = getWidth();
        if (!mIsLeftSwipe) {
            delLeft = -mDelView.getMeasuredWidth();
        }
        mContentView.layout(0, 0, getWidth(), getHeight());
        mDelView.layout(delLeft, 0, delLeft + mDelView.getMeasuredWidth(), getHeight());
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            if (mState == State.STATE_IDLE) {
                mState = State.STATE_ANIMATION;
            }
            invalidate();
        } else {
            if (mState == State.STATE_ANIMATION) {
                mState = State.STATE_IDLE;
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float actionX = ev.getX();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInterceptEvent = false;
                float leftBorder = 0;
                float rightBorder = getWidth() - mDelView.getWidth();
                if (!mIsLeftSwipe) {
                    leftBorder = mDelView.getWidth();
                    rightBorder = getWidth();
                }
                if (actionX > leftBorder && actionX < rightBorder) {
                    if (sOpenedDeleteLayout != null && sOpenedDeleteLayout == DeleteLayout.this) {
                        handleSmoothClose();
                        return false;
                    }
                }
                if (isTouching) {
                    return false;
                } else {
                    isTouching = true;
                }
                if (sOpenedDeleteLayout != null) {
                    if (sOpenedDeleteLayout != this) {
                        mInterceptEvent = true;
                        sOpenedDeleteLayout.handleSmoothClose();
                    }
                }
                mLastX = actionX;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(mLastX - actionX) > mViewDragHelper.getTouchSlop()) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (mInterceptEvent) {
                    break;
                }
                mLastX = actionX;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTouching = false;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    //每次ViewDetach的时候，判断一下 ViewCache是不是自己，如果是自己，关闭侧滑菜单，且ViewCache设置为null，
    // 理由：1 防止内存泄漏(ViewCache是一个静态变量)
    // 2 侧滑删除后自己后，这个View被Recycler回收，复用，下一个进入屏幕的View的状态应该是普通状态，而不是展开状态。
    @Override
    protected void onDetachedFromWindow() {
        if (this == sOpenedDeleteLayout) {
            sOpenedDeleteLayout.handleFastClose();
            sOpenedDeleteLayout = null;
        }
        super.onDetachedFromWindow();

    }

    private void handleSmoothOpen() {
        sOpenedDeleteLayout = this;
        int contentFinalLeft = -mDelView.getWidth();
        int delFinalLeft = getWidth() - mDelView.getWidth();
        if (!mIsLeftSwipe) {
            contentFinalLeft = mDelView.getWidth();
            delFinalLeft = 0;
        }
        mViewDragHelper.smoothSlideViewTo(mContentView, contentFinalLeft, 0);
        mViewDragHelper.smoothSlideViewTo(mDelView, delFinalLeft, 0);
        invalidate();
    }

    private void handleSmoothClose() {
        sOpenedDeleteLayout = null;
        int contentFinalLeft = 0;
        int delFinalLeft = getWidth();
        if (!mIsLeftSwipe) {
            contentFinalLeft = 0;
            delFinalLeft = -mDelView.getWidth();
        }
        mViewDragHelper.smoothSlideViewTo(mContentView, contentFinalLeft, 0);
        mViewDragHelper.smoothSlideViewTo(mDelView, delFinalLeft, 0);
        invalidate();
    }

    /**
     * 快速打开删除按钮，不带动画
     */
    public void handleFastOpen() {
        int contentFinalLeft = -mDelView.getWidth();
        int delFinalLeft = getWidth() - mDelView.getWidth();
        if (!mIsLeftSwipe) {
            contentFinalLeft = mDelView.getWidth();
            delFinalLeft = 0;
        }
        if (mContentView.getLeft() != contentFinalLeft) {
            mContentView.layout(contentFinalLeft, mContentView.getTop(), mContentView.getWidth() + contentFinalLeft, mContentView.getBottom());
        }
        if (mDelView.getWidth() != delFinalLeft) {
            mDelView.layout(delFinalLeft, mDelView.getTop(), mDelView.getWidth() + delFinalLeft, mDelView.getBottom());
        }
        sOpenedDeleteLayout = DeleteLayout.this;
    }

    /**
     * 快速关闭删除按钮，不带动画
     */
    public void handleFastClose() {
        int contentFinalLeft = 0;
        int delFinalLeft = getWidth();
        if (!mIsLeftSwipe) {
            contentFinalLeft = 0;
            delFinalLeft = -mDelView.getWidth();
        }
        if (mContentView.getLeft() != contentFinalLeft) {
            mContentView.layout(contentFinalLeft, mContentView.getTop(), contentFinalLeft + mContentView.getWidth(), mContentView.getBottom());
        }
        if (mDelView.getWidth() != delFinalLeft) {
            mDelView.layout(delFinalLeft, mDelView.getTop(), delFinalLeft + mDelView.getWidth(), mDelView.getBottom());
        }
        sOpenedDeleteLayout = null;
    }

    private int calculateRightSwipeViewChangeResult(@NonNull View changedView, int left) {
        if (changedView == mContentView) {
            left += -mDelView.getWidth();
        }
        if (changedView == mDelView) {
            left += mDelView.getWidth();
        }
        return left;
    }

    private int calculateLeftSwipeViewChangeResult(@NonNull View changedView, int left) {
        if (changedView == mContentView) {
            left += getWidth();
        }
        if (changedView == mDelView) {
            left -= getWidth();
        }
        return left;
    }

    private int calculateRightSwipeResult(View child, int left) {
        if (child == mContentView) {//[0,mDelView.getWidth()]
            if (left < 0) {
                left = 0;
            }
            if (left > mDelView.getWidth()) {
                left = mDelView.getWidth();
            }
        }
        if (child == mDelView) {//[-mDelView.getWidth(),0]
            if (left < -mDelView.getWidth()) {
                left = -mDelView.getWidth();
            }
            if (left > 0) {
                left = 0;
            }
        }
        return left;
    }

    private int calculateLeftSwipeResult(@NonNull View child, int left) {
        if (child == mContentView) {
            if (left < -mDelView.getWidth()) {
                left = -mDelView.getWidth();
            }
            if (left > 0) {
                left = 0;
            }
        }
        if (child == mDelView) {
            if (left < getWidth() - mDelView.getWidth()) {
                left = getWidth() - mDelView.getWidth();
            }
            if (left > getWidth()) {
                left = getWidth();
            }
        }
        return left;
    }

    public interface OnResultCallback {
        void onDel(View targetView);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({State.STATE_IDLE, State.STATE_ANIMATION})
    public @interface State {
        int STATE_IDLE = 0;
        int STATE_ANIMATION = 1;
    }

}
