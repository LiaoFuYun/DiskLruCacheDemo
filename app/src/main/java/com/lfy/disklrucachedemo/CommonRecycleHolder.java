package com.lfy.disklrucachedemo;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class CommonRecycleHolder extends RecyclerView.ViewHolder {

    /**
     * 用于存储当前item当中的View
     */
    private SparseArray<View> mViews;
//    private static DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
//            .showImageOnFail(R.drawable.ic_noimage)
//            // 设置图片加载或解码过程中发生错误显示的图片
//            .showImageForEmptyUri(R.drawable.ic_noimage)
//            .showImageOnLoading(R.drawable.ic_noimage)
//            .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
//            .cacheOnDisk(false) // 设置下载的图片是否缓存在SD卡中
//            .considerExifParams(true)
//            .build(); // 构建完成;


    public CommonRecycleHolder(View itemView) {
        super(itemView);
        mViews = new SparseArray<>();
    }

    public <T extends View> T findView(int ViewId) {
        View view = mViews.get(ViewId);
        //集合中没有，则从item当中获取，并存入集合当中
        if (view == null) {
            view = itemView.findViewById(ViewId);
            mViews.put(ViewId, view);
        }
        return (T) view;
    }

    public CommonRecycleHolder setText(int viewId, CharSequence text) {
        TextView tv = findView(viewId);
        tv.setText(text);
        return this;
    }

    public CommonRecycleHolder setText(int viewId, int text) {
        TextView tv = findView(viewId);
        tv.setText(text);
        return this;
    }

    public CommonRecycleHolder setImageResource(int viewId, int ImageId) {
        ImageView image = findView(viewId);
        image.setImageResource(ImageId);
        return this;
    }

    public CommonRecycleHolder setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView image = findView(viewId);
        image.setImageBitmap(bitmap);
        return this;
    }

//    public CommonRecycleHolder setImageURL(int viewId, String url) {
//        ImageView imageView = findView(viewId);
//        ImageLoader.getInstance().displayImage(url, imageView, defaultOptions);
//        return this;
//    }
//
//    public CommonRecycleHolder setImageURL(int viewId, String url, DisplayImageOptions imageOptions) {
//        ImageView imageView = findView(viewId);
//        if (imageOptions == null) {
//            imageOptions = defaultOptions;
//        }
//        ImageLoader.getInstance().displayImage(url, imageView, imageOptions);
//        return this;
//    }
//
//    public CommonRecycleHolder setImageURL(int viewId, String url, int emptyResId) {
//        ImageView imageView = findView(viewId);
//        DisplayImageOptions options;
//        if (emptyResId != -1) {
//            options = new DisplayImageOptions.Builder()
//                    .showImageOnFail(emptyResId)
//                    // 设置图片加载或解码过程中发生错误显示的图片
//                    .showImageForEmptyUri(emptyResId)
//                    .showImageOnLoading(emptyResId)
//                    .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
//                    .cacheOnDisk(false) // 设置下载的图片是否缓存在SD卡中
//                    .considerExifParams(true)
//                    .build(); // 构建完成
//        } else {
//            options = defaultOptions;
//        }
//        ImageLoader.getInstance().displayImage(url, imageView, options);
//        return this;
//    }
}
