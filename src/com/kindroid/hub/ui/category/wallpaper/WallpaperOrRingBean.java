package com.kindroid.hub.ui.category.wallpaper;

import com.kindroid.hub.proto.WeiboContentProtoc.Review;

public class WallpaperOrRingBean {	
	
	private Review review ;//评论 回复评论用
	private String replyContent; //评论内容
	private String type;// 分类 壁纸为 wallpaper 铃音为 ring 默认为 铃音
	private long id;	//壁纸或者铃音的ID
	private String token;  //用户Token
	private String replyType;// 是什么操作 转发 评论回复

	public Review getReview() {
		return review;
	}
	public void setReview(Review review) {
		this.review = review;
	}
	public String getReplyContent() {
		return replyContent;
	}
	public void setReplyContent(String replyContent) {
		this.replyContent = replyContent;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getReplyType() {
		return replyType;
	}
	public void setReplyType(String replyType) {
		this.replyType = replyType;
	}
	

}
