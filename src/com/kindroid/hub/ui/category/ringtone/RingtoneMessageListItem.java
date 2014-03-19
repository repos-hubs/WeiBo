package com.kindroid.hub.ui.category.ringtone;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kindroid.hub.R;
import com.kindroid.hub.ui.HubForwardActivity;
import com.kindroid.hub.ui.HubReplyActivity;
import com.kindroid.hub.ui.NewsDetailsActivity;
import com.kindroid.hub.ui.category.WallPaperOrringReply;
import com.kindroid.hub.ui.category.ringtone.DownloadManager.CallbackOnDownloadCompleted;
import com.kindroid.hub.ui.category.ringtone.DownloadManager.OnDownloadProgressChangedListener;
import com.kindroid.hub.ui.category.ringtone.DownloadManager.ViewType;
import com.kindroid.hub.ui.category.ringtone.RingtoneManager.OnStateChangedListener;

public class RingtoneMessageListItem extends LinearLayout{
	private static final String TAG = "RingtoneMessageListItem";
	private ImageView mRingImageBtn;
	private TextView mRingCreator;
	private TextView mRingTimeShared;
	private ImageView mRingShareBtn;
	private ImageView mRingCommentBtn;
	private TextView mRingOwner;
	private TextView mRingTitle;
	private TextView mRingLength;
	private Button mRingDownloadBtn;
	private SeekBar mRingSeekBar;
	private ImageView mRingPlayerBtn;
	
	private TextView mRingCommentTextView, mRingForwardTextView;
	private TextView mRingSizeTextView, mRingDownloadCoutTextView;
	
	private StateListDrawable mRingPause, mRingPlay;
	private RingtoneItemData mRintoneItemData;
	private Context mContext;
	private RingtoneManager mRingtoneManager;
	private Handler mHandler;
	/**
	 * true表示可以直接播放，false表示需要先下载(缓冲)
	 * 这个状态表示的是音乐文件要么存在于缓存目录中，要么存在于下载目录中
	 */
//	private boolean isEnablePlayOrNeedDownload = false;
	private ViewType mViewType;
	private long mId;
	private int mPosition;
	
	 public RingtoneMessageListItem(Context context) {
	        super(context);
	        mContext = context;
	    }

	    public RingtoneMessageListItem(Context context, AttributeSet attrs) {
	        super(context, attrs);
	        mContext = context;
	    }

	    @Override
	    protected void onFinishInflate() {
	        super.onFinishInflate();
	        mRingImageBtn = (ImageView) findViewById(R.id.ring_image);
			mRingCreator = (TextView) findViewById(R.id.ring_share_person);
			mRingTimeShared = (TextView) findViewById(R.id.ring_share_time);
			mRingShareBtn = (ImageView) findViewById(R.id.ring_share);
			mRingCommentBtn = (ImageView) findViewById(R.id.ring_comment);
			mRingDownloadBtn = (Button) findViewById(R.id.ring_download);
			mRingPlayerBtn = (ImageView) findViewById(R.id.ring_player);
			mRingSeekBar = (SeekBar) findViewById(R.id.ring_seekbar);
			mRingOwner = (TextView) findViewById(R.id.ring_owner);
			mRingTitle = (TextView) findViewById(R.id.ring_name);
			mRingLength = (TextView) findViewById(R.id.ring_length);
			mRingCommentTextView = (TextView) findViewById(R.id.commentTextView);
			mRingForwardTextView = (TextView) findViewById(R.id.forwardTextView);
			mRingSizeTextView = (TextView) findViewById(R.id.ring_size);
			mRingDownloadCoutTextView = (TextView) findViewById(R.id.ring_download_time);
	    }

