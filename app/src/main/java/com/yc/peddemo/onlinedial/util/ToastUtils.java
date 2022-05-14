package com.yc.peddemo.onlinedial.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    public static final String TAG = "Utils";


    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

}
