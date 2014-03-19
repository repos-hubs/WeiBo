package com.kindroid.hub.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ConnectTimeoutException;

import weibo4android.Comment;
import weibo4android.Count;
import weibo4android.Paging;
import weibo4android.Status;
import weibo4android.UserTrend;
import weibo4android.Weibo;
import weibo4android.WeiboException;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.kindroid.hub.entity.GroupFriend;
import com.kindroid.hub.entity.User;
import com.kindroid.hub.proto.CommonProtoc.FromType;
import com.kindroid.hub.proto.CommonProtoc.FromType.TypeFrom;
import com.kindroid.hub.proto.CommonProtoc.ItemType;
import com.kindroid.hub.proto.CommonProtoc.ItemType.Type;
import com.kindroid.hub.proto.CommonProtoc.PageHelper;
import com.kindroid.hub.proto.CommonProtoc.WallPaperOrRingType;
import com.kindroid.hub.proto.GroupProtoc.CreateGroupRequest;
import com.kindroid.hub.proto.GroupProtoc.CreateGroupResponse;
import com.kindroid.hub.proto.GroupProtoc.Group;
import com.kindroid.hub.proto.GroupProtoc.ListFriend2GroupRequest;
import com.kindroid.hub.proto.GroupProtoc.ListFriend2GroupResponse;
import com.kindroid.hub.proto.GroupProtoc.ListGroup4AccountRequest;
import com.kindroid.hub.proto.GroupProtoc.ListGroup4AccountResponse;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.ListRequest;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.ListResponse;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.RingOrWallPaper;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.SearchRequest;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.SearchResponse;
import com.kindroid.hub.proto.StyleBoxProtoc.CreateReleaseRequest;
import com.kindroid.hub.proto.StyleBoxProtoc.CreateReleaseResponse;
import com.kindroid.hub.proto.StyleBoxProtoc.CreateReviewRequest;
import com.kindroid.hub.proto.StyleBoxProtoc.CreateReviewResponse;
import com.kindroid.hub.proto.StyleBoxProtoc.ShowItemRequest;
import com.kindroid.hub.proto.StyleBoxProtoc.ShowItemResponse;
import com.kindroid.hub.proto.StyleBoxProtoc.ShowReivewRequest;
import com.kindroid.hub.proto.StyleBoxProtoc.ShowReviewResponse;
import com.kindroid.hub.proto.UserProtoc.Account;
import com.kindroid.hub.proto.UserProtoc.BindingEmailRequest;
import com.kindroid.hub.proto.UserProtoc.BindingEmailRequest.Operation;
import com.kindroid.hub.proto.UserProtoc.BindingResponse;
import com.kindroid.hub.proto.UserProtoc.DetailRequest;
import com.kindroid.hub.proto.UserProtoc.DetailResponse;
import com.kindroid.hub.proto.UserProtoc.EditNickNameRequest;
import com.kindroid.hub.proto.UserProtoc.EditResponse;
import com.kindroid.hub.proto.UserProtoc.EditSexRequest;
import com.kindroid.hub.proto.WeiboContentProtoc.Review;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;
import com.kindroid.hub.ui.NewsDetailsActivity;
import com.kindroid.hub.utils.Constant;
import com.kindroid.hub.utils.ConvertUtils;
import com.kindroid.hub.utils.HttpRequest;

public class DataService {

	private static final String TAG = "API";
	public static String pageTag = "";
	private static Weibo weibo = null;

