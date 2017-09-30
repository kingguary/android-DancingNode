package com.my.dn.data;

import java.util.List;

public class Edge {
	public List<String> labels; // 绘图时拼成字符串，英文逗号隔开

	public String source;

	public String target;

	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("[source=").append(source).append(" target=").append(target).append(" labels=");
		for (int i = 0; i < labels.size(); i++) {
			if (0 != i) {
				strBuilder.append(",");
			}
			strBuilder.append(labels.get(i));
		}
		strBuilder.append("]");
		return strBuilder.toString();
	}
}
