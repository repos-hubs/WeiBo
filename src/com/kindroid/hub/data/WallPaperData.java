package com.kindroid.hub.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ConnectTimeoutException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.kindroid.hub.proto.CommonProtoc.ItemType;
import com.kindroid.hub.proto.CommonProtoc.PageHelper;
import com.kindroid.hub.proto.CommonProtoc.WallPaperOrRingType;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.ListRequest;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.ListResponse;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.OperateRequest;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.ReviewListRequest;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.ReviewListResponse;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.RingOrWallPaper;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.OperateRequest.Type;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.ReviewListRequest.Builder;
import com.kindroid.hub.proto.StyleBoxProtoc.CreateReviewResponse;
import com.kindroid.hub.proto.StyleBoxProtoc.ShowItemResponse;
import com.kindroid.hub.ui.category.wallpaper.WallPaperReviewBean;
import com.kindroid.hub.ui.category.wallpaper.WallpaperOrRingBean;
import com.kindroid.hub.utils.Constant;
import com.kindroid.hub.utils.ConvertUtils;
import com.kindroid.hub.utils.HttpRequest;

public class WallPaperData {

	/****
	 * 
	 * @param isRing  判断是 铃音 还是壁纸
	 * @param typeValue 分类 最新 推荐 最热 等等
	 * @param pageNum  每页显示的条数
	 * @param page	 第几页
	 * @return
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws UnknownHostException 
	 * @throws ConnectTimeoutException 
	 * @throws Exception 应用扑捉异常确保 联网是否正确
	 */
	public static List<RingOrWallPaper> getRingOrWallpaperList(boolean isRing,
			WallPaperOrRingType.Type typeValue, int pageNum, int page) throws ConnectTimeoutException, UnknownHostException, ParserConfigurationException, IOException   {
		
		ListRequest.Builder request = ListRequest.newBuilder();
		ItemType.Builder itemType = ItemType.newBuilder();
		WallPaperOrRingType.Builder subType = WallPaperOrRingType.newBuilder();
		subType.setType(WallPaperOrRingType.Type.NEWEST);
		if (!isRing) {
			itemType.setType(com.kindroid.hub.proto.CommonProtoc.ItemType.Type.WALLPAPER);
		} else {
			itemType.setType(com.kindroid.hub.proto.CommonProtoc.ItemType.Type.RING);
		}
		request.setItemType(itemType);
		request.setType(subType);
		
		//分页
		PageHelper.Builder pageHelper = PageHelper.newBuilder();
		pageHelper.setCount(pageNum);
		pageHelper.setIndex(page);
		request.setPageHelper(pageHelper);
		
		InputStream in = HttpRequest.postData(Constant.RING_OR_WALLPAPER_LIST_URL, request.build().toByteArray());
		if (in != null) {
			ListResponse resp = ListResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
			List<RingOrWallPaper> list = resp.getRingOrWallPaperList();
			return list;
		}

		return null;
	}
	
	public static String getImageString(String url) throws IOException{
		return HttpRequest.getData(url);
	}
	
	public static byte[] getImage(String url) throws Exception{
		
		//byte[] aa = Base64.decodeBase64(HttpRequest.getData(url).getBytes())		
		return HttpRequest.getData(url).getBytes();
	}
	
	public static Bitmap getBitMap(String url) throws Exception 
	{
		byte[] data=getImage(url);
		return BitmapFactory.decodeByteArray(data, 0, data.length);
	}
	
	/***
	 * 查找评论
	 * @param imageId
	 * @param isWallpaper
	 * @return
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws UnknownHostException 
	 * @throws ConnectTimeoutException 
	 */
	public static WallPaperReviewBean getWallPaperReviewBean(long imageId,boolean isWallpaper,int pageNum, int page) {
		
		WallPaperReviewBean bean=new WallPaperReviewBean();
		Builder request = ReviewListRequest.newBuilder();
		request.setId(imageId);
		ItemType.Builder itemType = ItemType.newBuilder();
		if(isWallpaper){
			itemType.setType(com.kindroid.hub.proto.CommonProtoc.ItemType.Type.WALLPAPER);
		}else{
			itemType.setType(com.kindroid.hub.proto.CommonProtoc.ItemType.Type.RING);
		}
		request.setItemType(itemType);
		//分页
		PageHelper.Builder pageHelper = PageHelper.newBuilder();
		pageHelper.setCount(pageNum);
		pageHelper.setIndex(page);
		request.setPageHelper(pageHelper);
		
		
		try {
			InputStream in = HttpRequest.postData(Constant.RING_OR_WALLPAPER_REVIEW_LIST_URL, request.build().toByteArray());
			if (in != null) {
				ReviewListResponse resp = ReviewListResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				//评论
				bean.setListReview(resp.getReviewList());
				bean.setForwardedCount(resp.getForwardedCount());//当前壁纸或者铃声被转发的次数
				bean.setReviewedCount(resp.getReviewedCount());//当前壁纸或者铃声被评论的次数
			}
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return bean;		
	}
	
	/***
	 * 
	 * @param weibo
	 * @param review
	 * @param replyContent
	 * @param type
	 * @param id 壁纸或者铃音的Id
	 * @return
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws UnknownHostException 
	 * @throws ConnectTimeoutException 
	 */
	public static Map<String, Integer> sendReplyContent(WallpaperOrRingBean bean) throws ConnectTimeoutException, UnknownHostException, ParserConfigurationException, IOException {
		Map<String, Integer> resultMap = new HashMap<String, Integer>();
		OperateRequest.Builder request = OperateRequest.newBuilder();
		ItemType.Builder itemType = ItemType.newBuilder();
		if (bean.getType() == "wallpaper" || "wallpaper".equals(bean.getType())) {
			itemType.setType(ItemType.Type.WALLPAPER);
		} else {
			itemType.setType(ItemType.Type.RING);
		}
		//回复评论
		if (bean.getReview() != null) {
			request.setWeiboContentId(bean.getReview().getReviewId());
		} else {
			request.setWeiboContentId(0);
		}
		if("forward"==bean.getReplyType() || "forward".equals(bean.getReplyType())){
			request.setType(Type.FORWARD);
		}else{
			request.setType(Type.REVIEW);
		}
		
		try {
			request.setItemType(itemType);
			request.setId(bean.getId());
			request.setToken(bean.getToken());
			request.setContent(bean.getReplyContent());
			
			InputStream in = HttpRequest.postData(com.kindroid.hub.utils.Constant.RING_OR_WALLPAPER_OPERATION_URL,request.build().toByteArray());
			if (in != null) {
				CreateReviewResponse reponse = CreateReviewResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (reponse.getResponse().hasResult()) {
					resultMap.put("isSuccess", reponse.getResponse().getResult().getNumber());
				} else {
					resultMap.put("isSuccess", 2);
				}
				resultMap.put("reviewCount", reponse.getCountReviewByContent());
			}
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return resultMap;
	}
	

}
