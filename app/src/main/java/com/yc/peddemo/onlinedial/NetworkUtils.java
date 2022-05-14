package com.yc.peddemo.onlinedial;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.yc.pedometer.utils.LogUtils;

public class NetworkUtils {
    private static NetworkUtils instance = null;
    private static Context mContext;

    public static NetworkUtils getInstance(Context context) {
        if (instance == null || mContext == null) {
            instance = new NetworkUtils(context);
        }
        return instance;

    }

    private NetworkUtils(Context context) {
        mContext = context;
    }

    public boolean isNetworkAvailable() {
        if (mContext != null) {
            ConnectivityManager cm = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
            } else {
                NetworkInfo[] info = cm.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        } else {
            LogUtils.e("isNetworkAvailable", "mContext =" + mContext);
        }
        return false;
    }
}
