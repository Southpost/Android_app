package com.yc.peddemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.adapter.ArrayWheelAdapter;
import com.contrarywind.listener.OnItemSelectedListener;
import com.contrarywind.view.WheelView;
import com.yc.pedometer.utils.SPUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.wheelview)
    WheelView wheelView;
    @BindView(R.id.et_content)
    EditText etContent;
    private String type;
    private String planIndex;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);
        type = getIntent().getStringExtra("type");
        switch (type) {
            case "name":
                this.setTitle("设置姓名");
                setWeelView(false);
                break;
            case "height":
                this.setTitle("设置身高");
                setWeelView(false);
                break;
            case "weight":
                this.setTitle("设置体重");
                setWeelView(false);
                break;
            case "sex":
                this.setTitle("设置性别");
                setWeelView(false);
                break;
            case "plan":
                this.setTitle("设置目标步数");
                setWeelView(true);
                break;
        }
        wheelView.setCyclic(false);

        List<String> mOptionsItems = new ArrayList<>();
        for (int i = 2000; i <= 30000; i += 1000) {
            mOptionsItems.add(i+"");
        }

        wheelView.setAdapter(new ArrayWheelAdapter(mOptionsItems));
        wheelView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                planIndex = mOptionsItems.get(index);
//                Toast.makeText(SettingActivity.this, planIndex, Toast.LENGTH_SHORT).show();
            }
        });
        wheelView.setCurrentItem(6);
    }

    private void setWeelView(boolean showWeelView) {
        if (showWeelView) {
            wheelView.setVisibility(View.VISIBLE);
            etContent.setVisibility(View.GONE);
        } else {
            wheelView.setVisibility(View.GONE);
            etContent.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btn_save)
    public void onClick(View view) {

        String contentText = etContent.getText().toString();
        if (contentText.isEmpty() && !"plan".equals(type)) {
            Toast.makeText(this, "请输入内容...", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (type) {
            case "name":
                sharedPreferences.edit().putString("name", contentText).commit();
                break;
            case "height":
                SPUtil.getInstance(this).setPersonageHeight(contentText);
                break;
            case "weight":
                SPUtil.getInstance(this).setPersonageWeight(contentText);
                break;
            case "sex":
                SPUtil.getInstance(this).setPersonageGender("女".contentEquals(contentText) ? true : false);
                break;
            case "plan":
                sharedPreferences.edit().putString("step_plan", planIndex).commit();
                break;
        }
        finish();
    }
}