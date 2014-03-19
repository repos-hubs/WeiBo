package com.kindroid.hub.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weibo4android.Comment;
import weibo4android.Count;
import weibo4android.Status;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.CommentsListAdapter;
import com.kindroid.hub.adapter.WeiboDetailsCommentsAdapter;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.data.UserDefaultInfo;
import com.kindroid.hub.download.LazyImageLoader;
import com.kindroid.hub.proto.WeiboContentProtoc.Review;
import com.kindroid.hub.ui.NewsDetailsActivity.CollectionThread;
import com.kindroid.hub.utils.AsyncImageLoader;
import com.kindroid.hub.utils.AsyncImageLoader.ImageCallback;
import com.kindroid.hub.utils.ConvertUtils;
import com.kindroid.hub.utils.Utils;

public class HubSinaWeiboDetailsActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "HubSinaWeiboDetailsActivity";
	private final int HANDLE_DATA = 0;
	private final int REFRESH_DATA = 1;
	
	private final int HANDLER_COUNTS = 0;
	private final int HANDLER_COLLECTION = 1;
	
	final int imgLength = "[img][/img]".length();
	
	private ImageView statusIconImageView;
	private TextView statusTitleTextView;
	private TextView statusTimeTextView;
	private ImageView forwardImageView;
	private ImageView commentImageView;
//	private WebView newsContentWebView;
	private ImageView statusContentImageView;
