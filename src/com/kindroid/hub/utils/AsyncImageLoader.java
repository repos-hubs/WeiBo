package com.kindroid.hub.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

public class AsyncImageLoader {
	private static HashMap<String, SoftReference<Bitmap>> bitmapCache;
	private File cacheDir;
	private Context ctx;

	public AsyncImageLoader(Context context) {
		bitmapCache = new HashMap<String, SoftReference<Bitmap>>();
		
		  //Find the dir to save cached images
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(Environment.getExternalStorageDirectory(), "LazyList");
		} else {
			cacheDir = context.getCacheDir();
		}
        if(!cacheDir.exists())
            cacheDir.mkdirs();
		this.ctx = context;
	}

	public void freeBitmapFromIndex(String url) {
		if (bitmapCache.containsKey(url)) {
			SoftReference<Bitmap> softReference = bitmapCache.get(url);
			Bitmap Bitmap = softReference.get();
			if (Bitmap != null) {
				Bitmap.recycle();
			}
		}
	}

	public Bitmap loadBitmap(final String url, final ImageCallback callback) {
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				callback.imageLoaded((Bitmap) message.obj, url);
			}
		};
		bitmapCache.clear();
		System.gc();
		if (bitmapCache.containsKey(url)) {
			SoftReference<Bitmap> softReference = bitmapCache.get(url);
			Bitmap Bitmap = softReference.get();
			if (Bitmap != null) {
				return Bitmap;
			}
		} else {
			new Thread() {
				@Override
				public void run() {
//					Bitmap bitmap = loadBitmapFromUrl(url);
					Bitmap bitmap = getBitmap(url);
					if (bitmap != null) {
						bitmapCache.put(url, new SoftReference<Bitmap>(bitmap));
						Message message = handler.obtainMessage(0, bitmap);
						handler.sendMessage(message);
					}
				}
			}.start();
		}
		return null;
	}
	
	public static InputStream loadImageFromUrl(String url) {
		URL m;
		InputStream i = null;
		try {
			m = new URL(url);
			i = (InputStream) m.getContent();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return i;
	}
	
	public Bitmap getBitmap(String id) {
		File f = null;
		f = new File(cacheDir, "App");
		f.mkdir();
		File file = new File(f.getAbsolutePath(), id.hashCode() + "");
		// from web
		if (Utils.checkNetwork(ctx)) {
			try {
				InputStream inputStream = loadImageFromUrl(id);
				OutputStream os = new FileOutputStream(file);
				Utils.copyStream(inputStream, os);
				os.close();
				Bitmap bitmap = decodeFile(file);
				return bitmap;
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		} else {
			// from SD cache
			Bitmap b = decodeFile(file);
			if (b != null)
				return b;
		}
		return null;
	 }
	
	//decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE=250;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp/2 < REQUIRED_SIZE || height_tmp/2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        	
        } catch (OutOfMemoryError ex) {
        	ex.printStackTrace();
        }
        return null;
    }
	
	public static byte[] readStream(InputStream in) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while((len = in.read(buffer)) != -1) {
			outputStream.write(buffer, 0, len);
		}
		outputStream.close();
		in.close();
		return outputStream.toByteArray();
	}

	public interface ImageCallback {

		public void imageLoaded(Bitmap bitmap, String url);

		public void imageLoaded(Drawable drawable, String url);
		
		public void imageLoaded(Bitmap bitmap, int position, String url);
		
		public void imageLoaded(Drawable drawable, int position, String url);
	}
}
