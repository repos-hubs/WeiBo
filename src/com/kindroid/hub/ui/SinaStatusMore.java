package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.List;

import weibo4android.Paging;
import weibo4android.Status;
import weibo4android.Weibo;
import weibo4android.WeiboException;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.IndexAdapter;
import com.kindroid.hub.data.UserDefaultInfo;
import com.kindroid.hub.utils.Constant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class SinaStatusMore extends BaseActivity implements OnClickListener,OnItemClickListener,OnScrollListener{
	public final static String DATA_TYPE = "data_type";
	private ImageView mBackBtn;
	
	private ListView mStatusListView;
	private IndexAdapter mIndexAdapter;
	
	private List<Status> mListStatus = new ArrayList<Status>();
	private List<Status> mPageList;
	private String mId;
	private int mType;//1:favorite
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private int currentPage = 1;
	private boolean running = false;
	private LinearLayout footView;
	/**
	 * pagination
	 */
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			mProgressDialog.dismiss();
			switch (msg.what) {
			case 0:
				if (null != mPageList) {
					mListStatus.clear();
					mListStatus.addAll(mPageList);
					mIndexAdapter = new IndexAdapter(SinaStatusMore.this, mListStatus);
					mStatusListView.setAdapter(mIndexAdapter);
					if (mPageList.size() < 10) {
						mStatusListView.removeFooterView(footView);
					}
				} else {
					if (mIndexAdapter != null) {
						mIndexAdapter.notifyDataSetChanged();
						mStatusListView.removeFooterView(footView);
					}
				}
				break;
			case 1:
				if (mIndexAdapter != null) {
					if (mPageList != null) {
						mListStatus.addAll(mPageList);
						mIndexAdapter.notifyDataSetChanged();
						if (mPageList.size() < 10) {
							mStatusListView.removeFooterView(footView);
						}
					}
				}
				running = false;
				break;
			default:
				break;
			}
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sina_status_more);
		findViews();
		showProgressDialog();
		Intent intent = getIntent();
		if (intent != null) {
			mType = intent.getIntExtra(DATA_TYPE, -1);
			mId = intent.getStringExtra(UserDetailInfo.U_ID);
		}
		new LoadData(1, false).start();
	}
	
	private void findViews() {
		mBackBtn = (ImageView) findViewById(R.id.backImageView);
		mBackBtn.setOnClickListener(this);
		
		mStatusListView=(ListView)findViewById(R.id.listViewStatus);
		mStatusListView.setOnItemClickListener(this);
		mStatusListView.setOnScrollListener(this);
		
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footView = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		mStatusListView.addFooterView(footView);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.backImageView:
			finish();
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		if (position < mListStatus.size()) {
			Intent intent = new Intent(this, HubSinaWeiboDetailsActivity.class);
			intent.putExtra("listPosition", position);
			HubSinaWeiboDetailsActivity.statusList = mListStatus;
			startActivity(intent);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
		lastItem = firstVisibleItem + visibleItemCount - 1;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (!running && lastItem == mIndexAdapter.getCount() && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			running = true;
			currentPage = currentPage + 1;
			new LoadData(currentPage, true).start();
		}
	}
	
	private class LoadData extends Thread {
		private int startIndex;
		private boolean isRefresh;

		public LoadData(int startIndex, boolean isRefresh) {
			this.startIndex = startIndex;
			this.isRefresh = isRefresh;
		}
		public void run() {
			System.setProperty("weibo4j.oauth.consumerKey", Constant.CONSUMER_KEY);
			System.setProperty("weibo4j.oauth.consumerSecret", Constant.CONSUMER_SECRET);
			Weibo weibo = new Weibo();
			weibo.setToken(UserDefaultInfo.getWeiboToken(SinaStatusMore.this), UserDefaultInfo.getWeiboTokenSecret(SinaStatusMore.this));
			Paging paging = new Paging(startIndex, 10);
			try {
				if (mType == 1) {
					mPageList = weibo.getFavorites(startIndex);
				} else {
					mPageList = weibo.getUserTimeline(mId, paging);
				}
				if(!isRefresh){
					mHandler.sendEmptyMessage(0);
				}else{
					mHandler.sendEmptyMessage(1);
				}
			} catch (WeiboException e) {
				e.printStackTrace();
			} catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}

}
