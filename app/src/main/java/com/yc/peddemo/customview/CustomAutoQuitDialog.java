package com.yc.peddemo.customview;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.yc.peddemo.R;


public class CustomAutoQuitDialog extends Dialog {

    public CustomAutoQuitDialog(Context context) {
        super(context);
    }

    public CustomAutoQuitDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context;
        private String message;
        private TextView tv_message;
        private ImageView icon ;
        public Builder(Context context) {
            this.context = context;
        }

        // public Builder setMessage(String message) {
        // this.message = message;
        // return this;
        // }
        // public Builder setMessage(int message) {
        // this.message = (String) context.getText(message);
        // return this;
        // }

        public Builder setTitle(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        public Builder setTitle(String message) {
            this.message = message;
            return this;
        }

        CustomAutoQuitDialog dialog;
        private int mDuration = 1000 ;

        public CustomAutoQuitDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            dialog = new CustomAutoQuitDialog(context, R.style.shareDialog);
            View view = inflater.inflate(
                    R.layout.custom_auto_quit_dialog, null);
            dialog.addContentView(view, new LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            tv_message = view.findViewById(R.id.message);
            icon = view.findViewById(R.id.icon);
            if (message != null) {
                tv_message.setText(message);
            }
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.setContentView(view);
            alphaDismiss(view, dialog);
            return dialog;
        }

        public Builder dismissDialog() {
            dialog.dismiss();
            return this;
        }

        private Animation playAmin(View view) {
            Animation operatingAnim = AnimationUtils.loadAnimation(context,
                    R.anim.small_to_big);
//			operatingAnim =AnimationUtils.makeInAnimation(context, true);
//			operatingAnim =AnimationUtils.makeOutAnimation(context, true);
            LinearInterpolator lin = new LinearInterpolator();
            operatingAnim.setInterpolator(lin);
            operatingAnim.setDuration(mDuration);

            if (operatingAnim != null) {
                view.clearAnimation();
                view.startAnimation(operatingAnim);
            }
            return operatingAnim;
        }

        public void setAnimDuration(int duration) {
            mDuration = duration ;
        }
        public void setMessageVisible(boolean isVisible) {
            if (isVisible) {
                if (tv_message != null) {
                    tv_message.setVisibility(View.VISIBLE);
                }
            }else {
                if (tv_message != null) {
                    tv_message.setVisibility(View.GONE);
                }
            }
        }
        public void setMessage(String message) {

            if (tv_message != null) {
                tv_message.setText(message);
            }

        }
        public void setImageRes(int resId) {
            if (icon != null) {
                icon.setImageResource(resId);
            }
        }
        public void alphaDismiss(View view,final CustomAutoQuitDialog dialog) {
//			AlphaAnimation aa = new AlphaAnimation(0.5f, 1.0f);
//			aa.setDuration(2000);
//			view.startAnimation(aa);
            playAmin(view).setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    dialog.dismiss();
                }
            });

        }

        public boolean isShowing() {
            return dialog.isShowing();
        }

    }
}
