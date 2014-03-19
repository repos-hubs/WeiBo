package com.kindroid.hub.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
import android.util.Log;
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

public class HubForwardActivity extends Activity implements View.OnClickListener {
	final static String TAG = "HubForwardActivity";
	final int FORWARD_WEIBO_HANDLER = 0;
	final int MAX_WORDS = 140;						//请允许输入最多字数
	
	final String mimeType = "text/html";
	final String encoding = "utf-8";
	
	private ImageView btnBack;
	private EditText forwardEditText;
	private ImageView isForwardToWeiboImageView;
	private TextView synToWeiboTextView;
	private ImageView atImageView;
	private ImageView smileImageView;
	private Button forwardButton;
	private WebView newsContentWebView;
	private RelativeLayout titleBarLayout;
	private ProgressDialog dlgLoading = null;
	private TextView canInputWordsView;
	
	private WeiboContent weibo;
	private Status status;
	private int currentPosition = -1;
	private String from = "";
	private String fromSearch = "";
	private String fromMode = "";
	//indicate synchronize to weibo or not
	private boolean isSynWeibo = true;
	private boolean canForward = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_forward_content);
		fromMode = getIntent().getStringExtra("fromMode");
		if (!TextUtils.isEmpty(fromMode) && fromMode.equals("channel")) {
			
			//取得列表界面选择位置
			currentPosition = getIntent().getIntExtra("listPosition", -1);
			fromSearch = getIntent().getStringExtra("fromSearch");
			if (!TextUtils.isEmpty(fromSearch) && fromSearch.equals("yes")) {
				weibo = HubNewsSearchActivity.newsList.get(currentPosition);
			} else {
				weibo = NewsListActivity.newsList.get(currentPosition);
			}
			
		} else if (!TextUtils.isEmpty(fromMode) && fromMode.equals("sina")) {
			//取得列表界面选择位置
			currentPosition = getIntent().getIntExtra("listPosition", -1);
			if (HubSinaWeiboDetailsActivity.statusList != null) {
				status = HubSinaWeiboDetailsActivity.statusList.get(currentPosition);
			}
			
		}
		
		findViews();
		initComponents();
	}
	
	Handler dataHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			hideLoadingDialog();
			switch (msg.arg1) {
			case FORWARD_WEIBO_HANDLER:
				String result = (String) msg.obj;
				Log.d(TAG, "data handler");
				if (result.equals("success")) {
					Toast.makeText(HubForwardActivity.this, getResources().getString(R.string.msg_forward_success), Toast.LENGTH_SHORT).show();
					finish();
				}
				break;

			default:
				break;
			}
		}
		
	};
	
	private void findViews() {
		btnBack = (ImageView) findViewById(R.id.btn_back);
		forwardEditText = (EditText) findViewById(R.id.forwardEditText);
		isForwardToWeiboImageView = (ImageView) findViewById(R.id.isForwardToWeiboImageView);
		synToWeiboTextView = (TextView) findViewById(R.id.synToWeiboTextView);
		atImageView = (ImageView) findViewById(R.id.atImageView);
		smileImageView = (ImageView) findViewById(R.id.smileImageView);
		forwardButton = (Button) findViewById(R.id.forwardButton);
		newsContentWebView = (WebView) findViewById(R.id.newsContentsWebView);
		titleBarLayout = (RelativeLayout) findViewById(R.id.title_bar);
		canInputWordsView = (TextView) findViewById(R.id.canInputWordsView);
	}
	
	private void initComponents() {
		//news content
		String newsContentStr = "";
		if (!TextUtils.isEmpty(fromMode) && fromMode.equals("sina")) {
			newsContentStr = status.getText();
		} else if (!TextUtils.isEmpty(fromMode) && fromMode.equals("channel")) {
			newsContentStr = weibo.getContent();
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
		
		//initialize title and button background
		if (!TextUtils.isEmpty(fromMode) && fromMode.equals("channel")) {
			titleBarLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_blue_bg));
			forwardButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.forward_button_blue_bg));
		} else {
			titleBarLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.head_bg));
			forwardButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.forward_button_red_bg));
		}
		forwardButton.setOnClickListener(this);
		
		btnBack.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					finish();
				}
			}
		);
		isForwardToWeiboImageView.setOnClickListener(this);
		synToWeiboTextView.setOnClickListener(this);
		
		final TextWatcher watcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }
      
            @Override  
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  
            }
            
            @Override  
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	String inputWords = forwardEditText.getText().toString();
            	if (inputWords.length() <= MAX_WORDS) {
            		canForward = true;
            		String formatedWord = String.valueOf(MAX_WORDS - inputWords.length());
            		canInputWordsView.setText(formatedWord);
            	} else {
            		canForward = false;
            		canInputWordsView.setText("0");
            		Toast.makeText(HubForwardActivity.this, R.string.too_more_words, Toast.LENGTH_SHORT).show();
            	}
            }              
        };
        forwardEditText.addTextChangedListener(watcher);
	}

	@Override
	public void onClick(View view) {
		
		if (view.getId() == R.id.isForwardToWeiboImageView || view.getId() == R.id.synToWeiboTextView) {
			isSynWeibo = changeCheckStatus(isSynWeibo);
		}
		if (view.getId() == R.id.forwardButton) {
			String token = UserDefaultInfo.getUserToken(HubForwardActivity.this);
			if (!TextUtils.isEmpty(token)) {
				String forwardStr = forwardEditText.getText() + "";
				if (canForward) {
					showProgressDialog();
					new ForwardWeibo().start();
				} else {
					Toast.makeText(HubForwardActivity.this, R.string.too_more_words, Toast.LENGTH_SHORT).show();
				}
			} else {
				Intent intent = new Intent(HubForwardActivity.this, WeiboLogin.class);
				startActivity(intent);
			}
		}
	}
	
	class ForwardWeibo extends Thread {

		@Override
		public void run() {
			String weiboId = "";
			if (!TextUtils.isEmpty(fromMode) && fromMode.equals("channel")) {
				weiboId = weibo.getWeibocontentId() + "";
			} else {
				weiboId = status.getMid();
			}
			String result = DataService.forwardWeiboToSina((forwardEditText.getText() + "").trim(), weiboId, HubForwardActivity.this);
			Message msg = dataHandler.obtainMessage();
			msg.arg1 = FORWARD_WEIBO_HANDLER;
			msg.obj = result;
			dataHandler.sendMessage(msg);
		}
	}
	
	private boolean changeCheckStatus(boolean isSynWeibo) {
		boolean status = false;
		if (isSynWeibo) {
			isForwardToWeiboImageView.setImageDrawable(getResources().getDrawable(R.drawable.icon_check_button_unselected));
			status = false;
		} else {
			isForwardToWeiboImageView.setImageDrawable(getResources().getDrawable(R.drawable.icon_check_button_selected));
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
