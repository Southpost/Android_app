package com.yc.peddemo.customview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.yc.peddemo.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class OkSetDialog extends Dialog {


    Context context;

//    String message;
    @BindView(R.id.txtCancle)
    TextView txtCancle;
    @BindView(R.id.txtSure)
    TextView txtSure;
    public interface OnItemClick{
        void onOk();
        void onCancel();
    }

    public OnItemClick onItemClick;
    public OkSetDialog(Context context,OnItemClick onItemClick) {
        super(context, R.style.ActionSheetDialogStyle);
            this.context = context;
            this.onItemClick =onItemClick;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setCanceledOnTouchOutside(false);
        Window dialogWindow = this.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        init();
//        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//        lp.width = DensityUtil.dp2px(getContext(),240);
//        lp.height = DensityUtil.dp2px(getContext(),108);
//        dialogWindow.setAttributes(lp);
    }


    public void setOnClickItem(View.OnClickListener okListen, View.OnClickListener cancelListen) {

        txtCancle.setOnClickListener(cancelListen);
        txtSure.setOnClickListener(okListen);
    }

    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_sure, null);
        setContentView(view);
        ButterKnife.bind(this, view);

        txtSure.setOnClickListener(v->{
            onItemClick.onOk();
            this.dismiss();
        });

        txtCancle.setOnClickListener(v->{
            cancel();
            onItemClick.onCancel();
        });
    }


}
