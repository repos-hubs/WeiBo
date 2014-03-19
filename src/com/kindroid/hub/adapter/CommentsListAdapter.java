package com.kindroid.hub.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kindroid.hub.R;
import com.kindroid.hub.proto.UserProtoc.Account;
import com.kindroid.hub.proto.WeiboContentProtoc.Review;
import com.kindroid.hub.ui.HubReplyActivity;

public class CommentsListAdapter extends BaseAdapter {
	private final String TAG = "CommentsListAdapter";
	private List<Review> list;
	private Context ctx;
	
	final String mimeType = "text/html";
	final String encoding = "utf-8";
	
	public CommentsListAdapter(Context ctx, List<Review> commentsList) {
		this.list = commentsList;
		this.ctx = ctx;
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
	public View getView(final int position, View convertView, ViewGroup viewGroup) {
		View view = convertView;
		
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
			view = inflater.inflate(R.layout.hub_news_details_comments_item, null);
		}
		TextView userNameTextView = (TextView) view.findViewById(R.id.userNameTextView);
		ImageView replyImageView = (ImageView) view.findViewById(R.id.replyImageView);
//		WebView commentsTextView = (WebView) view.findViewById(R.id.commentsWebView);
		TextView commentsTextView = (TextView) view.findViewById(R.id.commentContentTextView);
		TextView timeTextView = (TextView) view.findViewById(R.id.timeTextView);
		Review review = list.get(position);
		
		//get reply user title
		Account replyAccount = review.getReplayAccount();
		String replyUserName = "";
		if (review.hasReplayAccount()) {
			replyUserName = ctx.getResources().getString(R.string.home_reply_title) + replyAccount.getNickName() + ":";
		}
		String accountUser = review.getAccount().getNickName();
		String replyUserContent = accountUser + replyUserName;
		userNameTextView.setText(replyUserContent);
	
		//reply image onclick
		replyImageView.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(ctx, HubReplyActivity.class);
					intent.putExtra("listPosition", position);
					intent.putExtra("fromMode", "channel");
					intent.putExtra("type", "reply");
					ctx.startActivity(intent);
				}
			}
		);
		
		timeTextView.setText(review.getTimeLag());
		return view;
	}

}
