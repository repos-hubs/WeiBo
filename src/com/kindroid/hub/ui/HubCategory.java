package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.hub.R;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.db.CategoryCacheDb;
import com.kindroid.hub.download.LazyImageLoader;
import com.kindroid.hub.proto.CommonProtoc.ItemType;
import com.kindroid.hub.utils.ClientInfo;
import com.kindroid.hub.utils.HttpRequest;

public class HubCategory extends Activity implements View.OnClickListener,OnTouchListener{

	public final static int MENU_ABOUT = 1001;
	
	private ImageView newsImageView;
	private ImageView laughImageView;
	private ImageView beautyImageView;
	private ImageView conImageView;
	private ImageView recreationImageView;
	private ImageView movieImageView;
	private ImageView sportsImageView;
	private ImageView techImageView;
	private ImageView gameImageView;
	
	private ImageView mFashionImageView;
	private ImageView mStreettakeImageView;
	private ImageView mQuotationImage;
	
	private ImageView mPetImageView;
	private ImageView mCarImageView;
	private ImageView mEnglishImageView;

	private ImageView mTravelImageView;
	private ImageView mBusinessImageView;
	private ImageView mOriginalityImageView;
	
	private ImageView mChangeDataResource;
	private TextView mTitleTextView;
	private View mPopView;
	private boolean mIsOpen = false;
	private PopupWindow mPopupWindow;
	
	private LazyImageLoader mImageLoader;
	private ImageView mAutoImageView;
	
	private List<List<Bitmap>> mAllMap = new ArrayList<List<Bitmap>>();
	
	private PicPoll picPoll;
	
	private int mDataType = 0;
	
	public int backTimes = 0;
	
	private static String mDownUrl;
	private static String mReleaseNote;
	
