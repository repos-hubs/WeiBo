package com.kindroid.hub.adapter;

import java.util.List;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kindroid.hub.R;
import com.kindroid.hub.ui.category.WallpaperSearch;
import com.kindroid.hub.ui.category.wallpaper.ImageDownloader;
import com.kindroid.hub.ui.category.wallpaper.WallPaperDB.WallpaperBean;

public class WallpaperSearckAdapter extends BaseAdapter  {
	
	private LayoutInflater mLayInflater;
	
	private WallpaperSearch mContext;
	private List<WallpaperBean> mBean;
	
	
	public WallpaperSearckAdapter(WallpaperSearch context, List<WallpaperBean> bean){
		mContext = context;		
		mLayInflater = LayoutInflater.from(context);
		mBean = bean;
	}
	
	@Override
	public int getCount() {
		if (mBean==null)
			return 0;
		
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
	public void setData(List<WallpaperBean> data){
		mBean=data;
		notifyDataSetChanged();
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		WallpaperBean bean=mBean.get(position);
		LinearLayout layout = (LinearLayout)mLayInflater.inflate(R.layout.wallpaper_preview_item, null);
		
		
		ImageView wallpaper_image = (ImageView)layout.findViewById(R.id.wallpaper_image);
		TextView wallpaper_share_person = (TextView)layout.findViewById(R.id.wallpaper_share_person);
		TextView wallpaper_share_time = (TextView)layout.findViewById(R.id.wallpaper_share_time);
		TextView wallpaper_title_name = (TextView)layout.findViewById(R.id.wallpaper_title_name);
		ImageView wallpaper_enlarge = (ImageView)layout.findViewById(R.id.wallpaper_enlarge);
		ImageView wallpaper_comment = (ImageView)layout.findViewById(R.id.wallpaper_comment);
		ImageView wallpaper_share = (ImageView)layout.findViewById(R.id.wallpaper_share);
	
		if(bean.getUser_img()==null || bean.getUser_img().length<=0){
			setViewImage(wallpaper_image,bean.getUser_url(),R.drawable.user_default);//用户头像
		}else{
			setViewImage(wallpaper_image,bean.getUser_img(),R.drawable.user_default);//用户头像
		}
		
		
		wallpaper_share_person.setText(bean.getUser_name());//用户名称
		wallpaper_share_time.setText(bean.getWallpaper_time()); //发布时间		
		wallpaper_title_name.setText(bean.getTitle()); //图片名称
		try {
			if(bean.getImageBitMap()==null){
				setViewImage(wallpaper_enlarge, bean.getImage_url(),R.drawable.pic_default);//图片
			}else{
				setViewImage(wallpaper_enlarge, bean.getImageBitMap(),R.drawable.pic_default);//图片
			}
		} catch (Exception e) {
			setViewImage(wallpaper_enlarge, bean.getImage_url(),R.drawable.pic_default);//图片
		}
	
		
		setViewImage(wallpaper_comment, bean.getCommentImage(),R.drawable.wallpaper_one_share_background);
		setViewImage(wallpaper_share,bean.getShareImage(),R.drawable.wallpaper_one_comment_background);

		layout.findViewById(R.id.wallpaper_comment).setTag(position);
		layout.findViewById(R.id.wallpaper_share).setTag(position);
		layout.findViewById(R.id.wallpaper_enlarge).setTag(position);
		wallpaper_enlarge.setOnClickListener(mContext);
		wallpaper_comment.setOnClickListener(mContext);
		wallpaper_share.setOnClickListener(mContext);
		
		return layout;
	}

	private final ImageDownloader imageDownloader = new ImageDownloader();

	private void setViewImage(ImageView v, Object data,int resId){
	    if(data instanceof String) {
            imageDownloader.download((String)data, (ImageView) v);
        } else if (data instanceof Integer) {
			v.setImageResource((Integer)data);			
		} else if(data instanceof Bitmap) {			
			v.setImageBitmap((Bitmap)data);
		} else {
			v.setImageResource(resId);			
		}
	}
}
