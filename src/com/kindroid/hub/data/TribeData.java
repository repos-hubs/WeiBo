package com.kindroid.hub.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ConnectTimeoutException;

import com.kindroid.hub.proto.CommonProtoc.ItemType;
import com.kindroid.hub.proto.CommonProtoc.PageHelper;
import com.kindroid.hub.proto.CommonProtoc.ItemType.Type;
import com.kindroid.hub.proto.StyleBoxProtoc.ShowItemRequest;
import com.kindroid.hub.proto.StyleBoxProtoc.ShowItemResponse;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;
import com.kindroid.hub.utils.Constant;
import com.kindroid.hub.utils.ConvertUtils;
import com.kindroid.hub.utils.HttpRequest;

public class TribeData extends BaseData{

	public List<WeiboContent> getHotDynamicList(int startIndex) {
		ShowItemRequest.Builder request = ShowItemRequest.newBuilder();

		ItemType.Builder itemType=ItemType.newBuilder();
		itemType.setType(Type.BEAUTY);
		
		request.setItemType(itemType);

		PageHelper.Builder pageHelper = PageHelper.newBuilder();
		pageHelper.setCount(PAGE_SIZE);
		pageHelper.setIndex(startIndex);
		request.setPageHelper(pageHelper);
		try {
			InputStream in = HttpRequest.postData(Constant.TRIBE_URL, request.build().toByteArray());
			if (in != null) {
				ShowItemResponse resp = ShowItemResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				List<WeiboContent> list = resp.getWeiboContentList();
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
	
	public List<WeiboContent> getTribeDynamicList(int startIndex) {
		ShowItemRequest.Builder request = ShowItemRequest.newBuilder();

		ItemType.Builder itemType=ItemType.newBuilder();
		itemType.setType(Type.HOT);
		
		request.setItemType(itemType);

		PageHelper.Builder pageHelper = PageHelper.newBuilder();
		pageHelper.setCount(PAGE_SIZE);
		pageHelper.setIndex(startIndex);
		request.setPageHelper(pageHelper);
		try {
			InputStream in = HttpRequest.postData(Constant.TRIBE_DYNAMIC_URL, request.build().toByteArray());
			if (in != null) {
				ShowItemResponse resp = ShowItemResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				List<WeiboContent> list = resp.getWeiboContentList();
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
}
