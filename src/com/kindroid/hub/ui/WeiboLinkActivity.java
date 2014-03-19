package com.kindroid.hub.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.kindroid.hub.R;

public class WeiboLinkActivity extends Activity {
	private final static String TAG = "WeiboLinkActivity";
	private WebView weiboWebView;
	private ImageView btnBack;
	private ProgressDialog dlgLoading = null;
	
	private String url;
	final String mimeType = "text/html";
	final String encoding = "utf-8";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_news_main_webview);
		url = getIntent().getStringExtra("url");
		findViews();
	}

	private void findViews() {
		weiboWebView = (WebView) findViewById(R.id.webview);
		
		weiboWebView.setBackgroundColor(0);
		weiboWebView.getSettings().setJavaScriptEnabled(true);
		weiboWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
		weiboWebView.getSettings().setDefaultTextEncodingName(encoding);
		weiboWebView.getSettings().setJavaScriptEnabled(true);
		weiboWebView.getSettings().setSupportZoom(true);
		weiboWebView.getSettings().setBuiltInZoomControls(true);
		weiboWebView.loadUrl(url);
		showProgressDialog();
		weiboWebView.setWebViewClient(new MyWebViewClient());
		
		btnBack = (ImageView) findViewById(R.id.btn_back);
		btnBack.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					finish();
				}
			}
		);
	
	}
	
	public class MyWebViewClient extends WebViewClient {
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(TAG, url);
			view.loadUrl(url);
			return true;
		}

		public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {
			Log.d(TAG, "WebView Received Error");
		}

		public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
			Log.d(TAG, "Too Many Redirects");
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			hideLoadingDialog();
		}

		/*@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			showProgressDialog();
		}*/
		
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
