package com.kindroid.hub.ui.category;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.WallpaperOrRingAdapter;
import com.kindroid.hub.data.WallPaperData;
import com.kindroid.hub.proto.WeiboContentProtoc.Review;
import com.kindroid.hub.ui.category.wallpaper.ImageDownloader;
import com.kindroid.hub.ui.category.wallpaper.WallPaperReviewBean;
import com.kindroid.hub.ui.category.wallpaper.WallPaperService;
import com.kindroid.hub.ui.category.wallpaper.WallPaperDB.WallpaperBean;
import com.kindroid.hub.utils.Constant;

public class WallpaperContent extends Activity implements View.OnClickListener,OnScrollListener
{

	private ImageView mOwnerImage; //作者头像
	private TextView mOwnerName; //作者名称
	private TextView mOwnerTime; //上传时间
	private ImageView mComment; //评论
	private ImageView mShare; //分享
	private TextView mWallpaperName; //图片名称
	private ImageView mWallpaperImage; //图片
	private TextView mImageSize;//图片大小
	private TextView mDownloadSize;//图片下载次数
	private Button mButton; //下载按钮
	private TextView mCommentSize;//评论次数
	private TextView mShareSize;//分享次数	
	

	
	
	private int mImageType;
	private int mTag;
	private long imageId;
	private WallpaperBean bean;
	
	private TextView button_next; //下一张
	private TextView button_before;//上一张
	
	private String mFileName; //保存文件的名称 
	private ProgressDialog myDialog = null;
	private String message;
	private boolean isWallpaperPresence=false; //判断壁纸是否存在，如果存在 显示为应用 直接设置为壁纸
	private ProgressDialog dlgLoading = null;
	private int screenWidth = 0;
	private int screenHeight = 0;
	private Bitmap mBitmap;
	private Bitmap mOldBitmap;
	private List<WallpaperBean> listMap;
	
	private View detail_previous_button; //上一页
	private View detail_next_button; //下一页
	
	/***
	 * 首张壁纸
	 */
	private static final int WALLPAPER_ONE=1; 
	/***
	 * 上翻页
	 */
	private static final int WALLPAPER_PREVIOUS=2; 
	/***
	 * 下翻页
	 */
	private static final int WALLPAPER_NEXT=3; 
	/***
	 * 网络连接失败
	 */
	private static final int WALLPAPER_NETWORK=4;
	/***
	 * 最后一张壁纸
	 */
	private static final int WALLPAPER_END=5;
	/***
	 * //加载原图
	 */
	private static final int WALLPAPER_ARTWORK=6; 
	/***
	 * 评论加载完成
	 */
	private static final int WALLPAPER_REVIEW=7; 
	/***
	 * 评论加载失败
	 */
	private static final int WALLPAPER_REVIEW_FAILURE=8;
	/***
	 * 所有评论都加载完成
	 */
	private static final int WALLPAPER_REVIEW_END=9; 
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
		mListReview=new ArrayList<Review>();
		
		
		setContentView(R.layout.wallpaper_content);
		//接受传入数据
		Bundle bundle=this.getIntent().getExtras();		
		mImageType=bundle.getInt("imageType");
		mTag=bundle.getInt("tag");
		listMap=WallPaperService.MyWallpaperRes.getResources(mImageType);		
		bean=listMap.get(mTag);	
		
