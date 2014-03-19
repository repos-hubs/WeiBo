package com.kindroid.hub.adapter;

import java.util.List;

import weibo4android.Comment;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kindroid.hub.R;
import com.kindroid.hub.ui.HubReplyActivity;
import com.kindroid.hub.utils.Utils;

public class WeiboDetailsCommentsAdapter extends BaseAdapter {
	private List<Comment> list;
	private Context ctx;
	private String fromMode = "";
	
	public WeiboDetailsCommentsAdapter(Context context, String from, List<Comment> commentList) {
		this.list = commentList;
		this.ctx = context;
		this.fromMode = from;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
			view = inflater.inflate(R.layout.hub_news_details_comments_item, null);
		}
		TextView userNameTextView = (TextView) view.findViewById(R.id.userNameTextView);
		ImageView replyImageView = (ImageView) view.findViewById(R.id.replyImageView);
		TextView commentsTextView = (TextView) view.findViewById(R.id.commentContentTextView);
		TextView timeTextView = (TextView) view.findViewById(R.id.timeTextView);
		
		Comment comment = list.get(position);
		userNameTextView.setText(comment.getUser().getName());
		commentsTextView.setText(comment.getText());
		timeTextView.setText(Utils.ddate(comment.getCreatedAt().getTime(), ctx));
		replyImageView.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(ctx, HubReplyActivity.class);
					HubReplyActivity.commentsList = list;
					intent.putExtra("listPosition", position);
					intent.putExtra("fromMode", fromMode);
					intent.putExtra("type", "reply");
					ctx.startActivity(intent);
				}
			}
		);
		return view;
	}

}
