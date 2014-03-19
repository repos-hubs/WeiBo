package com.kindroid.hub.adapter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kindroid.hub.R;
import com.kindroid.hub.proto.UserProtoc.Account;
import com.kindroid.hub.proto.WeiboContentProtoc.Review;
import com.kindroid.hub.ui.category.WallPaperOrringReply;

/***
 * 壁纸或者铃音适配器
 * @author huaiyu.zhao
 *
 */
public class WallpaperOrRingAdapter extends BaseAdapter{
	
	final String encoding = "utf-8";
	private LayoutInflater mLayInflater;
	final String mimeType = "text/html";
	
	private Context mContext;
	private List<Review>  mBean;
	private long mId;
	private boolean mIsWallpaper;
	
	public WallpaperOrRingAdapter(Context context, List<Review> bean, long id, boolean isWallpaper){
		mContext=context;
		mBean=bean;
		mId=id;
		mLayInflater=LayoutInflater.from(context);
		mIsWallpaper = isWallpaper;
	}

	@Override
	public int getCount() {
		if(mBean==null || mBean.isEmpty()){
			return 0;
		}
		
		return mBean.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		Review review=mBean.get(position);
		LinearLayout layout = (LinearLayout)mLayInflater.inflate(R.layout.hub_news_details_comments_item, null);
		
		TextView userName=(TextView)layout.findViewById(R.id.userNameTextView);
//		WebView commentsTextView=(WebView)layout.findViewById(R.id.commentsWebView);
		TextView time=(TextView)layout.findViewById(R.id.timeTextView);
		ImageView replyImageView = (ImageView) layout.findViewById(R.id.replyImageView);
		
		Account replyAccount = review.getReplayAccount();
		String replyUserName = "";
		if (review.hasReplayAccount()) {
			replyUserName = mContext.getResources().getString(R.string.home_reply_title) + replyAccount.getNickName() + ":";
		}
		String accountUser = review.getAccount().getNickName();
		String replyUserContent = accountUser + replyUserName+"";
		userName.setText(replyUserContent);
		/*try {
			commentsTextView.getSettings().setSupportZoom(false);
			commentsTextView.getSettings().setJavaScriptEnabled(true);
			commentsTextView.getSettings().setDefaultTextEncodingName(encoding);
			commentsTextView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS); 
			commentsTextView.loadData(URLEncoder.encode(review.getContent(), encoding).replaceAll("\\+", "%20"), mimeType, encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}	*/	
		time.setText(getStr(review.getTimeLag(),"no time"));
		//评论按钮
		
		replyImageView.setImageResource(R.drawable.wallpaper_one_share_background);
		
		//reply image onclick
		replyImageView.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(mContext, WallPaperOrringReply.class);
					intent.putExtra(WallPaperOrringReply.EXTRA_POSITION, position);
					if (mIsWallpaper) {
						intent.putExtra(WallPaperOrringReply.EXTRA_FROM_MODE, WallPaperOrringReply.MODE_WALLPAPER);
					} else {
						intent.putExtra(WallPaperOrringReply.EXTRA_FROM_MODE, WallPaperOrringReply.MODE_RING);
					}
					intent.putExtra(WallPaperOrringReply.EXTRA_TYPE, WallPaperOrringReply.ACTION_REPLY);
					intent.putExtra(WallPaperOrringReply.EXTRA_ID, mId);
					mContext.startActivity(intent);
				}
			}
		);
		
		
		return layout;
	}
	
	public void setReview(List<Review>  bean){
		this.mBean=bean;
	}
	
	private static String getStr(Object obj,String val){
		if(obj==null){
			return val;
		}
		try {
			return (String)obj;			
		} catch (Exception e) {
			return val;
		}
		
	}
	
}
