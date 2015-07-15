package com.itheima.mobilesafe.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itheima.mobilesafe.R;

/**
 * �Զ������Ͽؼ�
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
	 * �������������Ĺ��췽��,�����ļ�ʹ�õ�ʱ�����
	 * @param context
	 * @param attrs
	 */
	public SettingClickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
		
		/**
		 * ��ȡ�Զ���ؼ������Ե�ֵ
		 */
		String namespace="http://schemas.android.com/apk/res/com.itheima.mobilesafe";
		String title=attrs.getAttributeValue(namespace,"title");
		desc_on=attrs.getAttributeValue(namespace,"desc_on");  //��ȡ��״̬��������Ϣ
		desc_off=attrs.getAttributeValue(namespace,"desc_off"); //��ȡ��״̬��������Ϣ
		
		tv_title.setText(title);
		setDesc(desc_off);  //����Ĭ�ϵ�������Ϣ
	}

	public SettingClickView(Context context) {
		super(context);
		initView(context);
	}
	
	/**
	 * ��ʼ�������ļ�
	 * @param context
	 */
	private void initView(Context context) {
		View.inflate(context, R.layout.setting_click_view,this);
		tv_desc = (TextView) this.findViewById(R.id.tv_desc);
		tv_title = (TextView) this.findViewById(R.id.tv_title);
	}
	
	/**
	 * ������Ͽؼ���״̬
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
	 * ���� ��Ͽؼ���������Ϣ
	 */
	public void setDesc(String text){
		tv_desc.setText(text);
	}
	
	/**
	 * ������Ͽؼ��ı���
	 */
	public void setTitle(String title){
		tv_title.setText(title);
	}
}
