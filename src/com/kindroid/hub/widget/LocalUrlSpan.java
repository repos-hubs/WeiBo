package com.kindroid.hub.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.style.ClickableSpan;
import android.view.View;

import com.kindroid.hub.ui.WeiboLinkActivity;

public class LocalUrlSpan extends ClickableSpan implements ParcelableSpan {
	private final static String TAG = "LocalUrlSpan";
	
	private String url;
	public LocalUrlSpan(String link) {
		url = link;
	}
	

	public void onClick(View widget) {
		Context ctx = widget.getContext();
		Intent intent = new Intent(ctx, WeiboLinkActivity.class);
		intent.putExtra("url", url);
		ctx.startActivity(intent);
	}


	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void writeToParcel(Parcel parcel, int i) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public int getSpanTypeId() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public String getUrl() {  
        return url;  
    } 
	
}
