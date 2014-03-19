package com.kindroid.hub.utils;

import android.os.Environment;

public class Constant {

	public final static String GET_ITEMS_URL = "http://192.168.0.31:8080/microTribe/item/show";

	public final static String CATEGORY_URL = "http://192.168.0.31:8080/microTribe/item/show";
	public final static String LIST_GROUPS_URL = "http://192.168.0.31:8080/microTribe/group/list/";
	public final static String LIST_FRIENDS_BY_GROUP_URL = "http://192.168.0.31:8080/microTribe/group/list/friend/";
	public final static String CREATE_GROUP_URL = "http://192.168.0.31:8080/microTribe/group/create/";
	public final static String CREATE_RELEASE_URL = "http://192.168.0.31:8080/microTribe/hotdynamic/releaseCreate/";
	public final static String GET_USER_DETAIL_URL = "http://192.168.0.31:8080/microTribe/account/detail/";
	public final static String MODIFY_NICK_NAME_URL = "http://192.168.0.31:8080/microTribe/account/edit/nickName/";
	public final static String BINDING_EMAIL_URL = "http://192.168.0.31:8080/microTribe/account/email/bind/";
	public final static String SEARCH_WEIBOS_URL = "http://192.168.0.31:8080/microTribe/item/search";
	public final static String MODIFY_GENDER_URL = "http://192.168.0.31:8080/microTribe/account/edit/sex/";
	public final static String SEARCH_HOT_TRIBE_URL = "http://192.168.0.31:8080/microTribe/hotdynamic/showRelease/";
	
	public final static String SUMMARY_URL = "http://192.168.0.31:8080/microTribe/category/list/";
	public final static String SEND_REPLY_CONTENT_URL = "http://192.168.0.31:8080/microTribe/review/create/";

	public final static String RING_OR_WALLPAPER_LIST_URL = "http://192.168.0.31:8080/microTribe/ringorwallpaper/list/";
	public final static String RING_OR_WALLPAPER_OPERATION_URL = "http://192.168.0.31:8080/microTribe/ringorwallpaper/operate/";
	public final static String RING_OR_WALLPAPER_DOWNLOAD_URL = "http://192.168.0.31:8080/microTribe/ringorwallpaper/download/";
	public final static String RING_OR_WALLPAPER_SEARCH_URL = "http://192.168.0.31:8080/microTribe/ringorwallpaper/search/";
	public final static String GET_COMMENTS_URL = "http://192.168.0.31:8080/microTribe/review/show";

	public final static String RING_OR_WALLPAPER_REVIEW_LIST_URL = "http://192.168.0.31:8080/microTribe/ringorwallpaper/review/list/";
	public final static String TRIBE_URL = "http://192.168.0.31:8080/microTribe/hotdynamic/show";
	public final static String TRIBE_DYNAMIC_URL = "http://192.168.0.31:8080/microTribe/hotdynamic/showRelease";
	
	public final static String USER_LOGIN_URL = "http://192.168.0.31:8080/microTribe/account/generalSignIn";
	
	public final static String USER_REGISTER_URL = "http://192.168.0.31:8080/microTribe/account/signUp";
	public final static String USER_WEIBO_LOGIN_URL = "http://192.168.0.31:8080/microTribe/account/weiboSignIn";
	public final static String USER_EDIT_NICKNAME_URL = "http://192.168.0.31:8080/microTribe/account/edit/nickName";
	public final static String USER_EDIT_PASSWORD_URL = "http://192.168.0.31:8080/microTribe/account/edit/passWord";
	public final static String USER_EDIT_AVATER_URL = "http://192.168.0.31:8080/microTribe/account/edit/icon";
	public final static String USER_FIND_PASSWORD_BY_EMAIL = "http://192.168.0.31:8080/microTribe/account/forgotPasswordByEmail";
	public final static String USER_STATISTICS = "http://192.168.0.31:8080/microTribe/report/client/";
	public final static String WALLPAPERDOWNLOADPATH=Environment.getExternalStorageDirectory()+"/hub_download/wallpaper/";
	public final static String IMAGE_DOWNLOAD_PATH = Environment.getExternalStorageDirectory()+"/hub_photo/";
	
	public final static String CONSUMER_KEY = "2568701646";
	public final static String CONSUMER_SECRET = "9489704a62db482fc3a0f632c6823e9d";
}
