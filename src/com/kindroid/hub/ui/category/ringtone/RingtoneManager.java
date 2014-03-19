package com.kindroid.hub.ui.category.ringtone;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.StateListDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.kindroid.hub.R;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.RingOrWallPaper;
import com.kindroid.hub.proto.WeiboContentProtoc.Review;
import com.kindroid.hub.provider.RingtoneProvider;
import com.kindroid.hub.ui.category.ringtone.DownloadManager.CallbackOnDownloadCompleted;
import com.kindroid.hub.ui.category.ringtone.DownloadManager.OnDownloadProgressChangedListener;
import com.kindroid.hub.ui.category.ringtone.DownloadManager.ViewType;

/**
 * 曲目的播放和一些通用状态的管理，和{@link DownloadManager}协同工作。也可以在该对象上设置一系列的监听器来做额外的动作。
 *
 */
public class RingtoneManager implements OnCompletionListener, 
        OnErrorListener,
        OnPreparedListener, CallbackOnDownloadCompleted,
        OnDownloadProgressChangedListener {

	private static final String TAG = "RingtoneManager";
	private Context mContext;
	private static RingtoneManager mRingtoneManager;
    private MediaPlayer mPlayer = null;
    private File mDownloadDir;
    private File mTempDir;
    public static final int IDLE_STATE = 0;
    public static final int DOWNLOAD_STATE = 1;
    public static final int PLAYING_STATE = 2;
    public static final int PAUSE_STATE = 3;

    int mState = IDLE_STATE;

    public static final int NO_ERROR = 0;
    public static final int SDCARD_ACCESS_ERROR = 1;
    public static final int INTERNAL_ERROR = 2;
    public static final int IN_CALL_RECORD_ERROR = 3;

	private long mPlayedPreviewRawId;
	/**
	 * 这个值表示的是正在缓存下载的曲目ID，当下载完成时，该曲目会自动播放。
	 */
	private long mCachingId;
	private int mCurrentProgress;
	private int mCurrentSecondaryProgress;
	private ImageView mCurrentRingPlayer;
	private StateListDrawable mRingPause, mRingPlay;
	
	private long mLastSeekEventTime;
	private boolean mFromTouch = false;

	public enum Mode{
		PREVIEW,
		LOCAL,
		DETAIL
	}

    public interface OnStateChangedListener {
        public void onStateChanged(int state);
        public void onError(int error);
    }

    /**
     * 播放状态监听器，如果对某一首铃音的播放状态感兴趣，需要设置该监听器。
     * 比如PLAYING_STATE(播放状态),IDLE_STATE(空闲状态)以及一些错误状态
     */
    private OnStateChangedListener mOnStateChangedListener = null;
    private CallbackOnDownloadCompleted mCallbackOnDownloadCompleted = null;
    /**
     * 曲目下载或缓冲进度监听器。RingtoneManager对象本身实现了该接口，并注册到了DownloadManager对象上，
     * DownloadManager对象在下载过程中，会不时通知RingtoneManager, 此时，列表中对应的监听器得到调用。
     * 根据回调中的参数设置进度条进度。
     */
    private HashMap<Long, OnDownloadProgressChangedListener> mDownloadProgressChangeListener = new HashMap<Long, OnDownloadProgressChangedListener>();
    
    private DownloadManager mDownloadManager;
    private Handler mHandler;
    private static final int MSG_START_PLAYING = 10000;
    
    private static final int MAX_CACHE_MESSAGE = 10;
    private static final int MAX_DOWNLOAD_COUNT = 5;
    private static int CURRENT_DOWNLOAD_COUNT = 0;
    private ContentResolver mCr;
    
    public static final String[] HISTORY_PROJECTION = {
		HistoryDBHelper._ID,                //0
		HistoryDBHelper.RAW_ID,             //1
		HistoryDBHelper.CREATOR,            //2
		HistoryDBHelper.TIME_SHARE,         //3
		HistoryDBHelper.OWNER,              //4
		HistoryDBHelper.NAME,               //5
		HistoryDBHelper.LENGTH,             //6
		HistoryDBHelper.RAW_URI,            //7
		HistoryDBHelper.DOWNLOAD_COUNT,     //8
		HistoryDBHelper.DATE,               //9
		HistoryDBHelper.MIME_TYPE,          //10
		HistoryDBHelper.LOCAL_URI,          //11
		HistoryDBHelper.STATU,              //12
		HistoryDBHelper.PHOTO               //13
    };
    
    public static final String[] DOWLOAD_PROJECTION = {
		HistoryDBHelper._ID,                //0
		HistoryDBHelper.RAW_ID,             //1
		HistoryDBHelper.CREATOR,            //2
		HistoryDBHelper.TIME_SHARE,         //3
		HistoryDBHelper.OWNER,              //4
		HistoryDBHelper.NAME,               //5
		HistoryDBHelper.LENGTH,             //6
		HistoryDBHelper.RAW_URI,            //7
		HistoryDBHelper.DOWNLOAD_COUNT,     //8
//		HistoryDBHelper.PHOTO,              //9
		HistoryDBHelper.DATE,               //9
		HistoryDBHelper.MIME_TYPE,          //10
		HistoryDBHelper.LOCAL_URI,          //11
		HistoryDBHelper.STATU               //12
    };
    //历史和下载共用查询结果集列索引
    public static final int HISTORY_ID_INDEX = 0;
	public static final int HISTORY_RAWID_INDEX = 1;
	public static final int HISTORY_CREATOR_INDEX = 2;
	public static final int HISTORY_TIME_SHARE_INDEX = 3;
	public static final int HISTORY_OWNER_INDEX = 4;
	public static final int HISTORY_NAME_INDEX = 5;
	public static final int HISTORY_LENGTH_INDEX = 6;
	public static final int HISTORY_RAW_URI_INDEX = 7;
	public static final int HISTORY_DOWNLOAD_COUNT_INDEX = 8;
	public static final int HISTORY_PHOTO_INDEX = 13;
	public static final int HISTORY_DATE_INDEX = 9;
	public static final int HISTORY_MIME_TYPE_INDEX = 10;
	public static final int HISTORY_LOCAL_URI_INDEX = 11;
	public static final int HISTORY_STATU_INDEX = 12;

    private static final String HISTORY_TYPE_SELECTION = HistoryDBHelper.TYPE + "=?";
    private static final String RAWID_SELECTION = HistoryDBHelper.RAW_ID + "=?";
    private static final String[] PROJECTION_HISTORY_RAW_ID = {HistoryDBHelper.RAW_ID};

    private RingtoneManager(Context context, Handler handler) {
    	mContext = context;
    	mHandler = handler;
    	mDownloadManager = DownloadManager.newInstance(context, mHandler);
    	mDownloadManager.setCallbackOnDownloadCompleted(this);
    	mDownloadManager.setOnDownloadProgressChangedListener(this);
    	mDownloadDir = mDownloadManager.getDownloadDir();
    	mTempDir = mDownloadManager.getTempDir();
    	mCr = mContext.getContentResolver();
    }
    
    public static synchronized RingtoneManager newInstance(Context context, Handler handler) {
    	if (mRingtoneManager == null) {
    		mRingtoneManager = new RingtoneManager(context, handler);
    	}
    	return mRingtoneManager;
    }
    
    public boolean isDownloading(long id, String url) {
    	if (TextUtils.isEmpty(url) || id <= 0) return false;
    	return mDownloadManager.isDataDownloading(id, url);
    }
    /**
     * 曲目是否存在于缓存目录中
     * @param data
     * @return
     */
    public File isFileExistInCacheDir(String path) {
    	if (TextUtils.isEmpty(path)) return null;
    	File tem = new File(mTempDir, path);
    	if (tem.exists()) return tem;
    	
    	return null;
    	
    }
    
    /**
     * 曲目是否存在于下载目录中
     * @param data
     * @return
     */
    public File isFileExistInDownloadDir(String path) {
    	if (TextUtils.isEmpty(path)) return null;
    	File tem = new File(mDownloadDir, path);
    	if (tem.exists()) return tem;
    	return null;
    }

    public int getProgress(long rawId) {
    	if (rawId == mPlayedPreviewRawId) return mCurrentProgress;
    	else return 0;
    }

    public int getSecondaryProgress(long rawId) {
    	if (rawId == mPlayedPreviewRawId) return mCurrentSecondaryProgress/1000;
    	else return 0;
    }
//    /**
//     * 是否是播放中的id
//     * @param id
//     * @return
//     */
//    public boolean isPlayingRingId(long id) {
//    	return mPlayedPreviewRawId == id;
//    }
    /**
     * 设置播放状态改变监听器
     * @param listener
     */
    public void setOnStateChangedListener(OnStateChangedListener listener) {
        mOnStateChangedListener = listener;
    }

    public int state() {
        return mState;
    }

    /**
     * 添加下载完成回调监听器
     * @param listener
     */
    public void setOnDownloadCompletedListener(CallbackOnDownloadCompleted listener) {
    	mCallbackOnDownloadCompleted = listener;
    }
    
    public void addDownloadProgressChangeListener(long id, OnDownloadProgressChangedListener listener) {
    	mDownloadProgressChangeListener.put(id, listener);
    }
    
    public void removeDownloadProgressChangeListener(long... ids) {
    	if (ids.length == 0) {
    		mDownloadProgressChangeListener.clear();
    	} else {
    		mDownloadProgressChangeListener.remove(ids[0]);
    	}
    }
    
    private void doPlayBack(String path) {
        mPlayer = new MediaPlayer();
        showMessage("wait please.");
//        setState(PLAYING_STATE);
        mCurrentSecondaryProgress = 0;
        try {
            mPlayer.setDataSource(path);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
            mPlayer.setOnPreparedListener(this);
            mPlayer.prepareAsync();
        } catch (IllegalArgumentException e) {
            setError(INTERNAL_ERROR);
            setState(IDLE_STATE);
            mPlayer = null;
            return;
        } catch (IOException e) {
            setError(SDCARD_ACCESS_ERROR);
            setState(IDLE_STATE);
            mPlayer = null;
            return;
        }
    }
    /**
     * 构建一个带有ID的曲目名称，有效格式为     铃声名_id.扩展名，需要注意的是ringName中的空格需要用下划线"_"代替，比如"wlak_in_the_rain"
     * @param ringName
     * @param ringId
     * @param mime
     * @return
     */
    public String buildFullNameWithID(String ringName, long ringId, String mime) {
    	return ringName + "_" + ringId + "." + mime;
    }
    /**
     * 开始下载试听itemData指定的曲目
     * @param itemData 曲目数据结构
     */
    public void startCaching(RingtoneItemData itemData) {
		Log.v(TAG, "startCaching");
		if (CURRENT_DOWNLOAD_COUNT > MAX_DOWNLOAD_COUNT) {
			Toast.makeText(mContext, R.string.ring_max_download_reach, Toast.LENGTH_SHORT).show();
		}
		String fullName = buildFullNameWithID(itemData.rName, itemData.rRawId, itemData.rMimeType);
        boolean enable = mDownloadManager.commitPlaybackRequest(itemData.rRawId,itemData.rRawUrl, fullName);
        if (enable) {
        	CURRENT_DOWNLOAD_COUNT ++; 
        	moveToCachingDir(itemData);
        	mCachingId = itemData.rRawId;
        }
        setState(DOWNLOAD_STATE);
	}
    /**
     * 提交下载
     * @param itemData
     */
    public void startDownload(RingtoneItemData itemData){
        if (CURRENT_DOWNLOAD_COUNT > MAX_DOWNLOAD_COUNT) {
        	Toast.makeText(mContext, mContext.getString(R.string.ring_max_download_reach, String.valueOf(MAX_DOWNLOAD_COUNT)), Toast.LENGTH_SHORT).show();
		}
    	String fullName = buildFullNameWithID(itemData.rName, itemData.rRawId, itemData.rMimeType);
    	boolean enable = mDownloadManager.commitDownloadRequest(itemData.rRawId, itemData.rRawUrl, fullName);
    	if (enable) {
    		CURRENT_DOWNLOAD_COUNT ++; 
    		moveToCachingDir(itemData);
    	}
    	setState(DOWNLOAD_STATE);
    }

	public void startPlayback(long id, String path) {
		if (mPlayedPreviewRawId != id) {
			stopPlayback();
			mPlayedPreviewRawId = id;
			Log.v(TAG, "doPlayBack " + path);
			doPlayBack(path);
		} else {
			Log.v(TAG, "pauseOrResumePlayback " + path);
			pauseOrResumePlayback();
		}
    }

    public void stopPlayback() {
        if (mPlayer == null) // we were not in playback
            return;

        Log.v(TAG, "stop");
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        setState(IDLE_STATE);
        mPlayedPreviewRawId = 0;
        mCachingId = 0;
    }

    public void seekToPlay(int progress) {
    	if (mPlayer == null) // we were not in playback
            return;
    	Log.v("Recorder", "seekToPlay " + progress);
//    	mSampleStart = System.currentTimeMillis() - progress;
    	mPlayer.seekTo(progress);
    }

    /**
     * pause/resume music
     */
    public void pauseOrResumePlayback() {
    	if (mPlayer == null)
    		return;
    	if (mPlayer.isPlaying()){
    		mPlayer.pause();
    		Log.v(TAG, "pause");
    		setState(PAUSE_STATE);
    	} else if(mState == PAUSE_STATE){
    		mPlayer.start();
    		Log.v(TAG, "resume");
    		setState(PLAYING_STATE);
    	}
    }
    /**
     * 得到下载进度
     * @param id
     * @return
     */
    public int getDownloadPosition(long id) {
    	if (mDownloadManager == null)
    		return 0;
    	return mDownloadManager.getCurrentDownloadProgress(id);
    }
    /**
     * 当前播放进度
     * @return
     */
    public long getPosition() {
    	if (mPlayer == null)
    		return 0;
    	return mPlayer.getCurrentPosition()/1000;
    }
    
    /**
     * 曲目时长
     * @return
     */
    public long getDuration() {
    	return mCurrentSecondaryProgress==0?10000:mCurrentSecondaryProgress/1000;
    }

    /**
     * 曲目是否在播放
     * @param rawId
     * @return
     */
    public boolean isPlaying(long rawId) {
    	if (mPlayedPreviewRawId == rawId && mPlayer !=null && mPlayer.isPlaying())return true;
    	else return false;
    }
    
    /**
     * 是否是暂停状态
     * @param rawId
     * @return
     */
    public boolean isPausing(long rawId) {
    	if (mPlayedPreviewRawId == rawId && mPlayer !=null && mPlayer.isPlaying() && mState == PAUSE_STATE)return true;
    	else return false;
    }
    
    public boolean isAutoPlaying(long id) {
    	if (mCachingId == id) return true;
    	else return false;
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
    	stopPlayback();
        setError(SDCARD_ACCESS_ERROR);
        return true;
    }

    public void onCompletion(MediaPlayer mp) {
    	Log.v(TAG, "onCompletion");
    	stopPlayback();
    	mCachingId = 0;
    	mPlayedPreviewRawId = 0;
    	setState(IDLE_STATE);
    }

    private void setState(int state) {
        if (state == mState)
            return;
        Log.v(TAG,"setState " + state);
        mState = state;
        signalStateChanged(mState);
    }

    private void signalStateChanged(int state) {
        if (mOnStateChangedListener != null)
            mOnStateChangedListener.onStateChanged(state);
    }

    private void setError(int error) {
        if (mOnStateChangedListener != null)
            mOnStateChangedListener.onError(error);
        mPlayedPreviewRawId = 0;
        mCachingId = 0;
        doError(error);
    }


	/**
	 *  NO_ERROR = 0;
        SDCARD_ACCESS_ERROR = 1;
        INTERNAL_ERROR = 2;
        IN_CALL_RECORD_ERROR = 3;
	 * @param error
	 */
	private void doError(int error) {
		String text = "";
		switch(error){
		case SDCARD_ACCESS_ERROR:
			text = "SDCARD_ACCESS_ERROR";
			Log.v(TAG, "IO_ACCESS_ERROR");
			break;
		case INTERNAL_ERROR:
			text = "INTERNAL_ERROR";
			Log.v(TAG, "INTERNAL_ERROR");
			break;
		case IN_CALL_RECORD_ERROR:
			text = "IN_CALL_RECORD_ERROR";
			Log.v(TAG, "IN_CALL_RECORD_ERROR");
			break;
		case NO_ERROR:
			return;
		}
		showMessage(text);
	}
	
	private void showMessage(String text) {
		Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {
		Log.v(TAG, "onPrepared");
		mCurrentSecondaryProgress = mediaPlayer.getDuration();
		mediaPlayer.start();
		setState(PLAYING_STATE);
		mCachingId = 0;
	}

	@Override
	public void onDownloadCompleted(long rawId, String path, int status) {
		Log.v(TAG, "onDownloadCompleted " + rawId + " [" + path + " ]" + " status " + status);
		mPlayedPreviewRawId = rawId;
	    if (mCallbackOnDownloadCompleted != null) {
	    	mCallbackOnDownloadCompleted.onDownloadCompleted(rawId, path, status);
	    }
	}
	
	/**
	 * 曲目的下载进度改变监听器，这里是一个默认实现，如果有类对其感兴趣，
	 * 该类需要在RingtoneManager对象上调用addDownloadProgressChangeListener来添加监听器
	 */
	@Override
	public void onProgressChanged(long rawId, int progress) {
		if (mDownloadProgressChangeListener != null) {
			OnDownloadProgressChangedListener listener = mDownloadProgressChangeListener.get(rawId);
			if (listener != null) {
				listener.onProgressChanged(rawId, progress);
				if (progress == 100) {
					mDownloadProgressChangeListener.remove(rawId);
				}
			}
			
		}
	}
	
	/**
	 * 更新数据库
	 * @param uri
	 * @param values
	 * @param where
	 * @param selectionArgs
	 * @return
	 */
	public int updateDatabase(Uri uri, ContentValues values, String where, String[] selectionArgs) {
		return mCr.update(uri, values, where, selectionArgs);
	}
	
	/**
	 * 查询历史数据
	 * @param selection 条件
	 * @param selectionArgs 条件参数
	 * @return
	 */
	public Cursor queryHistory(String selection, String[] selectionArgs) {
		return mCr.query(RingtoneProvider.PREVIEW_CONTENT_URL, HISTORY_PROJECTION, selection, selectionArgs, null);
	}
	
	/**
	 * 查询下载数据
	 * @param selection 条件
	 * @param selectionArgs 条件参数
	 * @return
	 */
	public Cursor queryDownload(String selection, String[] selectionArgs) {
		return mCr.query(RingtoneProvider.DOWNLOAD_CONTENT_URL, DOWLOAD_PROJECTION, selection, selectionArgs, null);
	}
	
	/**
	 * 把音乐曲目的数据标记为已下载
	 * @param data
	 */
	public void moveToDownloadDir(RingtoneItemData data) {
		ContentValues values = new ContentValues();
		values.put(HistoryDBHelper.RAW_ID, data.rRawId);
		values.put(HistoryDBHelper.LOCAL_URI, data.rLocalPath);
		values.put(HistoryDBHelper.STATU, HistoryDBHelper.STATU_DOWNLOADED);
		updateDatabase(RingtoneProvider.DOWNLOAD_CONTENT_URL, values, RAWID_SELECTION, new String[]{String.valueOf(data.rRawId)});
	}
	
	public void preMoveToDownloadDir(RingtoneItemData data) {
		ContentValues values = new ContentValues();
		values.put(HistoryDBHelper.RAW_ID, data.rRawId);
		values.put(HistoryDBHelper.LOCAL_URI, data.rLocalPath);
		values.put(HistoryDBHelper.STATU, HistoryDBHelper.STATU_DOWNLOADED);
		clearRingDataCache(data.rRawId);
		updateDatabase(RingtoneProvider.PREVIEW_CONTENT_URL, values, RAWID_SELECTION, new String[]{String.valueOf(data.rRawId)});
	}
	
//	/**
//	 * 把音乐曲目的数据标记为正在下载
//	 * @param data
//	 */
//	public void moveToDownloadingDir(RingtoneItemData data) {
//		ContentValues values = new ContentValues();
//		values.put(HistoryDBHelper.RAW_ID, data.rRawId);
//		values.put(HistoryDBHelper.STATU, HistoryDBHelper.STATU_DOWNLOADING);
//		clearRingDataCache(data.rRawId);
//		updateDatabase(RingtoneProvider.DOWNLOAD_CONTENT_URL, values, RAWID_SELECTION, new String[]{String.valueOf(data.rRawId)});
//	}
	
	/**
	 * 把音乐曲目的数据标记为正在缓存
	 * @param data
	 */
	public void moveToCachingDir(RingtoneItemData data) {
		Log.v(TAG, "moveToCachingDir");
		ContentValues values = new ContentValues();
		values.put(HistoryDBHelper.RAW_ID, data.rRawId);
		values.put(HistoryDBHelper.STATU, HistoryDBHelper.STATU_DOWNLOADING);
		clearRingDataCache(data.rRawId);
		updateDatabase(RingtoneProvider.PREVIEW_CONTENT_URL, values, RAWID_SELECTION, new String[]{String.valueOf(data.rRawId)});
	}
	/**
	 * 移动缓存到下载目录
	 * @return
	 */
	public boolean moveCacheToDownload(RingtoneItemData data) {
		try {
			String fileName = buildFullNameWithID(data.rName, data.rRawId, data.rMimeType);
			Log.v(TAG, "start to move file to download directionary");
			
			File cache = new File(mTempDir,fileName);
			File download = new File(mDownloadDir,fileName);
			byte[] buffer = new byte[1024];
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(download));
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(cache));
			int count = bis.read(buffer);
			while(count != -1) {
				bos.write(buffer, 0, count);
				count = bis.read(buffer);
			}
			bos.flush();
			bos.close();
			bis.close();
			//修改路径
			data.rLocalPath = download.getAbsolutePath();
			moveToDownloadDir(data);
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 根据RingOrWallPaper对象填充ContentValues
	 * @param values
	 * @param ring
	 */
	 public void populateContentValues(ContentValues values, RingOrWallPaper ring, int messageType) {
		if (ring.hasId()) {
			values.put(HistoryDBHelper.RAW_ID, ring.getId());
		}
		if (ring.getAccount().hasNickName()) {
			values.put(HistoryDBHelper.CREATOR, ring.getAccount().getNickName());
		}
		if (ring.hasTimeLeft()) {
			values.put(HistoryDBHelper.TIME_SHARE, ring.getTimeLeft());
		}
		if (ring.hasOwner()) {
			values.put(HistoryDBHelper.OWNER, ring.getOwner());
		}
		if (ring.hasName()) {
			values.put(HistoryDBHelper.NAME, ring.getName());
		}
		if (ring.hasLength()) {
			values.put(HistoryDBHelper.LENGTH, ring.getLength());
		}
		if (ring.hasDownloadUrl()) {
			String url = ring.getDownloadUrl();
			values.put(HistoryDBHelper.RAW_URI, url);
			int index = url.lastIndexOf("/");
			String mime = null;
			if (index != -1)  {
				mime = url.substring(index+1);
				values.put(HistoryDBHelper.MIME_TYPE, mime);
			}
		}
		values.put(HistoryDBHelper.DATE, SystemClock.elapsedRealtime());
		if (messageType != -1) values.put(HistoryDBHelper.TYPE, messageType);
	 }
    
    private static long getKey(long id, ViewType viewType) {
        if (viewType == ViewType.LOCAL) {
            return -id;
        } else {
            return id;
        }
    }
    
    private static HashMap<Long,RingtoneItemData> mCachedRingData = new LinkedHashMap<Long,RingtoneItemData>(10, 1.0f, true) {

		@Override
		protected boolean removeEldestEntry(Entry<Long, RingtoneItemData> eldest) {
			return size() > 50;
		}
    	
    };
    
    public RingtoneItemData getRingItemDataFromCache(ViewType viewType, long rawId, Cursor c) {
    	Log.v(TAG, "getRingDataFromCache " + rawId);
    	RingtoneItemData ring = mCachedRingData.get(rawId);
    	if (ring == null && c != null && isCursorValid(c)) {
    		ring = new RingtoneItemData(mContext, c, this);
    		mCachedRingData.put(rawId, ring);
		}
    	return ring;
    }

    private boolean isCursorValid(Cursor cursor) {
        // Check whether the cursor is valid or not.
        if (cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
            return false;
        }
        return true;
    }
    
    public static void updateRingDataCache(RingtoneItemData ring) {
    	mCachedRingData.put(ring.rRawId, ring);
    }
    
    public static void addIntoRingDataCache(RingtoneItemData ring) {
    	if (!mCachedRingData.containsKey(ring.rRawId)) {
    		mCachedRingData.put(ring.rRawId, ring);
    	}
    }
    
    public static RingtoneItemData queryRingDataFromCache(long rawId) {
    	return mCachedRingData.get(rawId);
    }

    /**
     * 清理缓存的曲目RingtoneItemData,如果未指定id,则清空整个缓存
     * @param id
     */
    public static void clearRingDataCache(long... id) {
    	if (id.length == 0) mCachedRingData.clear();
    	else mCachedRingData.remove(id[0]);
    }
    
	public void deleteMessagesForMessageType() {
		Cursor cursor = null;
		String selection = null;
		String[] projection = new String[] {HistoryDBHelper.TIME_SHARE};
		String orderBy = HistoryDBHelper.TIME_SHARE + " DESC";
		String where = null;
		for (int i=0; i<4; i++) {
			selection = HistoryDBHelper.TYPE + "=" + i;
			where = selection + " and " + HistoryDBHelper.TIME_SHARE + "<?";
			cursor = mCr.query(RingtoneProvider.PREVIEW_CONTENT_URL, 
					projection, selection, null, orderBy );
			if (cursor == null ) {
				Log.e(TAG, "Ringtone: deleteMessagesForMessageType got back null cursor");
				continue ;
			}
			int count = cursor.getCount();
            int numberToDelete = count - MAX_CACHE_MESSAGE;
            if (numberToDelete <= 0) continue;
            // Move to the keep limit and then delete everything older than that one.
            cursor.move(MAX_CACHE_MESSAGE);
            long latestDate = cursor.getLong(0);
            count = mCr.delete(RingtoneProvider.PREVIEW_CONTENT_URL, where, new String[]{String.valueOf(latestDate)});
            Log.v(TAG, "messageType " + i + " delete " + count + " messages");
            cursor.close();
//            ContentValues values = new ContentValues();
//            //clear local uri of history table.
//            values.put(HistoryDBHelper.LOCAL_URI, "");
//            count = mCr.update(RingtoneProvider.PREVIEW_CONTENT_URL, values, null, null);
//            Log.v(TAG, "messageType " + i + " clear " + count + " paths");
            //clear cache dir.
		}
	}
	
	/**
	 * 释放所有资源
	 */
	public void release() {
		if (mRingtoneManager != null) {
			 mOnStateChangedListener = null;
			 mCallbackOnDownloadCompleted = null;
			 if (mDownloadProgressChangeListener != null) {
				 mDownloadProgressChangeListener.clear();
				 mDownloadProgressChangeListener = null;
			 }
			 mDownloadManager.release();
			 setOnStateChangedListener(null);
	         setOnDownloadCompletedListener(null);
	         mRingtoneManager = null;
		}
	}
}