	    /**
	     * 在该方法之前应该首先设置{@link #setListItemHandler(Handler)}
	     */
	    public void bind(RingtoneItemData ringtoneItemData, RingtoneManager ringtoneManager, ViewType viewType) {
	    	Log.v(TAG, "view id " + toString());
	    	mRingtoneManager = ringtoneManager;
	    	mRintoneItemData = ringtoneItemData;
	    	mId = ringtoneItemData.rRawId;
	    	mViewType = viewType;
	    	setDefaultView();
            mRingCreator.setText(mRintoneItemData.rCreator);
            mRingTimeShared.setText(mRintoneItemData.rTimeShare);
            mRingOwner.setText(mRintoneItemData.rOwner);
            mRingTitle.setText(mRintoneItemData.rName);
            mRingLength.setText(String.valueOf(mRintoneItemData.rLength));

            if (mViewType == ViewType.PREVIEW) {
                mRingPause = (StateListDrawable) mContext.getResources().getDrawable(R.drawable.ringtone_player_pause_background_small);
            	mRingPlay = (StateListDrawable) mContext.getResources().getDrawable(R.drawable.ringtone_player_background_small);
                setDownloadButtonStatu();
                findViewById(R.id.dot_line).setVisibility(View.VISIBLE);
    	    	findViewById(R.id.ring_preview_item_top).setVisibility(View.VISIBLE);
            } else if (mViewType == ViewType.LOCAL ){
            	mRingPause = (StateListDrawable) mContext.getResources().getDrawable(R.drawable.ringtone_player_pause_background_small);
            	mRingPlay = (StateListDrawable) mContext.getResources().getDrawable(R.drawable.ringtone_player_background_small);
            	setLocalView();
            } else {
            	mRingPause = (StateListDrawable) mContext.getResources().getDrawable(R.drawable.ringtone_player_pause_background);
            	mRingPlay = (StateListDrawable) mContext.getResources().getDrawable(R.drawable.ringtone_player_background);
            	
//            	File downloadFile = ringtoneManager.isFileExistInDownloadDir(
//            			ringtoneManager.buildFullNameWithID(mRintoneItemData.rName, mRintoneItemData.rRawId, mRintoneItemData.rMimeType));
//        		if (downloadFile != null) {
//        			mRintoneItemData.rLocalPath = downloadFile.getAbsolutePath();
//        			mRintoneItemData.rStatu = HistoryDBHelper.STATU_DOWNLOADED;
//        		}
            	setDownloadButtonStatu();
            }
            mRingPlayerBtn.setBackgroundDrawable(mRingPlay);
            if (mRintoneItemData.rStatu == HistoryDBHelper.STATU_CACHED || mRintoneItemData.rStatu == HistoryDBHelper.STATU_DOWNLOADED) {
            	setEnablePlayView();
            } else if(mRintoneItemData.rStatu == HistoryDBHelper.STATU_DOWNLOADING) {
            	setDownloadingView();
            } else {
            	setDisblePlayView();
            }
            //判断曲目是否在播放，设置对应的background
            if (mRingtoneManager.isPlaying(mId)){
            	Log.v(TAG, "playing view");
//            	mRingPlayerBtn.setBackgroundDrawable(mRingPause);
            	setPlayingView();
            } else if (mRingtoneManager.isPausing(mId)){
//            	mRingPlayerBtn.setBackgroundDrawable(mRingPlay);
            	setPausingView();
            } else {
            	mRingSeekBar.setProgress(0);
            	mRingPlayerBtn.setBackgroundDrawable(mRingPlay);
            }
            
//            setForwardAndCommentButtons();
	    }
	    
	    public void changeRingtoneItemData(RingtoneItemData ringtoneData) {
	    	bind(ringtoneData, mRingtoneManager, mViewType);
	    	postInvalidate();
	    }
	    
	    private void setDownloadButtonStatu() {
	    	if (mRintoneItemData.rStatu == HistoryDBHelper.STATU_DOWNLOADED) {
            	mRingDownloadBtn.setText(R.string.msg_downloaded);
            	mRingDownloadBtn.setEnabled(false);
            } else if (mRintoneItemData.rStatu == HistoryDBHelper.STATU_CACHED){
            	mRingDownloadBtn.setText(R.string.msg_cached);
            	mRingDownloadBtn.setEnabled(true);
			} else if (mRintoneItemData.rStatu == HistoryDBHelper.STATU_DOWNLOADING){
				//正在下载中，按钮不可用
				mRingDownloadBtn.setText(R.string.msg_downloading);
				mRingDownloadBtn.setEnabled(false);
			} else {
				mRingDownloadBtn.setText(R.string.content_download);
            	mRingDownloadBtn.setEnabled(true);
			}
	    	mRingDownloadBtn.setOnClickListener(new ItemDownloadHandler());
	    }
	    
