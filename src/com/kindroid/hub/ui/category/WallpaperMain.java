package com.kindroid.hub.ui.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.MyWallPaperAdapter;
import com.kindroid.hub.adapter.WallPaperAdapter;
import com.kindroid.hub.ui.category.wallpaper.Constant;
import com.kindroid.hub.ui.category.wallpaper.WallPaperService;
import com.kindroid.hub.ui.category.wallpaper.WallPaperDB.WallpaperBean;

public class WallpaperMain extends Activity implements View.OnClickListener, OnScrollListener {

	// 壁纸首页标题
	private ImageView wallpaper_back;
	private ImageView wallpaper_search;
	private TextView wallpaper_title; // 子类标题
	private TextView subjectRecommand;
	private TextView newOrder;
	private TextView hotOrder;
	private TextView localOrder;
	private int screenWidth = 0;
	private int screenHeight = 0;
	
	private View mFootView;
	private boolean mThreadRunning;
	private int mLastVisibleItem;
	private View mLoadingProgressBar;
	private TextView mLoadingText;

	private ListView mSubListView;
	private WallPaperAdapter mWpAdapter ;
	private MyWallPaperAdapter mLocalWlAdapter;
	private List<WallpaperBean> listMap=new ArrayList<WallpaperBean>();
	private List<Map<String,Object>> mGridViewList=new ArrayList<Map<String,Object>>();
	private GridView mGridView;
	
	private static int mImageType=WallPaperService.SUBJECTRECOMMANDITEM;
	private List<String> mImagesName=new ArrayList<String>();
	
