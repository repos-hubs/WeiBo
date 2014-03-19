package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.NewsListItemAdapter;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.db.CategoryCacheDb;
import com.kindroid.hub.proto.CommonProtoc.ItemType;
import com.kindroid.hub.proto.WeiboContentProtoc.Item;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;
import com.kindroid.hub.utils.Utils;

public class NewsListActivity extends Activity implements OnScrollListener, View.OnClickListener {
	private static final String TAG = "NewsListActivity";
	private final int HANDLE_DATA = 0;
	private final int REFRESH_DATA = 1;
	
	private ListView newsListView;
	private ImageView btnBack;
	private ImageView browserModeImageView;
	
	public static List<WeiboContent> newsList = new ArrayList<WeiboContent>();
	
	private NewsListItemAdapter adapter;
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private final static int PAGE_SIZE = 10;
	private int currentPage = 1;
	
	private boolean threadRunning = false;
	private LinearLayout footView;
	private TextView titleTextView;
	
	public static WeiboContent weiboContent;
	private int from = 0;
	private String isFromSearch = "no";
	private int mDataType;
	
	private boolean isBrowseMode = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.hub_news_main);
		//获取来自的频道
		from = getIntent().getIntExtra("from", 1);
		mDataType = getIntent().getIntExtra("dataSource", 0);
		findViews();
		if (!Utils.checkNetwork(NewsListActivity.this)) {
			Toast.makeText(NewsListActivity.this, getResources().getString(R.string.msg_net_work_error), Toast.LENGTH_SHORT).show();
		} else {
			new LoadNewsThread(currentPage, false).start();
		}
		loadLocalData();
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
		
		browserModeImageView = (ImageView) findViewById(R.id.browserMode);
		browserModeImageView.setOnClickListener(this);
		newsListView = (ListView) findViewById(R.id.newsListView);
		titleTextView = (TextView) findViewById(R.id.lbl_title);
		
		String dataSource = getResources().getStringArray(R.array.titleArray)[mDataType];
		titleTextView.setText(dataSource + getSubTitle(from,this));
		
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footView = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		newsListView.addFooterView(footView);
		newsListView.setOnScrollListener(this);
	}
	
	public static String getSubTitle(int from,Context ctx){
		String title="";
		if (from == ItemType.Type.NEWS.getNumber()) {
			title = ctx.getResources().getString(R.string.home_news_title);
		} else if (from == ItemType.Type.FUNNY.getNumber()) {
			title = ctx.getResources().getString(R.string.home_laugh_title);
		} else if (from == ItemType.Type.BEAUTY.getNumber()) {
			title = ctx.getResources().getString(R.string.home_beauty_title);
		} else if (from == ItemType.Type.CONSTELLATION.getNumber()) {
			title = ctx.getResources().getString(R.string.home_constellation_title);
		} else if (from == ItemType.Type.RECREATION.getNumber()) {
			title = ctx.getResources().getString(R.string.home_recreation_title);
		} else if (from == ItemType.Type.MOVIE.getNumber()) {
			title = ctx.getResources().getString(R.string.home_movie_title);
		} else if (from == ItemType.Type.TECHNOLOGY.getNumber()) {
			title = ctx.getResources().getString(R.string.home_technology_title);
		} else if (from == ItemType.Type.SPORTS.getNumber()) {
			title = ctx.getResources().getString(R.string.home_sports_title);
		} else if (from == ItemType.Type.GAME.getNumber()) {
			title = ctx.getResources().getString(R.string.home_game_title);
		} else if (from == ItemType.Type.FASHION.getNumber()) {
			title = ctx.getResources().getString(R.string.home_fashion_title);
		} else if (from == ItemType.Type.STREET.getNumber()) {
			title = ctx.getResources().getString(R.string.home_streettake_title);
		} else if (from == ItemType.Type.SAYING.getNumber()) {
			title = ctx.getResources().getString(R.string.home_quotation_title);
		}else if (from == ItemType.Type.PET.getNumber()) {
			title = ctx.getResources().getString(R.string.home_pet_title);
		} else if (from == ItemType.Type.CAR.getNumber()) {
			title = ctx.getResources().getString(R.string.home_car_title);
		} else if (from == ItemType.Type.ENGLISH.getNumber()) {
			title = ctx.getResources().getString(R.string.home_english_title);
		} else if (from == ItemType.Type.TRAVEL.getNumber()) {
			title = ctx.getResources().getString(R.string.home_travel_title);
		} else if (from == ItemType.Type.BUSINESS.getNumber()) {
			title = ctx.getResources().getString(R.string.home_business_title);
		} else if (from == ItemType.Type.CREATIVE.getNumber()) {
			title = ctx.getResources().getString(R.string.home_originality_title);
		}
		return title;
	}
	
	//handle news result
	Handler dataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case HANDLE_DATA:
				if (newsList != null && newsList.size() > 0) {
					
					adapter.notifyDataSetChanged();
					
					if (newsList.size() < PAGE_SIZE) {
						newsListView.removeFooterView(footView);
					}
				} else {
					if (adapter != null) {
						adapter.notifyDataSetChanged();
						newsListView.removeFooterView(footView);
					}
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
	
	private void addCacheData(List<WeiboContent> list) {
		CategoryCacheDb weiboDb = new CategoryCacheDb(this).open();
		try {
			weiboDb.deleteWeibo(mDataType, from, String.valueOf(isBrowseMode));
			for (WeiboContent weiboContent : list) {
				try {
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
								weiboContent.getItem().getLocName(),"1");
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
								weiboContent.getItem().getSinaId() + "","1");
					}
				} catch (Exception e) {
					continue;
				}
			}
		
			weiboDb.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			weiboDb.close();
		}
	}
	
	/**
	 * get latest 10 share record
	 * @return
	 */
	private List<WeiboContent> getCacheData() {

		CategoryCacheDb weiboDb = new CategoryCacheDb(this).open();
		Log.v(TAG, "from website:" + mDataType + "-->channel:" + from);
		Cursor cursor = weiboDb.getWeiboList(mDataType, from, String.valueOf(isBrowseMode));
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
	
	/**
	 * load cache data
	 */
	private void loadLocalData() {
		newsList.clear();
		newsList.addAll(getCacheData());
		if (adapter == null) {
			adapter = new NewsListItemAdapter(NewsListActivity.this, from, mDataType, isFromSearch, newsList);
		}
		newsListView.setAdapter(adapter);
		
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
	
	class LoadNewsThread extends Thread {
		private int startIndex;
		private boolean isRefresh;

		public LoadNewsThread(int startIndex, boolean isRefresh) {
			this.startIndex = startIndex;
			this.isRefresh = isRefresh;
			if (isRefresh == false && newsList != null && newsList.size() > 0) {
				if (Utils.checkNetwork(NewsListActivity.this) && currentPage != 1) {
					newsList.clear();
				}
			}
		}
		
		public void run() {
			
			List<WeiboContent> tempList = DataService.getNewsListData(startIndex, from, mDataType, isBrowseMode, NewsListActivity.this);
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
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		lastItem = firstVisibleItem + visibleItemCount - 1;
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (!threadRunning && adapter != null && lastItem == adapter.getCount() && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			threadRunning = true;
			currentPage = currentPage + 1;
			new LoadNewsThread(currentPage, true).start();
			
		}
	}
	
	
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.browserMode) {
			if (Utils.checkNetwork(NewsListActivity.this)) {
				
				Intent intent = new Intent(NewsListActivity.this, AsyncImageDataFlow.class);
				intent.putExtra("fromTag", "activity");
				intent.putExtra("from", from);
				intent.putExtra("dataSource", mDataType);
				startActivity(intent);
			} else {
				Toast.makeText(NewsListActivity.this, getResources().getString(R.string.msg_net_work_error), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
}
