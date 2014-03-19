package com.kindroid.hub.ui.category.ringtone;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class RingtoneItemData {
	public long rRawId;
	public String rCreator;
	public String rTimeShare;
	public String rOwner;
	public String rName;
	public long rLength;
	public long rSize=600;
	public String rRawUrl;
	public int rDwonloadCount;
	public long rDate;
	public String rMimeType;
	/**
	 * 当本地文件存在的时候，若本字段为空，rLocalPath为存在的文件的路径，但不对数据库做修改。
	 */
	public String rLocalPath;
	private Context rContext;
	/**
	 * 该曲目的状态，已下载或是已缓存，参考{@link HistoryDBHelper#STATU}
	 */
	public int rStatu = 0;
	
	public RingtoneItemData(Context context, Cursor cursor, RingtoneManager ringtoneManager) {
		rContext = context;
		rRawId = cursor.getLong(RingtoneManager.HISTORY_RAWID_INDEX);
		rCreator = cursor.getString(RingtoneManager.HISTORY_CREATOR_INDEX);
		rTimeShare = cursor.getString(RingtoneManager.HISTORY_TIME_SHARE_INDEX);
		rOwner = cursor.getString(RingtoneManager.HISTORY_OWNER_INDEX);
		rName = cursor.getString(RingtoneManager.HISTORY_NAME_INDEX);
		rLength = cursor.getLong(RingtoneManager.HISTORY_LENGTH_INDEX);
		rMimeType = cursor.getString(RingtoneManager.HISTORY_MIME_TYPE_INDEX);
		rDwonloadCount = cursor.getInt(RingtoneManager.HISTORY_DOWNLOAD_COUNT_INDEX);
		rRawUrl = cursor.getString(RingtoneManager.HISTORY_RAW_URI_INDEX);
		rLocalPath = cursor.getString(RingtoneManager.HISTORY_LOCAL_URI_INDEX);
		rStatu = cursor.getInt(RingtoneManager.HISTORY_STATU_INDEX);
		String fullName = ringtoneManager.buildFullNameWithID(rName, rRawId, rMimeType);
		File downloadFile = ringtoneManager.isFileExistInDownloadDir(fullName);
		Log.v("RingtoneItemData", "statu " + rStatu);
		
		if (downloadFile != null) {
			rLocalPath = downloadFile.getAbsolutePath();
			rStatu = HistoryDBHelper.STATU_DOWNLOADED;
		}
	}
}
