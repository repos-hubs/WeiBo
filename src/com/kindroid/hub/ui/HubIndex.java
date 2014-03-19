package com.kindroid.hub.ui;

import com.kindroid.hub.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class HubIndex extends BaseActivity {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_index);
		findViews();
		if(checkLogin()){
			finish();
			Intent intent=new Intent(HubIndex.this, HubMain.class);
			startActivity(intent);
		}
	}

	private void findViews(){
		ImageView loginTitle = (ImageView) findViewById(R.id.loginTitle);
		loginTitle.bringToFront();
		
		ImageView weiboLogin=(ImageView)findViewById(R.id.weiboLoginImageView);
		weiboLogin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(HubIndex.this, WeiboLogin.class);
				startActivity(intent);
			}
		});
		ImageView noLogin=(ImageView)findViewById(R.id.newBieImageView);
		noLogin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(HubIndex.this, HubMain.class);
				startActivity(intent);
			}
		});
	}
}
