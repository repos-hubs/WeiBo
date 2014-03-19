package com.kindroid.hub.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.w3c.dom.Text;

import com.kindroid.hub.R;
import com.kindroid.hub.data.SummaryData;
import com.kindroid.hub.data.UserDefaultInfo;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.RingOrWallPaper;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;
import com.kindroid.hub.ui.category.WallpaperMain;
import com.kindroid.hub.ui.category.ringtone.RingtoneMain;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HubSummary extends Activity implements OnClickListener{

	private ProgressBar mProgressBar;
	
	private LinearLayout mNewsLayout;
	private LinearLayout mWallpaperLayout;
	private LinearLayout mJokeLayout;
	private LinearLayout mRingtoneLayout;
	
	private TextView newsTitle1;
	private TextView newsTitle2;
	private TextView newsTitle3;
	private TextView newsTitle4;
	
	private TextView jokeTitle1;
	private TextView jokeTitle2;
	
	private ImageView wallPaper;
	private TextView wallPaperTitle;
	
	private TextView ringName1;
	private TextView ringArtist1;
	private TextView ringName2;
	private TextView ringArtist2;
	private TextView ringName3;
	private TextView ringArtist3;
	
	private List<WeiboContent> mNewsList;
	private List<RingOrWallPaper> mRingList;
	private List<RingOrWallPaper> mWallPaperList;
	private List<WeiboContent> mJokeList;
	
	// load picture
	public String mUrl;
	public InputStream mInputStream;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (mNewsList != null && mNewsList.size() > 0) {
					newsTitle1.setText(mNewsList.get(0).getContent());
/*					newsTitle2.setText(mNewsList.get(1).getContent());
					newsTitle3.setText(mNewsList.get(2).getContent());
					newsTitle4.setText(mNewsList.get(3).getContent());*/
				}
				mProgressBar.setVisibility(View.INVISIBLE);
				break;
			case 1:
				if (mRingList != null && mRingList.size() > 2) {
					ringName1.setText(mRingList.get(0).getName());
					ringArtist1.setText(mRingList.get(0).getOwner());
					ringName2.setText(mRingList.get(1).getName());
					ringArtist2.setText(mRingList.get(1).getOwner());
					ringName3.setText(mRingList.get(2).getName());
					ringArtist3.setText(mRingList.get(2).getOwner());
				}
				break;
			case 2:
				if (mWallPaperList != null && mWallPaperList.size() > 0) {
					wallPaperTitle.setText(mWallPaperList.get(0).getName());
				}
				break;
			case 3:
				if (null != mInputStream) {
					wallPaper.setImageBitmap(BitmapFactory.decodeStream(mInputStream));
				}
				break;
			case 4:
				if (mJokeList != null && mJokeList.size() > 1) {
					jokeTitle1.setText(mJokeList.get(0).getContent());
					jokeTitle2.setText(mJokeList.get(1).getContent());
				}
				break;
			default:
				break;
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_summary);
		findViews();
		new LoadData().start();
		checkLogin();
	}
	
	private void findViews() {
		mNewsLayout = (LinearLayout) findViewById(R.id.newsLayout);
		mNewsLayout.setOnClickListener(this);
		mWallpaperLayout = (LinearLayout) findViewById(R.id.wallpaperLayout);
		mWallpaperLayout.setOnClickListener(this);
		mJokeLayout = (LinearLayout) findViewById(R.id.jokeLayout);
		mJokeLayout.setOnClickListener(this);
		mRingtoneLayout = (LinearLayout) findViewById(R.id.ringtoneLayout);
		mRingtoneLayout.setOnClickListener(this);

		newsTitle1 = (TextView) findViewById(R.id.newsTitle1);
		newsTitle2 = (TextView) findViewById(R.id.newsTitle2);
		newsTitle3 = (TextView) findViewById(R.id.newsTitle3);
		newsTitle4 = (TextView) findViewById(R.id.newsTitle4);
		
		jokeTitle1 = (TextView) findViewById(R.id.jokeTitle1);
		jokeTitle2 = (TextView) findViewById(R.id.jokeTitle2);

		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

		wallPaper = (ImageView) findViewById(R.id.wallPaper);
		wallPaperTitle = (TextView) findViewById(R.id.wallPaperTitle);

		ringName1 = (TextView) findViewById(R.id.ringName1);
		ringArtist1 = (TextView) findViewById(R.id.ringArtist1);
		ringName2=(TextView)findViewById(R.id.ringName2);
		ringArtist2=(TextView)findViewById(R.id.ringArtist2);
		ringName3=(TextView)findViewById(R.id.ringName3);
		ringArtist3=(TextView)findViewById(R.id.ringArtist3);
	}
	
	private void checkLogin(){
		if(TextUtils.isEmpty(UserDefaultInfo.getUserToken(this))){
			//no login
			TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
			String number = telephonyManager.getLine1Number();
			if (TextUtils.isEmpty(number)) {
				if (!UserDefaultInfo.getUserTips(this)) {
					Intent intent = new Intent(this, UserLoginDialogTips.class);
					startActivity(intent);
				}
			} else {
				Intent intent = new Intent(this, UserRegisterByPhone.class);
				startActivity(intent);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.newsLayout:
			Intent intent = new Intent(this, NewsListActivity.class);
			intent.putExtra("from", "news");
			startActivity(intent);
			break;
		case R.id.wallpaperLayout:
			Intent intentWallpaper = new Intent(this, WallpaperMain.class);
			startActivity(intentWallpaper);
			break;
		case R.id.ringtoneLayout:
			Intent intentRingtone = new Intent(this, RingtoneMain.class);
			startActivity(intentRingtone);
			break;
		case R.id.jokeLayout:
			Intent intentJoke = new Intent(this, NewsListActivity.class);
			intentJoke.putExtra("from", "laugh");
			startActivity(intentJoke);
			break;
		default:
			break;
		}
	}
	
	public class LoadData extends Thread {
		public void run() {
			SummaryData data = new SummaryData();
			mNewsList = data.getNewsOrJokeList(false);
			mHandler.sendEmptyMessage(0);
			mRingList = data.getRingOrWallpaperList(true);
			mHandler.sendEmptyMessage(1);
			mJokeList = data.getNewsOrJokeList(true);
			mHandler.sendEmptyMessage(4);
			mWallPaperList = data.getRingOrWallpaperList(false);
			mHandler.sendEmptyMessage(2);
			if (mWallPaperList != null && mWallPaperList.size() > 0) {
				mUrl = mWallPaperList.get(0).getDownloadUrl();
				new LoadImage().start();
			}
		}
	}
	
	public class LoadImage extends Thread{
		public void run(){
			try {
				URL url = new URL(mUrl);
				mInputStream = (InputStream) url.getContent();
				mHandler.sendEmptyMessage(3);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
