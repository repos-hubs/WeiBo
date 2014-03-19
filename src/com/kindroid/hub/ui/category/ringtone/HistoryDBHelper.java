package com.kindroid.hub.ui.category.ringtone;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryDBHelper extends SQLiteOpenHelper{

	private static String mDbName = "ring.db";
	private static int mDbVersion = 13;
	public static final String _ID = "_id";
	public static final String RAW_ID = "raw_id";
	public static final String CREATOR = "creator";
	public static final String OWNER = "owner";
	public static final String TIME_SHARE = "time_share";
	public static final String NAME = "name";
	public static final String LENGTH = "length";
	public static final String RAW_URI = "uri";
	public static final String LOCAL_URI = "path";
	public static final String MIME_TYPE = "mime";
	public static final String DOWNLOAD_COUNT = "download_count";
	/**0未下载1正在下载2已缓存3已下载*/
	public static final String STATU = "statu";
	public static final int STATU_NOMAL = 0;
	public static final int STATU_DOWNLOADING = 1;
	public static final int STATU_CACHED = 2;
	public static final int STATU_DOWNLOADED = 3;
	
	public static final String DATE = "date";
	public static final String TYPE = "type";
	public static final String PHOTO = "photo";
	public static final int RECOMMAND_TYPE=0;
	public static final int NEWEST_TYPE=1;
	public static final int TOP_TYPE=2;
	/**本地视图*/
	public static final int OTHER_TYPE=3;
	/**搜索时使用，每次不同的搜索都要清空该type的数据*/
	public static final int SEARCH_TYPE=4;

	
	public static final String HISTORY_TABLE_NAME = "history";
	public static final String DOWNLOAD_TABLE_NAME = "download";
	private static final String CREATE_HISTORY_TABLE = "CREATE TABLE " + HISTORY_TABLE_NAME + " (" +
	_ID + " INTEGER PRIMARY KEY, " +
	RAW_ID + " INTEGER NOT NULL, " +
	CREATOR + " TEXT NOT NULL DEFAULT 0, " + 
	OWNER + " TEXT NOT NULL DEFAULT 0, " + 
	TIME_SHARE + " TEXT NOT NULL DEFAULT 0, " +
	DATE + " INTEGER NOT NULL DEFAULT 0, " +
	NAME + " TEXT NOT NULL DEFAULT 0, " +
	LENGTH + " INTEGER NOT NULL DEFAULT 0, " +
	TYPE + " INTEGER NOT NULL DEFAULT 1, " +
	DOWNLOAD_COUNT + " INTEGER NOT NULL DEFAULT 0, " +
	STATU + " INTEGER NOT NULL DEFAULT 0, " +
	MIME_TYPE + " TEXT, " +
	PHOTO + " BLOB, " +
	LOCAL_URI + " TEXT, " +
	RAW_URI + " TEXT" + ");";
	
	private static final String CREATE_DOWNLOAD_TABLE = "CREATE TABLE " + DOWNLOAD_TABLE_NAME + " (" +
	_ID + " INTEGER PRIMARY KEY, " +
	RAW_ID + " INTEGER NOT NULL, " +
	CREATOR + " TEXT NOT NULL DEFAULT 0, " + 
	OWNER + " TEXT NOT NULL DEFAULT 0, " + 
	TIME_SHARE + " TEXT NOT NULL DEFAULT 0, " +
	DATE + " INTEGER NOT NULL DEFAULT 0, " +
	NAME + " TEXT NOT NULL DEFAULT 0, " +
	LENGTH + " INTEGER NOT NULL DEFAULT 0, " +
	DOWNLOAD_COUNT + " INTEGER NOT NULL DEFAULT 0, " +
	RAW_URI + " TEXT, " +
	LOCAL_URI + " TEXT, " +
	STATU + " INTEGER NOT NULL DEFAULT 0, " +
	MIME_TYPE + " TEXT" +");";
	
	private static SQLiteDatabase mDb ;
	
	public HistoryDBHelper(Context context) {
		super(context, mDbName, null, mDbVersion);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_HISTORY_TABLE);
		db.execSQL(CREATE_DOWNLOAD_TABLE);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if ( newVersion>oldVersion ){
			db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + DOWNLOAD_TABLE_NAME);
			onCreate(db);
		}
	}
	
	public SQLiteDatabase getDatabase() {
		if (mDb == null) {
			mDb = getWritableDatabase();
		}
		return mDb;
	}
	
	public void closeDatabase() {
		if (mDb != null && mDb.isOpen())
			mDb.close();
	}
}
