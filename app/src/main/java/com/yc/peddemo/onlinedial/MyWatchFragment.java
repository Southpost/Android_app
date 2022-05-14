package com.yc.peddemo.onlinedial;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.makeramen.roundedimageview.RoundedImageView;
import com.yc.peddemo.DialogType;
import com.yc.peddemo.R;
import com.yc.peddemo.customview.OkSetDialog;
import com.yc.peddemo.customview.ShowAlphaDialog;
import com.yc.peddemo.onlinedial.util.BitmapUtil;
import com.yc.pedometer.dial.HttpRequestor;
import com.yc.pedometer.dial.OnlineDialUtil;
import com.yc.pedometer.dial.PicUtils;
import com.yc.pedometer.dial.PostUtil;
import com.yc.pedometer.dial.UIFile;
import com.yc.pedometer.dial.WatchChanged;
import com.yc.pedometer.utils.BLEVersionUtils;
import com.yc.pedometer.utils.SPUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MyWatchFragment extends BaseFragment {

    Unbinder unbinder;
    @BindView(R.id.txtEdit)
    TextView txtEdit;

    boolean isEditMode = false;
    @BindView(R.id.xxListView)
    GridView xxListView;
    @BindView(R.id.no_dial_tip)
    RelativeLayout no_dial_tip;
    WatchAdapter musicListAdapter;
    List<UIFile> watchList = new ArrayList<>();
    private static MyWatchFragment instance = null;

    public static MyWatchFragment getInstance() {
        if (instance == null) {
            instance = new MyWatchFragment();
        }
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_mybiaopan, null);
        unbinder = ButterKnife.bind(this, layout);
        EventBus.getDefault().register(this);
        loadData();
        musicListAdapter = new WatchAdapter(getContext(), watchList);
        xxListView.setAdapter(musicListAdapter);
        isNoHasDial();
        return layout;
    }

    @Subscribe
    public void wach(WatchChanged e) {

        if (e != null && e.changePos == 0) {
            loadData();
            musicListAdapter.notifyDataSetChanged();
            isNoHasDial();
        }
    }

    private void loadData() {

        List<UIFile> files = stringToUIFileList(SPUtil.getInstance(getContext()).getLocalWatchList());
        if (files == null) {
            watchList = new ArrayList<>();
        } else {
            watchList = files;
        }
    }

    private List<UIFile> stringToUIFileList(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        } else {
            List<UIFile> uiFileList = new Gson().fromJson(json, new TypeToken<List<UIFile>>() {
            }.getType());
            if (NetworkUtils.getInstance(getContext()).isNetworkAvailable()) {
                deleteLocalWatchIfServerDelete(uiFileList);
            }

            return uiFileList;
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    boolean haveSelected() {
        for (int i = 0, len = watchList.size(); i < len; i++) {
            if (watchList.get(i).isSelect) {
                return true;
            }
        }
        return false;
    }

    @OnClick(R.id.txtEdit)
    public void onViewClicked() {

        isEditMode = !isEditMode;
        if (isEditMode) { // 点击编辑后的状态
            txtEdit.setText(getString(R.string.goal_done));
            musicListAdapter.notifyDataSetChanged();
            isNoHasDial();
        } else { // 点击完成后的状态 ，保存结果状态
            txtEdit.setText(getString(R.string.edit));
            if (haveSelected()) {
                OkSetDialog okSetDialog = new OkSetDialog(getActivity(), new OkSetDialog.OnItemClick() {
                    @Override
                    public void onOk() {
                        for (int i = 0, len = watchList.size(); i < len; i++) {
                            if (watchList.get(i).isSelect) {
                                watchList.remove(i);
                                len--;
                                i--;
                            }
                        }
                        SPUtil.getInstance(getContext()).setLocalWatchList(new Gson().toJson(watchList));
                        musicListAdapter.notifyDataSetChanged();
                        isNoHasDial();
                    }

                    @Override
                    public void onCancel() {
                        isEditMode = false;
                        musicListAdapter.notifyDataSetChanged();
                        isNoHasDial();
                    }
                });

                okSetDialog.show();
            } else {
                musicListAdapter.notifyDataSetChanged();
                isNoHasDial();
            }

        }
    }

    public class WatchAdapter extends BaseAdapter {
        private Context mContext;

        public WatchAdapter(Context context, List<UIFile> mgroupList) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return watchList.size();
        }

        @Override
        public Object getItem(int position) {
            return watchList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;

            UIFile uiFile = watchList.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_me_watch, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (((OnlineDialGridView) parent).isOnMeasure) {
                //如果是onMeasure调用的就立即返回
                return convertView;
            }
            if (NetworkUtils.getInstance(getContext()).isNetworkAvailable()) {
                BitmapUtil.loadBitmap(getContext(), uiFile.getPreview(), viewHolder.ivBiaoPan);
            }
            if (uiFile.getShape() == (Integer.valueOf(PicUtils.DIAL_TYPE_CIRCLE))) {
                viewHolder.ivBiaoPan.setOval(true);
            } else {
                viewHolder.ivBiaoPan.setOval(false);
            }

            if (isEditMode) {
                viewHolder.ivDel.setVisibility(View.VISIBLE);
                if (uiFile.isSelect) {
                    viewHolder.ivDel.setBackgroundResource(R.drawable.watch_select);
                } else {
                    viewHolder.ivDel.setBackgroundResource(R.drawable.bg_notselect);
                }
            } else {
                viewHolder.ivDel.setVisibility(View.GONE);
            }

            viewHolder.rlbg.setOnClickListener(v -> {
                if (isEditMode) {
                    uiFile.isSelect = !uiFile.isSelect;
                    notifyDataSetChanged();
                } else {
                    if (!SPUtil.getInstance(mContext).getBleConnectStatus()) {
                        ShowAlphaDialog.show(DialogType.DIALOG_DISCONNECT, mContext);
                        return;
                    }
                    WatchTransferDialog picUpgradeSetDialog = new WatchTransferDialog(getActivity(), WatchTransferDialog.Status.UPGRADING
                            , uiFile, () -> {

                    });
                    WatchCenterFragment.getInstance().setUIFile(uiFile);
                    OnlineDialUtil.LogI("MyWatchFragment mUIFile 赋值 " + uiFile + "，mUIFile.getTitle() =" + uiFile.getTitle());
                    picUpgradeSetDialog.show();
                }
            });

            return convertView;
        }


        class ViewHolder {
            @BindView(R.id.ivBiaoPan)
            RoundedImageView ivBiaoPan;
            @BindView(R.id.ivDel)
            ImageView ivDel;
            @BindView(R.id.rlbg)
            RelativeLayout rlbg;

            ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }

    }

    private void isNoHasDial() {
        if (watchList == null || watchList.size() == 0) {
            if (no_dial_tip == null) {
                return;
            }
            no_dial_tip.setVisibility(View.VISIBLE);
        } else {
            if (no_dial_tip == null) {
                return;
            }
            no_dial_tip.setVisibility(View.GONE);
        }
    }
    /**
     * 我的表盘中，同步删除服务器上已删除的表盘，避免服务器上删除了该表盘而我的表盘中还存在
     * @param files
     * @return
     */
    private void deleteLocalWatchIfServerDelete(List<UIFile> files) {
        JSONArray ids = new JSONArray();
        for (int i = 0; i < files.size(); i++) {
            int id = files.get(i).getId();
            ids.put(id);
        }
        OnlineDialUtil.LogI("local ids = " + ids.toString());
        String btname = BLEVersionUtils.getInstance(getContext()).getBleVersionName();
        String jsonStr = PostUtil.getInstance(getContext()).issetWatchHashMap(btname, ids);
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("content", jsonStr);
        OnlineDialUtil.LogI("hashMap:" + new Gson().toJson(hashMap));
        HttpRequestor httpRequestor = new HttpRequestor();
        try {
            Flowable.just(0).subscribeOn(Schedulers.io()).map(integer -> {
                String json = httpRequestor.doPost(PostUtil.GET_WATCH_ISSETWATCH, hashMap);
                JSONObject jsonObject = new JSONObject(json);
                OnlineDialUtil.LogI("issetWatch jsonObject: " + jsonObject);
                int flag = jsonObject.getInt("flag");
                if (flag == 1) {
                    return null;
                }
                return jsonObject;
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(v -> {
                if (v != null) {
                    JSONArray jsonArray = v.getJSONArray("ids");
                    if (jsonArray != null)
                    for (int i = 0; i < jsonArray.length(); i++) {
                        OnlineDialUtil.LogI("不存在的ids = " + jsonArray.get(i));
                        for(int j = 0, len = watchList.size(); j < len; j++){
                            if (watchList.get(j).getId() == (int) jsonArray.get(i)) {
                                OnlineDialUtil.LogI("删除 = " + watchList.get(j).getId());
                                watchList.remove(j);
                                len--;
                                j--;

                            }
                        }
                    }
                    SPUtil.getInstance(getContext()).setLocalWatchList(new Gson().toJson(watchList));
                    musicListAdapter.notifyDataSetChanged();
                    isNoHasDial();

                }
            }, v -> {
//                Utils.showToast(getContext(), getString(R.string.confire_is_network_available));
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
