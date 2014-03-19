package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.List;

import weibo4android.Status;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.StatusItemAdapter;
import com.kindroid.hub.data.DataService;

public class AtMeTopicStatusListActivity extends Activity implements OnScrollListener, View.OnClickListener {
	private final String TAG = "AtMeTopicStatusListActivity";
	private final int HANDLE_DATA = 0;
	private final int REFRESH_DATA = 1;
	
	private ListView statusListView;
	private LinearLayout footView;
	private ProgressDialog dlgLoading = null;
	private TextView titleTextView;
	private ImageView btnBack;
	
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private int PAGE_INDEX = 1;
	private final static int PAGE_SIZE = 15;
	private int currentPage = 1;
	private boolean threadRunning = false;
	
	private StatusItemAdapter adapter;
	public static List<Status> statusList = new ArrayList<Status>();
	private String topicName = ""; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.at_me_hub_topic);
		topicName = getIntent().getStringExtra("topicName");
		findViews();
		
		showProgressDialog();
		new LoadTopicsStatusThread(currentPage, false).start();
	}

	private void findViews() {
		statusListView = (ListView) findViewById(R.id.topicListView);
		titleTextView = (TextView) findViewById(R.id.topicTitleTextView);
		titleTextView.setText(topicName);
		
		btnBack = (ImageView) findViewById(R.id.btn_back);
		btnBack.setOnClickListener(this);
		
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footView = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		statusListView.addFooterView(footView);
		statusListView.setOnScrollListener(this);
	}
	
	Handler dataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			hideLoadingDialog();
			switch (msg.arg1) {
			case HANDLE_DATA:
				if (statusList != null && statusList.size() > 0) {
					
					adapter = new StatusItemAdapter(statusList, AtMeTopicStatusListActivity.this);
					statusListView.setAdapter(adapter);
					adapter.notifyDataSetChanged();
					
					if (statusList.size() < PAGE_SIZE) {
						statusListView.removeFooterView(footView);
					}
				} else {
					if (adapter != null) {
						adapter.notifyDataSetChanged();
						statusListView.removeFooterView(footView);
					}
				}
				break;
			case REFRESH_DATA:
				if (adapter != null) {
					if (statusList != null) {
						adapter.notifyDataSetChanged();
						if (statusList.size() < PAGE_SIZE) {
							statusListView.removeFooterView(footView);
						}
					}
				}
				threadRunning = false;
			default:
				break;
			}
		}
	};
	
	class LoadTopicsStatusThread extends Thread {
		private int startIndex;
		private boolean isRefresh;

		public LoadTopicsStatusThread(int startIndex, boolean isRefresh) {
			this.startIndex = startIndex;
			this.isRefresh = isRefresh;
			if (isRefresh == false && statusList != null && statusList.size() > 0) {
				statusList.clear();
			}
		}
		
		public void run() {
			
			List<Status> tempList = DataService.getTopicsStatusDataList(startIndex, topicName, PAGE_SIZE, AtMeTopicStatusListActivity.this);
			statusList.addAll(tempList);
			Message msg = dataHandler.obtainMessage();
			msg.obj = statusList;
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
			new LoadTopicsStatusThread(currentPage, true).start();
			
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
