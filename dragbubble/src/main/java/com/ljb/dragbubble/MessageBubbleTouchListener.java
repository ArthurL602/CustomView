package com.ljb.dragbubble;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Author      :ljb
 * Date        :2019/10/4
 * Description :
 */
public   class MessageBubbleTouchListener implements View.OnTouchListener {

    private View mTargetView;
    private Context mContext;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;

    private MessageBubbleView mMessageBubbleView;


    public MessageBubbleTouchListener(View targetView,final  OnBubbleDismissListener onBubbleDismissListener) {
        mTargetView = targetView;
        mContext = mTargetView.getContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        // init layout params
        mParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL);
        mParams.format = PixelFormat.TRANSPARENT;
        // init MessageBubbleView
        mMessageBubbleView = new MessageBubbleView(mContext);
        mMessageBubbleView.setOnMessageBubbleStateListener(new MessageBubbleView.OnMessageBubbleStateListener() {
            @Override
            public void onRestore() {
                handleRestore();
            }

            @Override
            public void onDismiss() {
                onBubbleDismissListener.onDismiss(mTargetView);
            }
        });

    }

    /**
     * 处理 位置恢复
     */
    private void handleRestore() {
      mWindowManager.removeView(mMessageBubbleView);
      mTargetView.setVisibility(View.VISIBLE);

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 处理 down
                handleActionDown(v);
                break;
            case MotionEvent.ACTION_MOVE:
                // 处理move
                handleActionMove(event);
                break;
            case MotionEvent.ACTION_UP:
                // 处理 up
                handleActionUp(v,event);
                break;
        }
        return true;
    }

    /**
     * 处理up事件
     */
    private void handleActionUp(View v, MotionEvent event) {
        mMessageBubbleView.handleActionUp();

    }

    /**
     *  处理move事件
     */
    private void handleActionMove( MotionEvent event) {
        mMessageBubbleView.updateDragPoint(event.getRawX(),event.getRawY());
        // 隐藏原布局targetView，这里延时，用于处理隐藏时，TargetView有闪动的问题
        if(mTargetView.getVisibility() == View.VISIBLE){
            mTargetView.setVisibility(View.INVISIBLE);
        }
    }


    /**
     * 处理down
     */
    private void handleActionDown(final View v) {
        // 向WindowManager 添加MessageBubbleView
        mWindowManager.addView(mMessageBubbleView, mParams);
        int[] outLocation = new int[2];
        v.getLocationOnScreen(outLocation);
        float centerX = outLocation[0] + v.getWidth() / 2;
        float centerY = outLocation[1] + v.getHeight() / 2;
        // 初始化固定点位置
        mMessageBubbleView.initOrResetPoint(centerX, centerY);
        // 绘制原TargetView 镜像
       Bitmap bit =  getBitmapFromView(v);
        mMessageBubbleView.setTargetBitmap(bit);

//        v.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        },40);

    }

    private Bitmap getBitmapFromView(View targetView) {
        targetView.buildDrawingCache();
        Bitmap targetBit = targetView.getDrawingCache();
        return targetBit;
    }

    /**
     *  获取状态栏的高度
     */
    private float getStatusBarHeight(Context context) {
        int resId = context.getResources().getIdentifier("status_bar_height","dimen","android");
        if(resId < 0){
            return 0;
        }
        return context.getResources().getDimensionPixelOffset(resId);
    }


    public interface  OnBubbleDismissListener{
        void onDismiss(View targetView);
    }
}
