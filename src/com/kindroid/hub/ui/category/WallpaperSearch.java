package com.kindroid.hub.ui.category;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.WallpaperSearckAdapter;
import com.kindroid.hub.ui.category.wallpaper.WallPaperService;
import com.kindroid.hub.ui.category.wallpaper.WallPaperDB.WallpaperBean;
/***
 * 壁纸搜索
 * @author huaiyu.zhao
 *
 */
public class WallpaperSearch extends Activity implements View.OnClickListener,OnScrollListener{



    protected LinkedList<String> mFilterSearchHistory;
    protected LinkedList<String> mSearchHistroy;

    protected String mLastSearch;
    protected String mPreferenceKey;  
    
    private ImageView mIcon_search; //搜索标题
    private TextView mTitleTextView;//搜索标题
    private ImageView mIcon_back; //返回按钮    
    private ImageView mSearchBtn;//搜索按钮    
    private ListView mLsitView; //显示搜索结果
    
    private  EditText search_input;//搜索内容
    
	private View mFootView;	
	private View mLoadingProgressBar;
	private TextView mLoadingText;
	private boolean mThreadRunning;
	//private static final int pageNum=5; //每页显示的记录数
	private int mLastVisibleItem;
    
    private WallpaperSearckAdapter mWpAdapter;//适配器
    private  List<WallpaperBean> mListWallpaper=new ArrayList<WallpaperBean>(); //缓存搜索的壁纸   
    
