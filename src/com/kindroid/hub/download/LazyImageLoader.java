package com.kindroid.hub.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Stack;

import com.kindroid.hub.R;
import com.kindroid.hub.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class LazyImageLoader {

	 //the simplest in-memory cache implementation. This should be replaced with something like SoftReference or BitmapOptions.inPurgeable(since 1.6)
	public static HashMap<String, Bitmap> cache = new HashMap<String, Bitmap>();
	public static HashMap<String, SoftReference<Bitmap>> appCache = new HashMap<String, SoftReference<Bitmap>>();
    
    private File cacheDir;
    private Context ctx;
    
    /**
     * 
     * @param context
     * @param type type=1,type=2,1:代表用户头像，2:app图标
     */
    public LazyImageLoader(Context context){
        //Make the background thead low priority. This way it will not affect the UI performance
        photoLoaderThread.setPriority(Thread.NORM_PRIORITY-1);
        
        //Find the dir to save cached images
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(),"LazyList");
		} else {
			cacheDir = context.getCacheDir();
		}
        if(!cacheDir.exists())
            cacheDir.mkdirs();
		this.ctx = context;
    }
    
	public void displayUserIcon(String id, Activity activity, ImageView imageView) {
		if (cache.containsKey(id)) {
			if(cache.get(id)!=null){
				imageView.setImageBitmap(cache.get(id));
			}else{
				imageView.setImageResource(R.drawable.user_default);
			}
		} else {
			queuePhoto(id, activity, imageView,1);
			imageView.setImageResource(R.drawable.user_default);
		}
	}
	
	public void displayAppIcon(String id, Activity activity, ImageView imageView) {
		appCache.clear();
		if (appCache.containsKey(id)) {
			if (appCache.get(id) != null) {
				imageView.setImageBitmap(appCache.get(id).get());
			} else {
				imageView.setImageBitmap(null);
			}
		} else {
			queuePhoto(id, activity, imageView,2);
			imageView.setImageResource(R.drawable.pic_default);
		}
	}
	
	public void displayAppIcon(String id, Activity activity, ImageView imageView, boolean displayDefault) {
		if (appCache.containsKey(id)) {
			if (appCache.get(id) != null) {
				imageView.setImageBitmap(appCache.get(id).get());
			} else {
				imageView.setImageBitmap(null);
			}
		} else {
			queuePhoto(id, activity, imageView,2);
			if (displayDefault) {
				imageView.setImageResource(R.drawable.pic_default);
			}
		}
	}
        
    private void queuePhoto(String id, Activity activity, ImageView imageView,int type)
    {
        //This ImageView may be used for other images before. So there may be some old tasks in the queue. We need to discard them. 
        photosQueue.Clean(imageView);
        PhotoToLoad p=new PhotoToLoad(id, imageView,type);
        synchronized(photosQueue.photosToLoad){
            photosQueue.photosToLoad.push(p);
            photosQueue.photosToLoad.notifyAll();
        }
        
        //start thread if it's not started yet
        if(photoLoaderThread.getState()==Thread.State.NEW)
            photoLoaderThread.start();
    }
    
    public Bitmap getBitmap(int type,String id){
		File f = null;
		if (type == 1) {
			f = new File(cacheDir, "Avatar");
		} else if (type == 2) {
			f = new File(cacheDir, "App");
		}
		f.mkdir();

		File file = new File(f.getAbsolutePath(), id.hashCode()+"");

		// from web
		if (Utils.checkNetwork(ctx)) {
			try {
				InputStream inputStream = loadImageFromUrl(id);
				//Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
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
    
	public Bitmap getBitmapFromFile(int type, String id) {
		File f = null;
		if (type == 1) {
			f = new File(cacheDir, "Avatar");
		} else if (type == 2) {
			f = new File(cacheDir, "App");
		}
		File file = new File(f.getAbsolutePath(), id.hashCode() + "");
		Bitmap b = decodeFile(file);
		if (b != null)
			return b;
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

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE=250;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        catch(OutOfMemoryError ex){
        	System.gc();
        	ex.printStackTrace();
        }
        return null;
    }
    
	// Task for the queue
	private class PhotoToLoad {
		public String id;
		public ImageView imageView;
		public int type;

		public PhotoToLoad(String u, ImageView i, int type) {
			id = u;
			imageView = i;
			this.type = type;
		}
	}
    
    PhotosQueue photosQueue=new PhotosQueue();
    
    public void stopThread()
    {
        photoLoaderThread.interrupt();
    }
    
    //stores list of photos to download
    class PhotosQueue
    {
        private Stack<PhotoToLoad> photosToLoad=new Stack<PhotoToLoad>();
        
        //removes all instances of this ImageView
        public void Clean(ImageView image)
        {
            for(int j=0 ;j<photosToLoad.size();){
                if(photosToLoad.get(j).imageView==image)
                    photosToLoad.remove(j);
                else
                    ++j;
            }
        }
    }
    
	class PhotosLoader extends Thread {
		public void run() {
			try {
				while (true) {
					// thread waits until there are any images to load in the
					// queue
					if (photosQueue.photosToLoad.size() == 0)
						synchronized (photosQueue.photosToLoad) {
							photosQueue.photosToLoad.wait();
						}
					if (photosQueue.photosToLoad.size() != 0) {
						PhotoToLoad photoToLoad;
						synchronized (photosQueue.photosToLoad) {
							photoToLoad = photosQueue.photosToLoad.pop();
						}
						Bitmap bmp = getBitmap(photoToLoad.type, photoToLoad.id);
						if (photoToLoad.type == 1) {
							cache.put(photoToLoad.id,bmp);
						} else if (photoToLoad.type == 2) {
							appCache.put(photoToLoad.id,new SoftReference<Bitmap>(bmp));
						}
						Object tag = photoToLoad.imageView.getTag();
						if (tag != null&& (tag.toString()).equals(photoToLoad.id)) {
							BitmapDisplayer bd = new BitmapDisplayer(bmp,photoToLoad.imageView,photoToLoad.type,-1);
							Activity a = (Activity) photoToLoad.imageView.getContext();
							a.runOnUiThread(bd);
						}
					}
					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
				// allow thread to exit
			}
		}
	}
    
    PhotosLoader photoLoaderThread=new PhotosLoader();
    
	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		int id;
		Bitmap bitmap;
		ImageView imageView;
		int type;

		public BitmapDisplayer(Bitmap b, ImageView i, int type,int id) {
			bitmap = b;
			imageView = i;
			this.type = type;
			this.id = id;
		}

		public void run() {
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
			} else {
				if (type == 1) {
					imageView.setImageResource(R.drawable.user_default);
				} else if (type == 2) {
					imageView.setImageBitmap(null);
				}
			}
		}
	}

    public void clearCache() {
        //clear memory cache
        cache.clear();
        appCache.clear();
        //clear SD cache
        File[] files=cacheDir.listFiles();
        for(File f:files)
            f.delete();
    }
}
