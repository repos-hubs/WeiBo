package com.kindroid.hub.ui.category.wallpaper;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.conn.ConnectTimeoutException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.kindroid.hub.R;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.data.WallPaperData;
import com.kindroid.hub.proto.CommonProtoc.WallPaperOrRingType;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.RingOrWallPaper;
import com.kindroid.hub.ui.category.WallpaperSearch;
import com.kindroid.hub.ui.category.wallpaper.WallPaperDB.WallpaperBean;


/***
 * wallpaper 缓存 下载 添加数据库 文件操作
 * @author huaiyu.zhao
 *
 */
public class WallPaperService 
{
	
	//private WallPaperData wallPaperData=null;
	private Context mContext=null;

	public static final int SUBJECTRECOMMANDITEM=1; //推荐
	public static final int NEWORDERITEM=2; //最新
	public static final int HOTORDERITEM=3; //最热
	public static final int LOCALORDERITEM=4; //本地	
	public static final int CREATEDB=5; //创建数据库
	
	public static final int WALLPAPER_SEARCH=-1;// 搜索
	
	private static final int PAGENUM=5; //每次读取多少条	
	public static final int UPDATEDB=6; //更新数据库 为最新
	public static final int UPDATEDATA=7; //加载下页数据
	
	public WallPaperService(Context context){
		mContext=context;
	}
	

	private WallPaperOrRingType.Type gettypeValue(int type)
	{
		switch (type) {
		case SUBJECTRECOMMANDITEM:
			//最新
			return  WallPaperOrRingType.Type.NEWEST;
		case NEWORDERITEM:
			//最新
			return  WallPaperOrRingType.Type.TOP;	
		case HOTORDERITEM:
			//最新
			return  WallPaperOrRingType.Type.OTHER;
		default:
			//最新
			return  WallPaperOrRingType.Type.NEWEST;
			
		}
	}
	

	

	private WallPaperOrRingType.Type getType(int type){
		WallPaperOrRingType.Type typeValue=WallPaperOrRingType.Type.NEWEST;
		switch (type) {
		case SUBJECTRECOMMANDITEM:
			typeValue=WallPaperOrRingType.Type.TOP;
			break;
		case NEWORDERITEM:
			typeValue=WallPaperOrRingType.Type.NEWEST;
			break;
		case HOTORDERITEM:
			typeValue=WallPaperOrRingType.Type.OTHER;
			break;	
		default:
			break;
		}
		return typeValue;
	}
	
	/**
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws UnknownHostException 
	 * @throws ConnectTimeoutException *
	 * 设置 init值
	 * @throws
	 */
	public synchronized List<WallpaperBean> queryItem(int type,int imageType) throws ConnectTimeoutException, UnknownHostException, ParserConfigurationException, IOException      {
		return queryItem(type,imageType,"");
	}
	
