package com.kindroid.hub.adapter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weibo4android.Status;


import com.kindroid.hub.R;
import com.kindroid.hub.download.LazyImageLoader;
import com.kindroid.hub.ui.UserDetailInfo;
import com.kindroid.hub.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IndexAdapter extends BaseAdapter{
	
	
	private Context ctx;
	private List<Status> mList;
	private LazyImageLoader imageLoader;

	public IndexAdapter(Context ctx, List<Status> list) {
		this.ctx = ctx;
		this.mList = list;
		imageLoader = new LazyImageLoader(ctx);
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (null == v) {
			LayoutInflater li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = li.inflate(R.layout.hub_summary_sina_item, null);
		}
		
		final Status weiboContent = mList.get(position);
		
		ImageView tribeImage = (ImageView) v.findViewById(R.id.newsIconImageView);
		
		tribeImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ctx, UserDetailInfo.class);
				intent.putExtra(UserDetailInfo.U_ID, weiboContent.getUser().getId());
				ctx.startActivity(intent);
			}
		});
		TextView tribeTitle = (TextView) v.findViewById(R.id.newsTitleTextView);
		
		TextView tribeContent = (TextView) v.findViewById(R.id.newsContents);
		ImageView tribeContentImage=(ImageView)v.findViewById(R.id.newsContentImageView);
		TextView tribeQuoteContent = (TextView) v.findViewById(R.id.newsForwardTextView);
		ImageView tribeQuoteImage = (ImageView) v.findViewById(R.id.newsContentsQuoteImageView);
		LinearLayout tribeQuoteLayout = (LinearLayout) v.findViewById(R.id.newsQuoteLayout);
		
		TextView time=(TextView)v.findViewById(R.id.timeTextView);
		time.setText(Utils.ddate(weiboContent.getCreatedAt().getTime(), ctx));
		
		TextView source = (TextView) v.findViewById(R.id.fromTextView);
		String sourceStr = weiboContent.getSource();
		Pattern name = Pattern.compile("<a.*?>(.*?)</a>", Pattern.DOTALL);
		Matcher nameM = name.matcher(sourceStr);
		if (nameM.find()) {
			source.setText(nameM.group(1));
		}
		
		tribeTitle.setText(weiboContent.getUser().getName());
		tribeImage.setTag(weiboContent.getUser().getProfileImageURL().toString());
		imageLoader.displayUserIcon(weiboContent.getUser().getProfileImageURL().toString(),(Activity) ctx, tribeImage);
		
		tribeContent.setText(weiboContent.getText());
		if (!TextUtils.isEmpty(weiboContent.getBmiddle_pic())) {
			tribeContentImage.setTag(weiboContent.getBmiddle_pic());
			imageLoader.displayAppIcon(weiboContent.getBmiddle_pic(),(Activity) ctx, tribeContentImage);
		}else{
			tribeContentImage.setImageBitmap(null);
		}
		Status retweetContent = weiboContent.getRetweeted_status();
		if (retweetContent != null) {
			tribeQuoteLayout.setVisibility(View.VISIBLE);
			tribeQuoteContent.setText(retweetContent.getText());
			if (!TextUtils.isEmpty(retweetContent.getBmiddle_pic())) {
				tribeQuoteImage.setTag(retweetContent.getBmiddle_pic());
				imageLoader.displayAppIcon(retweetContent.getBmiddle_pic(),(Activity) ctx, tribeQuoteImage);
			}else{
				tribeQuoteImage.setImageBitmap(null);
			}
		} else {
			tribeQuoteLayout.setVisibility(View.GONE);
		}
		return v;
	}
}
