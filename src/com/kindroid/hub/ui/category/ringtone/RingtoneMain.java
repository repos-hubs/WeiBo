package com.kindroid.hub.ui.category.ringtone;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.RingtonesListAdapter;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.proto.CommonProtoc.WallPaperOrRingType;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.RingOrWallPaper;
import com.kindroid.hub.provider.RingtoneProvider;
import com.kindroid.hub.ui.HubForwardActivity;
import com.kindroid.hub.ui.category.ringtone.DownloadManager.ViewType;
import com.kindroid.hub.ui.category.ringtone.SubcategoryOrderBarControl.OnTabClickListener;

public class RingtoneMain extends Activity implements View.OnClickListener, OnTabClickListener{
	private static final String TAG ="RingtoneMain";
	private SubcategoryOrderBarControl mOrderBarFrame;
	//Title bar
	private ImageView mGoBackBtn, mSearchBtn;
	
	//preview list
	public static Cursor mRingsCursor;
	private View mFooterView;
	private ListView mPreviewListView;
	private TextView mLoadingText;
	private ProgressBar mLoadingProgressBar;
	private RingtonesListAdapter mPreviewAdapter;

	private Handler mHandler;
	//refresh interval (500ms)
	private int mManuallyRefreshInterval = 500;
	private boolean mIsNeedManuallyRefresh = true;

	private RefreshTimer mRefreshTimer;
	private boolean mIsEnableAutoRefresh = false;
	//Automatically refresh every 60s
	private int mAutoRefreshInterval = 60 *1000;
	private long mLastRefreshTime;
	private DownloadMessagesTask mDownloadTask;
	private LoadMessagesTask mLoadTask;
	private WallPaperOrRingType.Type mSubType;
	private static final int mRequestMessagesCount = 10;
	private int[] mRequestMessagesIndex = new int[4];
	private int[] mPositionIndex = new int[4];
	private ContentResolver mCr;
	private int lastItem = 0;
	private ViewType mViewType;

	private static final String SELECTION = HistoryDBHelper.TYPE + "=?";

	/**
	 * indicates which tab current preview list belongs to.
	 * Optional value: 1(recommand) 2(newest) 3(top) 4(other)
	 */
	private int mMessageType;
	
	private RingtoneManager mRingtoneManager;

	 class RefreshTimer extends Timer {
         private TimerTask timerTask = null;

         protected void clear() {
             timerTask = null;
         }

         protected synchronized void schedule(long delay) {
             if (!mIsEnableAutoRefresh || timerTask != null) return;
             if (delay < 0) {
                 downloadMessages();
             } else {
                 timerTask = new RefreshTimerTask();
                 schedule(timerTask, delay);
             }
         }
     }

     class RefreshTimerTask extends TimerTask {
         @Override
         public void run() {
        	 downloadMessages();
         }
     }
     
     private class LoadMessagesTask extends AsyncTask<Void, Void, Cursor> {

		@Override
		protected Cursor doInBackground(Void... params) {
			if (mMessageType == HistoryDBHelper.OTHER_TYPE) {
				Log.v(TAG, "local query");
				return mRingtoneManager.queryDownload(null, null);
			} else {
				return mRingtoneManager.queryHistory(SELECTION, new String[]{String.valueOf(mMessageType)});
			}
			
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			if (result.getCount() == 0) return;
			Log.v(TAG, "local result count " + result.getCount());
			//mRingsCursor = result;
			mPreviewAdapter.changeCursor(result);
			mPreviewListView.setSelection(mPositionIndex[mMessageType]);
			mLoadTask = null;
			startManagingCursor(result);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mLoadTask = null;
		}
		
		
     }

