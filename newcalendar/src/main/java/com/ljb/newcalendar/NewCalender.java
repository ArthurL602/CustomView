package com.ljb.newcalendar;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Author      :ljb
 * Date        :2018/6/23
 * Description :
 */
public class NewCalender extends LinearLayout {
    private String[] weaks = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
    private LinearLayout mWeakesContainer;
    private View mRoot;
    private Button mBtnPre;
    private Button mBtnNext;
    private TextView mTvDate;
    private GridView mGvCalender;

    private Calendar mCurrCalender = Calendar.getInstance();
    private ArrayList<Date> mCells;

    public NewCalender(Context context) {
        this(context, null);
    }

    public NewCalender(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewCalender(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRoot = LayoutInflater.from(context).inflate(R.layout.ui_calender_view, this, false);
        addView(mRoot);
        initContent();


    }

    private void initContent() {
        initWeaks();
        initView();// 初始化View
        initEvent();
        renderCalender();
    }


    private void initWeaks() {
        mWeakesContainer = mRoot.findViewById(R.id.ll_calender_weak);
        for (int i = 0; i < weaks.length; i++) {
            TextView tv_weak = getWeakText(weaks[i]);
            mWeakesContainer.addView(tv_weak);
        }
    }

    private void initView() {
        mBtnPre = mRoot.findViewById(R.id.btn_pre);
        mBtnNext = mRoot.findViewById(R.id.btn_next);
        mTvDate = mRoot.findViewById(R.id.tv_calender_date);
        mGvCalender = mRoot.findViewById(R.id.gv_calender_view);

    }


    private void initEvent() {
        mBtnPre.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrCalender.add(Calendar.MONTH, -1);
                renderCalender();
            }
        });
        mBtnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrCalender.add(Calendar.MONTH, 1);
                renderCalender();
            }
        });
    }

    /**
     * 渲染日历
     */
    private void renderCalender() {
        // 设置日期
        SimpleDateFormat format = new SimpleDateFormat("MM/yyyy");
        mTvDate.setText(format.format(mCurrCalender.getTime()));


        mCells = new ArrayList<>();
        Calendar calendar = (Calendar) mCurrCalender.clone();
        // 1. 计算一个月的第一天是从星期几开始
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        // 国外每个星期第一天是星期日 所以需要减1
        // 计算前面还有多少天
        int preDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        // 将calender移动到日历第一个cell
        calendar.add(Calendar.DAY_OF_MONTH, -preDay);
        // 2. 日历需要展开多少行（最多6行）
        int maxCellCount = 6 * 7;
        if (preDay + mCurrCalender.getActualMaximum(Calendar.DATE) > 35) {
            maxCellCount = 6 * 7;
        }else{
            maxCellCount = 5 * 7;
        }
        while (mCells.size() < maxCellCount) {
            mCells.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        mGvCalender.setAdapter(new CalenderAdapter(getContext()));
    }

    /**
     * 初始化星期
     *
     * @param weak;
     * @return
     */
    private TextView getWeakText(String weak) {
        TextView tv = new TextView(getContext());
        tv.setText(weak);
        tv.setGravity(Gravity.CENTER);
        LayoutParams lp = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        tv.setLayoutParams(lp);
        return tv;
    }


    private class CalenderAdapter extends BaseAdapter {

        private LayoutInflater mLayoutInflater;

        public CalenderAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            return mCells.size();
        }

        @Override
        public Object getItem(int position) {
            return mCells.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.ui_item_calendar_cell, parent, false);
                ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                layoutParams.height = NewCalender.this.getWidth()/7;
                convertView.setLayoutParams(layoutParams);
            }
            if (convertView instanceof TextView) {
                Date date = mCells.get(position);
                int day = date.getDate();
                ((TextView) convertView).setText(day + "");
                Date nowDate = new Date();
                // 1. 判断是不是当月
                if (date.getMonth() == nowDate.getMonth() && date.getYear() ==nowDate.getYear()) {
                    ((TextView) convertView).setTextColor(Color.BLACK);
                } else {
                    ((TextView) convertView).setTextColor(Color.GRAY);
                }
                // 2. 判断是不是当日
                if (date.getDate() == nowDate.getDate() && date.getMonth() == nowDate.getMonth() && date.getYear() ==
                        nowDate.getYear()) {
                    ((TextView) convertView).setTextColor(Color.RED);
                }


            }

            return convertView;
        }
    }
}
