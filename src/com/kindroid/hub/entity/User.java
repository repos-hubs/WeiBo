package com.kindroid.hub.entity;

public class User {

	private String username;
	private String nickname;
	private String password;
	private String telephone;
	private String email;
	private byte[] avatar;
	
	private int gender;
	private String address;
	private String avatarUrl;
	private int approvedMobile;
	private int approvedEmail;
	private int approvedSinaWeibo;
	private int approvedTencentWeibo;
	private String sinaWeibo;
	
	
	public String getSinaWeibo() {
		return sinaWeibo;
	}
	public void setSinaWeibo(String sinaWeibo) {
		this.sinaWeibo = sinaWeibo;
	}
	public String getAvatarUrl() {
		return avatarUrl;
	}
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
	public int getGender() {
		return gender;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getApprovedMobile() {
		return approvedMobile;
	}
	public void setApprovedMobile(int approvedMobile) {
		this.approvedMobile = approvedMobile;
	}
	public int getApprovedEmail() {
		return approvedEmail;
	}
	public void setApprovedEmail(int approvedEmail) {
		this.approvedEmail = approvedEmail;
	}
	public int getApprovedSinaWeibo() {
		return approvedSinaWeibo;
	}
	public void setApprovedSinaWeibo(int approvedSinaWeibo) {
		this.approvedSinaWeibo = approvedSinaWeibo;
	}
	public int getApprovedTencentWeibo() {
		return approvedTencentWeibo;
	}
	public void setApprovedTencentWeibo(int approvedTencentWeibo) {
		this.approvedTencentWeibo = approvedTencentWeibo;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public byte[] getAvatar() {
		return avatar;
	}
	public void setAvatar(byte[] avatar) {
		this.avatar = avatar;
	}
	
}
