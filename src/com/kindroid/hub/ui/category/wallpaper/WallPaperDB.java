package com.kindroid.hub.ui.category.wallpaper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.kindroid.hub.R;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.RingOrWallPaper;

public class WallPaperDB {

	protected static final String DATABASE_NAME = "hub_wallpaper.db"; // 数据库名称
	protected static final int DATABASE_VERSION = 3; // 数据库版本

	public static Context mContext;

	// 创建数据库
	public static synchronized DatabaseHelper getMOpenHelperDb(Context context) {
		mContext = context;
		return new DatabaseHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);
	}

	public static void closeDb(SQLiteDatabase mOpenHelperDb) {
		if (mOpenHelperDb != null) {
			mOpenHelperDb.close();
		}
		mOpenHelperDb = null;
	}

	// ---------------- wallpaper dao
	public static class WallPaperDao {

		private DatabaseHelper db;

		public WallPaperDao() {
			db = getMOpenHelperDb(mContext);

		}

		public WallPaperDao(Context context) {
			db = getMOpenHelperDb(context);
		}

		/***
		 * 查找
		 * 
		 * @param type
		 * @return
		 */
		public List<WallpaperBean> query(int type) {
			String sql = "select _id," + WallpaperBean.BEANALL
					+ " from wallpaper where image_type = ? and download =-1";

			Cursor cursor = null;
			List<WallpaperBean> listBean = new ArrayList<WallpaperBean>();
			try {
				cursor = db.getReadableDatabase().rawQuery(sql,
						new String[] { type + "" });

				if (cursor == null) {
					return null;
				}
				while (cursor.moveToNext()) {
					listBean.add(cursorToBean(cursor));
				}
			} finally {
				if (cursor != null) {
					cursor.close();
				}
				db.close();
			}
			return listBean;
		}

		/***
		 * 根据ID
		 * 
		 * @param id
		 * @return
		 */
		public WallpaperBean queryById(int id) {
			String sql = "select _id," + WallpaperBean.BEANALL
					+ " from wallpaper where _id= ?";
			Cursor cursor = null;
			try {
				cursor = db.getReadableDatabase().rawQuery(sql,
						new String[] { id + "" });
				if (cursor.moveToNext()) {
					return cursorToBean(cursor);
				}
			} finally {
				if (cursor != null) {
					cursor.close();
				}
				db.close();
			}
			return null;
		}

		public WallpaperBean queryImgId(long imgId) {
			String sql = "select _id," + WallpaperBean.BEANALL
					+ " from wallpaper where image_id= ?";
			Cursor cursor = null;
			try {
				cursor = db.getReadableDatabase().rawQuery(sql,
						new String[] { imgId + "" });
				if (cursor.moveToNext()) {
					return cursorToBean(cursor);
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				if (cursor != null) {
					cursor.close();
				}
				db.close();
			}
			return null;
		}

		/***
		 * 修改
		 * 
		 * @param bean
		 * @return
		 */
		public int update(WallpaperBean bean) {

			try {

			} catch (Exception e) {
				// TODO: handle exception
			}
			return 0;
		}

		/***
		 * 删除
		 * 
		 * @param bean
		 * @return
		 */
		public int del(WallpaperBean bean) {
			return 0;
		}

		/***
		 * 根据所传入的 imageType 删除所有的同类型数据
		 * 
		 * @param imageType
		 * @return
		 */
		public int delImageTye(int imageType) {

			String sql = "delete from wallpaper where imger_type=?";
			Object[] obj = new Object[] { imageType };
			try {
				db.getWritableDatabase().execSQL(sql, obj);
			} finally {
				db.close();
			}

			return 0;
		}

		/***
		 * 添加
		 * 
		 * @param bean
		 * @return
		 */
		public int add(WallpaperBean bean, SQLiteDatabase sqlDb) {
			Object[] val = new Object[] { bean.getImage_id(),
					bean.getImage_type(), bean.getUser_name(), bean.getTitle(),
					bean.getImage(), bean.getWallpaper_time(),
					bean.getImage_url(), bean.getDownload_nums(),
					bean.getImage_size(),bean.getUser_img() };

			String sql = " insert into wallpaper(image_id,image_type,user_name," +
					"title,image,wallpaper_time,image_url,download_nums,image_size,user_img )" +
					" values (?,?,?,?,?,?,?,?,?,?)";

			sqlDb.execSQL(sql, val);

			return 1;
		}

		// 添加接口数据
		public int addRingOrWallPaper(RingOrWallPaper wallpaper, int imageType,
				SQLiteDatabase sqlDb) {

			return add(ringOrWallPaperTobean(wallpaper, imageType), sqlDb);
		}

		/***
		 * 更新图片
		 * 
		 * @param imageType
		 * @return
		 */
		public int updateBeginTransaction(int imageType, Bitmap bitmap) {
			List<WallpaperBean> listBean = query(imageType);
			String sql = "update wallpaper set image=? where _id=? ";
			for (WallpaperBean bean : listBean) {
				// 图片存储为空进行 更新
				try {
					byte[] img = getWallPaperImageByte(bean.getImage_url());
					db.getWritableDatabase().execSQL(sql,
							new Object[] { img, bean.get_id() });

				} finally {
					db.close();
				}
			}

			return 1;
		}

		private byte[] getWallPaperImageByte(String url) {

			try {
				return readStream(url);
			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
		}

		public static byte[] readStream(String url) throws Exception {
			final HttpClient client = new DefaultHttpClient();
			final HttpGet getRequest = new HttpGet(url);

			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.w("ImageDownloader", "Error " + statusCode
						+ " while retrieving bitmap from " + url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				ByteArrayOutputStream outstream = null;
				try {
					inputStream = entity.getContent();
					outstream = new ByteArrayOutputStream();
					byte[] buffer = new byte[1024];
					int len = -1;
					while ((len = inputStream.read(buffer)) != -1) {
						outstream.write(buffer, 0, len);
					}
					return outstream.toByteArray();

				} finally {
					if (outstream != null) {
						outstream.close();
					}
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
			return null;
		}

		/***
		 * 进行 事务更新数据库 信息为最新信息，执行步骤为先删除原先存放在数据库的数据 然后再添加为 最新的前几条数据
		 * 
		 * @param listWallpaper
		 * @param imageType
		 * @return
		 */
		public int addBeginTransaction(List<RingOrWallPaper> listWallpaper,
				int imageType) {

			SQLiteDatabase sqlDb = db.getWritableDatabase();
			try {
				// 开启一个事务
				sqlDb.beginTransaction();
				// 进行数据库删除
				String sql = "delete from wallpaper where image_type=?";
				Object[] obj = new Object[] { imageType };
				sqlDb.execSQL(sql, obj);
				// 进行数据库添加
				for (RingOrWallPaper wallpaper : listWallpaper) {
					addRingOrWallPaper(wallpaper, imageType, sqlDb);
				}
				sqlDb.setTransactionSuccessful();
			} finally {
				sqlDb.endTransaction();
				db.close();
			}
			return 1;
		}

		/***
		 * 批量添加
		 * 
		 * @param listBean
		 * @return
		 */

		/***
		 * Cursor 对象 转换为Bean
		 * 
		 * @param cursor
		 * @return
		 */
		private WallpaperBean cursorToBean(Cursor cursor) {

			WallpaperBean bean = new WallpaperBean();
			bean.set_id(cursor.getInt(0));
			bean.setTitle(cursor.getString(1));
			bean.setArtworkUrl(cursor.getString(2));
			bean.setImage_id(cursor.getInt(3));
			bean.setImage_type(cursor.getInt(4));
			bean.setImage(cursor.getBlob(5));
			bean.setDownload(cursor.getInt(6));
			bean.setComment(cursor.getInt(7));
			bean.setForward(cursor.getInt(8));
			bean.setUser_name(cursor.getString(9));
			bean.setUser_img(cursor.getBlob(10));
			bean.setWallpager_num(cursor.getInt(11));
			bean.setWallpaper_time(cursor.getString(12));
			bean.setDownload_nums(cursor.getInt(13));
			bean.setImage_size(cursor.getFloat(14));
			return bean;
		}

		private WallpaperBean ringOrWallPaperTobean(RingOrWallPaper wallpaper,
				int imageType) {
			WallpaperBean bean = new WallpaperBean();
			bean.setImage_id(wallpaper.getId()); // 图片ID
			bean.setImage_type(imageType); // 分类
			bean.setUser_name(wallpaper.getAccount().getNickName());// 用户名
			bean.setTitle(wallpaper.getName());// 图片名称
			bean.setImage(getWallPaperImageByte(wallpaper.getDownloadUrl()));// 图片
			bean.setWallpaper_time(wallpaper.getTimeLeft());// 发布时间
			bean.setDownload_nums(wallpaper.getDownloadCount());// 下载次数
			bean.setImage_url(wallpaper.getDownloadUrlExt());// 原图地址
			bean.setImage_size(wallpaper.getLength());// 图片大小
			bean.setUser_img(getWallPaperImageByte(wallpaper.getAccount().getIconUrl()));
			
			return bean;
		}
	}

	// ------------------------数据库 操作

	public static class DatabaseHelper extends SQLiteOpenHelper {
		private final Context mContext;
		String sql = "CREATE TABLE if not exists  wallpaper ("
				+ "_id INTEGER PRIMARY KEY autoincrement," // ID 自动增长
				+ "title TEXT," // 标题
				+ "image_url TEXT," // 图片链接地址
				+ "image_type INTEGER NOT NULL DEFAULT 1," // 图片类型 推荐 1 最热 2 最新3
															// // 本地4
				+ "image BLOB," // 图片缓存地址
				+ "download INTEGER NOT NULL DEFAULT -1, " // 是否下载 已下载1 未下载 -1
				+ "comment INTEGER DEFAULT 0," // 评论
				+ "forward INTEGER DEFAULT 0," // 转发
				+ "image_id INTEGER NOT NULL ," // 图片存数据库的ID
				+ "user_name TEXT," // 上传用户名称
				+ "user_img BLOB," // 用户头像
				+ "wallpager_num INTEGER NOT NULL DEFAULT -1 ," // 图片下载后的顺序 查找倒序
				+ "wallpaper_time TEXT  DEFAULT -1 ," // 时间
				+ "download_nums INTEGER  DEFAULT 0," // 下载次数
				+ "image_size INTEGER  DEFAULT 0" // 图片大小
				+ ");";

		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);

			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(sql);
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public synchronized void close() {
			// TODO Auto-generated method stub
			super.close();
		}
	}

	// -----------java bean

	public static class WallpaperBean {
		public static final String ID = "_id";// ID 自动增长
		public static final String TITLE = "title";// 标题
		public static final String IMAGE_URL = "image_url";// 图片链接地址
		public static final String IMAGE_ID = "image_id";// 图片原数据库ID
		public static final String IMAGE_TYPE = "image_type";// 图片类型 推荐 1 最热 2
																// 最新3
		// 本地4
		public static final String IMAGE = "image";// 图片缓存地址
		public static final String DOWNLOAD = "download";// 是否下载 已下载1 未下载 -1
		public static final String COMMENT = "comment";// 评论次数
		public static final String FORWARD = "forward";// 转发次数
		public static final String USER_NAME = "user_name";// 上传者 名称;
		public static final String USER_IMG = "user_img"; // 用户头像
		public static final String WALLPAGER_NUM = "wallpager_num"; // 图片下载后的顺序
		public static final String WALLPAPER_TIME = "wallpaper_time"; // 发布时间
		public static final String DOWNLOAD_NUMS = "download_nums";// 下载次数
		public static final String IMAGE_SIZE = "image_size";// 图片大小
		// 查找倒序
		// 可以

		public static final String BEANALL = TITLE + "," + IMAGE_URL + ","
				+ IMAGE_ID + "," + IMAGE_TYPE + "," + IMAGE + "," + DOWNLOAD
				+ "," + COMMENT + "," + FORWARD + "," + USER_NAME + ","
				+ USER_IMG + "," + WALLPAGER_NUM + "," + WALLPAPER_TIME + ","
				+ DOWNLOAD_NUMS + "," + IMAGE_SIZE;

		private int _id;// ID 自动增长
		private String title;// 标题
		private String image_url;// 图片链接地址 如果为下载的图片设置为 下载存放的地址
		private long image_id;// 图片原数据库ID
		private int image_type;// 图片类型 推荐 1 最热 2 最新3 本地4
		private byte[] image;// 图片缓存地址
		private int download;// 是否下载 已下载1 未下载 -1
		private int comment;// 评论次数
		private int forward;// 转发次数
		private String user_name;// 上传者 名称;
		private byte[] user_img; // 用户头像
		private int wallpager_num; // 图片下载后的顺序 查找倒序 可以
		private String wallpaper_time;// 发布时间
		private int download_nums; // 下载次数
		private float image_size; // 图片大小
		private String artworkUrl;// 原图地址

		private int commentImage;// 评论按钮
		private int shareImage;// 分析按钮
		private int token; // 用户token
		private String user_url; //用户头像地址

		public static WallpaperBean wallPaperTobean(RingOrWallPaper wallpaper,
				int imageType) {
			WallpaperBean bean = new WallpaperBean();
			bean.setImage_id(wallpaper.getId()); // 图片ID
			bean.setImage_type(imageType); // 分类
			bean.setUser_name(wallpaper.getAccount().getNickName());// 用户名
			bean.setTitle(wallpaper.getName());// 图片名称
			bean.setWallpaper_time(wallpaper.getTimeLeft());// 发布时间
			bean.setDownload_nums(wallpaper.getDownloadCount());// 下载次数
			bean.setImage_url(wallpaper.getDownloadUrl());			
			bean.setCommentImage(R.drawable.wallpaper_one_share_background);
			bean.setShareImage(R.drawable.wallpaper_one_comment_background);
			bean.setArtworkUrl(wallpaper.getDownloadUrlExt());// 原图地址
			bean.setImage_size(wallpaper.getLength());// 壁纸大小
			bean.setUser_url(wallpaper.getAccount().getIconUrl());
			return bean;
		}

		public String getUser_url() {
			return user_url;
		}

		public void setUser_url(String userUrl) {
			user_url = userUrl;
		}

		public int getToken() {
			return token;
		}

		public void setToken(int token) {
			this.token = token;
		}

		public String getArtworkUrl() {
			return artworkUrl;
		}

		public void setArtworkUrl(String artworkUrl) {
			this.artworkUrl = artworkUrl;
		}

		public int getCommentImage() {
			return commentImage;
		}

		public void setCommentImage(int commentImage) {
			this.commentImage = commentImage;
		}

		public int getShareImage() {
			return shareImage;
		}

		public void setShareImage(int shareImage) {
			this.shareImage = shareImage;
		}

		public float getImage_size() {
			return image_size;
		}

		public void setImage_size(float imageSize) {
			image_size = imageSize;
		}

		public String getWallpaper_time() {
			return wallpaper_time;
		}

		public void setWallpaper_time(String wallpaperTime) {
			wallpaper_time = wallpaperTime;
		}

		public int getDownload_nums() {
			return download_nums;
		}

		public void setDownload_nums(int downloadNums) {
			download_nums = downloadNums;
		}

		public int get_id() {
			return _id;
		}

		public void set_id(int id) {
			_id = id;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public long getImage_id() {
			return image_id;
		}

		public void setImage_id(long imageId) {
			image_id = imageId;
		}

		public String getImage_url() {
			return image_url;
		}

		public void setImage_url(String imageUrl) {
			image_url = imageUrl;
		}

		public int getImage_type() {
			return image_type;
		}

		public void setImage_type(int imageType) {
			image_type = imageType;
		}

		public int getDownload() {
			return download;
		}

		public void setDownload(int download) {
			this.download = download;
		}

		public int getComment() {
			return comment;
		}

		public void setComment(int comment) {
			this.comment = comment;
		}

		public int getForward() {
			return forward;
		}

		public void setForward(int forward) {
			this.forward = forward;
		}

		public String getUser_name() {
			return user_name;
		}

		public void setUser_name(String userName) {
			user_name = userName;
		}

		public byte[] getImage() {
			return image;
		}

		public void setImage(byte[] image) {
			this.image = image;
		}

		public Bitmap getImageBitMap() {
			return Constant.getBitMap(image);
		}

		public Bitmap getUser_imgBitMap() {
			if(user_img==null || user_img.length<=0){
				return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.user_default);
			}else if(user_url!=null && "".equals(user_url)){
				byte[] data;
				try {
					data = WallPaperDao.readStream(user_url);
					return BitmapFactory.decodeByteArray(data, 0, data.length);
				} catch (Exception e) {
					return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.user_default);
				}
				
			}else{
				return Constant.getBitMap(user_img);
			}
		}



		public byte[] getUser_img() {
			return user_img;
		}

		public void setUser_img(byte[] userImg) {
			user_img = userImg;
		}

		public int getWallpager_num() {
			return wallpager_num;
		}

		public void setWallpager_num(int wallpagerNum) {
			wallpager_num = wallpagerNum;
		}
	}

}
