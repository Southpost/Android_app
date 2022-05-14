package com.yc.peddemo.customview;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.yc.peddemo.R;


public class CustomCardProgressDialog extends Dialog {

	public CustomCardProgressDialog(Context context) {
		super(context);
	}

	public CustomCardProgressDialog(Context context, int theme) {
		super(context, theme);
	}

	public static class Builder {
		private Context context;
		private String message;
		private TextView schedule;private TextView tip;
		private TextView tv_message;
		private ImageView icon;
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

		CustomCardProgressDialog dialog;

		public CustomCardProgressDialog create() {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			dialog = new CustomCardProgressDialog(context, R.style.shareDialog);
			View layout = inflater.inflate(
					R.layout.custom_card_progress_dialog, null);
			dialog.addContentView(layout, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			tv_message =  layout.findViewById(R.id.message);
			schedule =  layout.findViewById(R.id.schedule);
			tip =  layout.findViewById(R.id.tip);
			icon =  layout.findViewById(R.id.icon);
			if (message != null) {
				tv_message.setText(message);
			}
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(false);
			dialog.setContentView(layout);
			playAmin(icon);
			return dialog;
		}

		public Builder dismissDialog() {
			dialog.dismiss();
			return this;
		}

		private void playAmin(ImageView icon) {
			Animation operatingAnim = AnimationUtils.loadAnimation(context,
					R.anim.progress_dialog_amin);
			LinearInterpolator lin = new LinearInterpolator();
			operatingAnim.setInterpolator(lin);
			if (operatingAnim != null) {
				icon.clearAnimation();
				icon.startAnimation(operatingAnim);
			}
		}
		public void stopAmin() {
			if (icon != null) {
				icon.clearAnimation();
			}
		}
		public void setSchedule(int progress) {

			if (schedule != null) {
				schedule.setText(progress+"%");
			}
		}
		public void setSchedule(String progress) {

			if (schedule != null) {
				schedule.setText("");
			}
		}
		public void setMessage(String message) {

			if (tv_message != null) {
				tv_message.setText(message);
			}

		}
		public void setIcon(boolean ok) {

			if (icon != null) {
				if (ok) {
					icon.setImageResource(R.drawable.icon_gou);
				} else {
					icon.setImageResource(R.drawable.icon_cha);
				}

			}
		}
		public boolean isShowing() {
			return dialog.isShowing();
		}
		public void setTipVisible(boolean visible) {

			if (tip != null) {
				if (visible) {
					 tip.setVisibility(View.VISIBLE);
				}else {
					 tip.setVisibility(View.GONE);
				}
			}
		}
	}
}
