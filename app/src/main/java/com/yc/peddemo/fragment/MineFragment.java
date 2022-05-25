package com.yc.peddemo.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yc.peddemo.R;
import com.yc.peddemo.SettingActivity;
import com.yc.peddemo.onlinedial.BaseFragment;
import com.yc.pedometer.utils.SPUtil;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * desc:
 * author: lei
 * date: 2022/3/10
 **/

//蓝牙个人信息展示
public class MineFragment extends BaseFragment {

    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_height)
    TextView tvHeight;
    @BindView(R.id.tv_weight)
    TextView tvWeight;
    @BindView(R.id.tv_sex)
    TextView tvSex;
    @BindView(R.id.tv_plan)
    TextView tvPlan;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    private View rootView;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_mine, container, false);
        ButterKnife.bind(this, rootView);
        initView();
        initData();
        return rootView;
    }

    private void initData() {
        sharedPreferences = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        rootView.findViewById(R.id.iv_back).setVisibility(View.GONE);
        tvName.setText(sharedPreferences.getString("name", ""));
        tvPlan.setText(sharedPreferences.getString("step_plan", 8000+"")+"步");
        tvHeight.setText(SPUtil.getInstance(getActivity()).getPersonageHeight());
        tvWeight.setText(SPUtil.getInstance(getActivity()).getPersonageWeight());
        tvSex.setText(SPUtil.getInstance(getActivity()).getPersonageGender() ? "女" : "男");
    }

    private void initView() {
        tvTitle.setText("个人中心");
    }

    @OnClick({R.id.rl_name, R.id.rl_height, R.id.rl_weight, R.id.rl_sex, R.id.rl_plan})
    public void onClick(View view) {
        Intent intent = new Intent(getContext(), SettingActivity.class);
        switch (view.getId()) {
            case R.id.rl_name:
                intent.putExtra("type", "name");
                break;
            case R.id.rl_height:
                intent.putExtra("type", "height");
                break;
            case R.id.rl_weight:
                intent.putExtra("type", "weight");
                break;
            case R.id.rl_sex:
                intent.putExtra("type", "sex");
                break;
            case R.id.rl_plan:
                intent.putExtra("type", "plan");
                break;
        }
        startActivityForResult(intent, 0);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initData();
    }
}