package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.List;

import weibo4android.Comment;
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
import com.kindroid.hub.adapter.CommentItemAdapter;
import com.kindroid.hub.data.DataService;

public class AtMeCommentsActivity extends Activity implements OnScrollListener, View.OnClickListener {
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
	private CommentItemAdapter adapter;
	
	public static List<Comment> commentsList = new ArrayList<Comment>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.at_me_hub_reply);
		findViews();
		showProgressDialog();
		new LoadCommentsData(currentPage, false).start();
		
	}
	
	//handle news result
	Handler dataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			hideLoadingDialog();
			switch (msg.arg1) {
			case HANDLE_DATA:
				if (commentsList != null && commentsList.size() > 0) {
					
					adapter = new CommentItemAdapter(commentsList, AtMeCommentsActivity.this);
					statusListView.setAdapter(adapter);
					adapter.notifyDataSetChanged();
					
					if (commentsList.size() < PAGE_SIZE) {
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
					if (commentsList != null) {
						adapter.notifyDataSetChanged();
						if (commentsList.size() < PAGE_SIZE) {
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
	
	class LoadCommentsData extends Thread {

		private int startIndex;
		private boolean isRefresh;

		public LoadCommentsData(int startIndex, boolean isRefresh) {
			this.startIndex = startIndex;
			this.isRefresh = isRefresh;
			if (isRefresh == false && commentsList != null && commentsList.size() > 0) {
				commentsList.clear();
			}
		}
		
		@Override
		public void run() {
			Message msg = dataHandler.obtainMessage();
			List<Comment> tempList = DataService.getCommentsList(AtMeCommentsActivity.this, startIndex);
			commentsList.addAll(tempList);
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
			new LoadCommentsData(currentPage, true).start();
			
		}
	}
}
