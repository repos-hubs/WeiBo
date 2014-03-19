package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.TribeAdapter;
import com.kindroid.hub.data.TribeData;
import com.kindroid.hub.data.UserDefaultInfo;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;

public class HubTribe extends Activity implements OnClickListener,OnScrollListener,OnItemClickListener{
	
	private RelativeLayout topLayout;
	
	private LinearLayout tabTopInnerLayout;
	
	private RelativeLayout tribeTabLayout;
	private RelativeLayout hotDynamicTabLayout;

	private ImageView atMeBtn;
	private ImageView publishMsgBtn;
	private ListView mListView;
	
	private TribeAdapter mTribeAdapter;
	
	private ImageView mTribeDynamicImage;
	
	private ImageView mHotDynamicImage;
	
	private ProgressDialog mProgressDialog;
	
	public static List<WeiboContent> mList = new ArrayList<WeiboContent>();
	public List<WeiboContent> mPage;
	
	private ImageView initImage;
	private int current = 1; // 默认选中第一个，可以动态的改变此参数值
	private boolean isAdd = false; // 是否添加过 top_select
	private int select_width; // top_select_width
	private int select_height; // top_select_height
	private int firstLeft; // 第一次添加后的左边距*****
	private int startLeft; // 起始左边距
	
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private int currentPage = 1;
	private boolean running = false;
	private LinearLayout footView;
	/**
	 * pagination
	 */
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mProgressDialog.dismiss();
			switch (msg.what) {
			case 0:
				if (null != mPage) {
					mList.clear();
					mList.addAll(mPage);
					mTribeAdapter = new TribeAdapter(HubTribe.this, mList);
					mListView.setAdapter(mTribeAdapter);
					if (mPage.size() < 10) {
						mListView.removeFooterView(footView);
					}
				} else {
					if (mTribeAdapter != null) {
						mTribeAdapter.notifyDataSetChanged();
						mListView.removeFooterView(footView);
					}
				}
				break;
			case 1:
				if (mTribeAdapter != null) {
					if (mPage != null) {
						mList.addAll(mPage);
						mTribeAdapter.notifyDataSetChanged();
						if (mPage.size() < 10) {
							mListView.removeFooterView(footView);
						}
					}
				}
				running = false;
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_tribe);
		findViews();
		showProgressDialog();
		new LoadData(false,1,1).start();
	}

	private void findViews() {
		atMeBtn = (ImageView) findViewById(R.id.atMeBtn);
		atMeBtn.setOnClickListener(this);
		publishMsgBtn = (ImageView) findViewById(R.id.publishMsgBtn);
		publishMsgBtn.setOnClickListener(this);
		
		mListView = (ListView) findViewById(R.id.listView);
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this);
		
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footView = (LinearLayout) layoutInflater.inflate(R.layout.listview_loading, null);
		
		mListView.addFooterView(footView);

		mTribeDynamicImage = (ImageView) findViewById(R.id.tribeDynamicImage);
		mHotDynamicImage = (ImageView) findViewById(R.id.hotDynamicImage);
		
		topLayout = (RelativeLayout) findViewById(R.id.topTabLayout);
		tabTopInnerLayout=(LinearLayout)findViewById(R.id.squareTabTopInnerLinearLayout);

		tribeTabLayout = (RelativeLayout) findViewById(R.id.tribeTabLayout);
		tribeTabLayout.setClickable(true);
		tribeTabLayout.setOnClickListener(this);
		hotDynamicTabLayout = (RelativeLayout) findViewById(R.id.hotDynamicTabLayout);
		hotDynamicTabLayout.setClickable(true);
		hotDynamicTabLayout.setOnClickListener(this);
		
		RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rl.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		initImage = new ImageView(this);
		initImage.setTag("first");
		initImage.setImageResource(R.drawable.tribe_tab_button_bg);

		// 默认选中项
		switch (current) {
		case 1:
			tribeTabLayout.addView(initImage, rl);
			mTribeDynamicImage.bringToFront();
			current = R.id.tribeTabLayout;
			break;
		case 2:
			hotDynamicTabLayout.addView(initImage, rl);
			current = R.id.hotDynamicTabLayout;
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.atMeBtn) {
			if (TextUtils.isEmpty(UserDefaultInfo.getUserToken(this))) {
				Intent i = new Intent(this, UserLogin.class);
				startActivity(i);
			} else {
				Intent intent = new Intent(HubTribe.this, AtMeMain.class);
				startActivity(intent);
			}
			return;
		} else if (v.getId() == R.id.publishMsgBtn) {
			if (TextUtils.isEmpty(UserDefaultInfo.getUserToken(this))) {
				Intent i = new Intent(this, UserLogin.class);
				startActivity(i);
			} else {
				Intent intent = new Intent(this, HubTribeWriteWeibo.class);
				startActivity(intent);
			}
			return;
		}
		
		if (!isAdd) {
			replace(); // 初次使用移除old 添加新的top_select为RelativeLayout所使用
			isAdd = true;
		}
		
		ImageView top_select = (ImageView) topLayout.findViewWithTag("move");
		int tabLeft;
		int endLeft = 0;
		boolean run = false;
		switch (v.getId()) {
		case R.id.tribeTabLayout:
			if (current != R.id.tribeTabLayout) {
				// 中心位置
				tabLeft = ((RelativeLayout) mTribeDynamicImage.getParent()).getLeft() + mTribeDynamicImage.getLeft() + mTribeDynamicImage.getWidth() / 2;
				// 最终位置
				endLeft = tabLeft - select_width / 2;
				current = R.id.tribeTabLayout;
				run = true;
			}
			break;
		case R.id.hotDynamicTabLayout:
			if (current != R.id.hotDynamicTabLayout) {
				tabLeft = ((RelativeLayout) mHotDynamicImage.getParent()).getLeft() + mHotDynamicImage.getLeft() + mHotDynamicImage.getWidth() / 2;
				endLeft = tabLeft - select_width / 2;
				current = R.id.hotDynamicTabLayout;
				run = true;
			}
			break;
		default:
			break;
		}
		tabTopInnerLayout.bringToFront();
		if (run) {
			TranslateAnimation animation = new TranslateAnimation(startLeft,endLeft - firstLeft, 0, 0);
			startLeft = endLeft - firstLeft; 
			animation.setDuration(300);
			animation.setFillAfter(true);
			top_select.startAnimation(animation);
		}
		changeTabImage(v);
		//init ListView
		currentPage = 1;
		if (mListView.getFooterViewsCount() == 0) {
			mListView.addFooterView(footView);
		}
	}
	
	private void replace() {
		switch (current) {
		case R.id.tribeTabLayout:
			changeTop(tribeTabLayout);
			break;
		case R.id.hotDynamicTabLayout:
			changeTop(hotDynamicTabLayout);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 切换选中图片
	 * @param view
	 */
	private void changeTabImage(View view) {
		showProgressDialog();
		switch (view.getId()) {
		case R.id.tribeTabLayout:
			mTribeDynamicImage.setImageResource(R.drawable.tribe_dy_temp_on);
			mHotDynamicImage.setImageResource(R.drawable.hot_dy_temp);
			new LoadData(false,1,1).start();
			break;
		case R.id.hotDynamicTabLayout:
			mTribeDynamicImage.setImageResource(R.drawable.tribe_dy_temp);
			mHotDynamicImage.setImageResource(R.drawable.hot_dy_temp_on);
			new LoadData(false,1,2).start();
			break;
		}
	}
	
	private void changeTop(RelativeLayout relativeLayout){
		ImageView old = (ImageView) relativeLayout.findViewWithTag("first");;
		select_width = old.getWidth();
		select_height = old.getHeight();
		
		RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(select_width, select_height);
		rl.leftMargin = old.getLeft() + ((RelativeLayout)old.getParent()).getLeft();
		rl.topMargin = old.getTop() + ((RelativeLayout)old.getParent()).getTop();
		
		// 获取起始位置
		firstLeft = old.getLeft() + ((RelativeLayout)old.getParent()).getLeft();
		
		ImageView iv = new ImageView(this);
		iv.setTag("move");
		iv.setImageResource(R.drawable.tribe_tab_button_bg);
		
		topLayout.addView(iv , rl);
		relativeLayout.removeView(old);
	}
	
	
	private void showProgressDialog(){
		mProgressDialog=new ProgressDialog(this);
		mProgressDialog.setTitle(getResources().getText(R.string.app_name));
		mProgressDialog.setMessage(getResources().getText(R.string.progress_message));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
		lastItem = firstVisibleItem + visibleItemCount - 1;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (!running && lastItem == mTribeAdapter.getCount() && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			running = true;
			currentPage = currentPage + 1;
			if (current == R.id.tribeTabLayout) {
				new LoadData(true,currentPage,1).start();
			} else if (current == R.id.hotDynamicTabLayout) {
				new LoadData(true,currentPage,2).start();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		if (position < mList.size()) {
			WeiboContent content = mList.get(position);
			NewsListActivity.weiboContent = content;
			Intent intent = new Intent(this, NewsDetailsActivity.class);
			intent.putExtra("fromMode", "tribe");
			intent.putExtra("listPosition", position);
			startActivity(intent);
		}
	}
	
	public class LoadData extends Thread {
		public boolean isFresh;
		public int startIndex;
		public int type;

		/**
		 * 
		 * @param isFresh
		 * @param startIndex
		 * @param type 1:tribe dynamic 2:hot dynamic
		 */
		public LoadData(boolean isFresh, int startIndex,int type) {
			this.isFresh = isFresh;
			this.startIndex = startIndex;
			this.type = type;
		}

		public void run() {
			TribeData data = new TribeData();
			if (type == 1) {
				mPage = data.getTribeDynamicList(startIndex);
			} else if (type == 2) {
				mPage = data.getHotDynamicList(startIndex);
			}
			if (!isFresh) {
				mHandler.sendEmptyMessage(0);
			} else {
				mHandler.sendEmptyMessage(1);
			}
		}
	}
}
