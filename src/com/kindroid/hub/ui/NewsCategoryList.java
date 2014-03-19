package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.List;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.NewsListItemAdapter;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.db.CategoryCacheDb;
import com.kindroid.hub.proto.WeiboContentProtoc.Item;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;
import com.kindroid.hub.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class NewsCategoryList extends Activity implements OnScrollListener, View.OnClickListener{
	
	private static final String TAG = "NewsCategoryList";
	
	public static List<WeiboContent> newsList = new ArrayList<WeiboContent>();
	private final int HANDLE_DATA = 0;
	private final int REFRESH_DATA = 1;
	
	private ListView newsListView;
	private NewsListItemAdapter mListItemAdapter;
	private ImageView btnBack;
	private ImageView browserModeImageView;
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private final static int PAGE_SIZE = 10;
	private int currentPage = 1;
	
	private boolean threadRunning = false;
	private LinearLayout footView;
	/**
	 * pagination
	 */
	private TextView titleTextView;
	private int mDataType;
	private int from = 0;
	private String mCategoryId;
	
	//handle news result
	Handler dataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case HANDLE_DATA:
				if (newsList != null && newsList.size() > 0) {
					mListItemAdapter.notifyDataSetChanged();
					if (newsList.size() < PAGE_SIZE) {
						newsListView.removeFooterView(footView);
					}
				} else {
					if (mListItemAdapter != null) {
						mListItemAdapter.notifyDataSetChanged();
						newsListView.removeFooterView(footView);
					}
				}
				break;
			case REFRESH_DATA:
				if (mListItemAdapter != null) {
					if (newsList != null) {
						mListItemAdapter.notifyDataSetChanged();
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_category_news);
		mDataType = getIntent().getIntExtra("dataSource", 0);
		from = getIntent().getIntExtra("from", 1);
		mCategoryId = getIntent().getStringExtra("categoryId");
		findViews();
		new LoadNewsThread(currentPage, false).start();
	}
	
	private void findViews() {
		btnBack = (ImageView) findViewById(R.id.btn_back);
		btnBack.setOnClickListener(this);
		
		browserModeImageView = (ImageView) findViewById(R.id.browserMode);
		browserModeImageView.setOnClickListener(this);
		newsListView = (ListView) findViewById(R.id.newsListView);
		titleTextView = (TextView) findViewById(R.id.lbl_title);
		String title = getIntent().getStringExtra("itemName");
		
		String dataSource = getResources().getStringArray(R.array.titleArray)[mDataType];
		titleTextView.setText(dataSource + title);
		
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footView = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		newsListView.addFooterView(footView);
		newsListView.setOnScrollListener(this);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadLocalData();
	}
	
	private void loadLocalData() {
		newsList.clear();
		newsList.addAll(getCacheData());
		if (mListItemAdapter == null) {
			mListItemAdapter = new NewsListItemAdapter(NewsCategoryList.this, from, mDataType, "yes", newsList);
		}
		newsListView.setAdapter(mListItemAdapter);
		
	}
	
	private List<WeiboContent> getCacheData() {

		CategoryCacheDb weiboDb = new CategoryCacheDb(this).open();
		Log.v(TAG, "from website:" + mDataType + "-->channel:" + from);
		Cursor cursor = weiboDb.getWeiboListByUserId(mDataType, from, String.valueOf(false),mCategoryId);
		List<WeiboContent> list = new ArrayList<WeiboContent>();
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			WeiboContent.Builder weiboContent = WeiboContent.newBuilder();
			Item.Builder item = Item.newBuilder();
			item.setIcon(cursor.getString(cursor.getColumnIndexOrThrow(CategoryCacheDb.KEY_AVATAR_URL)));
			item.setName(cursor.getString(cursor.getColumnIndexOrThrow(CategoryCacheDb.KEY_USER_TITLE)));
			if (mDataType == 1) {
				item.setLocName(cursor.getString(cursor.getColumnIndexOrThrow(CategoryCacheDb.KEY_USER_ID)));
			} else {
				item.setSinaId(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(CategoryCacheDb.KEY_USER_ID))));
			}
			weiboContent.setItem(item);
			
			weiboContent.setContent(cursor.getString(cursor.getColumnIndexOrThrow(CategoryCacheDb.KEY_DATA_CONTENT)));
			weiboContent.setImg1(cursor.getString(cursor.getColumnIndexOrThrow(CategoryCacheDb.KEY_DATA_CONTENT_URL)));
			weiboContent.setRetweetedstatus(cursor.getString(cursor.getColumnIndexOrThrow(CategoryCacheDb.KEY_EXIST_FORWARD)));//forward or not 
			weiboContent.setRetweetedcontent(cursor.getString(cursor.getColumnIndexOrThrow(CategoryCacheDb.KEY_FORWARD_CONTENT)));
			weiboContent.setImg3(cursor.getString(cursor.getColumnIndexOrThrow(CategoryCacheDb.KEY_FORWARD_CONTENT_URL)));// forward image url
			weiboContent.setWeibocontentId(cursor.getString(cursor.getColumnIndexOrThrow(CategoryCacheDb.KEY_WEIBO_DATA_ID)));// weiboId
			weiboContent.setTimeLeft(cursor.getString(cursor.getColumnIndexOrThrow(CategoryCacheDb.KEY_CREATE_TIME)));
			weiboContent.setSource(cursor.getString(cursor.getColumnIndexOrThrow(CategoryCacheDb.KEY_FROM_CLIENT)));// from client
			list.add(weiboContent.build());
		}
		cursor.close();
		weiboDb.close();
		return list;
	}
	
	private void addCacheData(List<WeiboContent> list) {
		CategoryCacheDb weiboDb = new CategoryCacheDb(this).open();

		weiboDb.deleteUserWeibo(mDataType, from, String.valueOf(false),
				mCategoryId);
		for (WeiboContent weiboContent : list) {
			if (mDataType == 1) {
				weiboDb.insertWeiboTable(weiboContent.getItem().getIcon(),
						weiboContent.getItem().getName(),
						weiboContent.getTimeLeft(),
						weiboContent.getWeibocontentId(),
						weiboContent.getContent(), weiboContent.getImg1(),
						weiboContent.getRetweetedcontent(),
						weiboContent.getImg3(), weiboContent.getSource(),
						mDataType, from, weiboContent.getRetweetedstatus(),
						String.valueOf(weiboContent.getSwitchPic()),
						weiboContent.getItem().getLocName(),"2");
			} else {
				weiboDb.insertWeiboTable(weiboContent.getItem().getIcon(),
						weiboContent.getItem().getName(),
						weiboContent.getTimeLeft(),
						weiboContent.getWeibocontentId(),
						weiboContent.getContent(), weiboContent.getImg1(),
						weiboContent.getRetweetedcontent(),
						weiboContent.getImg3(), weiboContent.getSource(),
						mDataType, from, weiboContent.getRetweetedstatus(),
						String.valueOf(weiboContent.getSwitchPic()),
						weiboContent.getItem().getSinaId() + "","2");
			}
		}
		weiboDb.close();
	}
	
	private List<WeiboContent> checkSameData(List<WeiboContent> local, List<WeiboContent> network){
		List<WeiboContent> returnArray=new ArrayList<WeiboContent>();
		for (int i = 0; i < network.size(); i++) {
			boolean result = false;
			for (int j = 0; j < local.size(); j++) {
				if (local.get(j).getContentId() == network.get(i).getContentId()) {
					result = true;
					break;
				}
			}
			if(!result){
				returnArray.add(network.get(i));
			}
		}
		return returnArray;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.browserMode:
			Intent intent = new Intent(this, AsyncImageDataFlow.class);
			intent.putExtra("fromTag", "activity");
			intent.putExtra("from", from);
			intent.putExtra("dataSource", mDataType);
			intent.putExtra("categoryId", mCategoryId);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
		lastItem = firstVisibleItem + visibleItemCount - 1;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (!threadRunning && mListItemAdapter != null && lastItem == mListItemAdapter.getCount() && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			threadRunning = true;
			currentPage = currentPage + 1;
			new LoadNewsThread(currentPage, true).start();
			
		}
	}
	
	class LoadNewsThread extends Thread {
		private int startIndex;
		private boolean isRefresh;

		public LoadNewsThread(int startIndex, boolean isRefresh) {
			this.startIndex = startIndex;
			this.isRefresh = isRefresh;
			if (isRefresh == false && newsList != null && newsList.size() > 0) {
				if (Utils.checkNetwork(NewsCategoryList.this)) {
					newsList.clear();
				}
			}
		}
		
		public void run() {
			
			List<WeiboContent> tempList = DataService.getNewsListData(startIndex, from, mDataType, false, NewsCategoryList.this,mCategoryId);
			if (tempList != null && tempList.size() > 0) {
				addCacheData(tempList);
			}
			newsList.addAll(checkSameData(newsList, tempList));
			Message msg = dataHandler.obtainMessage();
			msg.obj = tempList;
			if (!isRefresh) {
				msg.arg1 = HANDLE_DATA;
				dataHandler.sendMessage(msg);
			} else {
				msg.arg1 = REFRESH_DATA;
				dataHandler.sendMessage(msg);
			}
		}
	}

}
