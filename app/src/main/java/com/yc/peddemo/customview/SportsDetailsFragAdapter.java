package com.yc.peddemo.customview;

import android.os.Parcelable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SportsDetailsFragAdapter extends FragmentStatePagerAdapter {
    
    private ArrayList<Fragment> fragments ;
    private List<String> titleList;
    public SportsDetailsFragAdapter(FragmentManager fm){
        super(fm);
    }
    
    public SportsDetailsFragAdapter(FragmentManager fm,ArrayList<Fragment> fragments,List<String> titleList) {
        super(fm);
        this.fragments = fragments;
        this.titleList =titleList;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }
//    public Fragment getItem(int position) {
//        Fragment fragment = null;
//        if (fragments.size() > position) {
//        	fragment = fragments.get(position);
//            if (fragment != null) {
//                return fragment;
//            }
//        }
//
//        while (position>=fragments.size()) {
//        	fragments.add(null);
//        }
//        fragment = Fragment.newPage(pageList.get(position),position);
//        fragments.set(position, fragment);
//        return fragment;
//    }


    @Override
    public int getCount() {
        return fragments.size();
    }
    
	@Override
	public int getItemPosition(Object object) {
		// TODO Auto-generated method stub
		return PagerAdapter.POSITION_NONE;
	}
	@Override
	public Parcelable saveState() {
	    return null;  
	}
    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);//页卡标题
    }
}
