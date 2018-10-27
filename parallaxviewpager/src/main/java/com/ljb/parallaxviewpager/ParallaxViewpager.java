package com.ljb.parallaxviewpager;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Author      :ljb
 * Date        :2018/6/9
 * Description : 视察动画的ViewPager
 */
public class ParallaxViewpager extends ViewPager {

    private List<ParallaxFragment> mFragments;

    public ParallaxViewpager(Context context) {
        this(context, null);
    }

    public ParallaxViewpager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFragments = new ArrayList<>();
    }

    /**
     * 设置布局
     *
     * @param layouts
     */
    public void setLayout(FragmentManager fm, int[] layouts) {
        mFragments.clear();
        for (int i = 0; i < layouts.length; i++) {
            ParallaxFragment fragment = ParallaxFragment.newInstance(layouts[i]);
            mFragments.add(fragment);
        }
        setAdapter(new ParallaxPagerAdapter(fm));
        // 监听滑动
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // position 当前位置  ； positionOffset : 0 --> 1 ； positionOffsetPixels : 0-->屏幕的宽度；
                // 左 out 右 in
                ParallaxFragment outFragment = mFragments.get(position);
                List<View> parallaxViews = outFragment.getParallaxViews();
                for (View parallaxView : parallaxViews) {

                    ParallaxTag  tag = (ParallaxTag) parallaxView.getTag(R.id.parallax_tag);
                    parallaxView.setTranslationX(-positionOffsetPixels*tag.translationXOut);
                    Log.e("TAG",
                            "ddddd: "+-positionOffsetPixels*tag.translationXOut);
                    parallaxView.setTranslationY(-positionOffsetPixels*tag.translationYOut);
                }
                try {
                    ParallaxFragment inFragment  = mFragments.get(position+1);
                parallaxViews = inFragment.getParallaxViews();
                    for (View parallaxView : parallaxViews) {
                        ParallaxTag  tag = (ParallaxTag) parallaxView.getTag(R.id.parallax_tag);
                        parallaxView.setTranslationX((getMeasuredWidth()-positionOffsetPixels)*tag.translationXIn);
                        parallaxView.setTranslationY((getMeasuredWidth()-positionOffsetPixels)*tag.translationYIn);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private class ParallaxPagerAdapter extends FragmentPagerAdapter {

        public ParallaxPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }
}
