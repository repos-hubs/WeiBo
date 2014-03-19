package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weibo4android.Comment;
import weibo4android.Count;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.CommentItemAdapter;
import com.kindroid.hub.adapter.CommentsListAdapter;
import com.kindroid.hub.adapter.WeiboDetailsCommentsAdapter;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.proto.CommonProtoc.ItemType;
import com.kindroid.hub.proto.WeiboContentProtoc.Review;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;
import com.kindroid.hub.utils.AsyncImageLoader;
import com.kindroid.hub.utils.AsyncImageLoader.ImageCallback;
import com.kindroid.hub.utils.ConvertUtils;
import com.kindroid.hub.utils.Utils;

public class NewsDetailsActivity extends Activity implements OnScrollListener, View.OnClickListener {
	private static final String TAG = "NewsDetailsActivity";
	private final int HANDLE_DATA = 0;
	private final int REFRESH_DATA = 1;
	
	private final int HANDLER_COUNTS = 0;
	private final int HANDLER_COLLECTION = 1;
	
	final int imgLength = "[img][/img]".length();
	
	private ImageView newsIconImageView;
	private TextView newsTitleTextView;
	private TextView newsTimeTextView;
	private ImageView forwardImageView;
	private ImageView commentImageView;
//	private WebView newsContentWebView;
	private ImageView newsImageView;
//	private WebView newsForwardWebView;
	private ImageView newsForwardImageView;
	private TextView newsContentTextView;
	private TextView newsForwardTextView;
	
	private TextView forwardTextView;
	private TextView commentTextView;
	private ListView newsCommentsListView;
	private LinearLayout commentsListLayout;
	private LinearLayout forwardLayout;
	private ImageView collectImageView;
	
	private ImageView beforeWeiboImageView;
	private ImageView nextWeiboImageView;
	
	public static WeiboContent weibo;
	
	private WeiboDetailsCommentsAdapter adapter;
	
	final String mimeType = "text/html";
	final String encoding = "utf-8";
	
	public static int forwardCount = 0;
	
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private int PAGE_INDEX = 10;
	private final static int PAGE_SIZE = 15;
	private int currentPage = 1;
	
	private boolean threadRunning = false;
	private LinearLayout footView;
	private ProgressDialog dlgLoading = null;
	private ImageView btnBack;
	private RelativeLayout pnlLayout;
	private TextView fromTextView;
//	private TextView beforeBtnTextView;
//	private TextView nextBtnTextView;
	private TextView titleTextView;
	private RelativeLayout titleBarLayout;
	
	AsyncImageLoader imgLoader;
	
	private int currentPosition = -1;
	private int from = 0;
	private String fromSearch = "";
	private String fromMode = "";
	//comments List
	public static List<Comment> commentsList = new ArrayList<Comment>();
	public static int reviewCount = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_news_details);
		
		fromMode = getIntent().getStringExtra("fromMode");
		if (!TextUtils.isEmpty(fromMode) && fromMode.equals("channel")) {
			
			//取得列表界面选择位置
			currentPosition = getIntent().getIntExtra("listPosition", -1);
			from = getIntent().getIntExtra("from", 1);
			fromSearch = getIntent().getStringExtra("fromSearch");
			if (!TextUtils.isEmpty(fromSearch) && fromSearch.equals("no")) {
				weibo = NewsListActivity.weiboContent;
			} else {
				weibo = HubNewsSearchActivity.newsList.get(currentPosition);
			}
			
		} else if (!TextUtils.isEmpty(fromMode) && fromMode.equals("tribe")) {
			//取得列表界面选择位置
			currentPosition = getIntent().getIntExtra("listPosition", -1);
			if (HubTribe.mList != null) {
				weibo = HubTribe.mList.get(currentPosition);
			}
			
		}
		
		if (weibo == null) {
			finish();
		}
		reviewCount = weibo.getReviewCount();
		forwardCount = weibo.getForwardCount();
		findViews();
		initComponents();
		
