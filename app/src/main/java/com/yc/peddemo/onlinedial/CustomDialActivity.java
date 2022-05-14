package com.yc.peddemo.onlinedial;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.makeramen.roundedimageview.RoundedImageView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.yc.peddemo.DialogType;
import com.yc.peddemo.R;
import com.yc.peddemo.customview.ShowAlphaDialog;
import com.yc.peddemo.onlinedial.util.ToastUtils;
import com.yc.pedometer.dial.OnlineDialUtil;
import com.yc.pedometer.dial.PicUtils;
import com.yc.pedometer.dial.Rgb;
import com.yc.pedometer.dial.WatchChanged;
import com.yc.pedometer.listener.OnlineDialListener;
import com.yc.pedometer.listener.WatchSyncProgressListener;
import com.yc.pedometer.sdk.BLEServiceOperate;
import com.yc.pedometer.sdk.BluetoothLeService;
import com.yc.pedometer.sdk.WriteCommandToBLE;
import com.yc.pedometer.utils.GlobalVariable;
import com.yc.pedometer.utils.LogUtils;
import com.yc.pedometer.utils.SPUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class CustomDialActivity extends AppCompatActivity implements OnlineDialListener {
    private final String TAG = "CustomDialActivity";
    private final String DIAL_BG = "dial_bg.png";
    private Context mContext;
    @BindView(R.id.xxListView)
    GridView xxListView;
    @BindView(R.id.dial_select_pic)
    TextView dial_select_pic;
    @BindView(R.id.dial_reset)
    TextView dial_reset;
    @BindView(R.id.syncWathch)
    TextView syncWathch;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.mergeBitmap)
    RoundedImageView mergeBitmap;
    Unbinder unbinder;
    private Bitmap mDialPreViewBitmap = null;
    private Bitmap mDialBackgroundBitmap = null;//背景图片

    private FontColorGirdAdapter mFontColorGirdAdapter;
    private int currentFontColor = 0xFFFFFF;
    private boolean isFontColorChange = false;
    private int fontColorPosition = 0;
    private int[] fontColorIdList = {R.color.dialFontColor0, R.color.dialFontColor1, R.color.dialFontColor2, R.color.dialFontColor3,
            R.color.dialFontColor4, R.color.dialFontColor5, R.color.dialFontColor6, R.color.dialFontColor7,
            R.color.dialFontColor8, R.color.dialFontColor9, R.color.dialFontColor10, R.color.dialFontColor11};

    private int pickerPosition = 0;
    private int[] colorPickerList = null;
    private String dialType = PicUtils.DIAL_TYPE_CIRCLE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_dial);
        mContext = getApplicationContext();
        unbinder = ButterKnife.bind(this);
        Intent intent = getIntent();
        dialType = intent.getStringExtra(PicUtils.DIAL_TYPE_KEY);
        String folderDial = intent.getStringExtra(PicUtils.DIAL_PATH_SD_KEY);
        int pathStatus = intent.getIntExtra(PicUtils.DIAL_PATH_WHERE_KEY, PicUtils.PATH_STATUS_ASSETS);

        PicUtils.getInstance().setDialType(dialType);
        PicUtils.getInstance().setFolderDial(folderDial);//自定义表盘是sd路径才用到,必须传入正确的路径。自定义表盘是 assets 路径用不到，可以传空
        PicUtils.getInstance().setPathStatus(pathStatus);

        if (dialType.equals(PicUtils.DIAL_TYPE_CIRCLE)) {
            mergeBitmap.setOval(true);
        } else {
            mergeBitmap.setOval(false);
        }
        colorPickerList = getResources().getIntArray(R.array.colorPickerList);
        mFontColorGirdAdapter = new FontColorGirdAdapter(mContext, fontColorIdList);
        xxListView.setAdapter(mFontColorGirdAdapter);
        mFontColorGirdAdapter.setSeclection(fontColorPosition);
        displayDefaultDialPreView();
        xxListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fontColorPosition = position;
                mFontColorGirdAdapter.setSeclection(fontColorPosition);
                if (position == fontColorIdList.length - 1) {
                    showDialColorPickerDialog();
                } else {
                    changeColorToByte(mContext.getResources().getColor(fontColorIdList[position]));
                }
            }
        });
        EventBus.getDefault().register(this);
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(GlobalVariable.ACTION_GATT_CONNECTED);
        mFilter.addAction(GlobalVariable.ACTION_GATT_CONNECT_FAILURE);
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unregisterReceiver(mReceiver);
    }


    @OnClick({R.id.dial_select_pic, R.id.dial_reset, R.id.syncWathch, R.id.back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                doFinish();
                break;
            case R.id.dial_select_pic:
                showDialSelectPicDialog();
                break;
            case R.id.dial_reset:
                displayDefaultDialPreView();
                break;
            case R.id.syncWathch:
                OnlineDialUtil.LogI("点击了syncWathch");
                BluetoothLeService mBluetoothLeService = BLEServiceOperate.getInstance(mContext).getBleService();
                if (mBluetoothLeService != null) {
                    mBluetoothLeService.setOnlineDialListener(this);
                }
                if (!SPUtil.getInstance(mContext).getBleConnectStatus()) {
                    ShowAlphaDialog.show(DialogType.DIALOG_DISCONNECT, CustomDialActivity.this);
                    return;
                }
                prepareSyncWatch();

                break;
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OnlineDialUtil.READ_DEVICE_ONLINE_DIAL_CONFIGURATION_OK:
                    OnlineDialUtil.LogI("Custom获取手环的表盘配置ok");

                    break;
                case OnlineDialUtil.PREPARE_SEND_ONLINE_DIAL_DATA:
                    OnlineDialUtil.LogI("Custom准备发送表盘数据");
                    syncDialData();
                    break;
                case OnlineDialUtil.SEND_ONLINE_DIAL_SUCCESS:
                    OnlineDialUtil.LogI("Custom发送完成，成功");
                    ToastUtils.showToast(mContext, mContext.getResources().getString(R.string.send_online_dial_success));
                    break;
                case OnlineDialUtil.SEND_ONLINE_DIAL_CRC_FAIL:
                    OnlineDialUtil.LogI("Custom发送完成，校验失败");
                    ToastUtils.showToast(mContext, mContext.getResources().getString(R.string.send_online_dial_crc_fail));
                    syncWathch.setTextColor(mContext.getResources().getColor(R.color.white));
                    syncWathch.setText(mContext.getString(R.string.syncWathch));
                    syncWathch.setClickable(true);
                    progressBar.setProgress(100);
                    break;
                case OnlineDialUtil.SEND_ONLINE_DIAL_DATA_TOO_LARGE:
                    OnlineDialUtil.LogI("Custom发送完成，表盘数据太大");
                    ToastUtils.showToast(mContext, mContext.getResources().getString(R.string.send_online_dial_data_too_large));
                    syncWathch.setTextColor(mContext.getResources().getColor(R.color.white));
                    syncWathch.setText(mContext.getString(R.string.syncWathch));
                    syncWathch.setClickable(true);
                    progressBar.setProgress(100);
                    break;
                case 10:
                    ShowAlphaDialog.dismissNoTitleNormalDialog();
                    syncWathch.setText(mContext.getString(R.string.syncWathch));
                    break;
                case 11:
                    ShowAlphaDialog.showNoTitleNormalDialog(CustomDialActivity.this, DialogType.DIALOG_DISCONNECT, mContext.getResources().getString(R.string.bluetoothDisconnect));
                    syncWathch.setText(mContext.getString(R.string.have_not_connect_ble));
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    public void onlineDialStatus(int status) {
        if (OnlineDialUtil.getInstance().getDialStatus() == OnlineDialUtil.DialStatus.CustomDial) {
            OnlineDialUtil.LogI("CustomonlineDialStatus  status =" + status);
            mHandler.sendEmptyMessage(status);
        }
    }

    private final int IMAGE_REQUEST_CODE = 0;
    private final int CAMERA_REQUEST_CODE = 1;
    private final int RESULT_REQUEST_CODE = 2;
    private Uri imageUri;

    private void showDialSelectPicDialog() {
        final DialSelectPicDialog.Builder builder = new DialSelectPicDialog.Builder(CustomDialActivity.this);

        builder.setAlbumonButton(new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                new RxPermissions(CustomDialActivity.this).request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                        if (granted) {
                            Intent intent = new Intent(Intent.ACTION_PICK, null);
                            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                            startActivityForResult(intent, IMAGE_REQUEST_CODE);
                        }
                    }
                });

                dialog.dismiss();
            }
        });
        builder.setPhotoButton(new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                new RxPermissions(CustomDialActivity.this).request(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                        if (granted) {
                            if (hasSdcard()) {
                                File outputImage = new File(getExternalCacheDir(), DIAL_BG);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    imageUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileProvider", outputImage);
                                } else {
                                    imageUri = Uri.fromFile(outputImage);
                                }
                                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                startActivityForResult(intent, CAMERA_REQUEST_CODE);
                            }

                        }
                    }
                });


                dialog.dismiss();
            }
        });
        builder.setCancelButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.i(TAG, "requestCode=" + requestCode + ",resultCode=" + resultCode);
        if (resultCode != RESULT_CANCELED) {

            switch (requestCode) {
                case IMAGE_REQUEST_CODE://
                    startPhotoZoom(data.getData());
                    break;
                case CAMERA_REQUEST_CODE:
                    if (hasSdcard()) {
                        startPhotoZoom(Uri.fromFile(new File(getExternalCacheDir(), DIAL_BG)));
                    } else {
                        Toast.makeText(mContext,
                                getResources().getString(R.string.find_no_sdcard),
                                Toast.LENGTH_LONG).show();
                    }

                    break;
                case RESULT_REQUEST_CODE:
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    Uri resultUri = result.getUri();
                    LogUtils.i("sss", "自定义裁剪工具  result=" + result + ",resultUri=" + resultUri);
                    if (resultUri == null) {
                        return;
                    }
                    BitmapFactory.Options options2 = new BitmapFactory.Options();
                    options2.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bm = BitmapFactory.decodeFile(resultUri.getPath(), options2);
                    if (bm == null) {
                        return;
                    }
                    LogUtils.i("sss", "自定义裁剪工具  bm.getWidth()=" + bm.getWidth() + "*" + bm.getHeight());
                    int corpWidth = SPUtil.getInstance(mContext).getResolutionWidth();
                    int corpHeigth = SPUtil.getInstance(mContext).getResolutionHeight();
                    if (bm.getWidth() == corpWidth && bm.getHeight() == corpHeigth) {//出现过截取得到的是239*239，导致闪退
                        changeBgToByte(bm);
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void startPhotoZoom(Uri uri) {
        int corpWidth = SPUtil.getInstance(mContext).getResolutionWidth();
        int corpHeigth = SPUtil.getInstance(mContext).getResolutionHeight();
        CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setRequestedSize(corpWidth, corpHeigth, CropImageView.RequestSizeOptions.RESIZE_EXACT)//解决有些图片裁剪得出239*239而导致更换不了表盘背景的问题
                .setMinCropResultSize(corpWidth, corpHeigth)
                .setAspectRatio(corpWidth, corpHeigth)//根据手表屏幕长方形或正方形进行背景裁剪，避免背景图被拉伸
                .start(this);

    }

    private class aysncTaskChange extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Void... btns) {

            long rechargeSpan = System.currentTimeMillis();
            Bitmap bitmap = PicUtils.getInstance().changeDialBackgroundAndColor(mContext, mDialBackgroundBitmap, isFontColorChange, currentFontColor);
            long elapsedTime = System.currentTimeMillis() - rechargeSpan;
            Rgb.LogD(" 耗时" + elapsedTime + " 豪秒");
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            previewDial(bitmap);
        }
    }

    private class aysncTaskSync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... btns) {

            long rechargeSpan = System.currentTimeMillis();
            boolean isStart = PicUtils.getInstance().syncCustomDialData(mContext, mDialPreViewBitmap);
            long elapsedTime = System.currentTimeMillis() - rechargeSpan;
            Rgb.LogD(" 耗时" + elapsedTime + " 豪秒");
            return isStart;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Rgb.LogD("是否已经开始同步数据 = " + result);
        }
    }

    /**
     * 更换背景
     *
     * @param bitmap
     */
    private void changeBgToByte(Bitmap bitmap) {
        if (dialType.equals(PicUtils.DIAL_TYPE_CIRCLE)) {
            mDialBackgroundBitmap = Rgb.getInstance().roundedCornerBitmap(bitmap);
        } else {
            mDialBackgroundBitmap = bitmap;
        }
        aysncTaskChange task = new aysncTaskChange();
        task.execute();
    }

    /**
     * 更换字体颜色
     *
     * @param newColor
     */
    private void changeColorToByte(int newColor) {
        currentFontColor = newColor;
        isFontColorChange = true;
        aysncTaskChange task = new aysncTaskChange();
        task.execute();
    }

    private void syncDialData() {
        if (!SPUtil.getInstance(mContext).getBleConnectStatus()) {
            return;
        }
        aysncTaskSync task = new aysncTaskSync();
        task.execute();
    }

    private void previewDial(Bitmap bitmap) {
        mDialPreViewBitmap = bitmap;
        mergeBitmap.setImageBitmap(Rgb.getInstance().zoomBitmap(bitmap, 2f, 2f));//放大两倍再显示到APP，不然有点小
    }

    /**
     * 通知设备端，准备发送表盘数据
     * 收到设备端应答后开始同步表盘数据syncDialData()
     * 设备端回调应答：mHandler的case:OnlineDialUtil.PREPARE_SEND_ONLINE_DIAL_DATA
     */
    private void prepareSyncWatch() {
        OnlineDialUtil.getInstance().setDialStatus(OnlineDialUtil.DialStatus.CustomDial);
        syncWathch.setClickable(false);
        syncWathch.setText(mContext.getString(R.string.syncing));
        OnlineDialUtil.LogI("Custom下载完成，开始同步");
//        if (GlobalVariable.SYNC_CLICK_ENABLE) {
        WriteCommandToBLE.getInstance(mContext).prepareSendOnlineDialData();
//        } else {
//            SyncParameterUtils.getInstance(mContext).addCommandIndex(SyncParameterUtils.SYNC_DILA_PREPARE_SEND);
//        }
        WriteCommandToBLE.getInstance(mContext).setWatchSyncProgressListener(new WatchSyncProgressListener() {
            @Override
            public void WatchSyncProgress(int progress) {
                if (progressBar.getProgress() != progress) {
                    if (progress == 0) {
                        syncWathch.setTextColor(mContext.getResources().getColor(R.color.fontColor));
                    } else if (progress == 100) {
                        syncWathch.setText(mContext.getString(R.string.sync_finish));
                        syncWathch.setTextColor(mContext.getResources().getColor(R.color.white));
                        Flowable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(s -> {
                            syncWathch.setText(mContext.getString(R.string.syncWathch));
                            syncWathch.setClickable(true);
                        });
                    }
                    progressBar.setProgress(progress);
                }
            }
        });
    }

    private void displayDefaultDialPreView() {
        //初始化以下参数
        fontColorPosition = 0;
        pickerPosition = 0;
        mFontColorGirdAdapter.setSeclection(fontColorPosition);
        currentFontColor = getResources().getColor(R.color.dialFontColor0);
        isFontColorChange = false;
        mDialBackgroundBitmap = null;
        PicUtils.getInstance().resetDialBackgroundAndColor();//必须，复位背景和字体颜色.
        Bitmap mDialPreViewBitmap = PicUtils.getInstance().getDialDefaultPreview(mContext);
        mergeBitmap.setImageBitmap(Rgb.getInstance().zoomBitmap(mDialPreViewBitmap, 2f, 2f));
    }

    /**
     * 避免快速连续点击弹出多个
     */
    private boolean isDialogShowing = false;
    private DialColorPickerDialog mDialColorPickerDialog;

    private void showDialColorPickerDialog() {
        if (isDialogShowing) {
            return;
        }
        isDialogShowing = true;
        final DialColorPickerDialog.Builder builder = new DialColorPickerDialog.Builder(CustomDialActivity.this, colorPickerList);


        builder.setChooseButton(new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                Rgb.LogD("ChooseButton position =" + position);
                pickerPosition = position;
                changeColorToByte(colorPickerList[position]);//defaultData在这个方法里面重新赋值
            }
        });
        builder.setCancelButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isDialogShowing = false;
            }
        });

        mDialColorPickerDialog = builder.create();
        mDialColorPickerDialog.show();
        builder.setSeclection(pickerPosition);
        changeColorToByte(colorPickerList[pickerPosition]);//defaultData在这个方法里面重新赋值
        mDialColorPickerDialog.setOnKeyListener(new backlistener());
