package com.yc.peddemo.customview;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.yc.peddemo.DialogType;
import com.yc.peddemo.R;

public class ShowAlphaDialog {

    public static void show(int type, Context mContext) {
        CustomAutoQuitDialog.Builder mCustomAutoQuitDialog = new CustomAutoQuitDialog.Builder(mContext);
        mCustomAutoQuitDialog.create().show();
        switch (type) {

            case DialogType.DIALOG_NETWORK_DISABLE:
                mCustomAutoQuitDialog.setMessage(mContext.getResources().getString(R.string.network_disable));
                break;

            case DialogType.DIALOG_FREQUENT_ACCESS_SERVER:
                mCustomAutoQuitDialog.setMessage(mContext.getResources().getString(
                        R.string.frequent_access_server));
                mCustomAutoQuitDialog.setImageRes(R.drawable.icon_cha);
                break;
            case DialogType.DIALOG_DISCONNECT:
                String text = mContext.getResources().getString(R.string.have_not_connect_ble);
                mCustomAutoQuitDialog.setMessage(text);
                break;


            case DialogType.DIALOG_SET_OK:
                mCustomAutoQuitDialog.setMessageVisible(false);
                mCustomAutoQuitDialog.setImageRes(R.drawable.icon_gou);
                break;
            default:
                break;
        }
    }

    static Dialog noticeDialog = null;
    public static void showNoTitleNormalDialog(Context mContext, int type,String msg) {
        if (noticeDialog != null && noticeDialog.isShowing()) {
            noticeDialog.dismiss();
        }
        NoTitleDoubleDialog.Builder builder = new NoTitleDoubleDialog.Builder(mContext);
        builder.setPositiveButton(mContext.getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        builder.setNegativeButton(mContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        noticeDialog = builder.create();
        noticeDialog.show();
        builder.setMessage(msg);
        switch (type) {
            case DialogType.DIALOG_OPEN_NOTIFY:
                break;
            default:
                builder.setNegativeVisibility(false);
                break;
        }

    }
    public static void dismissNoTitleNormalDialog() {
        if (noticeDialog != null && noticeDialog.isShowing()) {
            noticeDialog.dismiss();
        }

    }


    public static CustomCardProgressDialog.Builder mCustomCardProgressDialog;
    public static void startProgressDialog(String msg,Context mContext) {
        if (mCustomCardProgressDialog == null) {
            mCustomCardProgressDialog = new CustomCardProgressDialog.Builder(mContext);
            mCustomCardProgressDialog.create().show();
            mCustomCardProgressDialog.setMessage(msg);
        }
    }

    public static void dismissProgressDialog() {
        if (mCustomCardProgressDialog != null) {
            mCustomCardProgressDialog.dismissDialog();
            mCustomCardProgressDialog = null;
        }
    }
}