		//图片Id
		//imageId=new Long((String)map.get("wallpaper_imageid"));
		//初始化 标题
		initPage();	
		//赋值显示j
		setViewData(bean);
		
		
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
		mBitmap=getBitMap(mOldBitmap);
		mWallpaperImage.setImageBitmap(mBitmap);
	}
	private Bitmap getBitMap(Bitmap bitmap){
		if(bitmap==null){
			return BitmapFactory.decodeResource(getResources(), R.drawable.pic_default);
		}
		
		int width=bitmap.getWidth();
		int height=bitmap.getHeight();
		float num=(float)width/height;
		
		float newWidth=width>screenWidth?screenWidth:width;
		float newHeight=(newWidth/num);	
		float scaleWidth = ((float) newWidth) / width;
	    float scaleHeight= (((float) newHeight) / height);
	    
	    // 取得想要缩放的matrix参数
	    Matrix matrix = new Matrix();
	    matrix.postScale(scaleWidth, scaleHeight);
	    // 得到新的图片
	    Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,true);
		return newbm;
	}
	

	
	private void initPage(){
		//壁纸首页标题
		TextView wallpaper_title;//图片标题
		ImageView wallpaper_back;
		ImageView wallpaper_search;		
		// 返回
		wallpaper_back = (ImageView) findViewById(R.id.wallpaper_back);
		wallpaper_back.setOnClickListener(this);

		// 我的壁纸
		wallpaper_search = (ImageView) findViewById(R.id.wallpaper_search);
		wallpaper_search.setVisibility(View.GONE);

		// 标题
		wallpaper_title = (TextView) findViewById(R.id.wallpaper_title);
		wallpaper_title.setText(R.string.wallpaper_title_name);
		// 标题背景
		View layout = findViewById(R.id.hub_wallpaper_title_bar_id);
		layout.setBackgroundResource(R.drawable.wallpaper_title_bg);
		
		layout=findViewById(R.id.detail_bottom_panel);
		layout.setBackgroundResource(R.drawable.wallpaper_title_bg);
		
		button_before=(TextView)findViewById(R.id.detail_previous_button_text);
		button_before.setText(R.string.msg_wall_button_before);
		
		button_next=(TextView)findViewById(R.id.detail_next_button_text);
		button_next.setText(R.string.msg_wall_button_next);

		detail_previous_button=findViewById(R.id.detail_previous_button);
		detail_previous_button.setBackgroundResource(R.drawable.wallpaper_button);
		detail_previous_button.setOnClickListener(this);
		
		detail_next_button=findViewById(R.id.detail_next_button);
		detail_next_button.setBackgroundResource(R.drawable.wallpaper_button);
		detail_next_button.setOnClickListener(this);		
		
		mOwnerImage=(ImageView)findViewById(R.id.wallpaper_image);		
		mOwnerName=(TextView)findViewById(R.id.wallpaper_share_person);
		mOwnerTime=(TextView)findViewById(R.id.wallpaper_share_time);
		
		mComment=(ImageView)findViewById(R.id.wallpaper_comment);
		mComment.setOnClickListener(this);
		
		mShare=(ImageView)findViewById(R.id.wallpaper_share);
		mShare.setOnClickListener(this);
		
		mWallpaperName=(TextView)findViewById(R.id.wallpaper_title_name);
		mWallpaperImage=(ImageView)findViewById(R.id.wallpaper_enlarge);
		
		mDownloadSize=(TextView)findViewById(R.id.wallpaper_download_size);
		mImageSize=(TextView)findViewById(R.id.wallpaper_size);
		
		//保存按钮
		mButton=(Button)findViewById(R.id.wall_download_button);
		mButton.setText(R.string.msg_wall_button);
		mButton.setOnClickListener(this);		
		mCommentSize=(TextView)findViewById(R.id.wallpaper_comment_size);		
		mShareSize=(TextView)findViewById(R.id.wallpaper_share_size);	
		
		//评论
	
		
		
		
	}
  	/***
  	 * 重新设置数据 
  	 * @param bean
  	 */
	private void setViewData(WallpaperBean bean){
		
		
//		mOwnerImage.setImageResource(R.drawable.test_pic_touxiang); //作者头像
		mOwnerImage.setImageBitmap(bean.getUser_imgBitMap());//作者头像
		mOwnerName.setText(bean.getUser_name()); //作者名称
		mOwnerTime.setText(bean.getWallpaper_time()); //上传时间
		mWallpaperName.setText(bean.getTitle());//图片名称
		download_nums=bean.getDownload_nums();
		mDownloadSize.setText(getResources().getString(R.string.msg_download_count,download_nums));//图片下载次数
		//评论和转载次数
		mShareSize.setText(getResources().getString(R.string.msg_forwards_count, forwardedCount)); 
		mCommentSize.setText(getResources().getString(R.string.msg_comments_count, reviewedCount));
		
		//图片大小
		mImageSize.setText(getResources().getString(R.string.msg_file_size, bean.getImage_size())+"KB"); 
		
		//加载大图  评论和 评论次数等等信息
		//首先判断该图片是否存在如果存在  读取SD 卡上的图片
		mOldBitmap=BitmapFactory.decodeFile(Constant.WALLPAPERDOWNLOADPATH+bean.getTitle()+".jpg");
		if(mOldBitmap!=null){
			isWallpaperPresence=true;
			mButton.setText(R.string.wallpaper_msg_application);
		}else{
			//启动线程 加载图片
			new LoadWallpaperThread(bean.getArtworkUrl()).start();
			//显示默认图片
			mOldBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.pic_default);
			mButton.setOnClickListener(null);
		}
		final Display display = this.getWindow().getWindowManager().getDefaultDisplay();
		if (display != null) {
			screenWidth = display.getWidth();
			screenHeight = display.getHeight();
		}
		
		
		mCommentView=(ListView)findViewById(R.id.order_preview);
		mCommentView = (ListView) findViewById(R.id.order_preview);
		mCommentView.removeFooterView(mFootView);
	    LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    mFootView = layoutInflater.inflate(R.layout.ringtone_listview_loading, mCommentView, false);
	    mLoadingProgressBar = (ProgressBar) mFootView.findViewById(R.id.preview_loading_progressbar);
        mLoadingText = (TextView) mFootView.findViewById(R.id.preview_loading_text);
        mFootView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                loadList();
            }
        });
        
        mCommentView.addFooterView(mFootView);
        mCommentView.setOnScrollListener(this);
	    mWpAdapter = new WallpaperOrRingAdapter(this, mListReview,bean.getImage_id(), true);
	    mCommentView.setAdapter(mWpAdapter);
	    mCommentView.setSelector(getResources().getDrawable(R.drawable.mytransparent));
	    
	    commentsListLayout = (LinearLayout) findViewById(R.id.commentsListLayout);
		
		
		mBitmap=getBitMap(mOldBitmap);		
		mWallpaperImage.setImageBitmap(mBitmap);
		new LoadReviewTherad(bean.getImage_id()).start(); //加载评论
	}
	
	@Override
	public void onClick(View view) {	
		
		Intent intent;
		Bundle bundle;
		if(view instanceof Button){
			if(view.getId()==R.id.wall_download_button){				
				if(!isWallpaperPresence){
					CharSequence name=getText(R.string.wallpaper_msg_save_name);
					CharSequence prompt=getText(R.string.wallpaper_msg_save_prompt);
					myDialog =ProgressDialog.show(WallpaperContent.this, name, prompt, true);				
					mFileName=mWallpaperName.getText().toString()+".jpg";	
					download_nums++;
					mDownloadSize.setText(getResources().getString(R.string.msg_download_count,download_nums));//图片下载次数
					new Thread(saveFileRunnable).start();
				}else{
					CharSequence name=getText(R.string.wallpaper_msg_setwallpaper_name);
					CharSequence prompt=getText(R.string.wallpaper_msg_setwallpaper_prompt);
					myDialog =ProgressDialog.show(WallpaperContent.this, name, prompt, true);
					new Thread(setWallpaper).start();
				}
		
			}
		}
		
		if(view instanceof ImageView )
		{
			final int position= -1;
			switch (view.getId()) {
			case R.id.wallpaper_share:				
				System.out.println("=========分享");
				intent = new Intent(this, WallPaperOrringReply.class);
				intent.putExtra(WallPaperOrringReply.EXTRA_POSITION, position);
				intent.putExtra(WallPaperOrringReply.EXTRA_FROM_MODE, "wallaper");
				intent.putExtra(WallPaperOrringReply.EXTRA_TYPE, "forward");
				intent.putExtra(WallPaperOrringReply.EXTRA_ID, bean.getImage_id());
				this.startActivity(intent);
				forwardedCount++;
				mShareSize.setText(getResources().getString(R.string.msg_forwards_count, forwardedCount));
				break;
			case R.id.wallpaper_comment:					
				System.out.println("=========评论");
				intent = new Intent(this, WallPaperOrringReply.class);
				intent.putExtra(WallPaperOrringReply.EXTRA_POSITION, position);
				intent.putExtra(WallPaperOrringReply.EXTRA_FROM_MODE, "wallpaper");
				intent.putExtra(WallPaperOrringReply.EXTRA_TYPE, "comment");
				intent.putExtra(WallPaperOrringReply.EXTRA_ID, bean.getImage_id());					
				this.startActivity(intent);
				reviewedCount++;
				mCommentSize.setText(getResources().getString(R.string.msg_comments_count, reviewedCount));
				break;
				
			case R.id.wallpaper_back:
				if(mImageType!=-1){
					 intent = new Intent();				
					 intent.setClass(this, WallpaperMain.class);
					 bundle = new Bundle();
					 bundle.putInt("imageType", mImageType);			
					 intent.putExtras(bundle);
					 startActivity(intent);
				}
				 finish();					
				break;
			}
		}
		//分页
		if(view instanceof View){
			switch (view.getId()) {
			case R.id.detail_previous_button: //上一张
				if(mTag<=1){
								
					//提示已经到了第一张
					Toast.makeText(WallpaperContent.this, getResources().getText(R.string.wallpaper_msg_one), Toast.LENGTH_SHORT).show();
					
				}else{
					mListReview.clear();
					mListReview=new ArrayList<Review>();
					//加载数据
					mTag--;
					showLoadingDialog();					
					new LoadWallpaperListThread(false).start();
					detail_next_button.setOnClickListener(this);
					
				}				
				break;
			case R.id.detail_next_button:
				mListReview.clear();
				mListReview=new ArrayList<Review>();
				mTag++;
				showLoadingDialog();
				new LoadWallpaperListThread(true).start();
				detail_previous_button.setOnClickListener(this);
				break;
			default:
				break;
			}
			
		}
	}
	
	//保存
    private Runnable saveFileRunnable = new Runnable(){
        @Override
        public void run() {
        	//判断SD 卡是否存在
        	if(!Environment.getExternalStorageState().equals("mounted")){
        		message = getText(R.string.wallpaper_msg_issd).toString();   
    		}else{
                try {
                    saveFile(mOldBitmap, mFileName);                  
                    message = getText(R.string.wallpaper_msg_save_yes).toString();
                } catch (IOException e) {
                    message = getText(R.string.wallpaper_msg_save_no).toString();
                    //e.printStackTrace();
                }
    		}    
            messageHandler.sendMessage(messageHandler.obtainMessage());
        }            
    };
    //设置壁纸为当前壁纸
    private Runnable setWallpaper = new Runnable(){
        @Override
        public void run() {
        	//判断SD 卡是否存在
        	try {
				selectWallpaper(mBitmap);
				message =getText(R.string.wallpaper_msg_setwallpaper_yes).toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				message = getText(R.string.wallpaper_msg_setwallpaper_no).toString();
			}			
            messageHandler.sendMessage(messageHandler.obtainMessage());
        }            
    };
    
    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            myDialog.dismiss();
            mIsWallpaperSet=false;
            isWallpaperPresence=true;
			mButton.setText(R.string.wallpaper_msg_application);
            Toast.makeText(WallpaperContent.this, message, Toast.LENGTH_SHORT).show();
        }
    };
	
    
    /**
     * 保存文件
     * @param bm
     * @param fileName
     * @throws IOException
     */
    public void saveFile(Bitmap bm, String fileName) throws IOException {
        File dirFile = new File(Constant.WALLPAPERDOWNLOADPATH);       
        if(!dirFile.exists()){
           dirFile.mkdirs();           
        }
        File myCaptureFile = new File(Constant.WALLPAPERDOWNLOADPATH + fileName); 
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);       
        bos.flush();
        bos.close();
        
        ContentValues values = new ContentValues(7);       
        values.put(Images.Media.DISPLAY_NAME, fileName);         
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put(Images.Media.DATA, Constant.WALLPAPERDOWNLOADPATH + fileName);
        
        com.kindroid.hub.ui.category.wallpaper.Constant.insertWallpaper(this,values);

    }
    private boolean mIsWallpaperSet=false;
    //设置壁纸
	private void selectWallpaper(Bitmap bmap) throws IOException{
		if (mIsWallpaperSet){
			return;
		}		
		mIsWallpaperSet = true;
		this.setWallpaper(bmap);
		//同步其它应用
		setResult(RESULT_OK);
		//finish();
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean bl=super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK){
			if(mImageType!=-1){
				 Intent intent = new Intent();				
				 intent.setClass(this, WallpaperMain.class);
				 Bundle bundle = new Bundle();
				 bundle.putInt("imageType", mImageType);			
				 intent.putExtras(bundle);
				 startActivity(intent);
			}
			finish();			
		}
		return bl;		
		
	}
	//-----------
	private final ImageDownloader downloader=new ImageDownloader();
	/***
	 * 加载大图片
	 * @author huaiyu.zhao
	 *
	 */
	class LoadWallpaperThread extends Thread {
		private String mUrl;
		
		public LoadWallpaperThread(String url) {
			mUrl=url;
		}		
		public void run() {
		   Looper.prepare();
		   mOldBitmap=downloader.downloadBitmap(mUrl);
		   mHadler.sendEmptyMessage(WALLPAPER_ARTWORK);
		   //downloader.download(mUrl, mWallpaperImage);
			
		}
	}
	/***
	 * 评论加载
	 * @author huaiyu.zhao
	 *
	 */
	 class LoadReviewTherad extends Thread{
		private long mImageid;
		public boolean bl=false;
		public LoadReviewTherad(long imageid){
			mImageid=imageid;			
		}
		@Override
		public void run() {
			//进行数据加载
			mThreadRunning = true;
			int page=0;
			if(mListReview==null || mListReview.isEmpty()){
				mListReview=new ArrayList<Review>();
				page=0;
			}else{
				page=mListReview.size()/pageNum;
				if(page==0){
					bl=true;
				}else{
					bl=false;
				}
			}
			try {
				WallPaperReviewBean reviewBean=WallPaperData.getWallPaperReviewBean(mImageid, true, pageNum, page);
				if(reviewBean==null){
					//数据加载完成
					mHadler.sendEmptyMessage(WALLPAPER_REVIEW_END);
				}else{
					forwardedCount=reviewBean.getForwardedCount();
					reviewedCount=reviewBean.getReviewedCount(); 
					if(bl){
						mHadler.sendEmptyMessage(WALLPAPER_REVIEW_END);
					}else{
						mListReview.addAll(reviewBean.getListReview()); //评论
						//发出消息
						mHadler.sendEmptyMessage(WALLPAPER_REVIEW);
					}
			
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				mHadler.sendEmptyMessage(WALLPAPER_REVIEW_FAILURE);
				e.printStackTrace();
			} 
		}
		
	}
	
	/**
	 * Show loading dialog
	 */
	private void showLoadingDialog() {
		hideLoadingDialog();
		if (this.dlgLoading == null) {
			this.dlgLoading = ProgressDialog.show(this, "", this.getString(R.string.msg_loading), true, true);
		} else {			
			this.dlgLoading.show();
		}
	}
		
	/**
	 * Hide loading dialog
	 */
	private void hideLoadingDialog() {
		if (dlgLoading != null) {
			this.dlgLoading.dismiss();
		}
	}

	
	/***
	 * 加载 list 分页
	 * @author huaiyu.zhao
	 *
	 */
	class LoadWallpaperListThread extends Thread{	
		private boolean isNext=false;
		private List<WallpaperBean> newMap=new ArrayList<WallpaperBean>();
		WallPaperService wallpaperService=new WallPaperService(WallpaperContent.this);
		public LoadWallpaperListThread(boolean next){
			isNext=next;
		}
		
		@Override
		public void run() {
			//判断如果为空 进行数据查找
			if(listMap==null || listMap.isEmpty()){
				try {
					listMap = wallpaperService.queryItem(WallPaperService.CREATEDB, mImageType);
				} catch (Exception e) {
					mHadler.sendEmptyMessage(WALLPAPER_NETWORK);
					//提示网络连接异常
					e.printStackTrace();
				} 
			}
			if(!isNext){
				//判断是上页还是下 页
				if( mTag<0 ){
					mHadler.sendEmptyMessage(WALLPAPER_ONE);
				}else{
					bean=listMap.get(mTag);
					mHadler.sendEmptyMessage(WALLPAPER_PREVIOUS);
				}
			}else{
				//下页
				if(mTag>=listMap.size()){
					//加载数据
					try {
						newMap = wallpaperService.queryItem(WallPaperService.UPDATEDATA, mImageType);
						if(listMap.size()==newMap.size()){
							mHadler.sendEmptyMessage(WALLPAPER_END);
						}else{
							listMap.clear();
							listMap.addAll(newMap);
							bean=listMap.get(mTag);
							mHadler.sendEmptyMessage(WALLPAPER_NEXT);
						}
					} catch (Exception e) {
						mHadler.sendEmptyMessage(WALLPAPER_NETWORK);
						//提示网络连接异常
						e.printStackTrace();
					} 					
				}else{
					bean=listMap.get(mTag);
					mHadler.sendEmptyMessage(WALLPAPER_NEXT);
				}				
			}		
		}		
	}
	
	
	private Handler  mHadler=new Handler(){
		public void handleMessage(Message msg) {
			mThreadRunning = false;
			switch (msg.what) {
			case WALLPAPER_ONE:
				hideLoadingDialog();
				detail_previous_button.setOnClickListener(null);
				Toast.makeText(WallpaperContent.this, getResources().getText(R.string.wallpaper_msg_one), Toast.LENGTH_SHORT).show();
				break;
			case WALLPAPER_PREVIOUS:
				//上翻页
				setViewData(bean);
				hideLoadingDialog();
				break;
			case WALLPAPER_NEXT:
				//下翻页
				setViewData(bean);
				hideLoadingDialog();
				break;	
			case WALLPAPER_NETWORK:
				//网络连接失败				
				hideLoadingDialog();
				Toast.makeText(WallpaperContent.this, getResources().getText(R.string.msg_loading_network), Toast.LENGTH_SHORT).show();
				break;	
			case WALLPAPER_END:
				detail_next_button.setOnClickListener(null);
				hideLoadingDialog();
				Toast.makeText(WallpaperContent.this, getResources().getText(R.string.wallpaper_msg_end), Toast.LENGTH_SHORT).show();
				break;
			case WALLPAPER_ARTWORK:
				mBitmap=getBitMap(mOldBitmap);		
				mWallpaperImage.setImageBitmap(mBitmap);
				mButton.setOnClickListener(WallpaperContent.this);	
				break;
			case WALLPAPER_REVIEW:
				//评论加载完成
				
				LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT, (mListReview.size()+1) * 150 - 1);
				commentsListLayout.setLayoutParams(lp1);
				mWpAdapter.setReview(mListReview);
				mWpAdapter.notifyDataSetChanged();
				mShareSize.setText(getResources().getString(R.string.msg_forwards_count, forwardedCount));
				mCommentSize.setText(getResources().getString(R.string.msg_comments_count, reviewedCount));
				
				hideReviewLoadingDialog();
				
				break;
			case WALLPAPER_REVIEW_FAILURE:
				//评论联网失败
				noReviewNetworkDialog(MSG_NETWORK);
				break;
			case WALLPAPER_REVIEW_END:
				//评论加载完成 没数据可加载
				noReviewNetworkDialog(MSG_END);
			break;
			default:
				break;
			}
			
			
		};		
	};
	
	//----------
	private int mLastVisibleItem;
	private ListView mCommentView;//评论
	private View mFootView;
	public static WallpaperOrRingAdapter mWpAdapter;//适配器
	public static List<Review> mListReview =new ArrayList<Review>();//评论
	private View mLoadingProgressBar;
	private TextView mLoadingText;
	private boolean mThreadRunning;
	private static final int pageNum=10; //每页显示的记录数
	private int forwardedCount=0;
	private int reviewedCount=0;
	private int download_nums=0;
	private LinearLayout commentsListLayout;
	
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
	            new LoadReviewTherad(bean.getImage_id()).start();
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
		    if(type==MSG_NETWORK){
		    	 mLoadingText.setText(R.string.msg_loading_network);
		    }else if(type==MSG_END){
		    	 mLoadingText.setText(R.string.msg_loading_not);
		    	// mLoadingText.setOnClickListener(null); //完成了取消 按钮
		    }else{
		    	 mLoadingText.setText(R.string.msg_loading_network);
		    }
	       
			mLoadingProgressBar.setVisibility(View.GONE);
		}
	    
}