package com.kindroid.hub.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import weibo4android.Paging;
import weibo4android.Status;
import weibo4android.User;
import weibo4android.Weibo;
import weibo4android.WeiboException;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.IndexAdapter;
import com.kindroid.hub.data.UserDefaultInfo;
import com.kindroid.hub.utils.Constant;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UserDetailInfo extends BaseActivity implements OnClickListener,OnItemClickListener{
	
	public static final String U_ID = "u_id";
	public static final String IS_MY = "is_my";

	private ImageView mBackImageView;
	private long mUid;
	private User mUser;
	
	private TextView mNickname;
	private TextView mUserName;
	private TextView mFriendsTextView;
	private TextView mFollowsTextView;
	private TextView mGenderTextView;
	private TextView mLocationTextView;
	private TextView mAt;
	private InputStream mUserAvatar;
	private ImageView mUserAvatarImageView;

	private ListView mUserStatusListView;
	private List<Status> mListStatus;
	private ImageView mFollowOperate;
	
	private boolean mIsFollow;
	private int mFriendsCount;
	private boolean mIsMy;
	private TextView mFavoriteTextView;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			mProgressDialog.dismiss();
			switch (msg.what) {
			case 0:
				fillData();
				break;
			case 1:
				if (mUserAvatar != null) {
					mUserAvatarImageView.setImageBitmap(BitmapFactory.decodeStream(mUserAvatar));
				}
				break;
			case 2:
				IndexAdapter indexAdapter = new IndexAdapter(UserDetailInfo.this, mListStatus);
				mUserStatusListView.setAdapter(indexAdapter);
				break;
			case 3:
				mFriendsCount = mFriendsCount - 1;
				mFriendsTextView.setText(mFriendsCount + "");
				mIsFollow = false;
				mFollowOperate.setImageResource(R.drawable.follow_btn);
				Toast.makeText(UserDetailInfo.this, R.string.cancel_follow_success, Toast.LENGTH_SHORT).show();
				break;
			case 4:
				Toast.makeText(UserDetailInfo.this, R.string.cancel_follow_failure, Toast.LENGTH_SHORT).show();
				break;
			case 5:
				mFriendsCount = mFriendsCount + 1;
				mFriendsTextView.setText(mFriendsCount + "");
				mIsFollow = true;
				mFollowOperate.setImageResource(R.drawable.cancel_follow_btn);
				Toast.makeText(UserDetailInfo.this, R.string.follow_success, Toast.LENGTH_SHORT).show();
				break;
			case 6:
				Toast.makeText(UserDetailInfo.this, R.string.follow_failure, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}

	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_detail_info);
		Intent intent = getIntent();
		if (intent != null) {
			mUid = intent.getLongExtra(U_ID, -1);
			mIsMy = intent.getBooleanExtra(IS_MY, false);
			findViews();
			showProgressDialog();
			new LoadUser().start();
		}
	}
	
	private void findViews() {
		mBackImageView = (ImageView) findViewById(R.id.backImageView);
		mBackImageView.setOnClickListener(this);
		mNickname = (TextView) findViewById(R.id.nickNameTextView);
		mUserName = (TextView) findViewById(R.id.userNameTextView);
		mUserAvatarImageView = (ImageView) findViewById(R.id.userAvatarImageView);
		mFriendsTextView = (TextView) findViewById(R.id.friendTextView);
		mFollowsTextView = (TextView) findViewById(R.id.followsTextView);
		mGenderTextView = (TextView) findViewById(R.id.genderTextView);
		mLocationTextView = (TextView) findViewById(R.id.locationTextView);
		
		mAt = (TextView) findViewById(R.id.atTextView);
		mAt.setOnClickListener(this);

		mUserStatusListView = (ListView) findViewById(R.id.listViewUser);
		mUserStatusListView.setOnItemClickListener(this);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.listview_footer, null);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(UserDetailInfo.this,SinaStatusMore.class);
				intent.putExtra(U_ID, mUid + "");
				startActivity(intent);
			}
		});
		mUserStatusListView.addFooterView(view);
		mFollowOperate = (ImageView) findViewById(R.id.followOperate);
		if (!mIsMy) {
			mFollowOperate.setVisibility(View.VISIBLE);
			mFollowOperate.setOnClickListener(this);
		} else {
			mFollowOperate.setVisibility(View.INVISIBLE);
			LinearLayout modifyUser = (LinearLayout) findViewById(R.id.modifyUserInfoLayout);
			modifyUser.setOnClickListener(this);
		}

		LinearLayout friendLayout = (LinearLayout) findViewById(R.id.friendsLayout);
		friendLayout.setOnClickListener(this);

		LinearLayout followsLayout = (LinearLayout) findViewById(R.id.followsLayout);
		followsLayout.setOnClickListener(this);
		
		LinearLayout atLayout = (LinearLayout) findViewById(R.id.atLayout);
		if (!mIsMy) {
			atLayout.setOnClickListener(this);
			atLayout.setVisibility(View.VISIBLE);
		} else {
			atLayout.setVisibility(View.GONE);
			LinearLayout favorite = (LinearLayout) findViewById(R.id.favoriteLayout);
			favorite.setOnClickListener(this);
			favorite.setVisibility(View.VISIBLE);

			LinearLayout topic = (LinearLayout) findViewById(R.id.topicLayout);
			topic.setOnClickListener(this);
			topic.setVisibility(View.VISIBLE);
		}
		mFavoriteTextView = (TextView) findViewById(R.id.favoriteTextView);
	}
	
	private void fillData(){
		if (mUser != null) {
			mIsFollow = true;
			mNickname.setText(mUser.getName());
			mFriendsCount = mUser.getFriendsCount();
			mFriendsTextView.setText(mUser.getFriendsCount() + "");
			mFollowsTextView.setText(mUser.getFollowersCount() + "");
			mUserName.setText(mUser.getScreenName());
			mLocationTextView.setText(mUser.getLocation());
			if ("m".equals(mUser.getGender())) {
				mGenderTextView.setText(R.string.msg_gender_man);
			} else if ("f".equals(mUser.getGender())) {
				mGenderTextView.setText(R.string.msg_gender_woman);
			} else {
				mGenderTextView.setText(R.string.msg_gender_secret_modify);
			}
			mFavoriteTextView.setText(mUser.getFavouritesCount() + "");
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.backImageView:
			finish();
			break;
		case R.id.atTextView:
			break;
		case R.id.followOperate:
			showProgressDialog();
			new UpdateFollow().start();
			break;
		case R.id.friendsLayout:
			Intent intent = new Intent(this, SinaFollowMore.class);
			intent.putExtra(U_ID, mUid + "");
			startActivity(intent);
			break;
		case R.id.followsLayout:
			Intent intentFans = new Intent(this, SinaFollowMore.class);
			intentFans.putExtra(U_ID, mUid + "");
			intentFans.putExtra(SinaFollowMore.TYPE, true);
			startActivity(intentFans);
			break;
		case R.id.atLayout:
			if (mUser != null) {
				Intent intentWriteTo = new Intent(this,HubTribeWriteWeibo.class);
				HubTribeWriteWeibo.userName = mUser.getName();
				startActivity(intentWriteTo);
			}
			break;
		case R.id.favoriteLayout:
			Intent favoriteIntent = new Intent(this, SinaStatusMore.class);
			favoriteIntent.putExtra(SinaStatusMore.DATA_TYPE, 1);
			startActivity(favoriteIntent);
			break;
		case R.id.topicLayout:
			Intent topicIntent = new Intent(this, AtMeTopicActivity.class);
			startActivity(topicIntent);
			break;
		case R.id.modifyUserInfoLayout:
/*			Intent modifyIntent = new Intent(this,AtMeMyselfModificationActivity.class);
			AtMeMyselfModificationActivity.userInfo = mUser;
			startActivity(modifyIntent);*/
			break;
		default:
			break;
		}
	}
	
	public class LoadUser extends Thread {
		public void run() {
			if (mUid != -1) {
				System.setProperty("weibo4j.oauth.consumerKey",Constant.CONSUMER_KEY);
				System.setProperty("weibo4j.oauth.consumerSecret",Constant.CONSUMER_SECRET);
				Weibo weibo = new Weibo();
				weibo.setToken(UserDefaultInfo.getWeiboToken(UserDetailInfo.this), UserDefaultInfo.getWeiboTokenSecret(UserDetailInfo.this));
				
				try {
					mUser = weibo.showUser(mUid + "");
					mHandler.sendEmptyMessage(0);
					URL url = new URL(mUser.getProfileImageURL().toString());
					mUserAvatar = (InputStream) url.getContent();
					mHandler.sendEmptyMessage(1);
					mListStatus = weibo.getUserTimeline(mUid + "",new Paging(1,10));
					mHandler.sendEmptyMessage(2);
				} catch (WeiboException e) {
					mHandler.sendEmptyMessage(7);
					e.printStackTrace();
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public class UpdateFollow extends Thread{
		public void run(){
			if (mUid != -1) {
				System.setProperty("weibo4j.oauth.consumerKey",Constant.CONSUMER_KEY);
				System.setProperty("weibo4j.oauth.consumerSecret",Constant.CONSUMER_SECRET);
				Weibo weibo = new Weibo();
				weibo.setToken(UserDefaultInfo.getWeiboToken(UserDetailInfo.this), UserDefaultInfo.getWeiboTokenSecret(UserDetailInfo.this));
				try {
					User user;
					if (mIsFollow) {
						user = weibo.destroyFriendshipByUserid(mUid + "");
						if (user != null) {
							mHandler.sendEmptyMessage(3);
						} else {
							mHandler.sendEmptyMessage(4);
						}
					} else {
						user = weibo.createFriendshipByUserid(mUid + "");
						if (user != null) {
							mHandler.sendEmptyMessage(5);
						} else {
							mHandler.sendEmptyMessage(6);
						}
					}
				} catch (WeiboException e) {
					mHandler.sendEmptyMessage(7);
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		if (position < mListStatus.size()) {
			Intent intent = new Intent(this, HubSinaWeiboDetailsActivity.class);
			intent.putExtra("listPosition", position);
			HubSinaWeiboDetailsActivity.statusList = mListStatus;
			startActivity(intent);
		}
	}

}
