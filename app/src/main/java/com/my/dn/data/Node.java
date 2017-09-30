package com.my.dn.data;

public class Node {

	public static final String COMPANY = "Company";
	public static final String HUMAN = "Human";

	public String id; // 绘图的时候有用

	public String name; // 绘图只展示这个

	public String type; // "Company"大 "Human"小 区分大小圆圈

	public boolean isCompany() {
		return COMPANY.equals(type);
	}

	public boolean isHuman() {
		return HUMAN.equals(type);
	}
}
