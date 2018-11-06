package com.meloon.taobaoaddressfilter.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meloon.taobaoaddressfilter.R;
import com.meloon.taobaoaddressfilter.adapter.BaseRvAdapter;
import com.meloon.taobaoaddressfilter.adapter.BaseRvHolder;
import com.meloon.taobaoaddressfilter.adapter.PopChoiceAdapter;

import java.util.ArrayList;



/**
 * A simple {@link Fragment} subclass.
 */
public class ChoiceFragment extends Fragment {


    private View mRoot;
    private RecyclerView mRvChoice;
    public static final String DATA_KEY = "data_key";
    private ArrayList<String> mDatas;
    private int flag;
    private PopChoiceAdapter mAdapter;
    private OnFragmentRvClickListener mOnFragmentRvClickListener;

    public void setOnFragmentRvClickListener(OnFragmentRvClickListener onFragmentRvClickListener) {
        mOnFragmentRvClickListener = onFragmentRvClickListener;
    }

    public static ChoiceFragment newInstance(ArrayList<String> list, int flag) {
        ChoiceFragment fragment = new ChoiceFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(DATA_KEY, list);
        fragment.setArguments(bundle);
        fragment.flag = flag;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRoot == null) {
            mRoot = inflater.inflate(R.layout.fragment_city_choice, container, false);
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            mDatas = bundle.getStringArrayList(DATA_KEY);
        }
        if (mDatas == null) {
            mDatas = new ArrayList<>();
        }
        return mRoot;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initView(view);
    }

    private void initView(View view) {
        mRvChoice = view.findViewById(R.id.rv_choice);
        mRvChoice.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mAdapter = new PopChoiceAdapter(mDatas);
        mRvChoice.setAdapter(mAdapter);
        mAdapter.setItemClickListener(new BaseRvAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, BaseRvHolder holder) {
                if (mOnFragmentRvClickListener != null) {
                    mOnFragmentRvClickListener.onClick(position, flag);
                }
            }
        });

    }

    public interface OnFragmentRvClickListener {
        void onClick(int positon, int flag);
    }
}
