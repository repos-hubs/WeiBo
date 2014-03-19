package com.kindroid.hub.ui;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.kindroid.hub.R;
import com.kindroid.hub.data.UserDefaultInfo;

public class AtMeMain extends TabActivity implements View.OnClickListener {
	private static final String TAG = "AtMeMain";

	private final static String TAB_TAG_FRIEND = "tab_tag_friend";
	private final static String TAB_TAG_GROUP = "tab_tag_group";
	private final static String TAB_TAG_AT_ME = "tab_tag_me";
	
	private TextView mFriend;
	private TextView mGroup;
//	private TextView mAtMe;
	
	private Intent mFriendIntent;
	private Intent mGroupIntent;
//	private Intent mAtMeIntent;
	
	private LinearLayout mFriendLayout;
	private LinearLayout mGroupLayout;
//	private LinearLayout mAtMeLayout;
	
	private ImageView backBtn;
	private ImageView searchBtn;
	
	private TabHost mTabHost;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.at_me_main);
		findViews();
		prepareIntent();
		setupIntent();
	}
	
	private void findViews() {
		mFriend = (TextView) findViewById(R.id.friendTextView);
		mFriend.setOnClickListener(this);
		mGroup = (TextView) findViewById(R.id.groupTextView);
		mGroup.setOnClickListener(this);
//		mAtMe = (TextView) findViewById(R.id.atMeTextView);
//		mAtMe.setOnClickListener(this);
		
		mFriendLayout = (LinearLayout) findViewById(R.id.friendLayout);
		mGroupLayout = (LinearLayout) findViewById(R.id.groupLayout);
//		mAtMeLayout = (LinearLayout) findViewById(R.id.atMeLayout);
		
		backBtn = (ImageView) findViewById(R.id.btn_back);
		backBtn.setOnClickListener(this);
		
		searchBtn = (ImageView) findViewById(R.id.btn_search);
		searchBtn.setOnClickListener(this);
	}
	
	private void prepareIntent() {
		mFriendIntent = new Intent(this, AtMeStatusActivity.class);
		mGroupIntent = new Intent(this, AtMeCommentsActivity.class);
//		mAtMeIntent = new Intent(this, AtMeMyselfActivity.class);
	}
	
	private void setupIntent() {
		this.mTabHost = getTabHost();
		TabHost localTabHost = this.mTabHost;
		localTabHost.addTab(buildTabSpec(TAB_TAG_FRIEND, R.string.app_name, R.drawable.icon, mFriendIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_GROUP, R.string.app_name, R.drawable.icon, mGroupIntent));
//		localTabHost.addTab(buildTabSpec(TAB_TAG_AT_ME, R.string.app_name, R.drawable.icon, mAtMeIntent));
	}
	
	private TabHost.TabSpec buildTabSpec(String tag, int resLabel, int resIcon,Intent content) {
		return mTabHost.newTabSpec(tag).setIndicator(getString(resLabel),getResources().getDrawable(resIcon)).setContent(content);
	} 

	@Override
	
	public void onClick(View v) {
		if (v.getId() == R.id.friendTextView) {
			mFriendLayout.setBackgroundResource(R.drawable.at_me_tab_left_on);
			mGroupLayout.setBackgroundDrawable(null);
			mTabHost.setCurrentTabByTag(TAB_TAG_FRIEND);
		}
		
		if (v.getId() == R.id.groupTextView) {
			mFriendLayout.setBackgroundDrawable(null);
			mGroupLayout.setBackgroundResource(R.drawable.at_me_tab_right_on);
			mTabHost.setCurrentTabByTag(TAB_TAG_GROUP);
		}
		
		if (v.getId() == R.id.btn_back) {
			finish();
		}
		if (v.getId() == R.id.btn_search) {
			Intent intent = new Intent(AtMeMain.this, UserDetailInfo.class);
			intent.putExtra(UserDetailInfo.U_ID,Long.parseLong(UserDefaultInfo.getWeiboId(this)));
			intent.putExtra(UserDetailInfo.IS_MY, true);
			startActivity(intent);
		}
	}
	
}
