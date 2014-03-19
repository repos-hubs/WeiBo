package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.List;

import weibo4android.User;
import weibo4android.Weibo;
import weibo4android.WeiboException;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.FollowItemAdapter;
import com.kindroid.hub.data.UserDefaultInfo;
import com.kindroid.hub.utils.Constant;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class SinaFollowMore extends BaseActivity implements OnClickListener,OnItemClickListener,OnScrollListener{
	private ImageView mBackBtn;
	
	private GridView mStatusGridView;
	private FollowItemAdapter mFollowItemAdapter;
	
	private List<User> mListStatus = new ArrayList<User>();
	private List<User> mPageList;
	private String mId;
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private int currentPage = 1;
	private boolean running = false;
	/**
	 * pagination
	 */
	private boolean mIsFans;
	public static final String TYPE = "type";
	private ProgressBar mProgressBar;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			mProgressDialog.dismiss();
			switch (msg.what) {
			case 0:
				if (null != mPageList) {
					mListStatus.clear();
					mListStatus.addAll(mPageList);
					mFollowItemAdapter = new FollowItemAdapter(SinaFollowMore.this, mListStatus);
					mStatusGridView.setAdapter(mFollowItemAdapter);
					if (mPageList.size() < 10) {
					}
				} else {
					if (mFollowItemAdapter != null) {
						mFollowItemAdapter.notifyDataSetChanged();
					}
				}
				break;
			case 1:
				if (mFollowItemAdapter != null) {
					if (mPageList != null) {
						mListStatus.addAll(mPageList);
						mFollowItemAdapter.notifyDataSetChanged();
						if (mPageList.size() < 10) {
						}
					}
				}
				running = false;

				mProgressBar.setVisibility(View.INVISIBLE);
				break;
			case 2:
				mProgressBar.setVisibility(View.INVISIBLE);
				Toast.makeText(SinaFollowMore.this, R.string.user_register_connect_error, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sina_follow_more);
		findViews();
		showProgressDialog();
		Intent intent = getIntent();
		if (intent != null) {
			mId = intent.getStringExtra(UserDetailInfo.U_ID);
			mIsFans = intent.getBooleanExtra(TYPE, false);
		}
		currentPage = 1;
		new LoadData(1, false).start();
	}
	
	private void findViews() {
		mBackBtn = (ImageView) findViewById(R.id.backImageView);
		mBackBtn.setOnClickListener(this);
		
		mStatusGridView=(GridView)findViewById(R.id.usersGridView);
		mStatusGridView.setOnItemClickListener(this);
		mStatusGridView.setOnScrollListener(this);
		
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
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
			Intent intent = new Intent(this, UserDetailInfo.class);
			intent.putExtra(UserDetailInfo.U_ID, mListStatus.get(position).getId());
			startActivity(intent);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
		lastItem = firstVisibleItem + visibleItemCount;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (!running && lastItem == mFollowItemAdapter.getCount() && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			running = true;
			currentPage = currentPage + 1;
			mProgressBar.setVisibility(View.VISIBLE);
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
			Weibo weibo=new Weibo();
			weibo.setToken(UserDefaultInfo.getWeiboToken(SinaFollowMore.this), UserDefaultInfo.getWeiboTokenSecret(SinaFollowMore.this));
			try {
				if (mIsFans) {
					mPageList = weibo.getFollowersStatuses(mId, startIndex,12);
				} else {
					mPageList = weibo.getFriendsStatuses(mId, startIndex,12);
				}
				if(!isRefresh){
					mHandler.sendEmptyMessage(0);
				}else{
					mHandler.sendEmptyMessage(1);
				}
			} catch (WeiboException e) {
				mHandler.sendEmptyMessage(2);
				e.printStackTrace();
			} catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}

}
