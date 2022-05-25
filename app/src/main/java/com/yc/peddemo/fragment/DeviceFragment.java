package com.yc.peddemo.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yc.peddemo.MainActivity;
import com.yc.peddemo.R;
import com.yc.peddemo.event.ConnectEvent;
import com.yc.peddemo.onlinedial.BaseFragment;
import com.yc.pedometer.utils.SPUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * desc:
 * author: lei
 * date: 2022/3/10
 **/

//设备连接与显示
public class DeviceFragment extends BaseFragment {
    private View rootView;
    @BindView(R.id.iv_watch_state)
    ImageView ivWatchState;  //连接显示
    @BindView(R.id.tv_state)
    TextView tvState;
    @BindView(R.id.tv_battery)
    TextView tvBattery;
    @BindView(R.id.tv_title)
    TextView tvTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_device, container, false);
        ButterKnife.bind(this, rootView);
        EventBus.getDefault().register(this);
        initView();
        return rootView;
    }

    private void initView() {
        rootView.findViewById(R.id.iv_back).setVisibility(View.GONE);
        tvTitle.setText("设备状态");
        syncState();
    }

    private void syncState() {
        boolean ble_connecte = SPUtil.getInstance(getActivity()).getBleConnectStatus();
        if (ble_connecte) {
            tvState.setText(getString(R.string.connected));
            ivWatchState.setImageResource(R.drawable.ic_watch_online);
        } else {
            tvState.setText(getString(R.string.disconnect));
            ivWatchState.setImageResource(R.drawable.ic_watch_offline);
        }
        ((MainActivity) getActivity()).mWriteCommand.sendDisturbToBle(false, false, true, 00, 00,23, 59);
        ((MainActivity) getActivity()).mWriteCommand.sendToReadBLEBattery();
        tvBattery.setText("剩余电量：" + SPUtil.getInstance(getActivity()).getBleBatteryValue() + "%");
    }

    @OnClick(R.id.iv_watch_state)
    public void onClick(View view) {
        initView();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventbusReceiver(ConnectEvent event) {
        if (event.isConnect()) {
            syncState();
        }
    }
}
