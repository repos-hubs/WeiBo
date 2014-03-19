package com.kindroid.hub.ui.category;

import java.util.ArrayList;
import java.util.List;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.WallpaperOrRingAdapter;
import com.kindroid.hub.data.WallPaperData;
import com.kindroid.hub.proto.WeiboContentProtoc.Review;
import com.kindroid.hub.ui.category.wallpaper.WallPaperReviewBean;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ReviewsList extends Activity implements OnScrollListener,OnClickListener{
	
	private ProgressDialog mProgressDialog;
	
	private ListView mCommentsListView;
	public static List<Review> commentsList = new ArrayList<Review>();
	public List<Review> mPageList;
	private WallpaperOrRingAdapter mCommentsListAdapter;
	
	private ImageView mBackBtn;
	
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
	private long mId;
	private boolean isWallpaper;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			mProgressDialog.dismiss();
			switch (msg.what) {
			case 0:
				if (null != mPageList) {
					commentsList.clear();
					commentsList.addAll(mPageList);
					mCommentsListAdapter = new WallpaperOrRingAdapter(ReviewsList.this, commentsList,mId,isWallpaper);
					mCommentsListView.setAdapter(mCommentsListAdapter);
					if (mPageList.size() < 10) {
						mCommentsListView.removeFooterView(footView);
					}
				} else {
					if (mCommentsListAdapter != null) {
						mCommentsListAdapter.notifyDataSetChanged();
						mCommentsListView.removeFooterView(footView);
					}
				}
				break;
			case 1:
				if (mCommentsListAdapter != null) {
					if (mPageList != null) {
						commentsList.addAll(mPageList);
						mCommentsListAdapter.notifyDataSetChanged();
						if (mPageList.size() < 10) {
							mCommentsListView.removeFooterView(footView);
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
		setContentView(R.layout.hub_news_comments);
		findViews();
		showProgressDialog();
		mId = getIntent().getLongExtra("id", 0);
		isWallpaper = getIntent().getBooleanExtra("isWallpaper", false);
		new LoadCommentsThread(0, false).start();
	}
	
	
	private void findViews() {
		mBackBtn = (ImageView) findViewById(R.id.btn_back);
		mBackBtn.setOnClickListener(this);
		mCommentsListView = (ListView) findViewById(R.id.newsCommentsListView);
		mCommentsListView.setOnScrollListener(this);
		
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footView = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		
		mCommentsListView.addFooterView(footView);
	}


	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
		lastItem = firstVisibleItem + visibleItemCount - 1;
	}


	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (!running && lastItem == mCommentsListAdapter.getCount() && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			running = true;
			currentPage = currentPage + 1;
			new LoadCommentsThread(currentPage, true).start();
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		default:
			break;
		}
	}
	
	class LoadCommentsThread extends Thread {
		private int startIndex;
		private boolean isRefresh;

		public LoadCommentsThread(int startIndex, boolean isRefresh) {
			this.startIndex = startIndex;
			this.isRefresh = isRefresh;
		}

		public void run() {
			WallPaperReviewBean reviewBean = WallPaperData.getWallPaperReviewBean(mId, isWallpaper,10, startIndex);
			mPageList = reviewBean.getListReview();
			if (!isRefresh) {
				mHandler.sendEmptyMessage(0);
			} else {
				mHandler.sendEmptyMessage(1);
			}
		}
	}
	
	private void showProgressDialog(){
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle(getResources().getText(R.string.app_name));
		mProgressDialog.setMessage(getResources().getText(R.string.progress_message));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();
	}

}
