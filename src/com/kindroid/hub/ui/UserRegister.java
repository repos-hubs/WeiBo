package com.kindroid.hub.ui;

import com.kindroid.hub.R;
import com.kindroid.hub.data.UserData;
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

public class UserRegister extends Activity implements OnClickListener{
	
	private EditText mUsername;
	private EditText mPassword;
	
	private ImageView mRegister;
	private ImageView mSinaWeibo;
	private ImageView mQQWeibo;
	
	private ProgressDialog mProgressDialog;
	
	private String[] token=new String[1];
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			mProgressDialog.dismiss();
			super.handleMessage(msg);
			switch (msg.what) {
			case -2:
				Toast.makeText(UserRegister.this, R.string.user_register_username_format, Toast.LENGTH_SHORT).show();
				break;
			case -3:
				Toast.makeText(UserRegister.this, R.string.user_register_password, Toast.LENGTH_SHORT).show();
				break;
			case -4:
				Toast.makeText(UserRegister.this, R.string.user_register_phone_email_valid, Toast.LENGTH_SHORT).show();
				break;
			case -5:
				Toast.makeText(UserRegister.this, R.string.user_register_phone_used, Toast.LENGTH_SHORT).show();
				break;
			case -6:
				Toast.makeText(UserRegister.this, R.string.user_register_email_used, Toast.LENGTH_SHORT).show();
				break;
			case 1:
				Toast.makeText(UserRegister.this, R.string.user_register_username_duplicate, Toast.LENGTH_SHORT).show();
				break;
			case 10:
				if (token.length > 0) {
					Intent intent = new Intent(UserRegister.this,UserRegisterNickname.class);
					intent.putExtra(UserRegisterNickname.EXTRA_TOTKEN, token[0]);
					startActivity(intent);
				}
				break;
			case -10:
				Toast.makeText(UserRegister.this, R.string.user_register_connect_error, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
		
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_register);
		findViews();
		autoFillData();
	}
	
	private void findViews() {
		mUsername = (EditText) findViewById(R.id.userName);
		mPassword = (EditText) findViewById(R.id.password);

		mRegister = (ImageView) findViewById(R.id.userRegister);
		mRegister.setOnClickListener(this);
		mSinaWeibo = (ImageView) findViewById(R.id.sinaWeibo);
		mSinaWeibo.setOnClickListener(this);
		mQQWeibo = (ImageView) findViewById(R.id.qqWeibo);
		mQQWeibo.setOnClickListener(this);
		
		ImageView loginTitle = (ImageView) findViewById(R.id.loginTitle);
		loginTitle.bringToFront();
	}
	
	private void autoFillData(){
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		if (telephonyManager.getLine1Number() != null) {
			//mTelephone.setText(telephonyManager.getLine1Number());
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.userRegister:
			submitRegister();
			break;
		case R.id.sinaWeibo:

			break;
		case R.id.qqWeibo:

			break;
		default:
			break;
		}
	}
	
	public void submitRegister(){
		Utils.hideKeyBoard(this, getCurrentFocus());
		if(TextUtils.isEmpty(mUsername.getText().toString().trim())){
			Toast.makeText(UserRegister.this, R.string.user_register_username_format, Toast.LENGTH_SHORT).show();
			return;
		}else if(TextUtils.isEmpty(mPassword.getText().toString().trim())){
			Toast.makeText(UserRegister.this, R.string.user_register_password, Toast.LENGTH_SHORT).show();
			return;
		}
		showProgressDialog();
		new Register().start();
	}
	
	private void showProgressDialog(){
		mProgressDialog=new ProgressDialog(this);
		mProgressDialog.setTitle(getResources().getText(R.string.app_name));
		mProgressDialog.setMessage(getResources().getText(R.string.progress_message));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();
	}
	
	
	public class Register extends Thread {
		public void run() {
			UserData userData = new UserData(UserRegister.this);
			User user = new User();
			user.setUsername(mUsername.getText().toString());
			user.setPassword(mPassword.getText().toString());
			user.setNickname("");
			int status = userData.userRegister(user,token);
			mHandler.sendEmptyMessage(status);
		}
	}
}