    private static final int mImageType=-1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.ring_search);
		initPage();
		
	
	}
	
	/***
	 * 初始化 页面
	 */
	private void initPage(){
		//更换 标题背景
		View layout=findViewById(R.id.preview_title_bar_id);
		layout.setBackgroundResource(R.drawable.wallpaper_title_bg);
		
		//搜索按钮
		mIcon_search = (ImageView) findViewById(R.id.icon_search);
		mIcon_search.setVisibility(View.GONE);
		//搜索标题
		mTitleTextView = (TextView) findViewById(R.id.frame_title);
		mTitleTextView.setText(R.string.msg_search_title);
		// 返回按钮
		mIcon_back=(ImageView)findViewById(R.id.icon_back);
		mIcon_back.setOnClickListener(this);

		//标题背景
		layout=findViewById(R.id.preview_title_bar_id);
		layout.setBackgroundResource(R.drawable.wallpaper_title_bg);		
		//搜索内容
		mTitleTextView = (TextView) findViewById(R.id.frame_title);
		mTitleTextView.setText(R.string.msg_search_title);
		
		//搜索按钮
		mSearchBtn = (ImageView) findViewById(R.id.search_action);
		mSearchBtn.setImageResource(R.drawable.wallpaper_search_icon);
		mSearchBtn.setOnClickListener(this);
		
		search_input=(EditText)findViewById(R.id.search_input);
		
		mLsitView=(ListView) findViewById(R.id.search_result_view);
		mLsitView.setOnScrollListener(this);
		
		setmLsitView();
		
	}
	private void setmLsitView(){
		mLsitView.removeFooterView(mFootView);
	    LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    mFootView = layoutInflater.inflate(R.layout.ringtone_listview_loading, mLsitView, false);
	    mLoadingProgressBar = (ProgressBar) mFootView.findViewById(R.id.preview_loading_progressbar);
        mLoadingText = (TextView) mFootView.findViewById(R.id.preview_loading_text);
        mFootView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                loadList();
            }
        });
        
        mLsitView.addFooterView(mFootView);
        mLsitView.setOnScrollListener(this);
	    mWpAdapter = new WallpaperSearckAdapter(this, mListWallpaper);
	    mLsitView.setAdapter(mWpAdapter);
	    mLsitView.setSelector(getResources().getDrawable(R.drawable.mytransparent));
	    
	    mFootView.setVisibility(View.GONE);
	}
	
	public static String inputName="";
	
	class LoadWallpaperListThread extends Thread{			
		public LoadWallpaperListThread(){
			
		}
		//进行数据查找
		@Override
		public void run() {		
			Looper.prepare();
			WallPaperService wallpaperService=new WallPaperService(WallpaperSearch.this);
			
			try {
				if(null!=inputName && !"".equals(inputName)){		

					List<WallpaperBean> list = wallpaperService.queryItem(WallPaperService.UPDATEDATA, mImageType, inputName);
					if(list==null){
						mHadler.sendEmptyMessage(2);
						
					}else if(list.isEmpty()){
						mHadler.sendEmptyMessage(3);
						
					}else{						
						mListWallpaper.clear();
						mListWallpaper.addAll(list);
						if(list!=null && list.size()==mListWallpaper.size()){
				
							mHadler.sendEmptyMessage(4);
						}else{
							
							mHadler.sendEmptyMessage(1);
						}
						
					}
					
				}else{
					mHadler.sendEmptyMessage(4);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private Handler mHadler=new Handler(){
		public void handleMessage(Message msg) {
			mThreadRunning = false;
			search_input.setText(inputName);
			switch (msg.what) {
			case 1:
				mFootView.setVisibility(View.VISIBLE);
				hideReviewLoadingDialog();				
				mWpAdapter.setData(mListWallpaper);
				
				break;
			case 2:
				noReviewNetworkDialog(1);
				break;
			case 3:
				noReviewNetworkDialog(2);
				break;
			case 4:
				mWpAdapter.setData(mListWallpaper);
				mFootView.setVisibility(View.GONE);
				hideReviewLoadingDialog();				
				break;
				
			default:
				break;
			}
		}
	};
	
	
    private void loadList() {
        if (!mThreadRunning) {
        	hideReviewLoadingDialog();
        	new LoadWallpaperListThread().start();	
            showReviewLoadingDialog();
        }
    }
	
    
    
	/**
	 * Show loading dialog
	 */
	private void showReviewLoadingDialog() {
	    mLoadingText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        mLoadingText.setText(R.string.msg_loading);
		mLoadingProgressBar.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Hide loading dialog
	 */
	private void hideReviewLoadingDialog() {
	    mLoadingText.setCompoundDrawablesWithIntrinsicBounds(
	            getResources().getDrawable(R.drawable.refresh), null, null, null);
        mLoadingText.setText(R.string.more);
		mLoadingProgressBar.setVisibility(View.GONE);
	}
	
	/**
	 * Hide loading dialog
	 */
	private void noReviewNetworkDialog(int type) {
	    mLoadingText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.refresh), null, null, null);
	    if(type==1){
	    	 mLoadingText.setText(R.string.msg_loading_network);
	    }else if(type==2){
	    	 mLoadingText.setText(R.string.msg_loading_not);
	    }else{
	    	 mLoadingText.setText(R.string.msg_loading_network);
	    }
       
		mLoadingProgressBar.setVisibility(View.GONE);
	}
    
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		boolean bl= super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK){
			Intent intent = new Intent();
			intent.setClass(this, WallpaperMain.class);
			startActivity(intent);
			finish();	
		}		
		return bl;
	}




	@Override
	public void onClick(View view) {
		Intent intent;
		Bundle bundle;
		if(view instanceof ImageView && view.getTag()!=null)
		{
			final int position= new Integer(view.getTag().toString());
			switch (view.getId()) {
			case R.id.wallpaper_share:
				// list  中的第几个arg0.getTag();
				System.out.println("=========分享");
				intent = new Intent(this, WallPaperOrringReply.class);
				intent.putExtra("listPosition", position);
				intent.putExtra("fromMode", "wallaper");
				intent.putExtra("type", "forward");
				intent.putExtra("wallpaperid", mListWallpaper.get(position).getImage_id());
				this.startActivity(intent);
				
				
				break;
			case R.id.wallpaper_comment:					
				System.out.println("=========评论");
				intent = new Intent(this, WallPaperOrringReply.class);
				intent.putExtra("listPosition", position);
				intent.putExtra("fromMode", "wallpaper");
				intent.putExtra("type", "comment");
				intent.putExtra("wallpaperid", mListWallpaper.get(position).getImage_id());					
				this.startActivity(intent);

				break;
			case R.id.wallpaper_enlarge:
				 System.out.println("========图片");
				 intent = new Intent();
				
				 intent.setClass(this, WallpaperContent.class);
				 bundle = new Bundle();
				 bundle.putInt("imageType", mImageType);
				 bundle.putInt("tag", new Integer(view.getTag().toString()));
				 intent.putExtras(bundle);
				 startActivity(intent);
				 //finish();
				 
			
			break;
		}
		}
		
		if(view instanceof ImageView){
			switch (view.getId()) {
			case R.id.search_action:
				mThreadRunning = true;
				String input=search_input.getText()+"";
				inputName=input;
				if(inputName!=input){					
					mListWallpaper.clear();
					
				}				
				//启动线程进行搜索
				new LoadWallpaperListThread().start();				
				break;
			case R.id.icon_back:
				intent = new Intent();
				intent.setClass(this, WallpaperMain.class);
				startActivity(intent);
				finish();	
			default:
				break;
			}
			
		}
		
		
	}

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

}

