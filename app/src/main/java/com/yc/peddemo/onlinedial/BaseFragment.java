package com.yc.peddemo.onlinedial;

import android.app.Activity;
import androidx.fragment.app.Fragment;



public class BaseFragment extends Fragment {


    private Activity activity;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity = getActivity();
    }

}
