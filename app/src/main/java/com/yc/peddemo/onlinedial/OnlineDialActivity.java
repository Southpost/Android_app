package com.yc.peddemo.onlinedial;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.yc.peddemo.R;
import com.yc.peddemo.customview.SportsDetailsFragAdapter;

import java.util.ArrayList;
import java.util.List;

public class OnlineDialActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_dial);
        mContext = getApplicationContext();
        initView();
    }

    private void initView() {
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(this);
        mViewPager = findViewById(R.id.main_viewpager);
        List<String> titleList = new ArrayList<>();
        ArrayList<Fragment> fragments = new ArrayList<>();
        titleList.add(getStringResources(R.string.watchCenter));
        titleList.add(getStringResources(R.string.mywatch));
        titleList.add(getStringResources(R.string.dial_custom));
        WatchCenterFragment mFragmentOnlineDialCentre = WatchCenterFragment.getInstance();
        CustomWatchFragment mCustomWatchFragment = CustomWatchFragment.getInstance();
        MyWatchFragment mFragmentOnlineDialMine = MyWatchFragment.getInstance();
        fragments.add(mFragmentOnlineDialCentre);
        fragments.add(mFragmentOnlineDialMine);
        fragments.add(mCustomWatchFragment);

        TabLayout mTab = findViewById(R.id.main_tab);
        for (int i = 0; i < titleList.size(); i++) {
            mTab.addTab(mTab.newTab().setText(titleList.get(i)));
        }
        mTab.setupWithViewPager(mViewPager);
        int size = titleList.size();
        if (size > 4) {
            mTab.setTabMode(TabLayout.MODE_SCROLLABLE);
        } else {
            mTab.setTabMode(TabLayout.MODE_FIXED);
        }
        mTab.setTabMode(TabLayout.MODE_SCROLLABLE);
        SportsDetailsFragAdapter mAdapter = new SportsDetailsFragAdapter(getSupportFragmentManager(), fragments, titleList);
        mViewPager.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }

    private String getStringResources(int id) {
        return mContext.getResources().getString(id);
    }

}