	    /**
	     * 设置为可播放视图状态
	     */
	    private void setEnablePlayView() {
	    	Log.v(TAG, "setEnablePlayView()");
	    	mRingSeekBar.setVisibility(View.VISIBLE);
	    	mRingSeekBar.setSecondaryProgress(mRingSeekBar.getMax());
        	mRingSeekBar.setVisibility(View.VISIBLE);
        	mRingSeekBar.setClickable(true);
        	mRingPlayerBtn.setOnClickListener(new ItemPlayHandler(this));
        	mRingSeekBar.setOnSeekBarChangeListener(new ItemSeekBarChangeHandler(this));
        	mRingtoneManager.setOnStateChangedListener(new ItemStateChangedListener(this));
        	if (mRingtoneManager.isAutoPlaying(mId)) {
        		mRingPlayerBtn.performClick();
        	}
	    }
	    /**
	     * 设置为是播放中的视图状态
	     */
	    private void setPlayingView() {
	    	Log.v(TAG, "setPlayingView");
        	mRingSeekBar.setProgress(mRingtoneManager.getProgress(mId));
        	mRingPlayerBtn.setBackgroundDrawable(mRingPause);
//        	mRingPlayerBtn.setOnClickListener(new ItemPlayHandler());
//	    	mRingSeekBar.setOnSeekBarChangeListener(new ItemSeekBarChangeHandler());
//	    	resetPlayerOnStateChangeListener();
	    	updateTimerView();
	    }
	    /**
	     * 设置为是暂停时的视图状态
	     */
	    private void setPausingView() {
	    	Log.v(TAG, "setPausingView");
//        	mRingSeekBar.setClickable(true);
//        	resetPlayerOnStateChangeListener();
//        	mRingSeekBar.setOnSeekBarChangeListener(new ItemSeekBarChangeHandler());
//        	mRingPlayerBtn.setOnClickListener(new ItemPlayHandler());
//        	mRingSeekBar.setSecondaryProgress(mRingSeekBar.getMax());
        	mRingSeekBar.setProgress(mRingtoneManager.getProgress(mId));
        	mRingPlayerBtn.setBackgroundDrawable(mRingPlay);
	    	updateTimerView();
	    }
	    /**
	     * 设置为正在下载视图状态
	     */
	    private void setDownloadingView() {
	    	Log.v(TAG, "setDownloadingView");
	    	mRingSeekBar.setVisibility(View.VISIBLE);
	    	mRingSeekBar.setSecondaryProgress(mRingtoneManager.getDownloadPosition(mId));
        	mRingSeekBar.setVisibility(View.VISIBLE);
        	mRingSeekBar.setClickable(true);
        	mRingPlayerBtn.setOnClickListener(new ItemCacheHandler(this));
        	mRingSeekBar.setOnSeekBarChangeListener(null);
        	mRingtoneManager.addDownloadProgressChangeListener(mId, new ItemOnDownloadProgressChangedListener());
	    }
	    /**
	     * 设置为未下载或缓存视图状态
	     */
	    private void setDisblePlayView() {
	    	Log.v(TAG, "setDisblePlayView");
	    	mRingSeekBar.setSecondaryProgress(0);
        	mRingSeekBar.setVisibility(View.GONE);
        	mRingSeekBar.setClickable(false);
        	mRingPlayerBtn.setOnClickListener(new ItemCacheHandler(this));
        	mRingSeekBar.setOnSeekBarChangeListener(null);
	    }
	    
	    /**
	     * 设置为默认的视图状态
	     */
	    private void setDefaultView() {
	    	mRingSeekBar.setSecondaryProgress(0);
	    	mRingSeekBar.setProgress(0);
        	mRingSeekBar.setVisibility(View.GONE);
        	mRingSeekBar.setClickable(false);
        	mRingPlayerBtn.setOnClickListener(null);
        	mRingSeekBar.setOnSeekBarChangeListener(null);
        	mRingtoneManager.setOnStateChangedListener(null);
        	mRingtoneManager.removeDownloadProgressChangeListener(mId);
        	
        	setForwardAndCommentButtons();
	    }
	    
	    /**
	     * 设置成本地视图状态
	     */
	    private void setLocalView() {
	    	mRingDownloadBtn.setVisibility(View.GONE);
	    	findViewById(R.id.dot_line).setVisibility(View.GONE);
	    	findViewById(R.id.ring_preview_item_top).setVisibility(View.GONE);
	    }
	    
