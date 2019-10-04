package com.ljb.lovelayout;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * Author      :ljb
 * Date        :2019/10/4
 * Description :
 */
public class BezierTypeEvaluator implements TypeEvaluator<PointF> {
    private PointF mPointF1, mPointF2;

    public BezierTypeEvaluator(PointF pointF1, PointF pointF2) {
        mPointF1 = pointF1;
        mPointF2 = pointF2;
    }

    @Override
    public PointF evaluate(float fraction, PointF pointF0, PointF pointF3) {
        PointF pointF = new PointF();
        pointF.x = pointF0.x * (1 - fraction) * (1 - fraction) * (1 - fraction) +
                3 * mPointF1.x * fraction * (1 - fraction) * (1 - fraction) +
                3 * mPointF2.x * fraction * fraction * (1 - fraction) +
                pointF3.x * fraction * fraction;

        pointF.y = pointF0.y * (1 - fraction) * (1 - fraction) * (1 - fraction) +
                3 * mPointF1.y * fraction * (1 - fraction) * (1 - fraction) +
                3 * mPointF2.y * fraction * fraction * (1 - fraction) +
                pointF3.y * fraction * fraction;
        return pointF;
    }
}
