package com.lfy.disklrucachedemo;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class CommonAdapter<T> extends BaseAdapter {
	protected Context mContext;
	private ArrayList<T> mDatas;
	private int layoutId;

	public CommonAdapter(Context mContext, ArrayList<T> mDatas, int layoutId) {
		super();
		this.mContext = mContext;
		this.mDatas = mDatas;
		this.layoutId = layoutId;
	}

	@Override
	public int getCount() {
		return mDatas.size();
	}

	@Override
	public T getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CommonViewHolder viewHolder = CommonViewHolder.getHolder(mContext, convertView,
				parent, layoutId, position);
		convert(viewHolder, getItem(position));
		return viewHolder.getConvertView();
	}

	public abstract void convert(CommonViewHolder viewHolder, T data);

}
