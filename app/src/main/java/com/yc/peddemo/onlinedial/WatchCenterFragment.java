package com.yc.peddemo.onlinedial;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import com.yc.pedometer.utils.LogUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.makeramen.roundedimageview.RoundedImageView;
import com.yc.peddemo.DialogType;
import com.yc.peddemo.R;
import com.yc.peddemo.customview.ShowAlphaDialog;
import com.yc.peddemo.onlinedial.util.BitmapUtil;
import com.yc.pedometer.dial.HttpRequestor;
import com.yc.pedometer.dial.OnlineDialUtil;
import com.yc.pedometer.dial.PicUtils;
import com.yc.pedometer.dial.PostUtil;
import com.yc.pedometer.dial.Rgb;
import com.yc.peddemo.onlinedial.util.ToastUtils;
import com.yc.pedometer.dial.UIFile;
import com.yc.pedometer.dial.WatchChanged;
import com.yc.pedometer.listener.OnlineDialListener;
import com.yc.pedometer.sdk.BLEServiceOperate;
import com.yc.pedometer.sdk.BluetoothLeService;
import com.yc.pedometer.sdk.WriteCommandToBLE;
import com.yc.pedometer.utils.BLEVersionUtils;
import com.yc.pedometer.utils.SPUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 流程是
 * 第一次握手
 * 调用WriteCommandToBLE.getInstance(context).readDeviceOnlineDialConfiguration()查询手表的参数
 * 在 mHandler收到case OnlineDialUtil.READ_DEVICE_ONLINE_DIAL_CONFIGURATION_OK:
 * 表示查询到参数，然后用这些参数去服务器获取在线表盘loadData(dialSec, btname, mac, dpi, maxCapacity, type, shape, cn);
 * 获取到表盘后，点击某一个下载下来然后解析准备发送到手表，
 *第二次握手
 * 准备发送到手表前调用WriteCommandToBLE.getInstance(context).prepareSendOnlineDialData()通知手表
 * 在 mHandler收到case OnlineDialUtil.PREPARE_SEND_ONLINE_DIAL_DATA:后，即可发送表盘数据
 *  WriteCommandToBLE.getInstance(mContext).sendOnlineDialData(data);
 */
public class WatchCenterFragment extends BaseFragment implements OnlineDialListener {
    private final String TAG = "BiaoPanCenterFragment";
    List<UIFile> watchList = new ArrayList<>();
    private UIFile mUIFile;
    private Context mContext;
    @BindView(R.id.xxListView)
    GridView xxListView;
    @BindView(R.id.no_dial_tip)
    RelativeLayout no_dial_tip;
    @BindView(R.id.refresh_view)
    PullToRefreshLayout refresh_view;
    Unbinder unbinder;
    private int dialCount = 0;//总个数
    private int dialSec = 0;//多少组
    private int dialSecCount = 18;//每次18个
    private WatchTransferDialog mWatchTransferDialog;
    private static WatchCenterFragment instance = null;

