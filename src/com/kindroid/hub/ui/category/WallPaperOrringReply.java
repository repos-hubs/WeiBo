package com.kindroid.hub.ui.category;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.hub.R;
import com.kindroid.hub.data.UserDefaultInfo;
import com.kindroid.hub.data.WallPaperData;
import com.kindroid.hub.proto.WeiboContentProtoc.Review;
import com.kindroid.hub.ui.UserLogin;
import com.kindroid.hub.ui.category.ringtone.RingtoneDetailsActivity;
import com.kindroid.hub.ui.category.wallpaper.WallPaperReviewBean;
import com.kindroid.hub.ui.category.wallpaper.WallpaperOrRingBean;

/***
 * 壁纸和铃音
 * @author huaiyu.zhao
 *
 */
public class WallPaperOrringReply extends Activity implements View.OnClickListener {
	final String TAG = "WallPaperOrringReply";
	final int REPLY_RESULT = 0;
	private static final int REPLY_NETWORK=1;//网络连接失败
	final String mimeType = "text/html";
	final String encoding = "utf-8";
	
	private ImageView btnBack;
	private EditText replyEditText;
	private ImageView isReplyToWeiboImageView;
	private TextView synToWeiboTextView;
	private ImageView atImageView;
	private ImageView smileImageView;
	private Button replyButton;
	private WebView newsContentWebView;
	private RelativeLayout titleBarLayout;
	private TextView replyTitleTextView;
	/**铃声或壁纸id*/
	public static final String EXTRA_ID = "extra_id";
	/**评论列表位置*/
	public static final String EXTRA_POSITION = "listPosition";
	/**动作类型*/
	public static final String EXTRA_TYPE = "extra_type";
	/**来自铃声或是壁纸*/
	public static final String EXTRA_FROM_MODE = "extra_fromMode";
	/**壁纸类型*/
	public static final String MODE_WALLPAPER = "wallpaper";
	/**铃声类型*/
	public static final String MODE_RING = "ring";
	/**回复操作*/
	public static final String ACTION_REPLY = "replay";
	/**评论操作*/
	public static final String ACTION_COMMENT = "comment";
	/**转发操作*/
	public static final String ACTION_FORWARD = "forward";
	
	private int currentPosition = -1;
	private String from = "";
	private String fromMode = "";	
	private boolean isSynWeibo = true;
	private String replyType = "";
	
