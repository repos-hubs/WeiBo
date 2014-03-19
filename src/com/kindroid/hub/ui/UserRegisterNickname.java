package com.kindroid.hub.ui;

import com.kindroid.hub.R;
import com.kindroid.hub.data.UserData;
import com.kindroid.hub.data.UserDefaultInfo;
import com.kindroid.hub.entity.User;
import com.kindroid.hub.entity.WeiboInfo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class UserRegisterNickname extends Activity implements OnClickListener {
	public final static String EXTRA_TYPE = "type";
	public final static String EXTRA_TOTKEN = "token";
	
	private String mToken;

	public static WeiboInfo weiboInfo;
	public static User user;
	
	private EditText mNickname;
	private ImageView mFinish;
	
	private ProgressDialog mProgressDialog;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			mProgressDialog.dismiss();
			super.handleMessage(msg);
			switch (msg.what) {
			case -2:
				Toast.makeText(UserRegisterNickname.this, R.string.user_register_nickname_valid, Toast.LENGTH_SHORT).show();
				break;
			case 10:
				Toast.makeText(UserRegisterNickname.this, R.string.user_register_success, Toast.LENGTH_SHORT).show();
				if (mToken != null) {
					UserDefaultInfo.setUserToken(UserRegisterNickname.this,mToken);
					Intent intent = new Intent(UserRegisterNickname.this,HubMain.class);
					startActivity(intent);
				}
				break;
			case -10:
				Toast.makeText(UserRegisterNickname.this, R.string.user_register_connect_error, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_register_nickname);
		findViews();
		mToken = getIntent().getStringExtra(EXTRA_TOTKEN);
	}
	
	private void findViews() {
		mNickname = (EditText) findViewById(R.id.nickname);
		mFinish = (ImageView) findViewById(R.id.userFinish);
		mFinish.setOnClickListener(this);
		
		ImageView loginTitle = (ImageView) findViewById(R.id.loginTitle);
		loginTitle.bringToFront();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.userFinish:
			valid();
			break;

		default:
			break;
		}
	}
	
	private void showProgressDialog(){
		mProgressDialog=new ProgressDialog(this);
		mProgressDialog.setTitle(getResources().getText(R.string.app_name));
		mProgressDialog.setMessage(getResources().getText(R.string.progress_message));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();
	}
	
	
	private void valid() {
		if (mNickname.getText().toString().trim().length() < 4 && mNickname.getText().toString().trim().length() > 20) {
			Toast.makeText(this, R.string.user_register_nickname_valid, Toast.LENGTH_SHORT).show();
			return;
		}
		showProgressDialog();
		new EditUsername().start();
	}
	
	public class EditUsername extends Thread {
		public void run() {
			UserData userData = new UserData(UserRegisterNickname.this);
			int status = userData.editUserNickname(mToken, mNickname.getText().toString().trim());
			mHandler.sendEmptyMessage(status);
		}
	}

}
