package com.itheima.mobilesafe.engine;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Debug.MemoryInfo;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.domain.TaskInfo;

/**
 * �ṩ�ֻ�����Ľ�����Ϣ
 * @author Administrator
 */
public class TaskInfoProvider {
	/**
	 * ��ȡ���еĽ�����Ϣ
	 * @param context ������
	 * @return
	 */
	public static List<TaskInfo> getTaskInfos(Context context){
		List<TaskInfo> taskInfos=new ArrayList<TaskInfo>();
		
		ActivityManager am=(ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		PackageManager pm=context.getPackageManager();
		List<RunningAppProcessInfo> processInfos=am.getRunningAppProcesses();
		
		for(RunningAppProcessInfo processInfo:processInfos){
			TaskInfo taskInfo=new TaskInfo();
			String packName=processInfo.processName; //��ȡ������(��Ӧ�ó���İ���)
			MemoryInfo[] memoryInfos=am.getProcessMemoryInfo(new int[]{processInfo.pid});
			long memsize=memoryInfos[0].getTotalPrivateDirty()*1024;
			taskInfo.setPackname(packName);
			taskInfo.setMemsize(memsize);
			
			try {
				ApplicationInfo info=pm.getApplicationInfo(packName, 0);
				Drawable icon=info.loadIcon(pm);
				String name=info.loadLabel(pm).toString();
				taskInfo.setIcon(icon);
				taskInfo.setName(name);
				
				if((info.flags & ApplicationInfo.FLAG_SYSTEM)==0){
					//�û�����
					taskInfo.setUserTask(true);
				}else{
					//ϵͳ����
					taskInfo.setUserTask(false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				taskInfo.setIcon(context.getResources().getDrawable(R.drawable.ic_default));
				taskInfo.setName(packName);
			}
			taskInfos.add(taskInfo);
		}
		return taskInfos;
	}
}