//		new LoadCommentsThread(1, false).start();
	}
	
	private void findViews() {
		newsIconImageView = (ImageView) findViewById(R.id.newsIconImageView);
		newsIconImageView.setOnClickListener(this);
		newsTitleTextView = (TextView) findViewById(R.id.newsTitleTextView);
		newsTimeTextView = (TextView) findViewById(R.id.timeTextView);
		forwardImageView = (ImageView) findViewById(R.id.forwardImageView);
		commentImageView = (ImageView) findViewById(R.id.commentImageView);
//		newsContentWebView = (WebView) findViewById(R.id.newsContentsWebView);
		newsImageView = (ImageView) findViewById(R.id.newsContentImageView);
		forwardTextView = (TextView) findViewById(R.id.forwardTextView);
		commentTextView = (TextView) findViewById(R.id.commentTextView);
		newsCommentsListView = (ListView) findViewById(R.id.newsCommentsListView);
		btnBack = (ImageView) findViewById(R.id.btn_back);
		commentsListLayout = (LinearLayout) findViewById(R.id.commentsListLayout);
		nextWeiboImageView = (ImageView) findViewById(R.id.nextWeiboImageView);
		nextWeiboImageView.setOnClickListener(this);
		beforeWeiboImageView = (ImageView) findViewById(R.id.beforeWeiboImageView);
		beforeWeiboImageView.setOnClickListener(this);
		fromTextView = (TextView) findViewById(R.id.fromTextView);
		
		pnlLayout = (RelativeLayout) findViewById(R.id.pnl_option);
		titleTextView = (TextView) findViewById(R.id.lbl_title);
		titleBarLayout = (RelativeLayout) findViewById(R.id.title_bar);
		
//		newsForwardWebView = (WebView) findViewById(R.id.newsForwardWebView);
		newsContentTextView = (TextView) findViewById(R.id.newsContentsTextView);
		newsForwardTextView = (TextView) findViewById(R.id.newsForwardTextView);
		newsForwardImageView = (ImageView) findViewById(R.id.newsForwardImageView);
		
		forwardLayout = (LinearLayout) findViewById(R.id.forwardLayout);
		
		View foot = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listview_footer, null, false);
		newsCommentsListView.addFooterView(foot);
		foot.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
