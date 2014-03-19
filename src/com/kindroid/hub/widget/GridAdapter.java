package com.kindroid.hub.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kindroid.hub.entity.GroupFriend;

public class GridAdapter extends BaseAdapter {
	private Context mContext;

	private List<GroupFriend> mData;
	private Bitmap[] mBitmaps = null;
	private CategoryItemView[] mViewList = null;

	public GridAdapter(Context c) {
		super();
		this.mContext = c;
	}

	public void init(List<GroupFriend> data) {
		if (data != null && data.size() > 0) {
			
			this.mData = data;
			this.mBitmaps = new Bitmap[data.size()];
			this.mViewList = new CategoryItemView[data.size()];
			for (int i = 0; i < this.mBitmaps.length; i++) {
				this.mViewList[i] = new CategoryItemView(this.mContext, null);
				this.mViewList[i].init(data.get(i));
				this.mViewList[i].setTag(this.mData.get(i));// bind [product] in tag property
			}
		} else {
			mData = new ArrayList();
		}
	}
	
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int index) {

		return mData.get(index);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CategoryItemView view = this.mViewList[position];
		view.invalidate();
		return view;
	}
}
