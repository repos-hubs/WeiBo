package com.kindroid.hub.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kindroid.hub.R;
import com.kindroid.hub.entity.GroupFriend;
import com.kindroid.hub.utils.AsyncImageLoader;
import com.kindroid.hub.utils.AsyncImageLoader.ImageCallback;


public class CategoryItemView extends LinearLayout {
	/**
	 * category icon view
	 */
	private ImageView imgIcon;
	/**
	 * label show category name
	 */
	private TextView lblText;

	private AsyncImageLoader imgLoader;

	/**
	 * @description TODO
	 * @param context
	 * @param attrs
	 */
	public CategoryItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		// inflate view from /res/layout/accordion_item.xml
		String infServiString = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(infServiString);
		inflater.inflate(R.layout.category_item, this, true);
		// init components
		this.imgIcon = (ImageView) super.findViewById(R.id.img_icon);
		this.lblText = (TextView) super.findViewById(R.id.lbl_name);
		this.imgLoader = new AsyncImageLoader(context);
	}

	public void init(GroupFriend cate) {
		//this.setTag(cate);
		this.lblText.setText(cate.getName().replaceAll("&amp;", " & "));
		Bitmap bmp = null;
		if (!TextUtils.isEmpty(cate.getIcon())) {
			
			bmp = this.imgLoader.loadBitmap(cate.getIcon(), new ImageCallback() {
				@Override
				public void imageLoaded(Drawable drawable, int position, String url) {
				}
				@Override
				public void imageLoaded(Bitmap bitmap, int position, String url) {
				}
				@Override
				public void imageLoaded(Drawable drawable, String url) {
				}
				@Override
				public void imageLoaded(Bitmap bitmap, String url) {
					CategoryItemView.this.imgIcon.setImageBitmap(bitmap);
				}
			});
		}
		if (bmp != null) {
			this.imgIcon.setImageBitmap(bmp);
		} else {
			this.imgIcon.setImageResource(R.drawable.user_default);
		}
	}

}
