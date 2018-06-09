package com.ljb.passworddialog.widget;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;


import com.ljb.passworddialog.R;
import com.ljb.passworddialog.adapter.Holder;
import com.ljb.passworddialog.adapter.SimpleAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Author      :ljb
 * Date        :2018/3/28
 * Description :
 */

public class PassWordDialog extends DialogFragment {
    private List<String> mNums;
    Integer[] orderNums = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
    private PassWordWidget mPww;
    /*密码个数*/
    private int count;
    private List<String> mPsd = new ArrayList<>();
    private OnPassWordListener mOnPassWordListener;

    public void setOnPassWordListener(OnPassWordListener onPassWordListener) {
        mOnPassWordListener = onPassWordListener;
    }

    @Override
    public void onStart() {
        super.onStart();
        WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();
        lp.width = getResources().getDisplayMetrics().widthPixels;
        // 动画
        lp.windowAnimations = R.style.PswDialog;
        lp.gravity = Gravity.BOTTOM;
        setCancelable(false);
        getDialog().getWindow().setAttributes(lp);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.view_psw_dialog, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initView(view);

    }

    private void initView(View view) {
        RecyclerView rvKeyBoard = view.findViewById(R.id.rv_keyboard);
        rvKeyBoard.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvKeyBoard.addItemDecoration(new GridLayoutItemDecoration(3, mNums.size()));
        SimpleAdapter adapter = new SimpleAdapter(mNums);
        rvKeyBoard.setAdapter(adapter);

        mPww = view.findViewById(R.id.pww);

        ImageView ivBack = view.findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        adapter.setItemClickListener(new SimpleAdapter.OnItemClickListener() {
            @Override
            public void Onclick(int position, Holder holder) {
                String data = mNums.get(position);
                if (data.equals("00")) {// 删除
                    count--;
                    removePsd();
                } else if (data.equals("01")) { //确认
                    if (mOnPassWordListener != null) {
                        mOnPassWordListener.getPassword(mPsd.toString());
                    }
                } else {
                    count++;
                    addPsd(data);
                }
                if (count > 6) {
                    count = 6;
                } else if (count < 0) {
                    count = 0;
                }
                mPww.upDataCount(count);

            }
        });

    }

    private void addPsd(String data) {
        if (mPsd.size() < 6) {
            mPsd.add(data);
        }
    }

    /**
     * 删除密码
     */
    private void removePsd() {
        if (mPsd.size() - 1 >= 0) {
            mPsd.remove(mPsd.size() - 1);
        }
    }


    /**
     * 初始化数据
     */
    private void initData() {
        //键盘按键数
        int keyBoardCount = 12;
        List<Integer> orderList = Arrays.asList(orderNums);
        Collections.shuffle(orderList);
        mNums = new ArrayList<>();
        for (int i = 0; i < keyBoardCount; i++) {
            if (i == keyBoardCount - 3) {
                mNums.add("00");//删除
            } else if (i == keyBoardCount - 1) {
                mNums.add("01");//确认
            } else {
                if (i == keyBoardCount - 2) {
                    mNums.add(orderList.get(i - 1) + "");
                } else {
                    mNums.add(orderList.get(i) + "");
                }

            }
        }
    }

    public interface OnPassWordListener {
        void getPassword(String psw);
    }
}
