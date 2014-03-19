package com.kindroid.hub.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CategoryCacheDb {

	
	private final static String DATABASE_NAME = "hub_cache.db";
	private final static int DATABASE_VERSION = 2;

	public final static String KEY_ID = "_id";

	
	//weibo table
	public final static String WEIBO_TABLE_NAME = "weibocontent";
	public final static String KEY_AVATAR_URL = "avatar_url";
	public final static String KEY_USER_TITLE = "user_title";
	public final static String KEY_CREATE_TIME = "create_time";
	public final static String KEY_WEIBO_DATA_ID = "data_id";
	public final static String KEY_DATA_CONTENT = "data_content";
	public final static String KEY_DATA_CONTENT_URL = "data_content_url";
	public final static String KEY_FORWARD_CONTENT = "forward_content";
	public final static String KEY_FORWARD_CONTENT_URL = "forward_content_url";
	public final static String KEY_FROM_CLIENT = "from_client";
	public final static String KEY_FROM_WEBSITE = "from_website";
	public final static String KEY_WEIBO_TYPE = "weibo_type";
	public final static String KEY_EXIST_FORWARD = "exist_forward";
	public final static String KEY_IMG_TAG = "img_tag";
	public final static String KEY_USER_ID = "user_id";
	public final static String KEY_USER_FLAG = "user_flag";//1.list  2.category list
	

	private static final String CONTENTS_TABLE_CREATE_SQL = 
		"create table weibocontent (_id integer primary key autoincrement, "
		+ "avatar_url text, user_title text, create_time text, data_id text," 
		+ "data_content text, data_content_url text, forward_content text, forward_content_url text," 
		+ "from_client text, from_website integer, weibo_type integer, exist_forward text, img_tag text,user_id text,user_flag text" 
		+		");";
	private DatabaseHelper dbHelper;
	private final Context mCtx;
	private SQLiteDatabase mDb;

	public static class DatabaseHelper extends SQLiteOpenHelper{
		public DatabaseHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
            db.execSQL(CONTENTS_TABLE_CREATE_SQL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS weibocontent");
            onCreate(db);
		}
	}
	
	public CategoryCacheDb(Context context){
		this.mCtx=context;
		
	}
	
	public CategoryCacheDb open(){
		dbHelper=new DatabaseHelper(mCtx);
		mDb=dbHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		dbHelper.close();
	}
	
	public long insertWeiboTable(String avatarUrl, String title, String time, String dataId, String weiboContent, 
			String contentUrl, String forwardContent, String forwardUrl, String fromClient, int fromWebSite, int weiboType, 
			String existForward, String imgTag,String userId,String userFlag) {
		ContentValues values = new ContentValues();
		values.put(KEY_AVATAR_URL, avatarUrl);
		values.put(KEY_USER_TITLE, title);
		values.put(KEY_CREATE_TIME, time);
		values.put(KEY_WEIBO_DATA_ID, dataId);
		values.put(KEY_DATA_CONTENT, weiboContent);
		values.put(KEY_DATA_CONTENT_URL, contentUrl);
		values.put(KEY_FORWARD_CONTENT, forwardContent);
		values.put(KEY_FORWARD_CONTENT_URL, forwardUrl);
		values.put(KEY_FROM_CLIENT, fromClient);
		values.put(KEY_FROM_WEBSITE, fromWebSite);
		values.put(KEY_WEIBO_TYPE, weiboType);
		values.put(KEY_EXIST_FORWARD, existForward);
		values.put(KEY_IMG_TAG, imgTag);
		values.put(KEY_USER_ID, userId);
		values.put(KEY_USER_FLAG, userFlag);
		return mDb.insert(WEIBO_TABLE_NAME, null, values);
	}
	
	
	public Cursor getWeiboList(int fromWebSite, int weiboType, String isBrowseMode) {
		return mDb.query(WEIBO_TABLE_NAME, new String[] { KEY_ID,
				KEY_AVATAR_URL, KEY_USER_TITLE, KEY_CREATE_TIME, KEY_WEIBO_DATA_ID, KEY_FORWARD_CONTENT_URL,
				KEY_FROM_CLIENT, KEY_FROM_WEBSITE, KEY_WEIBO_TYPE, KEY_EXIST_FORWARD, KEY_IMG_TAG,
				KEY_DATA_CONTENT, KEY_DATA_CONTENT_URL, KEY_FORWARD_CONTENT,KEY_USER_ID}, 
				KEY_FROM_WEBSITE + " = " + fromWebSite + " and weibo_type = " + weiboType + " and img_tag = '" + isBrowseMode + "'"+ " and user_flag = '1'", null, null,
				null, null);
	}
	
	public Cursor getImgWeiboList(int fromWebSite, int weiboType, String imgTag) {
		return mDb.query(WEIBO_TABLE_NAME, new String[] { KEY_ID,
				KEY_AVATAR_URL, KEY_USER_TITLE, KEY_CREATE_TIME, KEY_WEIBO_DATA_ID, KEY_FORWARD_CONTENT_URL,
				KEY_FROM_CLIENT, KEY_FROM_WEBSITE, KEY_WEIBO_TYPE, KEY_EXIST_FORWARD, KEY_IMG_TAG,
				KEY_DATA_CONTENT, KEY_DATA_CONTENT_URL, KEY_FORWARD_CONTENT,KEY_USER_ID}, 
				KEY_FROM_WEBSITE + " = " + fromWebSite + " and weibo_type = " + weiboType + " and img_tag = '" + imgTag + "'"+ " and user_flag = '1'", null, null,
				null, null);
	}
	
	public boolean deleteWeibo(int fromWebSite, int channelType, String isBrowseMode){
		return mDb.delete(WEIBO_TABLE_NAME, KEY_FROM_WEBSITE + " = " + fromWebSite + " and weibo_type = " + channelType + " and img_tag = '" + isBrowseMode + "'"+ " and user_flag = '1'", null)>0;
	}
	
	public boolean deleteUserWeibo(int fromWebSite, int channelType,
			String isBrowseMode, String userId) {
		return mDb.delete(WEIBO_TABLE_NAME, KEY_FROM_WEBSITE + " = "
				+ fromWebSite + " and weibo_type = " + channelType
				+ " and img_tag = '" + isBrowseMode + "'" + "and user_id = '"
				+ userId + "'"+ " and user_flag = '2'", null) > 0;
	}
	
	public Cursor getWeiboListByUserId(int fromWebSite, int weiboType,
			String isBrowseMode, String userId) {
		return mDb.query(WEIBO_TABLE_NAME, new String[] { KEY_ID,
				KEY_AVATAR_URL, KEY_USER_TITLE, KEY_CREATE_TIME,
				KEY_WEIBO_DATA_ID, KEY_FORWARD_CONTENT_URL, KEY_FROM_CLIENT,
				KEY_FROM_WEBSITE, KEY_WEIBO_TYPE, KEY_EXIST_FORWARD,
				KEY_IMG_TAG, KEY_DATA_CONTENT, KEY_DATA_CONTENT_URL,
				KEY_FORWARD_CONTENT,KEY_USER_ID}, KEY_FROM_WEBSITE + " = " + fromWebSite
				+ " and weibo_type = " + weiboType + " and img_tag = '"
				+ isBrowseMode + "'"+ "and user_id = '"
				+ userId + "'"+ " and user_flag = '2'", null, null, null, null);
	}
	
}
