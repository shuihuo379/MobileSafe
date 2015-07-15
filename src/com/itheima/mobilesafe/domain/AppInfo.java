package com.itheima.mobilesafe.domain;

import android.graphics.drawable.Drawable;

/**
 * Ӧ�ó�����Ϣ��ҵ��bean
 */
public class AppInfo {
	private Drawable icon;
	private String name; //Ӧ�ó��������
	private String packname; //Ӧ�ó���İ���
	private boolean inRom;  //�����ڴ������
	private boolean userApp; //�����û�������ϵͳ����
	private int uid;
	
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPackname() {
		return packname;
	}
	public void setPackname(String packname) {
		this.packname = packname;
	}
	public boolean isInRom() {
		return inRom;
	}
	public void setInRom(boolean inRom) {
		this.inRom = inRom;
	}
	public boolean isUserApp() {
		return userApp;
	}
	public void setUserApp(boolean userApp) {
		this.userApp = userApp;
	}
	
	@Override
	public String toString() {
		return "AppInfo [name=" + name + ", packname=" + packname + ", inRom="
				+ inRom + ", userApp=" + userApp + "]";
	}
}
