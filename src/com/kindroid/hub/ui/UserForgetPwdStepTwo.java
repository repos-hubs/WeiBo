package com.kindroid.hub.ui;

import com.kindroid.hub.R;
import com.kindroid.hub.data.UserData;
import com.kindroid.hub.utils.Utils;

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
import android.widget.TextView;
import android.widget.Toast;

public class UserForgetPwdStepTwo extends Activity implements OnClickListener{

	public final static String FIND_TYPE = "find_type";

	private TextView mSubmit;
	private EditText mText;
	private TextView mTextTips;
	private int mType;
	private ImageView mUserRegister;

	private ProgressDialog mProgressDialog;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mProgressDialog.dismiss();
			switch (msg.what) {
			case 1:
				Toast.makeText(UserForgetPwdStepTwo.this,
						R.string.user_find_pwd_by_email_inactive, Toast.LENGTH_SHORT)
						.show();
				break;
			case 2:
				Toast.makeText(UserForgetPwdStepTwo.this,
						R.string.user_find_pwd_by_email_not_exist, Toast.LENGTH_SHORT)
						.show();
				break;
			case 10:
				Toast.makeText(UserForgetPwdStepTwo.this,
						R.string.user_find_pwd_by_email_success, Toast.LENGTH_SHORT)
						.show();
				finish();
				break;
			case -10:
				Toast.makeText(UserForgetPwdStepTwo.this,
						R.string.user_find_pwd_by_email_fail,
						Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_forget_pwd_step_two);
		findViews();
		Intent intent=getIntent();
		if (intent != null) {
			mType = intent.getIntExtra(FIND_TYPE, -1);
			if (mType == 1) {
				mTextTips.setText(R.string.user_forget_pwd_tip2);
			} else if (mType == 2) {
				mTextTips.setText(R.string.user_forget_pwd_tip1);
			}
		}
	}
	
	private void findViews(){
		ImageView loginTitle = (ImageView) findViewById(R.id.loginTitle);
		loginTitle.bringToFront();

		mSubmit = (TextView) findViewById(R.id.submit);
		mSubmit.setOnClickListener(this);
		mText = (EditText) findViewById(R.id.text);
		mTextTips = (TextView) findViewById(R.id.tipText);
		mUserRegister = (ImageView) findViewById(R.id.userRegister);
		mUserRegister.setOnClickListener(this);
	}

	private void showProgressDialog(){
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle(getResources().getText(R.string.app_name));
		mProgressDialog.setMessage(getResources().getText(R.string.progress_message));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.submit:
			if (mType == 1) {
				if (Utils.checkEmail(mText.getText().toString())) {
					Utils.hideKeyBoard(this, getCurrentFocus());
					showProgressDialog();
					new FindPasword().start();
				} else {
					Toast.makeText(this, R.string.user_register_email_format, Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.userRegister:
			Intent intent = new Intent(this, UserRegister.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	
	private class FindPasword extends Thread {
		public void run() {
			UserData userData=new UserData(UserForgetPwdStepTwo.this);
			int status=userData.findPasswordByEmail(mText.getText().toString().trim());
			mHandler.sendEmptyMessage(status);
		}
	}
}
