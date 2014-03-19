package com.kindroid.hub.adapter;

import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kindroid.hub.R;
import com.kindroid.hub.ui.category.WallpaperMain;
import com.kindroid.hub.ui.category.wallpaper.Constant;
/***
 * 本地壁纸 适配器
 * @author huaiyu.zhao
 *
 */
public class MyWallPaperAdapter extends BaseAdapter {
	
	private LayoutInflater mLayInflater;
	
	private WallpaperMain mContext;
	private List<Map<String,Object>> mBean;

	public MyWallPaperAdapter(WallpaperMain context,List<Map<String,Object>> list){
		mContext=context;
		mBean=list;
		mLayInflater = LayoutInflater.from(context);
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
	public void setData(List<Map<String,Object>> data){
		this.mBean=data;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		Map<String,Object> map=mBean.get(position);
		RelativeLayout layout = (RelativeLayout)mLayInflater.inflate(R.layout.wallpaper_myitem, null);
		
		ImageView wallpaper_artwork=(ImageView)layout.findViewById(R.id.wallpaper_artwork); //图片
		TextView wallpaper_share_person=(TextView)layout.findViewById(R.id.wallpaper_share_person);//图片名称
		ImageView wallpaper_del=(ImageView)layout.findViewById(R.id.wallpaper_del); //删除按钮
		
		setViewImage(wallpaper_artwork, map.get("bitmap"));
		
		wallpaper_share_person.setText(map.get("name").toString());
		setViewImage(wallpaper_del, map.get("del"));		
		
		layout.findViewById(R.id.wallpaper_artwork).setTag(position);
		wallpaper_artwork.setOnClickListener(mContext);
		
		layout.findViewById(R.id.wallpaper_del).setTag(position);
		wallpaper_del.setOnClickListener(mContext);
		
		return layout;
	}
	

	private void setViewImage(ImageView v, Object data){
		if (data instanceof Integer) {
			v.setImageResource((Integer)data);			
		} else if(data instanceof Bitmap) {			
			v.setImageBitmap((Bitmap)data);
		} else  if(data instanceof String ){
			v.setImageResource(R.drawable.pic_default);
			//Constant.setImage(v, (String)data);
		}
	}
}
