package com.itheima.mobilesafe.test;

import java.util.List;

import com.itheima.mobilesafe.domain.TaskInfo;
import com.itheima.mobilesafe.engine.TaskInfoProvider;

import android.test.AndroidTestCase;

public class TestTaskInfoProvider extends AndroidTestCase {
	public void testGetTaskInfos(){
		List<TaskInfo> infos=TaskInfoProvider.getTaskInfos(getContext());
		for(TaskInfo info:infos){
			System.out.println(info);
		}
	}
}
