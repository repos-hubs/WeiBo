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
import android.widget.LinearLayout;
import android.widget.ListView;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.NewsListItemAdapter;
import com.kindroid.hub.adapter.StatusItemAdapter;
import com.kindroid.hub.data.DataService;

public class AtMeStatusActivity extends Activity implements OnScrollListener, View.OnClickListener {
	private final String TAG = "AtMeReplayActivity";
	private final int HANDLE_DATA = 0;
	private final int REFRESH_DATA = 1;
	private ListView statusListView;
	
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private int PAGE_INDEX = 1;
	private final static int PAGE_SIZE = 15;
	private int currentPage = 1;
	
	private boolean threadRunning = false;
	private LinearLayout footView;
	private ProgressDialog dlgLoading = null;
	private StatusItemAdapter adapter;
	
	public static List<Status> statusList = new ArrayList<Status>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.at_me_hub_reply);
		findViews();
		showProgressDialog();
		new LoadAtStatusData(currentPage, false).start();
		
	}
	
	//handle news result
	Handler dataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			hideLoadingDialog();
			switch (msg.arg1) {
			case HANDLE_DATA:
				if (statusList != null && statusList.size() > 0) {
					
					adapter = new StatusItemAdapter(statusList, AtMeStatusActivity.this);
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

	private void findViews() {
		statusListView = (ListView) findViewById(R.id.replayListView);
		
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footView = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		statusListView.addFooterView(footView);
		statusListView.setOnScrollListener(this);
	}
	
	class LoadAtStatusData extends Thread {

		private int startIndex;
		private boolean isRefresh;

		public LoadAtStatusData(int startIndex, boolean isRefresh) {
			this.startIndex = startIndex;
			this.isRefresh = isRefresh;
			if (isRefresh == false && statusList != null && statusList.size() > 0) {
				statusList.clear();
			}
		}
		
		@Override
		public void run() {
			Message msg = dataHandler.obtainMessage();
			List<Status> tempList = DataService.getStatusList(AtMeStatusActivity.this, startIndex);
			statusList.addAll(tempList);
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
	
	/**
	 * Show loading dialog
	 */
	private void showProgressDialog() {
		dlgLoading = new ProgressDialog(this.getParent());
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
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
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
			new LoadAtStatusData(currentPage, true).start();
			
		}
	}
}