//					new LoadCommentsThread(++currentPage, true).start();
					/*showProgressDialog();
					new LoadCommentsThread(currentPage++, false).start();*/
					Intent intent = new Intent(NewsDetailsActivity.this, HubSinaCommentsListActivity.class);
					intent.putExtra("fromMode", fromMode);
					intent.putExtra("statusMid", weibo.getWeibocontentId());
					startActivity(intent);
				}
			}
		);
		
		collectImageView = (ImageView) findViewById(R.id.collectImageView);
	}

	// 初始化组件
	private void initComponents() {
		if (weibo != null) {
			
			newsTimeTextView.setText(weibo.getTimeLeft());
			String fmtForwardCount = getResources().getString(R.string.msg_forwards_count);
			String fmtCommentsCount = getResources().getString(R.string.msg_comments_count);
			forwardTextView.setText(String.format(fmtForwardCount, weibo.getForwardCount()));
//			forwardTextView.setOnClickListener(this);
			commentTextView.setText(String.format(fmtCommentsCount, 0));
			
			//come from
			String sourceStr = weibo.getSource();
			Pattern name = Pattern.compile("<a.*?>(.*?)</a>", Pattern.DOTALL);
			Matcher fromMatcher = name.matcher(sourceStr);
			if (fromMatcher.find()) {
				fromTextView.setText(fromMatcher.group(1));
			}
			
			String newsIcon = weibo.getItem().getIcon();
			imgLoader = new AsyncImageLoader(NewsDetailsActivity.this);
			Bitmap bmp = null;
			//weibo's avatar
			bmp = imgLoader.loadBitmap(newsIcon, new ImageCallback() {
				@Override
				public void imageLoaded(Bitmap bitmap, String url) {
					newsIconImageView.setImageBitmap(bitmap);
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
			if(bmp != null) {
				newsIconImageView.setImageBitmap(bmp);
			} else {
				newsIconImageView.setImageResource(R.drawable.user_default);
			}
			
			newsTitleTextView.setText(weibo.getItem().getName());
			
			//content's image
			
			String contentImgUrl = "";
			String forwardImgUrl = "";
			String forwardContentStr = "";
			if (weibo.hasRetweetedstatus()) {
				forwardContentStr = weibo.getRetweetedcontent();
				forwardImgUrl = weibo.getImg3().replace("large", "thumbnail");
			} else {
				contentImgUrl = weibo.getImg1().replace("large", "thumbnail");
			}
			if (TextUtils.isEmpty(forwardImgUrl.trim()) && TextUtils.isEmpty(forwardContentStr)) {
				forwardLayout.setVisibility(View.GONE);
			} else {
				forwardLayout.setVisibility(View.VISIBLE);
			}
			
			if (!TextUtils.isEmpty(contentImgUrl.trim())) {
				newsImageView.setVisibility(View.VISIBLE);
				Bitmap contentImage = imgLoader.loadBitmap(contentImgUrl,
						new ImageCallback() {
					@Override
					public void imageLoaded(Bitmap bitmap, String url) {
//						newsImageView.setImageBitmap(bitmap);
						if(bitmap != null) {
							newsImageView.setImageBitmap(bitmap);
						} else {
							newsImageView.setVisibility(View.GONE);
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
			} else {
				newsImageView.setVisibility(View.GONE);
			}
			newsImageView.setOnClickListener(this);
			newsForwardImageView.setOnClickListener(this);
			
			//forward image
			if (!TextUtils.isEmpty(forwardImgUrl.trim())) {
				newsForwardImageView.setVisibility(View.VISIBLE);
				Bitmap contentImage = imgLoader.loadBitmap(forwardImgUrl,
						new ImageCallback() {
					@Override
					public void imageLoaded(Bitmap bitmap, String url) {
//						newsImageView.setImageBitmap(bitmap);
						if(bitmap != null) {
							newsForwardImageView.setImageBitmap(bitmap);
						} else {
							newsForwardImageView.setVisibility(View.GONE);
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
			} else {
				newsForwardImageView.setVisibility(View.GONE);
			}
			
			//news content
			String regx = "\\[img\\](.*?)\\[/img\\]";
			String newsContentTextViewStr = weibo.getContent();
			Pattern pattern = Pattern.compile(regx);
			Matcher m = pattern.matcher(newsContentTextViewStr);
			List contentMapList = new ArrayList();
			int contentIndex = 1;
			while(m.find()) {
				Map indexMap = new HashMap();
				String url = m.group(1);
				System.out.println(url);
				int startIndex = newsContentTextViewStr.indexOf("[img]", contentIndex);
				indexMap.put("start", startIndex);
				indexMap.put("url", url);
				indexMap.put("end", startIndex + url.length() + imgLength); //imgLength为[img][/img]长度
				contentIndex = startIndex + url.length() + imgLength;
				newsContentTextViewStr = newsContentTextViewStr.replaceFirst(regx, Utils.generateMixString(url.length() + imgLength));
				contentMapList.add(indexMap); 
			}
			
			final SpannableString ss = new SpannableString(weibo.getContent());  
			if (contentMapList != null && contentMapList.size() > 0) {
				for (int i = 0; i < contentMapList.size(); i++) {
					final Map contentMap = (Map) contentMapList.get(i);
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
					} else {
						/*Drawable d = getResources().getDrawable(R.drawable.xx);
						d.setBounds(1, 1, d.getIntrinsicWidth(), d.getIntrinsicHeight());  
						ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
						int start = (Integer) contentMap.get("start");
						int end = (Integer) contentMap.get("end");
						ss.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); */
					}
				}
			}
		    newsContentTextView.setText(ss);  
		    newsContentTextView.setMovementMethod(LinkMovementMethod.getInstance());
			
		    //initialize forward content text view
		    String forwardTmpStr = forwardContentStr;
		    Matcher forwardMatcher = pattern.matcher(forwardTmpStr);
		    List forwardMapList = new ArrayList();
		    int forwardIndex = 1;
		    while(forwardMatcher.find()) {
		    	Map indexMap = new HashMap();
				String url = forwardMatcher.group(1);
				int startIndex = forwardTmpStr.indexOf("[img]", forwardIndex);
				indexMap.put("start", startIndex);
				indexMap.put("url", url);
				indexMap.put("end", startIndex + url.length() + imgLength); //imgLength为[img][/img]长度
				forwardIndex = startIndex + url.length() + imgLength;
				forwardTmpStr = forwardTmpStr.replaceFirst(regx, Utils.generateMixString(url.length() + imgLength));
				forwardMapList.add(indexMap); 
			}
			
			final SpannableString forwardSpannable = new SpannableString(forwardContentStr);  
			if (forwardMapList != null && forwardMapList.size() > 0) {
				for (int i = 0; i < forwardMapList.size(); i++) {
					final Map contentMap = (Map) forwardMapList.get(i);
					// start thread to load image
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
					} else {
						/*Drawable d = getResources().getDrawable(R.drawable.xx);
						d.setBounds(1, 1, d.getIntrinsicWidth(), d.getIntrinsicHeight());  
						ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
						int start = (Integer) contentMap.get("start");
						int end = (Integer) contentMap.get("end");
						forwardSpannable.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE); */
					} 
				}
			}
			newsForwardTextView.setText(forwardSpannable);  
			newsForwardTextView.setMovementMethod(LinkMovementMethod.getInstance());
		    if (TextUtils.isEmpty(forwardContentStr)) {
				newsForwardTextView.setVisibility(View.GONE);
			} else {
				newsForwardTextView.setVisibility(View.VISIBLE);
			}
		}
		
		btnBack.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					finish();
				}
			}
		);
		
		//设置标题
		if (!TextUtils.isEmpty(fromMode) && fromMode.equals("channel")) {
			
			if (from == ItemType.Type.NEWS.getNumber()) {
				titleTextView.setText(getResources().getString(R.string.home_news_title));
			} else if (from == ItemType.Type.FUNNY.getNumber()) {
				titleTextView.setText(getResources().getString(R.string.home_laugh_title));
			} else if (from == ItemType.Type.BEAUTY.getNumber()) {
				titleTextView.setText(getResources().getString(R.string.home_beauty_title));
			}
		} else {
			titleTextView.setText(getResources().getString(R.string.hub_tribe_title));
		}
		//设置标题背景
		if (!TextUtils.isEmpty(fromMode) && fromMode.equals("channel")) {
			if (from == ItemType.Type.NEWS.getNumber()) {
				titleBarLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_blue_bg));
				forwardImageView.setImageResource(R.drawable.icon_forward);
				commentImageView.setImageResource(R.drawable.icon_comment);
			} else if (from == ItemType.Type.FUNNY.getNumber()) {
				titleBarLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_blue_bg));
				forwardImageView.setImageResource(R.drawable.icon_laugh_forward);
				commentImageView.setImageResource(R.drawable.icon_laugh_comment);
			} else if (from == ItemType.Type.BEAUTY.getNumber()) {
				titleBarLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_blue_bg));
				forwardImageView.setImageResource(R.drawable.icon_beauty_forward);
				commentImageView.setImageResource(R.drawable.icon_beauty_comment);
			}
		} else {
			titleBarLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.head_bg));
			forwardImageView.setImageResource(R.drawable.tribe_forward_btn);
			commentImageView.setImageResource(R.drawable.tribe_comment_btn);
		}
		
		//forward image onclick
		forwardImageView.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(NewsDetailsActivity.this, HubForwardActivity.class);
					intent.putExtra("listPosition", currentPosition);
					intent.putExtra("fromSearch", fromSearch);
					if (!TextUtils.isEmpty(fromMode) && fromMode.equals("channel")) {
						intent.putExtra("fromMode", "channel");
					} else {
						intent.putExtra("fromMode", "tribe");
					}
					startActivity(intent);
				}
			}
		);
		
		//reply image onclick
		commentImageView.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(NewsDetailsActivity.this, HubReplyActivity.class);
					HubReplyActivity.commentsList = commentsList;
					intent.putExtra("listPosition", currentPosition);
					intent.putExtra("fromMode", fromMode);
					intent.putExtra("type", "comment");
					startActivity(intent);
				}
			}
		);
		
		collectImageView.setOnClickListener(this);
	}
	
	//handle search result
	Handler dataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			hideLoadingDialog();
			switch (msg.arg1) {
			case HANDLE_DATA:
				if (commentsList != null && commentsList.size() > 0) {
					
					adapter = new WeiboDetailsCommentsAdapter(NewsDetailsActivity.this, fromMode, commentsList);
					newsCommentsListView.setAdapter(adapter);
					
					LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.FILL_PARENT, (adapter.getCount() + 1) * 200 - 1);
					commentsListLayout.setLayoutParams(lp1);
					adapter.notifyDataSetChanged();
					
					if (commentsList.size() < PAGE_SIZE) {
						newsCommentsListView.removeFooterView(footView);
					}
				} else {
					if (adapter != null) {
						adapter.notifyDataSetChanged();
						adapter.notifyDataSetInvalidated();
						newsCommentsListView.removeFooterView(footView);
					}
				}
				break;
			case REFRESH_DATA:
				if (adapter != null) {
					if (commentsList != null) {
						adapter.notifyDataSetChanged();
						if (commentsList.size() < PAGE_SIZE) {
							newsCommentsListView.removeFooterView(footView);
						}
					}
				}
				threadRunning = false;
			default:
				break;
			}
		}
	};
	
	Handler countsHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case HANDLER_COUNTS:
				hideLoadingDialog();
				String forwardStr = getResources().getString(R.string.msg_forwards_count);
				String commentStr = getResources().getString(R.string.msg_comments_count);
				List list = (List) msg.obj;
				if (list != null && list.size() > 0) {
					Count count = (Count) list.get(0);
					forwardTextView.setText(String.format(forwardStr, count.getRt()));
					commentTextView.setText(String.format(commentStr, count.getComments()));
				} else {
					forwardTextView.setText(String.format(forwardStr, 0));
					commentTextView.setText(String.format(commentStr, 0));
				}
				break;
			case HANDLER_COLLECTION:
				hideLoadingDialog();
				String result = (String) msg.obj;
				if (!TextUtils.isEmpty(result) && result.equals("success")) {
					Toast.makeText(NewsDetailsActivity.this, getResources().getString(R.string.msg_collection_success), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(NewsDetailsActivity.this, getResources().getString(R.string.msg_collection_failure), Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
			}
		}
		
	};
	
	class LoadForwardsCounts extends Thread {

		@Override
		public void run() {
			Message msg = countsHandler.obtainMessage();
			List<Count> countList = DataService.getForwardsAndComments(weibo.getWeibocontentId() + "", NewsDetailsActivity.this);
			msg.arg1 = HANDLER_COUNTS;
			msg.obj = countList;
			countsHandler.sendMessage(msg);
		}		
	}
	
	class CollectionThread extends Thread {
		
		@Override
		public void run() {
			Message msg = countsHandler.obtainMessage();
			String result = DataService.collectWeibo(weibo.getWeibocontentId(), NewsDetailsActivity.this);
			msg.arg1 = HANDLER_COLLECTION;
			msg.obj = result;
			countsHandler.sendMessage(msg);
		}		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		showProgressDialog();
		new LoadCommentsThread(currentPage, false).start();
		new LoadForwardsCounts().start();
		
	}

	class LoadCommentsThread extends Thread {
		private int startIndex;
		private boolean isRefresh;

		public LoadCommentsThread(int startIndex, boolean isRefresh) {
			this.startIndex = startIndex;
			this.isRefresh = isRefresh;
			if (isRefresh == false && commentsList != null && commentsList.size() > 0) {
				commentsList.clear();
			}
		}
		
		public void run() {
			
			List<Comment> tempList = new ArrayList<Comment>();
//			tempList = DataService.getCommentsListData(weibo.getContentId(), startIndex, HubSinaWeiboDetailsActivity.this, PAGE_SIZE);
			tempList = DataService.getCommentsList(NewsDetailsActivity.this, weibo.getWeibocontentId() + "", startIndex, PAGE_SIZE);
			commentsList.addAll(tempList);
//			Log.v(TAG, "size:" + tempList.size());
			Message msg = dataHandler.obtainMessage();
			msg.obj = commentsList;
			if (!isRefresh) {
				msg.arg1 = HANDLE_DATA;
				dataHandler.sendMessage(msg);
			} else {
				msg.arg1 = REFRESH_DATA;
				dataHandler.sendMessage(msg);
			}
		}
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
		lastItem = firstVisibleItem + visibleItemCount - 1;
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
		if (!threadRunning && adapter != null && lastItem == adapter.getCount() && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			threadRunning = true;
			currentPage = currentPage + 1;
			new LoadCommentsThread(currentPage, true).start();
			
		}
	}
	
	/**
	 * Show loading dialog
	 */
	private void showProgressDialog() {
		dlgLoading = new ProgressDialog(this);
		dlgLoading.setTitle(getResources().getText(R.string.app_name));
		dlgLoading.setMessage(getResources().getText(R.string.progress_message));
		dlgLoading.setIndeterminate(true);
		dlgLoading.show();
	}
		
	/**
	 * Hide loading dialog
	 */
	private void hideLoadingDialog() {
		if (dlgLoading != null) {
			this.dlgLoading.dismiss();
		}
	}

	@Override
	public void onClick(View view) {
		
		if (view.getId() == R.id.newsContentImageView || view.getId() == R.id.newsForwardImageView) {
			loadPhoto();
		}
		
		if (view.getId() == R.id.newsIconImageView) {
			Intent intent = new Intent(NewsDetailsActivity.this, UserDetailInfo.class);
			intent.putExtra(UserDetailInfo.U_ID, weibo.getItem().getSinaId());
			startActivity(intent);
		}
		
		if (view.getId() == R.id.collectImageView) {
			showProgressDialog();
			new CollectionThread().start();
		}
		
		if (view.getId() == R.id.forwardTextView) {
			Intent intent = new Intent(NewsDetailsActivity.this, HubTribeWriteWeibo.class);
			startActivity(intent);
		}
		//previous weibo
		if (view.getId() == R.id.beforeWeiboImageView) {
			if (!TextUtils.isEmpty(fromMode) && fromMode.equals("tribe")) {
				if (currentPosition <= 0) {
					Toast.makeText(NewsDetailsActivity.this, getResources().getString(R.string.msg_first_item), Toast.LENGTH_SHORT).show();
				} else {
					currentPosition = currentPosition - 1;
					
					weibo = HubTribe.mList.get(currentPosition);
					reviewCount = weibo.getReviewCount();
					initComponents();
					showProgressDialog();
					if (adapter != null) {
						adapter.notifyDataSetChanged();
					}
					new LoadForwardsCounts().start();
					new LoadCommentsThread(1, false).start();
				}
				
			} else {
				
				if (currentPosition <= 0) {
					Toast.makeText(NewsDetailsActivity.this, getResources().getString(R.string.msg_first_item), Toast.LENGTH_SHORT).show();
				} else {
					currentPosition = currentPosition - 1;
					if (!TextUtils.isEmpty(fromSearch) && fromSearch.equals("no")) {
						weibo = NewsListActivity.newsList.get(currentPosition);
					} else {
						weibo = HubNewsSearchActivity.newsList.get(currentPosition);
					}
					initComponents();
					reviewCount = weibo.getReviewCount();
					showProgressDialog();
					if (adapter != null) {
						adapter.notifyDataSetChanged();
					}
					new LoadForwardsCounts().start();
					new LoadCommentsThread(1, false).start();
				}
			}
		}
		//next weibo
		if (view.getId() == R.id.nextWeiboImageView) {
			if (!TextUtils.isEmpty(fromMode) && fromMode.equals("tribe")) {
				if (currentPosition == HubTribe.mList.size() - 1) {
					Toast.makeText(NewsDetailsActivity.this, getResources().getString(R.string.msg_last_item), Toast.LENGTH_SHORT).show();
				} else {
					currentPosition = currentPosition + 1;
					
					weibo = HubTribe.mList.get(currentPosition);
					initComponents();
					reviewCount = weibo.getReviewCount();
					showProgressDialog();
					if (adapter != null) {
						adapter.notifyDataSetChanged();
					}
					new LoadForwardsCounts().start();
					new LoadCommentsThread(1, false).start();
				}
				
			} else {
				int totalSize = 0;
				if (!TextUtils.isEmpty(fromSearch) && fromSearch.equals("no")) {
					totalSize = NewsListActivity.newsList.size() - 1;
				} else {
					totalSize = HubNewsSearchActivity.newsList.size() - 1;
				}
				if (currentPosition == totalSize) {
					Toast.makeText(NewsDetailsActivity.this, getResources().getString(R.string.msg_last_item), Toast.LENGTH_SHORT).show();
				} else {
					currentPosition = currentPosition + 1;
					if (!TextUtils.isEmpty(fromSearch) && fromSearch.equals("no")) {
						weibo = NewsListActivity.newsList.get(currentPosition);
					} else {
						weibo = HubNewsSearchActivity.newsList.get(currentPosition);
					}
					reviewCount = weibo.getReviewCount();
					initComponents();
					showProgressDialog();
					if (adapter != null) {
						adapter.notifyDataSetChanged();
					}
					new LoadForwardsCounts().start();
					new LoadCommentsThread(1, false).start();
				}
			}
		}
	}
	
	private void loadPhoto() {

	    AlertDialog.Builder imageDialog = new AlertDialog.Builder(this);
	    LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

	    View layout = inflater.inflate(R.layout.pop_up_image,
	              (ViewGroup) findViewById(R.id.layout_root));
	    final ImageView image = (ImageView) layout.findViewById(R.id.fullimage);
	      
	    String contentImgUrl = "";
		if (weibo.hasRetweetedstatus()) {
			contentImgUrl = weibo.getImg3().replace("thumbnail", "bmiddle");
		} else {
			contentImgUrl = weibo.getImg1().replace("thumbnail", "bmiddle");
			
		}
		Bitmap contentImage = imgLoader.loadBitmap(contentImgUrl,
				new ImageCallback() {
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
//	    image.setImageDrawable(tempImageView.getDrawable());
	    imageDialog.setView(layout);
	    imageDialog.setPositiveButton(getResources().getString(R.string.ok_button), new DialogInterface.OnClickListener(){
	          public void onClick(DialogInterface dialog, int which) {
	              dialog.dismiss();
	          }

	      });

	    imageDialog.create();
	    imageDialog.show();     
	}
}
