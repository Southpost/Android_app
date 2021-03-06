package com.yc.peddemo.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.yc.peddemo.MainActivity;
import com.yc.peddemo.R;
import com.yc.peddemo.entity.BloodOxygenEntity;
import com.yc.peddemo.entity.BloodPressureEntity;
import com.yc.peddemo.entity.BodyTemperatureEntity;
import com.yc.peddemo.entity.HeartRateEntity;
import com.yc.peddemo.event.ConnectEvent;
import com.yc.peddemo.onlinedial.BaseFragment;
import com.yc.pedometer.info.BPVOneDayInfo;
import com.yc.pedometer.info.OxygenInfo;
import com.yc.pedometer.info.Rate24HourDayInfo;
import com.yc.pedometer.info.RateOneDayInfo;
import com.yc.pedometer.info.SleepTimeInfo;
import com.yc.pedometer.info.TemperatureInfo;
import com.yc.pedometer.sdk.UTESQLOperate;
import com.yc.pedometer.utils.CalendarUtils;
import com.yc.pedometer.utils.GlobalVariable;
import com.yc.pedometer.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * desc:
 * author: lei
 * date: 2022/3/10
 **/
public class HomeFragment extends BaseFragment {
    @BindView(R.id.chart_heart_rate)
    LineChart heartRateChart;
    @BindView(R.id.chart_oxygen)
    LineChart chartOxygen;
    @BindView(R.id.chart_blood_pressure)
    LineChart chartBloodPressure;
    @BindView(R.id.chart_temperature)
    LineChart chartTemperature;
    @BindView(R.id.tv_total_step)
    TextView tvTotalStep;
    @BindView(R.id.tv_plan_percent)
    TextView tvPlanPercent;
    @BindView(R.id.tv_step_info)
    TextView tvStepInfo;
    @BindView(R.id.tv_sleep_info)
    TextView tvSleepInfo;
    private View view;
    private String TAG = "HomeFragment";
    private List<String> yList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        for (int i = 0; i <= 24; i++) {
            yList.add(i + "");
        }
        initLineChar();
        getDataBy10min();
        initTodayStepData(0, 0, 0);
        return view;
    }

    public void initTodayStepData(int step, float calories, float Distance) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        String step_plan = sharedPreferences.getString("step_plan", 8000 + "");
        tvTotalStep.setText(step + "");
        tvPlanPercent.setText("??????????????? " + (int) (((float) step / (float) Integer.parseInt(step_plan)) * 100) + "%" + " \n????????????:" + step_plan);
        tvStepInfo.setText(String.format("?????????????????????%.3f ????????? %.3f ", Distance, calories));
    }

    private void getDataBy10min() {

        //?????????
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ((MainActivity) getActivity()).mWriteCommand.sendRateTestCommand(GlobalVariable.RATE_TEST_START);//?????????
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        syncData();
                    }
                });
            }
        }, 1000 * 60, 1000 * 60 * 10);
        //?????????
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ((MainActivity) getActivity()).mWriteCommand.startOxygenTest();//?????????
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        syncData();
                    }
                });
            }
        }, 1000 * 60 * 2, 1000 * 60 * 10);
        //?????????
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ((MainActivity) getActivity()).mWriteCommand.sendBloodPressureTestCommand(GlobalVariable.BLOOD_PRESSURE_TEST_START);//?????????
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        syncData();
                    }
                });
            }
        }, 1000 * 60 * 4, 1000 * 60 * 10);
        //?????????
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ((MainActivity) getActivity()).mWriteCommand.queryCurrentTemperatureData();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        syncData();
                    }
                });
            }
        }, 1000 * 60 * 6, 1000 * 60 * 10);

    }

    private void syncData() {
        ((MainActivity) getActivity()).mWriteCommand.syncRateData(); //??????
        ((MainActivity) getActivity()).mWriteCommand.syncAllSleepData(); //??????
        ((MainActivity) getActivity()).mWriteCommand.syncAllTemperatureData(); //??????
        ((MainActivity) getActivity()).mWriteCommand.syncAllBloodPressureData(); //??????
        ((MainActivity) getActivity()).mWriteCommand.syncOxygenData(); //??????
        ((MainActivity) getActivity()).mWriteCommand.syncAllStepData();

        //??????
        List<RateOneDayInfo> mRateOneDayInfoList = new ArrayList<RateOneDayInfo>();
        mRateOneDayInfoList = ((MainActivity) getActivity()).mySQLOperate.queryRateOneDayDetailInfo(CalendarUtils.getCalendar(0));
        LogUtils.d(TAG, "mRateOneDayInfoList =" + mRateOneDayInfoList);
        if (mRateOneDayInfoList != null) {
            List<HeartRateEntity> heartRateEntities = new ArrayList<>();
            for (int i = 0; i < mRateOneDayInfoList.size(); i++) {
                String time = mRateOneDayInfoList.get(i).getCalendarTime();
                int rate = mRateOneDayInfoList.get(i).getRate();
                LogUtils.d(TAG, "mRateOneDayInfoList time =" + time + ",rate =" + rate);
                heartRateEntities.add(new HeartRateEntity(rate + "", time + ""));
            }
            setHeartRateNewData(heartRateEntities);
        }
        List<Rate24HourDayInfo> rate24HourDayInfos = ((MainActivity) getActivity()).mySQLOperate.query24HourRateAllInfo();


        //??????
        List<OxygenInfo> oxygenInfos = ((MainActivity) getActivity()).mySQLOperate.queryOxygenDate(CalendarUtils.getCalendar(0));
        if (oxygenInfos != null) {
            List<BloodOxygenEntity> oxygenEntities = new ArrayList<>();
            for (int i = 0; i < oxygenInfos.size(); i++) {
                int oxygenValue = oxygenInfos.get(i).getOxygenValue();
                int time = oxygenInfos.get(i).getTime();
                oxygenEntities.add(new BloodOxygenEntity(oxygenValue + "", time + ""));
            }
            setOxygenNewData(oxygenEntities);
        }

        //??????
        List<BPVOneDayInfo> bpvOneDayInfos = ((MainActivity) getActivity()).mySQLOperate.queryBloodPressureOneDayInfo(CalendarUtils.getCalendar(0));
        if (bpvOneDayInfos != null) {
            List<BloodPressureEntity> bloodPressureEntities = new ArrayList<>();
            for (int i = 0; i < bpvOneDayInfos.size(); i++) {
                int lowBloodPressure = bpvOneDayInfos.get(i).getLowBloodPressure();
                int hightBloodPressure = bpvOneDayInfos.get(i).getHightBloodPressure();
                int time = bpvOneDayInfos.get(i).getBloodPressureTime();
                bloodPressureEntities.add(new BloodPressureEntity(lowBloodPressure + "", hightBloodPressure + "", time + ""));
            }
            setBloodPressureNewData(bloodPressureEntities);
        }

        //??????
        List<TemperatureInfo> temperatureInfos = ((MainActivity) getActivity()).mySQLOperate.queryTemperatureDate(CalendarUtils.getCalendar(0));
        if (temperatureInfos != null) {
            List<BodyTemperatureEntity> bodyTemperatureEntities = new ArrayList<>();
            for (int i = 0; i < temperatureInfos.size(); i++) {
                float bodyTemperature = temperatureInfos.get(i).getBodyTemperature();
                String time = temperatureInfos.get(i).getStartDate();
                bodyTemperatureEntities.add(new BodyTemperatureEntity(bodyTemperature + "", time + ""));
            }
            setTemperatureNewData(bodyTemperatureEntities);
        }


        //????????????
        SleepTimeInfo sleepTimeInfo = UTESQLOperate.getInstance(getActivity()).querySleepInfo(CalendarUtils.getCalendar(0));
        int deepTime, lightTime, awakeCount, sleepTotalTime;
        if (sleepTimeInfo != null) {
            deepTime = sleepTimeInfo.getDeepTime();
            lightTime = sleepTimeInfo.getLightTime();
            awakeCount = sleepTimeInfo.getAwakeCount();
            sleepTotalTime = sleepTimeInfo.getSleepTotalTime();
            double total_hour = ((float) sleepTotalTime / 60f);
            DecimalFormat df1 = new DecimalFormat("0.0"); // ??????1????????????????????????

            int deep_hour = deepTime / 60;
            int deep_minute = (deepTime - deep_hour * 60);
            int light_hour = lightTime / 60;
            int light_minute = (lightTime - light_hour * 60);
            int active_count = awakeCount;
            String total_hour_str = df1.format(total_hour);
            tvSleepInfo.setText(String.format("??????????????? %s ?????? ????????????%s ???????????? %s ?????????%s", total_hour_str,
                    deep_hour + getContext().getResources().getString(R.string.hour) + deep_minute + getContext().getResources().getString(R.string.minute),
                    light_hour + getContext().getResources().getString(R.string.hour) + light_minute + getContext().getResources().getString(R.string.minute),
                    active_count + getContext().getResources().getString(R.string.count)));
        } else {
            tvSleepInfo.setText("????????????????????????");
        }


    }


    private void initLineChar() {
        Description desc = new Description();
        desc.setText("??????");
        heartRateChart.setNoDataText("????????????");
        //??????X???
        XAxis xAxis = heartRateChart.getXAxis();
        //??????X??????????????????????????????)
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        //??????????????????
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        //??????X??????????????????????????????
        xAxis.setGranularity(1f);
        heartRateChart.setDescription(desc);
        //????????????????????????
        YAxis leftAxis = heartRateChart.getAxisLeft();
        leftAxis.setAxisMinimum(50);

        YAxis rightAxis = heartRateChart.getAxisRight();
        rightAxis.setEnabled(false);

        xAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {

                return String.format("%02d:00", (int) value);
            }
        });


        Description descOxygen = new Description();
        descOxygen.setText("??????");
        chartOxygen.setNoDataText("????????????");
        XAxis xAxisOxygen = chartOxygen.getXAxis();
        //??????X??????????????????????????????)
        xAxisOxygen.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisOxygen.setAxisMinimum(1f);
        xAxisOxygen.setGranularity(1f);
        xAxisOxygen.setDrawGridLines(false);
        chartOxygen.setDescription(descOxygen);
        //??????????????????
        xAxisOxygen.setDrawGridLines(false);
        xAxisOxygen.setDrawAxisLine(false);
        //??????X??????????????????????????????
        xAxisOxygen.setGranularity(1f);
        chartOxygen.setDescription(descOxygen);
        //????????????????????????
        YAxis leftAxisOxygen = chartOxygen.getAxisLeft();
        leftAxisOxygen.setAxisMinimum(95);
        leftAxisOxygen.setAxisMaximum(100);

        YAxis rightAxisOxygen = chartOxygen.getAxisRight();
        rightAxisOxygen.setEnabled(false);

        xAxisOxygen.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {

                return String.format("%02d:00", (int) value);
            }
        });


        Description descBloodPressure = new Description();
        descBloodPressure.setText("??????");
        chartBloodPressure.setNoDataText("????????????");
        XAxis xAxisBloodPressure = chartBloodPressure.getXAxis();
        xAxisBloodPressure.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisBloodPressure.setAxisMinimum(1f);
        xAxisBloodPressure.setGranularity(1f);
        xAxisBloodPressure.setDrawGridLines(false);
        chartBloodPressure.setDescription(descBloodPressure);
        //??????????????????
        xAxisBloodPressure.setDrawGridLines(false);
        xAxisBloodPressure.setDrawAxisLine(false);
        //??????X??????????????????????????????
        xAxisBloodPressure.setGranularity(1f);
        chartBloodPressure.setDescription(descBloodPressure);
        //????????????????????????
        YAxis leftAxisBloodPressure = chartBloodPressure.getAxisLeft();
        leftAxisBloodPressure.setAxisMinimum(60);

        YAxis rightAxisBloodPressure = chartBloodPressure.getAxisRight();
        rightAxisBloodPressure.setEnabled(false);

        xAxisBloodPressure.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {

                return String.format("%02d:00", (int) value);
            }
        });



        Description descTemperature = new Description();
        descTemperature.setText("??????");
        chartTemperature.setNoDataText("????????????");
        XAxis xAxisTemperature = chartTemperature.getXAxis();
        xAxisTemperature.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisTemperature.setAxisMinimum(1f);
        xAxisTemperature.setDrawGridLines(false);
        xAxisTemperature.setGranularity(1f);
        chartTemperature.setDescription(descTemperature);
        //??????????????????
        xAxisTemperature.setDrawGridLines(false);
        xAxisTemperature.setDrawAxisLine(false);
        //??????X??????????????????????????????
        xAxisTemperature.setGranularity(1f);
        chartTemperature.setDescription(descTemperature);
        //????????????????????????
        YAxis leftAxisTemperature = chartTemperature.getAxisLeft();
        leftAxisTemperature.setAxisMinimum(30);

        YAxis rightAxisTemperature = chartTemperature.getAxisRight();
        rightAxisTemperature.setEnabled(false);

        xAxisTemperature.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {

                return String.format("%02d:00", (int) value);
            }
        });
    }


    private void setHeartRateNewData(List<HeartRateEntity> datas) {

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

        //1.??????x??????y?????????
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < datas.size(); i++) {
            //?????????202201200838
            String time = datas.get(i).getTime();
            int timeIndex = Integer.parseInt(time.substring(time.length() - 4, time.length()));
            float finalTime = timeIndex / 100 + (float) timeIndex % 100 / 60;
            try {
                entries.add(new Entry(finalTime, Float.parseFloat(datas.get(i).getLevel())));
            } catch (Exception e) {

            }

        }
        LineDataSet dataSet = new LineDataSet(entries, "??????");
        dataSet.setLineWidth(3.0f);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setColor(Color.RED);
        dataSet.setCircleColor(Color.BLACK);
        dataSet.setDrawCircles(false);  //??????????????????
        dataSet.setDrawValues(false);//??????????????????????????????

        List<Entry> entriesy = new ArrayList<>();
        for (int i = 0; i < yList.size(); i++) {
            try {
                entriesy.add(new Entry(Float.parseFloat(yList.get(i)), 0));
            } catch (Exception e) {

            }
        }
        LineDataSet dataSet1 = new LineDataSet(entriesy, "");

        dataSet1.setLineWidth(0.1f);
        dataSet1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet1.setColor(Color.BLACK);
        dataSet1.setCircleColor(Color.BLACK);
        dataSet1.setDrawCircles(false);  //??????????????????
        dataSet1.setDrawValues(false);//??????????????????????????????

        dataSets.add(dataSet1);
        dataSets.add(dataSet);

        LineData lineData1 = new LineData(dataSets);
        heartRateChart.setData(lineData1);
        heartRateChart.invalidate(); // refresh
    }


    private void setOxygenNewData(List<BloodOxygenEntity> datas) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

        //1.??????x??????y?????????
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            try {
                entries.add(new Entry(i, Float.parseFloat(datas.get(i).getLevel())));
            } catch (Exception e) {

            }

        }
        LineDataSet dataSet = new LineDataSet(entries, "??????");
        dataSet.setLineWidth(3.0f);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setColor(Color.RED);
        dataSet.setCircleColor(Color.BLACK);
        dataSet.setDrawCircles(false);  //??????????????????
        dataSet.setDrawValues(false);//??????????????????????????????

        List<Entry> entriesy = new ArrayList<>();
        for (int i = 0; i < yList.size(); i++) {
            try {
                entriesy.add(new Entry(Float.parseFloat(yList.get(i)), 0));
            } catch (Exception e) {

            }
        }
        LineDataSet dataSet1 = new LineDataSet(entriesy, "");

        dataSet1.setLineWidth(0.1f);
        dataSet1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet1.setColor(Color.BLACK);
        dataSet1.setCircleColor(Color.BLACK);
        dataSet1.setDrawCircles(false);  //??????????????????
        dataSet1.setDrawValues(false);//??????????????????????????????

        dataSets.add(dataSet1);
        dataSets.add(dataSet);

        LineData lineData1 = new LineData(dataSets);
        chartOxygen.setData(lineData1);
        chartOxygen.invalidate(); // refresh
    }


    private void setBloodPressureNewData(List<BloodPressureEntity> datas) {
        //1.??????x??????y?????????
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

        List<Entry> entries1 = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            try {
                entries1.add(new Entry(i, Float.parseFloat(datas.get(i).getLowBloodPressure())));
            } catch (Exception e) {

            }
        }
        LineDataSet dataSet1 = new LineDataSet(entries1, "??????");
        dataSet1.setLineWidth(3.0f);
        dataSet1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet1.setColor(Color.RED);
        dataSet1.setCircleColor(Color.BLACK);
        dataSet1.setDrawCircles(false);  //??????????????????
        dataSet1.setDrawValues(false);//??????????????????????????????


        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            try {
                entries.add(new Entry(i, Float.parseFloat(datas.get(i).getHightBloodPressure())));
            } catch (Exception e) {

            }
        }
        LineDataSet dataSet = new LineDataSet(entries, "??????");
        dataSet.setLineWidth(3.0f);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setDrawCircles(false);  //??????????????????
        dataSet.setDrawValues(false);//??????????????????????????????


        List<Entry> entriesy = new ArrayList<>();
        for (int i = 0; i < yList.size(); i++) {
            try {
                entriesy.add(new Entry(Float.parseFloat(yList.get(i)), 0));
            } catch (Exception e) {

            }
        }
        LineDataSet dataSet2 = new LineDataSet(entriesy, "");

        dataSet2.setLineWidth(0.1f);
        dataSet2.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet2.setColor(Color.BLACK);
        dataSet2.setCircleColor(Color.BLACK);
        dataSet2.setDrawCircles(false);  //??????????????????
        dataSet2.setDrawValues(false);//??????????????????????????????


        dataSets.add(dataSet1);
        dataSets.add(dataSet2);
        dataSets.add(dataSet);

        LineData lineData = new LineData(dataSets);
        chartBloodPressure.setData(lineData);
        chartBloodPressure.invalidate(); // refresh
    }


    private void setTemperatureNewData(List<BodyTemperatureEntity> datas) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        //1.??????x??????y?????????
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            //20220120084438
            String time = datas.get(i).getTime();
            int timeIndex = Integer.parseInt(time.substring(time.length() - 6, time.length() - 2));
            float finalTime = timeIndex / 100 + (float) timeIndex % 100 / 60;
            try {
                entries.add(new Entry(finalTime, Float.parseFloat(datas.get(i).getLevel())));
            } catch (Exception e) {

            }

        }
        LineDataSet dataSet = new LineDataSet(entries, "??????");
        dataSet.setLineWidth(3.0f);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setColor(Color.RED);
        dataSet.setCircleColor(Color.BLACK);
        dataSet.setDrawCircles(false);  //??????????????????
        dataSet.setDrawValues(false);//??????????????????????????????


        List<Entry> entriesy = new ArrayList<>();
        for (int i = 0; i < yList.size(); i++) {
            try {
                entriesy.add(new Entry(Float.parseFloat(yList.get(i)), 0));
            } catch (Exception e) {

            }
        }
        LineDataSet dataSet1 = new LineDataSet(entriesy, "");

        dataSet1.setLineWidth(0.1f);
        dataSet1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet1.setColor(Color.BLACK);
        dataSet1.setCircleColor(Color.BLACK);
        dataSet1.setDrawCircles(false);  //??????????????????
        dataSet1.setDrawValues(false);//??????????????????????????????

        dataSets.add(dataSet1);
        dataSets.add(dataSet);

        LineData lineData = new LineData(dataSets);
        chartTemperature.setData(lineData);
        chartTemperature.invalidate(); // refresh

    }


    @OnClick({R.id.tv_update})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_update:
                //????????????
//                ((MainActivity)getActivity()).mWriteCommand.syncRateData(); //??????
//                ((MainActivity)getActivity()).mWriteCommand.syncAllSleepData(); //??????
//                ((MainActivity)getActivity()).mWriteCommand.syncAllTemperatureData(); //??????
//                ((MainActivity)getActivity()).mWriteCommand.syncAllBloodPressureData(); //??????
//                ((MainActivity)getActivity()).mWriteCommand.syncOxygenData(); //??????

//                ((MainActivity)getActivity()).mWriteCommand.queryCurrentTemperatureData();//????????????????????????????????????
//                ((MainActivity)getActivity()). mWriteCommand.startOxygenTest();//?????????
//                ((MainActivity)getActivity()).mWriteCommand.sendBloodPressureTestCommand(GlobalVariable.BLOOD_PRESSURE_TEST_START);//?????????
//                ((MainActivity)getActivity()).mWriteCommand.sendRateTestCommand(GlobalVariable.RATE_TEST_START);//?????????
//                ((MainActivity)getActivity()). mWriteCommand.

                syncData();


                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventbusReceiver(ConnectEvent event) {

        if (event.isConnect()) {
            syncData();
        }
    }

}
