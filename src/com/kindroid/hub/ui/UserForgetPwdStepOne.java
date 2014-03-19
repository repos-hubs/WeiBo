package com.kindroid.hub.ui;

import com.kindroid.hub.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class UserForgetPwdStepOne extends Activity implements OnClickListener{
	
	private ImageView mUserRegister;
	
	private ImageView mEmail;
	private ImageView mPhone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_forget_pwd_step_one);
		findViews();
	}
	
	private void findViews(){
		ImageView loginTitle = (ImageView) findViewById(R.id.loginTitle);
		loginTitle.bringToFront();
		
		mUserRegister = (ImageView) findViewById(R.id.userRegister);
		mUserRegister.setOnClickListener(this);

		mEmail = (ImageView) findViewById(R.id.findPwdByEmail);
		mEmail.setOnClickListener(this);
		mPhone = (ImageView) findViewById(R.id.findPwdByPhone);
		mPhone.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.userRegister:
			Intent intent = new Intent(this, UserRegister.class);
			startActivity(intent);
			break;
		case R.id.findPwdByEmail:
			Intent intentEmail = new Intent(this, UserForgetPwdStepTwo.class);
			intentEmail.putExtra(UserForgetPwdStepTwo.FIND_TYPE, 1);
			startActivity(intentEmail);
			break;
		case R.id.findPwdByPhone:
			Intent intentPhone = new Intent(this, UserForgetPwdStepTwo.class);
			intentPhone.putExtra(UserForgetPwdStepTwo.FIND_TYPE, 2);
			startActivity(intentPhone);
			break;
		default:
			break;
		}
	}

}
