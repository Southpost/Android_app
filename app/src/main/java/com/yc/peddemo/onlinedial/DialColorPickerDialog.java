package com.yc.peddemo.onlinedial;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.yc.peddemo.R;


public class DialColorPickerDialog extends Dialog implements View.OnClickListener {


    public DialColorPickerDialog(Context context) {
        super(context);
    }

    public DialColorPickerDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    public void onClick(View v) {

    }

    public static class Builder {
        private Context context;
        private int pickerPosition = 0;
        private ImageView cancel;
        private CustomDialGridView gvColorPicker;
        private ColorPickerGirdAdapter mColorPickerGirdAdapter;
        public int[] colorPickerList = null;

        private OnClickListener chooseClickListener;
        private OnClickListener cancelClickListener;

        public Builder(Context context, int[] colorPickerList) {
            this.context = context;
            this.colorPickerList = colorPickerList;
        }

        public Builder setChooseButton(OnClickListener listener) {
            this.chooseClickListener = listener;
            return this;
        }


        public Builder setCancelButton(OnClickListener listener) {
            this.cancelClickListener = listener;
            return this;
        }

        DialColorPickerDialog dialog;

        public DialColorPickerDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            dialog = new DialColorPickerDialog(context, R.style.BottomAnimDialogStyle);
            View layout = inflater.inflate(R.layout.dial_color_picker_dialog,
                    null);
            LayoutParams lp = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            dialog.addContentView(layout, lp);
// ??? dialog????????????
            Window window = dialog.getWindow();
// ??? DecorView ????????? padding ??????????????? DecorView ???????????????????????????
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams layoutParams = window.getAttributes();
// ????????????
            layoutParams.dimAmount = 0;//?????????????????????
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
// ??? DecorView ????????????????????????????????????????????? Dialog ???????????????????????????????????????????????? padding????????????????????????
            window.getDecorView().setBackgroundColor(Color.TRANSPARENT);
// ?????????????????????

            cancel = layout.findViewById(R.id.cancel);
            gvColorPicker = layout.findViewById(R.id.gvColorPicker);


            if (cancelClickListener != null) {
                cancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        cancelClickListener.onClick(dialog,
                                DialogInterface.BUTTON_NEGATIVE);
                    }
                });
            }
            mColorPickerGirdAdapter = new ColorPickerGirdAdapter(context, colorPickerList);
            gvColorPicker.setAdapter(mColorPickerGirdAdapter);

            gvColorPicker.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    pickerPosition = position;
                    mColorPickerGirdAdapter.setSeclection(pickerPosition);
                    chooseClickListener.onClick(dialog, position);
                }
            });

            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            dialog.setContentView(layout);
            return dialog;
        }

        public boolean isShowing() {
            return dialog.isShowing();
        }

        public void setSeclection(int position) {
            pickerPosition = position;
            mColorPickerGirdAdapter.setSeclection(pickerPosition);
        }
    }


}
