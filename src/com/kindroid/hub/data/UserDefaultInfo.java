package com.kindroid.hub.data;

import android.content.Context;
import android.content.SharedPreferences;

public final class UserDefaultInfo {

	private final static String USER_INFO = "user_info";
	private final static String USER_TOKEN = "user_token";
	private final static String USER_TIPS = "user_tips";
	
	// weibo token
	private final static String WEIBO_TOKEN = "weibo_token";
	private final static String WEIBO_SECRET = "weibo_secret";
	private final static String WEIBO_ID = "weibo_id";

	public static String getUserToken(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(USER_INFO,Context.MODE_PRIVATE);
		return preferences.getString(USER_TOKEN, null);
	}
	
	public static boolean setUserToken(Context context,String token){
		SharedPreferences preferences = context.getSharedPreferences(USER_INFO,Context.MODE_PRIVATE);
		return preferences.edit().putString(USER_TOKEN, token).commit();
	}
	
	public static boolean getUserTips(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(USER_INFO,Context.MODE_PRIVATE);
		return preferences.getBoolean(USER_TIPS, false);
	}
	
	public static boolean setUserTips(Context context,boolean value){
		SharedPreferences preferences = context.getSharedPreferences(USER_INFO,Context.MODE_PRIVATE);
		return preferences.edit().putBoolean(USER_TIPS, value).commit();
	}
	
	public static String getWeiboToken(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(USER_INFO,Context.MODE_PRIVATE);
		return preferences.getString(WEIBO_TOKEN,null);
	}
	
	public static boolean putWeiboToken(Context context,String value){
		SharedPreferences preferences = context.getSharedPreferences(USER_INFO,Context.MODE_PRIVATE);
		return preferences.edit().putString(WEIBO_TOKEN, value).commit();
	}
	
	public static String getWeiboTokenSecret(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(USER_INFO,Context.MODE_PRIVATE);
		return preferences.getString(WEIBO_SECRET,null);
	}
	
	public static boolean putWeiboTokenSecret(Context context,String value){
		SharedPreferences preferences = context.getSharedPreferences(USER_INFO,Context.MODE_PRIVATE);
		return preferences.edit().putString(WEIBO_SECRET, value).commit();
	}
	
	public static String getWeiboId(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(USER_INFO,Context.MODE_PRIVATE);
		return preferences.getString(WEIBO_ID,null);
	}
	
	public static boolean putWeiboId(Context context,String value){
		SharedPreferences preferences = context.getSharedPreferences(USER_INFO,Context.MODE_PRIVATE);
		return preferences.edit().putString(WEIBO_ID, value).commit();
	}
}
