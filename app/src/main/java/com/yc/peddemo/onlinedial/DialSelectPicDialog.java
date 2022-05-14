package com.yc.peddemo.onlinedial;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.yc.peddemo.R;


public class DialSelectPicDialog extends Dialog implements View.OnClickListener {

    private final static String TAG = "BodySelectDialog";

    public DialSelectPicDialog(Context context) {
        super(context);
    }

    public DialSelectPicDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    public void onClick(View v) {

    }

    public static class Builder {
        private Context context;
        private TextView photo, albumon;
        private Button cancel;
        private OnClickListener albumonClickListener;
        private OnClickListener photoClickListener;
        private OnClickListener cancelClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setPhotoButton(OnClickListener listener) {
            this.photoClickListener = listener;
            return this;
        }

        public Builder setAlbumonButton(OnClickListener listener) {
            this.albumonClickListener = listener;
            return this;
        }

        public Builder setCancelButton(OnClickListener listener) {
            this.cancelClickListener = listener;
            return this;
        }

        DialSelectPicDialog dialog;

        public DialSelectPicDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            dialog = new DialSelectPicDialog(context, R.style.BottomAnimDialogStyle);
            View layout = inflater.inflate(R.layout.dial_select_pic_dialog,
                    null);
            dialog.addContentView(layout, new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            photo = layout.findViewById(R.id.photo);
            albumon = layout.findViewById(R.id.albumon);
            cancel = layout.findViewById(R.id.cancel);

            if (photoClickListener != null) {
                photo.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        photoClickListener.onClick(dialog,
                                DialogInterface.BUTTON_POSITIVE);
                    }
                });
            }
            if (albumonClickListener != null) {
                albumon.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        albumonClickListener.onClick(dialog,
                                DialogInterface.BUTTON_NEGATIVE);
                    }
                });
            }
            if (cancelClickListener != null) {
                cancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        cancelClickListener.onClick(dialog,
                                DialogInterface.BUTTON_NEGATIVE);
                    }
                });
            }

            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.setContentView(layout);
            return dialog;
        }

        public boolean isShowing() {
            return dialog.isShowing();
        }


    }


}
