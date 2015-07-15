package com.itheima.mobilesafe.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewDebug.ExportedProperty;
import android.widget.TextView;

/**
 * 
 * @author Administrator
 * �Զ���һ��TextView һ�����ͻ�ȡ������
 * ע��:һ����ڲ����ļ��еĹ��췽��,��ִ�еڶ������췽��
 */
public class FocusedTextView extends TextView {
	public FocusedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FocusedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FocusedTextView(Context context) {
		super(context);
	}

	/*
	 * ��ǰ��û�н���,ֻ����ƭ��Androidϵͳ
	 */
	@Override
	@ExportedProperty(category= "focus")
	public boolean isFocused() {
		return true;
	}
}
