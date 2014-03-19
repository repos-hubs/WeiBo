package com.kindroid.hub.ui.category.ringtone;

import java.util.ArrayList;
import java.util.List;



import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.WallpaperOrRingAdapter;
import com.kindroid.hub.data.WallPaperData;
import com.kindroid.hub.proto.WeiboContentProtoc.Review;
import com.kindroid.hub.provider.RingtoneProvider;
import com.kindroid.hub.ui.category.ReviewsList;
import com.kindroid.hub.ui.category.ringtone.DownloadManager.ViewType;
import com.kindroid.hub.ui.category.wallpaper.WallPaperReviewBean;

public class RingtoneDetailsActivity extends Activity implements View.OnClickListener{

	private static final String TAG = "RingtoneDetailsActivity";
	//Title bar
	private ImageView mGoBackBtn, mSearchBtn;
	
	//Content panel
	private ImageView mIconImage, mCommentBtn, mShareBtn, mRingPlayBtn;
	private Button mDownBtn;
	private TextView mCreator, mShareTime, mRingOwner;
	private TextView mRingName, mRingLength, mRingSize, mRingDownloadCount;
	private SeekBar mSeekBar;
	private TextView mForwardTextView, mCommentTextView;
	
	//comments list
	private ListView mCommentsListView;
	public static List<Review> commentsList = new ArrayList<Review>();
	private View mFootView;
	private LinearLayout mCommentsListLayout;
	private WallpaperOrRingAdapter mCommentsListAdapter;
	public int reviewCount = 0;
	public int forwardCount = 0;
	private ProgressDialog dlgLoading;
	private int mCommentsPageIndex = 0;
	/**
	 * pagination
	 */
	private final static int PAGE_SIZE = 5;
	private Handler mHandler;
	private final int HANDLE_DATA = 0;
	private final int ERROR_GENERAL = 2;

	//Bottom bar
	private RelativeLayout mBottomPanel;
	private LinearLayout mNextButton, mPreviousButton;
	private TextView mNextTextView, mPreviousTextView;

	private Cursor mRingsCursor;
	private int mCursorPosition = -1;
	private int mMessageType;
	private long mRingId;

