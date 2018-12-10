package com.ljb.superedittext;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Author      :ljb
 * Date        :2018/3/28
 * Description : 银行卡和手机号码格式自定义EditText
 */
public class SuperEditText extends android.support.v7.widget.AppCompatEditText implements TextWatcher {
    public static final int BANK_CARD = 0X000001;
    public static final int PHONE_NUM = 0X000002;
    private int mCurrentCode = PHONE_NUM;


    public SuperEditText(Context context) {
        super(context);
    }

    public SuperEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SuperEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
addTextChangedListener(this);
    }

    public void setCode(int code) {
        mCurrentCode = code;
        Log.e("TAG", "eee");
    }

    private int mLastLen;


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }
    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        String data = text.toString();
        int length = data.length();
        boolean isAdd = (length - mLastLen) > 0;
        mLastLen = length;
        switch (mCurrentCode) {
            case BANK_CARD:
                setBankCard(data, length, isAdd);
                break;
            case PHONE_NUM:
                setPhoneNum(data, length, isAdd);
                break;
        }

    }

    @Override
    public void afterTextChanged(Editable s) {
a(s);
    }

    /**
     * 设置手机号码格式
     *
     * @param data
     * @param length
     * @param isAdd
     */
    private void setPhoneNum(String data, int length, boolean isAdd) {
        if (isAdd) {
            if (length == 4) {
                String sub1 = data.substring(0, length - 1);
                String sub2 = data.substring(length - 1, length);
                setText(sub1 + " " + sub2);
                setSelection(length + 1);
            }else if(length==9){
                String sub1 = data.substring(0, length - 1);
                String sub2 = data.substring(length - 1, length);
                setText(sub1 + " " + sub2);
                setSelection(length + 1);
            }
        }else {
            if (length == 4) {
                String sub = data.substring(0, length - 1);
                setText(sub);
                setSelection(length - 1);
            }else if(length==9){
                String sub = data.substring(0, length - 1);
                setText(sub);
                setSelection(length - 1);
            }
        }

    }

    /**
     * 用于规范银行卡号
     * @param s
     */
    private void a(Editable s){
        String str = s.toString().trim().replace(" ", "");
        String result = "";
        if (str.length() >= 4) {
           removeTextChangedListener(this);
            for (int i = 0; i < str.length(); i++) {
                result += str.charAt(i);
                if ((i + 1) % 4 == 0) {
                    result += " ";
                }
            }
            if (result.endsWith(" ")) {
                result = result.substring(0, result.length() - 1);
            }
            setText(result);
           addTextChangedListener(this);
            setSelection(getText().toString().length());//焦点到输入框最后位置
        }
    }
    /**
     * 银行卡格式
     *
     * @param data
     * @param length
     * @param isAdd
     */
    private void setBankCard(String data, int length, boolean isAdd) {
        if (isAdd) {
            if (length % 5 == 0) {
                String sub1 = data.substring(0, length - 1);
                String sub2 = data.substring(length - 1, length);
                setText(sub1 + " " + sub2);
                setSelection(length + 1);
            }
        } else {
            if (length - 1 < 0) return;
            if (length % 5 == 0) {
                String sub = data.substring(0, length - 1);
                setText(sub);
                setSelection(length - 1);
            }
        }

//        if (length == 5) {
//
//        }else if(length ==10){
//            String sub1 = data.substring(0, 9);
//            String sub2 = data.substring(9, length);
//            setText(sub1 + " " + sub2);
//            setSelection(length+1);
//        }else if(length==15){
//            String sub1 = data.substring(0, 14);
//            String sub2 = data.substring(14, length);
//            setText(sub1 + " " + sub2);
//            setSelection(length+1);
//        }else if(length==20){
//            String sub1 = data.substring(0, 19);
//            String sub2 = data.substring(19, length);
//            setText(sub1 + " " + sub2);
//            setSelection(length+1);
//        }
    }
}
