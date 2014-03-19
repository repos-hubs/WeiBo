package com.kindroid.hub.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kindroid.hub.R;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.entity.GroupFriend;
import com.kindroid.hub.utils.AsyncImageLoader;
import com.kindroid.hub.utils.AsyncImageLoader.ImageCallback;

public class AccordionItemView extends RelativeLayout {

	/**
	 * background image view
	 */
	private ImageView imgBackground;
	/**
	 * category icon view
	 */
	private ImageView imgIcon;
	/**
	 * label show category name
	 */
//	private TextView lblText;
	
	private TextView lblEditText;
	
	private TextView lblText;
	/**
	 * grid view show subcagegory
	 */
	private GridView grdContent;
	/**
	 * image loader
	 */
	private AsyncImageLoader imgLoader;
	/**
	 * event listener when subcategory item clicked
	 */
	private OnItemClickListener onItemClickListener;
	/**
	 * event listener when subcategory item clicked
	 */
	private OnEditClickListener onEditClickListener;
	/**
	 * 
	 */
	private OnClickListener onClickListener;

	/**
	 * <p>instance an accordion item</p>
	 * @param context
	 * @param attrs
	 */
	private GroupFriend item;
	private boolean first = true;
	private List cacheList =  new ArrayList();
	
	private int currentPage = 0;
	
//	private ScrollView scrollView;
	public AccordionItemView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// inflate view from /res/layout/accordion_item.xml
		String infServiString = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(infServiString);
		inflater.inflate(R.layout.accordion_item, this, true);

		// init components
		this.imgBackground = (ImageView) super.findViewById(R.id.img_accordion_item_bg);
//		this.imgIcon = (ImageView) super.findViewById(R.id.img_accordion_item_icon);
		this.lblText = (TextView) super.findViewById(R.id.lbl_accordion_item_txt);
//		this.lblEditText = (TextView) super.findViewById(R.id.lbl_accordion_item_edit);
		
		this.grdContent = (GridView) super.findViewById(R.id.grd_accordion_item_content);
		//add new about scroll view
//		scrollView = (ScrollView) findViewById(R.id.scrollview);
		this.imgLoader = new AsyncImageLoader(context);
		
		// add item click event listener
		this.grdContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// get selected item data
				GroupFriend item = (GroupFriend) arg0.getItemAtPosition(arg2);
				
				if(AccordionItemView.this.onItemClickListener != null) {
					AccordionItemView.this.onItemClickListener.onItemClick(item);
				}
			}
		});
	/*	this.lblEditText.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Log.v("AtMeGroupActivity", "dd");
				if(AccordionItemView.this.onEditClickListener != null) {
					AccordionItemView.this.onEditClickListener.onEditClick(item);
				}
			}
		});*/
		this.lblText.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Log.v("AtMeGroupActivity", "ddaaaa");
				if(AccordionItemView.this.onClickListener != null) {
					AccordionItemView.this.onClickListener.onClick(AccordionItemView.this, item);
				}
			}
		});
	}

	/**
	 * @param item
	 */
	public void init(GroupFriend item) {
		this.item = item;
		this.lblText.setText(item.getName().replace("&amp;", " & "));
		Bitmap bmp = null;
		/*if (!TextUtils.isEmpty(item.getIcon())) {
			
//			bmp = this.imgLoader.loadBitmap(Constant.IMAGE_SERVER + item.getIcon() + "/0.jpg", new ImageCallback() {
			bmp = this.imgLoader.loadBitmap(item.getIcon() + "/" + item.getName() + "/4.jpg", new ImageCallback() {
				@Override
				public void imageLoaded(Drawable drawable, int position, String url) {
					// TODO Auto-generated method stub
				}
				@Override
				public void imageLoaded(Bitmap bitmap, int position, String url) {
					// TODO Auto-generated method stub
				}
				@Override
				public void imageLoaded(Drawable drawable, String url) {
					// TODO Auto-generated method stub
				}
				@Override
				public void imageLoaded(Bitmap bitmap, String url) {
					// TODO Auto-generated method stub
					AccordionItemView.this.imgIcon.setImageBitmap(bitmap);
				}
			});
		}
		if(bmp != null) {
			this.imgIcon.setImageBitmap(bmp);
		} else {
			this.imgIcon.setImageResource(R.drawable.icon_category);
		}*/
		
		/*GridAdapter adapter = new GridAdapter(super.getContext());
			
		//init gridView
		adapter.init(item.getItemList());
		this.grdContent.setAdapter(adapter);*/
	}

	/**
	 * collapse
	 */
	public void collapse() {
//		this.imgBackground.setImageResource(R.drawable.accordion_item);
		this.grdContent.setVisibility(View.GONE);
//		scrollView.setVisibility(View.GONE);
	}
	
	/**
	 * expand
	 */
	public void expand() {
//		this.imgBackground.setImageResource(R.drawable.accordion_item_expand);
		this.grdContent.setVisibility(View.VISIBLE);
		
		//fill gridview 
		if (first) {
			
//			cacheList = DataService.getChildCategories(item.getId());
			new CacheList(super.getContext()).start();
			first = false;
		} else {
			
			GridAdapter adapter = new GridAdapter(super.getContext());
			
			//init gridView
			adapter.init(cacheList);
			this.grdContent.setAdapter(adapter);
			
		}
		
//		scrollView.setVisibility(View.VISIBLE);
	}
	
	//get sub category
	class CacheList extends Thread {
		Context ctx;
		public CacheList(Context context) {
			this.ctx = context;
		}
		@Override
		public void run() {
			List cachedList = DataService.getFriendsByGroup(item.getId(), currentPage, ctx);
			Message msg = mHandler.obtainMessage();
			msg.arg1 = 0;
			msg.obj = cachedList;
			cacheList = cachedList;
			mHandler.sendMessage(msg);
		}
		
	}
	
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case 0:
				GridAdapter adapter = new GridAdapter(AccordionItemView.super.getContext());
				if (cacheList == null) {
					first = true;
				}
				//init gridView
				adapter.init(cacheList);
				AccordionItemView.this.grdContent.setAdapter(adapter);
				break;

			default:
				break;
			}
		}
		
	};
	/**
	 * @return valid if content is visible
	 */
	public boolean isExpanded() {
		return this.grdContent.getVisibility() == View.VISIBLE;
	}

	
	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}
	
	public void setOnEditClickListener(OnEditClickListener onEditClickListener) {
		this.onEditClickListener = onEditClickListener;
	}	

	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	public static interface OnItemClickListener {
		public abstract void onItemClick(GroupFriend item);
	}
	
	public static interface OnEditClickListener {
		public abstract void onEditClick(GroupFriend item);
	}
	public static interface OnClickListener {
		public abstract void onClick(AccordionItemView view, GroupFriend item);
	}
}