//        WindowManager.LayoutParams lp = mDialColorPickerDialog.getWindow().getAttributes();
//        lp.dimAmount = 0.0f;
//        mDialColorPickerDialog.getWindow().setAttributes(lp);
    }

    private class backlistener implements DialogInterface.OnKeyListener {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (isDialogShowing) {
                    if (mDialColorPickerDialog != null) {
                        mDialColorPickerDialog.dismiss();
                    }
                    isDialogShowing = false;
                }
                return true;
            }
            return false;
        }
    }

    @Subscribe
    public void wach(WatchChanged e) {
        OnlineDialUtil.LogI("表盘对话框收到断开连接1");
        if (e != null && e.changePos == WatchChanged.disconnected) {
            ShowAlphaDialog.showNoTitleNormalDialog(CustomDialActivity.this, DialogType.DIALOG_DISCONNECT, mContext.getResources().getString(R.string.bluetoothDisconnect));
            syncWathch.setText(mContext.getString(R.string.have_not_connect_ble));
        } else if (e != null && e.changePos == WatchChanged.connected) {
            ShowAlphaDialog.dismissNoTitleNormalDialog();
            syncWathch.setText(mContext.getString(R.string.syncWathch));
        }
    }

    @Override
    public void onBackPressed() {
        doFinish();
    }

    private void doFinish() {
        if (syncWathch.getText().toString().equals(mContext.getResources().getString(R.string.syncing))) {
            ToastUtils.showToast(mContext, syncWathch.getText().toString());
        } else {
            finish();
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            LogUtils.i("stringBuilder", "action=" + action);
            if (action.equals(GlobalVariable.ACTION_GATT_CONNECTED)) {
                mHandler.sendEmptyMessage(10);
            } else if (action.equals(GlobalVariable.ACTION_GATT_CONNECT_FAILURE)) {
                mHandler.sendEmptyMessage(11);
            }
        }
    };
}
