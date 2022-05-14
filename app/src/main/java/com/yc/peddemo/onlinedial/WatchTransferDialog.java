package com.yc.peddemo.onlinedial;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.yc.pedometer.utils.LogUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.makeramen.roundedimageview.RoundedImageView;
import com.yc.peddemo.R;
import com.yc.peddemo.customview.ShowAlphaDialog;
import com.yc.peddemo.onlinedial.util.BitmapUtil;
import com.yc.pedometer.dial.HttpDownloader;
import com.yc.pedometer.dial.DensityUtil;
import com.yc.pedometer.dial.OnlineDialUtil;
import com.yc.pedometer.dial.PicUtils;
import com.yc.peddemo.onlinedial.util.ToastUtils;
import com.yc.pedometer.dial.UIFile;
import com.yc.pedometer.dial.WatchChanged;
import com.yc.pedometer.listener.WatchSyncProgressListener;
import com.yc.pedometer.sdk.WriteCommandToBLE;
import com.yc.pedometer.utils.GlobalVariable;
import com.yc.pedometer.utils.SPUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class WatchTransferDialog extends Dialog  {

    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSize)
    TextView txtSize;
    @BindView(R.id.txtDownNum)
    TextView txtDownNum;
    @BindView(R.id.ivPic)
    RoundedImageView ivPic;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.txtSync)
    TextView txtSync;
    @BindView(R.id.rlBottom)
    RelativeLayout rlBottom;
    private Context context;
    UIFile uiFile;
    OnUpgradeStateValue onUpgradeStateValue;

    public interface OnUpgradeStateValue {
          void onCompelete();
    }

    public enum Status {
        NotStart,
        DownLoading,
        UPGRADING,
        FINISH,
        NETERROR,
        BLUETOOTHERROR
    }


    public WatchTransferDialog(Context context, Status status, UIFile uiFile, OnUpgradeStateValue onUpgradeStateValue) {
        super(context, R.style.ActionSheetDialogStyle);
        this.context = context;
        this.uiFile = uiFile;
        this.state = status;
        this.onUpgradeStateValue = onUpgradeStateValue;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        init();
        Window dialogWindow = this.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = DensityUtil.getDisplayWidth(getContext()) - DensityUtil.dp2px(getContext(),76);
        dialogWindow.setAttributes(lp);
        setCanceledOnTouchOutside(false);
        EventBus.getDefault().register(this);
        IntentFilter mFilter =new IntentFilter();
        mFilter.addAction(GlobalVariable.ACTION_GATT_CONNECTED);
        mFilter.addAction(GlobalVariable.ACTION_GATT_CONNECT_FAILURE);
        context.registerReceiver(mReceiver,mFilter);
    }


    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_pic_upgrade, null);
        setContentView(view);
        ButterKnife.bind(this, view);
        update();
        if (txtSync.getText().toString().equals(context.getResources().getString(R.string.syncWathch))) {
            txtSync.setClickable(true);
        } else {
            txtSync.setClickable(false);
        }
    }

    void update() {
        txtTitle.setText(uiFile.getTitle());
        //下载次数按照软件会议讨论处理，由服务器统计次数，应用端获取服务器下载次数，小于1000的显示1k+，以此类推2k+…
        String NumdownLoad = String.format(context.getResources().getString(R.string.NumdownLoad), (uiFile.getDownload_num() / 1000 + 1));
        txtDownNum.setText(NumdownLoad);
        String SizedownLoad = String.format(context.getResources().getString(R.string.SizedownLoad), (uiFile.getCapacity()/1024));
        txtSize.setText(SizedownLoad);
//        if (!TextUtils.isEmpty(uiFile.getResouce())) {
//            BitmapUtil.loadBitmap(getContext(),uiFile.getPreview(),uiFile.getPreview(),ivPic);
        if (NetworkUtils.getInstance(getContext()).isNetworkAvailable()) {
            BitmapUtil.loadBitmap(getContext(), uiFile.getPreview(), ivPic);
        }
        if (uiFile.getShape() == (Integer.valueOf(PicUtils.DIAL_TYPE_CIRCLE))) {
            ivPic.setOval(true);
        } else {
            ivPic.setOval(false);
        }
//        } else if (uiFile.getId() != 0) {
//          ivPic.setBackgroundResource(uiFile.getId());
//        }

        switch (state) {
            case NotStart:
                break;
            case DownLoading:
                break;
            case UPGRADING:
                break;
            case FINISH:
                break;

             case NETERROR:
                break;
            case BLUETOOTHERROR:
                break;
        }
    }

    Status state ;
    @OnClick(R.id.txtSync)
    public void onViewClicked() {
        if (!NetworkUtils.getInstance(context).isNetworkAvailable()) {
            ToastUtils.showToast(context, context.getString(R.string.downError));
            return;
        }
        if (!SPUtil.getInstance(context).getBleConnectStatus()) {
            ToastUtils.showToast(context, context.getString(R.string.have_not_connect_ble));
            return;
        }
        txtSync.setClickable(false);
        OnlineDialUtil.getInstance().setDialStatus(OnlineDialUtil.DialStatus.RegularDial);
        if (state == Status.UPGRADING) {
            txtSync.setText(context.getString(R.string.syncing));
            OnlineDialUtil.LogI("state == Status.UPGRADING");
            syncWatch();
        }

        if (state != Status.NotStart) {
            return;
        }
        if (state == Status.NotStart) {
            state = Status.DownLoading;
            txtSync.setText(context.getString(R.string.downLoading));
            Flowable.just(0).map(integer -> {
                int status = HttpDownloader.downloadFile(uiFile.getResouce(),
                        context.getExternalFilesDir(null) + "/", uiFile.getTitle() + ".bin");
                OnlineDialUtil.LogI("下载结果状态 =" + status);
                if (status == -1) {
                    throw new Exception(context.getString(R.string.downError));
//                    return null;
                }
//                if (status == 0) {
//                    if (GlobalVariable.SYNC_CLICK_ENABLE) {
//                        WriteCommandToBLE.getInstance(context).prepareSendOnlineDialData();
//                    } else {
//                        SyncParameterUtils.getInstance(context).addCommandIndex(SyncParameterUtils.SYNC_DILA_PREPARE_SEND);
//                    }
//                }
                return status;
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(v -> {
                        state = Status.UPGRADING;
                        txtSync.setText(context.getString(R.string.syncing));
                        syncWatch();
                    }, throwable -> {
                        ToastUtils.showToast(context, context.getString(R.string.downError));
                        txtSync.setText(context.getString(R.string.syncWathch));
                        txtSync.setClickable(true);
                    });
        } else if (state == Status.UPGRADING) {
            txtSync.setText(context.getString(R.string.syncing));
            syncWatch();
        }
    }
    void syncWatch() {

        OnlineDialUtil.LogI("下载完成，开始同步");
//        if (GlobalVariable.SYNC_CLICK_ENABLE) {
            WriteCommandToBLE.getInstance(context).prepareSendOnlineDialData();
//        } else {
//            SyncParameterUtils.getInstance(context).addCommandIndex(SyncParameterUtils.SYNC_DILA_PREPARE_SEND);
//        }
        WriteCommandToBLE.getInstance(context).setWatchSyncProgressListener(new WatchSyncProgressListener() {
            @Override
            public void WatchSyncProgress(int progress) {
                if (progressBar.getProgress() != progress) {
                    if (progress == 0) {
                        txtSync.setTextColor(context.getResources().getColor(R.color.fontColor));
                    } else if (progress == 100) {
                        txtSync.setText(context.getString(R.string.sync_finish));
                        txtSync.setTextColor(context.getResources().getColor(R.color.white));
                        if (onUpgradeStateValue != null) {
                            onUpgradeStateValue.onCompelete();
                            Flowable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(s -> {
                                dismissDialog();
                            });
                        }
                    }
                    progressBar.setProgress(progress);
                }
            }
        });
    }
    public void dismissDialog() {
        EventBus.getDefault().unregister(this);
        context.unregisterReceiver(mReceiver);
        dismiss();
    }

    @Subscribe
    public void wach(WatchChanged e) {
        OnlineDialUtil.LogI("表盘对话框收到断开连接");
        if (e != null && e.changePos == WatchChanged.disconnected) {
//            ShowAlphaDialog.showNoTitleNormalDialog(context,DIALOG_TYPE.DIALOG_DISCONNECT, context.getResources().getString(R.string.bluetoothDisconnect));
        } else if (e != null && e.changePos == WatchChanged.connected) {
//            ShowAlphaDialog.dismissNoTitleNormalDialog();
        }
    }
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        doFinish();
    }

    private void doFinish() {
        if (txtSync.getText().toString().equals(context.getResources().getString(R.string.syncing))
                ||txtSync.getText().toString().equals(context.getResources().getString(R.string.downLoading))) {
            ToastUtils.showToast(context, txtSync.getText().toString());
        } else {
            dismissDialog();
        }
    }
    private Handler mHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    ShowAlphaDialog.dismissNoTitleNormalDialog();
                    break;
                case 1:
                    txtSync.setText(context.getString(R.string.bluetoothDisconnect));
                    break;
            }
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            LogUtils.i("stringBuilder", "action=" + action);
            if (action.equals(GlobalVariable.ACTION_GATT_CONNECTED)) {
                mHandler.sendEmptyMessage(0);
            } else if (action.equals(GlobalVariable.ACTION_GATT_CONNECT_FAILURE)) {
                mHandler.sendEmptyMessage(1);
            }
        }
    };
}
