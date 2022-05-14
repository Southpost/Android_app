package com.yc.peddemo.customview;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.yc.peddemo.R;


public class NoTitleDoubleDialog extends Dialog {

	public NoTitleDoubleDialog(Context context) {
		super(context);
	}

	public NoTitleDoubleDialog(Context context, int theme) {
		super(context, theme);
	}

	public static class Builder {
		private Context context;
		private TextView tv_comment;
		private Button confirm,cancel;
		private View line;
		private String positiveButtonText;
		private String negativeButtonText;
		private OnClickListener positiveButtonClickListener;
		private OnClickListener negativeButtonClickListener;

		public Builder(Context context) {
			this.context = context;
		}

		public Builder setPositiveButton(int positiveButtonText,
				OnClickListener listener) {
			this.positiveButtonText = (String) context
					.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		public Builder setPositiveButton(String positiveButtonText,
                                         OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		public Builder setNegativeButton(int negativeButtonText,
				OnClickListener listener) {
			this.negativeButtonText = (String) context
					.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}

		public Builder setNegativeButton(String negativeButtonText,
                                         OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		NoTitleDoubleDialog dialog;

		public NoTitleDoubleDialog create() {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			dialog = new NoTitleDoubleDialog(context, R.style.shareDialog);
			View layout = inflater.inflate(R.layout.no_title_double_dialog,
					null);
			dialog.addContentView(layout, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            tv_comment =  layout.findViewById(R.id.tv_comment);
            confirm =  layout.findViewById(R.id.confirm);
            cancel = layout.findViewById(R.id.cancel);
            line = layout.findViewById(R.id.line);
            if (negativeButtonText != null) {
                cancel.setText(negativeButtonText);
            } else {
                cancel.setVisibility(View.GONE);
            }
            if (negativeButtonClickListener != null) {
                cancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        negativeButtonClickListener.onClick(dialog,
                                DialogInterface.BUTTON_NEGATIVE);
                    }
                });
            }

            if (positiveButtonText != null) {
                confirm.setText(positiveButtonText);
            }else {
                confirm.setVisibility(View.GONE);
            }
            if (positiveButtonClickListener != null) {
                confirm.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        positiveButtonClickListener.onClick(dialog,
                                DialogInterface.BUTTON_POSITIVE);
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
		public void setMessage(String message) {
			if (tv_comment != null) {
				tv_comment.setText(message);
			}

		}

        public void setNegativeVisibility(boolean v) {
            if (cancel != null) {
                if (v) {
                    cancel.setVisibility(View.VISIBLE);
                    line.setVisibility(View.VISIBLE);
                } else {
                    cancel.setVisibility(View.GONE);
                    line.setVisibility(View.GONE);
                }
            }
        }
	}
}
