package com.kindroid.hub.ui.category.wallpaper;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.kindroid.hub.R;

/***
 * 设置背景图片
 * 
 * @author huaiyu.zhao
 * 
 */
public class WallpaperChooser extends Activity implements
		AdapterView.OnItemSelectedListener, OnClickListener
{
	private static final String TAG = "com.kindroid.hub.ui.category.wallpaper.WallpaperChooser";
	private Gallery mGallery;
	private ImageView mImageView;
	private boolean mIsWallpaperSet;
	private Bitmap mBitmap;	
	private WallpaperLoader mLoader;
	private List<String> mImagesName;//原图
	
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);	
		
		
		//判断SD 卡是否存在		
		if(!Environment.getExternalStorageState().equals("mounted")){
			//SD 卡正在使用
			System.out.println("============SD 卡正在使用");
			setContentView(R.layout.wallpaper_prompt);
			TextView text=(TextView)findViewById(R.id.wallpaper_prompt);
			text.setText(R.string.wallpaper_msg_issd);
			
		}else{
			//从 SD 卡加载图片
			mImagesName=com.kindroid.hub.ui.category.wallpaper.Constant.findWallpapers(this);
			if(mImagesName==null || mImagesName.isEmpty()){
				//提示未下载图片
				System.out.println("=========未下载图片 提示去下载");
				setContentView(R.layout.wallpaper_prompt);
				TextView text=(TextView)findViewById(R.id.wallpaper_prompt);
				text.setText(R.string.wallpaper_msg_notdownload);
			}else{
				
				setContentView(R.layout.wallpaper_chooser);
				mGallery = (Gallery) findViewById(R.id.gallery);
				// 设置图片适配器
				mGallery.setAdapter(new ImageAdapter(this));
				// 设置监听
				mGallery.setOnItemSelectedListener(this);
				mGallery.setCallbackDuringFling(false);		
				findViewById(R.id.set).setOnClickListener(this);		
				mImageView = (ImageView) findViewById(R.id.wallpaper);
			}
		}
	}


	@Override
	protected void onResume()
	{
		super.onResume();
		mIsWallpaperSet = false;
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (mLoader != null
				&& mLoader.getStatus() != WallpaperLoader.Status.FINISHED)
		{
			mLoader.cancel(true);
			mLoader = null;
		}
	}
	
	public void onItemSelected(AdapterView parent, View v, int position, long id)
	{
		if (mLoader != null
				&& mLoader.getStatus() != WallpaperLoader.Status.FINISHED)
		{
			mLoader.cancel();
		}
		mLoader = (WallpaperLoader) new WallpaperLoader().execute(position);
	}
	
	/*
	 * When using touch if you tap an image it triggers both the onItemClick and
	 * the onTouchEvent causing the wallpaper to be set twice. Ensure we only
	 * set the wallpaper once.
	 * 采用触摸时，如果你点击它触发的图像都onItemClick和onTouchEvent导致两次被设置壁纸。确保我们只设置壁纸一次
	 */
	private void selectWallpaper(int position)
	{
		if (mIsWallpaperSet)
		{
			return;
		}
		
		mIsWallpaperSet = true;
		try
		{
			//原图
			Bitmap bmap = BitmapFactory.decodeFile(mImagesName.get(position));
			//wpm.setBitmap(bmap);
			this.setWallpaper(bmap);
			//同步其它应用
			setResult(RESULT_OK);
			finish();
		} catch (IOException e)
		{
			Log.e(TAG, "Failed to set wallpaper: " + e);
		}
	}
	
	public void onNothingSelected(AdapterView parent)
	{
		
	}
	
	/***
	 * 图片适配器
	 * 
	 * @author huaiyu.zhao
	 * 
	 */
	
	private class ImageAdapter extends BaseAdapter
	{
		private LayoutInflater mLayoutInflater;
		
		ImageAdapter(WallpaperChooser context)
		{
			mLayoutInflater = context.getLayoutInflater();
		}
		
		public int getCount()
		{
			return mImagesName.size();
		}
		
		public Object getItem(int position)
		{
			return position;
		}
		
		public long getItemId(int position)
		{
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ImageView image;
			if (convertView == null)
			{
				image = (ImageView) mLayoutInflater.inflate(R.layout.wallpaper_item, parent, false);
			} else
			{
				image = (ImageView) convertView;
			}
			Drawable thumbDrawable;
			
			Bitmap bmap = com.kindroid.hub.ui.category.wallpaper.Constant.getThumbnailBitMap(mImagesName.get(position));
			//Bitmap bmap = BitmapFactory.decodeByteArray(mThumbsName.get(position), 0, mThumbsName.get(position).length);
			image.setImageBitmap(bmap);
			thumbDrawable = Drawable.createFromPath(mImagesName.get(position));
			
			if (thumbDrawable != null)
			{
				thumbDrawable.setDither(true);
			} else
			{
				Log.e(TAG, "Error decoding thumbnail resId= for wallpaper #"+ position);
			}
			return image;
		}
	}
	
	public void onClick(View v)
	{
		selectWallpaper(mGallery.getSelectedItemPosition());
	}
	
	class WallpaperLoader extends AsyncTask<Integer, Void, Bitmap>
	{
		BitmapFactory.Options mOptions;
		
		WallpaperLoader()
		{
			mOptions = new BitmapFactory.Options();
			mOptions.inDither = false;
			mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
		}
		
		protected Bitmap doInBackground(Integer... params)
		{
			if (isCancelled())
				return null;
			try
			{
				return BitmapFactory.decodeFile(mImagesName.get(params[0]));
			} catch (OutOfMemoryError e)
			{
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(Bitmap b)
		{
			if (b == null)
				return;
			if (!isCancelled() && !mOptions.mCancel)
			{
				// Help the GC
				if (mBitmap != null)
				{
					mBitmap.recycle();
				}
				final ImageView view = mImageView;
				view.setImageBitmap(b);
				mBitmap = b;
				final Drawable drawable = view.getDrawable();
				drawable.setFilterBitmap(true);
				drawable.setDither(true);
				view.postInvalidate();
				mLoader = null;
			} else
			{
				b.recycle();
			}
		}
		
		void cancel()
		{
			mOptions.requestCancelDecode();
			super.cancel(true);
		}
	}
}