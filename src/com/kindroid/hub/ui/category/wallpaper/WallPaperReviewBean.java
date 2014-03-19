package com.kindroid.hub.ui.category.wallpaper;

import java.util.List;

import com.kindroid.hub.proto.WeiboContentProtoc.Review;
/***
 * 壁纸或者铃音 的评论次数
 * @author huaiyu.zhao
 *
 */
public class WallPaperReviewBean {

	private List<Review> listReview; //评论
	private int forwardedCount; //转发的次数
	private int reviewedCount; //评论的次数
	public List<Review> getListReview() {
		return listReview;
	}
	public void setListReview(List<Review> listReview) {
		this.listReview = listReview;
	}
	public int getForwardedCount() {
		return forwardedCount;
	}
	public void setForwardedCount(int forwardedCount) {
		this.forwardedCount = forwardedCount;
	}
	public int getReviewedCount() {
		return reviewedCount;
	}
	public void setReviewedCount(int reviewedCount) {
		this.reviewedCount = reviewedCount;
	}
	
	
	
}
