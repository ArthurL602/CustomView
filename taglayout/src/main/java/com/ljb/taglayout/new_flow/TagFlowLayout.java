package com.ljb.taglayout.new_flow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Author      :ljb
 * Date        :2019/9/22
 * Description :
 */
public class TagFlowLayout extends FlowLayout {

    private TagAdapter mTagAdapter;

    private int mMaxSelectedCount = 0;

    public void setMaxSelectedCount(int maxSelectedCount) {
        mMaxSelectedCount = maxSelectedCount;
    }

    public TagFlowLayout(Context context) {
        super(context);
    }

    public TagFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAdapter(TagAdapter adapter) {
        mTagAdapter = adapter;

        mTagAdapter.setOnDataChangedListener(new TagAdapter.OnDataChangedListener() {
            @Override
            public void onDataChanged() {
                onDataChange();
            }
        });
        onDataChange();
    }

    private void onDataChange() {
        removeAllViews();
        for (int i = 0; i < mTagAdapter.getItemCount(); i++) {
            View view = mTagAdapter.createView(LayoutInflater.from(getContext()), this, i);
            mTagAdapter.bindView(view, i);
            addView(view);
            if (view.isSelected()) {
                mTagAdapter.onItemSelected(view, i);
            } else {
                mTagAdapter.onItemUnSelected(view, i);
            }
            bindViewMethod(view, i);
        }
    }

    private void bindViewMethod(View view, final int position) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTagAdapter.setOnItemClick(v, position);
                if (mMaxSelectedCount <= 0) {
                    return;
                }
                //特殊case
                if (!v.isSelected()) {
                    if (getSelectedViewCount() >= mMaxSelectedCount) {
                        // TODO: 2019/9/22 单选
                        if (getSelectedViewCount() == 1) {
                            View selectedView = getSelectedView();
                            if (selectedView != null) {
                                selectedView.setSelected(false);
                                mTagAdapter.onItemUnSelected(selectedView, getViewPosition(selectedView));
                            }
                            v.setSelected(true);
                            mTagAdapter.onItemSelected(v, position);
                        } else {
                            mTagAdapter.tipForSelectedMax(v, mMaxSelectedCount);
                        }
                        return;
                    }
                }

                if (v.isSelected()) {
                    v.setSelected(false);
                    mTagAdapter.onItemUnSelected(v, position);
                } else {
                    v.setSelected(true);
                    mTagAdapter.onItemSelected(v, position);
                }

            }
        });

    }


    public List<Integer> getSelectedItemPosition() {
        List<Integer> selectedItemPosition = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view.isSelected()) {
                selectedItemPosition.add(i);
            }
        }
        return selectedItemPosition;
    }

    private int getViewPosition(View selectedView) {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) == selectedView) {
                return i;
            }
        }
        return 0;
    }

    private View getSelectedView() {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).isSelected()) {
                return getChildAt(i);
            }
        }
        return null;
    }

    private int getSelectedViewCount() {
        int result = 0;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i).isSelected()) {
                result++;
            }
        }
        return result;
    }
}
