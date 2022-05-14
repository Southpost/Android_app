package com.yc.peddemo.onlinedial;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;



public class CustomDialGridView extends GridView {
    public boolean isOnMeasure;
    public CustomDialGridView(Context context) {
        super(context);
    }

    public CustomDialGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public CustomDialGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        isOnMeasure = true;
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        isOnMeasure = false;
        super.onLayout(changed, l, t, r, b);
    }

}
