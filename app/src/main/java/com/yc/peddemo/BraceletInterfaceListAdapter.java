package com.yc.peddemo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.yc.pedometer.info.BraceletInterfaceInfo;

import java.util.List;

public class BraceletInterfaceListAdapter extends BaseAdapter {
    private Context mContext;
    private List<BraceletInterfaceInfo> mWeartherInfoList;
    private String periodArray[];

//    private int [] resIDArray =new int []{R.drawable.icon_goal,R.drawable.icon_help_center,R.drawable.icon_hor_ver_screen,R.drawable.icon_message
//            ,R.drawable.icon_qq_remind,R.drawable.icon_qq_health,R.drawable.icon_weather_address,R.drawable.icon_walk,R.drawable.icon_run,R.drawable.icon_more_app};
    public BraceletInterfaceListAdapter(Context context,List<BraceletInterfaceInfo> list) {
        mContext = context;
        mWeartherInfoList = list;
        periodArray = mContext.getResources().getStringArray(R.array.bracelet_interface_text);
//        resIDArray = mContext.getResources().getIntArray(R.array.bracelet_interface_resid);
//        for (int i=0;i<periodArray.length;i++){
//            LogUtils.d("","periodArray["+i+"]="+periodArray[i]);
//        }
//        for (int i=0;i<resIDArray.length;i++){
//            LogUtils.d("","resIDArray["+i+"]="+resIDArray[i]);
//        }
    }

    public void updateData(List<BraceletInterfaceInfo> list) {
        mWeartherInfoList = list;
        notifyDataSetChanged();

    }


    @Override
    public int getCount() {
        if (mWeartherInfoList == null) {
            return 0;
        }
        return mWeartherInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        if (mWeartherInfoList == null) {
            return null;
        }
        return mWeartherInfoList.get(position);
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public void remove(int position) {
        if (mWeartherInfoList == null) {
            return;
        }
        mWeartherInfoList.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position,View view,ViewGroup parent) {
        final ViewHolder viewHolder;
        if (view == null) {
            view = View.inflate(mContext,R.layout.item_bracelet_interface,null);
            viewHolder = new ViewHolder();
            viewHolder.tv_name = (TextView) view.findViewById(R.id.tv_name);
//            viewHolder.icon_bracelet_interface = (ImageView) view.findViewById(R.id.icon_bracelet_interface);
            viewHolder.displayStatus = (CheckBox) view.findViewById(R.id.displayStatus);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }


        if (mWeartherInfoList != null) {
            BraceletInterfaceInfo info = new BraceletInterfaceInfo();
            info = mWeartherInfoList.get(position);
           final int whichInterface = info.getWhichInterface();
            boolean isDisplay = info.getDisplay();
            viewHolder.tv_name.setText(periodArray[whichInterface]);
            viewHolder.displayStatus.setChecked(isDisplay);
//            int resId =resIDArray[whichInterface];
//            viewHolder.icon_bracelet_interface.setImageResource(resId);
//            TempratureUtils.printLog("whichInterface =" + whichInterface + ",isDisplay =" + isDisplay+",文字="+periodArray[whichInterface]);
//            viewHolder.displayStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
//                    LogUtils.d("BandInterfaceSet","isAbleClick ="+isAbleClick);
//                    if (isAbleClick){
//                        mOnItemCheckBoxListener.onCheckBoxClick(position,whichInterface,isChecked);
//                    }
//
//                }
//            });
            viewHolder.displayStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemCheckBoxListener.onCheckBoxClick(position,whichInterface,viewHolder.displayStatus.isChecked());
                }
            });
        } else {
//            TempratureUtils.printLog("mWeartherInfoList == null");
        }

        return view;
    }

    private class ViewHolder {
        TextView tv_name;
//        ImageView icon_bracelet_interface;
        CheckBox displayStatus;
    }
    /**
     * 删除按钮的监听接口
     */
    public interface OnItemCheckBoxListener {
        void onCheckBoxClick(int itemPosition, int whichInterface, boolean isChecked);
    }

    private OnItemCheckBoxListener mOnItemCheckBoxListener;

    public void setOnItemCheckBoxClickListener(OnItemCheckBoxListener mOnItemCheckBoxListener) {
        this.mOnItemCheckBoxListener = mOnItemCheckBoxListener;
    }

}
