package com.kindroid.hub.adapter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kindroid.hub.R;
import com.kindroid.hub.data.UserDefaultInfo;
import com.kindroid.hub.download.LazyImageLoader;
import com.kindroid.hub.entity.WeiboInfo;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;
import com.kindroid.hub.ui.HubSinaWeiboDetailsActivity;
import com.kindroid.hub.ui.NewsDetailsActivity;
import com.kindroid.hub.ui.NewsListActivity;
import com.kindroid.hub.ui.UserDetailInfo;
import com.kindroid.hub.utils.Utils;

import weibo4android.Status;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatusItemAdapter extends BaseAdapter {
	
	private List<Status> list;
	private Context ctx;
	private LazyImageLoader imageLoader;
	
	public StatusItemAdapter(List<Status> statusList , Context context) {
		this.list = statusList;
		this.ctx = context;
		imageLoader = new LazyImageLoader(ctx);
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (null == view) {
			LayoutInflater li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = li.inflate(R.layout.hub_news_main_item, null);
		}
		
		ImageView avatarImageView = (ImageView) view.findViewById(R.id.newsIconImageView);
		TextView tribeTitle = (TextView) view.findViewById(R.id.newsTitleTextView);
		
		TextView statusContentTextView = (TextView) view.findViewById(R.id.newsContentsTextView);
		statusContentTextView.setOnClickListener(new ViewClick(position));
		ImageView statusContentImage=(ImageView)view.findViewById(R.id.newsContentImageView);
		TextView statusQuoteContent = (TextView) view.findViewById(R.id.newsForwardTextView);
		statusQuoteContent.setOnClickListener(new ViewClick(position));
		ImageView statusQuoteImage = (ImageView) view.findViewById(R.id.newsForwardImageView);
		TextView timeTextView = (TextView) view.findViewById(R.id.timeTextView);
		TextView fromTextView = (TextView) view.findViewById(R.id.fromTextView);
		LinearLayout statusQuoteLayout = (LinearLayout) view.findViewById(R.id.forwardLayout);
		
		final Status weiboContent = list.get(position);
		
		avatarImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ctx, UserDetailInfo.class);
				intent.putExtra(UserDetailInfo.U_ID, weiboContent.getUser().getId());
				if (String.valueOf(weiboContent.getUser().getId()).equals(UserDefaultInfo.getWeiboId(ctx))) {
					intent.putExtra(UserDetailInfo.IS_MY, true);
				}
				ctx.startActivity(intent);
			}
		});
		
		tribeTitle.setText(weiboContent.getUser().getName());
		tribeTitle.setOnClickListener(new ViewClick(position));
		timeTextView.setText(Utils.ddate(weiboContent.getCreatedAt().getTime(), ctx));
		
		String sourceStr = weiboContent.getSource();
		Pattern name = Pattern.compile("<a.*?>(.*?)</a>", Pattern.DOTALL);
		Matcher fromMatcher = name.matcher(sourceStr);
		if (fromMatcher.find()) {
			fromTextView.setText(fromMatcher.group(1));
		}
		
		avatarImageView.setTag(weiboContent.getUser().getProfileImageURL().toString());
		imageLoader.displayUserIcon(weiboContent.getUser().getProfileImageURL().toString(),(Activity) ctx, avatarImageView);
		
		statusContentTextView.setText(weiboContent.getText());
		if (!TextUtils.isEmpty(weiboContent.getThumbnail_pic())) {
			statusContentImage.setTag(weiboContent.getThumbnail_pic());
			imageLoader.displayAppIcon(weiboContent.getThumbnail_pic(),(Activity) ctx, statusContentImage);
		}else{
			statusContentImage.setVisibility(View.GONE);
			statusContentImage.setImageBitmap(null);
		}
		Status retweetContent = weiboContent.getRetweeted_status();
		if (retweetContent != null) {
			statusQuoteLayout.setVisibility(View.VISIBLE);
			statusQuoteContent.setText(retweetContent.getText());
			if (!TextUtils.isEmpty(retweetContent.getThumbnail_pic())) {
				statusQuoteImage.setVisibility(View.VISIBLE);
				statusQuoteImage.setTag(retweetContent.getThumbnail_pic());
				imageLoader.displayAppIcon(retweetContent.getThumbnail_pic(),(Activity) ctx, statusQuoteImage);
			} else {
				statusQuoteImage.setVisibility(View.GONE);
				statusQuoteImage.setImageBitmap(null);
			}
		} else {
			statusQuoteLayout.setVisibility(View.GONE);
		}
		return view;
	}
	
	class ViewClick implements View.OnClickListener {
		int listPos = -1;
		public ViewClick(int position) {
			listPos = position;
		}
		@Override
		public void onClick(View view) {
			HubSinaWeiboDetailsActivity.statusList = list;
			Intent intent = new Intent(ctx, HubSinaWeiboDetailsActivity.class);
			intent.putExtra("listPosition", listPos);
			ctx.startActivity(intent);
		}
	}
}
