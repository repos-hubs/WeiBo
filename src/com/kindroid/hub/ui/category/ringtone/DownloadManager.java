package com.kindroid.hub.ui.category.ringtone;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.kindroid.hub.R;
import com.kindroid.hub.provider.RingtoneProvider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 曲目下载管理器，和RingtoneManager搭配使用。支持{@link #MAX_DOWNLOAD_THREADS}个下载同时进行。
 *
 */
public class DownloadManager {
	private static final String TAG = "DownloadManager";
	/**Singleton */
	private static DownloadManager mDownloadManager;
	/**id->url*/
	private HashMap<Long, String> mDownloadMap;
	/**id->download progress*/
	private HashMap<Long, Integer> mProgressMap;
	
	private HashMap<Long, String> mPlaybackMap;
	private HashMap<Long, Integer> mPlaybackProgressMap;
	private ExecutorService mDownloadThreadPool;
	private RingPlaybackCachingTask mPlaybackThread;
	/**默认支持的批量下载个数*/
	private static final int MAX_DOWNLOAD_THREADS = 10;
	private Context mContext;
	private Handler mHandler;
	private File mDownloadDir;
    private File mTempDir;
    private Pattern mPattern = Pattern.compile("^([[a-z][0-9]_*]+)_([0-9]+)(\\.[[a-z][0-9]]+)$");
    private ContentResolver cr;
    private CallbackOnDownloadCompleted mCallbackOnDownloadCompleted;
    private OnDownloadProgressChangedListener mDownloadProgressChangedListener;

	public enum ViewType {
		LOCAL,  //本地视图
		PREVIEW,//在线视图
		DETAIL  //详细页视图
	}
	/**
	 * Give a chance to do something interesting when an id-specified download is completed.
	 * It's very useful in the case, that we need to play a ring file after downloaded.
	 * @author kai.chen
	 *
	 */
	public static interface CallbackOnDownloadCompleted {
		int SUCCESSFULL = 1;
		int CANCEL = 2;
		int FAILED = 3;
		/**
		 * 
		 * @param rawId   下载完的曲目id
		 * @param path    下载完得曲目本地路径
		 * @param status  下载状态， 1成功2取消3失败
		 */
		void onDownloadCompleted(long rawId, String path, int status);
	}
	
	public static interface OnDownloadProgressChangedListener {
		void onProgressChanged(long rawId, int progress);
	}


	private DownloadManager(Context context, Handler handler) {
		mDownloadMap = new HashMap<Long, String>();
		mProgressMap = new HashMap<Long, Integer>();
		mPlaybackMap = new HashMap<Long, String>();
		mPlaybackProgressMap = new HashMap<Long, Integer>();
		mDownloadThreadPool = Executors.newFixedThreadPool(MAX_DOWNLOAD_THREADS);
		mContext = context;
		mHandler = handler;
		cr = mContext.getContentResolver();
		String path = Environment.getExternalStorageDirectory().getPath() + "/" + 
		mContext.getPackageName() + "/ringtone";
	    mDownloadDir = new File(path, "download");
	    mTempDir = new File(path, "temp");
	    if (!mDownloadDir.exists()) {
		    mDownloadDir.mkdirs();
	    }
	    if (!mTempDir.exists()) {
		    mTempDir.mkdirs();
	    }
	}

	public static synchronized DownloadManager newInstance(Context context, Handler handler) {
		if (mDownloadManager == null) {
			mDownloadManager = new DownloadManager(context, handler);
		}
		return mDownloadManager;
	}

	public boolean isDataPrepared() {
		return false;
	}
	
	/**
	 * whether or not the id-specified file is downloading.
	 * @return
	 */
	public boolean isDataDownloading(long id, String url) {
		String downloadingUrl = mDownloadMap.get(id);
		return downloadingUrl != null && downloadingUrl.equals(url);
	}
	
	public File getDownloadDir() {
		return mDownloadDir;
	}
	
	public File getTempDir() {
		return mTempDir;
	}
	
	public void setCallbackOnDownloadCompleted(CallbackOnDownloadCompleted listener) {
		mCallbackOnDownloadCompleted = listener;
	}
	
	public void setOnDownloadProgressChangedListener(OnDownloadProgressChangedListener listener) {
		mDownloadProgressChangedListener = listener;
	}
	
	/**
	 * get current downloaded percent of specified file.
	 * @param id
	 * @return
	 */
	public int getCurrentDownloadProgress(long id) {
		Integer progress = mProgressMap.get(id);
		return (progress == null)? 0:progress;
	}
	
	private void setCurrentDownloadProgress(long id, int progress) {
		mProgressMap.put(id, progress);
	}

	/**
	 * 
	 * @param id
	 * @param url
	 * @param requestMode
	 * @param fullName the full name of a file. A full name contains three part, (name)_(id)(.mime), such as song walk_in_the_rain_02.mp3
	 */
	public synchronized boolean commitDownloadRequest(long id, String url, String fullName) {
		if (!isDataDownloading(id, url)) {
			mDownloadMap.put(id, url);
			mProgressMap.put(id, 0);
			mDownloadThreadPool.execute(new RingDownloadTask(id, url, fullName));
			showMessage(mContext.getString(R.string.msg_file_downloading));
			return true;
		} else {
			showMessage(mContext.getString(R.string.msg_file_has_downloading));
			return false;
		}
	}
	
	/**
	 * 
	 * @param id
	 * @param url
	 * @param requestMode
	 * @param fullName the full name of a file. A full name contains three part, (name)_(id)(.mime), such as song walk_in_the_rain_02.mp3
	 */
	public synchronized boolean commitPlaybackRequest(long id, String url, String fullName) {
		String downloadingUri = mPlaybackMap.get(id);
		if (downloadingUri != null && !downloadingUri.equals(url) || downloadingUri == null) {
			mPlaybackMap.put(id, url);
			mPlaybackProgressMap.put(id, 0);
			if (mPlaybackThread != null) mPlaybackThread.interrupt();
			mPlaybackThread = new RingPlaybackCachingTask(id, url, fullName);
			mPlaybackThread.start();
			showMessage(mContext.getString(R.string.ring_start_cache));
			return true;
		} else {
			showMessage(mContext.getString(R.string.msg_file_downloading));
			return false;
		}
	}
	
	public boolean isPlaybackCaching(long id) {
		return mPlaybackMap.get(id) != null;
	}
	
	/**
	 * 释放资源
	 */
	public void release() {
		if (mDownloadManager != null) {
			if (mDownloadThreadPool != null) shutdownAndAwaitTermination(mDownloadThreadPool);
			if (mPlaybackThread != null ) mPlaybackThread.cancel();
			mDownloadMap.clear();
			mProgressMap.clear();
			mPlaybackMap.clear();
			mPlaybackProgressMap.clear();
			mDownloadManager = null;
		}
	}
	
	public void shutdownAndAwaitTermination(ExecutorService pool) {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
		    // Wait a while for existing tasks to terminate
		    if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
		    	pool.shutdownNow(); // Cancel currently executing tasks
		         // Wait a while for tasks to respond to being cancelled
		         if (!pool.awaitTermination(60, TimeUnit.SECONDS))
		             System.err.println("Pool did not terminate");
		    }
		} catch (InterruptedException ie) {
		    // (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
		    // Preserve interrupt status
			mDownloadThreadPool = null;
		    Thread.currentThread().interrupt();
		}
    }


    private boolean checkSDcardState() {
	    return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public String requestDataPath(long id) {
    	return null;
    }
    
    private void showMessage(final String msg) {
    	mHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
			}
    		
    	});
    }

    class RingDownloadTask implements Runnable {

    	private long id;
    	private String url;
    	String fileName;
    	
    	public RingDownloadTask(long id, String url, String fullName) {
    		this.id = id;
    		this.url= url;
    		this.fileName = fullName;
    	}
    	
		@Override
		public void run() {
			HttpClient client = new DefaultHttpClient();
	        HttpGet getRequest = new HttpGet(url);
	        HttpResponse response = null;
	        Matcher mMatcher = mPattern.matcher(fileName);
	        String name = null;
	        Uri insertUri = null;
	        if (mMatcher.find()) {
	        	name = mMatcher.group(1) + mMatcher.group(3);
	        } else {
	        	name = fileName;
	        }
	        try {
				response = client.execute(getRequest);
				final int statusCode = response.getStatusLine().getStatusCode();
				Log.v("File downloader", "statusCode " + statusCode +
	                    " while retrieving file from " + url);
				switch(statusCode) {
				case 200:
					HttpEntity entity = response.getEntity();
			        if (entity != null) {
			             InputStream inputStream = null;
			             long length = 0;
			             try {
							 inputStream = entity.getContent();
							 length = entity.getContentLength();//file length
							 BufferedInputStream in = new BufferedInputStream(inputStream);
							 File outFile = null;
							 byte[] buffer = new byte[512];
							 outFile = new File(mDownloadDir,fileName);
							 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
							 int readCount = in.read(buffer);
							 int progress = 0;
							 while (readCount != -1) {
								 out.write(buffer, 0, readCount);
								 progress += readCount;
								 int percent = (int) (100.0 * progress/length);
//								 Log.v(TAG,"id/percent " + id + " complete " + percent);
								 if (mDownloadProgressChangedListener != null) {
									 mDownloadProgressChangedListener.onProgressChanged(id, percent);
								 }
								 mProgressMap.put(id, percent);
								 readCount = in.read(buffer);
							 }
							 out.flush();
							 out.close();
							 in.close();
							 //update database
							 ContentValues values = new ContentValues();
							 //for DOWNLOAD, we need to do some extra work.
							 //first update history table just like PREVIEW step.
							 values.put(HistoryDBHelper.LOCAL_URI, outFile.getAbsolutePath());
							 values.put(HistoryDBHelper.STATU, 3);
//							 mProgressMap.remove(id);
//							 mDownloadMap.remove(id);
							 RingtoneManager.clearRingDataCache(id);
//							 RingtoneItemData ringData = RingtoneManager.queryRingDataFromCache(id);
//							 if (ringData != null) {
//								Log.v(TAG, "update ringData in cache with id " + id);
//								ringData.rLocalPath = outFile.getAbsolutePath();
//							 }
							 cr.update(RingtoneProvider.PREVIEW_CONTENT_URL, values, HistoryDBHelper.RAW_ID + "=?", new String[]{String.valueOf(id)});
							 //then, insert data into download table
							 Cursor history = cr.query(RingtoneProvider.PREVIEW_CONTENT_URL, RingtoneManager.HISTORY_PROJECTION, null, null, null);
							 while (history.moveToNext()) {
								values.put(HistoryDBHelper.RAW_ID, history.getString(RingtoneManager.HISTORY_RAWID_INDEX));
								values.put(HistoryDBHelper.CREATOR, history.getString(RingtoneManager.HISTORY_CREATOR_INDEX));
								values.put(HistoryDBHelper.TIME_SHARE, history.getString(RingtoneManager.HISTORY_TIME_SHARE_INDEX));
								values.put(HistoryDBHelper.OWNER, history.getString(RingtoneManager.HISTORY_OWNER_INDEX));
								values.put(HistoryDBHelper.DATE, history.getString(RingtoneManager.HISTORY_DATE_INDEX));
								values.put(HistoryDBHelper.NAME, history.getString(RingtoneManager.HISTORY_NAME_INDEX));
								values.put(HistoryDBHelper.LENGTH, history.getString(RingtoneManager.HISTORY_LENGTH_INDEX));
								values.put(HistoryDBHelper.RAW_URI, history.getString(RingtoneManager.HISTORY_RAW_URI_INDEX));
								values.put(HistoryDBHelper.DOWNLOAD_COUNT, history.getString(RingtoneManager.HISTORY_DOWNLOAD_COUNT_INDEX));
								values.put(HistoryDBHelper.MIME_TYPE, history.getString(RingtoneManager.HISTORY_MIME_TYPE_INDEX));
							 }
							insertUri = cr.insert(RingtoneProvider.DOWNLOAD_CONTENT_URL, values);
						} catch (IllegalStateException e) {
							showMessage(e.getMessage());
							e.printStackTrace();
						} catch (IOException e) {// file I/O EXCEPTION
							showMessage(e.getMessage());
							e.printStackTrace();
						}
				    }
					break;
				case 404:    //file not exist
					showMessage(mContext.getString(R.string.msg_status_404, name));
					break;
				case 500:   //server error
					showMessage(mContext.getString(R.string.msg_status_500));
					break;
				case 400:
					showMessage(mContext.getString(R.string.msg_status_400, name));
					break;
				default:   //others
					break;
				}
				Log.v(TAG, "RingDownloadTask finish");
			} catch (ClientProtocolException e) {
				showMessage(e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				showMessage(mContext.getString(R.string.report_no_network_error));
				e.printStackTrace();
			} finally {
				Log.v(TAG, "download finally");
				mProgressMap.remove(id);
				mDownloadMap.remove(id);
				if (insertUri != null) {
					showMessage(mContext.getString(R.string.msg_status_200, name));
					Log.v(TAG, "insert uri " + insertUri);
				} else {
					Log.v(TAG, "rollback statu nomal id " + id);
					RingtoneManager.clearRingDataCache(id);
					ContentValues values = new ContentValues();
					values.put(HistoryDBHelper.STATU, HistoryDBHelper.STATU_NOMAL);
					cr.update(RingtoneProvider.PREVIEW_CONTENT_URL, values, HistoryDBHelper.RAW_ID + "=?", new String[]{String.valueOf(id)});
				}
			}
		}
    }
    
    class RingPlaybackCachingTask extends Thread {

    	private long id;
    	private String url;
    	private String fileName;
    	private boolean cancel;
    	
    	public RingPlaybackCachingTask(long id, String url, String fullName) {
    		this.id = id;
    		this.url= url;
    		this.fileName = fullName;
    	}
    	
    	public void cancel() {
    		cancel = true;
    	}
    	
		@Override
		public void run() {
			HttpClient client = new DefaultHttpClient();
	        HttpGet getRequest = new HttpGet(url);
	        HttpResponse response = null;
	        Matcher mMatcher = mPattern.matcher(fileName);
	        String name = null;
	        File outFile = null;
	        int count = 0;
	        if (mMatcher.find()) {
	        	name = mMatcher.group(1) + mMatcher.group(3);
	        } else {
	        	name = fileName;
	        }
	        try {
				response = client.execute(getRequest);
				final int statusCode = response.getStatusLine().getStatusCode();
				Log.v("File downloader", "statusCode " + statusCode +
	                    " while retrieving file from " + url);
				switch(statusCode) {
				case 200:
					HttpEntity entity = response.getEntity();
			        if (entity != null) {
			             InputStream inputStream = null;
			             long length = 0;
			             try {
			            	 if (cancel) throw new InterruptedException("cache canceled by user");
							 inputStream = entity.getContent();
							 length = entity.getContentLength();//file length
							 BufferedInputStream in = new BufferedInputStream(inputStream);
							 byte[] buffer = new byte[512];
							 outFile = new File(mTempDir,fileName);
							 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
							 int readCount = in.read(buffer);
							 int progress = 0;
							 while (readCount != -1) {
								 if (cancel) throw new InterruptedException("cache canceled by user");
								 out.write(buffer, 0, readCount);
								 progress += readCount;
								 int percent = (int) (100.0 * progress/length);
								 if (mDownloadProgressChangedListener != null) {
									 mDownloadProgressChangedListener.onProgressChanged(id, percent);
								 }
//								 Log.v(TAG,"id/percent " + id + " complete " + percent);
								 mProgressMap.put(id, percent);
								 readCount = in.read(buffer);
							 }
							 out.flush();
							 out.close();
							 in.close();
							 //update database
							 ContentValues values = new ContentValues();
						     values.put(HistoryDBHelper.LOCAL_URI, outFile.getAbsolutePath());
						     values.put(HistoryDBHelper.STATU, HistoryDBHelper.STATU_CACHED);
						     if (cancel) throw new InterruptedException("cache canceled by user");
//						     Log.v(TAG,"mPlaybackMap remove id " + id);
//						     mPlaybackMap.remove(id);
//						    
						     RingtoneManager.clearRingDataCache(id);
//						     RingtoneItemData ringData = RingtoneManager.queryRingDataFromCache(id);
//							 if (ringData != null) {
//								 Log.v(TAG, "update ringData in cache with id " + id);
//								 ringData.rLocalPath = outFile.getAbsolutePath();
//							 }
							 count = cr.update(RingtoneProvider.PREVIEW_CONTENT_URL, values, HistoryDBHelper.RAW_ID + "=?", new String[]{String.valueOf(id)});
							 Log.v(TAG, "update count " + count);
							 if (mCallbackOnDownloadCompleted != null) {
									//here we playback ring file.
									mCallbackOnDownloadCompleted.onDownloadCompleted(id, outFile.getAbsolutePath(), CallbackOnDownloadCompleted.SUCCESSFULL);
									mPlaybackThread = null;
							 }
							 
						} catch (IllegalStateException e) {
							showMessage(e.getMessage());
							e.printStackTrace();
						} catch (IOException e) {// file I/O EXCEPTION
							showMessage(e.getMessage());
							e.printStackTrace();
						} catch (InterruptedException e) {//cancel by user
							if (outFile.exists())outFile.delete();
							e.printStackTrace();
							mPlaybackThread = null;
						}
				    }
					break;
				case 404:    //file not exist
					showMessage(mContext.getString(R.string.msg_status_404, name));
					break;
				case 500:   //server error
					showMessage(mContext.getString(R.string.msg_status_500));
					break;
				case 400:
					showMessage(mContext.getString(R.string.msg_status_400, name));
					break;
				default:   //others
					showMessage(mContext.getString(R.string.msg_status_other, name));
					break;
				}
			} catch (ClientProtocolException e) {
				showMessage(e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				showMessage(mContext.getString(R.string.report_no_network_error));
				e.printStackTrace();
			} finally {
				Log.v(TAG,"mPlaybackCacheThread finnaly try_catch");
				Log.v(TAG,"mPlaybackMap remove id " + id);
				mPlaybackMap.remove(id);
				//重新修改系在状态为未下载
				 if (count > 0) {
					 showMessage(mContext.getString(R.string.msg_status_200, name));
				 } else {
					 RingtoneManager.clearRingDataCache(id);
					 ContentValues values = new ContentValues();
					 values.put(HistoryDBHelper.STATU, HistoryDBHelper.STATU_NOMAL);
					 cr.update(RingtoneProvider.PREVIEW_CONTENT_URL, values, HistoryDBHelper.RAW_ID + "=?", new String[]{String.valueOf(id)});
				 }
			}
		} 
    }
}