package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.List;

import weibo4android.Comment;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.WeiboDetailsCommentsAdapter;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.ui.AtMeTopicActivity.ItemClick;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TextView;

public class HubSinaCommentsListActivity extends Activity implements OnScrollListener, View.OnClickListener {
	private final String TAG = "HubSinaCommentsListActivity";
	private final int HANDLE_DATA = 0;
	private final int REFRESH_DATA = 1;
	
	private WeiboDetailsCommentsAdapter adapter;
	private ListView commentsListView;
	private TextView titleTextView;
	
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private int PAGE_INDEX = 10;
	private final static int PAGE_SIZE = 15;
	private int currentPage = 1;
	
	private boolean threadRunning = false;
	private LinearLayout footView;
	private ProgressDialog dlgLoading = null;
	private ImageView btnBack;
	
	private String statusMId = "";
	private String fromMode = "";
	//comments List
	public static List<Comment> commentsList = new ArrayList<Comment>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.at_me_hub_topic);
		statusMId = getIntent().getStringExtra("statusMid");
		fromMode = getIntent().getStringExtra("fromMode");
		findViews();
		
		showProgressDialog();
		new LoadCommentsThread(currentPage, false).start();
	}

	private void findViews() {
		commentsListView = (ListView) findViewById(R.id.topicListView);
		btnBack = (ImageView) findViewById(R.id.btn_back);
		btnBack.setOnClickListener(this);
		titleTextView = (TextView) findViewById(R.id.topicTitleTextView);
		titleTextView.setText(getResources().getString(R.string.home_comment_title));
		
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footView = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		commentsListView.addFooterView(footView);
		commentsListView.setOnScrollListener(this);
		
	}
	
	Handler dataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			hideLoadingDialog();
			switch (msg.arg1) {
			case HANDLE_DATA:
				if (commentsList != null && commentsList.size() > 0) {
					
					adapter = new WeiboDetailsCommentsAdapter(HubSinaCommentsListActivity.this, fromMode, commentsList);
					commentsListView.setAdapter(adapter);
					adapter.notifyDataSetChanged();
					if (commentsList.size() < PAGE_SIZE) {
						commentsListView.removeFooterView(footView);
					}
				} else {
					if (adapter != null) {
						adapter.notifyDataSetChanged();
						adapter.notifyDataSetInvalidated();
						commentsListView.removeFooterView(footView);
					}
				}
				break;
			case REFRESH_DATA:
				if (adapter != null) {
					if (commentsList != null) {
						adapter.notifyDataSetChanged();
						if (commentsList.size() < PAGE_SIZE) {
							commentsListView.removeFooterView(footView);
						}
					}
				}
				threadRunning = false;
			default:
				break;
			}
		}
	};
	
	class LoadCommentsThread extends Thread {
		private int startIndex;
		private boolean isRefresh;

		public LoadCommentsThread(int startIndex, boolean isRefresh) {
			this.startIndex = startIndex;
			this.isRefresh = isRefresh;
			if (isRefresh == false && commentsList != null && commentsList.size() > 0) {
				commentsList.clear();
			}
		}
		
		public void run() {
			
			List<Comment> tempList = new ArrayList<Comment>();
			tempList = DataService.getCommentsList(HubSinaCommentsListActivity.this, statusMId, startIndex, PAGE_SIZE);
			commentsList.addAll(tempList);
			Message msg = dataHandler.obtainMessage();
			msg.obj = commentsList;
			if (!isRefresh) {
				msg.arg1 = HANDLE_DATA;
				dataHandler.sendMessage(msg);
			} else {
				msg.arg1 = REFRESH_DATA;
				dataHandler.sendMessage(msg);
			}
		}
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		lastItem = firstVisibleItem + visibleItemCount - 1;
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (!threadRunning && adapter != null && lastItem == adapter.getCount() && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			threadRunning = true;
			currentPage = currentPage + 1;
			new LoadCommentsThread(currentPage, true).start();
			
		}
	}
	
	
	/**
	 * Show loading dialog
	 */
	private void showProgressDialog() {
		dlgLoading = new ProgressDialog(this);
		dlgLoading.setTitle(getResources().getText(R.string.app_name));
		dlgLoading.setMessage(getResources().getText(R.string.progress_message));
		dlgLoading.setIndeterminate(true);
		dlgLoading.show();
	}
		
	/**
	 * Hide loading dialog
	 */
	private void hideLoadingDialog() {
		if (dlgLoading != null) {
			this.dlgLoading.dismiss();
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btn_back) {
			finish();
		}
	}
	
}
