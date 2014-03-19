package com.kindroid.hub.ui;

import com.kindroid.hub.R;
import com.kindroid.hub.data.UserDefaultInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

public class UserLoginDialogTips extends Activity implements OnClickListener {
	
	private ImageView mRegiser;
	private ImageView mGo;
	
	private ImageView mCheck;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_login_dialog_tips);
		findViews();
	}
	
	private void findViews() {
		mRegiser = (ImageView) findViewById(R.id.register);
		mRegiser.setOnClickListener(this);
		mGo = (ImageView) findViewById(R.id.go);
		mGo.setOnClickListener(this);
		mCheck = (ImageView) findViewById(R.id.checkSwitch);
		mCheck.setOnClickListener(this);
		
		if(UserDefaultInfo.getUserTips(this)){
			mCheck.setImageResource(R.drawable.check_switch_on);
			mCheck.setTag(R.drawable.check_switch_on);
		}else{
			mCheck.setTag(R.drawable.check_switch);
			mCheck.setImageResource(R.drawable.check_switch);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.register:
			finish();
			Intent intent = new Intent(this, UserRegister.class);
			startActivity(intent);
			break;
		case R.id.go:
			finish();
			break;
		case R.id.checkSwitch:
			toggleCheck();
			break;
		default:
			break;
		}
	}
	
	private void toggleCheck() {
		int id = Integer.parseInt(mCheck.getTag().toString());
		if (id == R.drawable.check_switch) {
			mCheck.setImageResource(R.drawable.check_switch_on);
			mCheck.setTag(R.drawable.check_switch_on);
			UserDefaultInfo.setUserTips(this, true);
		} else if (id == R.drawable.check_switch_on) {
			mCheck.setImageResource(R.drawable.check_switch);
			mCheck.setTag(R.drawable.check_switch);
			UserDefaultInfo.setUserTips(this, false);
		}
	}

}
