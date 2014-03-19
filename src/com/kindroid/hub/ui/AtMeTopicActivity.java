package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.List;

import weibo4android.UserTrend;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.UserTrendsAdapter;
import com.kindroid.hub.data.DataService;

public class AtMeTopicActivity extends Activity implements OnScrollListener, View.OnClickListener {
	private static final String TAG = "AtMeTopicActivity";
	private final int HANDLE_DATA = 0;
	private final int REFRESH_DATA = 1;
	
	private ListView topicListView;
	private LinearLayout footView;
	private ImageView btnBack;
	
	private ProgressDialog dlgLoading = null;
	
	private UserTrendsAdapter adapter;
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private int PAGE_INDEX = 1;
	private final static int PAGE_SIZE = 15;
	private int currentPage = 1;
	
	private boolean threadRunning = false;
	private String userId;
	
	public static List<UserTrend> topicsList = new ArrayList<UserTrend>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.at_me_hub_topic);
		userId = getIntent().getStringExtra("userId");
		
		findViews();
		showProgressDialog();
		new LoadTopicsThread(currentPage, false).start();
	}
	
	private void findViews() {
		topicListView = (ListView) findViewById(R.id.topicListView);
		btnBack = (ImageView) findViewById(R.id.btn_back);
		btnBack.setOnClickListener(this);
		
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footView = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		topicListView.addFooterView(footView);
		topicListView.setOnScrollListener(this);
		
		topicListView.setOnItemClickListener(new ItemClick());
	}
	
	Handler dataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			hideLoadingDialog();
			switch (msg.arg1) {
			case HANDLE_DATA:
				if (topicsList != null && topicsList.size() > 0) {
					
					adapter = new UserTrendsAdapter(topicsList, AtMeTopicActivity.this);
					topicListView.setAdapter(adapter);
					adapter.notifyDataSetChanged();
					
					if (topicsList.size() < PAGE_SIZE) {
						topicListView.removeFooterView(footView);
					}
				} else {
					if (adapter != null) {
						adapter.notifyDataSetChanged();
						topicListView.removeFooterView(footView);
					}
				}
				break;
			case REFRESH_DATA:
				if (adapter != null) {
					if (topicsList != null) {
						adapter.notifyDataSetChanged();
						if (topicsList.size() < PAGE_SIZE) {
							topicListView.removeFooterView(footView);
						}
					}
				}
				threadRunning = false;
			default:
				break;
			}
		}
	};
	
	class ItemClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			Intent intent = new Intent(AtMeTopicActivity.this, AtMeTopicStatusListActivity.class);
			intent.putExtra("topicName", topicsList.get(position).getHotword());
			startActivity(intent);
			
		}
		
	}
	
	class LoadTopicsThread extends Thread {
		private int startIndex;
		private boolean isRefresh;

		public LoadTopicsThread(int startIndex, boolean isRefresh) {
			this.startIndex = startIndex;
			this.isRefresh = isRefresh;
			if (isRefresh == false && topicsList != null && topicsList.size() > 0) {
				topicsList.clear();
			}
		}
		
		public void run() {
			
			List<UserTrend> tempList = DataService.getTopicsDataList(startIndex, userId, PAGE_SIZE, AtMeTopicActivity.this);
			topicsList.addAll(tempList);
			Message msg = dataHandler.obtainMessage();
			msg.obj = topicsList;
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
//			new LoadNewsThread(currentPage, true).start();
			
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