	private ProgressDialog dlgLoading = null;
	private WallpaperOrRingBean bean=new WallpaperOrRingBean();
	private Review review;
	private long id;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_reply_content);
		fromMode = getIntent().getStringExtra(EXTRA_FROM_MODE);
		replyType = getIntent().getStringExtra(EXTRA_TYPE);
		id=getIntent().getLongExtra(EXTRA_ID, -1);
		if  (!TextUtils.isEmpty(fromMode) && fromMode.equals(MODE_WALLPAPER)) {
			//取得列表界面选择位置
			currentPosition = getIntent().getIntExtra(EXTRA_POSITION, -1);
			if (!TextUtils.isEmpty(replyType) && replyType.equals(ACTION_COMMENT)) {	
			} else if (!TextUtils.isEmpty(replyType) && replyType.equals(ACTION_REPLY)) {				
				review = WallpaperContent.mListReview.get(currentPosition);
			}			
		} else if  (!TextUtils.isEmpty(fromMode) && fromMode.equals(MODE_RING)) {
			id = getIntent().getLongExtra(EXTRA_ID, -1);
			currentPosition = getIntent().getIntExtra(EXTRA_POSITION, -1);
			if (!TextUtils.isEmpty(replyType) && replyType.equals(ACTION_COMMENT)) {	
			} else if (!TextUtils.isEmpty(replyType) && replyType.equals(ACTION_REPLY)) {				
				review = RingtoneDetailsActivity.commentsList.get(currentPosition);
			}
		}
	
		findViews();
		initComponentsWallpaper();
		bean.setType(fromMode);
		bean.setReview(review);
		bean.setId(id);
		bean.setReplyType(replyType);
	}
	
	private void initComponentsWallpaper(){
		
		
	if (replyType.equals(ACTION_COMMENT)) {
		replyTitleTextView.setText(getResources().getString(R.string.home_comment_title));
		replyButton.setText(getResources().getString(R.string.home_comment_title));
	} else if (replyType.equals(ACTION_REPLY)) {
		replyTitleTextView.setText(getResources().getString(R.string.home_reply_title));
		replyButton.setText(getResources().getString(R.string.home_reply_title));
	}else if (replyType.equals(ACTION_FORWARD)) {
		replyTitleTextView.setText(getResources().getString(R.string.home_forward_title));
		replyButton.setText(getResources().getString(R.string.home_forward_title));
	}
		
		replyButton.setOnClickListener(this);
	}
	
	
	private void findViews() {
		btnBack = (ImageView) findViewById(R.id.btn_back);
		btnBack.setOnClickListener(this);
		replyEditText = (EditText) findViewById(R.id.replyEditText);
		isReplyToWeiboImageView = (ImageView) findViewById(R.id.isReplyToWeiboImageView);
		
		synToWeiboTextView = (TextView) findViewById(R.id.synToWeiboTextView);
		isReplyToWeiboImageView.setVisibility(View.GONE);
		synToWeiboTextView.setVisibility(View.GONE);
		
		atImageView = (ImageView) findViewById(R.id.atImageView);
		smileImageView = (ImageView) findViewById(R.id.smileImageView);
		replyButton = (Button) findViewById(R.id.replyButton);
		newsContentWebView = (WebView) findViewById(R.id.newsContentsWebView);
		titleBarLayout = (RelativeLayout) findViewById(R.id.title_bar);
		replyTitleTextView = (TextView) findViewById(R.id.replyTitleTextView);
	}

	Handler dataHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			hideLoadingDialog();
			Map<String, Integer> result;
			int isSuccess ;
			int reviewCount;
			switch (msg.arg1) {
			case REPLY_RESULT:
				result = (Map<String,Integer>) msg.obj;
				isSuccess = result.get("isSuccess");
				reviewCount = result.get("reviewCount");
				//WallpaperContent.mListReview = reviewCount;
				if (isSuccess == 1) {
					Toast.makeText(WallPaperOrringReply.this, getResources().getString(R.string.msg_send_reply_success), Toast.LENGTH_SHORT).show();
					if(fromMode==(MODE_WALLPAPER)|| fromMode.equals(MODE_WALLPAPER)){
						if(WallpaperContent.mWpAdapter!=null){
							WallpaperContent.mListReview=new ArrayList<Review>();						
							try {
								WallPaperReviewBean reviewBean=WallPaperData.getWallPaperReviewBean(id, true, 10, 0);
								WallpaperContent.mListReview.addAll(reviewBean.getListReview());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								//e.printStackTrace();
							} 
							WallpaperContent.mWpAdapter.setReview(WallpaperContent.mListReview);
							WallpaperContent.mWpAdapter.notifyDataSetChanged();
						}
						
					} else {//铃音
					
					}
					finish();
				} else {
					Toast.makeText(WallPaperOrringReply.this, getResources().getString(R.string.msg_send_reply_failure), Toast.LENGTH_SHORT).show();
				}
				break;	
			case REPLY_NETWORK:
				Toast.makeText(WallPaperOrringReply.this, getResources().getString(R.string.msg_loading_network), Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.isReplyToWeiboImageView:
		case R.id.synToWeiboTextView:
			isSynWeibo = changeCheckStatus(isSynWeibo);
			break;
		case R.id.replyButton:
			String token = UserDefaultInfo.getUserToken(WallPaperOrringReply.this);
			if (!TextUtils.isEmpty(token)) {
				
				showLoadingDialog();
				new CommitReplyWallPaper().start();
			} else {
				Intent intent = new Intent(WallPaperOrringReply.this, UserLogin.class);
				startActivity(intent);
			}
			break;
		}
	}
	//weibo, review, replyConent,"wallpaper",wallpaperid
	private class CommitReplyWallPaper extends Thread {

		@Override
		public void run() {
			String replyConent = replyEditText.getText() + "";
			bean.setReplyContent(replyConent);
			bean.setToken(UserDefaultInfo.getUserToken(WallPaperOrringReply.this));
			Message msg = dataHandler.obtainMessage();
			Map<String, Integer> replyResult;
			try {
				replyResult = WallPaperData.sendReplyContent(bean);				
				msg.arg1 = REPLY_RESULT;
				msg.obj = replyResult;
				if (replyResult != null) {
					dataHandler.sendMessage(msg);
				}
			} catch (Exception e) {
				//联网失败 显示失败新
				e.printStackTrace();				
				dataHandler.sendEmptyMessage(REPLY_NETWORK);
				
			} 
			
		}
	}
	
	
	private boolean changeCheckStatus(boolean isSynWeibo) {
		boolean status = false;
		if (isSynWeibo) {
			isReplyToWeiboImageView.setImageDrawable(getResources().getDrawable(R.drawable.icon_check_button_unselected));
			status = false;
		} else {
			isReplyToWeiboImageView.setImageDrawable(getResources().getDrawable(R.drawable.icon_check_button_selected));
			status = true;
		}
		return status;
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
}