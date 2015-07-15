package com.itheima.mobilesafe;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima.mobilesafe.domain.TaskInfo;
import com.itheima.mobilesafe.engine.TaskInfoProvider;
import com.itheima.mobilesafe.utils.SystemInfoUtils;

public class TaskManagerActivity extends Activity {
	private TextView tv_process_count;
	private TextView tv_mem_info;
	private TextView tv_status;
	private LinearLayout ll_loading;
	private ListView lv_task_manager;
	
	private List<TaskInfo> allTaskInfos;
	private List<TaskInfo> userTaskInfos;
	private List<TaskInfo> systemTaskInfos;
	
	private TaskManagerAdapter adapter;
	
	private int processCount; //�������еĽ�������
	private long availMem;   //�����ڴ�
	private long totalMem;  //���ڴ�

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_manager);
		tv_mem_info = (TextView) findViewById(R.id.tv_mem_info);
		tv_process_count = (TextView) findViewById(R.id.tv_process_count);
		tv_status = (TextView) findViewById(R.id.tv_status);
		
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		lv_task_manager = (ListView) findViewById(R.id.lv_task_manager);
		fillData();
		
		lv_task_manager.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(userTaskInfos!=null && systemTaskInfos!=null){
					if(firstVisibleItem>userTaskInfos.size()){
						tv_status.setText("ϵͳ���̣�" + systemTaskInfos.size()+ "��");
					}else{
						tv_status.setText("�û����̣�" + userTaskInfos.size() + "��");
					}
				}
			}
		});
		
		lv_task_manager.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TaskInfo taskInfo;
				if(position==0){  //�û����̵ı�ǩ
					return;
				}else if(position == (userTaskInfos.size() + 1)){
					return;
				}else if (position <= userTaskInfos.size()) {
					taskInfo = userTaskInfos.get(position-1); 
				}else{
					taskInfo = systemTaskInfos.get(position-1-userTaskInfos.size()-1);
				}
				
				//�Լ��������ε���¼� 
				if(getPackageName().equals(taskInfo.getPackname())){
					return;
				}
				
				//��һ��ҵ��bean,ȥ��¼ListView��CheckBox�Ĺ�ѡ״̬
				ViewHolder holder=(ViewHolder) view.getTag();
				if(taskInfo.isChecked()){
					taskInfo.setChecked(false);
					holder.cb_status.setChecked(false);
				}else{
					taskInfo.setChecked(true);
					holder.cb_status.setChecked(true);
				}
			}
		});
	}
	
	/**
	 * ��ʾ������(���ݰ���:������,���ڴ�,�����ڴ�)
	 */
	private void setTitle() {
		processCount = SystemInfoUtils.getRunningProcessCount(this);
		tv_process_count.setText("�����еĽ��̣�" + processCount + "��");
		availMem = SystemInfoUtils.getAvailMem(this);
		totalMem = SystemInfoUtils.getTotalMem(this);
		tv_mem_info.setText("ʣ��/���ڴ�:"+ Formatter.formatFileSize(this, availMem) + "/"
							+ Formatter.formatFileSize(this, totalMem));
	}

	private void fillData() {
		ll_loading.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				allTaskInfos=TaskInfoProvider.getTaskInfos(getApplicationContext()); //��ȡϵͳ���������еĽ���
				userTaskInfos = new ArrayList<TaskInfo>();
				systemTaskInfos = new ArrayList<TaskInfo>();
				for(TaskInfo info:allTaskInfos){
					if(info.isUserTask()){
						userTaskInfos.add(info);
					}else{
						systemTaskInfos.add(info);  //�����û�������ϵͳ����
					}
				}
				
				//�������ý���
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ll_loading.setVisibility(View.INVISIBLE);
						if(adapter==null){
							adapter=new TaskManagerAdapter();
							lv_task_manager.setAdapter(adapter);
						}else{
							adapter.notifyDataSetChanged();
						}
						setTitle();
					}
				});
			}
		}).start();
	}
	
	private class TaskManagerAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			SharedPreferences sp=getSharedPreferences("config",MODE_PRIVATE);
			if(sp.getBoolean("showsystem",false)){
				return userTaskInfos.size()+1+systemTaskInfos.size()+1;
			}else{
				return userTaskInfos.size()+1;
			}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TaskInfo taskInfo;
			if(position==0){  //�û����̵ı�ǩ
				TextView tv=new TextView(getApplicationContext());
				tv.setBackgroundColor(Color.GRAY);
				tv.setTextColor(Color.WHITE);
				tv.setText("�û����̣�" + userTaskInfos.size() + "��");
				return tv;
			}else if(position == (userTaskInfos.size() + 1)){
				TextView tv = new TextView(getApplicationContext());
				tv.setBackgroundColor(Color.GRAY);
				tv.setTextColor(Color.WHITE);
				tv.setText("ϵͳ���̣�" + systemTaskInfos.size() + "��");
				return tv;
			}else if (position <= userTaskInfos.size()) {
				taskInfo = userTaskInfos.get(position-1); 
			}else{
				taskInfo = systemTaskInfos.get(position-1-userTaskInfos.size()-1);
			}
			
			View view;
			ViewHolder holder;
			if(convertView!=null && convertView instanceof RelativeLayout){
				view=convertView;
				holder=(ViewHolder) view.getTag();
			}else{
				view=View.inflate(getApplicationContext(), R.layout.list_item_taskinfo, null);
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) view.findViewById(R.id.iv_task_icon);
				holder.tv_name = (TextView) view.findViewById(R.id.tv_task_name);
				holder.tv_memsize = (TextView) view.findViewById(R.id.tv_task_memsize);
				holder.cb_status = (CheckBox) view.findViewById(R.id.cb_status);
				view.setTag(holder);
			}
			
			holder.iv_icon.setImageDrawable(taskInfo.getIcon());
			holder.tv_name.setText(taskInfo.getName());
			holder.tv_memsize.setText("�ڴ�ռ�ã�"+Formatter.formatFileSize(getApplicationContext(),taskInfo.getMemsize()));
			holder.cb_status.setChecked(taskInfo.isChecked());  //Ĭ����false(������ѡ״̬)
			if(getPackageName().equals(taskInfo.getPackname())){
				holder.cb_status.setVisibility(View.INVISIBLE);
			}else{
				//����ListView��Ŀ�Ļ������,��Ҫ������Ϊ�ɼ�
				holder.cb_status.setVisibility(View.VISIBLE);
			}
			return view;
		}
		
		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}
	
	static class ViewHolder{
		ImageView iv_icon;
		TextView tv_name;
		TextView tv_memsize;
		CheckBox cb_status;
	}
	
	/**
	 * ѡ��ȫ��
	 * @param view
	 */
	public void selectAll(View view) {
		for(TaskInfo info:allTaskInfos){
			//����ѡ���Լ��Ľ���
			if(getPackageName().equals(info.getPackname())){
				continue;
			}
			info.setChecked(true);
		}
		adapter.notifyDataSetChanged();
	}
	
	/**
	 * ѡ���෴��
	 * @param view
	 */
	public void selectOppo(View view) {
		for(TaskInfo info:allTaskInfos){
			if(getPackageName().equals(info.getPackname())){
				continue;
			}
			info.setChecked(!info.isChecked());
		}
		adapter.notifyDataSetChanged();
	}
	
	/**
	 * һ������
	 * @param view
	 */
	public void killAll(View view) {
		ActivityManager am=(ActivityManager) getSystemService(ACTIVITY_SERVICE);
		int count=0;
		long savemem=0;
		List<TaskInfo> killedTaskInfos=new ArrayList<TaskInfo>(); //��¼��Щ��ɱ���Ľ�����Ŀ
		
		for(TaskInfo info:allTaskInfos){
			if(info.isChecked()){
				am.killBackgroundProcesses(info.getPackname());
				if(info.isUserTask()){
					userTaskInfos.remove(info);
				}else{
					systemTaskInfos.remove(info);
				}
				killedTaskInfos.add(info);
				count++;
				savemem+=info.getMemsize();
			}
		}
		
		allTaskInfos.removeAll(killedTaskInfos); //Ŀ��:���ֽ�������ʾ�Ľ����뼯���еĽ��̴ﵽһ��
		adapter.notifyDataSetChanged();
		Toast.makeText(this,"ɱ����"+count+"������,�ͷ���"+Formatter.formatFileSize(this,savemem),1).show();
		
		processCount-=count;
		availMem+=savemem;
		
		//����ѡ�н��̺��������ñ�����Ϣ
		tv_process_count.setText("�����еĽ��̣�" + processCount + "��");
		tv_mem_info.setText("ʣ��/���ڴ�:"+ Formatter.formatFileSize(this, availMem) + "/"
				+ Formatter.formatFileSize(this, totalMem));
	}
	
	/**
	 * ��������
	 * @param view
	 */
	public void enterSetting(View view) {
		Intent intent=new Intent(this,TaskSettingActivity.class);
		startActivityForResult(intent,0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//�ر����ý���ص�������ʾ����ʱ,ˢ�µ�ǰҳ����Ϣ
		adapter.notifyDataSetChanged();
		super.onActivityResult(requestCode, resultCode, data);
	}
}
