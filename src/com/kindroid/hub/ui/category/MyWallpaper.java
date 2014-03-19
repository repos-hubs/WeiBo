package com.kindroid.hub.ui.category;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.kindroid.hub.R;

public class MyWallpaper extends Activity implements View.OnClickListener
{

	//壁纸首页标题
	private TextView categorySelect,toLocalView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.wallpaper_content);

		
		
		
	}
	
	@Override
	public void onClick(View arg0) 
	{
		Intent intent;
		if(arg0 instanceof TextView )
		{
			switch (arg0.getId()) {
			case R.id.to_online_view :
				
				intent = new Intent();
				intent.setClass(this, WallpaperMain.class);
				startActivity(intent);
				break;

			default:
				break;
			}
		}
		
	}

}
