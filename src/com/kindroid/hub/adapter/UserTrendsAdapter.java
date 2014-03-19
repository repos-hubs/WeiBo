package com.kindroid.hub.adapter;

import java.util.List;

import weibo4android.UserTrend;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kindroid.hub.R;

public class UserTrendsAdapter extends BaseAdapter {

	private List<UserTrend> list;
	private Context ctx;
	
	public UserTrendsAdapter(List<UserTrend> trendList, Context context) {
		this.list = trendList;
		this.ctx = context;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int i) {
		return list.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		View view = convertView;
		
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
			view = inflater.inflate(R.layout.at_me_hub_topic_item, null);
		}
		TextView topicTextView = (TextView) view.findViewById(R.id.topicTextView);
		UserTrend trend = (UserTrend) list.get(position);
		topicTextView.setText(trend.getHotword());
		
		return view;
	}

}
