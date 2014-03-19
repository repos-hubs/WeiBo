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
import com.kindroid.hub.adapter.NewsListItemAdapter;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.proto.CommonProtoc.ItemType;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;

public class HubNewsSearchActivity extends Activity implements OnScrollListener, View.OnClickListener {
	private static final String TAG = "HubNewsSearchActivity";
	private final int HANDLE_DATA = 0;
	private final int REFRESH_DATA = 1;
	
	public static List<WeiboContent> newsList = new ArrayList<WeiboContent>();
	private ProgressDialog dlgLoading = null;
	private boolean threadRunning = false;
	
	private NewsListItemAdapter adapter;
	private ListView newsListView;
	private ImageView btnBack;
	private ImageView searchImageView;
	private EditText keyWordEditText;
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private int PAGE_INDEX = 1;
	private final static int PAGE_SIZE = 5;
	private int currentPage = 1;
	
	private LinearLayout footView;
	//title
	private TextView titleTextView;
	
	private int from = 0;
	private int mDataType = 0;
	private String isFromSearch = "yes";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_news_search);
		
		from = getIntent().getIntExtra("from", 1);
		mDataType = getIntent().getIntExtra("dataSource", 0);
		findViews();
		initComponents();
	}
	
	private void findViews() {
		btnBack = (ImageView) findViewById(R.id.btn_back);
		btnBack.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					finish();
				}
			}
		);
		keyWordEditText = (EditText) findViewById(R.id.txt_input);
		searchImageView = (ImageView) findViewById(R.id.btn_search);
		searchImageView.setOnClickListener(this);
		titleTextView = (TextView) findViewById(R.id.lbl_title);
		newsListView = (ListView) findViewById(R.id.newsListView);
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footView = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		newsListView.addFooterView(footView);
		newsListView.setOnScrollListener(this);
	}
	
	private void initComponents() {
		
		//设置标题
		/*if (!TextUtils.isEmpty(from) && from.equals("news")) {
			titleTextView.setText(getResources().getString(R.string.home_news_title));
		} else if (!TextUtils.isEmpty(from) && from.equals("laugh")) {
			titleTextView.setText(getResources().getString(R.string.home_laugh_title));
		} else if (!TextUtils.isEmpty(from) && from.equals("beauty")) {
			titleTextView.setText(getResources().getString(R.string.home_beauty_title));
		}*/
	}
	
	//handle news result
	Handler dataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			hideLoadingDialog();
			switch (msg.arg1) {
			case HANDLE_DATA:
				if (newsList != null && newsList.size() > 0) {
					
					adapter = new NewsListItemAdapter(HubNewsSearchActivity.this, from, mDataType, isFromSearch, newsList);
					newsListView.setAdapter(adapter);
					adapter.notifyDataSetChanged();
					
					if (newsList.size() < PAGE_SIZE) {
						newsListView.removeFooterView(footView);
					}
				} else {
					if (adapter != null) {
						adapter.notifyDataSetChanged();
						newsListView.removeFooterView(footView);
					}
					Toast.makeText(HubNewsSearchActivity.this, getResources().getString(R.string.msg_search_result_empty), Toast.LENGTH_SHORT).show();
				}
				break;
			case REFRESH_DATA:
				if (adapter != null) {
					if (newsList != null) {
						adapter.notifyDataSetChanged();
						if (newsList.size() < PAGE_SIZE) {
							newsListView.removeFooterView(footView);
						}
					}
				}
				threadRunning = false;
			default:
				break;
			}
		}
	};
	
	class SearchNewsThread extends Thread {
		private int startIndex;
		private boolean isRefresh;

		public SearchNewsThread(int startIndex, boolean isRefresh) {
			this.startIndex = startIndex;
			this.isRefresh = isRefresh;
			if (isRefresh == false && newsList != null && newsList.size() > 0) {
				newsList.clear();
			}
		}
		
		public void run() {
			
			List<WeiboContent> tempList = null;
			String keyWord = keyWordEditText.getText().toString().trim();
			tempList = DataService.searchWeiboListData(startIndex, keyWord, HubNewsSearchActivity.this, from);
			
			newsList.addAll(tempList);
			Message msg = dataHandler.obtainMessage();
			msg.obj = newsList;
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
			new SearchNewsThread(currentPage, true).start();
			
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

	@Override
	public void onClick(View view) {
		
		if (view.getId() == R.id.btn_search) {
			String keyword = keyWordEditText.getText().toString().trim();
			if (!TextUtils.isEmpty(keyword)) {
				showLoadingDialog();
				initParams();
				new SearchNewsThread(PAGE_INDEX, false).start();
			} else {
				Toast.makeText(HubNewsSearchActivity.this, getResources().getString(R.string.msg_keyword_not_empty), Toast.LENGTH_SHORT).show();
			}
		}		
	}
	
	private void initParams() {
		PAGE_INDEX = 1;
		currentPage = 1;
		lastItem = 0;
	}
	
}
