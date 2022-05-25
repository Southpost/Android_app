package com.yc.peddemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import com.yc.pedometer.utils.LogUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tbruyelle.rxpermissions.RxPermissions;
import com.yc.pedometer.sdk.BLEServiceOperate;
import com.yc.pedometer.sdk.DeviceScanInterfacer;
import com.yc.pedometer.utils.SPUtil;

import rx.functions.Action1;

/**
 * 用于扫描和显示可用蓝牙设备的Activity。
 */
public class DeviceScanActivity extends AppCompatActivity implements
		DeviceScanInterfacer,AdapterView.OnItemClickListener {
	private String TAG="DeviceScanActivity";
	private LeDeviceListAdapter mLeDeviceListAdapter;
	private boolean mScanning;
	private Handler mHandler;

	private final int REQUEST_ENABLE_BT = 1;
	//设置蓝牙扫描所需的时间：10秒后停止扫描
	private final long SCAN_PERIOD = 10000;
	private BLEServiceOperate mBLEServiceOperate;

	private ListView mListView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_scan);
		setTitle("设备列表");
		mHandler = new Handler();
		mBLEServiceOperate = BLEServiceOperate
				.getInstance(getApplicationContext());// 用于BluetoothLeService实例化准备

		// 检查设备是否支持蓝牙
		if (!mBLEServiceOperate.isSupportBle4_0()) {
			Toast.makeText(this, R.string.not_support_ble, Toast.LENGTH_SHORT)
					.show();
			finish();
			return;
		}

        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

		new RxPermissions(this).request(permissions).subscribe(new Action1<Boolean>() {
			@Override
			public void call(Boolean granted) {
				if (granted) {
				} else {
				}
			}
		});

		mBLEServiceOperate.setDeviceScanListener(this);

        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mListView = (ListView) findViewById(R.id.xListView);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mLeDeviceListAdapter);

		scanLeDevice(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if (!mScanning) {
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(true);
//			menu.findItem(R.id.menu_refresh).setActionView(null);
		} else {
			menu.findItem(R.id.menu_stop).setVisible(true);
			menu.findItem(R.id.menu_scan).setVisible(false);
//			menu.findItem(R.id.menu_refresh).setActionView(
//					R.layout.actionbar_indeterminate_progress);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_scan:
			mLeDeviceListAdapter.clear();
			scanLeDevice(true);
			break;
		case R.id.menu_stop:
			scanLeDevice(false);
			break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		//确保在设备上启用蓝牙。如果当前未启用蓝牙，则触发意图以显示一个对话框，要求用户授予启用它的权限
		if (!mBLEServiceOperate.isBleEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		scanLeDevice(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//用户选择不启用蓝牙
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
		mLeDeviceListAdapter.clear();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		mBLEServiceOperate.unBindService();// unBindService
	}

//	@Override
//	protected void onListItemClick(ListView l, View v, int position, long id) {
//		final BleDevices device = mLeDeviceListAdapter.getDevice(position);
//		if (device == null)
//			return;
//		final Intent intent = new Intent(this, MainActivity.class);
//		intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, device.getName());
//		intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
//		if (mScanning) {
//			mBLEServiceOperate.stopLeScan();
//			mScanning = false;
//		}
//		startActivity(intent);
//	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			//在预定义的扫描周期后停止扫描
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBLEServiceOperate.stopLeScan();
					invalidateOptionsMenu();
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBLEServiceOperate.startLeScan();
			LogUtils.i(TAG,"startLeScan");
		} else {
			mScanning = false;
			mBLEServiceOperate.stopLeScan();
		}
		invalidateOptionsMenu();
	}

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final BleDevices device = mLeDeviceListAdapter.getDevice(position);
        if (device == null)
            return;
        final Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        SPUtil.getInstance(getApplicationContext()).setLastConnectDeviceAddress(device.getAddress());
        if (mScanning) {
            mBLEServiceOperate.stopLeScan();
            mScanning = false;
        }
        startActivity(intent);
        finish();
    }

    //用于固定通过扫描找到的设备的适配器
	private class LeDeviceListAdapter extends BaseAdapter {
		// private ArrayList<BluetoothDevice> mLeDevices;
		private ArrayList<BleDevices> mLeDevices;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			// mLeDevices = new ArrayList<BluetoothDevice>();
			mLeDevices = new ArrayList<BleDevices>();
			mInflator = DeviceScanActivity.this.getLayoutInflater();
		}

		// public void addDevice(BluetoothDevice device) {
		// if (!mLeDevices.contains(device)) {
		// mLeDevices.add(device);
		// }
		// }
		public void addDevice(BleDevices device) {
			boolean repeat = false;
			for (int i = 0; i < mLeDevices.size(); i++) {
				if (mLeDevices.get(i).getAddress().equals(device.getAddress())) {
					mLeDevices.remove(i);
					repeat = true;
					mLeDevices.add(i, device);
				}
			}
			if (!repeat) {
				mLeDevices.add(device);
			}
		}

		public BleDevices getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			// 通用 ListView 优化代码
			if (view == null) {
				view = mInflator.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceName = (TextView) view
						.findViewById(R.id.device_name);
				viewHolder.deviceAddress = (TextView) view
						.findViewById(R.id.device_address);
				viewHolder.deviceRssi = (TextView) view
						.findViewById(R.id.device_rssi);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			Collections.sort(mLeDevices);
			BleDevices device = mLeDevices.get(i);
			final String deviceName = device.getName();
			final int rssi = device.getRssi();
			if (deviceName != null && deviceName.length() > 0) {
				viewHolder.deviceName.setText(deviceName);
				
			} else {
				viewHolder.deviceName.setText(R.string.unknown_device);
			}
			viewHolder.deviceAddress.setText(device.getAddress());
			viewHolder.deviceRssi.setText(rssi+"");
			return view;
		}
	}

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
		TextView deviceRssi;
	}

    @Override
    public void LeScanCallback(final BluetoothDevice device, final int rssi,byte[] scanRecord) {
        // TODO Auto-generated method stub
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // mLeDeviceListAdapter.addDevice(device);
//				LogUtils.i(TAG,"device="+device);
                if (device != null) {
                    if (TextUtils.isEmpty(device.getName())) {
                        return;
                    }
//                    if (!device.getName().contains("SmartWristband")) {//SmartWristband
//                        return;
//                    }
                    BleDevices mBleDevices = new BleDevices(device.getName(),
                            device.getAddress(), rssi);
                    mLeDeviceListAdapter.addDevice(mBleDevices);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}