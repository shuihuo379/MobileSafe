package com.itheima.mobilesafe.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itheima.mobilesafe.R;

/**
 * 自定义的组合控件
 * @author Administrator
 *
 */
public class SettingClickView extends RelativeLayout {
	private TextView tv_desc;
	private TextView tv_title;
	
	private String desc_on;
	private String desc_off;
	
	public SettingClickView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}
	
	
	/**
	 * 带有两个参数的构造方法,布局文件使用的时候调用
	 * @param context
	 * @param attrs
	 */
	public SettingClickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
		
		/**
		 * 获取自定义控件的属性的值
		 */
		String namespace="http://schemas.android.com/apk/res/com.itheima.mobilesafe";
		String title=attrs.getAttributeValue(namespace,"title");
		desc_on=attrs.getAttributeValue(namespace,"desc_on");  //获取开状态的描述信息
		desc_off=attrs.getAttributeValue(namespace,"desc_off"); //获取关状态的描述信息
		
		tv_title.setText(title);
		setDesc(desc_off);  //设置默认的描述信息
	}

	public SettingClickView(Context context) {
		super(context);
		initView(context);
	}
	
	/**
	 * 初始化布局文件
	 * @param context
	 */
	private void initView(Context context) {
		View.inflate(context, R.layout.setting_click_view,this);
		tv_desc = (TextView) this.findViewById(R.id.tv_desc);
		tv_title = (TextView) this.findViewById(R.id.tv_title);
	}
	
	/**
	 * 设置组合控件的状态
	 * @param checked
	 */
	public void setChecked(boolean checked){
		if(checked){
			setDesc(desc_on);
		}else{
			setDesc(desc_off);
		}
	}
	
	/**
	 * 设置 组合控件的描述信息
	 */
	public void setDesc(String text){
		tv_desc.setText(text);
	}
	
	/**
	 * 设置组合控件的标题
	 */
	public void setTitle(String title){
		tv_title.setText(title);
	}
}
