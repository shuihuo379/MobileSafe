package com.itheima.mobilesafe.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewDebug.ExportedProperty;
import android.widget.TextView;

/**
 * 
 * @author Administrator
 * 自定义一个TextView 一出生就获取到焦点
 * 注意:一般放在布局文件中的构造方法,会执行第二个构造方法
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
	 * 当前并没有焦点,只是欺骗了Android系统
	 */
	@Override
	@ExportedProperty(category= "focus")
	public boolean isFocused() {
		return true;
	}
}