//	private WebView newsForwardWebView;
	private ImageView statusForwardImageView;
	private TextView statusContentTextView;
	private TextView statusForwardTextView;
	
	private TextView forwardTextView;
	private TextView commentTextView;
	private ListView newsCommentsListView;
	private LinearLayout commentsListLayout;
	private LinearLayout forwardLayout;
	
	private ImageView beforeWeiboImageView;
	private ImageView nextWeiboImageView;
	private TextView fromTextView;
	private ImageView collectImageView;
	
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

	private TextView titleTextView;
	private RelativeLayout titleBarLayout;
	
	AsyncImageLoader imgLoader;
	
	private int currentPosition = -1;
	public static List<Status> statusList = new ArrayList<Status>();
	private static Status status;
	
	//comments List
	public static List<Comment> commentsList = new ArrayList<Comment>();
	private static int reviewCount = 0;
	private String fromMode = "sina";
	private LazyImageLoader imageLoader;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_news_details);
		currentPosition = getIntent().getIntExtra("listPosition", -1);
		status = statusList.get(currentPosition);
		
		imageLoader = new LazyImageLoader(HubSinaWeiboDetailsActivity.this);
		findViews();
		initComponents();
		new LoadForwardsCounts().start();
	}
	
	private void findViews() {
		statusIconImageView = (ImageView) findViewById(R.id.newsIconImageView);
		statusTitleTextView = (TextView) findViewById(R.id.newsTitleTextView);
		statusTimeTextView = (TextView) findViewById(R.id.timeTextView);
		forwardImageView = (ImageView) findViewById(R.id.forwardImageView);
		commentImageView = (ImageView) findViewById(R.id.commentImageView);
		forwardTextView = (TextView) findViewById(R.id.forwardTextView);
		commentTextView = (TextView) findViewById(R.id.commentTextView);
		newsCommentsListView = (ListView) findViewById(R.id.newsCommentsListView);
		btnBack = (ImageView) findViewById(R.id.btn_back);
		commentsListLayout = (LinearLayout) findViewById(R.id.commentsListLayout);
		nextWeiboImageView = (ImageView) findViewById(R.id.nextWeiboImageView);
		nextWeiboImageView.setOnClickListener(this);
		beforeWeiboImageView = (ImageView) findViewById(R.id.beforeWeiboImageView);
		beforeWeiboImageView.setOnClickListener(this);
		
		pnlLayout = (RelativeLayout) findViewById(R.id.pnl_option);
		titleTextView = (TextView) findViewById(R.id.lbl_title);
		titleBarLayout = (RelativeLayout) findViewById(R.id.title_bar);
		fromTextView = (TextView) findViewById(R.id.fromTextView);
		
		statusContentTextView = (TextView) findViewById(R.id.newsContentsTextView);
		statusContentImageView = (ImageView) findViewById(R.id.newsContentImageView);
		statusForwardTextView = (TextView) findViewById(R.id.newsForwardTextView);
		statusForwardImageView = (ImageView) findViewById(R.id.newsForwardImageView);
		
		forwardLayout = (LinearLayout) findViewById(R.id.forwardLayout);
		
		View foot = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listview_footer, null, false);
		newsCommentsListView.addFooterView(foot);
		foot.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					/*showProgressDialog();
					new LoadCommentsThread(currentPage++, false).start();
					new LoadForwardsCounts().start();*/
					Intent intent = new Intent(HubSinaWeiboDetailsActivity.this, HubSinaCommentsListActivity.class);
					intent.putExtra("fromMode", fromMode);
					intent.putExtra("statusMid", status.getMid());
					startActivity(intent);
				}
			}
		);
		
		collectImageView = (ImageView) findViewById(R.id.collectImageView);
		collectImageView.setOnClickListener(this);
	}

	// 初始化组件
	private void initComponents() {
		if (status != null) {
			
			statusTimeTextView.setText(Utils.ddate(status.getCreatedAt().getTime(), HubSinaWeiboDetailsActivity.this));
			String weiboIconUrl = status.getUser().getProfileImageURL().toString();
			imgLoader = new AsyncImageLoader(HubSinaWeiboDetailsActivity.this);
			Bitmap bmp = null;
			//weibo's avatar
			bmp = imgLoader.loadBitmap(weiboIconUrl, new ImageCallback() {
				@Override
				public void imageLoaded(Bitmap bitmap, String url) {
					statusIconImageView.setImageBitmap(bitmap);
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
				statusIconImageView.setImageBitmap(bmp);
			} else {
				statusIconImageView.setImageResource(R.drawable.user_default);
			}
			statusIconImageView.setOnClickListener(this);
			//title
			statusTitleTextView.setText(status.getUser().getName());
			
			//from source
			String sourceStr = status.getSource();
			Pattern name = Pattern.compile("<a.*?>(.*?)</a>", Pattern.DOTALL);
			Matcher fromMatcher = name.matcher(sourceStr);
			if (fromMatcher.find()) {
				fromTextView.setText(fromMatcher.group(1));
			}
			
			//content's image
			statusContentTextView.setText(status.getText());
			if (!TextUtils.isEmpty(status.getThumbnail_pic())) {
				statusContentImageView.setTag(status.getThumbnail_pic());
				imageLoader.displayAppIcon(status.getThumbnail_pic(), HubSinaWeiboDetailsActivity.this, statusContentImageView);
			} else {
				statusContentImageView.setVisibility(View.GONE);
				statusContentImageView.setImageBitmap(null);
			}
			statusContentImageView.setOnClickListener(this);
			//forward content
			Status retweetContent = status.getRetweeted_status();
			if (retweetContent != null) {
				forwardLayout.setVisibility(View.VISIBLE);
				
				if (TextUtils.isEmpty(retweetContent.getText())) {
					statusForwardTextView.setVisibility(View.GONE);
				} else {
					statusForwardTextView.setText(retweetContent.getText());
					statusForwardTextView.setVisibility(View.VISIBLE);
				}
				
				if (!TextUtils.isEmpty(retweetContent.getThumbnail_pic())) {
					statusForwardImageView.setVisibility(View.VISIBLE);
					statusForwardImageView.setTag(retweetContent.getThumbnail_pic());
					imageLoader.displayAppIcon(retweetContent.getThumbnail_pic(), HubSinaWeiboDetailsActivity.this, statusForwardImageView);
				} else {
					statusForwardImageView.setVisibility(View.GONE);
					statusForwardImageView.setImageBitmap(null);
				}
			} else {
				forwardLayout.setVisibility(View.GONE);
			}
			statusForwardImageView.setOnClickListener(this);
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
		titleTextView.setText(getResources().getString(R.string.msg_weibo));
		//设置标题背景
		titleBarLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.head_bg));
		forwardImageView.setImageResource(R.drawable.tribe_forward_btn);
		commentImageView.setImageResource(R.drawable.tribe_comment_btn);
		
		//forward image onclick
		forwardImageView.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(HubSinaWeiboDetailsActivity.this, HubForwardActivity.class);
					intent.putExtra("fromMode", "sina");
					intent.putExtra("listPosition", currentPosition);
					startActivity(intent);
				}
			}
		);
		
		//reply image onclick
		commentImageView.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(HubSinaWeiboDetailsActivity.this, HubReplyActivity.class);
					HubReplyActivity.commentsList = commentsList;
					intent.putExtra("fromMode", "sina");
					intent.putExtra("listPosition", currentPosition);
					intent.putExtra("type", "comment");
					startActivity(intent);
				}
			}
		);
	}
	
	//handle search result
	Handler dataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			hideLoadingDialog();
			switch (msg.arg1) {
			case HANDLE_DATA:
				if (commentsList != null && commentsList.size() > 0) {
					
					adapter = new WeiboDetailsCommentsAdapter(HubSinaWeiboDetailsActivity.this, fromMode, commentsList);
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
					Toast.makeText(HubSinaWeiboDetailsActivity.this, getResources().getString(R.string.msg_collection_success), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(HubSinaWeiboDetailsActivity.this, getResources().getString(R.string.msg_collection_failure), Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
			}
		}
		
	};
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		showProgressDialog();
		new LoadCommentsThread(1, false).start();
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
			tempList = DataService.getCommentsList(HubSinaWeiboDetailsActivity.this, status.getMid() + "", startIndex, PAGE_SIZE);
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
	
	class LoadForwardsCounts extends Thread {

		@Override
		public void run() {
			Message msg = countsHandler.obtainMessage();
			List<Count> countList = DataService.getForwardsAndComments(status.getMid() + "", HubSinaWeiboDetailsActivity.this);
			msg.arg1 = HANDLER_COUNTS;
			msg.obj = countList;
			countsHandler.sendMessage(msg);
		}		
	}
	
	class CollectionThread extends Thread {
		
		@Override
		public void run() {
			Message msg = countsHandler.obtainMessage();
			String result = DataService.collectWeibo(status.getMid(), HubSinaWeiboDetailsActivity.this);
			msg.arg1 = HANDLER_COLLECTION;
			msg.obj = result;
			countsHandler.sendMessage(msg);
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
			loadPhoto(view);
		}
		
		if (view.getId() == R.id.newsIconImageView) {
			Intent intent = new Intent(HubSinaWeiboDetailsActivity.this, UserDetailInfo.class);
			intent.putExtra(UserDetailInfo.U_ID, status.getUser().getId());
			if (String.valueOf(status.getUser().getId()).equals(UserDefaultInfo.getWeiboId(HubSinaWeiboDetailsActivity.this))) {
				intent.putExtra(UserDetailInfo.IS_MY, true);
			}
			startActivity(intent);
		}
		
		if (view.getId() == R.id.collectImageView) {
			showProgressDialog();
			new CollectionThread().start();
		}
		if (view.getId() == R.id.forwardTextView) {
			Intent intent = new Intent(HubSinaWeiboDetailsActivity.this, HubTribeWriteWeibo.class);
			startActivity(intent);
		}
		//previous weibo
		if (view.getId() == R.id.beforeWeiboImageView) {
			if (currentPosition <= 0) {
				Toast.makeText(HubSinaWeiboDetailsActivity.this, getResources().getString(R.string.msg_first_item), Toast.LENGTH_SHORT).show();
			} else {
				currentPosition = currentPosition - 1;
				
				status = this.statusList.get(currentPosition);
				initComponents();
				showProgressDialog();
				if (adapter != null) {
					adapter.notifyDataSetChanged();
				}
				new LoadForwardsCounts().start();
				new LoadCommentsThread(1, false).start();
			}
		}
		//next weibo
		if (view.getId() == R.id.nextWeiboImageView) {
			if (currentPosition == statusList.size() - 1) {
				Toast.makeText(HubSinaWeiboDetailsActivity.this, getResources().getString(R.string.msg_last_item), Toast.LENGTH_SHORT).show();
			} else {
				currentPosition = currentPosition + 1;
				
				status = this.statusList.get(currentPosition);
				initComponents();
				showProgressDialog();
				if (adapter != null) {
					adapter.notifyDataSetChanged();
				}
				//load count
				new LoadForwardsCounts().start();
				new LoadCommentsThread(1, false).start();
				//load comments data
			}
		}
	}
	
	private void loadPhoto(View view) {

	    AlertDialog.Builder imageDialog = new AlertDialog.Builder(this);
	    LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

	    View layout = inflater.inflate(R.layout.pop_up_image, (ViewGroup) findViewById(R.id.layout_root));
	    final ImageView image = (ImageView) layout.findViewById(R.id.fullimage);
	      
	    String contentImgUrl = "";
	    
	    if (view.getId() == R.id.newsContentImageView) {
	    	
	    	if (!TextUtils.isEmpty(status.getThumbnail_pic())) {
	    		contentImgUrl = status.getThumbnail_pic();
	    	} 
	    } else {
	    	Status retweetContent = status.getRetweeted_status();
			if (retweetContent != null) {
				if (!TextUtils.isEmpty(retweetContent.getThumbnail_pic())) {
					contentImgUrl = retweetContent.getBmiddle_pic();
				} 
			} 
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
