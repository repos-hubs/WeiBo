package com.kindroid.hub.adapter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import com.kindroid.hub.R;
import com.kindroid.hub.download.LazyImageLoader;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;
import com.kindroid.hub.ui.HubForwardActivity;
import com.kindroid.hub.ui.HubReplyActivity;
import com.kindroid.hub.ui.NewsDetailsActivity;
import com.kindroid.hub.ui.NewsListActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TribeAdapter extends BaseAdapter{
	
	private final String mimeType = "text/html";
	private final String encoding = "utf-8";
	
	private Context ctx;
	private List<WeiboContent> mList;
	private LazyImageLoader imageLoader;

	public TribeAdapter(Context ctx, List<WeiboContent> list) {
		this.ctx = ctx;
		this.mList = list;
		imageLoader = new LazyImageLoader(ctx);
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (null == v) {
			LayoutInflater li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = li.inflate(R.layout.hub_tribe_item, null);
		}
		v.setTag(position);
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int po=Integer.parseInt(v.getTag().toString());
				WeiboContent content=mList.get(po);
				NewsListActivity.weiboContent=content;
				Intent intent = new Intent(ctx, NewsDetailsActivity.class);
				intent.putExtra("fromMode", "tribe");
				intent.putExtra("listPosition", po);
				ctx.startActivity(intent);
			}
		});
		
		ImageView tribeImage = (ImageView) v.findViewById(R.id.newsIconImageView);
		TextView tribeTitle = (TextView) v.findViewById(R.id.newsTitleTextView);
		TextView tribeTime = (TextView) v.findViewById(R.id.timeTextView);
		WebView tribeContent = (WebView) v.findViewById(R.id.newsContentsWebView);
		ImageView forwardImageView = (ImageView) v.findViewById(R.id.forwardImageView);
		ImageView commentImageView = (ImageView) v.findViewById(R.id.commentImageView);
		ImageView tribeContentImage=(ImageView)v.findViewById(R.id.contentImageView);
		ImageView tribeType=(ImageView)v.findViewById(R.id.typeImageView);
		WebView tribeQuoteContent = (WebView) v.findViewById(R.id.newsContentsQuoteWebView);
		ImageView tribeQuoteImage = (ImageView) v.findViewById(R.id.newsContentsQuoteImageView);
		LinearLayout tribeQuoteLayout = (LinearLayout) v.findViewById(R.id.newsQuoteLayout);
		
		WeiboContent weiboContent = mList.get(position);
		tribeTitle.setText(weiboContent.getItem().getName());
		tribeTime.setText(weiboContent.getTimeLeft());
		
		tribeImage.setTag(weiboContent.getItem().getIcon());
		imageLoader.displayUserIcon(weiboContent.getItem().getIcon(),(Activity) ctx, tribeImage);
		try {
			if (weiboContent.hasRetweetedstatus()) {
				tribeQuoteContent.getSettings().setSupportZoom(false);
				tribeQuoteContent.getSettings().setJavaScriptEnabled(true);
				tribeQuoteContent.getSettings().setDefaultTextEncodingName(encoding);
				tribeQuoteContent.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
				tribeQuoteContent.loadData(URLEncoder.encode(weiboContent.getRetweetedcontent(),encoding).replaceAll("\\+", "%20"), mimeType,encoding);
				tribeQuoteContent.setBackgroundColor(0);
				tribeQuoteImage.setTag(weiboContent.getImg3().replace("large", "thumbnail"));
				imageLoader.displayAppIcon(weiboContent.getImg3().replace("large", "thumbnail"), (Activity) ctx,tribeQuoteImage);
				tribeQuoteLayout.setVisibility(View.VISIBLE);
			} else {
				tribeQuoteLayout.setVisibility(View.GONE);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (weiboContent.hasImg1()) {
			tribeContentImage.setTag(weiboContent.getImg1().replace("large", "thumbnail"));
			imageLoader.displayAppIcon(weiboContent.getImg1().replace("large", "thumbnail"), (Activity) ctx,tribeContentImage);
		}
		try {
			tribeContent.getSettings().setSupportZoom(false);
			tribeContent.getSettings().setJavaScriptEnabled(true);
			tribeContent.getSettings().setDefaultTextEncodingName(encoding);
			tribeContent.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS); 
			tribeContent.loadData(URLEncoder.encode(weiboContent.getContent(), encoding).replaceAll("\\+", "%20"), mimeType, encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		int type = weiboContent.getCategory().getCategoryId();
		if (type == 1) {
			tribeType.setImageResource(R.drawable.tribe_news_icon);
		} else if (type == 2) {
			tribeType.setImageResource(R.drawable.tribe_joke_icon);
		} else if (type == 3) {
			tribeType.setImageResource(R.drawable.tribe_mm_icon);
		}
		forwardImageView.setTag(position);
		forwardImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int po=Integer.parseInt(v.getTag().toString());
				WeiboContent content=mList.get(po);
				NewsListActivity.weiboContent=content;
				Intent intent = new Intent(ctx, HubForwardActivity.class);
				intent.putExtra("fromMode", "tribe");
				intent.putExtra("listPosition", po);
				ctx.startActivity(intent);
			}
		});
		commentImageView.setTag(position);
		commentImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int po=Integer.parseInt(v.getTag().toString());
				WeiboContent content=mList.get(po);
				NewsListActivity.weiboContent=content;
				Intent intent = new Intent(ctx, HubReplyActivity.class);
				intent.putExtra("fromMode", "tribe");
				intent.putExtra("listPosition", po);
				intent.putExtra("type", "comment");
				ctx.startActivity(intent);
			}
		});
		return v;
	}
}
