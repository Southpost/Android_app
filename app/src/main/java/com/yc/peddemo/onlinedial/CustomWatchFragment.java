package com.yc.peddemo.onlinedial;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.yc.pedometer.utils.LogUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yc.peddemo.R;
import com.yc.pedometer.dial.DialZipInfo;
import com.yc.pedometer.dial.DialZipPreInfo;
import com.yc.pedometer.dial.FileUtil;
import com.yc.pedometer.dial.HttpRequestor;
import com.yc.pedometer.dial.OnlineDialUtil;
import com.yc.pedometer.dial.PicUtils;
import com.yc.pedometer.dial.PostUtil;
import com.yc.pedometer.dial.Rgb;
import com.yc.pedometer.dial.ZipUtils;
import com.yc.pedometer.sdk.UTESQLOperate;
import com.yc.pedometer.utils.BLEVersionUtils;
import com.yc.pedometer.utils.SPUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CustomWatchFragment extends BaseFragment {
    private final String TAG = "CustomWatchFragment";

    @BindView(R.id.xxListView)
    GridView xxListView;
    Context mContext;
    CustomDialGirdAdapter mCustomDialGirdAdapter;
    public List<DialZipPreInfo> dalZipPreInfoList = new ArrayList<>();
    List<DialZipInfo> watchList = new ArrayList<>();
    private static CustomWatchFragment instance = null;

    public static CustomWatchFragment getInstance() {
        if (instance == null) {
            instance = new CustomWatchFragment();
        }
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_custom_watch, null);
        mContext = getActivity();
        ButterKnife.bind(this, layout);
        dalZipPreInfoList.clear();

        folderDial = mContext.getFilesDir().getAbsolutePath() + File.separator + "DialCustom";
        mCustomDialGirdAdapter = new CustomDialGirdAdapter(getContext(), dalZipPreInfoList);
        xxListView.setAdapter(mCustomDialGirdAdapter);
        xxListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, CustomDialActivity.class);
                intent.putExtra(PicUtils.DIAL_PATH_SD_KEY, dalZipPreInfoList.get(position).getFolderDial());
                intent.putExtra(PicUtils.DIAL_PATH_WHERE_KEY, dalZipPreInfoList.get(position).getPathStatus());
                intent.putExtra(PicUtils.DIAL_TYPE_KEY, dalZipPreInfoList.get(position).getType());
                startActivity(intent);
            }
        });
        loadData();
        return layout;
    }

    private void loadlocalData(String type, String dpi) {
        DialZipPreInfo mDialZipPreInfo = new DialZipPreInfo();
        if (type.equals(PicUtils.DIAL_TYPE_CIRCLE) && dpi.equals(PicUtils.DIAL_DPI_240x240)
                || type.equals(PicUtils.DIAL_TYPE_SQUARE) && dpi.equals(PicUtils.DIAL_DPI_240x240)
                || type.equals(PicUtils.DIAL_TYPE_SQUARE) && dpi.equals(PicUtils.DIAL_DPI_320x360)) {//assets ????????????????????????
            PicUtils.getInstance().setAssetDialPathType(type,dpi);
            mDialZipPreInfo.setId("" + 0);
            mDialZipPreInfo.setPathStatus(PicUtils.PATH_STATUS_ASSETS);
            mDialZipPreInfo.setType(type);
            mDialZipPreInfo.setBitmap(PicUtils.getInstance().getDefaultPreviewAssetPath(mContext));
            dalZipPreInfoList.add(mDialZipPreInfo);
        }
        List<DialZipPreInfo> list = UTESQLOperate.getInstance(mContext).queryAllCustomDial(type, dpi);
        for (int i = 0; i < list.size(); i++) {
            Rgb.LogD("??????????????????" + list.get(i).getId());
            mDialZipPreInfo = new DialZipPreInfo();
            mDialZipPreInfo = list.get(i);
            mDialZipPreInfo.setBitmap(PicUtils.getInstance().getDefaultPreviewSDPath(list.get(i).getFolderDial()));
            dalZipPreInfoList.add(mDialZipPreInfo);
        }
        Collections.sort(dalZipPreInfoList);//list????????????
        for (int i = 0; i < dalZipPreInfoList.size(); i++) {
            Rgb.LogD("?????????????????????????????????" + dalZipPreInfoList.get(i).getId());
        }
        mCustomDialGirdAdapter.setData(dalZipPreInfoList);
    }
    private void loadData() {
        HttpRequestor httpRequestor = new HttpRequestor();
        String btname = BLEVersionUtils.getInstance(mContext).getBleVersionName();
        String dpi = SPUtil.getInstance(mContext).getResolutionWidthHeight();
        int shape = SPUtil.getInstance(mContext).getDialScreenType();
        loadlocalData(String.valueOf(shape), dpi);
        if (!NetworkUtils.getInstance(mContext).isNetworkAvailable()) {
            return;
        }
        //getWatchZipsHashMap????????????????????????4????????? boolean idRelease.true????????????????????????????????????????????????,false:????????????????????????????????????????????????
        Map<String, String> map = PostUtil.getInstance(mContext).getWatchZipsHashMap(btname, dpi, shape);
        String jsonStr = new Gson().toJson(map);
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("content", jsonStr);

        try {
            Flowable.just(0).subscribeOn(Schedulers.io()).map(integer -> {
                String json = httpRequestor.doPost(PostUtil.GET_WATCH_ZIPS, hashMap);
                JSONObject jsonObject = new JSONObject(json);
                int flag = jsonObject.getInt("flag");
                LogUtils.d(TAG, "flag: " + flag);
                if (flag < 0) {
                    return null;
                }
                JSONArray jsonArray = jsonObject.getJSONArray("list");

                LogUtils.d(TAG, "jsonArray: " + jsonArray);

                List<DialZipInfo> files = new Gson().fromJson(jsonArray.toString(), new TypeToken<List<DialZipInfo>>() {
                }.getType());
                Collections.sort(files);//list????????????
                return files;
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(v -> {
                LogUtils.d(TAG, "v: " + v);
                if (v != null) {
                    watchList = v;
                    LogUtils.d(TAG, "watchList: " + watchList.size());
                    if (mCustomDialGirdAdapter != null) {
                        for (int i = 0; i < watchList.size(); i++) {
                            isdownLoadZip(i);
                        }
                    }
                }
                System.out.println("-------");
            }, v -> {
//                ToastUtils.showToast(getContext(), getString(R.string.confire_is_network_available));
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String folderDial = "";

    /**
     * ?????????????????????????????????????????????
     */
    private void isdownLoadZip(int position) {
        boolean exist =UTESQLOperate.getInstance(mContext).isCustomDialExit(watchList.get(position).getId());
        if (!exist) {
            Rgb.LogD("???????????????"+watchList.get(position).getId());
            downLoadZip(position);
        }
    }
    private void downLoadZip(int position) {
        LogUtils.d(TAG, "??????????????????");
        String urlPath = watchList.get(position).getFile();
        final String path = folderDial;
        String dip = watchList.get(position).getDpi();
        dip = dip.replace("*", "x");
        String zipFileName = "DialCustom_" + dip + "_" + watchList.get(position).getId() + ".zip";
        String fileName2 = "DialCustom_" + dip + "_" + watchList.get(position).getId();
//        state = Status.DownLoading;
//        txtSync.setText(mContext.getString(R.string.downLoading));
        Flowable.just(0).map(integer -> {
            int status = downloadFile(urlPath, path + "/", zipFileName);
            OnlineDialUtil.LogI("?????????????????? =" + status);
            if (status == -1) {
                throw new Exception(mContext.getString(R.string.downError));
            }
            return status;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(v -> {
                    LogUtils.d(TAG, "??????????????????????????? " + v);
                    ZipUtils.UnZipFolder(path + "/" + zipFileName, path + "/" + fileName2);

                    DialZipPreInfo mDialZipPreInfo =new DialZipPreInfo();
                    mDialZipPreInfo.setId(watchList.get(position).getId());
                    mDialZipPreInfo.setType(watchList.get(position).getType());
                    mDialZipPreInfo.setDpi(watchList.get(position).getDpi());
                    mDialZipPreInfo.setFile(watchList.get(position).getFile());
                    mDialZipPreInfo.setNote(watchList.get(position).getNote());
                    mDialZipPreInfo.setCreatetime(watchList.get(position).getCreatetime());

                    mDialZipPreInfo.setBitmap(PicUtils.getInstance().getDefaultPreviewSDPath(path + "/" + fileName2));
                    mDialZipPreInfo.setFolderDial(path + "/" + fileName2);
                    mDialZipPreInfo.setPathStatus(PicUtils.PATH_STATUS_DATA);
                    dalZipPreInfoList.add(mDialZipPreInfo);
                    Collections.sort(dalZipPreInfoList);//list????????????
                    mCustomDialGirdAdapter.setData(dalZipPreInfoList);
                    UTESQLOperate.getInstance(mContext).saveCustomDial(mDialZipPreInfo);
                }, throwable -> {
                    LogUtils.d(TAG, "??????");
//                    ToastUtils.showToast(mContext, mContext.getString(R.string.downError));
                });
    }

    public int downloadFile(String urlPath, String path, String fileName) {
        File file = null;
        try {
            // ????????????
            URL url = new URL(urlPath);
            // ??????????????????????????????
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.connect();
            int fileLength = httpURLConnection.getContentLength();
            // ?????????
            //    String filePathUrl = httpURLConnection.getURL().getFile();
            // String fileFullName = fileName;
            System.out.println("file length---->" + fileLength);
            //     URLConnection con = url.openConnection();
            FileUtil fileUtil = new FileUtil();
            if (fileUtil.isFileExist(path + fileName)) {
                fileUtil.deleteFile(path + fileName);
            }
            BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());

            path = path + fileName;
            file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(file);
            int size = 0;
            int len = 0;
            byte[] buf = new byte[1024];
            while ((size = bin.read(buf)) != -1) {
                len += size;
                out.write(buf, 0, size);
                // ?????????????????????
//                LogUtils.d(TAG, "?????????-------> " + len * 100 / fileLength + "%\n");
            }
            bin.close();
            out.close();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        return 0;

    }
}
