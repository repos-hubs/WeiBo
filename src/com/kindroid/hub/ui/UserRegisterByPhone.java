package com.kindroid.hub.ui;

import com.kindroid.hub.R;
import com.kindroid.hub.data.UserData;
import com.kindroid.hub.data.UserDefaultInfo;
import com.kindroid.hub.entity.User;
import com.kindroid.hub.utils.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class UserRegisterByPhone extends Activity implements OnClickListener{

	private EditText mNickname;
	private String mPhone;
	
	private ImageView mCommonLogin;
	private ImageView mUserFinish;
	private String token[] = new String[1];
	
	
	private ProgressDialog mProgressDialog;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			mProgressDialog.dismiss();
			super.handleMessage(msg);
			switch (msg.what) {
			case 10:
				Toast.makeText(UserRegisterByPhone.this, R.string.user_register_success, Toast.LENGTH_SHORT).show();
				if (token.length > 0) {
					UserDefaultInfo.setUserToken(UserRegisterByPhone.this,token[0]);
					Intent intent = new Intent(UserRegisterByPhone.this,HubMain.class);
					startActivity(intent);
				}
				break;
			case -5:
				Toast.makeText(UserRegisterByPhone.this, R.string.user_register_phone_used, Toast.LENGTH_SHORT).show();
				break;
			case -10:
				Toast.makeText(UserRegisterByPhone.this, R.string.user_register_connect_error, Toast.LENGTH_SHORT).show();
				break;
			}
		}

	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_register_by_telephone);
		findViews();
		autoFillData();
	}
	
	private void findViews() {
		mNickname = (EditText) findViewById(R.id.nickname);
		mCommonLogin = (ImageView) findViewById(R.id.commonLogin);
		mCommonLogin.setOnClickListener(this);
		mUserFinish=(ImageView)findViewById(R.id.userFinish);
		mUserFinish.setOnClickListener(this);
		
		ImageView loginTitle = (ImageView) findViewById(R.id.loginTitle);
		loginTitle.bringToFront();
	}
	
	private void autoFillData(){
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		String phone = telephonyManager.getLine1Number();
		if (!TextUtils.isEmpty(phone)) {
			if (phone.length() > 11) {
				mPhone = phone.substring(phone.length() - 11, phone.length());
			} else {
				mPhone = phone;
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.commonLogin:
			Intent intent = new Intent(this, UserLogin.class);
			startActivity(intent);
			break;
		case R.id.userFinish:
			valid();
			break;
		default:
			break;
		}
	}
	
	private void showProgressDialog(){
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle(getResources().getText(R.string.app_name));
		mProgressDialog.setMessage(getResources().getText(R.string.progress_message));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();
	}
	
	private void valid(){
		Utils.hideKeyBoard(this, getCurrentFocus());
		if (mNickname.getText().toString().length() < 4 || mNickname.getText().toString().length() > 20) {
			Toast.makeText(this, R.string.user_register_nickname_valid, Toast.LENGTH_SHORT).show();
			return;
		}
		showProgressDialog();
		new TelephoneRegist().start();
	}
	
	private class TelephoneRegist extends Thread{
		public void run() {
			UserData userData = new UserData(UserRegisterByPhone.this);
			User user = new User();
			user.setNickname(mNickname.getText().toString().trim());
			user.setUsername(mPhone);
			user.setPassword("");
			int status = userData.userRegister(user, token);
			mHandler.sendEmptyMessage(status);
		}
	}

}
