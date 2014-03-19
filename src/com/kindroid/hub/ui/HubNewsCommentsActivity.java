package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.List;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.CommentsListAdapter;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.proto.WeiboContentProtoc.Review;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.AbsListView.OnScrollListener;

public class HubNewsCommentsActivity extends Activity implements OnScrollListener {
	private final static String TAG = "HubNewsCommentsActivity";
	private final int HANDLE_DATA = 0;
	private final int REFRESH_DATA = 1;
	
	private ListView newsCommentsListView;
	private LinearLayout footView;
	private ImageView btnBack;
	private RelativeLayout titleBarLayout;
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private int PAGE_INDEX = 1;
	private final static int PAGE_SIZE = 10;
	private int currentPage = 1;
	
	public static WeiboContent weibo;
	private boolean threadRunning = false;
	private CommentsListAdapter adapter;
	private ProgressDialog dlgLoading = null;
	//comments List
	public static List<Review> commentsList = new ArrayList<Review>();
	
	private String fromMode = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_news_comments);
		
		fromMode = getIntent().getStringExtra("fromMode");
		
		weibo = NewsDetailsActivity.weibo;
		findViews();
		initComponents();
//		showLoadingDialog();
//		new LoadCommentsThread(currentPage, false).start();
		
	}

	private void findViews() {
		newsCommentsListView = (ListView) findViewById(R.id.newsCommentsListView);
		titleBarLayout = (RelativeLayout) findViewById(R.id.title_bar);
		
		btnBack = (ImageView) findViewById(R.id.btn_back);
		btnBack.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					finish();
				}
			}
		);
		//foot view
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footView = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		newsCommentsListView.addFooterView(footView);
		newsCommentsListView.setOnScrollListener(this);
	}
	
	private void initComponents() {
		//设置标题背景
		if (!TextUtils.isEmpty(fromMode) && fromMode.equals("channel")) {
			titleBarLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_blue_bg));
		} else {
			titleBarLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.head_bg));
		}
	}
	
	//handle search result
	Handler dataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			hideLoadingDialog();
			switch (msg.arg1) {
			case HANDLE_DATA:
				if (commentsList != null && commentsList.size() > 0) {
					
					adapter = new CommentsListAdapter(HubNewsCommentsActivity.this, commentsList);
					newsCommentsListView.setAdapter(adapter);
					adapter.notifyDataSetChanged();
					
					if (commentsList.size() < PAGE_SIZE) {
						newsCommentsListView.removeFooterView(footView);
					}
				} else {
					if (adapter != null) {
						adapter.notifyDataSetChanged();
						adapter.notifyDataSetInvalidated();
						newsCommentsListView.removeFooterView(footView);
					}
				}
				break;
			case REFRESH_DATA:
				if (adapter != null) {
					if (commentsList != null) {
						adapter.notifyDataSetChanged();
						if (commentsList.size() < PAGE_SIZE) {
							newsCommentsListView.removeFooterView(footView);
						}
					}
				}
				threadRunning = false;
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		showLoadingDialog();
		new LoadCommentsThread(PAGE_INDEX, false).start();
		
	}

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
			
			List<Review> tempList = new ArrayList<Review>();
			tempList = DataService.getCommentsListData(weibo.getContentId(), startIndex, HubNewsCommentsActivity.this, PAGE_SIZE);
			commentsList.addAll(tempList);
//			Log.v(TAG, "size:" + tempList.size());
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
	public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
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
	private void showLoadingDialog() {
		if (this.dlgLoading == null) {
			this.dlgLoading = ProgressDialog.show(this, "", this.getString(R.string.msg_loading), true, true);
		} else {
			this.dlgLoading.show();
		}
	}
		
	/**
	 * Hide loading dialog
	 */
	private void hideLoadingDialog() {
		if (dlgLoading != null) {
			this.dlgLoading.dismiss();
		}
	}
}
