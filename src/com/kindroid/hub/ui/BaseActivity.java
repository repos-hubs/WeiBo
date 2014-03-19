package com.kindroid.hub.ui;

import com.kindroid.hub.R;
import com.kindroid.hub.data.UserDefaultInfo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.text.TextUtils;

public class BaseActivity extends Activity {
	
	public ProgressDialog mProgressDialog;

	public boolean checkLogin() {
		String token = UserDefaultInfo.getWeiboToken(this);
		String tokenSecret = UserDefaultInfo.getWeiboTokenSecret(this);
		if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(tokenSecret)) {
			return true;
		}
		return false;
	}
	
	public void showProgressDialog() {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle(getResources().getText(R.string.app_name));
		mProgressDialog.setMessage(getResources().getText(R.string.progress_message));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();
	}
}
