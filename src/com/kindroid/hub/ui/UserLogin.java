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
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UserLogin extends Activity  implements OnClickListener{
	
	private EditText mUserName;
	private EditText mUserPassword;
	
	private ImageView mUserLogin;
	private ImageView mUserRegister;
	private TextView mUserForgetPwd;
	
	private ProgressDialog mProgressDialog;
	
	private ImageView mSinaWeibo;
	private ImageView mQQWeibo;

	private User mUser;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			mProgressDialog.dismiss();
			super.handleMessage(msg);
			switch (msg.what) {
			case -1:
				Toast.makeText(UserLogin.this, R.string.user_valid_msg, Toast.LENGTH_SHORT).show();
				break;
			case -3:
				Toast.makeText(UserLogin.this, R.string.user_register_password, Toast.LENGTH_SHORT).show();
				break;
			case -4:
				Toast.makeText(UserLogin.this, R.string.user_register_phone_format, Toast.LENGTH_SHORT).show();
				break;
			case -6:
				Toast.makeText(UserLogin.this, R.string.user_register_email_format, Toast.LENGTH_SHORT).show();
				break;
			case 0:
				Toast.makeText(UserLogin.this, R.string.user_register_connect_error, Toast.LENGTH_SHORT).show();
				break;
			case 1:
				Toast.makeText(UserLogin.this, R.string.user_login_success, Toast.LENGTH_SHORT).show();
				if (TextUtils.isEmpty(mUser.getNickname())) {
					Intent intent = new Intent(UserLogin.this,UserRegister.class);
					intent.putExtra(UserRegisterNickname.EXTRA_TOTKEN,UserDefaultInfo.getUserToken(UserLogin.this));
					startActivity(intent);
				} else {
					Intent intent = new Intent(UserLogin.this, HubMain.class);
					startActivity(intent);
				}
				break;
			default:
				break;
			}
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_login);
		findViews();
	}
	
	private void findViews() {
		mUserName = (EditText) findViewById(R.id.userName);
		mUserPassword = (EditText) findViewById(R.id.password);

		mUserLogin = (ImageView) findViewById(R.id.userLogin);
		mUserLogin.setOnClickListener(this);
		mUserRegister = (ImageView) findViewById(R.id.userRegister);
		mUserRegister.setOnClickListener(this);

		ImageView loginTitle = (ImageView) findViewById(R.id.loginTitle);
		loginTitle.bringToFront();
		
	
		mUserForgetPwd = (TextView) findViewById(R.id.userForgetPwd);
		mUserForgetPwd.setOnClickListener(this);

		mSinaWeibo = (ImageView) findViewById(R.id.sinaWeibo);
		mSinaWeibo.setOnClickListener(this);
		mQQWeibo = (ImageView) findViewById(R.id.qqWeibo);
		mQQWeibo.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.userLogin:
			validUserLogin();
			break;
		case R.id.userRegister:
			Intent intent = new Intent(this, UserRegister.class);
			startActivity(intent);
			break;
		case R.id.userForgetPwd:
			Intent pwdIntent = new Intent(UserLogin.this,UserForgetPwdStepOne.class);
			startActivity(pwdIntent);
			break;
		case R.id.sinaWeibo:
			Intent i = new Intent(this, WeiboLogin.class);
			startActivity(i);
			break;
		case R.id.qqWeibo:
			
			break;
		default:
			break;
		}
	}
	
	private void validUserLogin(){
		Utils.hideKeyBoard(this, getCurrentFocus());
		if(TextUtils.isEmpty(mUserName.getText().toString())){
			Toast.makeText(UserLogin.this, R.string.user_register_username_format, Toast.LENGTH_SHORT).show();
			return;
		}else if(TextUtils.isEmpty(mUserPassword.getText().toString())){
			Toast.makeText(UserLogin.this, R.string.user_register_password, Toast.LENGTH_SHORT).show();
			return;
		}
		showProgressDialog();
		new ValidUserLogin().start();
	}
	
	private void showProgressDialog(){
		mProgressDialog=new ProgressDialog(this);
		mProgressDialog.setTitle(getResources().getText(R.string.app_name));
		mProgressDialog.setMessage(getResources().getText(R.string.progress_message));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();
	}
	
	public class ValidUserLogin extends Thread {
		public void run() {
			String name = mUserName.getText().toString();
			String pwd = mUserPassword.getText().toString();
			mUser = new User();
			mUser.setUsername(name);
			mUser.setPassword(pwd);
			UserData userData = new UserData(UserLogin.this);
			int status = userData.userLogin(mUser);
			mHandler.sendEmptyMessage(status);
		}
	}

}
