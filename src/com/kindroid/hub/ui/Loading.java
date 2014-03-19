package com.kindroid.hub.ui;

import com.kindroid.hub.R;
import com.kindroid.hub.data.UserData;
import com.kindroid.hub.proto.DistributedClientProtoc.Client;
import com.kindroid.hub.utils.ClientInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;

public class Loading extends Activity {
	
	private Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			finish();
			Intent i=new Intent(Loading.this, HubCategory.class);
			startActivity(i);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		mHandler.sendEmptyMessageDelayed(0, 2000);
		new UserStatistics().start();
	}
	
	private class UserStatistics extends Thread {
		public void run() {
			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String imei = telephonyManager.getDeviceId();

			UserData data = new UserData(Loading.this);
			Client.Builder builder = Client.newBuilder();
			builder.setResID(ClientInfo.MARKET_RES_ID);
			builder.setManuer(Build.MANUFACTURER);
			builder.setModel(Build.MODEL);
			builder.setBuild(Build.DISPLAY);
			builder.setFingerprint(Build.FINGERPRINT);
			builder.setImei(imei);
			builder.setHost(Build.HOST);
			builder.setDevice(Build.DEVICE);
			builder.setBoard(Build.BOARD);
			builder.setBrand(Build.BRAND);
			builder.setOsversion(Build.VERSION.RELEASE);
			builder.setSdkversion(Build.VERSION.SDK_INT + "");
			builder.setHardware("unknown");
			data.userStatistics(builder);
		}
	}

}
