package com.kindroid.hub.ui.category.wallpaper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;

public class Constant {
	
	
	
	 //------------- 
    
	/***
	 * 保存图片
	 */
	public static void insertWallpaper(Context paramContext,ContentValues values){
		 Uri localUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		 ContentResolver localContentResolver = paramContext.getContentResolver(); 
		 localContentResolver.insert(localUri, values);   
	}
	  
	/***
	 * 	 从SD 卡指定目录读取 图片
	 */
	public static List<String> findWallpapers(Context paramContext){
		List<String> mImagesName =new ArrayList<String>();
	    String[] arrayOfString = new String[1];
	    arrayOfString[0] = "_data";
	    Cursor localCursor = null;
	    try
	    {	    
	      String path=com.kindroid.hub.utils.Constant.WALLPAPERDOWNLOADPATH;
	      Uri localUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	      ContentResolver localContentResolver = paramContext.getContentResolver(); 
	      localCursor = localContentResolver.query(localUri, arrayOfString, "_data LIKE '%%"+path+"%%'", null, "date_added DESC");
	      mImagesName=new ArrayList<String>();
	      if (localCursor != null)
	      {	      
	        while (localCursor.moveToNext()){
	            String str = localCursor.getString(0);	            
	            mImagesName.add(str);		      
	        }
	      }
	    }
	    finally
	    {
	      if (localCursor != null)
	        localCursor.close();
	    }
	    return mImagesName;
	  }
	
	public static void setImage(ImageView image,String path){
		
		image.setImageBitmap(getThumbnailBitMap(path));
		
	}
	
	/***
	 * 图片压缩
	 * @param path 图片路径
	 * @return
	 */
	public static Bitmap getThumbnailBitMap(String path){		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		//获取原图  此时返回的bitmap 为空
		Bitmap bitmap=BitmapFactory.decodeFile(path,options);
		
		// 获得图片的宽高
	    int width = bitmap.getWidth();
	    int height = bitmap.getHeight();
	    // 设置想要的大小
	    int newWidth1 = 100;
	    int newHeight1 = 80;
	    // 计算缩放比例
	    float scaleWidth = ((float) newWidth1) / width;
	    float scaleHeight = ((float) newHeight1) / height;
	    // 取得想要缩放的matrix参数
	    Matrix matrix = new Matrix();
	    matrix.postScale(scaleWidth, scaleHeight);
	    // 得到新的图片
	    Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,true);
		return newbm;
	}	

	
	//删除图片
	public static  boolean removeWallpaper(Context paramContext, String paramString){	   
		if (TextUtils.isEmpty(paramString)){
			return false;
		}	     
		ContentResolver localContentResolver = paramContext.getContentResolver();
		Uri localUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		String[] arrayOfString = new String[1];
		arrayOfString[0] = paramString;
		if (localContentResolver.delete(localUri, "_data = ?", arrayOfString) > 0){
			return true;	        
		}
		
		return false;
	}
	
	
	
	public static Bitmap getBitMap(String url) throws Exception
	{
		byte[] data=getImage(url);
		System.out.println("===========data===="+data.length);
		
		return BitmapFactory.decodeByteArray(data, 0, data.length);
	}
	//出现异常 返回默认图片
	public static Bitmap getBitMap(byte[] image) 
	{
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}
	
	/**
	* 获取指定路径，的数据。
	* 
	* **/
	public static  byte[] getImage(String urlpath) throws Exception {
	
		URL url = new URL(urlpath);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();		  
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(6 * 1000);
		// 别超过10秒。
		if(conn.getResponseCode()==200){
			InputStream inputStream=conn.getInputStream();
		 return readStream(inputStream);
		}
		return null;
	}
	/**
	  * 读取数据 
	  * 输入流
	  * 
	  * */
	private static byte[] readStream(InputStream inStream) throws Exception {
	  ByteArrayOutputStream outstream=new ByteArrayOutputStream();
	  byte[] buffer=new byte[1024];
	  int len=-1;
	  while((len=inStream.read(buffer)) !=-1){
	   outstream.write(buffer, 0, len);
	  }
	  outstream.close();
	  inStream.close();
	  
	return outstream.toByteArray();
	}
}
