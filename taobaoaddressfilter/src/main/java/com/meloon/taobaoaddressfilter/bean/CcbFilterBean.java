package com.meloon.taobaoaddressfilter.bean;

/**
 * Author      :meloon
 * Date        :2018/10/29
 * Description : 建行地区选择实体类
 */
public class CcbFilterBean {


    private String address;// 地区
    private boolean isSelect;//是否选择了
    private boolean isFocus;// 是否有焦点，即是否在进行筛选

    public boolean isFocus() {
        return isFocus;
    }

    public void setFocus(boolean focus) {
        isFocus = focus;
    }

    public CcbFilterBean(String address, boolean isSelect, boolean isFocus) {

        this.address = address;
        this.isSelect = isSelect;
        this.isFocus = isFocus;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
