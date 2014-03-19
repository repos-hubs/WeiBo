package com.kindroid.hub.ui;

import com.kindroid.hub.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;

public class HubMain extends TabActivity implements View.OnClickListener{
	
	private final static String TAB_TAG_SUMMARY = "tab_tag_summary";
	private final static String TAB_TAG_CATEGORY = "tab_tag_category";
	private final static String TAB_TAG_TRIBE = "tab_tag_tribe";
	
	private ImageView mSummary;
	private ImageView mCategory;
	private ImageView mTribe;
	
	private Intent mSummaryIntent;
	private Intent mCategoryIntent;
	private Intent mTribeIntent;
	
	private LinearLayout mSummaryLayout;
	private LinearLayout mCategoryLayout;
	private LinearLayout mTribeLayout;
	
	private TabHost mTabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findViews();
		prepareIntent();
		setupIntent();
	}
	
	private void findViews() {
		mSummary = (ImageView) findViewById(R.id.summaryImageView);
		mSummary.setOnClickListener(this);
		mCategory = (ImageView) findViewById(R.id.categoryImageView);
		mCategory.setOnClickListener(this);
		mTribe = (ImageView) findViewById(R.id.TribeImageView);
		mTribe.setOnClickListener(this);
		
		mSummaryLayout = (LinearLayout) findViewById(R.id.summaryLayout);
//		mSummaryLayout.setOnClickListener(this);
		mCategoryLayout = (LinearLayout) findViewById(R.id.categoryLayout);
//		mCategoryLayout.setOnClickListener(this);
		mTribeLayout = (LinearLayout) findViewById(R.id.tribeLayout);
//		mTribeLayout.setOnClickListener(this);
	}
	
	private void prepareIntent() {
		mSummaryIntent = new Intent(this, HubSummarySina.class);
		mCategoryIntent = new Intent(this, HubCategory.class);
//		mTribeIntent = new Intent(this, HubTribe.class);
		mTribeIntent = new Intent(this, AtMeMain.class);
	}
	
	private void setupIntent() {
		this.mTabHost=getTabHost();
		TabHost localTabHost=this.mTabHost;
		localTabHost.addTab(buildTabSpec(TAB_TAG_SUMMARY, R.string.app_name, R.drawable.icon, mSummaryIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_CATEGORY, R.string.app_name, R.drawable.icon, mCategoryIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_TRIBE, R.string.app_name, R.drawable.icon, mTribeIntent));
	}
	
	private TabHost.TabSpec buildTabSpec(String tag, int resLabel, int resIcon,Intent content) {
		return mTabHost.newTabSpec(tag).setIndicator(getString(resLabel),getResources().getDrawable(resIcon)).setContent(content);
	} 

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.summaryImageView) {
			mSummary.setImageResource(R.drawable.summary_on);
			mSummaryLayout.setBackgroundResource(R.drawable.foot_on);
			mCategory.setImageResource(R.drawable.category);
			mCategoryLayout.setBackgroundResource(R.drawable.foot);
			mTribe.setImageResource(R.drawable.tribe);
			mTribeLayout.setBackgroundResource(R.drawable.foot);
			mTabHost.setCurrentTabByTag(TAB_TAG_SUMMARY);
		} else if (v.getId() == R.id.categoryImageView) {
			mSummary.setImageResource(R.drawable.summary);
			mSummaryLayout.setBackgroundResource(R.drawable.foot);
			mCategory.setImageResource(R.drawable.category_on);
			mCategoryLayout.setBackgroundResource(R.drawable.foot_on);
			mTribe.setImageResource(R.drawable.tribe);
			mTribeLayout.setBackgroundResource(R.drawable.foot);
			mTabHost.setCurrentTabByTag(TAB_TAG_CATEGORY);
		} else if (v.getId() == R.id.TribeImageView) {
			mSummary.setImageResource(R.drawable.summary);
			mSummaryLayout.setBackgroundResource(R.drawable.foot);
			mCategory.setImageResource(R.drawable.category);
			mCategoryLayout.setBackgroundResource(R.drawable.foot);
			mTribe.setImageResource(R.drawable.tribe_on);
			mTribeLayout.setBackgroundResource(R.drawable.foot_on);
			mTabHost.setCurrentTabByTag(TAB_TAG_TRIBE);
		}
	}

}
