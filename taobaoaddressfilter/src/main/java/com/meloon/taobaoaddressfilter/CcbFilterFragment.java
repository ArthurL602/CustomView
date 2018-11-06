package com.meloon.taobaoaddressfilter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.meloon.taobaoaddressfilter.adapter.CcbPopFilterAdapter;
import com.meloon.taobaoaddressfilter.adapter.PopVpAdapter;
import com.meloon.taobaoaddressfilter.bean.CcbFilterBean;
import com.meloon.taobaoaddressfilter.fragment.ChoiceFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Author      :meloon
 * Date        :2018/10/29
 * Description :
 */
@SuppressLint("ValidFragment")
public class CcbFilterFragment extends DialogFragment {

    private ImageView mIvCancel;
    private RecyclerView mRvFilter;
    private List<CcbFilterBean> mFilters;
    private String[] citys = new String[]{"长沙市", "益阳", "常德", "郴州", "永州", "岳阳", "株洲", "邵阳", "自治州",//
            "娄底", "怀化", "张家界", "湘潭", "衡阳"};
    private ArrayList<String> mCitys;
    private ArrayList<String> mDistricts;
    private ArrayList<String> mDots;
    private CcbPopFilterAdapter mFilterAdapter;
    public static final int FLAG_CITY = 0;// 选择城市
    public static final int FLAG_DISTRICT = 1;// 选择地区
    public static final int FLAG_DOT = 2;//选择网点

    public int mCurrentFlag = FLAG_CITY;//当前选择的
    private ViewPager mViewPager;
    private List<Fragment> mFragments;
    private ChoiceFragment mCityFragment;
    private ChoiceFragment mAddressFragment;
    private ChoiceFragment mDotFragment;
    private String mCity;// 城市
    private String mDistrict; // 地区
    @Override
    public void onStart() {
        super.onStart();
        int color = Color.parseColor("#00000000");
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(color));
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.gravity = Gravity.BOTTOM;
        window.setAttributes(attributes);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {

        View contentView = inflater.inflate(R.layout.ui_pop_filter_dot, null);

        return contentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initView(view.getContext(), view);
        initEvent();
    }


    private void initData() {
        mFilters = new ArrayList<>();
        mFilters.add(new CcbFilterBean("长沙", true, false));
        mFilters.add(new CcbFilterBean("请选择地区", false, true));

        mDistricts = new ArrayList<>();
        mDots = new ArrayList<>();
        mCitys = new ArrayList<>();
        for (int i = 0; i < citys.length; i++) {
            mCitys.add(citys[i]);
            mDistricts.add(citys[i] + "的地级单位");
            mDots.add(citys[i] + "的网点");
        }

        mFragments = new ArrayList<>();
    }

    private void initEvent() {
        mIvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        // 一级Rv
        mFilterAdapter.setListener(new CcbPopFilterAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(View view, int position) {
                switch (position) {
                    case 0:
                        mCurrentFlag = FLAG_CITY; //选择城市
                        mViewPager.setCurrentItem(0);
                        break;
                    case 1:
                        mCurrentFlag = FLAG_DISTRICT;// 选择地区
                        mViewPager.setCurrentItem(1);
                        break;
                    case 2:
                        mCurrentFlag = FLAG_DOT;//选择网点
                        mViewPager.setCurrentItem(2);
                        break;
                }
                // 选择的城市标红，其他的恢复
                mFilterAdapter.resetFocus();
                mFilters.get(position).setFocus(true);

                // 判断是否有地区选择，没有就添加地区选择，如果有就不操作
                if (!isDistrictAdd()) {
                    mFilterAdapter.resetFocus();
                    mFilters.add(new CcbFilterBean("请选择地区", false, true));
                    mViewPager.setCurrentItem(1);
                }
                // 判断是否有网点选择(地区已经选择了)，没有就添加网点选择，如果有就不操作
                if (!isDotAdd() && mFilters.get(1).isSelect()) {
                    mFilterAdapter.resetFocus();
                    mFilters.add(new CcbFilterBean("请选择网点", false, true));
                    mViewPager.setCurrentItem(2);
                }
                mFilterAdapter.notifyDataSetChanged();
                // 获取地区
            }


        });
        // 城市的选择
        mCityFragment.setOnFragmentRvClickListener(new ChoiceFragment.OnFragmentRvClickListener() {
            @Override
            public void onClick(int positon, int flag) {
                mCity = mCitys.get(positon);
                mFilters.get(0).setAddress(mCity);

                // 清空网点 、 地级
                clearDataExcludeIndex(mFilters, 0);
                // 添加地级
                mFilters.get(0).setFocus(false);
                mFilters.add(new CcbFilterBean("请选择地区", false, true));
                // 通过市级获取地级位置
                mFilterAdapter.notifyDataSetChanged();
                mViewPager.setCurrentItem(1);
            }
        });
        // 地区的选择
        mAddressFragment.setOnFragmentRvClickListener(new ChoiceFragment.OnFragmentRvClickListener() {



            @Override
            public void onClick(int positon, int flag) {
                mDistrict = mDistricts.get(positon);
                mFilters.get(1).setAddress(mDistrict);
                mFilters.get(1).setFocus(false);
                mFilters.get(1).setSelect(true);
                // 清空网点 、 地级
                clearDataExcludeIndex(mFilters, 1);
                // 添加地级
                mFilters.add(new CcbFilterBean("请选择网点", false, true));
                // 通过地级获取网点位置
                mFilterAdapter.notifyDataSetChanged();
                mViewPager.setCurrentItem(2);
            }
        });
        // 网点的选择
        mDotFragment.setOnFragmentRvClickListener(new ChoiceFragment.OnFragmentRvClickListener() {
            @Override
            public void onClick(int positon, int flag) {
                String dot = mDots.get(positon);
                Toast.makeText(getContext(), dot, Toast.LENGTH_LONG).show();
                dismiss();
            }
        });
    }

    /**
     * 清理数据
     *
     * @param datas
     * @param index 从0-index位保留
     */
    private void clearDataExcludeIndex(List<?> datas, int index) {
        if (index > datas.size()) {
            return;
        }

        for (int i = index+1; i < datas.size(); i++) {
            datas.remove(datas.get(i));
            i--;
        }
    }

    /**
     * 是否添加了网点
     *
     * @return
     */
    private boolean isDotAdd() {
        return mFilters.size() >= 3;
    }

    private void initView(Context context, View view) {
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
        mIvCancel = view.findViewById(R.id.iv_cancel); // 退出


        // 选择城市 选择地区 选择网点
        mRvFilter = view.findViewById(R.id.rv_filter);
        mFilterAdapter = new CcbPopFilterAdapter(mFilters);
        mRvFilter.setLayoutManager(new LinearLayoutManager(context));
        mRvFilter.setAdapter(mFilterAdapter);
        // 城市、地区、网点详细展示
        mViewPager = view.findViewById(R.id.vp);
        // 初始化Fragment
        mCityFragment = ChoiceFragment.newInstance(mCitys, FLAG_CITY);
        mAddressFragment = ChoiceFragment.newInstance(mDistricts, FLAG_DISTRICT);
        mDotFragment = ChoiceFragment.newInstance(mDots, FLAG_DOT);
        mFragments.add(mCityFragment);
        mFragments.add(mAddressFragment);
        mFragments.add(mDotFragment);

        PopVpAdapter adapter = new PopVpAdapter(getChildFragmentManager(), mFragments);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(1);

    }

    /**
     * 是否地区已经添加
     *
     * @return
     */
    public boolean isDistrictAdd() {
        return mFilters.size() >= 2;
    }
}