	 private class DownloadMessagesTask extends AsyncTask<Void, Void, Integer> {
		private List<RingOrWallPaper> ringtoneStructure;
		private static final int SUCCESS = 1;
		private static final int FAILED = 0;
		//represents no more messages and not needs to refresh list.
		private static final int EOF_EMPTY = 2;
		//represents no more messages but needs to refresh list.
		private static final int EOF = 3;
		private static final int CANCEL = 4;
		private int descriptionResID;
		private Drawable indicator;
		private int update = 0;

		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLoadingText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			mLoadingText.setText(R.string.msg_loading);
			mLoadingProgressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Integer doInBackground(Void... paramArrayOfParams) {
			Log.v(TAG, "Download messages");
			
			ringtoneStructure = DataService.getRingOrWallpaperList(true, mSubType, mRequestMessagesCount, mRequestMessagesIndex[mMessageType]);
			if (ringtoneStructure == null) {
				descriptionResID = R.string.report_no_network_error;
			    indicator = getResources().getDrawable(R.drawable.indicator_error);
			    Log.v(TAG, "Download messages failed");
				return FAILED;
			}
			int loadMsgsCount = ringtoneStructure.size();
			if (loadMsgsCount == 0) {
				descriptionResID = R.string.more;
			    indicator = getResources().getDrawable(R.drawable.refresh);
				return EOF_EMPTY;
			}
			//insert data into history table in ring.db
			if (isCancelled()) return CANCEL;
			String SELECTION = null;
			SELECTION = HistoryDBHelper.TYPE + "=" + mMessageType;
			if (mRequestMessagesIndex[mMessageType]==0) {
				mCr.delete(RingtoneProvider.PREVIEW_CONTENT_URL, SELECTION, null);
			}
			mRequestMessagesIndex[mMessageType] ++;
			ContentValues values = new ContentValues();
			Cursor tCursor = null;
			Uri isInsert = null;
			for (RingOrWallPaper ring:ringtoneStructure) {
				if (isCancelled()) return CANCEL;
				SELECTION = HistoryDBHelper.TYPE + "=" + mMessageType + " and " + HistoryDBHelper.RAW_ID+ "=" + ring.getId();
				mRingtoneManager.populateContentValues(values, ring, mMessageType);
				tCursor = mRingtoneManager.queryHistory(SELECTION, null);
				if (tCursor.moveToNext()) {
					mCr.update(RingtoneProvider.PREVIEW_CONTENT_URL, values, SELECTION, null);
					tCursor.close();
					values.clear();
					continue;
				}

				if (isCancelled()) return CANCEL;
				isInsert = mCr.insert(RingtoneProvider.PREVIEW_CONTENT_URL, values);
				if (isInsert != null) update ++;
			}
			Log.v(TAG, "count of downloaded messages " + loadMsgsCount);
			Log.v(TAG, "count of inserted messages " + update);
			Log.v(TAG, "current message type " + mMessageType);
			Log.v(TAG, "current requested index " + mRequestMessagesIndex[mMessageType]);
			if (loadMsgsCount < mRequestMessagesCount) {
				mIsNeedManuallyRefresh = false;
				descriptionResID = R.string.more;
			    indicator = getResources().getDrawable(R.drawable.refresh);
				return EOF;
			} else {
				mIsNeedManuallyRefresh = true;
				descriptionResID = R.string.msg_loading;
			    indicator = null;
				return SUCCESS;
			}
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			mLoadingText.setCompoundDrawablesWithIntrinsicBounds(indicator, null, null, null);
			mLoadingText.setText(descriptionResID);
			if (result == SUCCESS) {
				mLoadingProgressBar.setVisibility(View.VISIBLE);
				loadMessages();
			} else if (result == EOF) {
				loadMessages();
				mLoadingProgressBar.setVisibility(View.GONE);
			} else if (result == EOF_EMPTY) {
				mLoadingProgressBar.setVisibility(View.GONE);
				Log.v(TAG, "reach EOF with empty message");
			} else if (result == FAILED) {
				mLoadingProgressBar.setVisibility(View.GONE);
			} else if (update > 0) {
				loadMessages();
			} 
			mDownloadTask = null;
			mRefreshTimer.clear();
			mRefreshTimer.schedule(mAutoRefreshInterval);
		}
	 }