	public static List<GroupFriend> getGroupsByUser(int pageIndex, Context ctx) {
		List<GroupFriend> data = new ArrayList<GroupFriend>();
		ListGroup4AccountRequest.Builder request = ListGroup4AccountRequest.newBuilder();
		PageHelper.Builder page = PageHelper.newBuilder();
		page.setIndex(pageIndex);
		page.setCount(15);
		request.setPageHelper(page);
		String token = UserDefaultInfo.getUserToken(ctx);
		request.setToken(token);
		try {
			Log.d(TAG, "group list----->:1");
			InputStream in = HttpRequest.postData(com.kindroid.hub.utils.Constant.LIST_GROUPS_URL, request.build().toByteArray());
			if (in != null) {
				ListGroup4AccountResponse reponse = ListGroup4AccountResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (reponse.getResponse().getResult().getNumber() == 1) {
					List tmpList = new ArrayList();
					tmpList = reponse.getGroupList();
					if (tmpList != null && tmpList.size() > 0) {
						for (int i = 0; i < tmpList.size(); i++) {
							GroupFriend groupFriend = new GroupFriend();
							Group group = (Group) tmpList.get(i);
							String id = group.getId() + "";
							String name = group.getName();
							groupFriend.setId(id);
							groupFriend.setName(name);
							data.add(groupFriend);
						}
					}
					Log.d(TAG, "group list----->:" + data.size());
					if (data != null && data.size() > 0) {
						return data;
					}
				} else {
					Log.d(TAG, "group list----->:failure");
				}
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
		return data;
	}

	public static List<GroupFriend> getFriendsByGroup(String groupId, int pageIndex, Context ctx) {
		List<GroupFriend> data = new ArrayList<GroupFriend>();
		ListFriend2GroupRequest.Builder request = ListFriend2GroupRequest.newBuilder();
		PageHelper.Builder page = PageHelper.newBuilder();
		page.setIndex(pageIndex);
		page.setCount(15);
		request.setPageHelper(page);
		request.setGroupId(Long.valueOf(groupId));
		String token = UserDefaultInfo.getUserToken(ctx);
		request.setToken(token);
		try {
			Log.d(TAG, "friend list----->:1");
			InputStream in = HttpRequest.postData(com.kindroid.hub.utils.Constant.LIST_FRIENDS_BY_GROUP_URL, request.build().toByteArray());
			if (in != null) {
				ListFriend2GroupResponse reponse = ListFriend2GroupResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (reponse.getResponse().getResult().getNumber() == 1) {
					List tmpList = new ArrayList();
					tmpList = reponse.getFriendList();
					if (tmpList != null && tmpList.size() > 0) {
						for (int i = 0; i < tmpList.size(); i++) {
							GroupFriend groupFriend = new GroupFriend();
							Account account = (Account) tmpList.get(i);
							String id = account.getAccountId() + "";
							String name = account.getNickName();
							String avatar = account.getIconUrl();
							groupFriend.setId(id);
							groupFriend.setName(name);
							groupFriend.setIcon(avatar);
							data.add(groupFriend);
						}
					}
					Log.d(TAG, "friend list----->:" + data.size());
					if (data != null && data.size() > 0) {
						return data;
					}
				} else {
					Log.d(TAG, "group list----->:failure");
				}
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
		return data;
	}
	
	public static String createGroup(String groupName, Context ctx) {
		String result = "";
		CreateGroupRequest.Builder request = CreateGroupRequest.newBuilder();
		String token = UserDefaultInfo.getUserToken(ctx);
		request.setToken(token);
		request.setGroupName(groupName);
		
		try {
			InputStream in = HttpRequest.postData(com.kindroid.hub.utils.Constant.CREATE_GROUP_URL, request.build().toByteArray());
			if (in != null) {
				CreateGroupResponse reponse = CreateGroupResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (reponse.getResponse().getResult().getNumber() == 1) {
					result = "success";
				} else {
					result = "failure";
				}
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
		return result;
	}
	
	/**
	 * 获取新闻列表
	 * 
	 * @param pageIndex
	 * @param ctx
	 * @return
	 */
	public static List<WeiboContent> getNewsListData(int pageIndex, int type, int dataType, boolean isBrowseMode, Context ctx,String categoryType) {
		List<WeiboContent> list = new ArrayList<WeiboContent>();
		ShowItemRequest.Builder request = ShowItemRequest.newBuilder();
		ItemType.Builder itemType = ItemType.newBuilder();
		if (type == ItemType.Type.NEWS.getNumber()) {
			itemType.setType(ItemType.Type.NEWS);
		}
		if (type == ItemType.Type.FUNNY.getNumber()) {
			itemType.setType(ItemType.Type.FUNNY);
		}
		if (type == ItemType.Type.BEAUTY.getNumber()) {
			itemType.setType(ItemType.Type.BEAUTY);
		}
		if (type == ItemType.Type.CONSTELLATION.getNumber()) {
			itemType.setType(ItemType.Type.CONSTELLATION);
		}
		if (type == ItemType.Type.RECREATION.getNumber()) {
			itemType.setType(ItemType.Type.RECREATION);
		}
		if (type == ItemType.Type.MOVIE.getNumber()) {
			itemType.setType(ItemType.Type.MOVIE);
		}
		if (type == ItemType.Type.SPORTS.getNumber()) {
			itemType.setType(ItemType.Type.SPORTS);
		}
		if (type == ItemType.Type.TECHNOLOGY.getNumber()) {
			itemType.setType(ItemType.Type.TECHNOLOGY);
		}
		if (type == ItemType.Type.GAME.getNumber()) {
			itemType.setType(ItemType.Type.GAME);
		}
		if(type==ItemType.Type.FASHION.getNumber()){
			itemType.setType(ItemType.Type.FASHION);
		}
		if(type==ItemType.Type.STREET.getNumber()){
			itemType.setType(ItemType.Type.STREET);
		}
		if(type==ItemType.Type.SAYING.getNumber()){
			itemType.setType(ItemType.Type.SAYING);
		}
		if(type==ItemType.Type.PET.getNumber()){
			itemType.setType(ItemType.Type.PET);
		}
		if(type==ItemType.Type.CAR.getNumber()){
			itemType.setType(ItemType.Type.CAR);
		}
		if(type==ItemType.Type.ENGLISH.getNumber()){
			itemType.setType(ItemType.Type.ENGLISH);
		}
		if(type==ItemType.Type.TRAVEL.getNumber()){
			itemType.setType(ItemType.Type.TRAVEL);
		}
		if(type==ItemType.Type.BUSINESS.getNumber()){
			itemType.setType(ItemType.Type.BUSINESS);
		}
		if(type==ItemType.Type.CREATIVE.getNumber()){
			itemType.setType(ItemType.Type.CREATIVE);
		}

		FromType.Builder fromType = FromType.newBuilder();
		if (dataType == 0) {
			fromType.setTypeFrom(TypeFrom.SINA);
		} else if (dataType == 1) {
			fromType.setTypeFrom(TypeFrom.TT);
		} else if (dataType == 2) {
			fromType.setTypeFrom(TypeFrom.WY);
		} else if (dataType == 3) {
			fromType.setTypeFrom(TypeFrom.SH);
		} else {
			fromType.setTypeFrom(TypeFrom.SINA);
		}
		request.setSwitchPic(isBrowseMode);
		PageHelper.Builder page = PageHelper.newBuilder();
		page.setIndex(pageIndex);
		page.setCount(10);
		request.setPageHelper(page);
		request.setItemType(itemType);
		request.setFromType(fromType);
		request.setPageFlg(pageTag);
		if (categoryType != null) {
			request.setByItemComtent(categoryType);
		}
		try {
			InputStream in = HttpRequest.postData(com.kindroid.hub.utils.Constant.GET_ITEMS_URL, request.build().toByteArray());
			if (in != null) {
				ShowItemResponse response = ShowItemResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				list = response.getWeiboContentList();
				pageTag = response.getPageFlg();
				return list;
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
		return list;
	}

	/**
	 * 获取新闻列表
	 * 
	 * @param pageIndex
	 * @param ctx
	 * @return
	 */
	public static List<WeiboContent> getNewsListData(int pageIndex, int type, int dataType, boolean isBrowseMode, Context ctx) {
		return getNewsListData(pageIndex,type,dataType,isBrowseMode,ctx,null);
	}
	
	public static List<WeiboContent> searchWeiboListData(int pageIndex, String keyword, Context ctx, int from) {
		List<WeiboContent> list = new ArrayList<WeiboContent>();
		ShowItemRequest.Builder request = ShowItemRequest.newBuilder();
		
		if (from == ItemType.Type.NEWS.getNumber()) {
			request.setItemType(ItemType.newBuilder().setType(Type.NEWS));
		} else if (from == ItemType.Type.FUNNY.getNumber()) {
			request.setItemType(ItemType.newBuilder().setType(Type.FUNNY));
		} else if (from == ItemType.Type.BEAUTY.getNumber()) {
			request.setItemType(ItemType.newBuilder().setType(Type.BEAUTY));
		} else if (from == ItemType.Type.BULUO.getNumber()) {
			request.setItemType(ItemType.newBuilder().setType(Type.BULUO));
		}
		
		request.setKeyWord(keyword);
		PageHelper.Builder pageHelper = PageHelper.newBuilder();     
		pageHelper.setIndex(pageIndex);
		pageHelper.setCount(5);
		request.setPageHelper(pageHelper);

		Log.d(TAG, "page--------->:" + pageIndex);
		try {
			InputStream in = null;
			if (from == ItemType.Type.BULUO.getNumber()) {
				in = HttpRequest.postData(com.kindroid.hub.utils.Constant.SEARCH_WEIBOS_URL, request.build().toByteArray());
			} else {
				in = HttpRequest.postData(com.kindroid.hub.utils.Constant.SEARCH_HOT_TRIBE_URL, request.build().toByteArray());
			}
			if (in != null) {
				ShowItemResponse reponse = ShowItemResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				list = reponse.getWeiboContentList();
				return list;
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
		return list;
	}

	/**
	 * 获取搞笑列表
	 * 
	 * @param pageIndex
	 * @param ctx
	 * @return
	 */
	public static List<WeiboContent> getLaughListData(int pageIndex, Context ctx) {
		List<WeiboContent> list = new ArrayList<WeiboContent>();
		ShowItemRequest.Builder request = ShowItemRequest.newBuilder();
		ItemType.Builder itemType = ItemType.newBuilder();
		itemType.setType(ItemType.Type.FUNNY);
		PageHelper.Builder page = PageHelper.newBuilder();
		page.setIndex(pageIndex);
		page.setCount(15);
		request.setPageHelper(page);
		request.setItemType(itemType);
		Log.d(TAG, "page--------->:" + pageIndex);
		try {
			InputStream in = HttpRequest.postData(
					com.kindroid.hub.utils.Constant.GET_ITEMS_URL, request
							.build().toByteArray());
			if (in != null) {
				ShowItemResponse reponse = ShowItemResponse.parseFrom(Base64
						.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				list = reponse.getWeiboContentList();
				return list;
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
		return list;
	}

	/**
	 * 获取美女列表
	 * 
	 * @param pageIndex
	 * @param ctx
	 * @return
	 */
	public static List<WeiboContent> getBeautiesListData(int pageIndex,
			Context ctx) {
		List<WeiboContent> list = new ArrayList<WeiboContent>();
		ShowItemRequest.Builder request = ShowItemRequest.newBuilder();
		ItemType.Builder itemType = ItemType.newBuilder();
		itemType.setType(ItemType.Type.BEAUTY);
		PageHelper.Builder page = PageHelper.newBuilder();
		page.setIndex(pageIndex);
		page.setCount(15);
		request.setPageHelper(page);
		request.setItemType(itemType);
		Log.d(TAG, "page--------->:" + pageIndex);
		try {
			InputStream in = HttpRequest.postData(
					com.kindroid.hub.utils.Constant.GET_ITEMS_URL, request
							.build().toByteArray());
			if (in != null) {
				ShowItemResponse reponse = ShowItemResponse.parseFrom(Base64
						.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				list = reponse.getWeiboContentList();
				return list;
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
		return list;
	}

	/**
	 * 获取评论列表
	 * 
	 * @param weiboId
	 * @param pageIndex
	 * @param ctx
	 * @return
	 */
	public static List<Review> getCommentsListData(long weiboId, int pageIndex, Context ctx, int pageSize) {
		List<Review> list = new ArrayList<Review>();
		ShowReivewRequest.Builder request = ShowReivewRequest.newBuilder();
		ItemType.Builder itemType = ItemType.newBuilder();
		itemType.setType(ItemType.Type.CHANNEL);
		request.setContentId(weiboId);
		request.setItemType(itemType);
		//pagination
		PageHelper.Builder page = PageHelper.newBuilder();
		page.setIndex(pageIndex);
		page.setCount(pageSize);
		request.setPageHelper(page);
		Log.d(TAG, "weibo id ---->" + weiboId);
		try {
			InputStream in = HttpRequest.postData(com.kindroid.hub.utils.Constant.GET_COMMENTS_URL, request.build().toByteArray());
			if (in != null) {
				ShowReviewResponse response = ShowReviewResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				// if (reponse.getResponse().getResult().getNumber() == 1) {
				NewsDetailsActivity.forwardCount = response.getCountForwardCount();
				NewsDetailsActivity.reviewCount = response.getReviewsCount();
				list = response.getReviewsList();
				Log.d(TAG, "counts----->:" + response.getCountForwardCount() + "---->" + response.getReviewsCount());
				if (list != null && list.size() > 0) {
					return list;
				}
				
				// }
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
		return list;
	}

	/**
	 * get ring or wallpaper list, such as newest、top、recommend and so on.
	 * 
	 * @param isRing
	 * @param typeValue
	 *            subCategory
	 * @param requestCount
	 * @param pageIndex
	 * @return
	 */
	public static List<RingOrWallPaper> getRingOrWallpaperList(boolean isRing,
			WallPaperOrRingType.Type typeValue, int requestCount, int pageIndex) {
		ListRequest.Builder request = ListRequest.newBuilder();
		ItemType.Builder itemType = ItemType.newBuilder();
		WallPaperOrRingType.Builder subType = WallPaperOrRingType.newBuilder();
		subType.setType(typeValue);
		if (!isRing) {
			itemType
					.setType(com.kindroid.hub.proto.CommonProtoc.ItemType.Type.WALLPAPER);
			request.setItemType(itemType);
		} else {
			itemType
					.setType(com.kindroid.hub.proto.CommonProtoc.ItemType.Type.RING);
			request.setItemType(itemType);
		}
		request.setType(subType);
		PageHelper.Builder page = PageHelper.newBuilder();
		page.setCount(requestCount);
		page.setIndex(pageIndex);
		request.setPageHelper(page);
		try {
			InputStream in = HttpRequest.postData(
					Constant.RING_OR_WALLPAPER_LIST_URL, request.build()
							.toByteArray());
			if (in != null) {
				ListResponse resp = ListResponse.parseFrom(Base64
						.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				List<RingOrWallPaper> list = resp.getRingOrWallPaperList();
				return list;
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
		return null;
	}

	/**
	 * @param weibo
	 * @param review
	 * @param replyContent
	 * @return
	 */
	public static Map<String, Integer> sendReplyContent(WeiboContent weibo, Review review, String replyContent, Context ctx) {
		Map<String, Integer> resultMap = new HashMap<String, Integer>();
		CreateReviewRequest.Builder request = CreateReviewRequest.newBuilder();
		ItemType.Builder itemType = ItemType.newBuilder();
		itemType.setType(ItemType.Type.CHANNEL);
		request.setContentId(weibo.getContentId());
		if (review != null) {
			request.setReviewId(review.getReviewId());
		} else {
			request.setReviewId(0);
		}
		request.setItemType(itemType);
		String token = UserDefaultInfo.getUserToken(ctx);
		
		request.setToken(token);
		request.setContent(replyContent);

		try {
			InputStream in = HttpRequest.postData(
					com.kindroid.hub.utils.Constant.SEND_REPLY_CONTENT_URL,
					request.build().toByteArray());
			if (in != null) {
				CreateReviewResponse reponse = CreateReviewResponse
						.parseFrom(Base64.decodeBase64(ConvertUtils
								.InputStreamToByte(in)));
				if (reponse.getResponse().hasResult()) {
					resultMap.put("isSuccess", reponse.getResponse().getResult().getNumber());
				} else {
					resultMap.put("isSuccess", 9);
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
	
	/**
	 * 获取搜索结果列表
	 * @param isRing 是否是铃声
	 * @param key 查询关键字
	 * @param pageIndex 分页索引
	 * @param pageSize 请求数据个数
	 * @return
	 */
	public static List<RingOrWallPaper> getSearchListData(boolean isRing, String key, int pageIndex, int pageSize) {
		List<RingOrWallPaper> list = null;
		SearchRequest.Builder request = SearchRequest.newBuilder();
		ItemType.Builder itemType = ItemType.newBuilder();
		if (isRing) itemType.setType(ItemType.Type.RING);
		else itemType.setType(ItemType.Type.WALLPAPER);
		request.setType(itemType);
		//pagination
		PageHelper.Builder page = PageHelper.newBuilder();
		page.setIndex(pageIndex);
		page.setCount(pageSize);
		request.setPageHelper(page);
		request.setKeyword(key);
		Log.d(TAG, "search key ---->" + key);
		try {
			InputStream in = HttpRequest.postData(
					com.kindroid.hub.utils.Constant.RING_OR_WALLPAPER_SEARCH_URL, request
							.build().toByteArray());
			if (in != null) {
				SearchResponse reponse = SearchResponse
						.parseFrom(Base64.decodeBase64(ConvertUtils
								.InputStreamToByte(in)));
				// if (reponse.getResponse().getResult().getNumber() == 1) {

				list = reponse.getResultList();
				Log.d(TAG, "search list----->:" + list.size());
				if (list != null && list.size() > 0) {
					return list;
				}
				// }
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
		return list;
	}
	
	public static String releaseWeibo(String weiboContent, Context ctx) {
		String result = "";
		CreateReleaseRequest.Builder request = CreateReleaseRequest.newBuilder();
		String token = UserDefaultInfo.getUserToken(ctx);
		request.setToken(token);
		request.setContent(weiboContent);
		request.setContentId("0");
		request.setForwardAccountId(0L);
		request.setForwarId(0L);
		
		try {
			InputStream in = HttpRequest.postData(com.kindroid.hub.utils.Constant.CREATE_RELEASE_URL, request.build().toByteArray());
			if (in != null) {
				CreateReleaseResponse reponse = CreateReleaseResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (reponse.getResponse().getResult().getNumber() == 1) {
					result = "success";
				} else {
					result = "failure";
				}
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
		return result;
	}
	
	public static String forwardWeibo(String forwardContent, WeiboContent weibo, Context ctx) {
		String result = "";
		CreateReleaseRequest.Builder request = CreateReleaseRequest.newBuilder();
		String token = UserDefaultInfo.getUserToken(ctx);
		request.setToken(token);
		request.setContent(forwardContent);
		request.setContentId(weibo.getContentId() + "");
		request.setForwardAccountId(0L);
		if (weibo.getCategory().getCategoryId() == 8) {
			request.setForwardAccountId(weibo.getItem().getItemId());
		}
		request.setForwarId(weibo.getContentId());
		
		try {
			InputStream in = HttpRequest.postData(com.kindroid.hub.utils.Constant.CREATE_RELEASE_URL, request.build().toByteArray());
			if (in != null) {
				CreateReleaseResponse reponse = CreateReleaseResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (reponse.getResponse().getResult().getNumber() == 1) {
					NewsDetailsActivity.forwardCount = reponse.getCountRealseCount();
					result = "success";
				} else {
					result = "failure";
				}
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
		return result;
	}
	
	public static User getUserDetails(Context ctx) {
		User userInfo = new User();
		DetailRequest.Builder request = DetailRequest.newBuilder();
		String token = UserDefaultInfo.getUserToken(ctx);
		request.setToken(token);
		
		try {
			InputStream in = HttpRequest.postData(com.kindroid.hub.utils.Constant.GET_USER_DETAIL_URL, request.build().toByteArray());
			if (in != null) {
				DetailResponse response = DetailResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (response.getResponse().getResult().getNumber() == 1) {
					userInfo.setAddress(response.getAddress());
					userInfo.setEmail(response.getAccount().getEmail());
					userInfo.setAvatarUrl(response.getAccount().getIconUrl());
					userInfo.setGender(response.getSex());
					userInfo.setSinaWeibo(response.getSinaWeiboUserName());
					userInfo.setTelephone(response.getAccount().getPhone());
					userInfo.setNickname(response.getAccount().getNickName());
					Log.d(TAG, "nick name--------------->" + response.getAccount().getNickName());
					userInfo.setApprovedEmail(response.getApproved2Email());
					userInfo.setApprovedMobile(response.getApproved2Mobile());
					userInfo.setApprovedSinaWeibo(response.getApproved2SinaWeibo());
					userInfo.setApprovedTencentWeibo(response.getApproved2TTWeibo());
					
				} else {
					
				}
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
		return userInfo;
	}
	
	public static String modifyNickName(String nickName, Context ctx) {
		String result = "";
		EditNickNameRequest.Builder request = EditNickNameRequest.newBuilder();
		String token = UserDefaultInfo.getUserToken(ctx);
		request.setToken(token);
		request.setNickName(nickName);
				
		try {
			InputStream in = HttpRequest.postData(com.kindroid.hub.utils.Constant.MODIFY_NICK_NAME_URL, request.build().toByteArray());
			if (in != null) {
				EditResponse reponse = EditResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (reponse.getResponse().getResult().getNumber() == 1) {
					result = "success";
				} else {
					result = "failure";
				}
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
		return result;
	}
	
	public static String modifyGender(int gender, Context ctx) {
		String result = "";
		EditSexRequest.Builder request = EditSexRequest.newBuilder();
		String token = UserDefaultInfo.getUserToken(ctx);
		request.setToken(token);
		request.setSex(gender);
		
		try {
			InputStream in = HttpRequest.postData(com.kindroid.hub.utils.Constant.MODIFY_GENDER_URL, request.build().toByteArray());
			if (in != null) {
				EditResponse reponse = EditResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (reponse.getResponse().getResult().getNumber() == 1) {
					result = "success";
				} else {
					result = "failure";
				}
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
		return result;
	}
	
	public static String emailBinding(String email, Context ctx) {
		String result = "";
		BindingEmailRequest.Builder request = BindingEmailRequest.newBuilder();
		String token = UserDefaultInfo.getUserToken(ctx);
		request.setToken(token);
		request.setEmail(email);
		request.setOperation(Operation.BINDING);
		
		try {
			InputStream in = HttpRequest.postData(com.kindroid.hub.utils.Constant.BINDING_EMAIL_URL, request.build().toByteArray());
			if (in != null) {
				BindingResponse reponse = BindingResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (reponse.getResponse().getResult().getNumber() == 1) {
					result = "success";
				} else {
					result = "failure";
				}
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
		return result;
	}
	
	public static List<Count> getForwardsAndComments(String weiboId, Context ctx) {
		List<Count> resultList = new ArrayList<Count>();
		if (weibo == null) {
			
			weibo = new Weibo();
			String token = UserDefaultInfo.getWeiboToken(ctx);
			String tokenSecret = UserDefaultInfo.getWeiboTokenSecret(ctx);
			weibo.setToken(token, tokenSecret);
		}
		try {
			resultList = weibo.getCounts(weiboId);
		} catch (WeiboException e) {
			e.printStackTrace();
		}
		return resultList;
	}
	
	public static List<Status> getStatusList(Context ctx, int pageIndex) {
		List<Status> resultList = new ArrayList<Status>();
		if (weibo == null) {
			
			weibo = new Weibo();
			String token = UserDefaultInfo.getWeiboToken(ctx);
			String tokenSecret = UserDefaultInfo.getWeiboTokenSecret(ctx);
			weibo.setToken(token, tokenSecret);
		}
		try {
			Paging page = new Paging();
			page.setPage(pageIndex);
			page.setCount(15);
			resultList = weibo.getMentions(page);
		} catch (WeiboException e) {
			e.printStackTrace();
		}
		return resultList;
	}
	
	public static List<Status> getTopicsStatusDataList(int startIndex, String topicName, int pageSize, Context ctx) {
		List<Status> resultList = new ArrayList<Status>();
		if (weibo == null) {
			
			weibo = new Weibo();
			String token = UserDefaultInfo.getWeiboToken(ctx);
			String tokenSecret = UserDefaultInfo.getWeiboTokenSecret(ctx);
			weibo.setToken(token, tokenSecret);
		}
		try {
			Paging page = new Paging();
			page.setPage(startIndex);
			page.setCount(pageSize);
			resultList = weibo.getTrendStatus(topicName, page);
		} catch (WeiboException e) {
			e.printStackTrace();
		}
		return resultList;
	}
	
	public static List<Comment> getCommentsList(Context ctx, int pageIndex) {
		List<Comment> resultList = new ArrayList<Comment>();
		if (weibo == null) {
			
			weibo = new Weibo();
			String token = UserDefaultInfo.getWeiboToken(ctx);
			String tokenSecret = UserDefaultInfo.getWeiboTokenSecret(ctx);
			weibo.setToken(token, tokenSecret);
		}
		try {
			Paging page = new Paging();
			page.setPage(pageIndex);
			page.setCount(15);
			resultList = weibo.getCommentsToMe(page);
		} catch (WeiboException e) {
			e.printStackTrace();
		}
		return resultList;
	}
	
	public static List<Comment> getCommentsList(Context ctx, String weiboId, int pageIndex, int pageSize) {
		List<Comment> resultList = new ArrayList<Comment>();
		if (weibo == null) {
			
			weibo = new Weibo();
			String token = UserDefaultInfo.getWeiboToken(ctx);
			String tokenSecret = UserDefaultInfo.getWeiboTokenSecret(ctx);
			weibo.setToken(token, tokenSecret);
		}
		try {
			Paging page = new Paging();
			page.setPage(pageIndex);
			page.setCount(pageSize);
			Log.v(TAG, "---------------------->" + weiboId);
			resultList = weibo.getComments(weiboId, page);
		} catch (WeiboException e) {
			e.printStackTrace();
		}
		return resultList;
	}
	
	public static List<UserTrend> getTopicsDataList(int startIndex, String userId, int pageSize, Context ctx) {
		List<UserTrend> resultList = new ArrayList<UserTrend>();
		userId = "2168626813";
		if (weibo == null) {
			
			weibo = new Weibo();
			String token = UserDefaultInfo.getWeiboToken(ctx);
			String tokenSecret = UserDefaultInfo.getWeiboTokenSecret(ctx);
			weibo.setToken(token, tokenSecret);
		}
		try {
			Paging page = new Paging();
			page.setPage(startIndex);
			page.setCount(pageSize);
			Log.v(TAG, "---------------------->" + userId);
			resultList = weibo.getTrends(userId, page);
		} catch (WeiboException e) {
			e.printStackTrace();
		}
		return resultList;
	}
	
	public static String releaseWeiboToSina(String weiboContent, Context ctx) {
		String result = "";
		if (weibo == null) {
			weibo = new Weibo();
			String token = UserDefaultInfo.getWeiboToken(ctx);
			String tokenSecret = UserDefaultInfo.getWeiboTokenSecret(ctx);
			weibo.setToken(token, tokenSecret);
		}
		try {
			Status status = weibo.updateStatus(weiboContent);
			if (status.getCreatedAt() != null) {
				result = "success";
			}
		} catch (WeiboException e) {
			e.printStackTrace();
		}
		return result;
		
	}
	
	public static String sendReplyContentToSina(String commentContent, String weiboId, String commentId, String type, Context ctx) {
		String result = "";
		if (weibo == null) {
			weibo = new Weibo();
			String token = UserDefaultInfo.getWeiboToken(ctx);
			String tokenSecret = UserDefaultInfo.getWeiboTokenSecret(ctx);
			weibo.setToken(token, tokenSecret);
		}
		try {
			Comment comment = null;
			if (!TextUtils.isEmpty(type) && type.equals("comment")) {
				comment = weibo.updateComment(commentContent, weiboId, null);
			} else if (!TextUtils.isEmpty(type) && type.equals("reply")) {
				comment = weibo.updateComment(commentContent, weiboId, commentId);
			}
			if (comment != null && comment.getCreatedAt() != null) {
				result = "success";
			} else {
				result = "failure";
			}
		} catch (WeiboException e) {
			e.printStackTrace();
		}
		return result;
		
	}
	public static String forwardWeiboToSina(String forwardContent, String weiboId, Context ctx) {
		String result = "";
		if (weibo == null) {
			weibo = new Weibo();
			String token = UserDefaultInfo.getWeiboToken(ctx);
			String tokenSecret = UserDefaultInfo.getWeiboTokenSecret(ctx);
			weibo.setToken(token, tokenSecret);
		}
		try {
			Status status = null;
			status = weibo.repost(weiboId, forwardContent);
			if (status != null && status.getCreatedAt() != null) {
				result = "success";
			} else {
				result = "failure";
			}
		} catch (WeiboException e) {
			e.printStackTrace();
		}
		return result;
		
	}
	
	public static String collectWeibo(String weiboId, Context ctx) {
		String result = "";
		if (weibo == null) {
			weibo = new Weibo();
			String token = UserDefaultInfo.getWeiboToken(ctx);
			String tokenSecret = UserDefaultInfo.getWeiboTokenSecret(ctx);
			weibo.setToken(token, tokenSecret);
		}
		try {
			Status status = null;
			status = weibo.createFavorite(Long.valueOf(weiboId));
			if (status != null && status.getCreatedAt() != null) {
				result = "success";
			} else {
				result = "failure";
			}
		} catch (WeiboException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String modifyNickNameToSina(String nickName, Context ctx) {
		String result = "";
		if (weibo == null) {
			weibo = new Weibo();
			String token = UserDefaultInfo.getWeiboToken(ctx);
			String tokenSecret = UserDefaultInfo.getWeiboTokenSecret(ctx);
			weibo.setToken(token, tokenSecret);
		}
		try {
			weibo4android.User user = weibo.updateProfile(nickName, "", "", "", "");
			if (user != null && user.getCreatedAt() != null) {
				result = "success";
			} else {
				result = "failure";
			}
		} catch (WeiboException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String locatingAddress(String ip, Context ctx) {
		String result = "";
			String appKey = Constant.CONSUMER_KEY;
			String url = "http://api.map.sina.com.cn/geocode/ip_to_geo.json?ip=" + ip + "&source=" + appKey;
			try {
				result = HttpRequest.getData(url);
				Log.d(TAG, "============ip0000000=======>" + url);
				Log.d(TAG, "==========================>" + result);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		return result;
	}
	
	public static Status getWeiboConentById(long weiboId, Context ctx) {
		Status status = null;
		if (weibo == null) {
			weibo = new Weibo();
			String token = UserDefaultInfo.getWeiboToken(ctx);
			String tokenSecret = UserDefaultInfo.getWeiboTokenSecret(ctx);
			weibo.setToken(token, tokenSecret);
		}
		
		try {
			status = weibo.showStatus(weiboId);
		} catch (WeiboException e) {
			e.printStackTrace();
		}
		return status;
	}
}
