package com.yc.peddemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.yc.peddemo.customview.CustomPasswordDialog;
import com.yc.peddemo.customview.CustomProgressDialog;
import com.yc.peddemo.event.ConnectEvent;
import com.yc.peddemo.fragment.DeviceFragment;
import com.yc.peddemo.fragment.HomeFragment;
import com.yc.peddemo.fragment.MineFragment;
import com.yc.peddemo.onlinedial.BaseFragment;
import com.yc.peddemo.onlinedial.OnlineDialActivity;
import com.yc.pedometer.info.BPVOneDayInfo;
import com.yc.pedometer.info.BreatheInfo;
import com.yc.pedometer.info.CustomTestStatusInfo;
import com.yc.pedometer.info.HeartRateHeadsetSportModeInfo;
import com.yc.pedometer.info.OxygenInfo;
import com.yc.pedometer.info.RateOneDayInfo;
import com.yc.pedometer.info.SevenDayWeatherInfo;
import com.yc.pedometer.info.SkipDayInfo;
import com.yc.pedometer.info.SleepTimeInfo;
import com.yc.pedometer.info.SportsModesControlInfo;
import com.yc.pedometer.info.SportsModesInfo;
import com.yc.pedometer.info.StepOneDayAllInfo;
import com.yc.pedometer.info.SwimDayInfo;
import com.yc.pedometer.info.TemperatureInfo;
import com.yc.pedometer.listener.BreatheRealListener;
import com.yc.pedometer.listener.OxygenRealListener;
import com.yc.pedometer.listener.RateCalibrationListener;
import com.yc.pedometer.listener.TemperatureListener;
import com.yc.pedometer.listener.TurnWristCalibrationListener;
import com.yc.pedometer.sdk.BLEServiceOperate;
import com.yc.pedometer.sdk.BloodPressureChangeListener;
import com.yc.pedometer.sdk.BluetoothLeService;
import com.yc.pedometer.sdk.DataProcessing;
import com.yc.pedometer.sdk.ICallback;
import com.yc.pedometer.sdk.ICallbackStatus;
import com.yc.pedometer.sdk.OnServerCallbackListener;
import com.yc.pedometer.sdk.RateChangeListener;
import com.yc.pedometer.sdk.RateOf24HourRealTimeListener;
import com.yc.pedometer.sdk.ServiceStatusCallback;
import com.yc.pedometer.sdk.SleepChangeListener;
import com.yc.pedometer.sdk.StepChangeListener;
import com.yc.pedometer.sdk.UTESQLOperate;
import com.yc.pedometer.sdk.WriteCommandToBLE;
import com.yc.pedometer.update.Updates;
import com.yc.pedometer.utils.BandLanguageUtil;
import com.yc.pedometer.utils.BreatheUtil;
import com.yc.pedometer.utils.CalendarUtils;
import com.yc.pedometer.utils.GBUtils;
import com.yc.pedometer.utils.GetFunctionList;
import com.yc.pedometer.utils.GlobalVariable;
import com.yc.pedometer.utils.HeartRateHeadsetUtils;
import com.yc.pedometer.utils.LogUtils;
import com.yc.pedometer.utils.MultipleSportsModesUtils;
import com.yc.pedometer.utils.OxygenUtil;
import com.yc.pedometer.utils.SPUtil;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity implements OnClickListener,
        ICallback, ServiceStatusCallback, OnServerCallbackListener, RateCalibrationListener, TurnWristCalibrationListener, TemperatureListener, OxygenRealListener, BreatheRealListener {
    private TextView connect_status, rssi_tv, tv_steps, tv_distance,
            tv_calorie, tv_sleep, tv_deep, tv_light, tv_awake, show_result,
            tv_rate, tv_lowest_rate, tv_verage_rate, tv_highest_rate;
    private EditText et_height, et_weight, et_sedentary_period, et_universal_interface;
    private Button btn_confirm, btn_sync_step, btn_sync_sleep, update_ble,
            read_ble_version, read_ble_battery, set_ble_time,
            bt_sedentary_open, bt_sedentary_close, btn_sync_rate,
            btn_rate_start, btn_rate_stop, unit, push_message_content, open_camera, close_camera;

    private Button today_sports_time, seven_days_sports_time, universal_interface, universal_interface_ble_sdk, settings_bracelet_interface_set, query_bracelet_model, query_customer_ID;
    private Button query_currently_sport, set_currently_sport, sync_currently_sport, control_sport, query_data, dial_setting, body_temperature;
    private DataProcessing mDataProcessing;
    private CustomProgressDialog mProgressDialog;
    public UTESQLOperate mySQLOperate;
    public WriteCommandToBLE mWriteCommand;
    private Context mContext;

    private final int UPDATE_STEP_UI_MSG = 0;
    private final int UPDATE_SLEEP_UI_MSG = 1;
    private final int DISCONNECT_MSG = 18;
    private final int CONNECTED_MSG = 19;
    private final int UPDATA_REAL_RATE_MSG = 20;
    private final int RATE_SYNC_FINISH_MSG = 21;
    private final int OPEN_CHANNEL_OK_MSG = 22;
    private final int CLOSE_CHANNEL_OK_MSG = 23;
    private final int TEST_CHANNEL_OK_MSG = 24;
    private final int OFFLINE_SWIM_SYNC_OK_MSG = 25;
    private final int UPDATA_REAL_BLOOD_PRESSURE_MSG = 29;
    private final int OFFLINE_BLOOD_PRESSURE_SYNC_OK_MSG = 30;
    private final int SERVER_CALL_BACK_OK_MSG = 31;
    private final int OFFLINE_SKIP_SYNC_OK_MSG = 32;
    private final int test_mag1 = 35;
    private final int test_mag2 = 36;
    private final int OFFLINE_STEP_SYNC_OK_MSG = 37;
    private final int UPDATE_SPORTS_TIME_DETAILS_MSG = 38;

    private final int UNIVERSAL_INTERFACE_SDK_TO_BLE_SUCCESS_MSG = 39;//sdk???????????????ble??????????????????????????????????????????
    private final int UNIVERSAL_INTERFACE_SDK_TO_BLE_FAIL_MSG = 40;   //sdk???????????????ble??????????????????????????????????????????
    private final int UNIVERSAL_INTERFACE_BLE_TO_SDK_SUCCESS_MSG = 41;//ble???????????????sdk??????????????????????????????????????????
    private final int UNIVERSAL_INTERFACE_BLE_TO_SDK_FAIL_MSG = 42;   //ble???????????????sdk??????????????????????????????????????????

    private final int RATE_OF_24_HOUR_SYNC_FINISH_MSG = 43;
    private final int BIND_CONNECT_SEND_ACCOUNT_ID_MSG = 44;


    private final long TIME_OUT_SERVER = 10000;
    private final long TIME_OUT = 120000;
    private boolean isUpdateSuccess = false;
    private int mSteps = 0;
    private float mDistance = 0f;
    private float mCalories = 0, mRunCalories = 0, mWalkCalories = 0;
    private int mRunSteps, mRunDurationTime, mWalkSteps, mWalkDurationTime;
    private float mRunDistance, mWalkDistance;
    protected static final String TAG = "MainActivity1";
    private Updates mUpdates;
    private BLEServiceOperate mBLEServiceOperate;
    private BluetoothLeService mBluetoothLeService;
    // caicai add for sdk
    public static final String EXTRAS_DEVICE_NAME = "device_name";
    public static final String EXTRAS_DEVICE_ADDRESS = "device_address";
    private final int CONNECTED = 1;
    private final int CONNECTING = 2;
    private final int DISCONNECTED = 3;
    private int CURRENT_STATUS = DISCONNECTED;

    private String mDeviceName;
    private String mDeviceAddress;

    private int tempRate = 70;
    private int tempStatus;

    private Button test_channel;
    private StringBuilder resultBuilder = new StringBuilder();

    private TextView swim_time, swim_stroke_count, swim_calorie,
            tv_low_pressure, tv_high_pressure, skip_time, skip_count, skip_calorie;
    private Button btn_sync_pressure, btn_start_pressure,
            btn_stop_pressure, rate_calibration, turn_wrist_calibration, set_band_language;

    private int high_pressure, low_pressure;
    private int tempBloodPressureStatus;
    private Button ibeacon_command;
    private Spinner setOrReadSpinner, ibeaconStatusSpinner;
    private List<String> ibeaconStatusSpinnerList = new ArrayList<String>();
    private List<String> SetOrReadSpinnerList = new ArrayList<String>();
    private ArrayAdapter<String> aibeaconStatusAdapter;
    private ArrayAdapter<String> setOrReadAdapter;
    private int ibeaconStatus = GlobalVariable.IBEACON_TYPE_UUID;
    private int ibeaconSetOrRead = GlobalVariable.IBEACON_SET;
    private int leftRightHand = GlobalVariable.LEFT_HAND_WEAR;
    private int dialType = GlobalVariable.SHOW_HORIZONTAL_SCREEN;

    public static final String CONNECTED_DEVICE_CHANNEL = "connected_device_channel";
    public static final String FILE_SAVED_CHANNEL = "file_saved_channel";
    public static final String PROXIMITY_WARNINGS_CHANNEL = "proximity_warnings_channel";
    private Button hrh_stop_sport, hrh_start_sport, hrh_set_interval, hrh_query_status;

    private Button start_test_oxygen, stop_test_oxygen, sync_oxygen_data, query_oxygen_data;
    private Button start_test_breathe, stop_test_breathe, sync_breathe_data, query_breathe_data,
            query_breathe_test_status, breathe_automatic_test, breathe_time_period;

    @BindView(R.id.vp_home)
    ViewPager vpHome;
    @BindView(R.id.tab_bottom)
    BottomNavigationView tabBottom;
    public static final int PAGE_HOME = 0;
    public static final int PAGE_DEVICE = 1;
    public static final int PAGE_MY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        List<BaseFragment> fragmentList = new ArrayList<>();
        fragmentList.add(new HomeFragment());
        fragmentList.add(new DeviceFragment());
        fragmentList.add(new MineFragment());
        vpHome.setAdapter(new HomePageAdapter(getSupportFragmentManager(), fragmentList));
        vpHome.setOffscreenPageLimit(4);

        vpHome.setCurrentItem(PAGE_HOME);
        initBottomTab();
        mContext = getApplicationContext();
        LogUtils.setLogEnable(true);//????????????log
        mySQLOperate = UTESQLOperate.getInstance(mContext);// 2.2.1????????????
        mBLEServiceOperate = BLEServiceOperate.getInstance(mContext);
        LogUtils.d(TAG, "setServiceStatusCallback??? mBLEServiceOperate =" + mBLEServiceOperate);
        mBLEServiceOperate.setServiceStatusCallback(this);
        LogUtils.d(TAG, "setServiceStatusCallback??? mBLEServiceOperate =" + mBLEServiceOperate);
        // ????????????????????????????????????BLEServiceOperate??????????????????4???????????????OnServiceStatuslt
        mBluetoothLeService = mBLEServiceOperate.getBleService();
        if (mBluetoothLeService != null) {
            mBluetoothLeService.setICallback(this);

            mBluetoothLeService.setRateCalibrationListener(this);//????????????????????????
            mBluetoothLeService.setTurnWristCalibrationListener(this);//????????????????????????
            mBluetoothLeService.setTemperatureListener(this);//???????????????????????????????????????
            mBluetoothLeService.setOxygenListener(this);//Oxygen Listener
            mBluetoothLeService.setBreatheRealListener(this);//Breathe Listener
        }
        mRegisterReceiver();
        mfindViewById();
        mWriteCommand = WriteCommandToBLE.getInstance(mContext);
        mUpdates = Updates.getInstance(mContext);
        mUpdates.setHandler(mHandler);// ????????????????????????
        mUpdates.registerBroadcastReceiver();
        mUpdates.setOnServerCallbackListener(this);
        LogUtils.d(TAG, "MainActivity_onCreate   mUpdates  ="
                + mUpdates);
        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        mBLEServiceOperate.connect(mDeviceAddress);

        CURRENT_STATUS = CONNECTING;
        upDateTodaySwimData();
        upDateTodaySkipData();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//NRF????????????
            DfuServiceInitiator.createDfuNotificationChannel(this);

            final NotificationChannel channel = new NotificationChannel(CONNECTED_DEVICE_CHANNEL, getString(R.string.channel_connected_devices_title), NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.channel_connected_devices_description));
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            final NotificationChannel fileChannel = new NotificationChannel(FILE_SAVED_CHANNEL, getString(R.string.channel_files_title), NotificationManager.IMPORTANCE_LOW);
            fileChannel.setDescription(getString(R.string.channel_files_description));
            fileChannel.setShowBadge(false);
            fileChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            final NotificationChannel proximityChannel = new NotificationChannel(PROXIMITY_WARNINGS_CHANNEL, getString(R.string.channel_proximity_warnings_title), NotificationManager.IMPORTANCE_LOW);
            proximityChannel.setDescription(getString(R.string.channel_proximity_warnings_description));
            proximityChannel.setShowBadge(false);
            proximityChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            notificationManager.createNotificationChannel(fileChannel);
            notificationManager.createNotificationChannel(proximityChannel);
        }
    }

    private void initBottomTab() {
        vpHome.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabBottom.getMenu().getItem(position).setChecked(true);
            }
        });

        tabBottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.tab_home:
                        vpHome.setCurrentItem(PAGE_HOME);
                        break;
                    case R.id.tab_device:
                        vpHome.setCurrentItem(PAGE_DEVICE);
                        break;
                    case R.id.tab_my:
                        vpHome.setCurrentItem(PAGE_MY);
                        break;
                }

                return true;
            }
        });
    }

    private void mRegisterReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(GlobalVariable.READ_BATTERY_ACTION);
        mFilter.addAction(GlobalVariable.READ_BLE_VERSION_ACTION);
        registerReceiver(mReceiver, mFilter);
    }

    private void mfindViewById() {
        et_height = (EditText) findViewById(R.id.et_height);
        et_weight = (EditText) findViewById(R.id.et_weight);
        et_sedentary_period = (EditText) findViewById(R.id.et_sedentary_period);
        connect_status = (TextView) findViewById(R.id.connect_status);
        rssi_tv = (TextView) findViewById(R.id.rssi_tv);
        tv_steps = (TextView) findViewById(R.id.tv_steps);
        tv_distance = (TextView) findViewById(R.id.tv_distance);
        tv_calorie = (TextView) findViewById(R.id.tv_calorie);
        tv_sleep = (TextView) findViewById(R.id.tv_sleep);
        tv_deep = (TextView) findViewById(R.id.tv_deep);
        tv_light = (TextView) findViewById(R.id.tv_light);
        tv_awake = (TextView) findViewById(R.id.tv_awake);
        tv_rate = (TextView) findViewById(R.id.tv_rate);
        tv_lowest_rate = (TextView) findViewById(R.id.tv_lowest_rate);
        tv_verage_rate = (TextView) findViewById(R.id.tv_verage_rate);
        tv_highest_rate = (TextView) findViewById(R.id.tv_highest_rate);
        show_result = (TextView) findViewById(R.id.show_result);
        btn_confirm = (Button) findViewById(R.id.btn_confirm);
        bt_sedentary_open = (Button) findViewById(R.id.bt_sedentary_open);
        bt_sedentary_close = (Button) findViewById(R.id.bt_sedentary_close);
        btn_sync_step = (Button) findViewById(R.id.btn_sync_step);
        btn_sync_sleep = (Button) findViewById(R.id.btn_sync_sleep);
        btn_sync_rate = (Button) findViewById(R.id.btn_sync_rate);
        btn_rate_start = (Button) findViewById(R.id.btn_rate_start);
        btn_rate_stop = (Button) findViewById(R.id.btn_rate_stop);
        btn_confirm.setOnClickListener(this);
        bt_sedentary_open.setOnClickListener(this);
        bt_sedentary_close.setOnClickListener(this);
        btn_sync_step.setOnClickListener(this);
        btn_sync_sleep.setOnClickListener(this);
        btn_sync_rate.setOnClickListener(this);
        btn_rate_start.setOnClickListener(this);
        btn_rate_stop.setOnClickListener(this);
        read_ble_version = (Button) findViewById(R.id.read_ble_version);
        read_ble_version.setOnClickListener(this);
        read_ble_battery = (Button) findViewById(R.id.read_ble_battery);
        read_ble_battery.setOnClickListener(this);
        set_ble_time = (Button) findViewById(R.id.set_ble_time);
        set_ble_time.setOnClickListener(this);
        update_ble = (Button) findViewById(R.id.update_ble);
        update_ble.setOnClickListener(this);
        et_height.setText(SPUtil.getInstance(mContext).getPersonageHeight());
        et_weight.setText(SPUtil.getInstance(mContext).getPersonageWeight());

        mDataProcessing = DataProcessing.getInstance(mContext);
        mDataProcessing.setOnStepChangeListener(mOnStepChangeListener);
        mDataProcessing.setOnSleepChangeListener(mOnSleepChangeListener);
        mDataProcessing.setOnRateListener(mOnRateListener);
        mDataProcessing.setOnRateOf24HourListenerRate(mOnRateOf24HourListener);
        mDataProcessing.setOnBloodPressureListener(mOnBloodPressureListener);

        Button open_alarm = (Button) findViewById(R.id.open_alarm);
        open_alarm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
//				mWriteCommand.sendToSetAlarmCommand(1, GlobalVariable.EVERYDAY,
//						16, 25, true, 5);// ??????
//				mWriteCommand.sendToSetAlarmCommand(1, (byte) (GlobalVariable.MONDAY|GlobalVariable.WEDNESDAY),
//						16, 25, true, 5);// ???????????????
                mWriteCommand.sendToSetAlarmCommand(1, (byte) (GlobalVariable.MONDAY | GlobalVariable.WEDNESDAY | GlobalVariable.FRIDAY),
                        16, 25, true, 5);// ????????????????????????
            }
        });
        Button close_alarm = (Button) findViewById(R.id.close_alarm);
        close_alarm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // 2.2.1????????????
                mWriteCommand.sendToSetAlarmCommand(1, GlobalVariable.EVERYDAY,
                        16, 23, false, 5);// ???????????????????????????????????????
            }
        });

        LogUtils.d(TAG, "main_mDataProcessing =" + mDataProcessing);

        unit = (Button) findViewById(R.id.unit);
        unit.setOnClickListener(this);
        test_channel = (Button) findViewById(R.id.test_channel);
        test_channel.setOnClickListener(this);
        push_message_content = (Button) findViewById(R.id.push_message_content);
        push_message_content.setOnClickListener(this);

        swim_time = (TextView) findViewById(R.id.swim_time);
        swim_stroke_count = (TextView) findViewById(R.id.swim_stroke_count);
        swim_calorie = (TextView) findViewById(R.id.swim_calorie);

        tv_low_pressure = (TextView) findViewById(R.id.tv_low_pressure);
        tv_high_pressure = (TextView) findViewById(R.id.tv_high_pressure);
        btn_sync_pressure = (Button) findViewById(R.id.btn_sync_pressure);
        btn_start_pressure = (Button) findViewById(R.id.btn_start_pressure);
        btn_stop_pressure = (Button) findViewById(R.id.btn_stop_pressure);

        btn_sync_pressure.setOnClickListener(this);
        btn_start_pressure.setOnClickListener(this);
        btn_stop_pressure.setOnClickListener(this);
        initIbeacon();
        open_camera = (Button) findViewById(R.id.open_camera);
        close_camera = (Button) findViewById(R.id.close_camera);
        open_camera.setOnClickListener(this);
        close_camera.setOnClickListener(this);

        skip_time = (TextView) findViewById(R.id.skip_time);
        skip_count = (TextView) findViewById(R.id.skip_count);
        skip_calorie = (TextView) findViewById(R.id.skip_calorie);


        today_sports_time = (Button) findViewById(R.id.today_sports_time);
        today_sports_time.setOnClickListener(this);
        seven_days_sports_time = (Button) findViewById(R.id.seven_days_sports_time);
        seven_days_sports_time.setOnClickListener(this);

        et_universal_interface = findViewById(R.id.et_universal_interface);
        universal_interface = (Button) findViewById(R.id.universal_interface);
        universal_interface.setOnClickListener(this);
        universal_interface_ble_sdk = (Button) findViewById(R.id.universal_interface_ble_sdk);
        universal_interface_ble_sdk.setOnClickListener(this);

        rate_calibration = (Button) findViewById(R.id.rate_calibration);
        rate_calibration.setOnClickListener(this);
        turn_wrist_calibration = (Button) findViewById(R.id.turn_wrist_calibration);
        turn_wrist_calibration.setOnClickListener(this);
        set_band_language = (Button) findViewById(R.id.set_band_language);
        set_band_language.setOnClickListener(this);
        settings_bracelet_interface_set = (Button) findViewById(R.id.settings_bracelet_interface_set);
        settings_bracelet_interface_set.setOnClickListener(this);
        query_bracelet_model = (Button) findViewById(R.id.query_bracelet_model);
        query_bracelet_model.setOnClickListener(this);
        query_customer_ID = (Button) findViewById(R.id.query_customer_ID);
        query_customer_ID.setOnClickListener(this);
        query_currently_sport = (Button) findViewById(R.id.query_currently_sport);
        query_currently_sport.setOnClickListener(this);
        set_currently_sport = (Button) findViewById(R.id.set_currently_sport);
        set_currently_sport.setOnClickListener(this);
        control_sport = (Button) findViewById(R.id.control_sport);
        control_sport.setOnClickListener(this);
        sync_currently_sport = (Button) findViewById(R.id.sync_currently_sport);
        sync_currently_sport.setOnClickListener(this);
        query_data = (Button) findViewById(R.id.query_data);
        query_data.setOnClickListener(this);

        hrh_stop_sport = (Button) findViewById(R.id.hrh_stop_sport);
        hrh_stop_sport.setOnClickListener(this);
        hrh_start_sport = (Button) findViewById(R.id.hrh_start_sport);
        hrh_start_sport.setOnClickListener(this);
        hrh_set_interval = (Button) findViewById(R.id.hrh_set_interval);
        hrh_set_interval.setOnClickListener(this);
        hrh_query_status = (Button) findViewById(R.id.hrh_query_status);
        hrh_query_status.setOnClickListener(this);
        dial_setting = (Button) findViewById(R.id.dial_setting);
        dial_setting.setOnClickListener(this);
        body_temperature = (Button) findViewById(R.id.body_temperature);
        body_temperature.setOnClickListener(this);

        start_test_oxygen = (Button) findViewById(R.id.start_test_oxygen);
        start_test_oxygen.setOnClickListener(this);
        stop_test_oxygen = (Button) findViewById(R.id.stop_test_oxygen);
        stop_test_oxygen.setOnClickListener(this);
        sync_oxygen_data = (Button) findViewById(R.id.sync_oxygen_data);
        sync_oxygen_data.setOnClickListener(this);
        query_oxygen_data = (Button) findViewById(R.id.query_oxygen_data);
        query_oxygen_data.setOnClickListener(this);
        Button query_supported_messaging_apps = findViewById(R.id.query_supported_messaging_apps);
        query_supported_messaging_apps.setOnClickListener(this);

        start_test_breathe = (Button) findViewById(R.id.start_test_breathe);
        start_test_breathe.setOnClickListener(this);
        stop_test_breathe = (Button) findViewById(R.id.stop_test_breathe);
        stop_test_breathe.setOnClickListener(this);
        sync_breathe_data = (Button) findViewById(R.id.sync_breathe_data);
        sync_breathe_data.setOnClickListener(this);
        query_breathe_data = (Button) findViewById(R.id.query_breathe_data);
        query_breathe_data.setOnClickListener(this);
        query_breathe_test_status = (Button) findViewById(R.id.query_breathe_test_status);
        query_breathe_test_status.setOnClickListener(this);
        breathe_automatic_test = (Button) findViewById(R.id.breathe_automatic_test);
        breathe_automatic_test.setOnClickListener(this);
        breathe_time_period = (Button) findViewById(R.id.breathe_time_period);
        breathe_time_period.setOnClickListener(this);
    }

    /**
     * ???????????? ???????????????UI
     */
    private StepChangeListener mOnStepChangeListener = new StepChangeListener() {
        @Override
        public void onStepChange(StepOneDayAllInfo info) {
            if (info != null) {
                mSteps = info.getStep();
                mDistance = info.getDistance();
                mCalories = info.getCalories();

                mRunSteps = info.getRunSteps();
                mRunCalories = info.getRunCalories();
                mRunDistance = info.getRunDistance();
                mRunDurationTime = info.getRunDurationTime();

                mWalkSteps = info.getWalkSteps();
                mWalkCalories = info.getWalkCalories();
                mWalkDistance = info.getWalkDistance();
                mWalkDurationTime = info.getWalkDurationTime();

            }
            LogUtils.d(TAG, "mSteps =" + mSteps + ",mDistance ="
                    + mDistance + ",mCalories =" + mCalories + ",mRunSteps ="
                    + mRunSteps + ",mRunCalories =" + mRunCalories
                    + ",mRunDistance =" + mRunDistance + ",mRunDurationTime ="
                    + mRunDurationTime + ",mWalkSteps =" + mWalkSteps
                    + ",mWalkCalories =" + mWalkCalories + ",mWalkDistance ="
                    + mWalkDistance + ",mWalkDurationTime ="
                    + mWalkDurationTime);

            mHandler.sendEmptyMessage(UPDATE_STEP_UI_MSG);

            HomeFragment homeFragment = (HomeFragment) (getSupportFragmentManager().getFragments().get(0));

            homeFragment.initTodayStepData(mSteps, mCalories, mDistance);
        }
    };
    /**
     * ???????????? ???????????????UI
     */
    private SleepChangeListener mOnSleepChangeListener = new SleepChangeListener() {

        @Override
        public void onSleepChange() {
            mHandler.sendEmptyMessage(UPDATE_SLEEP_UI_MSG);
        }

    };

    private RateChangeListener mOnRateListener = new RateChangeListener() {

        @Override
        public void onRateChange(int rate, int status) {
            tempRate = rate;
            tempStatus = status;
            LogUtils.i(TAG, "Rate_tempRate =" + tempRate);
            mHandler.sendEmptyMessage(UPDATA_REAL_RATE_MSG);
        }
    };

    private RateOf24HourRealTimeListener mOnRateOf24HourListener = new RateOf24HourRealTimeListener() {
        @Override
        public void onRateOf24HourChange(int maxHeartRateValue, int minHeartRateValue, int averageHeartRateValue, boolean isRealTimeValue) {
            //??????24??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            LogUtils.i(TAG, "??????24???????????? maxHeartRateValue =" + maxHeartRateValue + ",minHeartRateValue=" + minHeartRateValue + ",averageHeartRateValue=" + averageHeartRateValue);
        }
    };
    private BloodPressureChangeListener mOnBloodPressureListener = new BloodPressureChangeListener() {

        @Override
        public void onBloodPressureChange(int hightPressure, int lowPressure,
                                          int status) {
            tempBloodPressureStatus = status;
            high_pressure = hightPressure;
            low_pressure = lowPressure;
            mHandler.sendEmptyMessage(UPDATA_REAL_BLOOD_PRESSURE_MSG);
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RATE_SYNC_FINISH_MSG:
                    UpdateUpdataRateMainUI(CalendarUtils.getCalendar(0));
                    Toast.makeText(mContext, "Rate sync finish", Toast.LENGTH_SHORT).show();
                    break;
                case RATE_OF_24_HOUR_SYNC_FINISH_MSG:
                    Toast.makeText(mContext, "24 Hour Rate sync finish", Toast.LENGTH_SHORT).show();
                    mySQLOperate.query24HourRateAllInfo();
                    break;
                case UPDATA_REAL_RATE_MSG:
                    tv_rate.setText(tempRate + "");// ????????????
                    if (tempStatus == GlobalVariable.RATE_TEST_FINISH) {
                        UpdateUpdataRateMainUI(CalendarUtils.getCalendar(0));
                        Toast.makeText(mContext, "Rate test finish", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case GlobalVariable.GET_RSSI_MSG:
                    Bundle bundle = msg.getData();
                    rssi_tv.setText(bundle.getInt(GlobalVariable.EXTRA_RSSI) + "");
                    break;
                case UPDATE_STEP_UI_MSG:

                    updateSteps(mSteps);
                    updateCalories(mCalories);
                    updateDistance(mDistance);

                    LogUtils.d(TAG, "mSteps =" + mSteps + ",mDistance ="
                            + mDistance + ",mCalories =" + mCalories);
                    break;
                case UPDATE_SLEEP_UI_MSG:
                    querySleepInfo();
                    LogUtils.d(TAG, "UPDATE_SLEEP_UI_MSG");
                    break;
                case GlobalVariable.START_PROGRESS_MSG:
                    LogUtils.i(TAG, "(Boolean) msg.obj=" + (Boolean) msg.obj);
                    isUpdateSuccess = (Boolean) msg.obj;
                    LogUtils.i(TAG, "BisUpdateSuccess=" + isUpdateSuccess);
                    startProgressDialog();
                    mHandler.postDelayed(mDialogRunnable, TIME_OUT);
                    break;
                case GlobalVariable.DOWNLOAD_IMG_FAIL_MSG:
                    Toast.makeText(MainActivity.this, R.string.download_fail, Toast.LENGTH_LONG)
                            .show();
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    if (mDialogRunnable != null)
                        mHandler.removeCallbacks(mDialogRunnable);
                    break;
                case GlobalVariable.DISMISS_UPDATE_BLE_DIALOG_MSG:
                    LogUtils.i(TAG, "(Boolean) msg.obj=" + (Boolean) msg.obj);
                    isUpdateSuccess = (Boolean) msg.obj;
                    LogUtils.i(TAG, "BisUpdateSuccess=" + isUpdateSuccess);
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    if (mDialogRunnable != null) {
                        mHandler.removeCallbacks(mDialogRunnable);
                    }

                    if (isUpdateSuccess) {
                        Toast.makeText(
                                mContext,
                                getResources().getString(
                                        R.string.ble_update_successful), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case GlobalVariable.SERVER_IS_BUSY_MSG:
                    Toast.makeText(mContext,
                            getResources().getString(R.string.server_is_busy), Toast.LENGTH_SHORT)
                            .show();
                    break;
                case DISCONNECT_MSG:
                    connect_status.setText(getString(R.string.disconnect));
                    CURRENT_STATUS = DISCONNECTED;
                    Toast.makeText(mContext, "disconnect or connect falie", Toast.LENGTH_SHORT)
                            .show();

                    String lastConnectAddr0 = SPUtil.getInstance(mContext).getLastConnectDeviceAddress();
                    boolean connectResute0 = mBLEServiceOperate
                            .connect(lastConnectAddr0);
                    LogUtils.i(TAG, "connectResute0=" + connectResute0);

                    break;
                case CONNECTED_MSG:
                    connect_status.setText(getString(R.string.connected));
                    mBluetoothLeService.setRssiHandler(mHandler);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!Thread.interrupted()) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                if (mBluetoothLeService != null) {
                                    mBluetoothLeService.readRssi();
                                }
                            }
                        }
                    }).start();
                    CURRENT_STATUS = CONNECTED;
                    Toast.makeText(mContext, "connected", Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(new ConnectEvent(true));
                    break;

                case GlobalVariable.UPDATE_BLE_PROGRESS_MSG: // (???) ????????????????????????
                    int schedule = msg.arg1;
                    LogUtils.i("zznkey", "schedule =" + schedule);
                    if (mProgressDialog == null) {
                        startProgressDialog();
                    }
                    mProgressDialog.setSchedule(schedule);
                    break;
                case OPEN_CHANNEL_OK_MSG:// ????????????OK
                    test_channel.setText(getResources().getString(
                            R.string.open_channel_ok));
                    resultBuilder.append(getResources().getString(
                            R.string.open_channel_ok)
                            + ",");
                    show_result.setText(resultBuilder.toString());

                    mWriteCommand.sendAPDUToBLE(WriteCommandToBLE
                            .hexString2Bytes(testKey1));
                    break;
                case CLOSE_CHANNEL_OK_MSG:// ????????????OK
                    test_channel.setText(getResources().getString(
                            R.string.close_channel_ok));
                    resultBuilder.append(getResources().getString(
                            R.string.close_channel_ok)
                            + ",");
                    show_result.setText(resultBuilder.toString());
                    break;
                case TEST_CHANNEL_OK_MSG:// ????????????OK
                    test_channel.setText(getResources().getString(
                            R.string.test_channel_ok));
                    resultBuilder.append(getResources().getString(
                            R.string.test_channel_ok)
                            + ",");
                    show_result.setText(resultBuilder.toString());
                    mWriteCommand.closeBLEchannel();
                    break;

                case SHOW_SET_PASSWORD_MSG:
                    showPasswordDialog(GlobalVariable.PASSWORD_TYPE_SET);
                    break;
                case SHOW_INPUT_PASSWORD_MSG:
                    showPasswordDialog(GlobalVariable.PASSWORD_TYPE_INPUT);
                    break;
                case SHOW_INPUT_PASSWORD_AGAIN_MSG:
                    showPasswordDialog(GlobalVariable.PASSWORD_TYPE_INPUT_AGAIN);
                    break;
                case OFFLINE_SWIM_SYNC_OK_MSG:
                    upDateTodaySwimData();
                    show_result.setText(mContext.getResources().getString(
                            R.string.sync_swim_finish));
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.sync_swim_finish), Toast.LENGTH_SHORT)
                            .show();
                    break;

                case UPDATA_REAL_BLOOD_PRESSURE_MSG:
                    tv_low_pressure.setText(low_pressure + "");// ????????????
                    tv_high_pressure.setText(high_pressure + "");// ????????????
                    if (tempBloodPressureStatus == GlobalVariable.BLOOD_PRESSURE_TEST_FINISH) {
                        UpdateBloodPressureMainUI(CalendarUtils.getCalendar(0));
                        Toast.makeText(
                                mContext,
                                getResources().getString(R.string.test_pressure_ok),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case OFFLINE_BLOOD_PRESSURE_SYNC_OK_MSG:
                    UpdateBloodPressureMainUI(CalendarUtils.getCalendar(0));
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.sync_pressure_ok), Toast.LENGTH_SHORT)
                            .show();
                    break;
                case SERVER_CALL_BACK_OK_MSG:
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    if (mDialogServerRunnable != null) {
                        mHandler.removeCallbacks(mDialogServerRunnable);
                    }
                    String localVersion = SPUtil.getInstance(mContext).getImgLocalVersion();
                    int status = mUpdates.getBLEVersionStatus(localVersion);
                    LogUtils.i(TAG, "???????????? VersionStatus =" + status);
                    if (status == GlobalVariable.OLD_VERSION_STATUS) {
                        updateBleDialog();// update remind
                    } else if (status == GlobalVariable.NEWEST_VERSION_STATUS) {
                        Toast.makeText(mContext,
                                getResources().getString(R.string.ble_is_newest), Toast.LENGTH_SHORT)
                                .show();
                    }/*
                     * else if (status == GlobalVariable.FREQUENT_ACCESS_STATUS) {
                     * Toast.makeText( mContext, getResources().getString(
                     * R.string.frequent_access_server), 0) .show(); }
                     */
                    break;
                case OFFLINE_SKIP_SYNC_OK_MSG:
                    upDateTodaySkipData();
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.sync_skip_finish), Toast.LENGTH_SHORT)
                            .show();
                    show_result.setText(mContext.getResources().getString(
                            R.string.sync_skip_finish));
                    break;
                case test_mag1:
                    Toast.makeText(MainActivity.this, "????????????1??????????????????????????????,???????????????????????????", Toast.LENGTH_SHORT)//
                            .show();
                    show_result.setText("????????????1??????????????????????????????,???????????????????????????");
                    break;
                case test_mag2:
                    Toast.makeText(MainActivity.this, "????????????3???????????????????????????SOS", Toast.LENGTH_SHORT)
                            .show();
                    show_result.setText("????????????3???????????????????????????SOS");
                    break;
                case OFFLINE_STEP_SYNC_OK_MSG:
                    Toast.makeText(MainActivity.this, "????????????????????????", Toast.LENGTH_SHORT)
                            .show();
                    show_result.setText("????????????????????????");
                    break;
                case UPDATE_SPORTS_TIME_DETAILS_MSG:
                    show_result.setText(resultBuilder.toString());
                    break;
                case UNIVERSAL_INTERFACE_SDK_TO_BLE_SUCCESS_MSG:
                    show_result.setText("sdk???????????????ble??????????????????????????????????????????");
                    break;
                case UNIVERSAL_INTERFACE_SDK_TO_BLE_FAIL_MSG:
                    show_result.setText("sdk???????????????ble??????????????????????????????????????????");
                    break;
                case UNIVERSAL_INTERFACE_BLE_TO_SDK_SUCCESS_MSG:
                    show_result.setText("ble???????????????sdk??????????????????????????????????????????");
                    break;
                case UNIVERSAL_INTERFACE_BLE_TO_SDK_FAIL_MSG:
                    show_result.setText("ble???????????????sdk??????????????????????????????????????????");
                    break;
                case BREATHE_TEST_START_HAS_VALUE_MSG:
                    BreatheInfo info1 = (BreatheInfo) msg.obj;
                    show_result.setText("Start has breathe value:" + info1.getBreatheValue());
                    break;
                case BREATHE_TEST_START_NO_VALUE_MSG:
                    show_result.setText("Start has no breathe value");
                    break;
                case BREATHE_TEST_STOP_HAS_VALUE_MSG:
                    BreatheInfo info2 = (BreatheInfo) msg.obj;
                    show_result.setText("Stop has breathe value:" + info2.getBreatheValue());
                    break;
                case BREATHE_TEST_STOP_NO_VALUE_MSG:
                    show_result.setText("Stop has no breathe value");
                    break;
                case BREATHE_TEST_TIME_OUT_MSG:
                    show_result.setText("Breath test time out");
                    break;
                case BIND_CONNECT_SEND_ACCOUNT_ID_MSG:
                    mWriteCommand.sendAccountId(1234);
                    break;
                default:
                    break;
            }
        }
    };

    /*
     * ???????????????????????????????????????????????????????????????
     */
    private void UpdateUpdataRateMainUI(String calendar) {
        // UTESQLOperate mySQLOperate = UTESQLOperate.getInstance(mContext);
        RateOneDayInfo mRateOneDayInfo = mySQLOperate
                .queryRateOneDayMainInfo(calendar);
        if (mRateOneDayInfo != null) {
            int currentRate = mRateOneDayInfo.getCurrentRate();
            int lowestValue = mRateOneDayInfo.getLowestRate();
            int averageValue = mRateOneDayInfo.getVerageRate();
            int highestValue = mRateOneDayInfo.getHighestRate();
            // current_rate.setText(currentRate + "");
            if (currentRate == 0) {
                tv_rate.setText("--");
            } else {
                tv_rate.setText(currentRate + "");
            }
            if (lowestValue == 0) {
                tv_lowest_rate.setText("--");
            } else {
                tv_lowest_rate.setText(lowestValue + "");
            }
            if (averageValue == 0) {
                tv_verage_rate.setText("--");
            } else {
                tv_verage_rate.setText(averageValue + "");
            }
            if (highestValue == 0) {
                tv_highest_rate.setText("--");
            } else {
                tv_highest_rate.setText(highestValue + "");
            }
        } else {
            tv_rate.setText("--");
        }
    }

    /*
     * ??????????????????????????????????????????
     */
    public void getOneDayRateinfo(String calendar) {
        // UTESQLOperate mySQLOperate = UTESQLOperate.getInstance(mContext);
        List<RateOneDayInfo> mRateOneDayInfoList = mySQLOperate
                .queryRateOneDayDetailInfo(calendar);
        if (mRateOneDayInfoList != null && mRateOneDayInfoList.size() > 0) {
            int size = mRateOneDayInfoList.size();
            int[] rateValue = new int[size];
            int[] timeArray = new int[size];
            for (int i = 0; i < size; i++) {
                rateValue[i] = mRateOneDayInfoList.get(i).getRate();
                timeArray[i] = mRateOneDayInfoList.get(i).getTime();
                LogUtils.d(TAG, "rateValue[" + i + "]=" + rateValue[i]
                        + "timeArray[" + i + "]=" + timeArray[i]);
            }
        } else {

        }
    }

    private void startProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = CustomProgressDialog
                    .createDialog(MainActivity.this);
            mProgressDialog.setMessage(getResources().getString(
                    R.string.ble_updating));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    private Runnable mDialogRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            // mDownloadButton.setText(R.string.suota_update_succeed);
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            mHandler.removeCallbacks(mDialogRunnable);
            if (!isUpdateSuccess) {
                Toast.makeText(MainActivity.this,
                        getResources().getString(R.string.ble_fail_update), Toast.LENGTH_SHORT)
                        .show();
                mUpdates.clearUpdateSetting();
            } else {
                isUpdateSuccess = false;
                Toast.makeText(
                        MainActivity.this,
                        getResources()
                                .getString(R.string.ble_update_successful), Toast.LENGTH_SHORT)
                        .show();
            }

        }
    };
    private Runnable mDialogServerRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            // mDownloadButton.setText(R.string.suota_update_succeed);
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            mHandler.removeCallbacks(mDialogServerRunnable);
            Toast.makeText(MainActivity.this,
                    getResources().getString(R.string.server_is_busy), Toast.LENGTH_SHORT)
                    .show();
        }
    };

    private void updateSteps(int steps) {
        LogUtils.d(TAG, "steps =" + steps);
        String stepString = "0";
        if (steps <= 0) {
        } else {
            stepString = "" + steps;
        }

        tv_steps.setText(stepString);

    }


    private void updateCalories(float mCalories) {
        if (mCalories <= 0) {
            tv_calorie.setText(mContext.getResources().getString(
                    R.string.zero_kilocalorie));
        } else {
            tv_calorie.setText("" + mCalories + " "
                    + mContext.getResources().getString(R.string.kilocalorie));
        }

    }

    private void updateDistance(float mDistance) {
        if (mDistance < 0.01) {
            tv_distance.setText(mContext.getResources().getString(
                    R.string.zero_kilometers));

        } else {
            tv_distance.setText(mDistance + " "
                    + mContext.getResources().getString(R.string.kilometers));
        }
//		else if (mDistance >= 100) {
//			tv_distance.setText(("" + mDistance).substring(0, 3) + " "
//					+ mContext.getResources().getString(R.string.kilometers));
//		} else {
//			tv_distance.setText(("" + (mDistance + 0.000001f)).substring(0, 4)
//					+ " "
//					+ mContext.getResources().getString(R.string.kilometers));
//		}

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        boolean ble_connecte = SPUtil.getInstance(mContext).getBleConnectStatus();
        if (ble_connecte) {
            connect_status.setText(getString(R.string.connected));
        } else {
            connect_status.setText(getString(R.string.disconnect));
        }
    }


    @Override
    public void onClick(View v) {
        boolean ble_connecte = SPUtil.getInstance(mContext).getBleConnectStatus();
        switch (v.getId()) {
            case R.id.btn_confirm:

                if (ble_connecte) {
                    String height = et_height.getText().toString();
                    String weight = et_weight.getText().toString();
                    if (height.equals("") || weight.equals("")) {
                        Toast.makeText(mContext, "???????????????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        int Height = Integer.valueOf(height);
                        int Weight = Integer.valueOf(weight);
                        mWriteCommand.sendStepLenAndWeightToBLE(Height, Weight, 5,
                                10000, true, true, 150, true, 20, false, true, 50, GlobalVariable.TMP_UNIT_CELSIUS, true);
//                    int height, int weight,int offScreenTime,int stepTask,
//                    boolean isRraisHandbrightScreenSwitchOpen,boolean isHighestRateOpen,
//                    int highestRate,boolean isMale,int age,boolean bandLostOpen
//                    ,boolean isLowestRateOpen,int lowestRate,int celsiusFahrenheitValue,boolean isChinese
                        // ????????????????????????????????????5s,????????????10000?????????????????????true?????????false??????????????????????????????true?????????false?????????
                        //???????????????????????????????????????true?????????false?????????20??????????????????0~255???????????????????????????true????????????false?????????;
//                    ??????????????????  true ?????????false ????????????????????????????????????40-100?????????50
//                  int celsiusFahrenheitValue?????????????????????GlobalVariable.TMP_UNIT_CELSIUS????????????GlobalVariable.TMP_UNIT_FAHRENHEIT
//                   boolean isChinese true ?????????false ??????
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
//			List<RateOneDayInfo> mRateOneDayInfoList = new ArrayList<RateOneDayInfo>();
//			mRateOneDayInfoList =mySQLOperate.queryRateOneDayDetailInfo(CalendarUtils.getCalendar(0));
//			LogUtils.d(TAG, "mRateOneDayInfoList ="+mRateOneDayInfoList);
//			if (mRateOneDayInfoList!=null) {
//				for (int i = 0; i < mRateOneDayInfoList.size(); i++) {
//					int time = mRateOneDayInfoList.get(i).getTime();
//					int rate = mRateOneDayInfoList.get(i).getRate();
//					LogUtils.d(TAG, "mRateOneDayInfoList time ="+time+",rate ="+rate);
//				}
//			}else {
//				
//			}
//			RateOneDayInfo mRateOneDayInfo = null;
//			mRateOneDayInfo =mySQLOperate.queryRateOneDayMainInfo(CalendarUtils.getCalendar(0));
//			if (mRateOneDayInfo!=null) {
//				 int lowestRate;
//				 int verageRate;
//				 int highestRate;
//				 int currentRate;
//			}
//			List<StepOneDayAllInfo> list = mySQLOperate.queryRunWalkAllDay();
//			if (list != null) {
//				for (int i = 0; i < list.size(); i++) {
//					String calendar = list.get(i).getCalendar();
//					int step = list.get(i).getStep();
//					int runSteps = list.get(i).getRunSteps();
//					int walkSteps = list.get(i).getWalkSteps();
//					LogUtils.d(TAG, "queryRunWalkAllDay calendar =" + calendar
//							+ ",step =" + step + ",runSteps =" + runSteps
//							+ ",walkSteps =" + walkSteps);
//				}
//			}
                break;
            case R.id.bt_sedentary_open:
                String period = et_sedentary_period.getText().toString();
                if (period.equals("")) {
                    Toast.makeText(mContext, "Please input remind peroid", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    int period_time = Integer.valueOf(period);
                    if (period_time < 30) {
                        Toast.makeText(
                                mContext,
                                "Please make sure period_time more than 30 minutes",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        if (ble_connecte) {
//						mWriteCommand.sendSedentaryRemindCommand(
//								GlobalVariable.OPEN_SEDENTARY_REMIND,
//								period_time);
                            int fromTimeHour = 10;//?????????????????????
                            int fromTimeMinute = 59;//?????????????????????
                            int toTimeHour = 16;//?????????????????????
                            int toTimeMinute = 50;//?????????????????????
                            boolean lunchBreak = true;//??????????????? true???12:00-14:00 ???????????????????????????,false ???12:00-14:00 ??????????????????????????????
                            mWriteCommand.sendSedentaryRemindCommand(
                                    GlobalVariable.OPEN_SEDENTARY_REMIND,
                                    period_time, fromTimeHour, fromTimeMinute, toTimeHour, toTimeMinute, lunchBreak);
                        } else {
                            Toast.makeText(mContext,
                                    getString(R.string.disconnect),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }

//			StepOneDayAllInfo mStepInfo =mySQLOperate.queryRunWalkInfo("20171106");
//			if (mStepInfo != null) {
//				String calendar ="";
//				 int step = mStepInfo.getStep();
//				 int mCaloriesValue = mStepInfo.getCalories();
//				 float distance = mStepInfo.getDistance();
//				// ??????
//				int runSteps = mStepInfo.getRunSteps();
//				int runCalories = mStepInfo.getRunCalories();
//				float runDistance = mStepInfo.getRunDistance();
//				int runDurationTime = mStepInfo.getRunDurationTime();
//				String runHourDetails = mStepInfo.getRunHourDetails();
//				// ??????
//				 int walkSteps = mStepInfo.getWalkSteps();
//				 int walkCalories = mStepInfo.getWalkCalories();
//				 float walkDistance = mStepInfo.getWalkDistance();
//				 int walkDurationTime = mStepInfo.getWalkDurationTime();
//				 String walkHourDetails = mStepInfo.getWalkHourDetails();
//				int totalSteps = runSteps + walkSteps;
//				
//				
//				LogUtils.d(TAG, "queryRunWalkInfo calendar ="+calendar+",step ="+step+",mCaloriesValue ="+mCaloriesValue+",distance ="+distance);
//				LogUtils.d(TAG, "queryRunWalkInfo runSteps ="+runSteps+",runCalories ="+runCalories+",runDistance ="+runDistance+",runDurationTime ="+runDurationTime);
//				LogUtils.d(TAG, "queryRunWalkInfo walkSteps ="+walkSteps+",walkCalories ="+walkCalories+",walkDistance ="+walkDistance+",walkDurationTime ="+walkDurationTime);
//			}
                break;
            case R.id.bt_sedentary_close:
                if (ble_connecte) {
//				mWriteCommand.sendSedentaryRemindCommand(
//						GlobalVariable.CLOSE_SEDENTARY_REMIND, 0);
                    int fromTimeHour = 10;//?????????????????????
                    int fromTimeMinute = 59;//?????????????????????
                    int toTimeHour = 16;//?????????????????????
                    int toTimeMinute = 50;//?????????????????????
                    boolean lunchBreak = true;//??????????????? true???12:00-14:00 ???????????????????????????,false ???12:00-14:00 ??????????????????????????????
                    mWriteCommand.sendSedentaryRemindCommand(
                            GlobalVariable.CLOSE_SEDENTARY_REMIND,
                            0, fromTimeHour, fromTimeMinute, toTimeHour, toTimeMinute, lunchBreak);
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_sync_step:
                if (ble_connecte) {
                    mWriteCommand.syncAllStepData();
//				mWriteCommand.syncAllSwimData();
//				mWriteCommand.syncAllSkipData();
//				mySQLOperate.querySkipDayInfo("20170629");
//				mySQLOperate.querySwimDayInfo("20170629");
//				mySQLOperate.queryRunWalkInfo("20170629");

                    // ????????????
//                    LogUtils.setLogEnable(true);//????????????log
//                    ????????????????????????????????????????????????sendDeviceParametersInfoToBLE(info)?????????
//                    ?????????????????????set???????????????????????????set.???????????????????????????????????????true????????????
//                    GetFunctionList.isSupportFunction_Second(getApplicationContext(),
//                            GlobalVariable.IS_SUPPORT_NEW_PARAMETER_SETTINGS_FUNCTION)
//                    DeviceParametersInfo info = new DeviceParametersInfo();
//                    info.setBodyHeight(180);
//                    info.setBodyWeight(65);
//                    info.setOffScreenTime(15);
//                    info.setStepTask(12450);
//                    info.setRaisHandbrightScreenSwitch(DeviceParametersInfo.switchStatusYes);
//                    info.setHighestRateAndSwitch(141, DeviceParametersInfo.switchStatusYes);
//                    info.setBodyAge(36);
//                    info.setBodyGender(DeviceParametersInfo.switchStatusNo);
//                    info.setDeviceLostSwitch(DeviceParametersInfo.switchStatusNo);
//                    info.setOnlySupportEnCn(DeviceParametersInfo.switchStatusYes);
//                    info.setCelsiusFahrenheitValue(DeviceParametersInfo.switchStatusYes);
//                    mWriteCommand.sendDeviceParametersInfoToBLE(info);
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.btn_sync_sleep:
                if (ble_connecte) {
//                testSetBandParm();
                    mWriteCommand.syncAllSleepData();
//				mWriteCommand.syncAllSportsModeData();
                    // mWriteCommand.syncWeatherToBLE(mContext, "?????????"); //??????????????????
                    // mWriteCommand.syncWeatherToBLE(mContext, "?????????");
//				mWriteCommand.queryDialMode();//??????????????????????????????
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_sync_rate:
//			mySQLOperate.queryBallSports(GlobalVariable.BALL_TYPE_TABLETENNIS);
                if (ble_connecte) {
                    mWriteCommand.syncRateData();

                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_rate_start:
//			 List<BallSportsInfo> info =mySQLOperate.queryBallSportsDayInfo(GlobalVariable.BALL_TYPE_TABLETENNIS,"20180625");
//			 if (info!=null) {
//				 for (int i = 0; i < info.size(); i++) {
//						String calendar =info.get(i).getCalendar();
//						int ca =info.get(i).getCalories();
//						LogUtils.d(TAG, "??????????????? calendar ="+calendar+",ca ="+ca);
//					}
//			}

                if (ble_connecte) {
                    mWriteCommand
                            .sendRateTestCommand(GlobalVariable.RATE_TEST_START);
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_rate_stop:
                if (ble_connecte) {
                    mWriteCommand
                            .sendRateTestCommand(GlobalVariable.RATE_TEST_STOP);
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.read_ble_version:
                if (ble_connecte) {
                    mWriteCommand.sendToReadBLEVersion(); // ????????????BLE?????????
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.read_ble_battery:
//			StepOneDayAllInfo allInfo = mySQLOperate
//					.queryRunWalkInfo("20170724");
//			LogUtils.d(TAG, "allInfo =" + allInfo);
//			if (allInfo != null) {
//				// ?????????????????????
//				int steps = allInfo.getStep();
//				int calories = allInfo.getCalories();
//				float distance = allInfo.getDistance();
//				// ??????
//				int runSteps = allInfo.getRunSteps();
//				int runCalories = allInfo.getRunCalories();
//				float runDistance = allInfo.getRunDistance();
//				int runDurationTime = allInfo.getRunDurationTime();
//				// ??????
//				int walkSteps = allInfo.getWalkSteps();
//				int walkCalories = allInfo.getWalkCalories();
//				float walkDistance = allInfo.getWalkDistance();
//				int walkDurationTime = allInfo.getWalkDurationTime();
//				LogUtils.d(TAG, " steps =" + steps + ",calories =" + calories
//						+ ",distance" + distance);
//				LogUtils.d(TAG, " runSteps =" + runSteps + ",runCalories ="
//						+ runCalories + ",runDistance" + runDistance
//						+ ",runDurationTime=" + runDurationTime);
//				LogUtils.d(TAG, " walkSteps =" + walkSteps + ",walkCalories ="
//						+ walkCalories + ",walkDistance" + walkDistance
//						+ ",walkDurationTime=" + walkDurationTime);
//				int hourStep = 0;
//				int time = 0;
//				int startTime =0;
//				int endTime =0;
//				int useTime =0;
//				ArrayList<StepOneHourInfo> hourInfos = allInfo
//						.getStepOneHourArrayInfo();
//				for (int i = 0; i < hourInfos.size(); i++) {
//					time = hourInfos.get(i).getTime();
//					hourStep = hourInfos.get(i).getStep();
//					LogUtils.d(TAG, "????????????????????? time =" + time + ",hourStep ="
//							+ hourStep);
//				}
//				ArrayList<StepRunHourInfo> hourRunInfos = allInfo
//						.getStepRunHourArrayInfo();
//				for (int i = 0; i < hourRunInfos.size(); i++) {
//					time = hourRunInfos.get(i).getTime();
//					hourStep = hourRunInfos.get(i).getRunSteps();
//					startTime =hourRunInfos.get(i).getStartRunTime();
//					endTime =hourRunInfos.get(i).getEndRunTime();
//					useTime =hourRunInfos.get(i).getRunDurationTime();
//					LogUtils.d(TAG, " ?????? time =" + time + ",hourStep =" + hourStep+ ",startTime =" + startTime+ ",endTime =" + endTime+ ",useTime =" + useTime);
//
//				}
//				ArrayList<StepWalkHourInfo> hourWalkInfos = allInfo
//						.getStepWalkHourArrayInfo();
//				for (int i = 0; i < hourWalkInfos.size(); i++) {
//					time = hourWalkInfos.get(i).getTime();
//					hourStep = hourWalkInfos.get(i).getWalkSteps();
//					startTime =hourWalkInfos.get(i).getStartWalkTime();
//					endTime =hourWalkInfos.get(i).getEndWalkTime();
//					useTime =hourWalkInfos.get(i).getWalkDurationTime();
//					LogUtils.d(TAG, " ?????? time =" + time + ",hourStep =" + hourStep+ ",startTime =" + startTime+ ",endTime =" + endTime+ ",useTime =" + useTime);
//
//				}
//			} else {
//
//			}

                if (ble_connecte) {
                    mWriteCommand.sendToReadBLEBattery();// ????????????????????????
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.set_ble_time:
                if (ble_connecte) {
//				 mWriteCommand.sendDisturbToBle(false, false, true, 20, 12,
//				 8, 20);
                    mWriteCommand.syncBLETime();
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.update_ble:
                new RxPermissions(MainActivity.this).request(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                        if (granted) {
                            //?????????????????????
//                        if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
//                            mWriteCommand.queryDeviceFearture();
//                            if (isNetworkAvailable(mContext)) {
//                                String localVersion = SPUtil.getInstance(mContext).getImgLocalVersion();
//                                if (!localVersion.equals("0")) {
//                                    int status = mUpdates.accessServerersionStatus(localVersion);
//                                    if (status == GlobalVariable.FREQUENT_ACCESS_STATUS) {
//                                        Toast.makeText(mContext,getResources().getString(R.string.frequent_access_server), Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        startProgressDialog();
//                                        mHandler.postDelayed(mDialogServerRunnable,TIME_OUT_SERVER);
//                                    }
//                                } else {
//                                    Toast.makeText(mContext,getResources().getString(R.string.read_ble_version_first), Toast.LENGTH_SHORT).show();
//                                }
//                            } else {
//								Toast.makeText(mContext,getResources().getString(R.string.confire_is_network_available), Toast.LENGTH_SHORT).show();
//
//                            }
//                        } else {
//							Toast.makeText(mContext,getResources().getString(R.string.please_connect_bracelet), Toast.LENGTH_SHORT).show();
//                        }

                            //????????????bin??????
                            String localVersion = SPUtil.getInstance(mContext).getImgLocalVersion();
                            if (!localVersion.equals("0")) {
                                String imgDir = "";
                                imgDir = getExternalFilesDir(null) + "/"
                                        + "RB196A_0.0.991.0_339c7fe6-b730e07fcfe61ec04c158350415b8efe.bin";
                                mUpdates.startOTA(imgDir);
                            } else {
                                Toast.makeText(mContext, getResources().getString(R.string.read_ble_version_first), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                break;
            // case 11:
            // mWriteCommand.sendToSetAlarmCommand(1, (byte) 33, 12, 22, true);
            // break;

            case R.id.unit:
                boolean ble_connected3 = SPUtil.getInstance(mContext).getBleConnectStatus();
                if (ble_connected3) {
                    if (unit.getText()
                            .toString()
                            .equals(getResources()
                                    .getString(R.string.metric_system))) {
                        SPUtil.getInstance(mContext).setMetriceUnit(true);
                        mWriteCommand.sendUnitAndHourFormatToBLE();
                        unit.setText(getResources().getString(R.string.inch_system));
                    } else {
                        SPUtil.getInstance(mContext).setMetriceUnit(false);
                        mWriteCommand.sendUnitAndHourFormatToBLE();//
                        // mWriteCommand.sendUnitAndHourFormatToBLE(unitType,
                        // hourFormat);//???????????????????????????
                        // unitType == GlobalVariable.UNIT_TYPE_METRICE ????????????
                        // unitType == GlobalVariable.UNIT_TYPE_IMPERIAL ????????????
                        // hourFormat == GlobalVariable.HOUR_FORMAT_24 24?????????
                        // hourFormat == GlobalVariable.HOUR_FORMAT_12 12?????????
                        unit.setText(getResources().getString(
                                R.string.metric_system));
                    }
                } else {
                    Toast.makeText(
                            mContext,
                            getResources().getString(
                                    R.string.please_connect_bracelet), Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.test_channel:
                boolean ble_connected4 = SPUtil.getInstance(mContext).getBleConnectStatus();
                if (ble_connected4) {
                    if (test_channel
                            .getText()
                            .toString()
                            .equals(getResources().getString(R.string.test_channel))
                            || test_channel
                            .getText()
                            .toString()
                            .equals(getResources().getString(
                                    R.string.test_channel_ok))
                            || test_channel
                            .getText()
                            .toString()
                            .equals(getResources().getString(
                                    R.string.close_channel_ok))) {
                        resultBuilder = new StringBuilder();
                        mWriteCommand.openBLEchannel();
                    } else {
                        Toast.makeText(
                                mContext,
                                getResources().getString(R.string.channel_testting),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(
                            mContext,
                            getResources().getString(
                                    R.string.please_connect_bracelet), Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.query_supported_messaging_apps:
                if (SPUtil.getInstance(mContext).getBleConnectStatus()) {
                    boolean pushMessageDisplay = GetFunctionList.isSupportFunction_Fifth(getApplicationContext(),
                            GlobalVariable.IS_SUPPORT_PUSH_MESSAGE_DISPLAY);
                    if (pushMessageDisplay) {
                        //???????????????????????????OnResult(true, ICallbackStatus.QUERY_PUSH_MESSAGE_DISPLAY_OK)
                        //????????????PushMessageUtil.isPushMessageDisplay1???PushMessageUtil.isPushMessageDisplay2??????
//                    ??????PushMessageUtil.isPushMessageDisplay1(mContext, PushMessageUtil.IS_DISPLAY_YOUTUBE);
                        //??????PushMessageUtil.isPushMessageDisplay2(mContext, PushMessageUtil.IS_DISPLAY_TELEGRAM);
                        mWriteCommand.queryPushMessageDisplay();
                    } else {
                        Toast.makeText(
                                mContext,
                                getResources().getString(
                                        R.string.not_support), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(
                            mContext,
                            getResources().getString(
                                    R.string.please_connect_bracelet), Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.push_message_content:
                if (ble_connecte) {
                    String pushContent = getResources().getString(
                            R.string.push_message_content);// ???????????????
                    boolean moreForeign = GetFunctionList.isSupportFunction_Third(getApplicationContext(),
                            GlobalVariable.IS_SUPPORT_MORE_FOREIGN_APP);
                    if (moreForeign) {//?????????????????????????????????????????????24????????????????????????
//                    mWriteCommand.sendTextToBle(pushContent, GlobalVariable.TYPE_QQ);
                        mWriteCommand.sendTextToBle(pushContent, GlobalVariable.TYPE_WECHAT);
//                         String smsNumber ="18045811234";
//                        SPUtil.getInstance(mContext).setSmsReceivedNumber(smsNumber);//???????????????????????????,????????????????????????.SDK ????????????
                        // mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_SMS);
                        // mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_PHONE);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_OTHERS);//???????????????
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_FACEBOOK);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_TWITTER);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_WHATSAPP);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_SKYPE);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_FACEBOOK_MESSENGER);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_HANGOUTS);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_LINE);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_LINKEDIN);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_INSTAGRAM);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_VIBER);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_KAKAO_TALK);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_VKONTAKTE);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_SNAPCHAT);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_GOOGLE_PLUS);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_GMAIL);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_FLICKR);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_TUMBLR);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_PINTEREST);
//                     mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_YOUTUBE);
                        //?????????queryPushMessageDisplay???????????????????????????APP?????????????????????????????????APP
//                        boolean telegram = PushMessageUtil.isPushMessageDisplay2(mContext, PushMessageUtil.IS_DISPLAY_TELEGRAM);
//                        if (telegram) {
//                            mWriteCommand.sendTextToBle(pushContent, GlobalVariable.TYPE_TELEGRAM);
//                        }

                    } else {//????????????????????????????????????5????????????????????????
//                    mWriteCommand.sendTextToBle(pushContent, GlobalVariable.TYPE_QQ);
                        // mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_WECHAT);
                        String smsNumber = "18045811234";
                        SPUtil.getInstance(mContext).setSmsReceivedNumber(smsNumber);//???????????????????????????,????????????????????????.SDK ????????????
                        mWriteCommand.sendTextToBle(pushContent, GlobalVariable.TYPE_SMS);
                        // mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_PHONE);
//                    mWriteCommand.sendTextToBle(pushContent,GlobalVariable.TYPE_OTHERS);//???????????????
                    }


                    show_result.setText(pushContent);
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_sync_pressure:
                if (ble_connecte) {
                    mWriteCommand.syncAllBloodPressureData();
                    show_result.setText(mContext.getResources().getString(
                            R.string.sync_pressure));
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_start_pressure:
                if (ble_connecte) {
                    mWriteCommand
                            .sendBloodPressureTestCommand(GlobalVariable.BLOOD_PRESSURE_TEST_START);
                    show_result.setText(mContext.getResources().getString(
                            R.string.start_pressure));
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }


                break;
            case R.id.btn_stop_pressure:
                if (ble_connecte) {
                    mWriteCommand
                            .sendBloodPressureTestCommand(GlobalVariable.BLOOD_PRESSURE_TEST_STOP);
                    show_result.setText(mContext.getResources().getString(
                            R.string.stop_pressure));
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ibeacon_command:
                if (ble_connecte) {
                    if (GetFunctionList.isSupportFunction(mContext,
                            GlobalVariable.IS_SUPPORT_IBEACON)) {// ?????????????????????ibeacon??????
                        switch (ibeaconSetOrRead) {
                            case GlobalVariable.IBEACON_SET:// ??????
                                switch (ibeaconStatus) {
                                    case GlobalVariable.IBEACON_TYPE_UUID:
                                        // ????????????ibeacon
                                        // ??????UUID????????????????????????16byte???ASIIC,??????30313233343536373031323334353637
                                        mWriteCommand.sendIbeaconSetCommand(
                                                "30313233343536373031323334353637",
                                                GlobalVariable.IBEACON_TYPE_UUID);// ??????UUID
                                        break;
                                    case GlobalVariable.IBEACON_TYPE_MAJOR:
                                        // //major???minor???????????????2byte???????????????0224
                                        mWriteCommand.sendIbeaconSetCommand("0224",
                                                GlobalVariable.IBEACON_TYPE_MAJOR);// ??????major
                                        break;
                                    case GlobalVariable.IBEACON_TYPE_MINOR:
                                        // //major???minor???????????????2byte???????????????0424
                                        mWriteCommand.sendIbeaconSetCommand("3424",
                                                GlobalVariable.IBEACON_TYPE_MINOR);// ??????minor
                                        break;
                                    case GlobalVariable.IBEACON_TYPE_DEVICE_NAME:
                                        // //Device
                                        // name????????????????????????0??????14byte???ASIIC??????3031323334353637303132333435
                                        mWriteCommand.sendIbeaconSetCommand(
                                                "3031323334353637303132333435",
                                                GlobalVariable.IBEACON_TYPE_DEVICE_NAME);// ????????????device
                                        // name
                                        break;
                                    case GlobalVariable.IBEACON_TYPE_TX_POWER:
                                        // TX_POWER??????????????? 1~0xfe??????????????????)???
                                        mWriteCommand.sendIbeaconSetCommand("78", GlobalVariable.IBEACON_TYPE_TX_POWER);// ??????TX_POWER
                                        break;
                                    case GlobalVariable.IBEACON_TYPE_ADVERTISING_INTERVAL:
                                        // advertising interval???????????????1~20????????????100ms?????????800ms?????????
                                        mWriteCommand.sendIbeaconSetCommand("14", GlobalVariable.IBEACON_TYPE_ADVERTISING_INTERVAL);// ??????advertising interval
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case GlobalVariable.IBEACON_GET:// ??????
                                switch (ibeaconStatus) {
                                    case GlobalVariable.IBEACON_TYPE_UUID:
                                        // //??????UUID
                                        mWriteCommand
                                                .sendIbeaconGetCommand(GlobalVariable.IBEACON_TYPE_UUID);
                                        break;
                                    case GlobalVariable.IBEACON_TYPE_MAJOR:
                                        // //??????major
                                        mWriteCommand
                                                .sendIbeaconGetCommand(GlobalVariable.IBEACON_TYPE_MAJOR);
                                        break;
                                    case GlobalVariable.IBEACON_TYPE_MINOR:
                                        // ??????minor
                                        mWriteCommand
                                                .sendIbeaconGetCommand(GlobalVariable.IBEACON_TYPE_MINOR);
                                        break;
                                    case GlobalVariable.IBEACON_TYPE_DEVICE_NAME:
                                        // //??????device name
                                        mWriteCommand
                                                .sendIbeaconGetCommand(GlobalVariable.IBEACON_TYPE_DEVICE_NAME);
                                        break;
                                    case GlobalVariable.IBEACON_TYPE_TX_POWER:
                                        // //??????TX_POWER
                                        mWriteCommand
                                                .sendIbeaconGetCommand(GlobalVariable.IBEACON_TYPE_TX_POWER);
                                        break;
                                    case GlobalVariable.IBEACON_TYPE_ADVERTISING_INTERVAL:
                                        // //??????advertising interval
                                        mWriteCommand
                                                .sendIbeaconGetCommand(GlobalVariable.IBEACON_TYPE_ADVERTISING_INTERVAL);
                                        break;
                                    default:
                                        break;
                                }
                                break;

                            default:
                                break;
                        }
                    } else {
                        Toast.makeText(mContext, "?????????ibeacon??????", Toast.LENGTH_SHORT)
                                .show();
                    }

                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.open_camera:
                if (ble_connecte) {
                    mWriteCommand.NotifyBLECameraOpenOrNot(true);
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.close_camera:
                if (ble_connecte) {
                    mWriteCommand.NotifyBLECameraOpenOrNot(false);
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.today_sports_time:
                if (ble_connecte) {
                    resultBuilder = new StringBuilder();
                    resultBuilder.append(getString(R.string.today_sports_time) + ":");
                    mWriteCommand.sendKeyToGetSportsTime(GlobalVariable.SPORTS_TIME_TODAY);
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.seven_days_sports_time:
                if (ble_connecte) {
                    resultBuilder = new StringBuilder();
                    resultBuilder.append(getString(R.string.seven_days_sports_time) + ":");
                    mWriteCommand.sendKeyToGetSportsTime(GlobalVariable.SPORTS_TIME_HISTORY_DAY);
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.universal_interface:
                if (ble_connecte) {
//                sdk???????????????BLE??????
                    String universalKeyInput = et_universal_interface.getText().toString();
                    if (TextUtils.isEmpty(universalKeyInput)) {
                        //???????????????????????????
                        boolean isSupportBigData = GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_UNIVERSAL_INTERFACE_BIG_DATA);
                        Toast.makeText(mContext, "??????????????????????????????????????????????????????????????? isSupportBigData =" + isSupportBigData, Toast.LENGTH_SHORT).show();
                        mWriteCommand.universalInterface(WriteCommandToBLE
                                .hexString2Bytes(universalKey), GlobalVariable.UNIVERSAL_INTERFACE_SDK_TO_BLE);
                    } else {
                        mWriteCommand.universalInterface(WriteCommandToBLE
                                .hexString2Bytes(universalKeyInput), GlobalVariable.UNIVERSAL_INTERFACE_SDK_TO_BLE);
                    }

                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.universal_interface_ble_sdk:
                if (ble_connecte) {
//                BLE?????????????????????sdk??????
                    String universalKeyInput = et_universal_interface.getText().toString();
                    if (TextUtils.isEmpty(universalKeyInput)) {
                        //???????????????????????????
                        boolean isSupportBigData = GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_UNIVERSAL_INTERFACE_BIG_DATA);
                        Toast.makeText(mContext, "??????????????????????????????????????????????????????????????? isSupportBigData =" + isSupportBigData, Toast.LENGTH_SHORT).show();
                        mWriteCommand.universalInterface(WriteCommandToBLE
                                .hexString2Bytes(universalKey), GlobalVariable.UNIVERSAL_INTERFACE_BLE_TO_SDK);
                    } else {
                        mWriteCommand.universalInterface(WriteCommandToBLE
                                .hexString2Bytes(universalKeyInput), GlobalVariable.UNIVERSAL_INTERFACE_BLE_TO_SDK);
                    }


                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.rate_calibration:
                if (ble_connecte) {
                    if (GetFunctionList.isSupportFunction_Second(mContext, GlobalVariable.IS_SUPPORT_HEART_RATE_DETECTION_CALIBRATION)) {
                        mWriteCommand.startRateCalibration();
                    } else {
                        Toast.makeText(mContext, "???????????????????????????",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.turn_wrist_calibration:
                if (ble_connecte) {
                    if (GetFunctionList.isSupportFunction_Second(mContext, GlobalVariable.IS_SUPPORT_TURN_WRIST_CALIBRATION)) {
                        mWriteCommand.startTurnWristCalibration();
                    } else {
                        Toast.makeText(mContext, "???????????????????????????",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.set_band_language:
                if (ble_connecte) {
                    if (GetFunctionList.isSupportFunction_Second(mContext, GlobalVariable.IS_SUPPORT_BAND_LANGUAGE_FUNCTION)) {
//                    BandLanguageUtil.BAND_LANGUAGE_SYSTEM //????????????????????????
//                    BandLanguageUtil.BAND_LANGUAGE_CN//????????????
//                    BandLanguageUtil.BAND_LANGUAGE_EN//??????
//                    BandLanguageUtil.BAND_LANGUAGE_KO//??????
//                    BandLanguageUtil.BAND_LANGUAGE_JA//??????
//                    BandLanguageUtil.BAND_LANGUAGE_DE//??????
//                    BandLanguageUtil.BAND_LANGUAGE_ES//????????????
//                    BandLanguageUtil.BAND_LANGUAGE_FR//??????
//                    BandLanguageUtil.BAND_LANGUAGE_IT//????????????
//                    BandLanguageUtil.BAND_LANGUAGE_PT//????????????
//                    BandLanguageUtil.BAND_LANGUAGE_AR//????????????
//                    BandLanguageUtil.BAND_LANGUAGE_HI//????????? ?????????????????????
//                    BandLanguageUtil.BAND_LANGUAGE_PL//?????????
//                    BandLanguageUtil.BAND_LANGUAGE_RU//??????
//                    BandLanguageUtil.BAND_LANGUAGE_NL//?????????
//                    BandLanguageUtil.BAND_LANGUAGE_TR //????????????
//                    BandLanguageUtil.BAND_LANGUAGE_IN //?????????????????????????????????
//                    BandLanguageUtil.BAND_LANGUAGE_CS//?????????
//                    BandLanguageUtil.BAND_LANGUAGE_HK//????????????
//                    BandLanguageUtil.BAND_LANGUAGE_IW//????????????
//                    BandLanguageUtil.BAND_LANGUAGE_SK//???????????????
//                    BandLanguageUtil.BAND_LANGUAGE_HU//????????????
//                    BandLanguageUtil.BAND_LANGUAGE_RO//???????????????
                        mWriteCommand.syncBandLanguage(BandLanguageUtil.BAND_LANGUAGE_SYSTEM);
//                        mWriteCommand.queryBraceletLanguage();
                    } else {
                        Toast.makeText(mContext, "?????????????????????????????????",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.settings_bracelet_interface_set:
                if (ble_connecte) {
                    boolean bandInterface = GetFunctionList.isSupportFunction_Second(mContext, GlobalVariable.IS_SUPPORT_CUSTOMIZED_BRACELET_INTERFACE);
                    LogUtils.i(TAG, "?????????????????? bandInterface = " + bandInterface);
                    if (bandInterface) {
                        startActivity(new Intent(mContext, BandInterfaceSetActivity.class));
                    } else {
                        Toast.makeText(mContext, "?????????????????????????????????",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.query_bracelet_model:
                String BTName = mWriteCommand.getBTName();
                if (BTName != null) {
                    show_result.setText(BTName);
                } else {
                    Toast.makeText(mContext, getString(R.string.get_version_first),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.query_customer_ID:
                if (ble_connecte) {
                    boolean isSupportCustomerID = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_QUERY_CUSTOMER_ID);
                    LogUtils.i(TAG, "????????????ID isSupportCustomerID = " + isSupportCustomerID);
                    if (isSupportCustomerID) {//???onResult??????
                        mWriteCommand.queryCustomerID();
                    } else {
                        Toast.makeText(mContext, "?????????????????????ID??????",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.query_currently_sport:
                if (ble_connecte) {
                    boolean isSupport = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_MULTIPLE_SPORTS_MODES_HEART_RATE);
                    LogUtils.i(TAG, "??????????????? isSupport = " + isSupport);
                    if (isSupport) {//???onResult??????
                        mWriteCommand.queryCurrentlySportOpened();
                    } else {
                        Toast.makeText(mContext, "????????????????????????????????????",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.set_currently_sport:
                if (ble_connecte) {
                    boolean isSupport = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_MULTIPLE_SPORTS_MODES_HEART_RATE);
                    LogUtils.i(TAG, "??????????????? isSupport = " + isSupport);
                    if (isSupport) {//???onResult??????
                        Random random = new Random();
                        boolean isOpen = random.nextBoolean();
                        int sportType = random.nextInt(0X18);

                        isOpen = true;
                        sportType = 1;

                        int N = 1;//??????N???10??????1byte?????????1~255?????????N=1???10s?????????????????????????????????????????????10s???20s???30s???1?????????2?????????3?????????4?????????5????????????
                        LogUtils.i(TAG, "??????????????? isOpen = " + isOpen + ",sportType =" + sportType + ",N =" + N);
                        mWriteCommand.setMultipleSportsModes(isOpen, sportType, N);
                    } else {
                        Toast.makeText(mContext, "????????????????????????????????????",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.control_sport:
                if (ble_connecte) {
                    boolean isSupport = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_MULTIPLE_SPORTS_MODES_HEART_RATE);
                    LogUtils.i(TAG, "??????????????? isSupport = " + isSupport);
                    if (isSupport) {
                        int switchStatus = GlobalVariable.MULTIPLE_SPORT_SWITCH_REAL_DATA;//?????????????????????GlobalVariable.MULTIPLE_SPORT_SWITCH_PAUSE???GlobalVariable.MULTIPLE_SPORT_SWITCH_RESUME???GlobalVariable.MULTIPLE_SPORT_SWITCH_REAL_DATA
                        int duration = 100;//???????????? ?????? ???
                        float distance = 1500; // ?????? ?????? ???
                        int calories = 10; //????????? ?????? kcal
                        float pace = 6.5f; //?????? ?????? ??????/?????????
                        boolean isKmType = true;//?????????????????????
                        boolean isGPSModes = true;// ?????????true??????????????? ??????+?????????+??????
                        SportsModesControlInfo info = new SportsModesControlInfo(switchStatus, duration, distance, calories, pace, isKmType, isGPSModes);
                        mWriteCommand.controlMultipleSportsModes(info);
                    }

                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sync_currently_sport:
                if (ble_connecte) {
                    boolean isSupport = GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_MULTIPLE_SPORTS_MODES_HEART_RATE);
                    LogUtils.i(TAG, "??????????????? isSupport = " + isSupport);
                    if (isSupport) {//???onResult??????
                        String calendar = "201903090819";//calendar ???201903090819 ????????????2019???03???09???08???19?????????????????????????????????????????????12
                        mWriteCommand.syncMultipleSportsModes(calendar);
                    } else {
                        Toast.makeText(mContext, "????????????????????????????????????",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.query_data:
                int sportsModes = new Random().nextInt(30);
                String calendar = CalendarUtils.getCalendar(-new Random().nextInt(5));
                List<SportsModesInfo> list = UTESQLOperate.getInstance(mContext).querySportsModes(null);
                MultipleSportsModesUtils.LLogI("saveSportsModesData ?????? list.size() =" + list.size());

                for (int i = 0; i < list.size(); i++) {
                    SportsModesInfo info = list.get(i);
                    MultipleSportsModesUtils.LLogI("saveSportsModesData ?????? SportsModes =" + info.getCurrentSportsModes() + ",StartDateTime =" + info.getStartDateTime() + ",BleTimeInterval = " + info.getBleTimeInterval());
                    byte[] a = GBUtils.getInstance(mContext).hexStringToBytes(info.getBleAllRate());
//                for (int j = 0; j < a.length; j++) {
//                    MultipleSportsModesUtils.LLogI("????????? =" + (a[j] & 0xFF));
//                }
                    int steps = info.getBleStepCount();
                    int cal = info.getBleSportsCalories();
                    float dis = info.getBleSportsDistance();
                    MultipleSportsModesUtils.LLogI("saveSportsModesData ?????? ??????=" + steps + ",?????????=" + cal + ",??????=" + dis);
                }
                break;
            case R.id.hrh_stop_sport:
                if (ble_connecte) {
                    boolean isSupport = GetFunctionList.isSupportFunction_Fourth(mContext, GlobalVariable.IS_SUPPORT_HEART_RATE_HEADSET);
                    LogUtils.i(TAG, "???????????? isSupport = " + isSupport);
                    if (isSupport) {
                        mWriteCommand.stopHRHSportMode();
                    } else {
                        Toast.makeText(mContext, "???????????????",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.hrh_start_sport:
                if (ble_connecte) {
                    boolean isSupport = GetFunctionList.isSupportFunction_Fourth(mContext, GlobalVariable.IS_SUPPORT_HEART_RATE_HEADSET);
                    LogUtils.i(TAG, "???????????? isSupport = " + isSupport);
                    if (isSupport) {
                        mWriteCommand.startHRHSportMode(GlobalVariable.MULTIPLE_SPORTS_MODES_RUNNING);
                    } else {
                        Toast.makeText(mContext, "???????????????",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.hrh_set_interval:
                if (ble_connecte) {
                    boolean isSupport = GetFunctionList.isSupportFunction_Fourth(mContext, GlobalVariable.IS_SUPPORT_HEART_RATE_HEADSET);
                    LogUtils.i(TAG, "???????????? isSupport = " + isSupport);
                    if (isSupport) {
                        mWriteCommand.setHRHRateInterval(1);
                    } else {
                        Toast.makeText(mContext, "???????????????",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.hrh_query_status:
                if (ble_connecte) {
                    boolean isSupport = GetFunctionList.isSupportFunction_Fourth(mContext, GlobalVariable.IS_SUPPORT_HEART_RATE_HEADSET);
                    LogUtils.i(TAG, "???????????? isSupport = " + isSupport);
                    if (isSupport) {
                        mWriteCommand.queryHRHSportStatus();
                    } else {
                        Toast.makeText(mContext, "???????????????",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.dial_setting://????????????????????????????????????
                if (GetFunctionList.isSupportFunction_Third(mContext, GlobalVariable.IS_SUPPORT_ONLINE_DIAL)) {
                    new RxPermissions(MainActivity.this).request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean granted) {
                            if (granted) {
                                startActivity(new Intent(mContext, OnlineDialActivity.class));
                            }
                        }
                    });
                } else {
                    Toast.makeText(mContext, getString(R.string.not_support_online_dial),
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.body_temperature://????????????????????????????????????
                if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_TEMPERATURE_TEST)) {
//                public static final int QUERY_CURRENT_TEMPERATURE_COMMAND_OK = 131;//????????????????????????????????????
//                public static final int SYNC_TEMPERATURE_TIME_PERIOD_COMMAND_OK = 132;//????????????????????????
//                public static final int SYNC_TEMPERATURE_AUTOMATICTEST_INTERVAL_COMMAND_OK = 133;//???????????????????????????????????????
//                public static final int DELETE_TEMPERATURE_HISTORICAL_DATA_COMMAND_OK = 134;//??????????????????
//                public static final int SYNC_MAX_MIN_ALARM_TEMPERATURE_COMMAND_OK = 135;//??????????????????????????????
//                public static final int SYNC_TEMPERATURE_COMMAND_OK = 136;//??????????????????OK
//                public static final int TEMPERATURE_DATA_SYNCING = 137;//???????????????
//                public static final int SYNC_TEMPERATURE_COMMAND_FINISH_CRC_FAIL = 138;//????????????,???????????????????????????????????????????????????
//
//                mWriteCommand.syncAllTemperatureData();//??????????????????
                    mWriteCommand.queryCurrentTemperatureData();//????????????????????????????????????
//                mWriteCommand.syncTemperatureAutomaticTestInterval(false,0);//??????????????????????????????????????????????????????
//                mWriteCommand.syncTemperatureTimePeriod(true,111,111);//????????????????????????????????????????????????????????????????????????
//                mWriteCommand.deleteTemperatureHistoricalData();//???????????????????????????
//                mWriteCommand.syncMaxMinAlarmTemperature(true,40.0f,35.1f);//??????????????????????????????
//                mWriteCommand.calibrationTemperature();//????????????(?????????????????????????????????????????????,IS_SUPPORT_TEMPERATURE_CALIBRATION)
//                mWriteCommand.setRawTemperatureStatus(true);//????????????????????????????????????
//                mWriteCommand.queryRawTemperatureStatus();//?????????????????????????????????????????????0???????????????1????????????
                } else {
                    Toast.makeText(mContext, getString(R.string.not_support_body_temperature),
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.start_test_oxygen:
                if (ble_connecte) {
                    if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_OXYGEN)) {
                        //???????????????????????????????????????????????????????????????????????????????????????false.
                        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????false.
                        mWriteCommand.startOxygenTest();//onTestResult
                    } else {
                        Toast.makeText(mContext, "No support",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.stop_test_oxygen:
                if (ble_connecte) {
                    if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_OXYGEN)) {
                        mWriteCommand.stopOxygenTest();//onTestResult
                    } else {
                        Toast.makeText(mContext, "No support",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sync_oxygen_data:
                if (ble_connecte) {
                    if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_OXYGEN)) {
                        mWriteCommand.syncOxygenData();//OnResult(true, ICallbackStatus.OXYGEN_DATA_SYNCING) and OnResult(true, ICallbackStatus.SYNC_OXYGEN_COMMAND_OK)
                    } else {
                        Toast.makeText(mContext, "No support",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.query_oxygen_data:
                if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_OXYGEN)) {
                    List<OxygenInfo> list1 = UTESQLOperate.getInstance(mContext).queryOxygenAll();//Query all oxygen data
//              List<OxygenInfo> list1 =  UTESQLOperate.getInstance(mContext).queryOxygenAll(OxygenUtil.ORDER_BY_ASC);//Query all oxygen data,order by asc
//              List<OxygenInfo> list1 =  UTESQLOperate.getInstance(mContext).queryOxygenAll(OxygenUtil.ORDER_BY_DESC);////Query all oxygen data,order by desc
//              List<OxygenInfo> list1 =  UTESQLOperate.getInstance(mContext).queryOxygenDate(CalendarUtils.getCalendar(0));//Query one day oxygen data
//              List<OxygenInfo> list1 =  UTESQLOperate.getInstance(mContext).queryOxygenDate(CalendarUtils.getCalendar(0),OxygenUtil.ORDER_BY_ASC);//Query one day oxygen data,order by asc/desc
                    for (int i = 0; i < list1.size(); i++) {
                        list1.get(i).getCalendar();
                        list1.get(i).getStartDate();
                        list1.get(i).getTime();
                        list1.get(i).getOxygenValue();
                    }
                } else {
                    Toast.makeText(mContext, "No support",
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.start_test_breathe:
                if (ble_connecte) {
                    if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_BREATHE)) {
                        mWriteCommand.startBreatheTest();//
                    } else {
                        Toast.makeText(mContext, "No support",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.stop_test_breathe:
                if (ble_connecte) {
                    if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_BREATHE)) {
                        mWriteCommand.stopBreatheTest();//
                    } else {
                        Toast.makeText(mContext, "No support",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sync_breathe_data:
                if (ble_connecte) {
                    if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_BREATHE)) {
                        mWriteCommand.syncBreatheData();//OnResult(true, ICallbackStatus.BREATHE_DATA_SYNCING) and OnResult(true, ICallbackStatus.SYNC_BREATHE_COMMAND_OK)
                    } else {
                        Toast.makeText(mContext, "No support",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.query_breathe_data:
                if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_BREATHE)) {
                    List<BreatheInfo> list1 = UTESQLOperate.getInstance(mContext).queryBreatheAll();//Query all breathe data
//              List<BreatheInfo> list1 =  UTESQLOperate.getInstance(mContext).queryBreatheAll(BreatheUtil.ORDER_BY_ASC);//Query all breathe data,order by asc
//              List<BreatheInfo> list1 =  UTESQLOperate.getInstance(mContext).queryBreatheAll(BreatheUtil.ORDER_BY_DESC);////Query all breathe data,order by desc
//              List<BreatheInfo> list1 =  UTESQLOperate.getInstance(mContext).queryBreatheDate(CalendarUtils.getCalendar(0));//Query one day breathe data
//              List<BreatheInfo> list1 =  UTESQLOperate.getInstance(mContext).queryBreatheDate(CalendarUtils.getCalendar(0),BreatheUtil.ORDER_BY_ASC);//Query one day breathe data,order by asc/desc
                    for (int i = 0; i < list1.size(); i++) {
                        list1.get(i).getCalendar();
                        list1.get(i).getStartDate();
                        list1.get(i).getTime();
                        list1.get(i).getBreatheValue();
                    }
                    BreatheUtil.LogI("queryBreatheAll =" + new Gson().toJson(list1));
                } else {
                    Toast.makeText(mContext, "No support",
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.query_breathe_test_status:
                if (ble_connecte) {
                    if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_BREATHE)) {
                        mWriteCommand.queryBreatheTestStatus();
                    } else {
                        Toast.makeText(mContext, "No support",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.breathe_automatic_test:
                if (ble_connecte) {
                    if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_BREATHE)) {
                        //???????????????true false???????????????????????????????????????????????????
                        //????????????????????????????????????????????????10?????????10?????????????????????????????????????????????????????????1?????????5?????????10?????????30?????????1?????????2?????????3?????????4?????????6?????????8?????????12?????????24?????????
                        // ????????????8??????????????????8??????14??????20????????????12??????????????????8??????20????????????24??????????????????8????????????
                        mWriteCommand.syncBreatheAutomaticTest(true, 10);
                    } else {
                        Toast.makeText(mContext, "No support",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.breathe_time_period:
                if (ble_connecte) {
                    if (GetFunctionList.isSupportFunction_Fifth(mContext, GlobalVariable.IS_SUPPORT_BREATHE)) {
                        //??????????????? ?????????????????????????????????
                        //?????????????????????????????????????????????680????????????????????? 11:20 ???Hour = startTime / 60,Minute = startTime % 60???
                        //???????????????????????????????????????.
                        mWriteCommand.syncBreatheTimePeriod(true, 530, 1225);
                    } else {
                        Toast.makeText(mContext, "No support",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.disconnect),
                            Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (CURRENT_STATUS == CONNECTING) {
            AlertDialog.Builder builder = new Builder(this);
            builder.setMessage("????????????????????????????????????????????????????????????");
            builder.setTitle(mContext.getResources().getString(R.string.tip));
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
                                    .getDefaultAdapter();
                            if (mBluetoothAdapter == null) {
                                finish();
                            }
                            if (mBluetoothAdapter.isEnabled()) {
                                mBluetoothAdapter.disable();// ????????????
                            }
                            finish();
                        }
                    });
            builder.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean updateBleDialog() {

        final AlertDialog alert = new AlertDialog.Builder(this).setCancelable(
                false).create();
        alert.show();
        window = alert.getWindow();
        window.setContentView(R.layout.update_dialog_layout);
        Button btn_yes = (Button) window.findViewById(R.id.btn_yes);
        Button btn_no = (Button) window.findViewById(R.id.btn_no);
        TextView update_warn_tv = (TextView) window
                .findViewById(R.id.update_warn_tv);
        update_warn_tv.setText(getResources().getString(
                R.string.find_new_version_ble));

        btn_yes.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isNetworkAvailable(mContext)) {
                    mUpdates.startUpdateBLE();
                } else {
                    Toast.makeText(
                            mContext,
                            getResources().getString(
                                    R.string.confire_is_network_available), Toast.LENGTH_SHORT)
                            .show();
                }

                alert.dismiss();
            }
        });
        btn_no.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mUpdates.clearUpdateSetting();
                alert.dismiss();
            }

        });
        return false;

    }

    /**
     * ?????????????????????????????????????????????UI CalendarUtils.getCalendar(0)???????????????????????????"20141101"
     * CalendarUtils.getCalendar(-1)???????????????????????????"20141031"
     * CalendarUtils.getCalendar(-2)???????????????????????????"20141030" ????????????
     */
    private void querySleepInfo() {
        SleepTimeInfo sleepTimeInfo = UTESQLOperate.getInstance(mContext).querySleepInfo(CalendarUtils.getCalendar(0));
        int deepTime, lightTime, awakeCount, sleepTotalTime;
        if (sleepTimeInfo != null) {
            deepTime = sleepTimeInfo.getDeepTime();
            lightTime = sleepTimeInfo.getLightTime();
            awakeCount = sleepTimeInfo.getAwakeCount();
            sleepTotalTime = sleepTimeInfo.getSleepTotalTime();

            int[] colorArray = sleepTimeInfo.getSleepStatueArray();// ?????????????????????????????????????????????????????????????????????
            int[] timeArray = sleepTimeInfo.getDurationTimeArray();
            int[] timePointArray = sleepTimeInfo.getTimePointArray();

            LogUtils.d(TAG, "Calendar=" + CalendarUtils.getCalendar(0)
                    + ",timeArray =" + timeArray + ",timeArray.length ="
                    + timeArray.length + ",colorArray =" + colorArray
                    + ",colorArray.length =" + colorArray.length
                    + ",timePointArray =" + timePointArray
                    + ",timePointArray.length =" + timePointArray.length);

            double total_hour = ((float) sleepTotalTime / 60f);
            DecimalFormat df1 = new DecimalFormat("0.0"); // ??????1????????????????????????

            int deep_hour = deepTime / 60;
            int deep_minute = (deepTime - deep_hour * 60);
            int light_hour = lightTime / 60;
            int light_minute = (lightTime - light_hour * 60);
            int active_count = awakeCount;
            String total_hour_str = df1.format(total_hour);

            if (total_hour_str.equals("0.0")) {
                total_hour_str = "0";
            }
            tv_sleep.setText(total_hour_str);
            tv_deep.setText(deep_hour + " "
                    + mContext.getResources().getString(R.string.hour) + " "
                    + deep_minute + " "
                    + mContext.getResources().getString(R.string.minute));
            tv_light.setText(light_hour + " "
                    + mContext.getResources().getString(R.string.hour) + " "
                    + light_minute + " "
                    + mContext.getResources().getString(R.string.minute));
            tv_awake.setText(active_count + " "
                    + mContext.getResources().getString(R.string.count));
        } else {
            LogUtils.d(TAG, "sleepTimeInfo =" + sleepTimeInfo);
            tv_sleep.setText("0");
            tv_deep.setText(mContext.getResources().getString(
                    R.string.zero_hour_zero_minute));
            tv_light.setText(mContext.getResources().getString(
                    R.string.zero_hour_zero_minute));
            tv_awake.setText(mContext.getResources().getString(
                    R.string.zero_count));
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(GlobalVariable.READ_BLE_VERSION_ACTION)) {
                String version = intent
                        .getStringExtra(GlobalVariable.INTENT_BLE_VERSION_EXTRA);
                if (SPUtil.getInstance(mContext).getRKPlatform()) {
                    show_result.setText("version="
                            + version
                            + ","
                            + SPUtil.getInstance(mContext).getPathLocalVersion());
                } else {
                    show_result.setText("version=" + version);
                }

            } else if (action.equals(GlobalVariable.READ_BATTERY_ACTION)) {
                int battery = intent.getIntExtra(
                        GlobalVariable.INTENT_BLE_BATTERY_EXTRA, -1);
                show_result.setText("battery=" + battery);
                SPUtil.getInstance(MainActivity.this).setBleBatteryValue(battery);

            }
        }
    };
    private Window window;

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
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
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "MainActivity_onDestroy");
        GlobalVariable.BLE_UPDATE = false;
        mUpdates.unRegisterBroadcastReceiver();
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            // TODO: handle exception
        }

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (mDialogRunnable != null)
            mHandler.removeCallbacks(mDialogRunnable);

        mBLEServiceOperate.disConnect();
        mBluetoothLeService.ClearGattForDisConnect();
        mBLEServiceOperate.unBindService();
        System.exit(0);
        finish();

    }

    @Override
    public void OnResult(boolean result, int status) {
        // TODO Auto-generated method stub
        LogUtils.i(TAG, "result=" + result + ",status=" + status);
        switch (status) {
            case ICallbackStatus.OFFLINE_STEP_SYNC_OK:
                mHandler.sendEmptyMessage(OFFLINE_STEP_SYNC_OK_MSG);
                break;
            case ICallbackStatus.OFFLINE_SLEEP_SYNC_OK:
                break;
            case ICallbackStatus.SYNC_TIME_OK:// (??????????????????SDK?????????????????????????????????????????????????????????sdk????????????????????????????????????????????????)
                //???????????????????????????????????????????????????20???????????????????????????
                // delay 20ms  send
                // to read
                // localBleVersion
                // mWriteCommand.sendToReadBLEVersion();
                break;
            case ICallbackStatus.GET_BLE_VERSION_OK:// ??????????????????????????????????????????????????????20????????????????????????????????????
                // localBleVersion
                // finish,
                // then sync
                // step
                // mWriteCommand.syncAllStepData();
                break;
            case ICallbackStatus.DISCONNECT_STATUS:
                mHandler.sendEmptyMessage(DISCONNECT_MSG);
                break;
            case ICallbackStatus.CONNECTED_STATUS:
                mHandler.sendEmptyMessage(CONNECTED_MSG);
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mWriteCommand.sendToQueryPasswardStatus();
                    }
                }, 600);// 2.2.1????????????

                break;

            case ICallbackStatus.DISCOVERY_DEVICE_SHAKE:
                LogUtils.d(TAG, "???????????????");
                // Discovery device Shake
                break;
            case ICallbackStatus.OFFLINE_RATE_SYNC_OK:
                mHandler.sendEmptyMessage(RATE_SYNC_FINISH_MSG);
                break;
            case ICallbackStatus.OFFLINE_24_HOUR_RATE_SYNC_OK:
                mHandler.sendEmptyMessage(RATE_OF_24_HOUR_SYNC_FINISH_MSG);
                break;
            case ICallbackStatus.SET_METRICE_OK: // ????????????????????????
                break;
            case ICallbackStatus.SET_INCH_OK: //// ????????????????????????
                break;
            case ICallbackStatus.SET_FIRST_ALARM_CLOCK_OK: // ?????????1?????????OK
                break;
            case ICallbackStatus.SET_SECOND_ALARM_CLOCK_OK: //?????????2?????????OK
                break;
            case ICallbackStatus.SET_THIRD_ALARM_CLOCK_OK: // ?????????3?????????OK
                break;
            case ICallbackStatus.SEND_PHONE_NAME_NUMBER_OK:
                mWriteCommand.sendQQWeChatVibrationCommand(5);
                break;
            case ICallbackStatus.SEND_QQ_WHAT_SMS_CONTENT_OK:
                mWriteCommand.sendQQWeChatVibrationCommand(1);
                break;
            case ICallbackStatus.PASSWORD_SET:
                LogUtils.d(TAG, "??????????????????????????????4???????????????");
                mHandler.sendEmptyMessage(SHOW_SET_PASSWORD_MSG);
                break;
            case ICallbackStatus.PASSWORD_INPUT:
                LogUtils.d(TAG, "??????????????????????????????????????????4???????????????");
                mHandler.sendEmptyMessage(SHOW_INPUT_PASSWORD_MSG);
                break;
            case ICallbackStatus.PASSWORD_AUTHENTICATION_OK:
                LogUtils.d(TAG, "????????????????????????????????????");
                break;
            case ICallbackStatus.PASSWORD_INPUT_AGAIN:
                LogUtils.d(TAG, "??????????????????????????????????????????????????????4????????????????????????????????????????????????????????????????????????");
                mHandler.sendEmptyMessage(SHOW_INPUT_PASSWORD_AGAIN_MSG);
                break;
            case ICallbackStatus.OFFLINE_SWIM_SYNCING:
                LogUtils.d(TAG, "?????????????????????");
                break;
            case ICallbackStatus.OFFLINE_SWIM_SYNC_OK:
                LogUtils.d(TAG, "????????????????????????");
                mHandler.sendEmptyMessage(OFFLINE_SWIM_SYNC_OK_MSG);
                break;
            case ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNCING:
                LogUtils.d(TAG, "?????????????????????");
                break;
            case ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNC_OK:
                LogUtils.d(TAG, "????????????????????????");
                mHandler.sendEmptyMessage(OFFLINE_BLOOD_PRESSURE_SYNC_OK_MSG);
                break;
            case ICallbackStatus.OFFLINE_SKIP_SYNCING:
                LogUtils.d(TAG, "?????????????????????");
                break;
            case ICallbackStatus.OFFLINE_SKIP_SYNC_OK:
                LogUtils.d(TAG, "????????????????????????");
                mHandler.sendEmptyMessage(OFFLINE_SKIP_SYNC_OK_MSG);
                break;
            case ICallbackStatus.MUSIC_PLAYER_START_OR_STOP:
                LogUtils.d(TAG, "????????????/??????");
                break;
            case ICallbackStatus.MUSIC_PLAYER_NEXT_SONG:
                LogUtils.d(TAG, "???????????????");
                break;
            case ICallbackStatus.MUSIC_PLAYER_LAST_SONG:
                LogUtils.d(TAG, "???????????????");
                break;
            case ICallbackStatus.OPEN_CAMERA_OK:
                LogUtils.d(TAG, "????????????ok");
                break;
            case ICallbackStatus.CLOSE_CAMERA_OK:
                LogUtils.d(TAG, "????????????ok");
                break;
            case ICallbackStatus.PRESS_SWITCH_SCREEN_BUTTON:
                LogUtils.d(TAG, "????????????1??????????????????????????????,???????????????????????????");
                mHandler.sendEmptyMessage(test_mag1);
                break;
            case ICallbackStatus.PRESS_END_CALL_BUTTON:
                LogUtils.d(TAG, "????????????1??????????????????????????????");
                break;
            case ICallbackStatus.PRESS_TAKE_PICTURE_BUTTON:
                LogUtils.d(TAG, "????????????2?????????????????????????????????");
                break;
            case ICallbackStatus.PRESS_SOS_BUTTON:
                LogUtils.d(TAG, "????????????3???????????????????????????SOS");
                mHandler.sendEmptyMessage(test_mag2);
                break;
            case ICallbackStatus.PRESS_FIND_PHONE_BUTTON:
                LogUtils.d(TAG, "???????????????????????????????????????????????????");

                break;
            case ICallbackStatus.READ_ONCE_AIR_PRESSURE_TEMPERATURE_SUCCESS:
                LogUtils.d(TAG, "??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
                break;
            case ICallbackStatus.SYNC_HISORY_AIR_PRESSURE_TEMPERATURE_SUCCESS:
                LogUtils.d(TAG, "????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
                break;
            case ICallbackStatus.SYNC_HISORY_AIR_PRESSURE_TEMPERATURE_FAIL:
                LogUtils.d(TAG, "????????????????????????????????????????????????");
                break;
            case ICallbackStatus.START_BREATHE_COMMAND_OK:
                LogUtils.d(TAG, "?????????????????????");
                BreatheUtil.LogI("?????????????????????");
                break;
            case ICallbackStatus.STOP_BREATHE_COMMAND_OK:
                BreatheUtil.LogI("?????????????????????");
                break;
            case ICallbackStatus.QUERY_CURRENT_BREATHE_COMMAND_OK:
                BreatheUtil.LogI("?????????????????????????????????");
                break;
            case ICallbackStatus.BREATHE_DATA_SYNCING:
                BreatheUtil.LogI("????????????????????????");
                break;
            case ICallbackStatus.SYNC_BREATHE_COMMAND_OK:
                BreatheUtil.LogI("???????????????????????????");
                break;
            case ICallbackStatus.SET_BREATHE_AUTOMATIC_TEST_COMMAND_OK:
                BreatheUtil.LogI("?????????????????????????????????");
                break;
            case ICallbackStatus.SYNC_BREATHE_TIME_PERIOD_COMMAND_OK:
                BreatheUtil.LogI("?????????????????????????????????");
                break;
            case ICallbackStatus.READ_CHAR_SUCCESS:
                //???????????????OK ?????????????????? ???????????? GetFunctionList ??????????????????
                break;
            case ICallbackStatus.BIND_CONNECT_SEND_ACCOUNT_ID:
                LogUtils.d(TAG, "????????????ID");
                mHandler.sendEmptyMessage(BIND_CONNECT_SEND_ACCOUNT_ID_MSG);
                break;
            case ICallbackStatus.BIND_CONNECT_COMPARE_SUCCESS:
                LogUtils.d(TAG, "????????????");
                break;
            case ICallbackStatus.BIND_CONNECT_BAND_CLICK_CONFIRM:
                LogUtils.d(TAG, "???????????? ?????? ??????");
                break;
            case ICallbackStatus.BIND_CONNECT_VALID_ID:
                LogUtils.d(TAG, "????????????????????????ID");
                break;
            case ICallbackStatus.BIND_CONNECT_IDVALID_ID:
                LogUtils.d(TAG, "?????????????????????ID");
                break;
            case ICallbackStatus.BIND_CONNECT_BAND_CLICK_CANCEL:
                LogUtils.d(TAG, "???????????? ?????? ??????");
                break;

            default:
                break;
        }
    }

    private final String testKey1 = "00a4040008A000000333010101000003330101010000333010101000033301010100003330101010000033301010100333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100a4040008A0000003330101010000033301010100003330101010000333010101000033301010100000333010101003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010101";
    private final String universalKey = "A6040008A0000040008A00000033301010100000333010101000033301010100003330101010000333010101000003330100333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010040008A000000333010101000003330101010000333010101000033301010100003330101010000033301003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100003330100033301010100000333010101000033301010100003330101010000333010101000003330100333010101000033301010100003330101010000333010101000033301010100003330101010000333010101000033301010100003330101010000333010";
//	private final String universalKey = "1102";

    @Override
    public void OnDataResult(boolean result, int status, byte[] data) {
        StringBuilder stringBuilder = null;
        if (data != null && data.length > 0) {
            stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) {
                stringBuilder.append(String.format("%02X", byteChar));
            }
            LogUtils.i("sendTextKey", "BLE---->APK data =" + stringBuilder.toString());
        }
//		if (status == ICallbackStatus.OPEN_CHANNEL_OK) {// ????????????OK
//			mHandler.sendEmptyMessage(OPEN_CHANNEL_OK_MSG);
//		} else if (status == ICallbackStatus.CLOSE_CHANNEL_OK) {// ????????????OK
//			mHandler.sendEmptyMessage(CLOSE_CHANNEL_OK_MSG);
//		} else if (status == ICallbackStatus.BLE_DATA_BACK_OK) {// ????????????OK???????????????
//			mHandler.sendEmptyMessage(TEST_CHANNEL_OK_MSG);
//		}
        switch (status) {
            case ICallbackStatus.OPEN_CHANNEL_OK:// ????????????OK
                mHandler.sendEmptyMessage(OPEN_CHANNEL_OK_MSG);
                break;
            case ICallbackStatus.CLOSE_CHANNEL_OK:// ????????????OK
                mHandler.sendEmptyMessage(CLOSE_CHANNEL_OK_MSG);
                break;
            case ICallbackStatus.BLE_DATA_BACK_OK:// ????????????OK???????????????
                mHandler.sendEmptyMessage(TEST_CHANNEL_OK_MSG);
                break;
            //========?????????????????? Universal Interface   start====================
            case ICallbackStatus.UNIVERSAL_INTERFACE_SDK_TO_BLE_SUCCESS://sdk???????????????ble??????????????????????????????????????????
                mHandler.sendEmptyMessage(UNIVERSAL_INTERFACE_SDK_TO_BLE_SUCCESS_MSG);
                break;
            case ICallbackStatus.UNIVERSAL_INTERFACE_SDK_TO_BLE_FAIL://sdk???????????????ble??????????????????????????????????????????
                mHandler.sendEmptyMessage(UNIVERSAL_INTERFACE_SDK_TO_BLE_FAIL_MSG);
                break;
            case ICallbackStatus.UNIVERSAL_INTERFACE_BLE_TO_SDK_SUCCESS://ble???????????????sdk??????????????????????????????????????????
                mHandler.sendEmptyMessage(UNIVERSAL_INTERFACE_BLE_TO_SDK_SUCCESS_MSG);
                break;
            case ICallbackStatus.UNIVERSAL_INTERFACE_BLE_TO_SDK_FAIL://ble???????????????sdk??????????????????????????????????????????
                mHandler.sendEmptyMessage(UNIVERSAL_INTERFACE_BLE_TO_SDK_SUCCESS_MSG);
                break;
            //========?????????????????? Universal Interface   end====================
            case ICallbackStatus.CUSTOMER_ID_OK://?????? ??????id
                if (result) {
                    LogUtils.d(TAG, "??????ID = " + GBUtils.getInstance(mContext).customerIDAsciiByteToString(data));
                }

                break;
            case ICallbackStatus.DO_NOT_DISTURB_CLOSE://?????? ??????????????????
                if (data != null && data.length >= 2) {
                    LogUtils.d(TAG, "?????????????????????");
                    //???data[1]????????????????????????data[1] =10?????????????????????1010??????B3???1?????????????????????????????????????????????B2???0???????????????????????????????????????B1???1??????????????????????????????????????????B0???0??????????????????????????????????????????
//                    ???1???0
//                    B3	              B2	     B1	     B0
//                    ????????????????????????	????????????	  ????????????	????????????????????????
                }
                break;
            case ICallbackStatus.DO_NOT_DISTURB_OPEN://?????? ??????????????????
                if (data != null && data.length >= 2) {
                    LogUtils.d(TAG, "?????????????????????");
                    //???data[1]????????????????????????data[1] =10?????????????????????1010??????B3???1?????????????????????????????????????????????B2???0???????????????????????????????????????B1???1??????????????????????????????????????????B0???0??????????????????????????????????????????
//                    ???1???0
//                    B3	              B2	     B1	     B0
//                    ????????????????????????	????????????	  ????????????	????????????????????????
//
                }
                break;
            case ICallbackStatus.QUICK_SWITCH_SURPPORT_COMMAND_OK://?????? APP?????????????????????????????????????????????????????????
                LogUtils.d(TAG, "APP?????????????????????????????????????????????????????????");
                //???data????????????,????????????queryQuickSwitchSupList????????????
                break;
            case ICallbackStatus.QUICK_SWITCH_STATUS_COMMAND_OK://?????? APP??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                LogUtils.d(TAG, "APP??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
                //???data???????????????????????????queryQuickSwitchSupListStatus????????????
                break;
            default:
                break;
        }

    }

    @Override
    public void onCharacteristicWriteCallback(int status) {// add 20170221
        // ??????????????????????????????status = 0????????????????????????????????????????????????
        LogUtils.d(TAG, "Write System callback status = " + status);
    }

    @Override
    public void OnServerCallback(int status, String description) {
        LogUtils.i(TAG, "??????????????? OnServerCallback status =" + status);
        if (status == GlobalVariable.SERVER_CALL_BACK_SUCCESSFULL) {//???????????????OK
            mHandler.sendEmptyMessage(SERVER_CALL_BACK_OK_MSG);
        } else {//?????????????????????
            mHandler.sendEmptyMessage(GlobalVariable.SERVER_IS_BUSY_MSG);
        }
    }

    @Override
    public void OnServiceStatuslt(int status) {
        if (status == ICallbackStatus.BLE_SERVICE_START_OK) {
            LogUtils.d(TAG, "OnServiceStatuslt mBluetoothLeService11 =" + mBluetoothLeService);
            if (mBluetoothLeService == null) {
                mBluetoothLeService = mBLEServiceOperate.getBleService();
                mBluetoothLeService.setICallback(this);
                mBluetoothLeService.setRateCalibrationListener(this);//????????????????????????
                mBluetoothLeService.setTurnWristCalibrationListener(this);//????????????????????????
                mBluetoothLeService.setTemperatureListener(this);//???????????????????????????????????????
                mBluetoothLeService.setOxygenListener(this);//Oxygen Listener
                mBluetoothLeService.setBreatheRealListener(this);//Breathe Listener
                LogUtils.d(TAG, "OnServiceStatuslt mBluetoothLeService22 =" + mBluetoothLeService);
            }
        }
    }

    private static final int SHOW_SET_PASSWORD_MSG = 26;
    private static final int SHOW_INPUT_PASSWORD_MSG = 27;
    private static final int SHOW_INPUT_PASSWORD_AGAIN_MSG = 28;

    private boolean isPasswordDialogShowing = false;
    private String password = "";

    private void showPasswordDialog(final int type) {
        LogUtils.d(TAG, "showPasswordDialog");
        if (isPasswordDialogShowing) {
            LogUtils.d(TAG, "?????????????????????");
            return;
        }
        CustomPasswordDialog.Builder builder = new CustomPasswordDialog.Builder(
                MainActivity.this, mTextWatcher);
        builder.setPositiveButton(getResources().getString(R.string.confirm),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (password.length() == 4) {
                            LogUtils.d("CustomPasswordDialog", "?????????4???  password =" + password);
                            dialog.dismiss();
                            isPasswordDialogShowing = false;

                            mWriteCommand.sendToSetOrInputPassward(password,
                                    type);
                        }
                    }
                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        isPasswordDialogShowing = false;
                    }
                });
        builder.create().show();

        if (type == GlobalVariable.PASSWORD_TYPE_SET) {
            builder.setTittle(mContext.getResources().getString(
                    R.string.set_password_for_band));
        } else if (type == GlobalVariable.PASSWORD_TYPE_INPUT_AGAIN) {
            builder.setTittle(mContext.getResources().getString(
                    R.string.input_password_for_band_again));
        } else {
            builder.setTittle(mContext.getResources().getString(
                    R.string.input_password_for_band));
        }
        isPasswordDialogShowing = true;
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            password = s.toString();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
        }
    };

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    /**
     * ????????????????????????
     *
     * @return
     */
    private boolean isEnabled() {
        String pkgName = getPackageName();
        LogUtils.w(TAG, "---->pkgName = " + pkgName);
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName
                        .unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void upDateTodaySwimData() {
        // TODO Auto-generated method stub
//		SwimInfo mSwimInfo = mySQLOperate.querySwimData(CalendarUtils
//				.getCalendar(0));// ???????????????0????????????-1????????????-2?????????????????????
        List<SwimDayInfo> list = mySQLOperate.querySwimDayInfo(CalendarUtils
                .getCalendar(0));// ???????????????0????????????-1????????????-2?????????????????????
        if (list != null) {
            SwimDayInfo mSwimInfo = null;
            for (int i = 0; i < list.size(); i++) {
                mSwimInfo = list.get(i);
                if (mSwimInfo != null) {
                    swim_time.setText(mSwimInfo.getUseTime() + "");
                    swim_stroke_count.setText(mSwimInfo.getCount() + "");
                    swim_calorie.setText(mSwimInfo.getCalories() + "");
                }

            }

        }
    }

    ;

    /*
     * ???????????????????????????????????????????????????????????????
     */
    private void UpdateBloodPressureMainUI(String calendar) {
        // UTESQLOperate mySQLOperate = new UTESQLOperate(mContext);
        List<BPVOneDayInfo> mBPVOneDayListInfo = mySQLOperate
                .queryBloodPressureOneDayInfo(calendar);
        if (mBPVOneDayListInfo != null) {
            int highPressure = 0;
            int lowPressure = 0;
            int time = 0;
            for (int i = 0; i < mBPVOneDayListInfo.size(); i++) {
                highPressure = mBPVOneDayListInfo.get(i)
                        .getHightBloodPressure();
                lowPressure = mBPVOneDayListInfo.get(i).getLowBloodPressure();
                time = mBPVOneDayListInfo.get(i).getBloodPressureTime();
            }
            LogUtils.d(TAG, "highPressure =" + highPressure
                    + ",lowPressure =" + lowPressure);
            // current_rate.setText(currentRate + "");
            if (highPressure == 0) {
                tv_high_pressure.setText("--");

            } else {
                tv_high_pressure.setText(highPressure + "");

            }
            if (lowPressure == 0) {
                tv_low_pressure.setText("--");
            } else {
                tv_low_pressure.setText(lowPressure + "");
            }

        } else {
            tv_high_pressure.setText("--");
            tv_low_pressure.setText("--");

        }
    }

    private void initIbeacon() {
        // TODO Auto-generated method stub
        ibeacon_command = (Button) findViewById(R.id.ibeacon_command);
        ibeacon_command.setOnClickListener(this);
        ibeaconStatusSpinner = (Spinner) findViewById(R.id.ibeacon_status);
        setOrReadSpinner = (Spinner) findViewById(R.id.SetOrReadSpinner);
        ibeaconStatusSpinnerList.add("UUID");
        ibeaconStatusSpinnerList.add("major");
        ibeaconStatusSpinnerList.add("minor");
        ibeaconStatusSpinnerList.add("device name");
        ibeaconStatusSpinnerList.add("TX power");
        ibeaconStatusSpinnerList.add("advertising interval");
//		ibeaconStatusSpinnerList.add("??????");
//		ibeaconStatusSpinnerList.add("????????????");
//		ibeaconStatusSpinnerList.add("????????????");
//		ibeaconStatusSpinnerList.add("?????????");
        aibeaconStatusAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, ibeaconStatusSpinnerList);
        aibeaconStatusAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ibeaconStatusSpinner.setAdapter(aibeaconStatusAdapter);
        ibeaconStatusSpinner
                .setOnItemSelectedListener(new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        // TODO Auto-generated method stub
                        LogUtils.d(TAG,
                                "????????? "
                                        + aibeaconStatusAdapter
                                        .getItem(position));

                        if (position == 0) {
                            ibeaconStatus = GlobalVariable.IBEACON_TYPE_UUID;
//							if (GetFunctionList.isSupportFunction(mContext,
//									GlobalVariable.IS_SUPPORT_DIAL_SWITCH)) {
//								dialType = GlobalVariable.SHOW_HORIZONTAL_SCREEN;
//								mWriteCommand
//										.controlDialSwitchAandLeftRightHand(
//												leftRightHand, dialType);
//							}
                        } else if (position == 1) {
                            ibeaconStatus = GlobalVariable.IBEACON_TYPE_MAJOR;
//							if (GetFunctionList.isSupportFunction(mContext,
//									GlobalVariable.IS_SUPPORT_DIAL_SWITCH)) {
//							dialType =GlobalVariable.SHOW_VERTICAL_ENGLISH_SCREEN;
//							mWriteCommand.controlDialSwitchAandLeftRightHand(leftRightHand, dialType);
//							}
                        } else if (position == 2) {
                            ibeaconStatus = GlobalVariable.IBEACON_TYPE_MINOR;
//							if (GetFunctionList.isSupportFunction(mContext,
//									GlobalVariable.IS_SUPPORT_DIAL_SWITCH)) {
//							dialType =GlobalVariable.SHOW_VERTICAL_CHINESE_SCREEN;
//							mWriteCommand.controlDialSwitchAandLeftRightHand(leftRightHand, dialType);
//							}
                        } else if (position == 3) {
                            ibeaconStatus = GlobalVariable.IBEACON_TYPE_DEVICE_NAME;
//							if (GetFunctionList.isSupportFunction(mContext,
//									GlobalVariable.IS_SUPPORT_DIAL_SWITCH)) {
//							dialType =GlobalVariable.NOT_SET_UP;
//							mWriteCommand.controlDialSwitchAandLeftRightHand(leftRightHand, dialType);
//							}
                        } else if (position == 4) {
                            ibeaconStatus = GlobalVariable.IBEACON_TYPE_TX_POWER;
                        } else if (position == 5) {
                            ibeaconStatus = GlobalVariable.IBEACON_TYPE_ADVERTISING_INTERVAL;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // TODO Auto-generated method stub

                    }
                });

        SetOrReadSpinnerList.add("??????");
        SetOrReadSpinnerList.add("??????");
//		SetOrReadSpinnerList.add("??????");
//		SetOrReadSpinnerList.add("??????");
//		SetOrReadSpinnerList.add("?????????");

        setOrReadAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, SetOrReadSpinnerList);
        setOrReadAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setOrReadSpinner.setAdapter(setOrReadAdapter);
        setOrReadSpinner
                .setOnItemSelectedListener(new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        // TODO Auto-generated method stub
                        LogUtils.d(TAG, "????????? " + setOrReadAdapter.getItem(position)/*+",???????????? ="+GetFunctionList.isSupportFunction(mContext,
								GlobalVariable.IS_SUPPORT_DIAL_SWITCH)*/);
                        if (position == 0) {
                            ibeaconSetOrRead = GlobalVariable.IBEACON_SET;
//							if (GetFunctionList.isSupportFunction(mContext,
//									GlobalVariable.IS_SUPPORT_DIAL_SWITCH)) {
//							leftRightHand =GlobalVariable.LEFT_HAND_WEAR;
//							mWriteCommand.controlDialSwitchAandLeftRightHand(leftRightHand, dialType);
//							}
                        } else if (position == 1) {
                            ibeaconSetOrRead = GlobalVariable.IBEACON_GET;
//							if (GetFunctionList.isSupportFunction(mContext,
//									GlobalVariable.IS_SUPPORT_DIAL_SWITCH)) {
//							leftRightHand =GlobalVariable.RIGHT_HAND_WEAR;
//							mWriteCommand.controlDialSwitchAandLeftRightHand(leftRightHand, dialType);
//							}
                        }
//						else if (position==2) {
//							if (GetFunctionList.isSupportFunction(mContext,
//									GlobalVariable.IS_SUPPORT_DIAL_SWITCH)) {
//							leftRightHand =GlobalVariable.NOT_SET_UP;
//							mWriteCommand.controlDialSwitchAandLeftRightHand(leftRightHand, dialType);
//							}
//						}
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // TODO Auto-generated method stub

                    }
                });
    }

    @Override
    public void onIbeaconWriteCallback(boolean result, int ibeaconSetOrGet,
                                       int ibeaconType, String data) {
        // public static final int IBEACON_TYPE_UUID = 0;// Ibeacon
        // ????????????,??????UUID/??????UUID
        // public static final int IBEACON_TYPE_MAJOR = 1;// Ibeacon
        // ????????????,??????major/??????major
        // public static final int IBEACON_TYPE_MINOR = 2;// Ibeacon
        // ????????????,??????minor/??????minor
        // public static final int IBEACON_TYPE_DEVICE_NAME = 3;// Ibeacon
        // ????????????,????????????device name/????????????device name
        // public static final int IBEACON_SET = 0;// Ibeacon
        // ??????(??????UUID/??????major,??????minor,????????????device name)
        // public static final int IBEACON_GET = 1;// Ibeacon
        // ??????(??????UUID/??????major,??????minor,????????????device name)
        LogUtils.d(TAG, "onIbeaconWriteCallback ?????????????????????result =" + result
                + ",ibeaconSetOrGet =" + ibeaconSetOrGet + ",ibeaconType ="
                + ibeaconType + ",??????data =" + data);
        if (result) {// success
            switch (ibeaconSetOrGet) {
                case GlobalVariable.IBEACON_SET:
                    switch (ibeaconType) {
                        case GlobalVariable.IBEACON_TYPE_UUID:
                            LogUtils.d(TAG, "??????UUID??????,data =" + data);
                            break;
                        case GlobalVariable.IBEACON_TYPE_MAJOR:
                            LogUtils.d(TAG, "??????major??????,data =" + data);
                            break;
                        case GlobalVariable.IBEACON_TYPE_MINOR:
                            LogUtils.d(TAG, "??????minor??????,data =" + data);
                            break;
                        case GlobalVariable.IBEACON_TYPE_DEVICE_NAME:
                            LogUtils.d(TAG, "??????device name??????,data =" + data);
                            break;
                        case GlobalVariable.IBEACON_TYPE_TX_POWER:
                            LogUtils.d(TAG, "??????TX power??????,data =" + data);
                            break;
                        case GlobalVariable.IBEACON_TYPE_ADVERTISING_INTERVAL:
                            LogUtils.d(TAG, "??????advertising interval??????,data =" + data);
                            break;

                        default:
                            break;
                    }
                    break;
                case GlobalVariable.IBEACON_GET:
                    switch (ibeaconType) {
                        case GlobalVariable.IBEACON_TYPE_UUID:
                            LogUtils.d(TAG, "??????UUID??????,data =" + data);
                            break;
                        case GlobalVariable.IBEACON_TYPE_MAJOR:
                            LogUtils.d(TAG, "??????major??????,data =" + data);
                            break;
                        case GlobalVariable.IBEACON_TYPE_MINOR:
                            LogUtils.d(TAG, "??????minor??????,data =" + data);
                            break;
                        case GlobalVariable.IBEACON_TYPE_DEVICE_NAME:
                            LogUtils.d(TAG, "??????device name??????,data =" + data);
                            break;
                        case GlobalVariable.IBEACON_TYPE_TX_POWER:
                            LogUtils.d(TAG, "??????TX power??????,data =" + data);
                            break;
                        case GlobalVariable.IBEACON_TYPE_ADVERTISING_INTERVAL:
                            LogUtils.d(TAG, "??????advertising interval,data =" + data);
                            break;

                        default:
                            break;
                    }
                    break;

                default:
                    break;
            }

        } else {// fail
            switch (ibeaconSetOrGet) {
                case GlobalVariable.IBEACON_SET:
                    switch (ibeaconType) {
                        case GlobalVariable.IBEACON_TYPE_UUID:
                            LogUtils.d(TAG, "??????UUID??????");
                            break;
                        case GlobalVariable.IBEACON_TYPE_MAJOR:
                            LogUtils.d(TAG, "??????major??????");
                            break;
                        case GlobalVariable.IBEACON_TYPE_MINOR:
                            LogUtils.d(TAG, "??????minor??????");
                            break;
                        case GlobalVariable.IBEACON_TYPE_DEVICE_NAME:
                            LogUtils.d(TAG, "??????device name??????");
                            break;

                        default:
                            break;
                    }
                    break;
                case GlobalVariable.IBEACON_GET:
                    switch (ibeaconType) {
                        case GlobalVariable.IBEACON_TYPE_UUID:
                            LogUtils.d(TAG, "??????UUID??????");
                            break;
                        case GlobalVariable.IBEACON_TYPE_MAJOR:
                            LogUtils.d(TAG, "??????major??????");
                            break;
                        case GlobalVariable.IBEACON_TYPE_MINOR:
                            LogUtils.d(TAG, "??????minor??????");
                            break;
                        case GlobalVariable.IBEACON_TYPE_DEVICE_NAME:
                            LogUtils.d(TAG, "??????device name??????");
                            break;

                        default:
                            break;
                    }
                    break;

                default:
                    break;
            }
        }

    }

    @Override
    public void onQueryDialModeCallback(boolean result, int screenWith,
                                        int screenHeight, int screenCount) {// ????????????????????????
        LogUtils.d(TAG, "result =" + result + ",screenWith =" + screenWith
                + ",screenHeight =" + screenHeight + ",screenCount ="
                + screenCount);
    }

    @Override
    public void onControlDialCallback(boolean result, int leftRightHand,
                                      int dialType) {// ??????????????????????????????????????????
        switch (leftRightHand) {
            case GlobalVariable.LEFT_HAND_WEAR:
                LogUtils.d(TAG, "????????????????????????");
                break;
            case GlobalVariable.RIGHT_HAND_WEAR:
                LogUtils.d(TAG, "????????????????????????");
                break;
            case GlobalVariable.NOT_SET_UP:
                LogUtils.d(TAG, "??????????????????????????????????????????");
                break;

            default:
                break;
        }
        switch (dialType) {
            case GlobalVariable.SHOW_VERTICAL_ENGLISH_SCREEN:
                LogUtils.d(TAG, "????????????????????????????????????");
                break;
            case GlobalVariable.SHOW_VERTICAL_CHINESE_SCREEN:
                LogUtils.d(TAG, "????????????????????????????????????");
                break;
            case GlobalVariable.SHOW_HORIZONTAL_SCREEN:
                LogUtils.d(TAG, "????????????????????????");
                break;
            case GlobalVariable.NOT_SET_UP:
                LogUtils.d(TAG, "?????????????????????????????????????????????");
                break;

            default:
                break;
        }
    }


    /**
     * ????????????????????????
     */
    private void testSendSevenDayWeather() {
        // TODO Auto-generated method stub
        // SevenDayWeatherInfo info =new SevenDayWeatherInfo(cityName,
        // todayWeatherCode, todayTmpCurrent, todayTmpMax, todayTmpMin,
        // todayPm25, todayAqi,
        // secondDayWeatherCode, secondDayTmpMax, secondDayTmpMin,
        // thirdDayWeatherCode, thirdDayTmpMax, thirdDayTmpMin,
        // fourthDayWeatherCode, fourthDayTmpMax, fourthDayTmpMin,
        // fifthDayWeatherCode, fifthDayTmpMax, fifthDayTmpMin,
        // sixthDayWeatherCode, sixthDayTmpMax, sixthDayTmpMin,
        // seventhDayWeatherCode, seventhDayTmpMax, seventhDayTmpMin);

        if (GetFunctionList.isSupportFunction(mContext,
                GlobalVariable.IS_SUPPORT_SEVEN_DAYS_WEATHER)) {
            SevenDayWeatherInfo info = new SevenDayWeatherInfo("?????????", "308",
                    25, 30, 20, 155, 50, "311", 32, 12, "312", 33, 13, "313",
                    34, 14, "314", 35, 15, "315", 36, 16, "316", 37, 17);

            mWriteCommand.syncWeatherToBLEForXiaoYang(info);
        } else {
            Toast.makeText(mContext, "?????????????????????", Toast.LENGTH_SHORT).show();
        }
    }

    private void upDateTodaySkipData() {
        // TODO Auto-generated method stub
        List<SkipDayInfo> list = mySQLOperate.querySkipDayInfo(CalendarUtils
                .getCalendar(0));// ???????????????0????????????-1????????????-2?????????????????????

        if (list != null) {
            SkipDayInfo mSkipInfo = null;
            for (int i = 0; i < list.size(); i++) {
                mSkipInfo = list.get(i);
                if (mSkipInfo != null) {
                    skip_time.setText(mSkipInfo.getUseTime() + "");
                    skip_count.setText(mSkipInfo.getCount() + "");
                    skip_calorie.setText(mSkipInfo.getCalories() + "");
                }
            }
        }
    }

    @Override
    public void onSportsTimeCallback(boolean result, String calendar, int sportsTime,
                                     int timeType) {

        if (timeType == GlobalVariable.SPORTS_TIME_TODAY) {

            LogUtils.d(TAG, "?????????????????????  calendar =" + calendar + ",sportsTime ="
                    + sportsTime);
            resultBuilder.append("\n" + calendar + "," + sportsTime
                    + getResources().getString(R.string.fminute));
            mHandler.sendEmptyMessage(UPDATE_SPORTS_TIME_DETAILS_MSG);

        } else if (timeType == GlobalVariable.SPORTS_TIME_HISTORY_DAY) {// 7??????????????????
            LogUtils.d(TAG, "7??????????????????  calendar =" + calendar
                    + ",sportsTime =" + sportsTime);
            resultBuilder.append("\n" + calendar + "," + sportsTime
                    + getResources().getString(R.string.fminute));
            mHandler.sendEmptyMessage(UPDATE_SPORTS_TIME_DETAILS_MSG);
        }
    }

    @Override
    public void OnResultSportsModes(boolean result, int status, int switchStatus, int sportsModes, SportsModesInfo info) {
        MultipleSportsModesUtils.LLogI("OnResultSportsModes  result =" + result + ",status =" + status + ",switchStatus =" + switchStatus
                + ",sportsModes =" + sportsModes + ",info =" + info);
        switch (status) {
            case ICallbackStatus.CONTROL_MULTIPLE_SPORTS_MODES://?????????????????????????????? ?????????????????????0-?????????1-?????????2-?????????3-?????????4-????????????
                break;
            case ICallbackStatus.INQUIRE_MULTIPLE_SPORTS_MODES://?????????????????????????????? ???????????????????????????
                break;
            case ICallbackStatus.SYNC_MULTIPLE_SPORTS_MODES_START://?????????????????????????????? ???????????????????????????????????????????????????
                //?????????sportsModes ?????????case???????????????sportsModes???????????????????????????????????????,???sportsModes???0???????????????????????????????????????
                sportTimes = sportsModes;
                break;
            case ICallbackStatus.SYNC_MULTIPLE_SPORTS_MODES://?????????????????????????????? ?????????????????????????????????
                sportTimes--;
                if (sportTimes == 0) {
                    Toast.makeText(mContext, "sportTimes==0?????????????????????", Toast.LENGTH_SHORT).show();
                }
                break;
            case ICallbackStatus.MULTIPLE_SPORTS_MODES_REAL://?????????????????????????????? ???????????????????????????????????????SportsModesInfo????????????????????????status???SportsModesInfo????????????SportsModesInfo????????????????????????????????????
//                ????????????
                int bleRateReal = info.getBleRateReal();
                break;
        }
    }

    //????????????????????????????????????
    private int sportTimes = 0;

    @Override
    public void OnResultHeartRateHeadset(boolean result, int status, int sportStatus, int values, HeartRateHeadsetSportModeInfo info) {
        HeartRateHeadsetUtils.LLogI("OnResultHeartRateHeadset  result =" + result + ",status =" + status + ",sportStatus =" + sportStatus
                + ",values =" + values + ",info =" + info);
        switch (status) {
            case ICallbackStatus.HEART_RATE_HEADSET_SPORT_STATUS://???????????????????????????
                HeartRateHeadsetUtils.LLogI("???????????? ???????????? ????????????=" + values + ",????????????=" + sportStatus);
                break;
            case ICallbackStatus.HEART_RATE_HEADSET_RATE_INTERVAL://???????????????????????????????????????????????????
                HeartRateHeadsetUtils.LLogI("???????????? ????????????=" + values);
                break;
            case ICallbackStatus.HEART_RATE_HEADSET_SPORT_DATA://???????????????????????????????????????
                if (info != null) {
                    int sportMode = info.getHrhSportsModes();
                    int rateValue = info.getHrhRateValue();
                    int calories = info.getHrhCalories();
                    int pace = info.getHrhPace();
                    int stepCount = info.getHrhSteps();
                    int count = info.getHrhCount();
                    float distance = info.getHrhDistance();
                    int durationTime = info.getHrhDuration();
                    HeartRateHeadsetUtils.LLogI("???????????? ??????????????????????????? ?????? sportMode=" + sportMode + ",rateValue=" + rateValue + ",calories=" + calories
                            + ",pace=" + pace + ",stepCount=" + stepCount + ",count=" + count + ",distance=" + distance + ",durationTime=" + durationTime);
                }
                break;
        }
    }

    @Override
    public void OnResultCustomTestStatus(boolean result, int status, CustomTestStatusInfo info) {
        LogUtils.d(TAG, "  result =" + result + ",status =" + status);
        if (status == ICallbackStatus.SET_CUSTOM_TEST_STATUS_OK) {//???????????????CustomTestStatusInfo.OPEN?????????CustomTestStatusInfo.CLOSE??????
            int heartRateStatus = info.getHeartRateStatus();
            int bpStatus = info.getBpStatus();
            int oxygenStatus = info.getOxygenStatus();
            int bodyTemperatureStatus = info.getBodyTemperatureStatus();
            LogUtils.d(TAG, "  heartRateStatus =" + heartRateStatus + ", bpStatus =" + bpStatus + ", oxygenStatus =" + oxygenStatus + ", bodyTemperatureStatus =" + bodyTemperatureStatus);
        } else if (status == ICallbackStatus.CUSTOM_TEST_RESULT_OK) {//????????????
            int functionType = info.getFunctionType();
            String calendar = info.getCalendar();
            int currentMinute = info.getCurrentMinute();
            int hour = currentMinute / 60;
            int minute = currentMinute % 60;
            String timeString = hour + ":" + minute;
            LogUtils.d(TAG, "  calendar =" + calendar + ", timeString =" + timeString);
            if (functionType == CustomTestStatusInfo.TYPE_HEART_RATE) {
                int heartRateValue = info.getHeartRateValue();
                LogUtils.d(TAG, "  heartRateValue =" + heartRateValue);
            } else if (functionType == CustomTestStatusInfo.TYPE_BP) {
                int bpHighValue = info.getBpHighValue();
                int bpLowValue = info.getBpLowValue();
                LogUtils.d(TAG, "  bpHighValue =" + bpHighValue + ", bpLowValue =" + bpLowValue);
            } else if (functionType == CustomTestStatusInfo.TYPE_OXYGEN) {
                int oxygenValue = info.getOxygenValue();
                LogUtils.d(TAG, "  oxygenValue =" + oxygenValue);
            } else if (functionType == CustomTestStatusInfo.TYPE_BODY_TEMPERATURE) {
                float bodySurfaceTemperature = info.getBodySurfaceTemperature();
                float bodyTemperature = info.getBodyTemperature();
                LogUtils.d(TAG, "  bodySurfaceTemperature =" + bodySurfaceTemperature + ", bodyTemperature =" + bodyTemperature);
            }

        } else if (status == ICallbackStatus.QUERY_CUSTOM_TEST_STATUS_OK) {//??????????????????????????????CustomTestStatusInfo.OPEN?????????CustomTestStatusInfo.CLOSE??????
            int heartRateStatus = info.getHeartRateStatus();
            int bpStatus = info.getBpStatus();
            int oxygenStatus = info.getOxygenStatus();
            int bodyTemperatureStatus = info.getBodyTemperatureStatus();
            LogUtils.d(TAG, " heartRateStatus =" + heartRateStatus + ", bpStatus =" + bpStatus + ", oxygenStatus =" + oxygenStatus + ", bodyTemperatureStatus =" + bodyTemperatureStatus);
        }
    }

    @Override
    public void onRateCalibrationStatus(int status) {
        // TODO Auto-generated method stub
/*		status: 0----????????????
		        1----????????????
		        253---????????????????????????
		        ?????????????????????????????????????????????10???????????????????????????0?????????????????????????????????stopRateCalibration()*/

        LogUtils.d(TAG, "???????????? status:" + status);

    }

    @Override
    public void onTurnWristCalibrationStatus(int status) {
        // TODO Auto-generated method stub
        LogUtils.d(TAG, "???????????? status:" + status);
		/*status: 0----????????????
                  1----????????????
                  255----????????????
                  253---????????????????????????*/
    }

    @Override
    public void onTestResult(TemperatureInfo info) {//??????????????????
        LogUtils.d(TAG, "calendar =" + info.getCalendar() + ",startDate =" + info.getStartDate() + ",secondTime =" + info.getSecondTime()
                + ",bodyTemperature =" + info.getBodyTemperature());
    }

    @Override
    public void onSamplingResult(TemperatureInfo info) {
//        info.getType();??????????????????
//        TemperatureUtil.TYPE_NOT_SUPPORT_SAMPLING ;//?????????????????????????????????
//        TemperatureUtil.TYPE_SUPPORT_SAMPLING_MODE_1;//??????????????????????????????,?????????
//        TemperatureUtil.TYPE_SUPPORT_SAMPLING_MODE_2;//??????????????????????????????,?????????
        LogUtils.d(TAG, "type =" + info.getType() + ",calendar =" + info.getCalendar() + ",startDate =" + info.getStartDate() + ",secondTime =" + info.getSecondTime()
                + ",bodyTemperature =" + info.getBodyTemperature() + ",bodySurfaceTemperature =" + info.getBodySurfaceTemperature()
                + ",ambientTemperature =" + info.getAmbientTemperature());
    }

    @Override
    public void onTestResult(int status, OxygenInfo info) {
        Message message = new Message();
        if (status == OxygenUtil.OXYGEN_TEST_START_HAS_VALUE) { //start has oxygen value
            message.what = status;
            message.obj = info;
        } else if (status == OxygenUtil.OXYGEN_TEST_START_NO_VALUE) { //start has no oxygen value
            message.what = status;
        } else if (status == OxygenUtil.OXYGEN_TEST_STOP_HAS_VALUE) { //stop has oxygen value
            message.what = status;
            message.obj = info;//oxygen value is "info.getOxygenValue()"
        } else if (status == OxygenUtil.OXYGEN_TEST_STOP_NO_VALUE) {//stop has no oxygen value
            message.what = status;
        } else if (status == OxygenUtil.OXYGEN_TEST_TIME_OUT) {//Test time out
            message.what = status;
        }
        OxygenUtil.LogI("onTestResult status =" + status + ",info =" + info);
        mHandler.sendMessage(message);
    }

//	private void queryAirPressureTemperature(int type) {
//		List<AirPressureTemperatureDayInfo>  list;
//		if (type==1) {//???????????????????????????????????????????????????
//			list =mySQLOperate.queryAirPressureTemperatureOneDayInfo("20171113");
//		}else {//???????????????????????????
//			list =mySQLOperate.queryAirPressureTemperatureAllDayInfo();
//		}
//		AirPressureTemperatureDayInfo info =null;
//		for (int i = 0; i < list.size(); i++) {
//			info =list.get(i);
//			String calendar = info.getCalendar();
//			int time = info.getTime();
//			int airPressure = info.getAirPressure();
//			int temperature = info.getTemperature();
//			LogUtils.d(TAG,"??????????????????  calendar ="+calendar+",time ="+time+",airPressure ="+airPressure+",temperature ="+temperature);
//		}
//	}

//    public void updateSteps(int steps, float distance, float calories) {//?????????????????????????????????
//        if (GetFunctionList.isSupportFunction_Fourth(getApplicationContext(), GlobalVariable.IS_SUPPORT_STEP_CALORIES_CUMULATIVE)) {//???????????????????????????????????????
//            List<SportsModesInfo> list = UTESQLOperate.getInstance(getApplicationContext()).querySportsModes(CalendarUtils.getCalendar());
//            for (int i = 0; i < list.size(); i++) {
//                if (list.get(i).getBleStepCount() > 0) {
//                    steps += list.get(i).getBleStepCount();
//                    distance += list.get(i).getBleSportsDistance();
//                    calories += list.get(i).getBleSportsCalories();
//                }
//            }
//        }
//    }

    public final static int BREATHE_TEST_STOP_NO_VALUE_MSG = 50;
    public final static int BREATHE_TEST_STOP_HAS_VALUE_MSG = 51;
    public final static int BREATHE_TEST_START_NO_VALUE_MSG = 52;
    public final static int BREATHE_TEST_START_HAS_VALUE_MSG = 53;
    public final static int BREATHE_TEST_TIME_OUT_MSG = 54;

    @Override
    public void onBreatheResult(int status, BreatheInfo info) {
        BreatheUtil.LogI("onBreatheResult status =" + status + ",info =" + new Gson().toJson(info));
        Message message = new Message();
        if (status == BreatheUtil.BREATHE_TEST_START_HAS_VALUE) { //start has breathe value
            message.what = BREATHE_TEST_START_HAS_VALUE_MSG;
            message.obj = info;
        } else if (status == BreatheUtil.BREATHE_TEST_START_NO_VALUE) { //start has no breathe value
            message.what = BREATHE_TEST_START_NO_VALUE_MSG;
        } else if (status == BreatheUtil.BREATHE_TEST_STOP_HAS_VALUE) { //stop has breathe value
            message.what = BREATHE_TEST_STOP_HAS_VALUE_MSG;
            message.obj = info;//breathe value is "info.getOxygenValue()"
        } else if (status == BreatheUtil.BREATHE_TEST_STOP_NO_VALUE) {//stop has no breathe value
            message.what = BREATHE_TEST_STOP_NO_VALUE_MSG;
        } else if (status == BreatheUtil.BREATHE_TEST_TIME_OUT) {//Test time out
            message.what = BREATHE_TEST_TIME_OUT_MSG;
        }
        mHandler.sendMessage(message);
    }
}
