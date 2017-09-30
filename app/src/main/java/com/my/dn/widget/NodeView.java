package com.my.dn.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.my.dn.R;
import com.my.dn.data.Node;

public class NodeView extends RelativeLayout {

	private static final String TAG = "NodeView";

	private Node node;
	private boolean isLocal;

	private CircleTextView mTextView;
	private ImageButton mImgCloase;

	private boolean isDiscoverView;

	public NodeView(Context context, Node node, boolean isLocal) {
		super(context, null);
		this.node = node;
		this.isLocal = isLocal;
		// 在构造函数中将Xml中定义的布局解析出来。
		LayoutInflater.from(context).inflate(R.layout.node_item, this, true);
		initViews();
	}

	private void initViews() {
		mTextView = (CircleTextView) findViewById(R.id.text);
		mImgCloase = (ImageButton) findViewById(R.id.img_del);
		mTextView.setText(node.name);
		mTextView.setTextSize(10);
		mTextView.setLines(5);
		if(isLocal){
			mTextView.setTextColor(getContext().getResources().getColor(R.color.B1));
		}else{
			mTextView.setTextColor(getContext().getResources().getColor(R.color.B1));
		}
		mTextView.setGravity(Gravity.CENTER);
	}

	public Node getNode() {
		return this.node;
	}

	public void setIsDiscoverView(boolean isDiscover) {
		isDiscoverView = isDiscover;
	}

	public boolean isDiscoverView() {
		return isDiscoverView;
	}

	public void setBackgroundResource(int resId) {
		mTextView.setBackgroundResource(resId);
	}

	public void enterEditMode() {
		mImgCloase.setVisibility(View.VISIBLE);
	}

	public void exitEditMode() {
		mImgCloase.setVisibility(View.INVISIBLE);
	}
}