    public static WatchCenterFragment getInstance() {
        if (instance == null) {
            instance = new WatchCenterFragment();
        }
        return instance;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                    case OnlineDialUtil.READ_DEVICE_ONLINE_DIAL_CONFIGURATION_OK:
                    OnlineDialUtil.LogI("获取手环的表盘配置ok");
                    if (dialSec != 0 && dialSec * dialSecCount >= dialCount) {
                        ToastUtils.showToast(mContext, mContext.getResources().getString(R.string.no_more));
                    } else {
                        String btname = BLEVersionUtils.getInstance(mContext).getBleVersionName();
                        String mac = BLEVersionUtils.getInstance(mContext).getBleMac();
                        String dpi = SPUtil.getInstance(mContext).getResolutionWidthHeight();
                        String maxCapacity = SPUtil.getInstance(mContext).getDialMaxDataSize();
                        String cn = judgeLanguage();
                        int type = 0;//屏幕类型，区分指针和数字，0是全部。并不是方圆屏
                        int shape = SPUtil.getInstance(mContext).getDialScreenType();
                        int compatibleLevel = SPUtil.getInstance(mContext).getDialScreenCompatibleLevel();
//                    btname = "RB192C";
//                    dpi = PicUtils.DIAL_DPI_240x240;
                        if (NetworkUtils.getInstance(mContext).isNetworkAvailable()) {
                            //btname是固件版本，如果btname为空的话，请先获取固件版本，不然获取不到在线表盘
                            loadData(dialSec, btname, mac, dpi, maxCapacity, type, shape, cn, compatibleLevel);
                            dialSec++;
                        } else {
                            ShowAlphaDialog.show(DialogType.DIALOG_NETWORK_DISABLE, mContext);
                        }
                    }
                    break;
                case OnlineDialUtil.PREPARE_SEND_ONLINE_DIAL_DATA:
                    OnlineDialUtil.LogI("准备发送表盘数据");
                    if (mUIFile != null) {
                        OnlineDialDataTask task = new OnlineDialDataTask();
                        task.execute();
                    }
                    break;
                case OnlineDialUtil.SEND_ONLINE_DIAL_SUCCESS:
                    OnlineDialUtil.LogI("发送完成，成功");
                    ToastUtils.showToast(mContext, mContext.getResources().getString(R.string.send_online_dial_success));
                    break;
                case OnlineDialUtil.SEND_ONLINE_DIAL_CRC_FAIL:
                    OnlineDialUtil.LogI("发送完成，校验失败");
                    ToastUtils.showToast(mContext, mContext.getResources().getString(R.string.send_online_dial_crc_fail));
                    if (mWatchTransferDialog != null) {
                        mWatchTransferDialog.dismissDialog();
                    }
                    break;
                case OnlineDialUtil.SEND_ONLINE_DIAL_DATA_TOO_LARGE:
                    OnlineDialUtil.LogI("发送完成，表盘数据太大");
                    ToastUtils.showToast(mContext, mContext.getResources().getString(R.string.send_online_dial_data_too_large));
                    if (mWatchTransferDialog != null) {
                        mWatchTransferDialog.dismissDialog();
                    }
                    break;
                default:
                    break;
            }

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_wacth_center, null);
        mContext = getActivity();
        BluetoothLeService mBluetoothLeService = BLEServiceOperate.getInstance(mContext).getBleService();
        if (mBluetoothLeService != null) {
            OnlineDialUtil.getInstance().setDialStatus(OnlineDialUtil.DialStatus.RegularDial);
            mBluetoothLeService.setOnlineDialListener(this);
        }
//        if (GlobalVariable.SYNC_CLICK_ENABLE) {
        WriteCommandToBLE.getInstance(mContext).readDeviceOnlineDialConfiguration();
//        } else {
//            SyncParameterUtils.getInstance(mContext).addCommandIndex(SyncParameterUtils.SYNC_DILA_DEVICE_CONFIG);
//        }

        unbinder = ButterKnife.bind(this, layout);
        musicListAdapter = new MusicListAdapter(getContext(), watchList);
        xxListView.setAdapter(musicListAdapter);
        refresh_view.setOnRefreshListener(new MyListener());
        isNoHasDial();