	private RingtoneManager mRingtoneManager;
	private RingtoneMessageListItem mRingtoneMsgItem;
	private LoadRingMessage mLoadRingMessage;
	private Context mContext;
	private ContentObserver sContactsObserver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_ring);
		mContext = this;
		
		//取得列表界面的选择位置
		mCursorPosition = getIntent().getIntExtra("position", -1);
		mMessageType = getIntent().getIntExtra("type", 0);
		mRingId = getIntent().getLongExtra("id", 0);
		initTitleBar();
		initContentPanel();
		mCommentsListLayout = (LinearLayout) findViewById(R.id.commentsListLayout);
		mCommentsListView = (ListView) findViewById(R.id.commentsListView);
		mFootView = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listview_footer, null, false);
		mCommentsListView.addFooterView(mFootView);
		mFootView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(RingtoneDetailsActivity.this,ReviewsList.class);
				intent.putExtra("isWallpaper", false);
				intent.putExtra("id", mRingId);
				startActivity(intent);
			}
		});
		mRingtoneMsgItem = (RingtoneMessageListItem) findViewById(R.id.ring_item_view);
		initBottomBar();

		mHandler =  new Handler() {
			@Override
			public void handleMessage(Message msg) {
				hideLoadingDialog();
				switch (msg.arg1) {
				case HANDLE_DATA:
					//rewrite comment count
					String fmtCommentsCount = getResources().getString(R.string.msg_comments_count);
					mCommentTextView.setText(String.format(fmtCommentsCount, reviewCount));
					mForwardTextView.setText(getResources().getString(R.string.msg_forwards_count,forwardCount));
					if (commentsList != null && commentsList.size() > 0) {
						
						mCommentsListAdapter = new WallpaperOrRingAdapter(RingtoneDetailsActivity.this, commentsList, mRingtoneMsgItem.getRingId(), false);
						mCommentsListView.setAdapter(mCommentsListAdapter);
						
						LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, (mCommentsListAdapter.getCount() + 1) * 200 - 1);
						mCommentsListLayout.setLayoutParams(lp1);
						mCommentsListAdapter.notifyDataSetChanged();
					} else {
						if (mCommentsListAdapter != null) {
							mCommentsListAdapter.notifyDataSetChanged();
							mCommentsListAdapter.notifyDataSetInvalidated();
						}
					}
					break;
				case ERROR_GENERAL:
					
					break;
				default:
					break;
				}
			}
		};
		sContactsObserver = new ContentObserver(mHandler) {
	        @Override
	        public void onChange(boolean selfUpdate) {
	            Log.v(TAG, "onChange selfUpdate" + selfUpdate);
	            queryRingHistory();
	            populateRingInfos();
	        }
	    };
	    mContext.getContentResolver().registerContentObserver(
                RingtoneProvider.PREVIEW_CONTENT_URL, true, sContactsObserver);
		mRingtoneManager = RingtoneManager.newInstance(this, mHandler);
		queryRingHistory();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mRingtoneManager.stopPlayback();
		mContext.getContentResolver().unregisterContentObserver(sContactsObserver);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		mRingtoneManager.stopPlayback();
	}

	@Override
	protected void onResume() {
		super.onResume();
		new LoadCommentsThread(mCommentsPageIndex, false).start();
	}

	private void initTitleBar() {
		mGoBackBtn = (ImageView) findViewById(R.id.icon_back);
		mGoBackBtn.setOnClickListener(this);
		mSearchBtn = (ImageView) findViewById(R.id.icon_search);
		mSearchBtn.setVisibility(View.GONE);
		((TextView) findViewById(R.id.frame_title)).setText(R.string.ringtone);
		
	}

	private void initContentPanel() {
		mIconImage = (ImageView) findViewById(R.id.ring_image);
		mCommentBtn = (ImageView) findViewById(R.id.ring_comment);
		mShareBtn = (ImageView) findViewById(R.id.ring_share);
		mDownBtn = (Button) findViewById(R.id.ring_download);
		mRingPlayBtn = (ImageView) findViewById(R.id.ring_player);

		mCommentBtn.setOnClickListener(this);
		mShareBtn.setOnClickListener(this);
		mDownBtn.setOnClickListener(this);
		
		mCreator = (TextView) findViewById(R.id.ring_share_person);
		mShareTime = (TextView) findViewById(R.id.ring_share_time);
		mRingOwner = (TextView) findViewById(R.id.ring_owner);
		mRingName = (TextView) findViewById(R.id.ring_name);
		mRingLength = (TextView) findViewById(R.id.ring_length);
		mRingSize = (TextView) findViewById(R.id.ring_size);
		mRingDownloadCount = (TextView) findViewById(R.id.ring_download_time);
		mForwardTextView = (TextView) findViewById(R.id.forwardTextView);
		mCommentTextView = (TextView) findViewById(R.id.commentTextView);
		
		mCommentTextView.setText(getString(R.string.msg_comments_count,0));
		mForwardTextView.setText(getString(R.string.msg_forwards_count,0));
		
		mSeekBar = (SeekBar) findViewById(R.id.ring_seekbar);
	}

	public  void refreshCommentsList() {
		new LoadCommentsThread(0, false).start();
	}

	private void initBottomBar() {
		mBottomPanel = (RelativeLayout) findViewById(R.id.detail_bottom_panel);
		mNextButton = (LinearLayout) findViewById(R.id.detail_next_button);
		mPreviousButton = (LinearLayout) findViewById(R.id.detail_previous_button);

		mPreviousTextView = (TextView) findViewById(R.id.detail_previous_button_text);
		mPreviousTextView.setText(R.string.ring_previous_text);
		mNextTextView = (TextView) findViewById(R.id.detail_next_button_text);
		mNextTextView.setText(R.string.ring_next_text);

		mBottomPanel.setBackgroundResource(R.drawable.detail_ring_bottom_bg);
		mNextButton.setBackgroundResource(R.drawable.detail_ring_bottom_button_background);
		mPreviousButton.setBackgroundResource(R.drawable.detail_ring_bottom_button_background);

		mNextButton.setOnClickListener(this);
		mPreviousButton.setOnClickListener(this);
	}
	
	public static Intent actionStartIntent(Context context, long id, int position, int messageType, int pageindex) {
        Intent intent = new Intent(context, RingtoneDetailsActivity.class);
		intent.putExtra("position", position);
		intent.putExtra("type", messageType);
		intent.putExtra("pageindex", pageindex);
		intent.putExtra("id", id);
        return intent;
    }
	
	private void queryRingHistory() {
		if (mLoadRingMessage != null) {
			mLoadRingMessage.cancel(true);
		}
		mLoadRingMessage = new LoadRingMessage();
		mLoadRingMessage.execute();
	}
	
	private void populateRingInfos() {
		RingtoneItemData data = new RingtoneItemData(mContext, mRingsCursor, mRingtoneManager);
		if (mRingtoneMsgItem.getRingtoneItemMessage() != null) {
			mRingtoneMsgItem.changeRingtoneItemData(data);
		} else {
			mRingtoneMsgItem.setListItemHandler(mHandler);
			mRingtoneMsgItem.bind(data, mRingtoneManager, ViewType.DETAIL);
		}
		mRingId = mRingtoneMsgItem.getRingId();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.ring_comment:
			break;
		case R.id.ring_share:
			break;
//		case R.id.ring_player:
//			break;
		case R.id.ring_download:
			break;
		case R.id.detail_next_button:
				if(mRingsCursor != null && !mRingsCursor.isAfterLast() && mRingsCursor.moveToNext()) {
					mCursorPosition++;
					populateRingInfos();
					new LoadCommentsThread(0, false).start();
				} else {
					mNextTextView.setTextColor(getResources().getColor(R.color.gray));
					Toast.makeText(this, getResources().getString(R.string.msg_last_item), Toast.LENGTH_SHORT).show();
				}
			break;
		case R.id.detail_previous_button:

			if (mRingsCursor != null && !mRingsCursor.isBeforeFirst() && mRingsCursor.moveToPrevious()) {
				mCursorPosition--;
				populateRingInfos();
				new LoadCommentsThread(0, false).start();
			} else {
				mPreviousTextView.setTextColor(getResources().getColor(R.color.gray));
				Toast.makeText(this, getResources().getString(R.string.msg_first_item), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.icon_back:
			finish();
			break;
		}
	}
	
	/**
	 * Show loading dialog
	 */
	private void showLoadingDialog() {
		if (dlgLoading == null) {
			dlgLoading = ProgressDialog.show(this, "", this.getString(R.string.msg_loading), true, true);
		} else {
			dlgLoading.show();
		}
	}
		
	/**
	 * Hide loading dialog
	 */
	private void hideLoadingDialog() {
		if (dlgLoading != null) {
			dlgLoading.dismiss();
		}
	}
	
	class LoadCommentsThread extends Thread {
		private int startIndex;
		private boolean isRefresh;

		public LoadCommentsThread(int startIndex, boolean isRefresh) {
			this.startIndex = startIndex;
			this.isRefresh = isRefresh;
			if (isRefresh == false && commentsList != null && commentsList.size() > 0) {
				commentsList.clear();
			}
		}
		
		public void run() {
			WallPaperReviewBean reviewBean = null;
			Message msg = mHandler.obtainMessage();
			reviewBean = WallPaperData.getWallPaperReviewBean(mRingId, false,PAGE_SIZE, startIndex);
			reviewCount = reviewBean.getReviewedCount();
			forwardCount = reviewBean.getForwardedCount();
			if (null != reviewBean.getListReview()) {
				commentsList.addAll(reviewBean.getListReview());
			}
			msg.obj = commentsList;
			if (!isRefresh) {
				msg.arg1 = HANDLE_DATA;
				mHandler.sendMessage(msg);
			} 
		}
	}
	
	class LoadRingMessage extends AsyncTask<Void, Void, Cursor> {

		@Override
		protected Cursor doInBackground(Void... params) {
			mRingsCursor = mRingtoneManager.queryHistory(HistoryDBHelper.TYPE + "=?", new String[]{String.valueOf(mMessageType)});
			return mRingsCursor;
		}
		
		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			if (mRingsCursor!=null && mRingsCursor.moveToPosition(mCursorPosition)) {
				//填充当前位置的ring数据
				populateRingInfos();
				//检索评论数据
//				new LoadCommentsThread(0, false).start();
			}
			mLoadRingMessage = null;
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			mLoadRingMessage = null;
		}
		
	}
}
