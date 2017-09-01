package com.lfy.disklrucachedemo;

import android.content.Context;
import android.graphics.BitmapFactory;

import java.util.ArrayList;

public class RecyclerAdapter extends CommonRecyclerAdapter<String> {

    private String dirPath = "";

    public RecyclerAdapter(Context mContext, ArrayList<String> mDatas) {
        super(mContext, mDatas, R.layout.recycler_item);
    }

    @Override
    public void convert(CommonRecycleHolder holder, String data, int position) {
        holder.setImageBitmap(R.id.imageV, BitmapFactory.decodeFile(dirPath + "/" + data));
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
        notifyDataSetChanged();
    }
}
