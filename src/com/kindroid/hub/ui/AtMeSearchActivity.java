package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.TribeAdapter;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.proto.CommonProtoc.ItemType;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;

public class AtMeSearchActivity extends Activity implements View.OnClickListener, OnScrollListener {
	private static final String TAG = "AtMeSearchActivity";
	final int HANDLE_SEARCH_DATA = 0;
	final int REFRESH_SEARCH_DATA = 1;
	
	private int PAGE_INDEX = 1;
	private String keyWord = "";
	private String fromTab = "";
	private List<WeiboContent> weiboList = new ArrayList<WeiboContent>();
	private List<WeiboContent> userList = new ArrayList<WeiboContent>();
	
	//define components
	private ListView searchListView;
	private ImageView btnBack;
	
	private TribeAdapter adapter;
	private ImageView btnSearch;
	
	private LinearLayout searchUserLayout;
	private TextView searchUserTextView;
	private LinearLayout searchWeiboLayout;
	private TextView searchWeiboTextView;
	private ImageView searchUserImageView;
	private ImageView searchWeiboImageView;
	private EditText keywordEditText;
	private ProgressDialog dlgLoading = null;
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private final static int PAGE_SIZE = 10;
	private int currentPage = 0;
	
	private boolean threadRunning = false;
	private LinearLayout footView;
	
	private boolean searchWeibo = false;
	private int from = ItemType.Type.BULUO.getNumber();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.at_me_hub_myself_search);
		findViews();
	}
	
	class SearchThread extends Thread {
		private int startIndex;
		private boolean isRefresh;

		public SearchThread(int startIndex, boolean isRefresh) {
			this.startIndex = startIndex;
			this.isRefresh = isRefresh;
			if (searchWeibo) {
				if (isRefresh == false && weiboList != null && weiboList.size() > 0) {
					weiboList.clear();
				}
			} else {
				if (isRefresh == false && userList != null && userList.size() > 0) {
					userList.clear();
				}
			}
		}
		
		public void run() {
			List tmpList = new ArrayList();
			Message msg = dataHandler.obtainMessage();
			if (searchWeibo) {
				tmpList = DataService.searchWeiboListData(startIndex, keyWord, AtMeSearchActivity.this, from);
				weiboList.addAll(tmpList);
				msg.obj = weiboList;
			} else {
//				tmpList = DataService.searchUsers(keyWord, currencyCode, startIndex, AtMeSearchActivity.this);
				userList.addAll(tmpList);
				msg.obj = userList;
			}
			
			if (!isRefresh) {
				msg.arg1 = HANDLE_SEARCH_DATA;
				dataHandler.sendMessage(msg);
			} else {
				msg.arg1 = REFRESH_SEARCH_DATA;
				dataHandler.sendMessage(msg);
			}
		}
	}
	//handle search result
	Handler dataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case HANDLE_SEARCH_DATA:
				if (searchWeibo) {
					
					if (weiboList != null && weiboList.size() > 0) {
						adapter = new TribeAdapter(AtMeSearchActivity.this, weiboList);
						searchListView.setAdapter(adapter);
						adapter.notifyDataSetChanged();
						
						if (weiboList.size() < PAGE_SIZE) {
							searchListView.removeFooterView(footView);
						}
					} else {
						if (adapter != null) {
							adapter.notifyDataSetChanged();
							searchListView.removeFooterView(footView);
						}
						Toast.makeText(AtMeSearchActivity.this, getResources().getString(R.string.msg_search_result_empty), Toast.LENGTH_SHORT).show();
					}
				} else {
					if (userList != null && userList.size() > 0) {
						adapter = new TribeAdapter(AtMeSearchActivity.this, userList);
						searchListView.setAdapter(adapter);
						adapter.notifyDataSetChanged();
						
						if (userList.size() < PAGE_SIZE) {
							searchListView.removeFooterView(footView);
						}
					} else {
						if (adapter != null) {
							adapter.notifyDataSetChanged();
							searchListView.removeFooterView(footView);
						}
					}
				}
				break;
			case REFRESH_SEARCH_DATA:
				if (adapter != null) {
					if (searchWeibo) {
						
						if (weiboList != null) {
							adapter.notifyDataSetChanged();
							if (weiboList.size() < PAGE_SIZE) {
								searchListView.removeFooterView(footView);
							}
						}
					} else {
						if (userList != null) {
							adapter.notifyDataSetChanged();
							if (userList.size() < PAGE_SIZE) {
								searchListView.removeFooterView(footView);
							}
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
	
	private void findViews() {
		//title
		searchUserTextView = (TextView) findViewById(R.id.searchUserTextView);
		searchUserTextView.setOnClickListener(this);
		searchUserImageView = (ImageView) findViewById(R.id.searchUserImageView);
		searchUserLayout = (LinearLayout) findViewById(R.id.searchUserLayout);
		searchWeiboTextView = (TextView) findViewById(R.id.searchWeiboTextView);
		searchWeiboTextView.setOnClickListener(this);
		searchWeiboImageView = (ImageView) findViewById(R.id.searchWeiboImageView);
		searchWeiboLayout = (LinearLayout) findViewById(R.id.searchWeiboLayout);
		btnSearch = (ImageView) findViewById(R.id.btn_search);
		btnSearch.setOnClickListener(this);
		keywordEditText = (EditText) findViewById(R.id.txt_input);
		
		searchListView = (ListView) findViewById(R.id.friendsListView);
		searchListView.setOnScrollListener(this);
		btnBack = (ImageView) findViewById(R.id.btn_back);
		btnBack.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					hideLoadingDialog();
					AtMeSearchActivity.this.finish();
				}
			}
		);
		
//		searchListView.setOnItemClickListener(new SearchListOnItemClick());
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footView = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		searchListView.addFooterView(footView);
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
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
		lastItem = firstVisibleItem + visibleItemCount - 1;
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
		if (!threadRunning && adapter != null && lastItem == adapter.getCount() && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			threadRunning = true;
			currentPage = currentPage + 1;
			new SearchThread(currentPage, true).start();
			
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.searchUserTextView) {
			searchUserImageView.setImageResource(R.drawable.icon_friend_on);
			searchUserLayout.setBackgroundResource(R.drawable.at_me_search_title_bg);
			searchUserTextView.setTextColor(getResources().getColor(R.color.white));
			searchWeiboImageView.setImageResource(R.drawable.icon_recognize);
			searchWeiboLayout.setBackgroundDrawable(null);
			searchWeiboTextView.setTextColor(getResources().getColor(R.color.black));
			searchWeibo = false;
		} else if (view.getId() == R.id.searchWeiboTextView) {
			searchUserImageView.setImageResource(R.drawable.icon_friend);
			searchUserLayout.setBackgroundDrawable(null);
			searchUserTextView.setTextColor(getResources().getColor(R.color.black));
			searchWeiboImageView.setImageResource(R.drawable.icon_recognize_on);
			searchWeiboLayout.setBackgroundResource(R.drawable.at_me_search_title_bg);
			searchWeiboTextView.setTextColor(getResources().getColor(R.color.white));
			searchWeibo = true;
		} else if (view.getId() == R.id.btn_search) {
			keyWord = keywordEditText.getText().toString().trim();
			if (!TextUtils.isEmpty(keyWord)) {
				showLoadingDialog();
				new SearchThread(PAGE_INDEX, false).start();
			} else {
				Toast.makeText(AtMeSearchActivity.this, getResources().getString(R.string.msg_keyword_not_empty), Toast.LENGTH_SHORT).show();
			}
		}
	}
}
