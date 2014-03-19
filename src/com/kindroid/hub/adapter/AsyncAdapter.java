package com.kindroid.hub.adapter;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.taptwo.android.widget.TitleProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kindroid.hub.R;
import com.kindroid.hub.download.LazyImageLoader;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;
import com.kindroid.hub.utils.AsyncImageLoader;
import com.kindroid.hub.utils.AsyncImageLoader.ImageCallback;
import com.kindroid.hub.utils.Constant;
import com.kindroid.hub.utils.ConvertUtils;
import com.kindroid.hub.utils.Utils;
import com.kindroid.hub.widget.LocalUrlSpan;


public class AsyncAdapter extends BaseAdapter implements TitleProvider {
	private final String TAG = "AsyncAdapter";
	private LayoutInflater mInflater;
	
	public List<WeiboContent> list;
	public Context ctx;
	public int from = 0;
	private LazyImageLoader imageLoader; //cache in file
	AsyncImageLoader imgLoader;
	private Handler dataHandler;
	
	private Bitmap tmpBitMap;
	
	private class ViewHolder {
		ProgressBar mProgressBar;
		View mContent;
		ImageView weiboImageView;
		
		LinearLayout weiboLayout;
		LinearLayout forwardLayout;
		TextView contentTextView;
		TextView forwardTextView;
		Button saveButton;
		Button mailButton;
		RelativeLayout saveLayout;
		
	}
	
	
	public AsyncAdapter(Context context, List<WeiboContent> newsList, Handler handler) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.ctx = context;
		imageLoader = new LazyImageLoader(ctx);
		this.list = newsList;
//		prepareDates();
		imgLoader = new AsyncImageLoader(context);
		this.dataHandler = handler;
	}
	
	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position; 
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return drawView(position, convertView);
	}

	private View drawView(int position, View view) {
		ViewHolder holder = new ViewHolder();
		
		if(view == null) {
			view = mInflater.inflate(R.layout.hub_news_image_item_view, null);
			holder.mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
			holder.weiboImageView = (ImageView) view.findViewById(R.id.weiboImageView);
			holder.mContent = (View) view.findViewById(R.id.content);
			
			//weibo views
			holder.weiboLayout = (LinearLayout) view.findViewById(R.id.weiboLayout);
			holder.contentTextView = (TextView) view.findViewById(R.id.newsContentsTextView);
			holder.forwardLayout = (LinearLayout) view.findViewById(R.id.forwardLayout);
			holder.forwardTextView = (TextView) view.findViewById(R.id.newsForwardTextView);
			holder.saveButton = (Button) view.findViewById(R.id.btn_save);
			holder.mailButton = (Button) view.findViewById(R.id.btn_mail);
			holder.saveLayout = (RelativeLayout) view.findViewById(R.id.title_bar);
			
			holder.weiboLayout.setTag("weiboLayout");
			view.setTag(holder);
			
			holder.weiboImageView.setOnClickListener(new ViewClick(holder, true));
			holder.contentTextView.setOnClickListener(new ViewClick(holder, false));
			holder.forwardTextView.setOnClickListener(new ViewClick(holder, false));
		} else {
			holder = (ViewHolder) view.getTag();
		}		


		final WeiboContent content = list.get(position);
		if (content != null) {
			setBitMap(holder, content);
			setWeiboConent(holder, content);
			setWeiboForward(holder, content);
			
		} else {
			holder.mContent.setVisibility(View.GONE);
			holder.mProgressBar.setVisibility(View.VISIBLE);
		}
	
		return view;
	}

	@Override
	public String getTitle(int i) {
		return null;
	}
	
	class SaveImg implements View.OnClickListener {

		Handler mHandler;
		Bitmap mBitmap;
		WeiboContent mcontent;
		boolean isSendingMail;
		public SaveImg(Handler handler, Bitmap bitmap, WeiboContent content, boolean sendMail) {
			this.mHandler = handler;
			this.mBitmap = bitmap;
			this.mcontent = content;
			this.isSendingMail = sendMail;
		}
		
		@Override
		public void onClick(View view) {
			Message msg = mHandler.obtainMessage();
			if (!Environment.getExternalStorageState().equals("mounted")) {
				msg.arg1 = 1;
			} else {
				if (isSendingMail) {
					sendMail(mcontent);
				} else {
					
					boolean isSuccess = Utils.storeImageToFile(mBitmap, mcontent.getWeibocontentId());
					if (isSuccess) {
						msg.arg1 = 2;
					} else {
						msg.arg1 = 3;
					}
				}
			}
			mHandler.sendMessage(msg);
			
		}
		
	}
	
	class ViewClick implements View.OnClickListener {
		ViewHolder holder;
		boolean tag;
		public ViewClick(ViewHolder holders, boolean flag) {
			this.holder = holders;
			this.tag = flag;
		}
		@Override
		public void onClick(View view) {
			if (tag) {
				holder.weiboLayout.setVisibility(View.VISIBLE);
				holder.saveLayout.setVisibility(View.VISIBLE);
			} else {
				holder.weiboLayout.setVisibility(View.INVISIBLE);
				holder.saveLayout.setVisibility(View.INVISIBLE);
			}
		}
		
	}

	public void setBitMap(final ViewHolder holder, final WeiboContent content) {
		String imgUrl = content.getImg1().replaceAll("thumbnail", "bmiddle");
		if (TextUtils.isEmpty(imgUrl)) {
			imgUrl = content.getImg3().replaceAll("thumbnail", "bmiddle");
		}
		Bitmap bitMap = imgLoader.loadBitmap(imgUrl, new ImageCallback() {
			@Override
			public void imageLoaded(Bitmap bitmap, String url) {
				if(bitmap != null) {
					holder.weiboImageView.setImageBitmap(bitmap);
					holder.mProgressBar.setVisibility(View.GONE);
					holder.mContent.setVisibility(View.VISIBLE);
					tmpBitMap = bitmap;
					holder.saveButton.setOnClickListener(new SaveImg(dataHandler, tmpBitMap, content, false));
					holder.mailButton.setOnClickListener(new SaveImg(dataHandler, tmpBitMap, content, true));
				} else {
					holder.mContent.setVisibility(View.VISIBLE);
					holder.mProgressBar.setVisibility(View.VISIBLE);
				}
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
		if (bitMap != null) {
			holder.weiboImageView.setImageBitmap(bitMap);
			holder.mProgressBar.setVisibility(View.GONE);
			holder.saveButton.setOnClickListener(new SaveImg(dataHandler, bitMap, content, false));
			holder.mailButton.setOnClickListener(new SaveImg(dataHandler, bitMap, content, true));
		}
	}
	
	public void sendMail(final WeiboContent content) {
		Utils.storeImageToFile(tmpBitMap, content.getWeibocontentId());
		File file = new File(Constant.IMAGE_DOWNLOAD_PATH + content.getWeibocontentId() + ".png"); //附件文件地址
		String body = content.getContent() +  content.getRetweetedcontent() + ctx.getResources().getString(R.string.msg_mail_body);
		Intent intent = new Intent(Intent.ACTION_SEND);
		String subject = "[" + ctx.getResources().getString(R.string.msg_mail_subject) + "]" + " " + content.getItem().getName();
		intent.putExtra(Intent.EXTRA_SUBJECT, subject); 		//主题
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file)); 	//添加附件，附件为file对象
		intent.putExtra(Intent.EXTRA_TEXT, body);  
        intent.setType("application/octet-stream"); 				//其他的均使用流当做二进制数据来发送
		ctx.startActivity(intent); 									//调用系统的mail客户端进行发送。
	}
	
	
	public void setWeiboConent(final ViewHolder holder, final WeiboContent content) {
		// initialize weibo content text view
		String newsContentTextViewStr = Utils.changeALink(content.getContent());
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
							holder.contentTextView.setText(ss);  
							holder.contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
							holder.contentTextView.invalidate();
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
					
					holder.contentTextView.setText(ss);  
					holder.contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
					holder.contentTextView.invalidate();
				} else if (contentMap.get("midTag") != null) {
					int start = (Integer) contentMap.get("start");
					int end = (Integer) contentMap.get("end");
					ss.setSpan(new ForegroundColorSpan(ctx.getResources().getColor(R.color.darkBlue)), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
					
					holder.contentTextView.setText(ss);  
					holder.contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
					holder.contentTextView.invalidate();
				} else if (contentMap.get("topicTag") != null) {
					int start = (Integer) contentMap.get("start");
					int end = (Integer) contentMap.get("end");
					ss.setSpan(new ForegroundColorSpan(ctx.getResources().getColor(R.color.dark_green)), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
					
					holder.contentTextView.setText(ss);  
					holder.contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
					holder.contentTextView.invalidate();
				}
			}
		}
	    holder.contentTextView.setText(ss);  
	    holder.contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
	}

	public void setWeiboForward(final ViewHolder holder, final WeiboContent content) {
		String newsForwardStr = "";
		if (content.hasRetweetedstatus()) {
			newsForwardStr = content.getRetweetedcontent();
		} 
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
							
							holder.forwardTextView.setText(forwardSpannable);  
							holder.forwardTextView.setMovementMethod(LinkMovementMethod.getInstance());
							holder.forwardTextView.invalidate();
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
					
					holder.forwardTextView.setText(forwardSpannable);  
					holder.forwardTextView.setMovementMethod(LinkMovementMethod.getInstance());
					holder.forwardTextView.invalidate();
				} else if (contentMap.get("midTag") != null) {
					int start = (Integer) contentMap.get("start");
					int end = (Integer) contentMap.get("end");
					forwardSpannable.setSpan(new ForegroundColorSpan(ctx.getResources().getColor(R.color.darkBlue)), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
					
					holder.forwardTextView.setText(forwardSpannable);  
					holder.forwardTextView.setMovementMethod(LinkMovementMethod.getInstance());
					holder.forwardTextView.invalidate();
				} else if (contentMap.get("topicTag") != null) {
					int start = (Integer) contentMap.get("start");
					int end = (Integer) contentMap.get("end");
					forwardSpannable.setSpan(new ForegroundColorSpan(ctx.getResources().getColor(R.color.dark_green)), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
					
					holder.forwardTextView.setText(forwardSpannable);  
					holder.forwardTextView.setMovementMethod(LinkMovementMethod.getInstance());
					holder.forwardTextView.invalidate();
				}
			}
		}
		holder.forwardTextView.setText(forwardSpannable);  
		holder.forwardTextView.setMovementMethod(LinkMovementMethod.getInstance());
	    if (TextUtils.isEmpty(newsForwardStr)) {
	    	holder.forwardLayout.setVisibility(View.GONE);
			holder.forwardTextView.setVisibility(View.GONE);
		} else {
			holder.forwardLayout.setVisibility(View.VISIBLE);
			holder.forwardTextView.setVisibility(View.VISIBLE);
		}
	}

}