	    private void setForwardAndCommentButtons() {
	    	//forward image onclick
	    	mRingCommentBtn.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent = new Intent(mContext, WallPaperOrringReply.class);
						intent.putExtra(WallPaperOrringReply.EXTRA_ID, mId);
						intent.putExtra(WallPaperOrringReply.EXTRA_FROM_MODE, WallPaperOrringReply.MODE_RING);
						intent.putExtra(WallPaperOrringReply.EXTRA_TYPE, WallPaperOrringReply.ACTION_COMMENT);
						mContext.startActivity(intent);
					}
				}
			);
			//reply image onclick
	    	mRingShareBtn.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent = new Intent(mContext, WallPaperOrringReply.class);
						intent.putExtra(WallPaperOrringReply.EXTRA_ID, mId);
						intent.putExtra(WallPaperOrringReply.EXTRA_FROM_MODE, WallPaperOrringReply.MODE_RING);
						intent.putExtra(WallPaperOrringReply.EXTRA_TYPE, WallPaperOrringReply.ACTION_FORWARD);
						mContext.startActivity(intent);
					}
				}
			);
	    }

		public void updateSeekbarProgress(int progress) {
	    	mRingSeekBar.setSecondaryProgress(progress);
	    }

	    public RingtoneItemData getRingtoneItemMessage() {
	        return mRintoneItemData;
	    }
	    
	    public long getRingId() {
	    	return mId;
	    }
	    
	    /**
	     * 当item被点击时，在进入detail页面前我们需要停止音乐的播放
	     */
	    public void stopMediaPlay(){
	    	mRingtoneManager.stopPlayback();
	    }
	    
	    public void setListItemHandler(Handler handler) {
	    	mHandler = handler;
	    }
	    
