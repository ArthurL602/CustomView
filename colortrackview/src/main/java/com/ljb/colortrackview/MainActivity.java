package com.ljb.colortrackview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;


import com.ljb.colortrackview.view.ColorTrackView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String[] titles = {"直播", "推荐", "视频", "图片", "段子", "精华"};
    private LinearLayout mIndicator;
    private List<ColorTrackView> mColorTrackViews;
    private ViewPager mViewPager;
    private List<Fragment> mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initView();
        initData();

    }

    private void initData() {
        mColorTrackViews = new ArrayList<>();
        mFragments = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            ItemFragment fragment = ItemFragment.newInstance(titles[i]);

            ColorTrackView trackView = new ColorTrackView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.weight = 1;
            trackView.setText(titles[i]);
            trackView.setTextSize(20);
            trackView.setChangeColor(Color.RED);
            trackView.setOriginalColor(Color.BLACK);
            trackView.setLayoutParams(lp);

            mFragments.add(fragment);
            mColorTrackViews.add(trackView);
            mIndicator.addView(trackView);

        }

        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                ColorTrackView left = mColorTrackViews.get(position);
                left.setDirection(ColorTrackView.Direction.RIGHT_TO_LEFT);
                left.setCurrentProgress(1-positionOffset);

                if(position+1<mColorTrackViews.size()){
                    ColorTrackView right = mColorTrackViews.get(position+1);
                    right.setDirection(ColorTrackView.Direction.LEFT_TO_RIGHT);
                    right.setCurrentProgress(positionOffset);
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

    private void initView() {
        mIndicator = (LinearLayout) findViewById(R.id.indicator);
        mViewPager = (ViewPager) findViewById(R.id.vp);

    }


}
