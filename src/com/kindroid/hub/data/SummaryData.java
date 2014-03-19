package com.kindroid.hub.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ConnectTimeoutException;

import com.kindroid.hub.proto.CommonProtoc.ItemType;
import com.kindroid.hub.proto.CommonProtoc.ItemType.Type;
import com.kindroid.hub.proto.IndexProtoc.OverviewRequest;
import com.kindroid.hub.proto.IndexProtoc.OverviewResponse;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.RingOrWallPaper;
import com.kindroid.hub.proto.WeiboContentProtoc.WeiboContent;
import com.kindroid.hub.utils.Constant;
import com.kindroid.hub.utils.ConvertUtils;
import com.kindroid.hub.utils.HttpRequest;

public class SummaryData {

	public List<WeiboContent> getNewsOrJokeList(boolean isJoke) {
		OverviewRequest.Builder request = OverviewRequest.newBuilder();

		ItemType.Builder type = ItemType.newBuilder();
		if (isJoke) {
			request.addItemTypes(type.setType(Type.FUNNY));
		} else {
			request.addItemTypes(type.setType(Type.NEWS));
		}
		try {
			InputStream in = HttpRequest.postData(Constant.SUMMARY_URL,request.build().toByteArray());
			if (in != null) {
				OverviewResponse resp = OverviewResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
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
	
	public List<RingOrWallPaper> getRingOrWallpaperList(boolean isRing) {
		OverviewRequest.Builder request = OverviewRequest.newBuilder();

		ItemType.Builder type = ItemType.newBuilder();
		if(!isRing) {
			request.addItemTypes(type.setType(Type.WALLPAPER));
		} else {
			request.addItemTypes(type.setType(Type.RING));
		}
		try {
			InputStream in = HttpRequest.postData(Constant.SUMMARY_URL,request.build().toByteArray());
			if (in != null) {
				OverviewResponse resp = OverviewResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				List<RingOrWallPaper> list=resp.getRingOrWallPaperList();
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
