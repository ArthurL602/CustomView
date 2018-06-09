package com.ljb.lockpatternview.entity;

/**
 * Author      :ljb
 * Date        :2018/2/7
 * Description :
 */

public class Circle {
    public static final int STATUS_NORAML = 1;
    public static final int STATUS_ERROR = 2;
    public static final int STATUS_PRESSED = 3;
    //圆心点坐标位置
    private float mCenterX;
    private float mCenterY;
    //下标，第几个圆
    private int mIndex;

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public Circle(float centerX, float centerY, int index) {

        mCenterX = centerX;
        mCenterY = centerY;
        mIndex = index;
    }

    public float getCenterX() {

        return mCenterX;
    }

    public void setCenterX(float centerX) {
        mCenterX = centerX;
    }

    public float getCenterY() {
        return mCenterY;
    }

    public void setCenterY(float centerY) {
        mCenterY = centerY;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    private int mStatus=STATUS_NORAML;

}
