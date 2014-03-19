package com.kindroid.hub.adapter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weibo4android.Comment;
import weibo4android.Status;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kindroid.hub.R;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.download.LazyImageLoader;
import com.kindroid.hub.ui.UserDetailInfo;
import com.kindroid.hub.utils.Utils;

public class CommentItemAdapter extends BaseAdapter {
	private final String TAG = "CommentItemAdapter";
	private List<Comment> list;
	private Context ctx;
	private LazyImageLoader imageLoader;
	
	public CommentItemAdapter(List<Comment> commentList , Context context) {
		this.list = commentList;
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
		ImageView statusContentImage=(ImageView)view.findViewById(R.id.newsContentImageView);
		final TextView statusQuoteContent = (TextView) view.findViewById(R.id.newsForwardTextView);
		final ImageView statusQuoteImage = (ImageView) view.findViewById(R.id.newsForwardImageView);
		TextView timeTextView = (TextView) view.findViewById(R.id.timeTextView);
		TextView fromTextView = (TextView) view.findViewById(R.id.fromTextView);
		final LinearLayout statusQuoteLayout = (LinearLayout) view.findViewById(R.id.forwardLayout);
		
		final Comment weiboContent = list.get(position);
		statusContentImage.setVisibility(View.GONE);
		tribeTitle.setText(weiboContent.getUser().getName());
		timeTextView.setText(Utils.ddate(weiboContent.getCreatedAt().getTime(), ctx));
		
		String sourceStr = weiboContent.getSource();
		Pattern name = Pattern.compile("<a.*?>(.*?)</a>", Pattern.DOTALL);
		Matcher fromMatcher = name.matcher(sourceStr);
		if (fromMatcher.find()) {
			fromTextView.setText(fromMatcher.group(1));
		}
		weiboContent.getStatus().getText();
		avatarImageView.setTag(weiboContent.getUser().getProfileImageURL().toString());
		imageLoader.displayUserIcon(weiboContent.getUser().getProfileImageURL().toString(),(Activity) ctx, avatarImageView);
		
		statusContentTextView.setText(weiboContent.getText());
		
		avatarImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ctx, UserDetailInfo.class);
				intent.putExtra(UserDetailInfo.U_ID, weiboContent.getUser().getId());
				ctx.startActivity(intent);
			}
		});
		
		Status retweetContent = weiboContent.getStatus();
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
}
