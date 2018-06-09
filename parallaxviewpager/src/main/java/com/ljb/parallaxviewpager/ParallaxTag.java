package com.ljb.parallaxviewpager;
/**
*  Author      :ljb
*  Date        :2018/6/9
*  Description : 存储属性
*/
public class ParallaxTag {
    public float translationXIn;
    public float translationXOut;
    public float translationYIn;
    public float translationYOut;

    @Override
    public String toString() {
        return "translationXIn->"+translationXIn+" translationXOut->"+translationXOut
                +" translationYIn->"+translationYIn+" translationYOut->"+translationYOut;
    }
}