//        mHandler.sendEmptyMessageDelayed(OnlineDialUtil.READ_DEVICE_ONLINE_DIAL_CONFIGURATION_OK, 1000);//测试
        if (!NetworkUtils.getInstance(mContext).isNetworkAvailable()) {
            ShowAlphaDialog.show(DialogType.DIALOG_NETWORK_DISABLE, mContext);
        }
        return layout;
    }

    MusicListAdapter musicListAdapter;

   private void loadData(int n ,String btname, String mac, String dpi, String maxCapacity, int type,int shape, String cn,int compatible) {
        HttpRequestor httpRequestor = new HttpRequestor();

       HashMap hashMap = PostUtil.getInstance(mContext).getWatchHashMap(n, dialSecCount, btname, mac, dpi, maxCapacity, type, shape, cn, compatible);

        try {
            Flowable.just(0).subscribeOn(Schedulers.io()).map(integer -> {
                String json = httpRequestor.doPost(PostUtil.GET_WATCH, hashMap);
                JSONObject jsonObject = new JSONObject(json);
                int flag = jsonObject.getInt("flag");
                OnlineDialUtil.LogI("flag: " + flag);
                if (flag < 0) {
                    return null;
                }
                dialCount= jsonObject.getInt("count");
                JSONArray jsonArray = jsonObject.getJSONArray("list");
                List<UIFile> files = new Gson().fromJson(jsonArray.toString(), new TypeToken<List<UIFile>>() {
                }.getType());
                Collections.sort(files);//list重新排序
                return files;
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(v -> {
                if (v != null) {
                    LogUtils.d("flag", "dialCount: " + dialCount+",dialSecCount: " + dialSecCount+",dialSec: " + dialSec+",v.size(): " + v.size());
                    if (dialCount <= dialSecCount) {
                        watchList = v;
                    } else {
                        watchList.addAll(v);
                    }
                    LogUtils.d("flag", "dialCount: " + dialCount+",dialSecCount: " + dialSecCount+",watchList.size(): " + watchList.size());
                    if (musicListAdapter != null) {
                        musicListAdapter.notifyDataSetChanged();
                        isNoHasDial();
                    }
                }
                System.out.println("-------");
            }, v -> {
//                ToastUtils.showToast(getContext(), getString(R.string.confire_is_network_available));
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dialCount = 0;
        dialSec = 0;
        watchList.clear();
        unbinder.unbind();
    }

    @Override
    public void onlineDialStatus(int status) {
        if (OnlineDialUtil.getInstance().getDialStatus() == OnlineDialUtil.DialStatus.RegularDial) {
            OnlineDialUtil.LogI("onlineDialStatus  status =" + status);
            mHandler.sendEmptyMessage(status);
        }
    }

    public class MusicListAdapter extends BaseAdapter {
        private Context mContext;

        public MusicListAdapter(Context context, List<UIFile> mgroupList) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return watchList.size();
        }

        @Override
        public Object getItem(int position) {
            return watchList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;

            UIFile uiFile = watchList.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.xxitemwatch, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
//            if(!TextUtils.isEmpty(uiFile.getResouce())){
//            }   else if(uiFile.getId()!=0){
//                viewHolder.ivBiaoPan.setBackgroundResource(uiFile.getId());
//            }
            if (((PullableGridView) parent).isOnMeasure) {
                //如果是onMeasure调用的就立即返回.解决多次执行
                return convertView;
            }
            if (NetworkUtils.getInstance(getContext()).isNetworkAvailable()) {
                BitmapUtil.loadBitmap(getContext(), uiFile.getPreview(), viewHolder.ivBiaoPan);
            }
            if (uiFile.getShape() == (Integer.valueOf(PicUtils.DIAL_TYPE_CIRCLE))) {
                viewHolder.ivBiaoPan.setOval(true);
            } else {
                viewHolder.ivBiaoPan.setOval(false);
            }
            viewHolder.ivBiaoPan.setOnClickListener(v -> {
                if (!SPUtil.getInstance(mContext).getBleConnectStatus()) {
                    ShowAlphaDialog.show(DialogType.DIALOG_DISCONNECT, mContext);
                    return;
                }
                if (!NetworkUtils.getInstance(mContext).isNetworkAvailable()) {
                    ShowAlphaDialog.show(DialogType.DIALOG_NETWORK_DISABLE, mContext);
                    return;
                }
                WatchTransferDialog picUpgradeSetDialog = new WatchTransferDialog(getActivity(), WatchTransferDialog.Status.NotStart
                        , uiFile, () -> {
                    List<UIFile> uiFileList = stringToUIFileList(SPUtil.getInstance(mContext).getLocalWatchList());
                    if (uiFileList == null || uiFileList.size() == 0) {
                        uiFileList = new ArrayList<>();
                        uiFileList.add(uiFile);
                        SPUtil.getInstance(mContext).setLocalWatchList(new Gson().toJson(uiFileList));
                    } else {
                        int index = -1;
                        for (UIFile uiFile1 : uiFileList) {
                            if (uiFile1.getId() == uiFile.getId()) {
                                index = 0;
                                break;
                            }
                        }
                        if (index == -1) {
                            uiFileList.add(uiFile);
                            SPUtil.getInstance(mContext).setLocalWatchList(new Gson().toJson(uiFileList));
                        }
                    }
                    if (NetworkUtils.getInstance(getContext()).isNetworkAvailable()) {
                        HttpRequestor httpRequestor = new HttpRequestor();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String json = new Gson().toJson(PostUtil.getInstance(mContext).addWatchHashMap(BLEVersionUtils.getInstance(mContext).getBleVersionName(), uiFile.getId()));
                                Map<String, String> map = new HashMap<>();
                                map.put("content", json);
                                try {
                                    httpRequestor.doPost(PostUtil.ADD_WATCH, map);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                        EventBus.getDefault().post(new WatchChanged(0));
                    }

                });
                mUIFile = uiFile;
                OnlineDialUtil.LogI("mUIFile 赋值 " + mUIFile + "，mUIFile.getTitle() =" + mUIFile.getTitle());
                picUpgradeSetDialog.show();
            });

            return convertView;
        }


        class ViewHolder {
            @BindView(R.id.ivBiaoPan)
            RoundedImageView ivBiaoPan;

            ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }

    private List<UIFile> stringToUIFileList(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        } else {
            List<UIFile> uiFileList = new Gson().fromJson(json, new TypeToken<List<UIFile>>() {
            }.getType());
            return uiFileList;
        }
    }
    public class OnlineDialDataTask extends AsyncTask<Void, Void, Boolean> {
        String stringData = null;
        byte[] data = null;
        long rechargeSpan = 0;

        @Override
        protected Boolean doInBackground(Void... params) {
            rechargeSpan = System.currentTimeMillis();
//            OnlineDialUtil.LogI("mUIFile doInBackground " + mUIFile);
//            OnlineDialUtil.LogI("mUIFile.getTitle() =" + mUIFile.getTitle());
            String path = mContext.getExternalFilesDir(null) + "/" + mUIFile.getTitle() + ".bin";
//            stringData = OnlineDialUtil.getInstance(mContext).getBinStringData(mContext, path);//20200306 delete
            //20200306 add
            data = Rgb.getInstance().readBinToByte(mContext, path);
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            long elapsedTime = System.currentTimeMillis() - rechargeSpan;
            OnlineDialUtil.LogI(" 耗时" + elapsedTime + " 豪秒");
            //20200306 add
            if (data != null && data.length > 0) {
                if (!SPUtil.getInstance(mContext).getBleConnectStatus()) {
                    return;
                }
                WriteCommandToBLE.getInstance(mContext).sendOnlineDialData(data);
            }
            //20200306 delete
//            if (!TextUtils.isEmpty(stringData)) {
//                if (!SPUtil.getInstance().getBleConnectStatus()) {
//                    return;
//                }
//                WriteCommandToBLE.getInstance(mContext).sendOnlineDialData(stringData);
//            }
        }
    }

    public String judgeLanguage() {
        String locale = Locale.getDefault().toString();
        LogUtils.d("zh_CN", "locale = judgeLanguage =" + locale);
        String language = "cn";
        if (locale.contains("zh_CN") || locale.contains("zh_TW") || locale.contains("zh_MO") || locale.contains("zh_HK")) {
            language = "cn";
        } else {
            language = "en";
        }

        return language;
    }

    public void setUIFile(UIFile file) {
        mUIFile = file;
    }

    private void isNoHasDial() {
        if (watchList == null || watchList.size() == 0) {
            if (no_dial_tip == null) {
                return;
            }
            no_dial_tip.setVisibility(View.VISIBLE);
        } else {
            if (no_dial_tip == null) {
                return;
            }
            no_dial_tip.setVisibility(View.GONE);
        }
    }
    public class MyListener implements PullToRefreshLayout.OnRefreshListener {

        @Override
        public void onRefresh(final PullToRefreshLayout pullToRefreshLayout) {
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
        }

        @Override
        public void onLoadMore(final PullToRefreshLayout pullToRefreshLayout) {
            pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
            mHandler.sendEmptyMessage(OnlineDialUtil.READ_DEVICE_ONLINE_DIAL_CONFIGURATION_OK);

        }
    }
}
