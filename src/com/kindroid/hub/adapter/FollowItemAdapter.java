package com.kindroid.hub.adapter;

import java.util.List;

import com.kindroid.hub.R;
import com.kindroid.hub.download.LazyImageLoader;

import weibo4android.User;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FollowItemAdapter extends BaseAdapter {

	Context ctx;
	List<User> list;
	private LazyImageLoader imageLoader;
	
	public FollowItemAdapter(Context ctx,  List<User> friendsList) {
		this.ctx = ctx;
		this.list = friendsList;
		imageLoader = new LazyImageLoader(ctx);
	}
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		View view = convertView;
		if (null == view) {
			LayoutInflater li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = li.inflate(R.layout.follow_item, null);
		}
		User user = list.get(position);
		ImageView userIcon = (ImageView) view.findViewById(R.id.userIcon);
		userIcon.setTag(user.getProfileImageURL().toString());
		imageLoader.displayUserIcon(user.getProfileImageURL().toString(),(Activity) ctx, userIcon);
		TextView userName = (TextView) view.findViewById(R.id.userName);
		userName.setText(user.getName());
		TextView fansCount = (TextView) view.findViewById(R.id.fansCount);
		fansCount.setText(ctx.getResources().getString(R.string.fans_count, user.getFollowersCount()));
		return view;
	}

}
