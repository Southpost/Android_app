package com.yc.peddemo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.yc.pedometer.info.BraceletInterfaceInfo;
import com.yc.pedometer.listener.BraceletInterfaceListener;
import com.yc.pedometer.sdk.BLEServiceOperate;
import com.yc.pedometer.sdk.BluetoothLeService;
import com.yc.pedometer.sdk.WriteCommandToBLE;
import com.yc.pedometer.utils.LogUtils;
import com.yc.pedometer.utils.SPUtil;
import com.yc.pedometer.utils.TrainUtil;

import java.util.ArrayList;
import java.util.List;


public class BandInterfaceSetActivity extends AppCompatActivity implements View.OnClickListener, BraceletInterfaceListener, BraceletInterfaceListAdapter.OnItemCheckBoxListener {
    private final String TAG = "BandInterfaceSet";
    private Context mContext;
    private ListView MyListView;
    private List<BraceletInterfaceInfo> mWeartherInfoList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_band_interface_set);
        mContext = getApplicationContext();
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(this);
        MyListView = findViewById(R.id.MyListView);
        BLEServiceOperate mBLEPedometerService = BLEServiceOperate.getInstance(mContext);
        BluetoothLeService mBluetoothLeService = mBLEPedometerService.getBleService();
        if (mBluetoothLeService != null) {
            mBluetoothLeService.setBraceletInterfaceListener(this);
        } else {
            LogUtils.d(TAG, "BandInterfaceSetActivity mBluetoothLeService==null");
        }

        WriteCommandToBLE.getInstance(mContext).queryBraceletInterface();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;

            default:
                break;
        }
    }

    private final int UPDATE_UI_MSG = 1;
    private final int SHOW_DIALOG_MSG = 2;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_UI_MSG:
//                    mWeartherInfoList =
                    LogUtils.d(TAG, "更新UI");
                    BraceletInterfaceListAdapter adapter = new BraceletInterfaceListAdapter(mContext, mWeartherInfoList);
                    MyListView.setAdapter(adapter);
                    adapter.setOnItemCheckBoxClickListener(BandInterfaceSetActivity.this);
                    break;
                case SHOW_DIALOG_MSG:

                    LogUtils.d(TAG, "设置OK");
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onBraceletInterface(StringBuilder stringBuilder, byte[] data) {
        int keyValue = data[1] & 0xFF;

        switch (keyValue) {
            case 0xaa:
//                LogUtils.d(TAG,"查询 stringBuilder =" + stringBuilder);
//                data[2] =(byte)0x6d;
//                data[3] =(byte)0x6d;
//                data[4] =(byte)0xde;
//                data[5] =(byte)0xde;
//                data[6] =(byte)0x56;
//                data[7] =(byte)0x56;
//                data[8] =(byte)0x34;
//                data[9] =(byte)0x34;
//                data[10] =(byte)0x56;
//                data[11] =(byte)0x56;
//                data[12] =(byte)0x3;
//                data[13] =(byte)0x3;

                int addr0 = data[2] & 0xFF;
                int addr1 = (data[4] << 8) & 0xFF00;
                int addr2 = (data[6] << 16) & 0xFF0000;
                int addr3 = (data[8] << 24) & 0xFF000000;

                int addr00 = data[3] & 0xFF;
                int addr10 = (data[5] << 8) & 0xFF00;
                int addr20 = (data[7] << 16) & 0xFF0000;
                int addr30 = (data[9] << 24) & 0xFF000000;

                int add0 = data[10] & 0xFF;
                int add1 = (data[12] << 8) & 0xFF00;

                int add00 = data[11] & 0xFF;
                int add10 = (data[13] << 8) & 0xFF00;

                int function1 = (addr0 | addr1 | addr2 | addr3);
                int status1 = (addr00 | addr10 | addr20 | addr30);

                int function2 = (add0 | add1);
                int status2 = (add00 | add10);
                String function1HTB = Integer.toBinaryString(function1);
                String status1HTB = Integer.toBinaryString(status1);

                String function2HTB = Integer.toBinaryString(function2);
                String status2HTB = Integer.toBinaryString(status2);
                LogUtils.d(TAG, "function1HTB =" + function1HTB + ",status1HTB =" + status1HTB);
                LogUtils.d(TAG, "function2HTB =" + function2HTB + ",status2HTB =" + status2HTB);

                byte[] cycleByteFunction1 = TrainUtil.byte2String2(function1HTB);
                byte[] cycleByteStatus1 = TrainUtil.byte2String2(status1HTB);

                byte[] cycleByteFunction2 = TrainUtil.byte2String2(function2HTB);
                byte[] cycleByteStatus2 = TrainUtil.byte2String2(status2HTB);
                for (int i = 0; i < cycleByteFunction1.length; i++) {
                    LogUtils.d(TAG, "cycleByteFunction1[" + i + "] =" + (cycleByteFunction1[i]));
                    if (cycleByteFunction1[i] == 1) {//support
                        boolean isDisplay = false;
                        if (i < cycleByteStatus1.length) {
                            if (cycleByteStatus1[i] == 1) {
                                isDisplay = true;
                            }
                        }
                        BraceletInterfaceInfo info = new BraceletInterfaceInfo(i, isDisplay);
                        mWeartherInfoList.add(info);
                    }
                }
                for (int i = 0; i < cycleByteFunction2.length; i++) {
                    LogUtils.d(TAG, "cycleByteFunction2[" + i + "] =" + (cycleByteFunction2[i]));
                    if (cycleByteFunction2[i] == 1) {//support
                        boolean isDisplay = false;
                        if (i < cycleByteStatus2.length) {
                            if (cycleByteStatus2[i] == 1) {
                                isDisplay = true;
                            }
                        }
                        BraceletInterfaceInfo info = new BraceletInterfaceInfo(i + 32, isDisplay);
                        mWeartherInfoList.add(info);
                    }
                }
                mHandler.sendEmptyMessage(UPDATE_UI_MSG);
                break;
            case 1:
                LogUtils.d(TAG, "设置显示OK");
                break;
            case 2:
                LogUtils.d(TAG, "设置隐藏OK");
                break;

            default:
                break;
        }

    }

    @Override
    public void onCheckBoxClick(int itemPosition, int whichInterface, boolean isChecked) {
        LogUtils.d(TAG, " itemPosition =" + itemPosition + ",whichInterface =" + whichInterface + ",isChecked =" + isChecked);

        boolean ble_connected21 = SPUtil.getInstance(mContext).getBleConnectStatus();
        if (ble_connected21) {
            WriteCommandToBLE.getInstance(mContext).displayOrHideBraceletInterface(isChecked, whichInterface);
            mHandler.sendEmptyMessage(SHOW_DIALOG_MSG);
        } else {
            LogUtils.d(TAG, "未连接");
        }


    }

}
