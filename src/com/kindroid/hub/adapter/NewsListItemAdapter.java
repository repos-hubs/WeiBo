package com.kindroid.hub.adapter;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kindroid.hub.R;
import com.kindroid.hub.download.LazyImageLoader;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;
import com.kindroid.hub.ui.AsyncImageDataFlow;
import com.kindroid.hub.ui.NewsCategoryList;
import com.kindroid.hub.ui.NewsListActivity;
import com.kindroid.hub.ui.WeiboLinkActivity;
import com.kindroid.hub.utils.AsyncImageLoader;
import com.kindroid.hub.utils.AsyncImageLoader.ImageCallback;
import com.kindroid.hub.utils.ConvertUtils;
import com.kindroid.hub.utils.Utils;
import com.kindroid.hub.widget.LocalUrlSpan;

public class NewsListItemAdapter extends BaseAdapter {
	private final String TAG = "NewsListItemAdapter";
	public List<WeiboContent> list;
	public Context ctx;
	public int from = 0;
	public String fromSearch = "";
	private AsyncImageLoader imgLoader;
	private LazyImageLoader imageLoader;
	
	final String mimeType = "text/html";
	final String encoding = "utf-8";
	final int faceImgLength = "[img][/img]".length();
	final int linkLength = "[uri][/uri]".length();
	
	int mDataType;
	
