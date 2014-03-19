package com.kindroid.hub.ui;

import java.util.SortedSet;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import com.kindroid.hub.R;
import com.kindroid.hub.data.UserDefaultInfo;
import com.kindroid.hub.utils.Constant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WeiboLogin extends Activity {
	
	private WebView mWebView;
	
	private ProgressDialog mProgressDialog;
	
	private CommonsHttpOAuthConsumer httpOauthConsumer;
	private OAuthProvider httpOauthprovider;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			mProgressDialog.dismiss();
			switch (msg.what) {
			case 10:
				Intent intent = new Intent(WeiboLogin.this, HubMain.class);
				startActivity(intent);
				break;
			case 20:
				Toast.makeText(WeiboLogin.this,getResources().getString(R.string.sina_auth_failure),Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}

	};
	
	WebViewClient loginWebViewClient = new WebViewClient() {
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			mProgressDialog.dismiss();
		}

		@Override
		public void onPageStarted(WebView view, String url,Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			if(mProgressDialog!=null){
				mProgressDialog.show();
			}
		}

		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			super.shouldOverrideUrlLoading(view, url);
			if (url.contains("https://sina_callback")) {
				Uri uri = Uri.parse(url);
				String verifier = uri.getQueryParameter("oauth_verifier");
				if (verifier != null) {
					new CheckLogin(verifier).start();
				}
			}
			return true;
		}

	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_weibo_login);
		findViews();
		showProgressDialog();
		 try {
	        	httpOauthConsumer = new CommonsHttpOAuthConsumer(Constant.CONSUMER_KEY, Constant.CONSUMER_SECRET);
	    		httpOauthprovider = new DefaultOAuthProvider("http://api.t.sina.com.cn/oauth/request_token", "http://api.t.sina.com.cn/oauth/access_token",
	    				"http://api.t.sina.com.cn/oauth/authorize");
	    		String authUrl = httpOauthprovider.retrieveRequestToken(httpOauthConsumer, "https://sina_callback");
	    		mWebView.getSettings().setJavaScriptEnabled(true);
	    		mWebView.getSettings().setSupportZoom(true);
	    		mWebView.getSettings().setBuiltInZoomControls(true);
	    		mWebView.loadUrl(authUrl);
				mWebView.setWebViewClient(loginWebViewClient);
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	}
	
	private void findViews() {
		mWebView = (WebView) findViewById(R.id.web);
	}
	
	private void showProgressDialog(){
		mProgressDialog=new ProgressDialog(this);
		mProgressDialog.setTitle(getResources().getText(R.string.app_name));
		mProgressDialog.setMessage(getResources().getText(R.string.progress_message));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();
	}
	
	private class CheckLogin extends Thread {
		public String verifier;

		public CheckLogin(String verifier) {
			this.verifier = verifier;
		}

		public void run() {
			try {
				httpOauthprovider.setOAuth10a(true);
				httpOauthprovider.retrieveAccessToken(httpOauthConsumer,verifier);
    			SortedSet<String> user_id = httpOauthprovider.getResponseParameters().get("user_id");
    			String userId = user_id.first();
    			UserDefaultInfo.putWeiboId(WeiboLogin.this, userId);
				String token = httpOauthConsumer.getToken();
				String tokenSecret = httpOauthConsumer.getTokenSecret();
				if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(tokenSecret)) {
					UserDefaultInfo.putWeiboToken(WeiboLogin.this, token);
					UserDefaultInfo.putWeiboTokenSecret(WeiboLogin.this,tokenSecret);
					mHandler.sendEmptyMessage(10);
				}
			} catch (OAuthMessageSignerException ex) {
				mHandler.sendEmptyMessage(20);
				ex.printStackTrace();
			} catch (OAuthNotAuthorizedException ex) {
				mHandler.sendEmptyMessage(20);
				ex.printStackTrace();
			} catch (OAuthExpectationFailedException ex) {
				mHandler.sendEmptyMessage(20);
				ex.printStackTrace();
			} catch (OAuthCommunicationException ex) {
				mHandler.sendEmptyMessage(20);
				ex.printStackTrace();
			} 
		}
	}

}
