package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.List;

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
import com.kindroid.hub.adapter.FriendsItemAdapter;

public class AtMeFriendActivity extends Activity implements View.OnClickListener, OnScrollListener {
	private static final String TAG = "AtMeFriendActivity";
	private final int HANDLE_DATA = 0;
	private final int REFRESH_DATA = 1;
	
	private LinearLayout friendLayout;
	private LinearLayout friendTitleBgLayout;
	private TextView friendTextView;
	private LinearLayout recognizeLayout;
	private TextView recognizeTextView;
	private ImageView friendImageView;
	private ImageView recognizeImageView;
	private ListView friendsListView;
	
	private FriendsItemAdapter adapter;
	private List friendsList = new ArrayList();
	
	private ProgressDialog dlgLoading = null;
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private int PAGE_INDEX = 1;
	private final static int PAGE_SIZE = 10;
	private int currentPage = 0;
	
	private boolean threadRunning = false;
	private LinearLayout footView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.at_me_hub_friend);
		
		findViews();
	}
	
	private void findViews() {
		friendTextView = (TextView) findViewById(R.id.lblFriendTextView);
		friendTextView.setOnClickListener(this);
		friendLayout = (LinearLayout) findViewById(R.id.friendLayout);
		friendTitleBgLayout = (LinearLayout) findViewById(R.id.friendTitleBgLayout);
		recognizeLayout = (LinearLayout) findViewById(R.id.recognizeLayout);
		recognizeTextView = (TextView) findViewById(R.id.lblRecognizeTextView);
		recognizeTextView.setOnClickListener(this);
		friendImageView = (ImageView) findViewById(R.id.friendIamgeView);
		recognizeImageView = (ImageView) findViewById(R.id.recognizeImageView);
		friendsListView = (ListView) findViewById(R.id.friendListView);
		
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footView = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		friendsListView.addFooterView(footView);
	}
	
	//handle search result
	Handler dataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case HANDLE_DATA:
				if (friendsList != null && friendsList.size() > 0) {
					
					adapter = new FriendsItemAdapter(AtMeFriendActivity.this, friendsList);
					friendsListView.setAdapter(adapter);
					adapter.notifyDataSetChanged();
					
					if (friendsList.size() < PAGE_SIZE) {
						friendsListView.removeFooterView(footView);
					}
				} else {
					if (adapter != null) {
						adapter.notifyDataSetChanged();
						friendsListView.removeFooterView(footView);
					}
				}
				break;
			case REFRESH_DATA:
				if (adapter != null) {
					if (friendsList != null) {
						adapter.notifyDataSetChanged();
						if (friendsList.size() < PAGE_SIZE) {
							friendsListView.removeFooterView(footView);
						}
					}
				}
				threadRunning = false;
			default:
				break;
			}
			hideLoadingDialog();
		}
	};

	class LoadFriendsThread extends Thread {
		private int startIndex;
		private boolean isRefresh;

		public LoadFriendsThread(int startIndex, boolean isRefresh) {
			this.startIndex = startIndex;
			this.isRefresh = isRefresh;
			if (isRefresh == false && friendsList != null && friendsList.size() > 0) {
				friendsList.clear();
			}
		}
		
		public void run() {
			
			List tempList = null;
//			tempList = DataService.LoadFrends(startIndex, AtMeFriendActivity.this);
			friendsList.addAll(tempList);
			Message msg = dataHandler.obtainMessage();
			msg.obj = friendsList;
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
	public void onClick(View view) {
		if (view.getId() == R.id.lblFriendTextView) {
			friendImageView.setImageResource(R.drawable.icon_friend_on);
			friendLayout.setBackgroundResource(R.drawable.at_me_friend_tab_red);
//			friendTitleBgLayout.setBackgroundResource(R.drawable.at_me_friend_tab_red_left);
			friendTextView.setTextColor(getResources().getColor(R.color.white));
			recognizeImageView.setImageResource(R.drawable.icon_recognize);
			recognizeLayout.setBackgroundResource(R.drawable.at_me_friend_tab_gray_bg);
			recognizeTextView.setTextColor(getResources().getColor(R.color.at_me_friend_title));
		} else if (view.getId() == R.id.lblRecognizeTextView) {
			friendImageView.setImageResource(R.drawable.icon_friend);
			friendLayout.setBackgroundResource(R.drawable.at_me_friend_tab_gray_bg);
//			friendTitleBgLayout.setBackgroundResource(R.drawable.at_me_friend_tab_red_right);
			friendTextView.setTextColor(getResources().getColor(R.color.at_me_friend_title));
			recognizeImageView.setImageResource(R.drawable.icon_recognize_on);
			recognizeLayout.setBackgroundResource(R.drawable.at_me_friend_tab_red);
			recognizeTextView.setTextColor(getResources().getColor(R.color.white));
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
//			new LoadFriendsThread(currentPage, true, isLocal).start();
			
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
