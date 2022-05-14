package com.yc.peddemo.onlinedial;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.makeramen.roundedimageview.RoundedImageView;
import com.yc.peddemo.R;
import com.yc.pedometer.dial.DialZipPreInfo;
import com.yc.pedometer.dial.PicUtils;
import com.yc.pedometer.dial.Rgb;

import java.util.ArrayList;
import java.util.List;



public class CustomDialGirdAdapter extends BaseAdapter {
    private Context mContext;
    List<DialZipPreInfo> dalZipPreInfoList = new ArrayList<>();

    public CustomDialGirdAdapter(Context context, List<DialZipPreInfo> dalZipPreInfoList) {
        mContext = context;
        this.dalZipPreInfoList = dalZipPreInfoList;
    }

    public void setData(List<DialZipPreInfo> dalZipPreInfoList) {

        this.dalZipPreInfoList = dalZipPreInfoList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dalZipPreInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return dalZipPreInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.dial_custom_item, null);
            viewHolder = new ViewHolder();
            viewHolder.ivBiaoPan = convertView.findViewById(R.id.ivBiaoPan);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (((OnlineDialGridView) parent).isOnMeasure) {
            //如果是onMeasure调用的就立即返回.解决多次执行
            return convertView;
        }
//        viewHolder.ivBiaoPan.setImageBitmap(dalZipPreInfoList.get(position).getBitmap());
        viewHolder.ivBiaoPan.setImageBitmap(Rgb.getInstance().zoomBitmap(dalZipPreInfoList.get(position).getBitmap(), 1.35f, 1.35f));
        if (dalZipPreInfoList.get(position).getType().equals(PicUtils.DIAL_TYPE_CIRCLE)) {
            viewHolder.ivBiaoPan.setOval(true);
        } else {
            viewHolder.ivBiaoPan.setOval(false);
        }

        return convertView;
    }


    class ViewHolder {
        RoundedImageView ivBiaoPan;

    }
}
