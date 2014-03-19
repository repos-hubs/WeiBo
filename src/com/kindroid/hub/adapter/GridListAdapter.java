package com.kindroid.hub.adapter;

import com.kindroid.hub.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GridListAdapter extends BaseAdapter {
	private LayoutInflater layInflater;
	String text[];
	int id[];
	private Context context;
	
	public GridListAdapter(Activity context, String text[], int id[]) {
		this.context=context;
		this.text = text;
		this.id = id;
		layInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		if (id == null || text == null)
			return 0;
		if (id.length != text.length)
			return 0;
		return id.length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = layInflater.inflate(R.layout.grid_item, null);
		TextView tv_bg = (TextView) convertView.findViewById(R.id.tv_bg);
		TextView tv_me = (TextView) convertView.findViewById(R.id.tv_me);
		tv_bg.setBackgroundResource(id[position]);
		tv_me.setText(text[position]);
		if (position == 0) {
			tv_me.setTextColor(context.getResources().getColor(R.color.grid_news));
		} else if (position == 1) {
			tv_me.setTextColor(context.getResources().getColor(R.color.grid_joke));
		} else if (position == 2) {
			tv_me.setTextColor(context.getResources().getColor(R.color.grid_mm));
		} else if (position == 3) {
			tv_me.setTextColor(context.getResources().getColor(R.color.grid_wallpaper));
		} else if (position == 4) {
			tv_me.setTextColor(context.getResources().getColor(R.color.grid_ringtone));
		}
		return convertView;
	}

}
