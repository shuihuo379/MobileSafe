package com.itheima.mobilesafe.domain;

import android.graphics.drawable.Drawable;

public class TaskInfo {
	private Drawable icon;
	private String name;
	private String packname;
	private long memsize;
	private boolean checked;
	private boolean userTask; //true用户进程 false系统进程
	
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
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
	public long getMemsize() {
		return memsize;
	}
	public void setMemsize(long memsize) {
		this.memsize = memsize;
	}
	public boolean isUserTask() {
		return userTask;
	}
	public void setUserTask(boolean userTask) {
		this.userTask = userTask;
	}
	@Override
	public String toString() {
		return "TaskInfo [name=" + name + ", packname=" + packname
				+ ", memsize=" + memsize + ", userTask=" + userTask + "]";
	}
}
