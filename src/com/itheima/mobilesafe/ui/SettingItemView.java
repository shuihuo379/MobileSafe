package com.itheima.mobilesafe.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itheima.mobilesafe.R;

/**
 * 自定义的组合控件(实现控件开启关闭以及描述信息的逻辑)
 * @author Administrator
 *
 */
public class SettingItemView extends RelativeLayout {
	private CheckBox cb_status;
	private TextView tv_desc;
	private TextView tv_title;
	
	private String desc_on;
	private String desc_off;
	
	public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}
	
	
	/**
	 * 带有两个参数的构造方法,布局文件使用的时候调用
	 * @param context
	 * @param attrs
	 */
	public SettingItemView(Context context, AttributeSet attrs) {
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

	public SettingItemView(Context context) {
		super(context);
		initView(context);
	}
	
	/**
	 * 初始化布局文件
	 * @param context
	 */
	private void initView(Context context) {
		View.inflate(context, R.layout.setting_item_view,this);
		cb_status = (CheckBox) this.findViewById(R.id.cb_status);
		tv_desc = (TextView) this.findViewById(R.id.tv_desc);
		tv_title = (TextView) this.findViewById(R.id.tv_title);
	}
	
	/**
	 * 校验组合控件是否被选中
	 * @return
	 */
	public boolean ischecked(){
		return cb_status.isChecked();
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
		cb_status.setChecked(checked);  
	}
	
	/**
	 * 设置 组合控件的描述信息
	 */
	
	public void setDesc(String text){
		tv_desc.setText(text);
	}
	
}
