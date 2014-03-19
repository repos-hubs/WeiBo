package com.kindroid.hub.ui;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.TribeAdapter;
import com.kindroid.hub.data.UserData;
import com.kindroid.hub.data.UserDefaultInfo;
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

public class UserModifyPwd extends Activity implements OnClickListener{
	
	private EditText mOldPassword;
	private EditText mNewPassword;
	private EditText mConfirmPassword;
	
	private TextView mSubmit;
	
	private ImageView mBackBtn;
	private ImageView mSearchBtn;
	
	private ProgressDialog mProgressDialog;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mProgressDialog.dismiss();
			switch (msg.what) {
			case -3:
				Toast.makeText(UserModifyPwd.this, R.string.user_alert_pwd_inconrect, Toast.LENGTH_SHORT).show();
				break;
			case -6:
				Toast.makeText(UserModifyPwd.this, R.string.user_register_email_format, Toast.LENGTH_SHORT).show();
				break;
			case -8:
				Toast.makeText(UserModifyPwd.this, R.string.user_alert_pwd_old, Toast.LENGTH_SHORT).show();
				break;
			case -7:
				Toast.makeText(UserModifyPwd.this, R.string.user_alert_pwd_inconrect, Toast.LENGTH_SHORT).show();
				break;
			case 10:
				Toast.makeText(UserModifyPwd.this, R.string.user_edit_pwd_success, Toast.LENGTH_SHORT).show();
				finish();
				break;
			case -10:
				Toast.makeText(UserModifyPwd.this, R.string.user_register_connect_error, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_modify_pwd);
		findViews();
	}
	
	private void findViews() {
		mOldPassword = (EditText) findViewById(R.id.oldPassword);
		mNewPassword = (EditText) findViewById(R.id.newPassword);
		mConfirmPassword = (EditText) findViewById(R.id.confirmPassword);
		
		mSubmit=(TextView)findViewById(R.id.userSubmit);
		mSubmit.setOnClickListener(this);

		mBackBtn = (ImageView) findViewById(R.id.btn_back);
		mBackBtn.setOnClickListener(this);
		mSearchBtn = (ImageView) findViewById(R.id.btn_search);
		mSearchBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.userSubmit:
			validParameter();
			break;
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_search:
			Intent intent = new Intent(this, AtMeSearchActivity.class);
			startActivity(intent);
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
	
	public void validParameter() {
		if (TextUtils.isEmpty(mOldPassword.getText().toString())) {
			Toast.makeText(this, R.string.user_edit_pwd_oldpwd_msg, Toast.LENGTH_SHORT).show();
			return;
		} else if (TextUtils.isEmpty(mNewPassword.getText().toString())) {
			Toast.makeText(this, R.string.user_edit_pwd_newpwd_msg, Toast.LENGTH_SHORT).show();
			return;
		} else if (TextUtils.isEmpty(mConfirmPassword.getText().toString())) {
			Toast.makeText(this, R.string.user_edit_pwd_confirmpwd_msg, Toast.LENGTH_SHORT).show();
			return;
		} else if(!mNewPassword.getText().toString().equals(mConfirmPassword.getText().toString())){
			Toast.makeText(this, R.string.user_register_repassword_not_match, Toast.LENGTH_SHORT).show();
			return;
		}
		Utils.hideKeyBoard(this, getCurrentFocus());
		showProgressDialog();
		new EditPassword().start();
		
	}
	
	public class EditPassword extends Thread {
		public void run() {
			UserData userData = new UserData(UserModifyPwd.this);
			int status = userData.editUserPassword(mOldPassword.getText()
					.toString().trim(),
					UserDefaultInfo.getUserToken(UserModifyPwd.this),
					mConfirmPassword.getText().toString());
			mHandler.sendEmptyMessage(status);
		}
	}
}
