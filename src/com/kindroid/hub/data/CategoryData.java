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

public class CategoryData {
	public List<WeiboContent> getRecommendAppList(int category) {
		ShowItemRequest.Builder request = ShowItemRequest.newBuilder();

		ItemType.Builder type = ItemType.newBuilder();
		if (category == 0) {
			request.setItemType(type.setType(Type.NEWS));
		} else if (category == 1) {
			request.setItemType(type.setType(Type.FUNNY));
		} else {
			request.setItemType(type.setType(Type.BEAUTY));
		}

		PageHelper.Builder page = PageHelper.newBuilder();
		page.setCount(10);
		page.setIndex(0);
		request.setPageHelper(page);
		try {
			InputStream in = HttpRequest.postData(Constant.CATEGORY_URL,request.build().toByteArray());
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