	private UpgradeDialog upgradeDialog;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				Bitmap bitmap = (Bitmap) msg.obj;
				if (mAutoImageView != null) {
					mAutoImageView.setImageBitmap(bitmap);
					Animation animation = AnimationUtils.loadAnimation(HubCategory.this, android.R.anim.fade_in);
					animation.setDuration(500);
					mAutoImageView.startAnimation(animation);
				}
				break;
			case 1:
				picPoll = new PicPoll();
				picPoll.start();
				break;
			case 2:
				showUpgradeDialog();
				break;
			default:
				break;
			}
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_category);
		findViews();
		initPopMenu();
		mImageLoader = new LazyImageLoader(this);
		mHandler.sendEmptyMessageDelayed(1, 2000);
		backTimes = 0;
		upgradeDialog = new UpgradeDialog(this);
		new UpdatingThread().start();
	}

	private void findViews() {
		newsImageView = (ImageView) findViewById(R.id.newsImage);
		newsImageView.setOnClickListener(this);
		laughImageView = (ImageView) findViewById(R.id.laughImage);
		laughImageView.setOnClickListener(this);
		beautyImageView = (ImageView) findViewById(R.id.beautyImage);
		beautyImageView.setOnClickListener(this);
		conImageView = (ImageView) findViewById(R.id.constellationImage);
		conImageView.setOnClickListener(this);
		recreationImageView = (ImageView) findViewById(R.id.recreationImage);
		recreationImageView.setOnClickListener(this);
		movieImageView = (ImageView) findViewById(R.id.movieImage);
		movieImageView.setOnClickListener(this);
		sportsImageView = (ImageView) findViewById(R.id.sportsImage);
		sportsImageView.setOnClickListener(this);
		techImageView = (ImageView) findViewById(R.id.techImage);
		techImageView.setOnClickListener(this);
		gameImageView = (ImageView) findViewById(R.id.gameImage);
		gameImageView.setOnClickListener(this);

		mFashionImageView = (ImageView) findViewById(R.id.fashionImage);
		mFashionImageView.setOnClickListener(this);
		mStreettakeImageView = (ImageView) findViewById(R.id.streettakeImage);
		mStreettakeImageView.setOnClickListener(this);
		mQuotationImage = (ImageView) findViewById(R.id.quotationImage);
		mQuotationImage.setOnClickListener(this);

		mPetImageView = (ImageView) findViewById(R.id.petImage);
		mPetImageView.setOnClickListener(this);
		mCarImageView = (ImageView) findViewById(R.id.carImage);
		mCarImageView.setOnClickListener(this);
		mEnglishImageView = (ImageView) findViewById(R.id.englishImage);
		mEnglishImageView.setOnClickListener(this);
		
		mTravelImageView = (ImageView) findViewById(R.id.travelImage);
		mTravelImageView.setOnClickListener(this);
		mBusinessImageView = (ImageView) findViewById(R.id.businessImage);
		mBusinessImageView.setOnClickListener(this);
		mOriginalityImageView = (ImageView) findViewById(R.id.originalityImage);
		mOriginalityImageView.setOnClickListener(this);

		mTitleTextView = (TextView) findViewById(R.id.titleTextView);
		mTitleTextView.setText(getResources().getTextArray(R.array.dataArray)[0]);
		
		mChangeDataResource = (ImageView) findViewById(R.id.changeDataResource);
		mChangeDataResource.setOnClickListener(this);
		
		LinearLayout categoryLayout=(LinearLayout)findViewById(R.id.rootLayout);
		categoryLayout.setClickable(true);
		categoryLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				closePopMenu();
			}
		});
		TableLayout tableLayout=(TableLayout)findViewById(R.id.tableLayout);
		tableLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				closePopMenu();
			}
		});
	}
	
	public void closePopMenu(){
		if (mPopupWindow != null) {
			mIsOpen = false;
			mPopupWindow.dismiss();
		}
	}
	
	public void initPopMenu(){
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		mPopView = inflater.inflate(R.layout.pop_menu, null);
		ListView listView = (ListView) mPopView.findViewById(R.id.popMenuListView);
		listView.setAdapter(new PopAdapter(this));
	}
	
	public void showUpgradeDialog() {
		upgradeDialog.show();
		upgradeDialog.reset(mDownUrl,mReleaseNote);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, MENU_ABOUT, 0, R.string.menu_about).setIcon(R.drawable.menu_about);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ABOUT:
			showAboutDialog();
			return true;
		}
		return false;
	}
	
	public void showAboutDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.app_name).setMessage(R.string.abount_info).setIcon(R.drawable.icon)
		.setPositiveButton(R.string.msg_add_group_confirm, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
			}
		}).setNegativeButton(R.string.msg_add_group_cancel,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	@Override
    public void onResume() {
    	super.onResume();  
    	fillAnimationData(mDataType,true);
    }
	
	public void fillAnimationData(int dataType,boolean isRefresh) {
		if (isRefresh) {
			newsImageView.setImageResource(R.drawable.category_news);
			laughImageView.setImageResource(R.drawable.category_joke);
			beautyImageView.setImageResource(R.drawable.category_mm);
			conImageView.setImageResource(R.drawable.category_constellation);
			recreationImageView.setImageResource(R.drawable.category_entertainment);
			movieImageView.setImageResource(R.drawable.category_movie);
			sportsImageView.setImageResource(R.drawable.category_sports);
			techImageView.setImageResource(R.drawable.category_technology);
			gameImageView.setImageResource(R.drawable.category_games);

			mFashionImageView.setImageResource(R.drawable.category_fashion);
			mStreettakeImageView.setImageResource(R.drawable.category_streettake);
			mQuotationImage.setImageResource(R.drawable.category_quotation);
			
			mPetImageView.setImageResource(R.drawable.category_pet);
			mCarImageView.setImageResource(R.drawable.category_car);
			mEnglishImageView.setImageResource(R.drawable.category_english);
			
			mTravelImageView.setImageResource(R.drawable.category_travel);
			mBusinessImageView.setImageResource(R.drawable.category_business);
			mOriginalityImageView.setImageResource(R.drawable.category_originality);
		}
		
		mAllMap.clear();
		List<String> urlList = new ArrayList<String>();
		
		CategoryCacheDb cacheDb = new CategoryCacheDb(this).open();
		// news
		int[] weiboType = { ItemType.Type.NEWS.getNumber(),
				ItemType.Type.FUNNY.getNumber(),
				ItemType.Type.BEAUTY.getNumber(),
				ItemType.Type.CONSTELLATION.getNumber(),
				ItemType.Type.RECREATION.getNumber(),
				ItemType.Type.MOVIE.getNumber(),
				ItemType.Type.SPORTS.getNumber(),
				ItemType.Type.TECHNOLOGY.getNumber(),
				ItemType.Type.GAME.getNumber(),
				ItemType.Type.FASHION.getNumber(),
				ItemType.Type.STREET.getNumber(),
				ItemType.Type.SAYING.getNumber(),
				ItemType.Type.PET.getNumber(), ItemType.Type.CAR.getNumber(),
				ItemType.Type.ENGLISH.getNumber(),
				ItemType.Type.TRAVEL.getNumber(),
				ItemType.Type.BUSINESS.getNumber(),
				ItemType.Type.CREATIVE.getNumber() };
		for (int i = 0; i < weiboType.length; i++) {
			Cursor cursor = cacheDb.getWeiboList(dataType, weiboType[i], "false");
			List<Bitmap> list = new ArrayList<Bitmap>();
			urlList.clear();
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				String url = cursor.getString(1);
				if (!urlList.contains(url)) {
					urlList.add(url);
					Bitmap bitmap = mImageLoader.getBitmapFromFile(1, url);
					if (bitmap != null) {
						list.add(bitmap);
					}
				}
			}
			mAllMap.add(list);
			cursor.close();
		}
		cacheDb.close();
	}

	private class PicPoll extends Thread {
		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			while (true) {
				changePic();
			}
		}
		
		private void changePic() {
			int randNum = getRandomNum();
			//Log.d("randNum", randNum + "");
			mAutoImageView = getImageView(randNum);
			if (randNum < mAllMap.size()) {
				List<Bitmap> list = mAllMap.get(randNum);
				if (mAutoImageView != null && list.size() > 1) {
					for (int i = 0; i < list.size(); i++) {
						Message message = new Message();
						message.what = 0;
						message.obj = list.get(i);
						mHandler.sendMessage(message);
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	
	public int getRandomNum() {
		Random random = new Random();
		return random.nextInt(17);
	}
	
	private ImageView getImageView(int num){
		if (num == 0) {
			return newsImageView;
		} else if (num == 1) {
			return laughImageView;
		} else if (num == 2) {
			return beautyImageView;
		} else if (num == 3) {
			return conImageView;
		} else if (num == 4) {
			return recreationImageView;
		} else if (num == 5) {
			return movieImageView;
		} else if (num == 6) {
			return sportsImageView;
		} else if (num == 7) {
			return techImageView;
		} else if (num == 8) {
			return gameImageView;
		}else if (num == 9) {
			return mFashionImageView;
		} else if (num == 10) {
			return mStreettakeImageView;
		} else if (num == 11) {
			return mQuotationImage;
		} else if (num == 12) {
			return mPetImageView;
		} else if (num == 13) {
			return mCarImageView;
		} else if (num == 14) {
			return mEnglishImageView;
		} else if (num == 15) {
			return mTravelImageView;
		} else if (num == 16) {
			return mBusinessImageView;
		}else if (num == 17) {
			return mOriginalityImageView;
		}
		return null;
	}
	

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mIsOpen) {
			mPopupWindow.dismiss();
			int y = mChangeDataResource.getTop()
					+ mChangeDataResource.getHeight()
					+ mChangeDataResource.getHeight();
			mIsOpen = false;
			toggle(getWindow().getWindowManager().getDefaultDisplay()
					.getWidth() - 162, y);
			
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.changeDataResource) {
			int y=mChangeDataResource.getTop()+mChangeDataResource.getHeight()+mChangeDataResource.getHeight();
			toggle(getWindow().getWindowManager().getDefaultDisplay().getWidth()-162, y);
			return;
		}
		if (mPopupWindow != null) {
			mIsOpen = false;
			mPopupWindow.dismiss();
		}
		Intent intent = new Intent(this, NewsListActivity.class);
		intent.putExtra("dataSource", getDataType(mTitleTextView.getText().toString()));
		DataService.pageTag = "";
		switch (view.getId()) {
		case R.id.newsImage:
			intent.putExtra("from", ItemType.Type.NEWS.getNumber());
			startActivity(intent);
			break;
		case R.id.laughImage:
			intent.putExtra("from", ItemType.Type.FUNNY.getNumber());
			startActivity(intent);
			break;
		case R.id.beautyImage:
			intent.putExtra("from", ItemType.Type.BEAUTY.getNumber());
			startActivity(intent);
			break;
		case R.id.constellationImage:
			intent.putExtra("from", ItemType.Type.CONSTELLATION.getNumber());
			startActivity(intent);
			break;
		case R.id.recreationImage:
			intent.putExtra("from", ItemType.Type.RECREATION.getNumber());
			startActivity(intent);
			break;
		case R.id.movieImage:
			intent.putExtra("from", ItemType.Type.MOVIE.getNumber());
			startActivity(intent);
			break;
		case R.id.sportsImage:
			intent.putExtra("from", ItemType.Type.SPORTS.getNumber());
			startActivity(intent);
			break;
		case R.id.techImage:
			intent.putExtra("from", ItemType.Type.TECHNOLOGY.getNumber());
			startActivity(intent);
			break;
		case R.id.gameImage:
			intent.putExtra("from", ItemType.Type.GAME.getNumber());
			startActivity(intent);
			break;
		case R.id.fashionImage:
			intent.putExtra("from", ItemType.Type.FASHION.getNumber());
			startActivity(intent);
			break;
		case R.id.streettakeImage:
			intent.putExtra("from", ItemType.Type.STREET.getNumber());
			startActivity(intent);
			break;
		case R.id.quotationImage:
			intent.putExtra("from", ItemType.Type.SAYING.getNumber());
			startActivity(intent);
			break;
		case R.id.petImage:
			intent.putExtra("from", ItemType.Type.PET.getNumber());
			startActivity(intent);
			break;
		case R.id.carImage:
			intent.putExtra("from", ItemType.Type.CAR.getNumber());
			startActivity(intent);
			break;
		case R.id.englishImage:
			intent.putExtra("from", ItemType.Type.ENGLISH.getNumber());
			startActivity(intent);
			break;
		case R.id.travelImage:
			intent.putExtra("from", ItemType.Type.TRAVEL.getNumber());
			startActivity(intent);
			break;
		case R.id.businessImage:
			intent.putExtra("from", ItemType.Type.BUSINESS.getNumber());
			startActivity(intent);
			break;
		case R.id.originalityImage:
			intent.putExtra("from", ItemType.Type.CREATIVE.getNumber());
			startActivity(intent);
			break;
		default:
			break;
		}
		
	}
	
	public void toggle(int x,int y) {
		mIsOpen = !mIsOpen;
		if (mIsOpen) {
			mPopupWindow = new PopupWindow(mPopView, 162,android.view.ViewGroup.LayoutParams.WRAP_CONTENT, false);
			mPopupWindow.showAtLocation(findViewById(R.id.rootLayout),Gravity.NO_GRAVITY, x, y);
		} else {
			mPopupWindow.dismiss();
		}
	}
	
	public class PopAdapter extends BaseAdapter{
		
		private Context context;
		private String[] data;

		public PopAdapter(Context context) {
			this.context = context;
			data = context.getResources().getStringArray(R.array.dataArray);
		}

		@Override
		public int getCount() {
			return data.length;
		}

		@Override
		public Object getItem(int position) {
			return data[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			TextView textView = new TextView(context);
			textView.setText(data[position]);
			textView.setClickable(true);
			textView.setTextSize(18);
			textView.setHeight(50);
			textView.setPadding(5, 5, 5, 5);
			textView.setBackgroundResource(R.drawable.pop_menu_btn);
			textView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
			textView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mIsOpen=false;
					mPopupWindow.dismiss();
					mTitleTextView.setText(getResources().getTextArray(R.array.dataArray)[position]);
					mAutoImageView = null;
					mDataType = position;
					fillAnimationData(position,true);
				}
			});
			return textView;
		}
		
	}
	
	public int getDataType(String currentStr) {
		int type = -1;
		CharSequence[] data = getResources().getTextArray(R.array.dataArray);
		for (int i = 0; i < data.length; i++) {
			if (currentStr.equals(data[i])) {
				type = i;
				break;
			}
		}
		return type;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			int y=mChangeDataResource.getTop()+mChangeDataResource.getHeight()+mChangeDataResource.getHeight();
			toggle(getWindow().getWindowManager().getDefaultDisplay().getWidth()-162, y);
			break;

		default:
			break;
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK  && event.getRepeatCount()==0) {
			if (backTimes < 1) {
				Toast.makeText(this, R.string.quit_app_tips, Toast.LENGTH_SHORT).show();
				backTimes = backTimes + 1;
			} else {
				finish();
			}
			return true;
		}
		return false;
	}
	
	class UpdatingThread extends Thread {
		public void run() {
			int versionCode = 0;
			PackageManager manager = getPackageManager();
			try {
				PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
				versionCode = info.versionCode;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}

			if (hasNewVersion(versionCode, HubCategory.this)) {
				mHandler.sendEmptyMessage(2);
			} 
		}
	}

	public static boolean hasNewVersion(int oldVersionCode, Context ctx) {
		boolean b = false;
		try {
			String str = HttpRequest.getData(ClientInfo.Upgrade.UPGRADE_URL);
			JSONObject jobj = new JSONObject(str);
			if (jobj != null) {
				int result = jobj.getInt("result");
				if (result == 0) {
					mReleaseNote = jobj.getString("releaseNote");
					mDownUrl = jobj.getString("upgradePath");
					int version = jobj.getInt("version");
					if (version > oldVersionCode) {
						b = true;
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

}
