package com.kindroid.hub.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weibo4android.Comment;
import weibo4android.Status;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.hub.R;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.data.UserDefaultInfo;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;

public class HubReplyActivity extends Activity implements View.OnClickListener {
	final String TAG = "HubReplyActivity";
	final int REPLY_RESULT = 0;
	final int MAX_WORDS = 140;						//请允许输入最多字数
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
	private ProgressDialog dlgLoading = null;
	private TextView canInputWordsView;
	
	private WeiboContent weibo;
	private int currentPosition = -1;
//	private String from = "";
	private String fromMode = "";
	//indicate synchronize to weibo or not
	private boolean isSynWeibo = true;
	private String replyType = "";
	
	private boolean canReplay = false;	
	
	public static List<Comment> commentsList = new ArrayList<Comment>();
	private Comment comment;
	
	private Status status;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_reply_content);
		fromMode = getIntent().getStringExtra("fromMode");
		replyType = getIntent().getStringExtra("type");
		if (!TextUtils.isEmpty(fromMode) && fromMode.equals("channel")) {
			
			//取得列表界面选择位置
			currentPosition = getIntent().getIntExtra("listPosition", -1);
//			from = getIntent().getStringExtra("from");
			if (!TextUtils.isEmpty(replyType) && replyType.equals("comment")) {
//				weibo = NewsListActivity.newsList.get(currentPosition);
				weibo = NewsDetailsActivity.weibo;
			} else if (!TextUtils.isEmpty(replyType) && replyType.equals("reply")) {
				weibo = NewsDetailsActivity.weibo;
				comment = this.commentsList.get(currentPosition);
			}
			
		} else if (!TextUtils.isEmpty(fromMode) && fromMode.equals("sina")) {
			//取得列表界面选择位置
			currentPosition = getIntent().getIntExtra("listPosition", -1);
			
			if (!TextUtils.isEmpty(replyType) && replyType.equals("comment")) {
				if (HubSinaWeiboDetailsActivity.statusList != null) {
					status = HubSinaWeiboDetailsActivity.statusList.get(currentPosition);
				}
			} else if (!TextUtils.isEmpty(replyType) && replyType.equals("reply")) {
				if (HubSinaWeiboDetailsActivity.statusList != null) {
					status = HubSinaWeiboDetailsActivity.statusList.get(currentPosition);
				}
				comment = this.commentsList.get(currentPosition);
			}
		}
		/*if (weibo == null) {
			finish();
		}*/
		findViews();
		initComponents();
	}
	
	private void findViews() {
		btnBack = (ImageView) findViewById(R.id.btn_back);
		replyEditText = (EditText) findViewById(R.id.replyEditText);
		isReplyToWeiboImageView = (ImageView) findViewById(R.id.isReplyToWeiboImageView);
		synToWeiboTextView = (TextView) findViewById(R.id.synToWeiboTextView);
		atImageView = (ImageView) findViewById(R.id.atImageView);
		smileImageView = (ImageView) findViewById(R.id.smileImageView);
		replyButton = (Button) findViewById(R.id.replyButton);
		newsContentWebView = (WebView) findViewById(R.id.newsContentsWebView);
		titleBarLayout = (RelativeLayout) findViewById(R.id.title_bar);
		replyTitleTextView = (TextView) findViewById(R.id.replyTitleTextView);
		canInputWordsView = (TextView) findViewById(R.id.canInputWordsView);
	}
	
	private void initComponents() {
		//news content
		String newsContentStr = "";
		if (!TextUtils.isEmpty(fromMode) && fromMode.equals("sina")) {
			if (replyType.equals("comment")) {
				newsContentStr = status.getText();
			} else if (replyType.equals("reply")) {
				newsContentStr = comment.getText();
			}
		} else {
			if (replyType.equals("comment")) {
				newsContentStr = weibo.getContent();
			} else if (replyType.equals("reply")) {
				newsContentStr = comment.getText();
			}
		}
		
		try {
			newsContentWebView.setBackgroundColor(0);
			newsContentWebView.getSettings().setSupportZoom(false);
			newsContentWebView.getSettings().setJavaScriptEnabled(true);
			newsContentWebView.getSettings().setDefaultTextEncodingName(encoding);
			newsContentWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS); 
			newsContentWebView.loadData(URLEncoder.encode(newsContentStr, encoding).replaceAll("\\+", "%20"), mimeType, encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		final TextWatcher watcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }
      
            @Override  
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  
            }
            
            @Override  
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	String inputWords = replyEditText.getText().toString();
            	if (inputWords.length() <= MAX_WORDS) {
            		canReplay = true;
            		String formatedWord = String.valueOf(MAX_WORDS - inputWords.length());
            		canInputWordsView.setText(formatedWord);
            	} else {
            		canReplay = false;
            		canInputWordsView.setText("0");
            		Toast.makeText(HubReplyActivity.this, R.string.too_more_words, Toast.LENGTH_SHORT).show();
            	}
            }              
        };
        replyEditText.addTextChangedListener(watcher);
		
		//initialize title and button background
		if (!TextUtils.isEmpty(fromMode) && fromMode.equals("channel")) {
			titleBarLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_blue_bg));
			replyButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.forward_button_blue_bg));
		} else {
			titleBarLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.head_bg));
			replyButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.forward_button_red_bg));
		}
		
		btnBack.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					finish();
				}
			}
		);
		isReplyToWeiboImageView.setOnClickListener(this);
		synToWeiboTextView.setOnClickListener(this);
		//set title content
		if (replyType.equals("comment")) {
			replyTitleTextView.setText(getResources().getString(R.string.home_comment_title));
			replyButton.setText(getResources().getString(R.string.home_comment_title));
		} else if (replyType.equals("reply")) {
			replyTitleTextView.setText(getResources().getString(R.string.home_reply_title));
			replyButton.setText(getResources().getString(R.string.home_reply_title));
		}
		
		//设置标题背景
		if (!TextUtils.isEmpty(fromMode) && fromMode.equals("channel")) {
			titleBarLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_blue_bg));
			replyButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.forward_button_blue_bg));
		} else {
			titleBarLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.head_bg));
			replyButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.forward_button_red_bg));
		}
		replyButton.setOnClickListener(this);
	}

	Handler dataHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			hideLoadingDialog();
			switch (msg.arg1) {
			case REPLY_RESULT:
				String result = (String) msg.obj;
				if (result.equals("success")) {
					Toast.makeText(HubReplyActivity.this, getResources().getString(R.string.msg_reply_success), Toast.LENGTH_SHORT).show();
					finish();
				} else {
					Toast.makeText(HubReplyActivity.this, getResources().getString(R.string.msg_reply_failure), Toast.LENGTH_SHORT).show();
				}
				
				break;

			default:
				break;
			}
		}
	};
	
	@Override
	public void onClick(View view) {
		
		if (view.getId() == R.id.isReplyToWeiboImageView || view.getId() == R.id.synToWeiboTextView) {
			isSynWeibo = changeCheckStatus(isSynWeibo);
		}
		//commit reply content
		if (view.getId() == R.id.replyButton) {
			String token = UserDefaultInfo.getWeiboToken(HubReplyActivity.this);
			if (!TextUtils.isEmpty(token)) {
				String replyStr = replyEditText.getText() + "";
				if (!TextUtils.isEmpty(replyStr.trim())) {
					if (canReplay) {
						showProgressDialog();
						new CommitReply().start();
					} else {
						Toast.makeText(HubReplyActivity.this, R.string.too_more_words, Toast.LENGTH_SHORT).show();
					}
					
				} else {
					Toast.makeText(HubReplyActivity.this, getResources().getString(R.string.msg_release_not_empty), Toast.LENGTH_SHORT).show();
				}
			} else {
				Intent intent = new Intent(HubReplyActivity.this, WeiboLogin.class);
				startActivity(intent);
			}
		}
	}
	
	private class CommitReply extends Thread {

		@Override
		public void run() {
			String replyConent = replyEditText.getText() + "";
			String weiboId = "";
			String commentId = "";
			if (!TextUtils.isEmpty(fromMode) && fromMode.equals("channel")) {
				weiboId = weibo.getWeibocontentId() + "";
			} else {
				weiboId = status.getMid();
			}
			if (!TextUtils.isEmpty(replyType) && replyType.equals("reply")) {
				commentId = comment.getId() + "";
			}
			
			String replyResult = DataService.sendReplyContentToSina(replyConent, weiboId, commentId, replyType, HubReplyActivity.this);
			Message msg = dataHandler.obtainMessage();
			msg.arg1 = REPLY_RESULT;
			msg.obj = replyResult;
			if (replyResult != null) {
				dataHandler.sendMessage(msg);
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
	private void showProgressDialog() {
		dlgLoading = new ProgressDialog(this);
		dlgLoading.setTitle(getResources().getText(R.string.app_name));
		dlgLoading.setMessage(getResources().getText(R.string.progress_message));
		dlgLoading.setIndeterminate(true);
		dlgLoading.show();
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
