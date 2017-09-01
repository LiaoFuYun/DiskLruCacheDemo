package com.lfy.disklrucachedemo;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CommonViewHolder {
    private SparseArray<View> mViews;
    private int mPosition;
    private View mConvertView;
//    private static DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
//            .showImageOnFail(R.drawable.ic_noimage)
//            // 设置图片加载或解码过程中发生错误显示的图片
//            .showImageForEmptyUri(R.drawable.ic_noimage)
//            .showImageOnLoading(R.drawable.ic_noimage)
//            .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
//            .cacheOnDisk(false) // 设置下载的图片是否缓存在SD卡中
//            .considerExifParams(true).build(); // 构建完成;

    public CommonViewHolder(Context context, ViewGroup parent, int layoutId,
                            int position) {
        this.mPosition = position;
        this.mViews = new SparseArray<>();
        mConvertView = LayoutInflater.from(context).inflate(layoutId, parent,
                false);
        mConvertView.setTag(this);
    }

    public static CommonViewHolder getHolder(Context context, View convertView,
                                             ViewGroup parent, int layoutId, int position) {
        if (convertView == null) {
            return new CommonViewHolder(context, parent, layoutId, position);
        } else {
            CommonViewHolder holder = (CommonViewHolder) convertView.getTag();
            holder.mPosition = position;
            return holder;
        }
    }

    /**
     * 通过viewId获取view
     */
    @SuppressWarnings("unchecked")
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    public View getConvertView() {
        return mConvertView;
    }

    public int getPosition() {
        return mPosition;
    }

    public CommonViewHolder setText(int viewId, String text) {
        TextView tv = getView(viewId);
        tv.setText(text);
        return this;
    }

    public CommonViewHolder setImageResource(int viewId, int resId) {
        ImageView imageView = getView(viewId);
        imageView.setImageResource(resId);
        return this;
    }

//    public CommonViewHolder setImageURL(int viewId, String url) {
//        ImageView imageView = getView(viewId);
//        ImageLoader.getInstance().displayImage(url, imageView, defaultOptions);
//        return this;
//    }
//
//    public CommonViewHolder setImageURL(int viewId, String url, DisplayImageOptions imageOptions) {
//        ImageView imageView = getView(viewId);
//        if (imageOptions == null) {
//            imageOptions = defaultOptions;
//        }
//        ImageLoader.getInstance().displayImage(url, imageView, imageOptions);
//        return this;
//    }
//
//    public CommonViewHolder setImageURL(int viewId, String url, int emptyResId) {
//        ImageView imageView = getView(viewId);
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