//	    private void resetPlayerOnStateChangeListener() {
//	    	Log.v(TAG, "set OnStateChangeListener");
//	    	
//	    	mRingtoneManager.setOnStateChangedListener(new OnStateChangedListener() {
//
//				@Override
//				public void onError(int error) {
//					mRingPlayerBtn.setBackgroundDrawable(mRingPlay);
//				}
//
//				@Override
//				public void onStateChanged(int state) {
//					Log.v(TAG, "onStateChanged " + state);
//					Log.v(TAG, "onStateChanged id " + mId);
//					if (state == RingtoneManager.IDLE_STATE) {
//						mRingPlayerBtn.setBackgroundDrawable(mRingPlay);
//						mRingSeekBar.setProgress(0);
//					} else if (state == RingtoneManager.PLAYING_STATE){
//						mRingPlayerBtn.setBackgroundDrawable(mRingPause);
//						updateTimerView();
//					} else if (state == RingtoneManager.PAUSE_STATE){
//						mRingPlayerBtn.setBackgroundDrawable(mRingPlay);
//					}
//				}
//				
//			});
//	    }
	    
	    private void updateTimerView() {
//	    	if (!mRingtoneManager.isPlaying(mId) || mHandler == null) {
//	    		return ;
//	    	}
	        int state = mRingtoneManager.state();

	        boolean ongoing = state == RingtoneManager.PLAYING_STATE;

//	        long time = ongoing ? mRecorder.progress() : mRecorder.sampleLength(); 
//	        if (state == Recorder.PAUSE_STATE || state == Recorder.PLAYING_STATE) time = mRecorder.getPosition();
//	       
//	        String timeStr = String.format(mTimerFormat, time/60, time%60);
//	        mTimerView.setText(timeStr);
	        long time = 0;
	        if (state == RingtoneManager.PLAYING_STATE) {
	        	//得到当前播放进度
	        	time = mRingtoneManager.getPosition();
	        	mRingSeekBar.setProgress((int)(100*time/mRingtoneManager.getDuration()));
//	        	 Log.v("updateTimerView", "play ");
	        } 
//	        Log.v("updateTimerView", "time:total" +time+ ":"+ mRingtoneManager.getDuration());

//	        if (state == Recorder.PAUSE_STATE) {
//	        	int vis = mTimerView.getVisibility();
//	       	    mTimerView.setVisibility(vis == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
//	        } else {
//	        	 mTimerView.setVisibility(View.VISIBLE);
//	        }
	        if (ongoing)
	            mHandler.postDelayed(mUpdateTimer, 500);
	    }
	    
	    class ItemPlayHandler implements View.OnClickListener {
	    	private RingtoneMessageListItem view;
			
			public ItemPlayHandler(RingtoneMessageListItem itemView) {
				view = itemView;
			}
			@Override
			public void onClick(View v) {
				Log.v(TAG, "id "+ mId + " path" + mRintoneItemData.rLocalPath);
	            mRingtoneManager.startPlayback(view.mId, view.mRintoneItemData.rLocalPath);
	            view.updateTimerView();
			}
		}

		class ItemCommentHandler implements View.OnClickListener {

			@Override
			public void onClick(View paramView) {
				
			}
		}

		class ItemShareHandler implements View.OnClickListener {

			@Override
			public void onClick(View paramView) {
				
			}
		}

		class ItemCacheHandler implements View.OnClickListener {

			private RingtoneMessageListItem view;
			
			public ItemCacheHandler(RingtoneMessageListItem itemView) {
				view = itemView;
			}
			@Override
			public void onClick(View paramView) {
//				mRingtoneManager.setOnDownloadCompletedListener(new CallbackOnDownloadCompleted() {
//					@Override
//					public void onDownloadCompleted(long rawId, final String path,
//							int status) {
//						Log.v(TAG, "download completed id " + rawId);
//						if (status == CallbackOnDownloadCompleted.SUCCESSFULL && rawId == mId) {
////							mHandler.post(new Runnable() {
////								@Override
////								public void run() {
//////									mRingtoneManager.startPlayback(mId, path);
////									setEnablePlayView();
////									mRingPlayerBtn.performClick();
////								}
////							});
//						}
//					}
//				});

				mRingtoneManager.startCaching(view.mRintoneItemData);
//				setDownloadingView();
			}
		}
		
		class ItemSeekBarChangeHandler implements SeekBar.OnSeekBarChangeListener {
			private long mLastSeekEventTime = 0;
			private boolean mFromTouch = false;
			private RingtoneMessageListItem view;
			
			public ItemSeekBarChangeHandler(RingtoneMessageListItem itemView) {
				view = itemView;
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					  long now = SystemClock.elapsedRealtime();
			          if ((now - mLastSeekEventTime) > 250) {
			        	  Log.v(TAG, "onProgressChanged " + progress);
			        	  int progressToSecond = (int) (1.0 *progress/100 * mRingtoneManager.getDuration());
			        	  mRingtoneManager.seekToPlay(1000*progressToSecond);
			  			  if (!mFromTouch) view.updateTimerView();
			          }
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				mLastSeekEventTime = 0;
		        mFromTouch = true;
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mFromTouch = false;
			}
		}
		
		class ItemOnDownloadProgressChangedListener implements OnDownloadProgressChangedListener {
			@Override
			public void onProgressChanged(long rawId, final int progress) {
				if (rawId == mId) {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							updateSeekbarProgress(progress);
						}
					});
				}
			}
		}
		
		class ItemDownloadHandler implements View.OnClickListener {

			@Override
			public void onClick(View paramView) {
				if (mRintoneItemData.rStatu == HistoryDBHelper.STATU_CACHED) {//已经缓存有数据
					//先更新history表数据为已下载
					mRingtoneManager.preMoveToDownloadDir(mRintoneItemData);
					//之后启动一个线程复制文件，之后更新download表数据为已下载
					new Thread(new Runnable() {
						@Override
						public void run() {
							mRingtoneManager.moveCacheToDownload(mRintoneItemData);
						}
					}).start();
				} else if (mRintoneItemData.rStatu == HistoryDBHelper.STATU_NOMAL) {//开始下载
//					setDownloadingView();
//					mRingtoneManager.addDownloadProgressChangeListener(new ItemOnDownloadProgressChangedListener());
					mRingtoneManager.startDownload(mRintoneItemData);
				}
			}
		}
		
		class ItemStateChangedListener implements OnStateChangedListener {

			private RingtoneMessageListItem view;
			
			public ItemStateChangedListener(RingtoneMessageListItem itemView) {
				view = itemView;
			}
			@Override
			public void onError(int error) {
				view.mRingPlayerBtn.setBackgroundDrawable(mRingPlay);
			}

			@Override
			public void onStateChanged(int state) {
				Log.v(TAG, "onStateChanged " + state);
				Log.v(TAG, "onStateChanged id " + mId);
				if (state == RingtoneManager.IDLE_STATE) {
					view.mRingPlayerBtn.setBackgroundDrawable(mRingPlay);
					view.mRingSeekBar.setProgress(0);
				} else if (state == RingtoneManager.PLAYING_STATE){
					view.mRingPlayerBtn.setBackgroundDrawable(mRingPause);
					view.updateTimerView();
				} else if (state == RingtoneManager.PAUSE_STATE){
					view.mRingPlayerBtn.setBackgroundDrawable(mRingPlay);
				}
			}
			
		}
		
		Runnable mUpdateTimer = new Runnable() {
	        public void run() { 
	        	updateTimerView(); }
	    };
}