	/***
	 * 提示网络连接失败
	 */
	private static final int MSG_NETWORK=1;
	/***
	 * 提示加载结束
	 */
	private static final int MSG_END=2; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Display display = this.getWindow().getWindowManager().getDefaultDisplay();
		if (display != null) {
			screenWidth = display.getWidth();
			screenHeight = display.getHeight();
		}
		Bundle bundle=this.getIntent().getExtras();			
		if(bundle!=null){
			mImageType=bundle.getInt("imageType");
		}		
		setContentView(R.layout.hub_wallpaper_all);
		//初始化页面
		initPage();
		//提示正在加载
		showLoadingDialog();
		//进行数据加载
		new LoadWallpaperThread(mImageType, false).start();
		
	}
	/****
	 * 启动线程 加载数据
	 * @author huaiyu.zhao
	 *
	 */
	class LoadWallpaperThread extends Thread {
		private int imageType;
		private boolean isRefresh ;
		public LoadWallpaperThread(int imageType, boolean isRefresh) {
			this.imageType = imageType;
			this.isRefresh = isRefresh;
		}
		
		public void run() {
		    mThreadRunning = true;
			System.out.println("==========启动线程==========");
			WallPaperService wallpaperService=new WallPaperService(WallpaperMain.this);
			List<WallpaperBean> newMap=new ArrayList<WallpaperBean>();
			try {
				if(!isRefresh){
					if(imageType != WallPaperService.LOCALORDERITEM){
						newMap = wallpaperService.queryItem(WallPaperService.CREATEDB, imageType);
					}else{
						newMap = wallpaperService.queryItem(WallPaperService.LOCALORDERITEM, imageType);
					}
				}else{
					if(imageType != WallPaperService.LOCALORDERITEM){
						newMap = wallpaperService.queryItem(WallPaperService.UPDATEDATA, imageType);
					}
				}
				if(newMap==null || newMap.isEmpty() || newMap.size()==listMap.size()){
					myHandler.sendEmptyMessage(DATA_IS_LOADED);
				}else{
					listMap.clear();
					listMap.addAll(newMap);
					
					myHandler.sendEmptyMessage(INITITEM);					
				}
				
			} catch (Exception e) {
				System.out.println("=============连接失败");		
				myHandler.sendEmptyMessage(NO_NETWORK);
				//显示 联网失败信息
				e.printStackTrace();
			}
			
		}
	}
	
	
	class  LoadMyWallpaperThread extends Thread {
		
		private boolean mIsImage=false;
		public LoadMyWallpaperThread(boolean isImage) {
			mIsImage=isImage;
		}
		public void run() {
			if(!mIsImage){
				mImagesName=com.kindroid.hub.ui.category.wallpaper.Constant.findWallpapers(WallpaperMain.this);
				if(mImagesName!=null && !mImagesName.isEmpty()){
					mGridViewList=new ArrayList<Map<String,Object>>();
					for(String pathUrl:mImagesName){
						Map<String,Object> map=new HashMap<String, Object>();
						map.put("bitmap", pathUrl);
						String name=pathUrl.substring(pathUrl.lastIndexOf("/")+1).replaceAll(".jpg", "");
						name=name.length()>6?name.substring(0,6)+"...":name;
						map.put("name",name);					
						map.put("del", BitmapFactory.decodeResource(WallpaperMain.this.getResources(), R.drawable.wallpaper_del));
						mGridViewList.add(map);
					}		
				}
				myHandler.sendEmptyMessage(LOCAL_URL);				
			}else{
				Object url;
				try {
					for(int i=0;i<mGridViewList.size();i++){
						Map<String,Object> map=mGridViewList.get(i);
						url=map.get("bitmap");
						if(url instanceof String){
							map.put("bitmap", Constant.getThumbnailBitMap((String)url));
							mGridViewList.remove(i);
							mGridViewList.add(i, map);
							myHandler.sendEmptyMessage(LOCAL_IMAGE);
						}else{
							continue;
						}					
					}
				} catch (Exception e) {
					myHandler.sendEmptyMessage(LOCAL_IMAGE);
				}
		
				
				
			}
		}
	}
	
	
	/**
	 * Show loading dialog
	 */
	private void showLoadingDialog() {
	    mLoadingText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        mLoadingText.setText(R.string.msg_loading);
		mLoadingProgressBar.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Hide loading dialog
	 */
	private void hideLoadingDialog() {
	    mLoadingText.setCompoundDrawablesWithIntrinsicBounds(
	            getResources().getDrawable(R.drawable.refresh), null, null, null);
        mLoadingText.setText(R.string.more);
		mLoadingProgressBar.setVisibility(View.GONE);
	}
	
	/**
	 * Hide loading dialog
	 */
	private void noNetworkDialog(int type) {
	    mLoadingText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.refresh), null, null, null);
	    if(type==MSG_NETWORK){
	    	 mLoadingText.setText(R.string.msg_loading_network);
	    }else if(type==MSG_END){
	    	 mLoadingText.setText(R.string.msg_loading_not);
	    }else{
	    	 mLoadingText.setText(R.string.msg_loading_network);
	    }
       
		mLoadingProgressBar.setVisibility(View.GONE);
	}
	
	private void initPage(){
		// 返回
		wallpaper_back = (ImageView) findViewById(R.id.wallpaper_back);
		wallpaper_back.setOnClickListener(this);

		// 我的壁纸
		wallpaper_search = (ImageView) findViewById(R.id.wallpaper_search);
		wallpaper_search.setOnClickListener(this);

		// 标题
		wallpaper_title = (TextView) findViewById(R.id.wallpaper_title);
		wallpaper_title.setText(R.string.wallpaper_title_name);

		// 标题背景
		View layout = findViewById(R.id.hub_wallpaper_title_bar_id);
		layout.setBackgroundResource(R.drawable.wallpaper_title_bg);

		layout = findViewById(R.id.wallpaper_order_bar_id);
		layout.setBackgroundResource(R.drawable.tab_bg01);

		// 推荐
		subjectRecommand = (TextView) findViewById(R.id.recommand_order);
		subjectRecommand.setText(R.string.wallpaper_subject_recommand_name);
		subjectRecommand.setTextColor(R.color.black);
		subjectRecommand.setOnClickListener(this);

		newOrder = (TextView) findViewById(R.id.new_order);
		newOrder.setText(R.string.wallpaper_new_order_name);
		newOrder.setTextColor(R.color.black);
		newOrder.setOnClickListener(this);

		hotOrder = (TextView) findViewById(R.id.hot_order);
		hotOrder.setText(R.string.wallpaper_hot_order_name);
		hotOrder.setTextColor(R.color.black);
		hotOrder.setOnClickListener(this);

		localOrder = (TextView) findViewById(R.id.local_order);
		localOrder.setText(R.string.wallpaper_local_order_name);
		localOrder.setTextColor(R.color.black);
		localOrder.setOnClickListener(this);

		mSubListView = (ListView) findViewById(R.id.order_preview);
		mSubListView.removeFooterView(mFootView);
	    LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    mFootView = layoutInflater.inflate(R.layout.ringtone_listview_loading, mSubListView, false);
	    mLoadingProgressBar = (ProgressBar) mFootView.findViewById(R.id.preview_loading_progressbar);
        mLoadingText = (TextView) mFootView.findViewById(R.id.preview_loading_text);
        mFootView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                loadList();
            }
        });
        
	    mSubListView.addFooterView(mFootView);
	    mSubListView.setOnScrollListener(this);
	    mWpAdapter = new WallPaperAdapter(this, listMap);
	    mSubListView.setAdapter(mWpAdapter);
	    mSubListView.setSelector(getResources().getDrawable(R.drawable.mytransparent));
	    
	    
		mGridView = (GridView) findViewById(R.id.gridview);		
	    mLocalWlAdapter=new MyWallPaperAdapter(this, mGridViewList);
	    mGridView.setAdapter(mLocalWlAdapter);
	    mGridView.setSelector(getResources().getDrawable(R.drawable.mytransparent));
	    mGridView.setVisibility(View.GONE);

	 // 初始化显示标题 背景
	    if(mImageType==WallPaperService.SUBJECTRECOMMANDITEM){
	    	setTitleBackground(R.id.subject_recommand);
	    }else if(mImageType==WallPaperService.HOTORDERITEM){
	    	setTitleBackground(R.id.hot_order);
	    }else{
	    	setTitleBackground(R.id.hot_order);
	    }
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		final Display display = this.getWindow().getWindowManager()
				.getDefaultDisplay();
		if (display != null) {
			screenWidth = display.getWidth();
			screenHeight = display.getHeight();
		}
		setTitleBackground(titleBackgroundId);
	}

	
	@Override
	public void onClick(View v) {

		if (v != null) {
			Intent intent;
			Bundle bundle;
			//判断 是否为 GridView 中单击事件
			if(v instanceof ImageView && v.getTag()!=null)
			{
				final int position= new Integer(v.getTag().toString());
				switch (v.getId()) {
				case R.id.wallpaper_share:
					// list  中的第几个arg0.getTag();
					System.out.println("=========分享");
					intent = new Intent(this, WallPaperOrringReply.class);
					intent.putExtra(WallPaperOrringReply.EXTRA_POSITION, position);
					intent.putExtra(WallPaperOrringReply.EXTRA_FROM_MODE, "wallaper");
					intent.putExtra(WallPaperOrringReply.EXTRA_TYPE, "forward");
					intent.putExtra(WallPaperOrringReply.EXTRA_ID, listMap.get(position).getImage_id());
					this.startActivity(intent);
					
					
					break;
				case R.id.wallpaper_comment:					
					System.out.println("=========评论");
					intent = new Intent(this, WallPaperOrringReply.class);
					intent.putExtra(WallPaperOrringReply.EXTRA_POSITION, position);
					intent.putExtra(WallPaperOrringReply.EXTRA_FROM_MODE, "wallpaper");
					intent.putExtra(WallPaperOrringReply.EXTRA_TYPE, "comment");
					intent.putExtra(WallPaperOrringReply.EXTRA_ID, listMap.get(position).getImage_id());					
					this.startActivity(intent);

					break;
				case R.id.wallpaper_enlarge:
					 System.out.println("========图片");
					 intent = new Intent();
					
					 intent.setClass(this, WallpaperContent.class);
					 bundle = new Bundle();
					 bundle.putInt("imageType", mImageType);
					 bundle.putInt("tag", new Integer(v.getTag().toString()));
					 intent.putExtras(bundle);
					 startActivity(intent);
					 finish();
				break;
				
				case R.id.wallpaper_del:
					int tag=new Integer(v.getTag().toString());	
					String path= mImagesName.get(tag);
					com.kindroid.hub.ui.category.wallpaper.Constant.removeWallpaper(this,path);
					mGridViewList.remove(tag);
					mImagesName.remove(path);
					myHandler.sendEmptyMessage(LOCAL_URL);					
				break;	
				case R.id.wallpaper_artwork:
					Uri uri = Uri.parse("file://" + mImagesName.get(new Integer(v.getTag().toString())));
					intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setAction(Intent.ACTION_VIEW);
					String type = "image/x-cals";
					intent.setDataAndType(uri, type);
					startActivity(intent);

				break;
				


				}
			}
			
			if (v instanceof TextView || v instanceof ImageView) {
				switch (v.getId()) {
				case R.id.wallpaper_back:
					// 返回
					finish();
					break;
				case R.id.wallpaper_search:
					
					// 壁纸搜索
					intent = new Intent();
					intent.setClass(this, WallpaperSearch.class);
					startActivity(intent);
					//LoadWallpaperListThread().
					finish();
					break;

				case R.id.recommand_order:
					// 推荐
					listMap.clear();
					mSubListView.setVisibility(View.VISIBLE);
					mGridView.setVisibility(View.GONE);
					mWpAdapter.setData(listMap);
					mWpAdapter.notifyDataSetChanged();
					//提示正在加载
					showLoadingDialog();
					mImageType=WallPaperService.SUBJECTRECOMMANDITEM;
					//进行数据加载
					new LoadWallpaperThread(WallPaperService.SUBJECTRECOMMANDITEM,false).start();
					setTitleBackground(R.id.recommand_order);

					break;

				case R.id.new_order:
					listMap.clear();
					mSubListView.setVisibility(View.VISIBLE);
					mGridView.setVisibility(View.GONE);
					mWpAdapter.setData(listMap);
					mWpAdapter.notifyDataSetChanged();
					//提示正在加载
					showLoadingDialog();
					mImageType=WallPaperService.NEWORDERITEM;
					//进行数据加载
					new LoadWallpaperThread(WallPaperService.NEWORDERITEM,false).start();
					// 最新
					setTitleBackground(R.id.new_order);

					break;

				case R.id.hot_order:
					listMap.clear();
					mSubListView.setVisibility(View.VISIBLE);
					mGridView.setVisibility(View.GONE);
					mWpAdapter.setData(listMap);
					mWpAdapter.notifyDataSetChanged();
					//提示正在加载
					showLoadingDialog();
					mImageType=WallPaperService.HOTORDERITEM;
					//进行数据加载
					new LoadWallpaperThread(WallPaperService.HOTORDERITEM,false).start();
					// 最热
					setTitleBackground(R.id.hot_order);
					break;

				case R.id.local_order:
					
					listMap.clear();
					mGridViewList.clear();
					mLocalWlAdapter.setData(mGridViewList);
					mGridView.setVisibility(View.VISIBLE);
					mSubListView.setVisibility(View.GONE);
					mImagesName=new ArrayList<String>();
					//进行数据加载					
					new LoadMyWallpaperThread(false).start();					
					setTitleBackground(R.id.local_order);
					
					break;
				default:
					break;
				}
				
			
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		return super.onKeyDown(keyCode, event);
	}

	private int titleBackgroundId = 0;

	// 切换图片 背景
	private void setTitleBackground(int id) {
		titleBackgroundId = id;
		// oolean bl=false;
		int tab_id = R.drawable.wallpaper_tab_button_row;
		if (screenHeight > screenWidth) {
			tab_id = R.drawable.wallpaper_tab_button_vertical;
		}
		switch (id) {		
		case R.id.recommand_order:
			subjectRecommand.setBackgroundResource(tab_id);
			newOrder.setBackgroundDrawable(null);
			hotOrder.setBackgroundDrawable(null);
			localOrder.setBackgroundDrawable(null);
			break;
		case R.id.new_order:
			subjectRecommand.setBackgroundDrawable(null);
			newOrder.setBackgroundResource(tab_id);
			hotOrder.setBackgroundDrawable(null);
			localOrder.setBackgroundDrawable(null);
			break;
		case R.id.hot_order:
			subjectRecommand.setBackgroundDrawable(null);
			newOrder.setBackgroundDrawable(null);
			hotOrder.setBackgroundResource(tab_id);
			localOrder.setBackgroundDrawable(null);
			break;
		case R.id.local_order:
			subjectRecommand.setBackgroundDrawable(null);
			newOrder.setBackgroundDrawable(null);
			hotOrder.setBackgroundDrawable(null);
			localOrder.setBackgroundResource(tab_id);
			break;
		default:
			subjectRecommand.setBackgroundResource(tab_id);
			newOrder.setBackgroundDrawable(null);
			hotOrder.setBackgroundDrawable(null);
			localOrder.setBackgroundDrawable(null);
			break;
		}
	}
	private static final int INITITEM = 2;
	private static final int UPDATEITEM = 1;
	private static final int NO_NETWORK = 3; //网络连接失败
	private static final int DATA_IS_LOADED = 4; //网络连接失败
	private static final int LOCAL_URL = 5;
	private static final int LOCAL_IMAGE = 6;
	
	
	public Handler myHandler=new Handler(){
		
		public void handleMessage(android.os.Message msg) {
		    mThreadRunning = false;
		    switch (msg.what) {
			case  1:
				mWpAdapter.notifyDataSetChanged();
				break;
			case 2:
				mWpAdapter.setData(listMap);
				mWpAdapter.notifyDataSetChanged();
				hideLoadingDialog();
				break;
			case  NO_NETWORK:
				mLocalWlAdapter.setData(mGridViewList);
				mLocalWlAdapter.notifyDataSetChanged();
				noNetworkDialog(MSG_NETWORK);
				break;
			case  DATA_IS_LOADED:
				mLocalWlAdapter.setData(mGridViewList);
				mLocalWlAdapter.notifyDataSetChanged();
				noNetworkDialog(MSG_END);
				break;
			case  LOCAL_URL:
				mLocalWlAdapter.setData(mGridViewList);
				mLocalWlAdapter.notifyDataSetChanged();
				new LoadMyWallpaperThread(true).start();
				break;	
			case  LOCAL_IMAGE:
				mLocalWlAdapter.setData(mGridViewList);
				mLocalWlAdapter.notifyDataSetChanged();
				
				break;
			default:
				break;
			}
			
		};
	};

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        mLastVisibleItem = firstVisibleItem + visibleItemCount - 1;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mWpAdapter != null && !mThreadRunning && mLastVisibleItem == mWpAdapter.getCount()
                && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            loadList();
        }
    }
    
    private void loadList() {
        if (!mThreadRunning) {
            new LoadWallpaperThread(mImageType, true).start();
            showLoadingDialog();
        }
    }
}
