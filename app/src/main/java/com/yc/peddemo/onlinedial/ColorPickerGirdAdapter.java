package com.yc.peddemo.onlinedial;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.yc.peddemo.R;



public class ColorPickerGirdAdapter extends BaseAdapter {
    private Context mContext;
    public int[] colorPickerList;
    private int pickerPosition = 0;
    public ColorPickerGirdAdapter(Context context, int[] colorPickerList) {
        mContext = context;
        this.colorPickerList = colorPickerList;
//        LogUtils.d("FontColorGirdAdapter", "FontColorGirdAdapter构造方法");
    }
    public void setSeclection(int position) {
        pickerPosition = position;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
//        LogUtils.d("FontColorGirdAdapter", "fontColorIdList.length =" + fontColorIdList.length);
        return colorPickerList.length;
    }

    @Override
    public Object getItem(int position) {
        return colorPickerList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.dial_color_picker_item, null);
            viewHolder = new ViewHolder();
            viewHolder.fontColor = convertView.findViewById(R.id.fontColor);
            viewHolder.fontColor2 = convertView.findViewById(R.id.fontColor2);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (((CustomDialGridView) parent).isOnMeasure) {
            //如果是onMeasure调用的就立即返回.解决多次执行
            return convertView;
        }


//        LogUtils.d("FontColorGirdAdapter", "fontColor设置背景 position ="+position);
//        LogUtils.d("FontColorGirdAdapter", "fontColor = " + viewHolder.fontColor + ",设置背景"+fontColorIdList[position]);
//        viewHolder.fontColor.setBackgroundColor(mContext.getResources().getColor(fontColorIdList[position]));
        if (pickerPosition == position) {
            viewHolder.fontColor.setImageResource(R.drawable.color_picker_choose_selector);
            viewHolder.fontColor2.setImageResource(R.drawable.color_picker_choose_selector2);
        }else {
            viewHolder.fontColor.setImageResource(R.color.transparent);
            viewHolder.fontColor2.setImageResource(R.color.transparent);
        }
        viewHolder.fontColor.setBackgroundColor(colorPickerList[position]);
//        LogUtils.d("FontColorGirdAdapter", "fontColor设置背景后");
//            viewHolder.fontColor.setBackgroundColor(mContext.getResources().getColor(fontColorIdList[position]));

        return convertView;
    }


    class ViewHolder {
        ImageView fontColor;
        ImageView fontColor2;

    }
}
