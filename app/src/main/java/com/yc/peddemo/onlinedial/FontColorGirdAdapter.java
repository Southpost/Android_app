package com.yc.peddemo.onlinedial;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.yc.peddemo.R;

public class FontColorGirdAdapter extends BaseAdapter {
    private Context mContext;
    public int[] fontColorIdList;
    private int pickerPosition = 0;
    public FontColorGirdAdapter(Context context,int[] fontColorIdList) {
        mContext = context;
        this.fontColorIdList = fontColorIdList;
    }
    public void setSeclection(int position) {
        pickerPosition = position;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return fontColorIdList.length;
    }

    @Override
    public Object getItem(int position) {
        return fontColorIdList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.dial_font_color_item, null);
            viewHolder = new ViewHolder();
            viewHolder.fontColor = convertView.findViewById(R.id.fontColor);
            viewHolder.fontColorBond = convertView.findViewById(R.id.fontColorBond);
            viewHolder.whiteFont = convertView.findViewById(R.id.whiteFont);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (((CustomDialGridView) parent).isOnMeasure) {
            //如果是onMeasure调用的就立即返回.解决多次执行
            return convertView;
        }
        if (position == fontColorIdList.length - 1) {
            viewHolder.fontColor.setImageResource(R.drawable.dial_more_color);
        } else {
            viewHolder.fontColor.setImageResource(fontColorIdList[position]);
        }
        if (position == 0) {
            viewHolder.whiteFont.setImageResource(R.drawable.color_circle_white_selector);
        } else {
            viewHolder.whiteFont.setImageResource(R.color.transparent);
        }
        if (pickerPosition == position) {
            viewHolder.fontColorBond.setImageResource(R.drawable.color_circle_choose_selector);
        } else {
            viewHolder.fontColorBond.setImageResource(R.color.transparent);
        }

        return convertView;
    }


    class ViewHolder {
        ImageView fontColor;
        ImageView whiteFont;
        ImageView fontColorBond;

    }
}
