package com.kindroid.hub.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.kindroid.hub.R;

public class Utils {
	public static final String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String letterChar = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static String mDateFormat = "yyyy-MM-dd HH:mm";
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(mDateFormat, Locale.getDefault());
	
	/**
     * 检查网络
     */
	public static boolean checkNetwork(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();
		if (networkinfo != null) {
			if (networkinfo.isConnected()) {
				return true;
			}
		}
		return false;
	}
	

	public static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}
	
	/**
	 * Format published date of content.
	 * Here has a special rule according to current time. Such as:
	 * <p> when published time is not more than one minute from now, return the formatted string N second(s) ago
	 * <p> when published time is not more than one hour from now, return the formatted string N minute(s) ago
	 * <p> when published time is not more than one day from now, return the formatted string N hour(s) ago
	 * <p> when published time is more than one day from now, return the formatted string yyyy-MM-dd HH:mm
	 * @param milliseconds when content was submitted
 	 * @return formatted date expression.
	 */
	public static String getFormattedDateExpression(Context context, long milliseconds) {
		long currentTime = SystemClock.elapsedRealtime();
		int duration = (int) ((currentTime-milliseconds)/1000);
		if (duration < 60) {//n second(s) ago
			return context.getString(R.string.date_format_second_ago, duration);
		} else if ((duration = duration/60) < 60) {//n minute(s) ago
			return context.getString(R.string.date_format_minute_ago, duration);
		} else if ((duration = duration/60) < 24) {//n hour(s) ago
			return context.getString(R.string.date_format_hour_ago, duration);
		} 
		//yyyy-MM-dd HH:mm
		return dateFormat.format(new Date(milliseconds));
	}
	
	/**
	 * check email format
	 * @param email
	 * @return
	 */
	public static boolean checkEmail(String email) {
		if (TextUtils.isEmpty(email)) {
			return false;
		}
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9-_.]+?@([a-zA-Z0-9]+(?:\\.[a-zA-Z0-9-_]+){1,})$");
        return (pattern.matcher(email)).matches();
	}
	
	public static void hideKeyBoard(Context context,View view){
		InputMethodManager inputMethodManager=(InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	public static String generateMixString(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(allChar.charAt(random.nextInt(letterChar.length())));
        }
        return sb.toString();
    }
	
	/**
	 * 计算时间隔间
	 */
	public static String ddate(long sTime, Context context) {
		long time = sTime;
		Date currentTime = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String s = sf.format(new Date(time));
		long current = (currentTime.getTime() / 1000) * 1000;
		long second = (current - time) / 1000;
		long day = -1;
		if (second > 86400) {
			day = second / 86400;
		}
		if (day == -1) {
			if (second > 3600) {
				return context.getString(R.string.square_time_hour,second / 3600);
			} else if (second > 1800) {
				return context.getString(R.string.square_time_half_hour);
			} else if (second > 60) {
				return context.getString(R.string.square_time_minutus,second / 60);
			} else if (second > 0) {
				return context.getString(R.string.square_time_second, second);
			} else if (second == 0) {
				return context.getString(R.string.square_time_just);
			} else {
				return s;
			}
		} else if (day >= 1 && day < 8) {
			if (day == 1) {
				return context.getString(R.string.square_time_yesterday);
			} else if (day == 2) {
				return context.getString(R.string.square_time_day_before_yesterday);
			} else {
				return context.getString(R.string.square_time_day, day);
			}
		} else {
			return s;
		}
	}
	
	public static String getLocalIpAddress() {   
        try {   
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) { 
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("WifiPreference IpAddress", ex.toString());
        }
        return null; 
    }
	
	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}
	
	public static List<Map> createUrlMapList(String forwardTmpStr) {
		List<Map> resultMapList = new ArrayList<Map>();
		String faceRegx = "\\[img\\](.*?)\\[/img\\]";
		
		int faceImgLength = "[img][/img]".length();

		//match face image
		Pattern pattern = Pattern.compile(faceRegx);
		Matcher forwardMatcher = pattern.matcher(forwardTmpStr);
		int forwardIndex = 0;
	    while(forwardMatcher.find()) {
			Map indexMap = new HashMap();
			String url = forwardMatcher.group(1);
			int startIndex = forwardTmpStr.indexOf("[img]", forwardIndex);
			indexMap.put("start", startIndex);
			indexMap.put("url", url);
			indexMap.put("end", startIndex + url.length() + faceImgLength); //faceImgLength为[img][/img]长度
			forwardIndex = startIndex + url.length() + faceImgLength;
			forwardTmpStr = forwardTmpStr.replaceFirst(faceRegx, Utils.generateMixString(url.length() + faceImgLength));
			resultMapList.add(indexMap); 
		}
	    
	    //match title
	    String titleRegxTencent = "【(.*?)】";
		List<Map> titlesList = new ArrayList<Map>();
	    Pattern titlePattern = Pattern.compile(titleRegxTencent);
	    Matcher titlesMatcher = titlePattern.matcher(forwardTmpStr);
	    int index = 0;
	    while(titlesMatcher.find()) {
	    	
	    	String linkSinaEaseRegx = "(www\\.[!-~]+)|(https?://[!-~]+)";
	 	    List<Map> linkSinaList = new ArrayList<Map>();
	 	    Pattern linkSinaPattern = Pattern.compile(linkSinaEaseRegx);
	 	    Matcher linkSinaMatcher = linkSinaPattern.matcher(titlesMatcher.group(0));
	 	    int indexSina = 0;
	 	    if (linkSinaMatcher.find()) {//过滤掉带URL情况
	 	    	continue;
	 	    } else {
	 	    	
	 	    	Map indexMap = new HashMap();
	 	    	String midContent = titlesMatcher.group(1);
	 	    	int startIndex = forwardTmpStr.indexOf("【", index);
	 	    	indexMap.put("start", startIndex);
	 	    	indexMap.put("midTag", midContent);
	 	    	indexMap.put("end", startIndex + titlesMatcher.group(0).length()); //group(0)为【(.*?)】
	 	    	index = startIndex + titlesMatcher.group(0).length();
	 	    	forwardTmpStr = forwardTmpStr.replaceFirst(titleRegxTencent, Utils.generateMixString(titlesMatcher.group(0).length()));
	 	    	titlesList.add(indexMap); 
	 	    }
	    	
	    }
	    
	    //match topic
	    String topicRegxTencent = "#(.*?)#";
	    List<Map> topicsList = new ArrayList<Map>();
	    Pattern topicPattern = Pattern.compile(topicRegxTencent);
	    Matcher topicMatcher = topicPattern.matcher(forwardTmpStr);
	    int topicIndex = 0;
	    while(topicMatcher.find()) {
	    	Map indexMap = new HashMap();
	    	String topicContent = topicMatcher.group(1);
	    	int startIndex = forwardTmpStr.indexOf("#", topicIndex);
	    	indexMap.put("start", startIndex);
	    	indexMap.put("topicTag", topicContent);
	    	indexMap.put("end", startIndex + topicMatcher.group(0).length()); //group(0)为#(.*?)#
	    	topicIndex = startIndex + topicMatcher.group(0).length();
	    	forwardTmpStr = forwardTmpStr.replaceFirst(topicRegxTencent, Utils.generateMixString(topicMatcher.group(0).length()));
	    	topicsList.add(indexMap); 
	    }
	    
	    //tatch url
	    String linkSinaEaseRegx = "(www\\.[!-~]+)|(https?://[!-~]+)";
	    List<Map> linkSinaList = new ArrayList<Map>();
	    Pattern linkSinaPattern = Pattern.compile(linkSinaEaseRegx);
	    Matcher linkSinaMatcher = linkSinaPattern.matcher(forwardTmpStr);
	    int indexSina = 0;
	    while(linkSinaMatcher.find()) {
	    	Map indexMap = new HashMap();
	    	String url = linkSinaMatcher.group(0);
	    	int startIndex = forwardTmpStr.indexOf(url, indexSina);
	    	indexMap.put("start", startIndex);
	    	indexMap.put("linkUrl", url);
	    	indexMap.put("end", startIndex + linkSinaMatcher.group(0).length()); //group(0)为<(www\\.[!-~]+)|(https?://[!-~]+)
	    	indexSina = startIndex + linkSinaMatcher.group(0).length();
	    	forwardTmpStr = forwardTmpStr.replaceFirst(linkSinaEaseRegx, Utils.generateMixString(linkSinaMatcher.group(0).length()));
	    	linkSinaList.add(indexMap); 
	    }
	    
	    if (titlesList != null && titlesList.size() > 0) {
	    	resultMapList.addAll(titlesList);
	    }
	    
	    if (topicsList != null && topicsList.size() > 0) {
	    	resultMapList.addAll(topicsList);
	    }
	    if (linkSinaList != null && linkSinaList.size() > 0) {
	    	resultMapList.addAll(linkSinaList);
	    }
	    
	    return resultMapList;
	}
	
	public static String changeALink(String contentStr) {
		String linkRegxTencent = "<a href='(.*?)' target='_blank'>([^<]*)</a>".replaceAll("'", "\"");
	    Pattern linkTencentPattern = Pattern.compile(linkRegxTencent);
	    Matcher linkTencentMatcher = linkTencentPattern.matcher(contentStr);
	    while(linkTencentMatcher.find()) {
	    	contentStr = contentStr.replaceFirst(linkRegxTencent, linkTencentMatcher.group(1));
	    }
	    return contentStr; 
	}
	
	public static boolean storeImageToFile(Bitmap bitmap, String name) {
	    if(bitmap == null){
	        return false;
	    }
	    File dirFile = new File(Constant.IMAGE_DOWNLOAD_PATH);       
        if(!dirFile.exists()){
           dirFile.mkdirs();           
        }
	    File file = null;
	    RandomAccessFile accessFile = null;
	    String path = Constant.IMAGE_DOWNLOAD_PATH;
	    ByteArrayOutputStream steam = new ByteArrayOutputStream();
	    
	    bitmap.compress(Bitmap.CompressFormat.PNG, 100, steam);
	    byte[] buffer = steam.toByteArray();

	    try {
	        path = path + name + ".png";
	        file = new File(path);
	        accessFile = new RandomAccessFile(file, "rw");
	        accessFile.write(buffer);
	    } catch (Exception e) {
	    	e.printStackTrace();
	        return false;
	    }
	    try {
	        steam.close();
	        accessFile.close();
	    } catch (IOException e) {
	        return false;
	    }
	    
	    return true;
	}
}
