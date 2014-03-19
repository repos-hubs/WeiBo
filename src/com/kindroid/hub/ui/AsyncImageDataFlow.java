package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.List;

import org.taptwo.android.widget.TitleFlowIndicator;
import org.taptwo.android.widget.ViewFlow;
import org.taptwo.android.widget.ViewFlow.ViewSwitchListener;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.AsyncAdapter;
import com.kindroid.hub.adapter.NewsListItemAdapter;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.db.CategoryCacheDb;
import com.kindroid.hub.proto.WeiboContentProtoc.Item;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;
import com.kindroid.hub.utils.AsyncImageLoader;
import com.kindroid.hub.utils.Utils;

public class AsyncImageDataFlow extends Activity implements ViewSwitchListener {
	private final static String TAG = "AsyncImageDataFlow";
	private final int HANDLE_DATA = 0;
	private final int SDCARD_NOT_EXIST = 1;
	private final int SAVE_IMAGE_SUCCESS = 2;
	private final int SAVE_IMAGE_FAILURE = 3;
	private ViewFlow viewFlow;

	private AsyncAdapter adapter;
	
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private final static int PAGE_SIZE = 10;
	private int currentPage = 1;
	
	private int mDataType;
	private int from;
	public static List<WeiboContent> weiboList = new ArrayList<WeiboContent>();
	private boolean isImageBrowse = true;
	
	private String fromTag;
	private String mCategoryId = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_news_image_layout);
		viewFlow = (ViewFlow) findViewById(R.id.viewflow);
		viewFlow.setOnViewSwitchListener(this);
		//from news list item image click
		fromTag = getIntent().getStringExtra("fromTag");
		//获取来自的频道
		from = getIntent().getIntExtra("from", 1);
		mDataType = getIntent().getIntExtra("dataSource", 0);
		mCategoryId = getIntent().getStringExtra("categoryId");
		if (!TextUtils.isEmpty(fromTag) && fromTag.equals("item")) {
			int position = getIntent().getIntExtra("position", -1);
			weiboList.clear();
			if (mCategoryId != null) {
				weiboList.add(NewsCategoryList.newsList.get(position));
			} else {
				weiboList.add(NewsListActivity.newsList.get(position));
			}
			if (!Utils.checkNetwork(AsyncImageDataFlow.this)) {
				Toast.makeText(AsyncImageDataFlow.this, getResources().getString(R.string.msg_net_work_error), Toast.LENGTH_SHORT).show();
			}
		} else {
			new LoadImage(currentPage).start();
		}
    }
    
    Handler dataHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case HANDLE_DATA:
//				adapter = new AsyncAdapter(this);
//				viewFlow.setAdapter(adapter);
				Log.v(TAG, "weiboList size:" + weiboList.size());
				adapter.notifyDataSetChanged();
				break;
			case SDCARD_NOT_EXIST:
				Toast.makeText(AsyncImageDataFlow.this, getResources().getString(R.string.msg_sdcard_not_exist), Toast.LENGTH_SHORT).show();
				break;
			case SAVE_IMAGE_SUCCESS:
				Toast.makeText(AsyncImageDataFlow.this, getResources().getString(R.string.msg_save_success), Toast.LENGTH_SHORT).show();
				break;
			case SAVE_IMAGE_FAILURE:
				Toast.makeText(AsyncImageDataFlow.this, getResources().getString(R.string.msg_save_failure), Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
    	
    };
    
    private void addCacheData(List<WeiboContent> list) {
		CategoryCacheDb weiboDb = new CategoryCacheDb(this).open();
		
		String type = "1";
		if (mCategoryId != null) {
			type = "2";
		}
		
		try {
			weiboDb.deleteWeibo(mDataType, from, String.valueOf(isImageBrowse));
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
								weiboContent.getItem().getLocName(),type);
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
								weiboContent.getItem().getSinaId() + "",type);
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
		
		Cursor cursor = null;
		if (mCategoryId != null) {
			cursor = weiboDb.getImgWeiboList(mDataType, from,String.valueOf(isImageBrowse));
		}else{
			cursor = weiboDb.getWeiboListByUserId(mDataType, from,String.valueOf(isImageBrowse),mCategoryId);
		}
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
		if (!TextUtils.isEmpty(fromTag) && fromTag.equals("activity")) {
			weiboList.clear();
			weiboList.addAll(getCacheData());
		}
		if (adapter == null) {
			adapter = new AsyncAdapter(AsyncImageDataFlow.this, weiboList, dataHandler);
		}
		viewFlow.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadLocalData();
	}

	private List<WeiboContent> checkSameData(List<WeiboContent> local, List<WeiboContent> network){
		List<WeiboContent> returnArray = new ArrayList<WeiboContent>();
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
	
    class LoadImage extends Thread {
    	
    	int pageIndex;
    	public LoadImage(int curPage) {
    		this.pageIndex = curPage;
    	}
		@Override
		public void run() {
			List<WeiboContent> tempList = null;
			if (mCategoryId != null) {
				tempList = DataService.getNewsListData(pageIndex, from,mDataType, isImageBrowse, AsyncImageDataFlow.this,mCategoryId);
			}else{
				tempList = DataService.getNewsListData(pageIndex, from,mDataType, isImageBrowse, AsyncImageDataFlow.this);
			}
			if (tempList != null && tempList.size() > 0) {
				addCacheData(tempList);
			}
			Log.v(TAG, "pageIndex:" + pageIndex);
			Log.v("check size:", "" + checkSameData(weiboList, tempList).size());
			weiboList.addAll(checkSameData(weiboList, tempList));
//			weiboList.addAll(tempList);
			Message msg = dataHandler.obtainMessage();
			msg.obj = tempList;
			msg.arg1 = HANDLE_DATA;
			dataHandler.sendMessage(msg);
		}
    }

	@Override
	public void onSwitched(View arg0, int position) {
		Log.v(TAG, "position:" + position);
		if (!Utils.checkNetwork(AsyncImageDataFlow.this)) {
			Toast.makeText(AsyncImageDataFlow.this, getResources().getString(R.string.msg_net_work_error), Toast.LENGTH_SHORT).show();
		} else {
			
			if (position == adapter.getCount() - 1) {
				Log.v(TAG, "adapter.getcount:" + adapter.getCount());
				currentPage = currentPage + 1;
				new LoadImage(currentPage).start();
			}
		}
	}
    
}