	 private void doDestroy(){
		 new Thread(new Runnable() {

				@Override
				public void run() {
					//check out whether we need to delete those cached old messages more than MAX_CACHE_MESSAGE
					//and only remain newest messages less than MAX_CACHE_MESSAGE.
					mRingtoneManager.deleteMessagesForMessageType();
				}
			}).start();
	 }
	 
	 @Override
	 public void onDestroy() {
		 super.onDestroy();
		 mRingtoneManager.stopPlayback();
		 mRingtoneManager.release();
		 doDestroy();
	 }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preview_ring_v2);
		mHandler = new Handler();
		mCr = getContentResolver();
		mRingtoneManager = RingtoneManager.newInstance(this, mHandler);
		initTitleBar();
		initOrderBar();
		initPreviewList();
		mRefreshTimer = new RefreshTimer();
		//initialize current tab, 0 default.
		mOrderBarFrame.initOrderSelection(0);
//		mRefreshTimer.schedule(-1);
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	
	
	@Override
	protected void onStop() {
		Log.v(TAG, "onStop");
		mRingtoneManager.pauseOrResumePlayback();
		super.onStop();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	private void initTitleBar() {
		mGoBackBtn = (ImageView) findViewById(R.id.icon_back);
		mGoBackBtn.setOnClickListener(this);
		mSearchBtn = (ImageView) findViewById(R.id.icon_search);
		mSearchBtn.setOnClickListener(this);
		((TextView) findViewById(R.id.frame_title)).setText(R.string.ringtone);
		
	}
	
	private void initOrderBar() {
		mOrderBarFrame = (SubcategoryOrderBarControl) findViewById(R.id.preview_order_bar_wrap_id);
		mOrderBarFrame.setBackgroundResource(R.drawable.tab_bg);
		TextView order = null;
		order = (TextView) mOrderBarFrame.findViewById(R.id.recommand_order);
		order.setText(R.string.tab_text_recommand_order);
		order = (TextView) mOrderBarFrame.findViewById(R.id.new_order);
		order.setText(R.string.tab_text_new_order);
		order = (TextView) mOrderBarFrame.findViewById(R.id.hot_order);
		order.setText(R.string.tab_text_hot_order);
		order = (TextView) mOrderBarFrame.findViewById(R.id.local_order);
		order.setText(R.string.tab_text_local_order);
		mOrderBarFrame.setOnTabClickListener(this);
	}
	
	private void addFooterView() {
		if (mFooterView == null) {
			LayoutInflater inflater = LayoutInflater.from(this);
			mFooterView = inflater.inflate(R.layout.ringtone_listview_loading, mPreviewListView, false);
			mLoadingProgressBar = (ProgressBar) mFooterView.findViewById(R.id.preview_loading_progressbar);
			mLoadingText = (TextView) mFooterView.findViewById(R.id.preview_loading_text);
		}
		mPreviewListView.addFooterView(mFooterView);
	}
	
	private void initPreviewList() {
		mPreviewListView = (ListView) findViewById(R.id.order_preview);
		mPreviewListView.setOnItemClickListener(new PreviewOnItemClickListener());
		mPreviewListView.setOnScrollListener(new PreviewOnScrollListener());
		mPreviewAdapter = new RingtonesListAdapter(this, mRingtoneManager, ViewType.PREVIEW);
		mPreviewAdapter.setListItemHandler(mHandler);
		addFooterView();
		mPreviewListView.setAdapter(mPreviewAdapter);
	}
	
	/**
	 * download data.
	 */
	private void downloadMessages() {
		if (mDownloadTask != null) mDownloadTask.cancel(true);
		mDownloadTask = new DownloadMessagesTask();
		mDownloadTask.execute();
	}
	/**
	 * load message from local DB.
	 */
	private void loadMessages() {
		if (mLoadTask != null) mLoadTask.cancel(true);
		mLoadTask = new LoadMessagesTask();
		mLoadTask.execute();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.icon_back:
			finish();
			break;
		case R.id.icon_search:
			Intent search = new Intent(this, RingSearch.class);
			startActivity(search);
			break;
		}
	}
	
	class PreviewOnItemClickListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position,
				long id) {
			if (position == mPreviewAdapter.getCount() || mPreviewAdapter.getCount() == 0) {
				downloadMessages();
			} else {
				RingtoneMessageListItem itemView = (RingtoneMessageListItem) view;
				Intent intent = RingtoneDetailsActivity.actionStartIntent(RingtoneMain.this, 
						itemView.getRingId(),position, mMessageType, mRequestMessagesIndex[mMessageType]);
				startActivity(intent);
//				RingtoneManager.clearRingDataCache();
				mRingtoneManager.stopPlayback();
				mRingtoneManager.removeDownloadProgressChangeListener();
			}
		}
	}
	
	class PreviewOnScrollListener implements AbsListView.OnScrollListener {

		@Override
		public void onScroll(AbsListView paramAbsListView, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastItem = firstVisibleItem + visibleItemCount - 1;
		}

		@Override
		public void onScrollStateChanged(AbsListView listView, int scrollState) {
			if (mDownloadTask == null && mPreviewAdapter != null && lastItem == mPreviewAdapter.getCount() && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				downloadMessages();
			}
		}
	}

	@Override
	public void onTabClickListener(View menuButton) {
		//FIXME
		int position = mPreviewListView.getFirstVisiblePosition();
		Log.v(TAG, "mPositionIndex " + position);
		mPositionIndex[mMessageType] = position;
		Log.v(TAG, "mIdIndex " + mPositionIndex[mMessageType]);
		mPreviewAdapter.changeCursor(null);
//		RingtoneManager.clearRingDataCache();
		switch(menuButton.getId()) {
		case R.id.recommand_order:
			mSubType = WallPaperOrRingType.Type.RECOMMEND;
			mMessageType = HistoryDBHelper.RECOMMAND_TYPE;
			mPreviewAdapter = new RingtonesListAdapter(this, mRingtoneManager, ViewType.PREVIEW);
			mPreviewAdapter.setListItemHandler(mHandler);
			mPreviewListView.removeFooterView(mFooterView);
			addFooterView();
			mPreviewListView.setAdapter(mPreviewAdapter);
//			mPreviewAdapter.setViewType(ViewType.PREVIEW);
			break;
		case R.id.new_order:
			mSubType = WallPaperOrRingType.Type.NEWEST;
			mMessageType = HistoryDBHelper.NEWEST_TYPE;
//			mPreviewAdapter.setViewType(ViewType.PREVIEW);
			mPreviewAdapter = new RingtonesListAdapter(this, mRingtoneManager, ViewType.PREVIEW);
			mPreviewAdapter.setListItemHandler(mHandler);
			mPreviewListView.removeFooterView(mFooterView);
			addFooterView();
			mPreviewListView.setAdapter(mPreviewAdapter);
			break;
		case R.id.hot_order:
			mSubType = WallPaperOrRingType.Type.TOP;
			mMessageType = HistoryDBHelper.TOP_TYPE;
//			mPreviewAdapter.setViewType(ViewType.PREVIEW);
			mPreviewAdapter = new RingtonesListAdapter(this, mRingtoneManager, ViewType.PREVIEW);
			mPreviewAdapter.setListItemHandler(mHandler);
			mPreviewListView.removeFooterView(mFooterView);
			addFooterView();
			mPreviewListView.setAdapter(mPreviewAdapter);
			break;
		case R.id.local_order:
			mSubType = WallPaperOrRingType.Type.OTHER;
			mMessageType = HistoryDBHelper.OTHER_TYPE;
//			mPreviewAdapter.setViewType(ViewType.LOCAL);
			mPreviewAdapter = new RingtonesListAdapter(this, mRingtoneManager, ViewType.LOCAL);
			mPreviewAdapter.setListItemHandler(mHandler);
			mPreviewListView.removeFooterView(mFooterView);
			mPreviewListView.setAdapter(mPreviewAdapter);
			loadMessages();
			return;
		}
		loadMessages();
		downloadMessages();
	}
}
