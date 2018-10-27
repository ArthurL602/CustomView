package com.ljb.viewpagerindicator;

import android.view.View;

/**
 * Author      :ljb
 * Date        :2018/6/22
 * Description :
 */
public abstract class IVpIndicatorAdapter {
    public abstract int getCount();

    public abstract View getView(int position);

}
