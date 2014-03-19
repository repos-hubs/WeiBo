package com.kindroid.hub.entity;

import java.util.List;

public class GroupFriend {
	private String id;
    private String name;
    private String icon;
    private List<GroupFriend> itemList;
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public List<GroupFriend> getItemList() {
		return this.itemList;
	}
	public void setItemList(List<GroupFriend> itemList) {
		this.itemList = itemList;
	}
	public GroupFriend(String id, String name, String icon, List<GroupFriend> itemList) {
		super();
		this.id = id;
		this.name = name;
		this.icon = icon;
		this.itemList = itemList;
	}
	public GroupFriend() {
		super();
		// TODO Auto-generated constructor stub
	}
}