	public NewsListItemAdapter(Context ctx, int from, int dataSource, String isfromSearch, List<WeiboContent> newsList) {
		this.list = newsList;
		this.ctx = ctx;
		this.from = from;
		imageLoader = new LazyImageLoader(ctx);
		fromSearch = isfromSearch;
		this.mDataType = dataSource;
	}
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup viewGroup) {
		View view = convertView;
		
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
			view = inflater.inflate(R.layout.hub_news_main_item, null);
		}
		
		final ImageView newsImage = (ImageView) view.findViewById(R.id.newsIconImageView);
		TextView newsTitle = (TextView) view.findViewById(R.id.newsTitleTextView);
		
		TextView newsTime = (TextView) view.findViewById(R.id.timeTextView);
		TextView fromTextView = (TextView) view.findViewById(R.id.fromTextView);
		final TextView newsContentTextView = (TextView) view.findViewById(R.id.newsContentsTextView);
		final TextView newsForwardTextView = (TextView) view.findViewById(R.id.newsForwardTextView);
		
		final ImageView forwardContentImageView = (ImageView) view.findViewById(R.id.newsForwardImageView);
		final ImageView newsConentImageView = (ImageView) view.findViewById(R.id.newsContentImageView);
		LinearLayout forwardLayout = (LinearLayout) view.findViewById(R.id.forwardLayout);
		
		final WeiboContent content = list.get(position);
		String newsTitleStr = content.getItem().getName();
		newsTitle.setText(newsTitleStr);
		String iconUrl = content.getItem().getIcon();
		imgLoader = new AsyncImageLoader(ctx);
		
		String sourceStr = content.getSource();
		Pattern name = Pattern.compile("<a.*?>(.*?)</a>", Pattern.DOTALL);
		Matcher fromMatcher = name.matcher(sourceStr);
		if (fromMatcher.find()) {
			fromTextView.setText(fromMatcher.group(1));
		}
		
		String newsContentStr = "";
		String newsForwardStr = "";
		String newsForwardUrl = "";
		String contentImgUrl = "";
		newsContentStr = content.getContent();
		if (content.hasRetweetedstatus()) {
			newsForwardStr = content.getRetweetedcontent();
			newsForwardUrl = content.getImg3().replace("thumbnail", "bmiddle");
//			newsForwardUrl = content.getImg3().replace("large", "thumbnail");
		} 
		if (!TextUtils.isEmpty(content.getImg1())) {
//			contentImgUrl = content.getImg1().replace("large", "thumbnail");
			contentImgUrl = content.getImg1().replace("thumbnail", "bmiddle");
		}
		
		if (TextUtils.isEmpty(newsForwardStr) && TextUtils.isEmpty(newsForwardUrl.trim())) {
			forwardLayout.setVisibility(View.GONE);
		} else {
			forwardLayout.setVisibility(View.VISIBLE);
		}

		// initialize weibo content text view
		String newsContentTextViewStr = Utils.changeALink(newsContentStr);
		
		List contentMapList = Utils.createUrlMapList(newsContentTextViewStr);
		
		final SpannableString ss = new SpannableString(newsContentTextViewStr);  
		if (contentMapList != null && contentMapList.size() > 0) {
			for (int i = 0; i < contentMapList.size(); i++) {
				final Map contentMap = (Map) contentMapList.get(i);
				String tmpUrl = String.valueOf(contentMap.get("url"));
				if (contentMap.get("url") != null) { //match face url
					
					// start thread to load image
					Bitmap img = this.imgLoader.loadBitmap(String.valueOf(contentMap.get("url")), new ImageCallback() {
						@Override
						public void imageLoaded(Bitmap bitmap, String url) {
							
							ImageSpan span = new ImageSpan(bitmap, ImageSpan.ALIGN_BASELINE);
							int start = (Integer) contentMap.get("start");
							int end = (Integer) contentMap.get("end");
							ss.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
							newsContentTextView.setText(ss);  
							newsContentTextView.setMovementMethod(LinkMovementMethod.getInstance());
							newsContentTextView.invalidate();
						}
						
						public void imageLoaded(Drawable drawable, String url) {
						}
						@Override
						public void imageLoaded(Bitmap bitmap, int position, String url) {
						}
						@Override
						public void imageLoaded(Drawable drawable, int position, String url) {
						}
					});
					
					if (img != null) {
						
						Drawable d = ConvertUtils.bitmapToDrawable(img);
						d.setBounds(1, 1, d.getIntrinsicWidth(), d.getIntrinsicHeight());  
						ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
						int start = (Integer) contentMap.get("start");
						int end = (Integer) contentMap.get("end");
						ss.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
					}
				} else if (contentMap.get("linkUrl") != null) { //match link
					int start = (Integer) contentMap.get("start");
					int end = (Integer) contentMap.get("end");
					String linkUrl = String.valueOf(contentMap.get("linkUrl"));
					ss.setSpan(new LocalUrlSpan(linkUrl), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					
					newsContentTextView.setText(ss);  
					newsContentTextView.setMovementMethod(LinkMovementMethod.getInstance());
					newsContentTextView.invalidate();
				} else if (contentMap.get("midTag") != null) {
					int start = (Integer) contentMap.get("start");
					int end = (Integer) contentMap.get("end");
					ss.setSpan(new ForegroundColorSpan(ctx.getResources().getColor(R.color.darkBlue)), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
					
					newsContentTextView.setText(ss);  
					newsContentTextView.setMovementMethod(LinkMovementMethod.getInstance());
					newsContentTextView.invalidate();
				} else if (contentMap.get("topicTag") != null) {
					int start = (Integer) contentMap.get("start");
					int end = (Integer) contentMap.get("end");
					ss.setSpan(new ForegroundColorSpan(ctx.getResources().getColor(R.color.dark_green)), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
					
					newsContentTextView.setText(ss);  
					newsContentTextView.setMovementMethod(LinkMovementMethod.getInstance());
					newsContentTextView.invalidate();
				}
			}
		}
	    newsContentTextView.setText(ss);  
	    newsContentTextView.setMovementMethod(LinkMovementMethod.getInstance());
		
	    //initialize forward content text view
	    String forwardTmpStr = Utils.changeALink(newsForwardStr);
	    List<Map> forwardMapList = Utils.createUrlMapList(forwardTmpStr);
		
		final SpannableString forwardSpannable = new SpannableString(forwardTmpStr);  
		if (forwardMapList != null && forwardMapList.size() > 0) {
			for (int i = 0; i < forwardMapList.size(); i++) {
				final Map contentMap = (Map) forwardMapList.get(i);
				// start thread to load image
				String tmpUrl = String.valueOf(contentMap.get("url"));
				if (contentMap.get("url") != null) { //match face url
					
					Bitmap img = this.imgLoader.loadBitmap(String.valueOf(contentMap.get("url")), new ImageCallback() {
						@Override
						public void imageLoaded(Bitmap bitmap, String url) {
							ImageSpan span = new ImageSpan(bitmap, ImageSpan.ALIGN_BASELINE);
							int start = (Integer) contentMap.get("start");
							int end = (Integer) contentMap.get("end");
							forwardSpannable.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
							
							newsForwardTextView.setText(forwardSpannable);  
							newsForwardTextView.setMovementMethod(LinkMovementMethod.getInstance());
							newsForwardTextView.invalidate();
						}
						
						public void imageLoaded(Drawable drawable, String url) {
						}
						@Override
						public void imageLoaded(Bitmap bitmap, int position, String url) {
							
						}
						@Override
						public void imageLoaded(Drawable drawable, int position, String url) {
						}
					});
					
					if (img != null) {
						
						Drawable d = ConvertUtils.bitmapToDrawable(img);
						d.setBounds(1, 1, d.getIntrinsicWidth(), d.getIntrinsicHeight());  
						ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
						int start = (Integer) contentMap.get("start");
						int end = (Integer) contentMap.get("end");
						forwardSpannable.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
					}
				} else if (contentMap.get("linkUrl") != null) { //match link
					int start = (Integer) contentMap.get("start");
					int end = (Integer) contentMap.get("end");
					String linkUrl = String.valueOf(contentMap.get("linkUrl"));
					forwardSpannable.setSpan(new LocalUrlSpan(linkUrl), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					
					newsForwardTextView.setText(forwardSpannable);  
					newsForwardTextView.setMovementMethod(LinkMovementMethod.getInstance());
					newsForwardTextView.invalidate();
				} else if (contentMap.get("midTag") != null) {
					int start = (Integer) contentMap.get("start");
					int end = (Integer) contentMap.get("end");
					forwardSpannable.setSpan(new ForegroundColorSpan(ctx.getResources().getColor(R.color.darkBlue)), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
					
					newsForwardTextView.setText(forwardSpannable);  
					newsForwardTextView.setMovementMethod(LinkMovementMethod.getInstance());
					newsForwardTextView.invalidate();
				} else if (contentMap.get("topicTag") != null) {
					int start = (Integer) contentMap.get("start");
					int end = (Integer) contentMap.get("end");
					forwardSpannable.setSpan(new ForegroundColorSpan(ctx.getResources().getColor(R.color.dark_green)), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
					
					newsForwardTextView.setText(forwardSpannable);  
					newsForwardTextView.setMovementMethod(LinkMovementMethod.getInstance());
					newsForwardTextView.invalidate();
				}
			}
		}
		newsForwardTextView.setText(forwardSpannable);  
		newsForwardTextView.setMovementMethod(LinkMovementMethod.getInstance());
	    if (TextUtils.isEmpty(newsForwardStr)) {
			newsForwardTextView.setVisibility(View.GONE);
		} else {
			newsForwardTextView.setVisibility(View.VISIBLE);
		}
		
		//weibo's avatar
	    newsImage.setTag(iconUrl);
	    imageLoader.displayUserIcon(iconUrl, (Activity) ctx, newsImage);
	    
		if ("no".equals(fromSearch)) {
			newsImage.setOnClickListener(new UserClick(position));
		}

		String time = content.getTimeLeft();
		newsTime.setText(time);
		if (!TextUtils.isEmpty(contentImgUrl.trim())) {
			Log.d(TAG, "content image:" + contentImgUrl);
			//image of news's content
			newsConentImageView.setVisibility(View.VISIBLE);
			newsConentImageView.setTag(contentImgUrl);
			imageLoader.displayAppIcon(contentImgUrl, (Activity) ctx, newsConentImageView);
			newsConentImageView.setOnClickListener(new ImageOnClick(content, position, false));
		} else {
			newsConentImageView.setVisibility(View.GONE);
		}
		//image of forward content
		if (!TextUtils.isEmpty(newsForwardUrl.trim())) {
			Log.d(TAG, "content image:" + newsForwardUrl);
			forwardContentImageView.setOnClickListener(new ImageOnClick(content, position, true));
			forwardContentImageView.setTag(newsForwardUrl);
			imageLoader.displayAppIcon(newsForwardUrl, (Activity) ctx, forwardContentImageView);
		}else{
			forwardContentImageView.setVisibility(View.GONE);
		}
		
		return view;
	}
	
	class UserClick implements View.OnClickListener{
		private int pos;

		public UserClick(int position) {
			this.pos = position;
		}
		public void onClick(View view) {
			Intent intent = new Intent(ctx, NewsCategoryList.class);
			intent.putExtra("dataSource", mDataType);
			intent.putExtra("from", from);
			intent.putExtra("itemName", NewsListActivity.getSubTitle(from,ctx));
			if (mDataType == 1) {
				intent.putExtra("categoryId", list.get(pos).getItem().getLocName());
			} else {
				intent.putExtra("categoryId", list.get(pos).getItem().getSinaId() + "");
			}
			ctx.startActivity(intent);
		}
	}

	class ImageOnClick implements View.OnClickListener {
		WeiboContent weiboTmp;
		int position;
		boolean isForward;
		public ImageOnClick(WeiboContent weibo, int pos, boolean forward) {
			weiboTmp = weibo;
			this.position = pos;
			this.isForward = forward;
		}
		@Override
		public void onClick(View view) {
			/*if (isForward) {
				loadPhoto(weiboTmp);
			} else {*/
				Intent intent = new Intent(ctx, AsyncImageDataFlow.class);
				intent.putExtra("from", from);
				intent.putExtra("dataSource", mDataType);
				intent.putExtra("fromTag", "item");
				intent.putExtra("position", position);
				if ("yes".equals(fromSearch)) {
					if (mDataType == 1) {
						intent.putExtra("categoryId", list.get(position).getItem().getLocName());
					} else {
						intent.putExtra("categoryId", list.get(position).getItem().getSinaId() + "");
					}
				}
				ctx.startActivity(intent);
//			}
		}
	}
	//pop up large photo
	private void loadPhoto(WeiboContent weibo) {
	    LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
	    final Dialog imageDialog = new Dialog(ctx, R.style.pop_up_dialog);   

	    View layout = inflater.inflate(R.layout.pop_up_image, null);
	    final ImageView image = (ImageView) layout.findViewById(R.id.fullimage);
	      
	    String contentImgUrl = "";
		if (weibo.hasRetweetedstatus()) {
			contentImgUrl = weibo.getImg3().replace("thumbnail", "bmiddle");
		} else {
			contentImgUrl = weibo.getImg1().replace("thumbnail", "bmiddle");
		}
		Bitmap contentImage = imgLoader.loadBitmap(contentImgUrl, new ImageCallback() {
			@Override
			public void imageLoaded(Bitmap bitmap, String url) {
				image.setImageBitmap(bitmap);
			}
			
			public void imageLoaded(Drawable drawable, String url) {
			}
			@Override
			public void imageLoaded(Bitmap bitmap, int position, String url) {
				
			}
			@Override
			public void imageLoaded(Drawable drawable, int position, String url) {
			}
		});
	    if (contentImage != null) {
	    	image.setImageBitmap(contentImage);
	    }
	    imageDialog.setContentView(layout); 
	    imageDialog.setTitle("");
	    image.setOnClickListener(
	    	new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					imageDialog.dismiss();
				}
			}
	    );

	    imageDialog.show();     
	}
}
