package com.yc.peddemo;

public class DialogType {
	public final static int DIALOG_EXCEPTION = 0 ; //异常对话框
	public final static int DIALOG_DISCONNECT = 1 ; //未连接设备
	public final static int DIALOG_NETWORK_DISABLE = 2 ;//没有网络
	public final static int DIALOG_NETWORK_CHECK = 3 ;  //没有网络       请检查网络连接再试试
	public final static int DIALOG_SYNCING = 4 ;  //同步中，请稍候
	public final static int DIALOG_FIND_BAND = 5 ;  //已发现设备
	public final static int DIALOG_BLE_NEWEST_VERSION = 6 ;  //ble已是最新版本
	public final static int DIALOG_APK_NEWEST_VERSION = 7 ;  //apk已是最新版本
	public final static int DIALOG_DEAL_WITH_SUCCESS = 8 ;  //处理成功
	public final static int DIALOG_DEAL_WITH_FAIL = 9 ;  //处理失败
	public final static int DIALOG_FREQUENT_ACCESS_SERVER = 10 ;  //请勿频繁访问服务器
	public final static int DIALOG_FACTORY_DATA_RESET = 11 ;  //恢复出厂设置
	public final static int DIALOG_FIND_APK_NEW_VERSION = 12 ;  //发现APK新版本
	public final static int DIALOG_FIND_BLE_NEW_VERSION = 13 ;  //发现BLE新版本
	public final static int DIALOG_FIND_BLE_NEW_VERSION_CANCEL = 14 ;  //发现BLE新版本,但是取消升级
	public final static int DIALOG_INITALIZE_FAIL = 15 ;  //初始化失败
	public final static int DIALOG_POWER_POOL = 16 ;  //电量不足
	public final static int DIALOG_CONFIRM_SWITCH_TO_WECHAT = 17 ;  //showConfirmSwitchToWechatDialog
	public final static int DIALOG_UNINSTALL_APP = 18 ; 
	public final static int DIALOG_FIND_BLE_NEW_VERSION_RK = 19 ;  //发现BLE新版本  for RK
	public final static int DIALOG_TEST_LESS_THAN_ONE_MINITE = 20 ;  //请测试超过1分钟
	public final static int DIALOG_TESTTING_RATE = 21 ;  //请测试超过1分钟
	public final static int DIALOG_FINISH = 22 ;  //更新完成
	public final static int DIALOG_MAP_RECORD_PAUSE = 23 ; 
	public final static int DIALOG_MAP_RECORD_CONTINUE = 24 ; 
	public final static int DIALOG_SAVE_QRCODE_FINISH = 25 ;  //保存成功
	public final static int DIALOG_SAVE_QRCODE_FAIL = 26 ;  //保存失败
	public final static int DIALOG_GET_QRCODE_FAIL = 27 ;  //获取二维码失败
	public final static int DIALOG_SET_OK = 28 ;  //设置成功
	public final static int DIALOG_ACCESS_SERVER_TIME_OUT = 29 ;  //服务器无响应
	public final static int DIALOG_UNBIND_DEVICE = 30 ;  //解绑
	public final static int DIALOG_UNBIND_DEVICE_SUCCESS = 31 ;  //解绑成功
	public final static int DIALOG_RATE_TESTING = 32 ;  //心率测试中
	public final static int DIALOG_BP_TESTING = 33 ;  //血压测试中
//	public final static int DIALOG_READ_BLE_VERSION_FIRST = 34 ;  //请先获取版本
	public final static int DIALOG_BLUETOOTH_CLOSED = 35 ; //蓝牙未打开
	public final static int DIALOG_CHOOSE_AT_LEAST_ONE_DAY = 36 ; //至少选择一天训练
	public final static int DIALOG_NEEDS_LESS_THAN_START_TIME = 37 ; //提示时间需要小于训练开始时间
	public final static int DIALOG_DELETE_TRAINING_PLAN = 38 ; //是否删除训练目标
	public final static int DIALOG_DELETE_TRAINING_PLAN_OK = 39 ; //删除训练目标完成
	public final static int DIALOG_LOCATION_FAIL = 40 ; //获取天气信息时定位失败
	public final static int DIALOG_GET_WEATHER_FAIL = 41 ; //获取天气信息时失败
	public final static int DIALOG_DO_NOT_LOGIN = 42 ; //未登陆
	public final static int DIALOG_LOADING_FAILED = 43 ; //加载失败
	public final static int DIALOG_DELETE = 44 ; //删除生理周期
	public final static int DIALOG_SHOW_PHYSIOLOGICAL = 45 ; //显示生理周期
	public final static int DIALOG_LOGOUT = 46 ; //注销
	public final static int DIALOG_OPEN_NOTIFY = 47 ; //打开通知栏

	
}
