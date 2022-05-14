package com.yc.peddemo.onlinedial.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;


public class BitmapUtil {


    /**
     * 不需要指定大小,img为wrapcontent
     *
     * @param context
     * @param url
     * @param img
     */
    public static void loadBitmap(Context context, String url, ImageView img) {
        Glide.with(context)
                .load(url)
                .into(img);
    }


    public static void loadSignalBitmap(Context context, String url, ImageView img, String sig) {
        Glide.with(context)
                .load(url)
                .into(img);
    }


    /**
     * 指定具体大小
     *
     * @param context
     * @param url
     * @param img
     * @param width
     * @param height
     */
    public static void loadBitmap(Context context, String url, ImageView img, int width, int height) {
        Glide.with(context)
                .load("http://nuuneoi.com/uploads/source/playstore/cover.jpg")
                .into(img);
    }


    /**
     * img为非wrapcontent时,需要自动缩放时使用此方法
     *
     * @param context
     * @param url
     * @param img
     */
    public static void loadBitmap(Context context, String url, ImageView img, boolean isAutoFit) {
        if (isAutoFit) {
            Glide.with(context)
                    .load(url)
                    .into(img);
        } else {
            loadBitmap(context, url, img);
        }
    }

    /**
     * @param context
     * @param url
     * @param img
     * @param placeholderResId   占位图R.id
     * @param imagenotfoundResId 图片未找到R.id
     */
    public static void loadBitmap(Context context, String url, int placeholderResId, int imagenotfoundResId, ImageView img) {
        myCashStrategyOptions.fitCenter().placeholder(placeholderResId)
                .error(imagenotfoundResId);
        Glide.with(context)
                .load(url).apply(myCashStrategyOptions)
                .into(img);

    }

    /**
     * @param context
     * @param url
     * @param img
     * @param placeholderResId 占位图R.id
     */
    public static void loadBitmap(Context context, String url, int placeholderResId, ImageView img) {
        myOptions.fitCenter().placeholder(placeholderResId)
                .error(placeholderResId);

        Glide.with(context)
                .load(url).apply(myOptions)
                .into(img);

    }

    static RequestOptions myOptions = new RequestOptions();
    static RequestOptions myCashStrategyOptions = new RequestOptions();
    static {
        myOptions.skipMemoryCache(true);
        myOptions.diskCacheStrategy(DiskCacheStrategy.NONE); // 不使用磁盘缓存
        myCashStrategyOptions.skipMemoryCache(false);
    }

    public static void loadBitmap(Context context, String url, Drawable imagenotfound, ImageView img) {
        Glide.with(context)
                .load(url)
                .into(img);
    }


    public static void loadBitmap(Context context, String url, Drawable placeholder, Drawable imagenotfound, ImageView img) {
        Glide.with(context)
                .load(url)
                .into(img);
    }


    public static void loadBitmap(Context context, String url, Drawable placeholder, int imagenotfoundResId, ImageView img) {
        Glide.with(context)
                .load(url)
                .into(img);
    }


    public static void loadBitmap(Context context, Integer resId, ImageView img) {
        RequestOptions myOptions = new RequestOptions()
                .fitCenter();


        Glide.with(context)
                .load(resId)
                .apply(myOptions)
                .into(img);
    }


    public static void loadBitmap(Context context, Integer resId, ImageView img, int width, int height) {
        RequestOptions myOptions = new RequestOptions()
                .fitCenter()
                .override(width, height);

        Glide.with(context)
                .load(resId)
                .apply(myOptions)
                .into(img);
    }


    public static void loadBitmap(Context context, Integer resId, ImageView img, Drawable errorDrawable) {
        Glide.with(context)
                .load(resId)
                .into(img).onLoadFailed(errorDrawable);
    }

    public static void loadBurBitmap(Context context, String url, ImageView img) { // 毛玻璃
//        Glide.with(context).load(url).bitmapTransform(new1 BlurTransformation(context, 25)).crossFade(1000).into(img);

    }

    public static void cleanMemory(Context context) {
        Glide.get(context).clearMemory();
    }

//
//    /**
//     * 本地圆形图片
//     * @param context
//     * @param resId
//     * @param img
//     */
//    public static void loadCircleBitmap(Context context, Integer resId, ImageView img) {
//        Glide.with(context)
//                .load(resId)
//                .bitmapTransform(new1 CropCircleTransformation(context))
//                .fitCenter()
//                .centerCrop()
//                .into(img);
//    }
//
//    /**
//     * 网络圆形图片
//     * @param context
//     * @param url
//     * @param img
//     */
//    public static void loadCircleBitmap(Context context, String url, ImageView img) {
//        Glide.with(context)
//                .load(url)
//                .bitmapTransform(new1 CropCircleTransformation(context))
//                .fitCenter()
//                .centerCrop()
//                .into(img);
//    }


}
