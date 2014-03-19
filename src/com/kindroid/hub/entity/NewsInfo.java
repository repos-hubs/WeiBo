package com.kindroid.hub.entity;

public class NewsInfo {
	private String newsId;
	private String newsTitle;
	private String newsIconUrl;
	private String forwardTime;
	
	public String getNewsId() {
		return newsId;
	}
	public void setNewsId(String newsId) {
		this.newsId = newsId;
	}
	public String getNewsTitle() {
		return newsTitle;
	}
	public void setNewsTitle(String newsTitle) {
		this.newsTitle = newsTitle;
	}
	public String getNewsIconUrl() {
		return newsIconUrl;
	}
	public void setNewsIconUrl(String newsIconUrl) {
		this.newsIconUrl = newsIconUrl;
	}
	public String getForwardTime() {
		return forwardTime;
	}
	public void setForwardTime(String forwardTime) {
		this.forwardTime = forwardTime;
	}
}
