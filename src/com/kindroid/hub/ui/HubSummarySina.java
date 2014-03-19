package com.kindroid.hub.ui;



import java.util.ArrayList;
import java.util.List;

import weibo4android.Paging;
import weibo4android.User;
import weibo4android.Weibo;
import weibo4android.WeiboException;
import weibo4android.Status;


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
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class HubSummarySina extends BaseActivity implements OnClickListener,OnScrollListener,OnItemClickListener{

	private ImageView mPublishWeibo;
	private ImageView mRefreshWeibo;
	
	private ListView mWeiboListView;
	private TextView mTips;
	private TextView mNickname;
	private String mNickNameStr;

	private List<Status> mListStatus = new ArrayList<Status>();
	private List<Status> mPageList;
	private IndexAdapter mIndexAdapter;
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private int currentPage = 1;
	private boolean running = false;
	private LinearLayout footView;
	
	private LinearLayout footView1;
	public boolean isOpen = false;
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
					mIndexAdapter = new IndexAdapter(HubSummarySina.this, mListStatus);
					mWeiboListView.setAdapter(mIndexAdapter);
					if (mPageList.size() < 10) {
						mWeiboListView.removeFooterView(footView);
					}
				} else {
					if (mIndexAdapter != null) {
						mIndexAdapter.notifyDataSetChanged();
						mWeiboListView.removeFooterView(footView);
					}
				}
				break;
			case 1:
				if (mIndexAdapter != null) {
					if (mPageList != null) {
						mListStatus.addAll(mPageList);
						mIndexAdapter.notifyDataSetChanged();
						if (mPageList.size() < 10) {
							mWeiboListView.removeFooterView(footView);
						}
					}
				}
				running = false;
				break;
			case 2:
				if (mNickNameStr != null) {
					mNickname.setText(mNickNameStr);
				}
				break;
			default:
				break;
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_summary_sina);
		findViews();
		if (checkLogin()) {
			showProgressDialog();
			mWeiboListView.setVisibility(View.VISIBLE);
			mTips.setVisibility(View.INVISIBLE);
			new LoadData(1,false).start();
		} else {
			mWeiboListView.setVisibility(View.INVISIBLE);
			mTips.setVisibility(View.VISIBLE);
		}
	}
	
	private void findViews() {
		mPublishWeibo = (ImageView) findViewById(R.id.publishWeibo);
		mPublishWeibo.setOnClickListener(this);
		mRefreshWeibo = (ImageView) findViewById(R.id.refreshWeibo);
		mRefreshWeibo.setOnClickListener(this);

		mWeiboListView = (ListView) findViewById(R.id.listViewSina);
		mWeiboListView.setOnItemClickListener(this);
		mWeiboListView.setOnScrollListener(this);
		mTips = (TextView) findViewById(R.id.noLoginTips);

		mNickname = (TextView) findViewById(R.id.nickNameTextView);
		
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footView = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		
		mWeiboListView.addFooterView(footView);
		
		footView1 = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		getWindow().addContentView(footView1, new ViewGroup.LayoutParams(100, 100));
		footView1.setVisibility(View.GONE);
	}
	
	public void toggle() {
		TranslateAnimation tranAnim = null;
		isOpen = !isOpen;
		if (isOpen) {
			footView1.setVisibility(View.VISIBLE);
			tranAnim = new TranslateAnimation(0.0f, 0.0f, 0, 100.0f);
			tranAnim.setFillAfter(true);
		} else {
			tranAnim = new TranslateAnimation(0.0f, 0.0f, 100.0f, 0);
			tranAnim.setAnimationListener(collapseListener);
			tranAnim.setFillAfter(false);
		}
		tranAnim.setDuration(300);
		tranAnim.setInterpolator(new AccelerateInterpolator(1.0f));
		footView1.startAnimation(tranAnim);
	}

	Animation.AnimationListener collapseListener = new Animation.AnimationListener() {
		public void onAnimationEnd(Animation animation) {
			footView1.setVisibility(View.GONE);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}
	};
	
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.publishWeibo:
			toggle();
/*			if (!checkLogin()) {
				Intent intent = new Intent(this, WeiboLogin.class);
				startActivity(intent);
			} else {
				Intent intent = new Intent(this, HubTribeWriteWeibo.class);
				startActivity(intent);
			}*/
			break;
		case R.id.refreshWeibo:
			if (!checkLogin()) {
				Intent intent = new Intent(this, WeiboLogin.class);
				startActivity(intent);
			}else{
				currentPage = 1;
				showProgressDialog();
				new LoadData(1, false).start();
			}
			break;
		default:
			break;
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
			Weibo weibo=new Weibo();
			weibo.setToken(UserDefaultInfo.getWeiboToken(HubSummarySina.this), UserDefaultInfo.getWeiboTokenSecret(HubSummarySina.this));
			Paging paging=new Paging(startIndex,10);
			try {
				mPageList = weibo.getFriendsTimeline(paging);
				if(!isRefresh){
					mHandler.sendEmptyMessage(0);
				}else{
					mHandler.sendEmptyMessage(1);
				}
				User user = weibo.showUser(UserDefaultInfo.getWeiboId(HubSummarySina.this));
				if (user != null) {
					mNickNameStr = user.getName();
				}
				mHandler.sendEmptyMessage(2);
			} catch (WeiboException e) {
				e.printStackTrace();
			} catch(Exception ex){
				ex.printStackTrace();
			}
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
			new LoadData(currentPage,true).start();
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

}