	/***
	 * 
	 * @param type 操作分类
	 * @param imageType 壁纸分类
	 * @param name 搜索名称
	 * @return
	 * @throws ConnectTimeoutException
	 * @throws UnknownHostException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public synchronized List<WallpaperBean> queryItem(int type,int imageType,String name) throws ConnectTimeoutException, UnknownHostException, ParserConfigurationException, IOException      {
		
		
		//需要返回的结果集
		List<WallpaperBean> listMap=new ArrayList<WallpaperBean>();
		SQLiteDatabase db=null;
		WallPaperDB.WallPaperDao dao;
		List<WallpaperBean> listBean;
		List<RingOrWallPaper> httpList;
		List<WallpaperBean> listOld = MyWallpaperRes.getResources(imageType);
		System.out.println("===========imageType="+imageType);
		if(listOld==null){
			listOld=new ArrayList<WallpaperBean>();
		}
		
		
		int page=listOld.size()/PAGENUM;		
		switch (type) {
		
		case UPDATEDATA:
			if(imageType==WALLPAPER_SEARCH){
				if(null==name || "".equals(name)){
					name=WallpaperSearch.inputName;
				}			
				//加载分页数据				
				httpList = DataService.getSearchListData(false, name, page,PAGENUM );
				//httpList = WallPaperData.getRingOrWallpaperList(false, getType(imageType),PAGENUM,page);
				for(RingOrWallPaper wallpaper:httpList) {
					WallpaperBean bean=WallpaperBean.wallPaperTobean(wallpaper,imageType);
					if(!listMap.contains(bean)) {
						listMap.add(bean);
					}
				}
			}else{
				//加载分页数据
				System.out.println("==============联网 查询");
				httpList = WallPaperData.getRingOrWallpaperList(false, getType(imageType),PAGENUM,page);
				for(RingOrWallPaper wallpaper:httpList) {
					WallpaperBean bean=WallpaperBean.wallPaperTobean(wallpaper,imageType);
					if(!listMap.contains(bean)) {
						listMap.add(bean);
					}
				}
			}
			
	
			break;			
//		case LOCALORDERITEM:			
//			try {
//				//
//				db = WallPaperDB.getMOpenHelperDb(mContext).getWritableDatabase();
//				dao=new WallPaperDB.WallPaperDao();
//				listBean = dao.query(imageType);				
//				if(listBean!=null && !listBean.isEmpty())
//				{
//					System.out.println("==============加载本地图片");
//					for(WallpaperBean bean:listBean){
//						bean.setCommentImage(R.drawable.wallpaper_one_share_background);
//						bean.setShareImage(R.drawable.wallpaper_one_comment_background);
//						//排重
//						if(!listMap.contains(bean)){
//							listMap.add(bean);
//						}
//					}
//				}
//				
//			} finally{
//				db.close();
//			}
//			break;
		//初始化 数据库	
		case CREATEDB:
			
			if(imageType==WALLPAPER_SEARCH){
				System.out.println("==============联网 查询");
				httpList =DataService.getSearchListData(false, name,PAGENUM , 0);					
				for(RingOrWallPaper wallpaper:httpList) {	
					WallpaperBean bean=WallpaperBean.wallPaperTobean(wallpaper,imageType);
					if(!listMap.contains(bean)){
						listMap.add(bean);
					}
				}
			}else{
				//创建数据库和 建立链接
				try {
					db = WallPaperDB.getMOpenHelperDb(mContext).getWritableDatabase();
					dao=new WallPaperDB.WallPaperDao();
					listBean = dao.query(imageType);				
					if(listBean!=null && !listBean.isEmpty()) {
						System.out.println("==============读取数据库");
						for(WallpaperBean bean:listBean){
							bean.setCommentImage(R.drawable.wallpaper_one_share_background);
							bean.setShareImage(R.drawable.wallpaper_one_comment_background);
							//排重
							if(!listMap.contains(bean)){
								listMap.add(bean);
							}
						}
						//更新数据库数据为最新
						new WallpaperDbThread(null,imageType,false).start();
					} else {
						System.out.println("==============联网 查询");
						httpList = WallPaperData.getRingOrWallpaperList(false, getType(imageType),PAGENUM,0);
						
						//进行 插入数据库
						new WallpaperDbThread(httpList, imageType,true).start();
						// dao.addBeginTransaction(list, type);
						for(RingOrWallPaper wallpaper:httpList) {	
							WallpaperBean bean=WallpaperBean.wallPaperTobean(wallpaper,imageType);
							if(!listMap.contains(bean)){
								listMap.add(bean);
							}
						}
					}
				} finally {
					db.close();
				}
			}
			break;
			
			case UPDATEDB:
			//------数据库为最新显示的数据
				List<WallpaperBean> listTop=new ArrayList<WallpaperBean>();
				try {
					System.out.println("==============联网 查询更新数据库为最新消息");					
					httpList= WallPaperData.getRingOrWallpaperList(false, getType(imageType),PAGENUM,0);
					//int i=dao.addBeginTransaction(list, type);
					//如果事务执行成功 进行更新最前面的数据
					new WallpaperDbThread(httpList, type,true).start();					
					for(RingOrWallPaper wallpaper:httpList) {	
						WallpaperBean bean=WallpaperBean.wallPaperTobean(wallpaper,imageType);				
						listTop.add(bean);
					}
				} finally{
					
					if(listTop!=null && !listTop.isEmpty()) {
						List<WallpaperBean> old=new ArrayList<WallpaperBean>();
						//listMap=listOld;
						if(listOld!=null && !listOld.isEmpty()){
							old=listOld.subList(listTop.size()-1, listOld.size());
							listOld.clear();
							listOld.addAll(listTop);
							listOld.addAll(old);
							
						}else{
							listOld.addAll(listTop);
						}
						listMap=new ArrayList<WallpaperBean>();
					}
				}
			break;
		default:
			break;
		}
		
		
		//排重返回最新数据
		if(listMap!=null && !listMap.isEmpty()) {
			for(WallpaperBean map:listMap) {
				if(!listOld.contains(map)) {
					listOld.add(map);
				}
			}
		}
		listMap.clear();		
		MyWallpaperRes.setResources(listOld,imageType);
		listOld.clear();
		return MyWallpaperRes.getResources(imageType);
	}
	

	
	//启动线程添加数据库
	class WallpaperDbThread extends Thread {	
		private WallPaperDB.WallPaperDao mdao;
		private List<RingOrWallPaper> mList;
		private int mType;
		private boolean removeFooterView;
		public WallpaperDbThread(List<RingOrWallPaper> list,int type,boolean updateDb) {
			mdao=new WallPaperDB.WallPaperDao();	
			mList=list;
			mType=type;
			removeFooterView=updateDb;
		}		
		public void run() {
			if(removeFooterView){
				mdao.addBeginTransaction(mList, mType);
				//mdao.updateBeginTransaction(mType);
			}else{
				try {
					System.out.println("==========更新数据库=======");
					//queryItem(UPDATEDB,mType);
				} catch (Exception e) {
					
				}
			}
			
		}
	}
	
	public static class  MyWallpaperRes{
		private static List<WallpaperBean> subjectRecommandItem = new ArrayList<WallpaperBean>();
		private static List<WallpaperBean> newOrderItem = new ArrayList<WallpaperBean>();
		private static List<WallpaperBean> hotOrderItem = new ArrayList<WallpaperBean>();
		private static List<WallpaperBean> localOrderItem = new ArrayList<WallpaperBean>();
		private static List<WallpaperBean> searchWallpaper=new ArrayList<WallpaperBean>(); //搜索壁纸
		
		public static void setResources(List<WallpaperBean> list,int imageType){
			switch (imageType) {
			case SUBJECTRECOMMANDITEM:
				subjectRecommandItem =new ArrayList<WallpaperBean>();
				subjectRecommandItem.addAll(list);
				break;
			case NEWORDERITEM:
				newOrderItem =new ArrayList<WallpaperBean>();
				newOrderItem.addAll(list);
				break;
			case HOTORDERITEM:
				
				hotOrderItem =new ArrayList<WallpaperBean>();
				hotOrderItem.addAll(list);
				break;
			case LOCALORDERITEM:
				
				localOrderItem =new ArrayList<WallpaperBean>();
				localOrderItem.addAll(list);
				break;
			case WALLPAPER_SEARCH:				
				searchWallpaper =new ArrayList<WallpaperBean>();
				searchWallpaper.addAll(list);
				break;
			default:
				
				subjectRecommandItem =new ArrayList<WallpaperBean>();
				subjectRecommandItem.addAll(list);
				break;
			}
		}
		/***
		 * 返回对应的资源图片
		 * @param type
		 * @return
		 */
		public static List<WallpaperBean> getResources(int type)
		{
			switch (type) {
			case SUBJECTRECOMMANDITEM:
				return subjectRecommandItem;
			case NEWORDERITEM:
				return newOrderItem;
			case HOTORDERITEM:
				return hotOrderItem;
			case LOCALORDERITEM:
				return localOrderItem;
			case WALLPAPER_SEARCH:
				return searchWallpaper;
			default:
				System.out.println("========default=====");
				return null;
			}
		}
	}
	
}
