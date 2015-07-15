package com.itheima.mobilesafe;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima.mobilesafe.db.ApplockDao;
import com.itheima.mobilesafe.domain.AppInfo;
import com.itheima.mobilesafe.engine.AppInfoProvider;
import com.itheima.mobilesafe.utils.DensityUtils;

public class AppManagerActivity extends Activity implements OnClickListener{
	private TextView tv_avail_rom;
	private TextView tv_avail_sd;
	private TextView tv_status;
	private ListView lv_app_manager;
	private LinearLayout ll_loading;
	private PopupWindow popupWindow; // ��������

	private List<AppInfo> appInfos; // ���е�Ӧ�ó������Ϣ
	private List<AppInfo> userAppInfos; // �û�Ӧ�ó���ļ���
	private List<AppInfo> systemAppInfos; // ϵͳӦ�ó���ļ���
	private AppInfo appInfo; // ���������Ŀ
	
	private LinearLayout ll_start; //����
	private LinearLayout ll_share; //����
	private LinearLayout ll_uninstall; //ж��
	
	private AppManagerAdapter adapter;
	private ApplockDao dao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);
		dao=new ApplockDao(this);
		tv_avail_rom = (TextView) findViewById(R.id.tv_avail_rom);
		tv_avail_sd = (TextView) findViewById(R.id.tv_avail_sd);
		tv_status = (TextView) findViewById(R.id.tv_status);

		long out_sdsize = getAvailSpace(Environment.getExternalStorageDirectory()
				.getAbsolutePath()); // ��ȡ����SD���Ŀ��ÿռ�(��û������SD��,���ȡ����SD���Ŀռ�)
		long inner_sdsize=getAvailSpace(Environment.getExternalStorageDirectory()
				.getAbsolutePath().replace('0','1')); //��ȡ����SD���Ŀ��ÿռ�(·��Ϊ /storage/sdacrd1)
		long romsize = getAvailSpace(Environment.getDataDirectory()
				.getAbsolutePath()); // ��ȡ�ڲ��洢�ռ�

		// ��ʽ���洢�ռ��С,ת��ΪByte,KB,MB,GB��
		tv_avail_sd.setText("�ڲ�SD������:" + Formatter.formatFileSize(this,inner_sdsize)); //����+����
		tv_avail_rom.setText("�ڴ����:"+ Formatter.formatFileSize(this, romsize));

		lv_app_manager = (ListView) findViewById(R.id.lv_app_manager);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);

		fillData();

		// ��listviewע��һ�������ļ�����
		lv_app_manager.setOnScrollListener(new OnScrollListener() {
			// ������״̬�����仯ʱ����
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			// ������ʱ����õķ���
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				dismissPopupWindow();
				if (userAppInfos != null && systemAppInfos != null) {
					// �������,ʼ�ձ���TextView�е���Ϣλ�ò���
					if (firstVisibleItem > userAppInfos.size()) {
						tv_status.setText("ϵͳ����:" + systemAppInfos.size() + "��");
					} else {
						tv_status.setText("�û�����:" + userAppInfos.size() + "��");
					}
				}
			}
		});

		/**
		 * ����¼�������������
		 */
		lv_app_manager.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) { // ��һ��С��ǩ
					return;
				} else if (position == (userAppInfos.size() + 1)) { // �ڶ���С��ǩ
					return;
				} else if (position <= userAppInfos.size()) { // �û�����
					int newposition = position - 1;
					appInfo = userAppInfos.get(newposition);
				} else { // ϵͳ����
					int newposition = position - 1 - userAppInfos.size() - 1;
					appInfo = systemAppInfos.get(newposition);
				}

				dismissPopupWindow(); // ÿ���һ����Ŀ,�ȹر�֮ǰ��Ŀ�ĵ���ʽ����,�ٿ����µĵ���ʽ����
				View contentView = View.inflate(getApplicationContext(),
						R.layout.popup_app_item, null);
				
				ll_start = (LinearLayout) contentView.findViewById(R.id.ll_start);
				ll_share = (LinearLayout) contentView.findViewById(R.id.ll_share);
				ll_uninstall = (LinearLayout) contentView.findViewById(R.id.ll_uninstall);
				
				ll_start.setOnClickListener(AppManagerActivity.this);
				ll_share.setOnClickListener(AppManagerActivity.this);
				ll_uninstall.setOnClickListener(AppManagerActivity.this);
						

				popupWindow = new PopupWindow(contentView,
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				int[] location = new int[2];
				view.getLocationInWindow(location);
				int dip = 60;
				int px = DensityUtils.dip2px(getApplicationContext(), dip);

				 //ע��:����Ч���Ĳ��ű���Ҫ�����б�����ɫ
			    //͸����ɫҲ����ɫ
				popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
				popupWindow.showAtLocation(parent, Gravity.LEFT | Gravity.TOP,
						px, location[1]);

				// Ϊ�����˵���Ӷ���Ч��
				ScaleAnimation sa = new ScaleAnimation(0.3f, 1.0f, 0.3f,
						1.0f, Animation.RELATIVE_TO_SELF, 0,
						Animation.RELATIVE_TO_SELF, 0.5f);
				sa.setDuration(500);
				AlphaAnimation aa=new AlphaAnimation(0.5f,1.0f);
				aa.setDuration(500);
				AnimationSet set=new AnimationSet(false);  //������
				set.addAnimation(aa);
				set.addAnimation(sa);
				contentView.startAnimation(set);
			}
		});
		
		/**
		 * ������¼�Ϊ��Ŀ����
		 */
		lv_app_manager.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					return true;  
				} else if (position == (userAppInfos.size() + 1)) {
					return true;
				} else if (position <= userAppInfos.size()) {// �û�����
					int newposition = position - 1;
					appInfo = userAppInfos.get(newposition);
				} else {// ϵͳ����
					int newposition = position - 1 - userAppInfos.size() - 1;
					appInfo = systemAppInfos.get(newposition);
				}
				
				ViewHolder holder=(ViewHolder) view.getTag();
				//�ж���Ŀ�Ƿ�����ڳ��������ݿ�����
				if(dao.find(appInfo.getPackname())){
					//�������ĳ��򣬽�����������½���Ϊ�򿪵�С��ͼƬ,ͬʱɾ�����ݿ��и���Ŀ�ļ�¼
					dao.delete(appInfo.getPackname());
					holder.iv_status.setImageResource(R.drawable.unlock);
				}else{
					dao.add(appInfo.getPackname());
					holder.iv_status.setImageResource(R.drawable.lock);
				}
				return true;  //����ֵΪtrue��ʾ����¼����˼���ֹ,��������¼��ϸ�,Ҳ�Ͳ�����ֵ����Ŀ������������
			}
		});
	}

	private void fillData() {
		ll_loading.setVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				// �õ��ֻ����氲װ�����е�Ӧ�ó�����Ϣ
				appInfos = AppInfoProvider.getAppInfos(AppManagerActivity.this);
				// �����û������ϵͳ����
				userAppInfos = new ArrayList<AppInfo>();
				systemAppInfos = new ArrayList<AppInfo>();
				for (AppInfo info : appInfos) {
					if (info.isUserApp()) {
						userAppInfos.add(info);
					} else {
						systemAppInfos.add(info);
					}
				}
				// ����listview������������
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(adapter==null){
							adapter=new AppManagerAdapter();
							lv_app_manager.setAdapter(adapter);
						}else{
							adapter.notifyDataSetChanged();
						}
						ll_loading.setVisibility(View.INVISIBLE);
					}
				});
			}
		}.start();
	}

	@Override
	protected void onDestroy() {
		dismissPopupWindow(); // ������Դ
		super.onDestroy();
	}

	private class AppManagerAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			// return appInfos.size();
			return userAppInfos.size() + systemAppInfos.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder;
		
			if (position == 0) {
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.WHITE);
				tv.setBackgroundColor(Color.GRAY);
				tv.setText("�û�����:" + userAppInfos.size() + "��");
				return tv;
			} else if (position == (userAppInfos.size() + 1)) {
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.WHITE);
				tv.setBackgroundColor(Color.GRAY);
				tv.setText("ϵͳ����:" + systemAppInfos.size() + "��");
				return tv;
			} else if (position <= userAppInfos.size()) { // �û�����
				int newposition = position - 1;
				appInfo = userAppInfos.get(newposition);
			} else { // ϵͳ����
				int newposition = position - 1 - userAppInfos.size() - 1;
				appInfo = systemAppInfos.get(newposition);
			}

			if (convertView != null && convertView instanceof RelativeLayout) {
				// ������Ҫ����Ƿ�Ϊ��,��Ҫ�ж��Ƿ��Ǻ��ʵ�����ȥ����
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = View.inflate(AppManagerActivity.this,
						R.layout.list_item_appinfo, null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) view.findViewById(R.id.tv_app_name);
				holder.tv_location = (TextView) view
						.findViewById(R.id.tv_app_location);
				holder.iv_icon = (ImageView) view
						.findViewById(R.id.iv_app_icon);
				holder.iv_status=(ImageView) view.findViewById(R.id.iv_status);
				view.setTag(holder);
			}

			holder.iv_icon.setImageDrawable(appInfo.getIcon());
			holder.tv_name.setText(appInfo.getName()); // ����Ӧ�ó��������
			if (appInfo.isInRom()) {
				holder.tv_location.setText("�ֻ��ڴ�"+"uid:"+appInfo.getUid());
			} else {
				holder.tv_location.setText("�ⲿ�洢"+"uid:"+appInfo.getUid());
			}
			
			//ÿ��������ҳ��ʱ,��ȥ��ѯ���ݿ��еļ�¼,�Ӷ����½���(�����),�ﵽ�����ݿ��ж�Ӧ��¼��ͬ��Ч��
			if(dao.find(appInfo.getPackname())){
				//��ѯ��������������ݿ����м�¼
				holder.iv_status.setImageResource(R.drawable.lock);
			}else{
				holder.iv_status.setImageResource(R.drawable.unlock);
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

	static class ViewHolder {
		TextView tv_name;
		TextView tv_location;
		ImageView iv_icon;
		ImageView iv_status;
	}

	/**
	 * ��ȡĳ��Ŀ¼�Ŀ��ÿռ�
	 * @param path
	 * @return
	 */
	private long getAvailSpace(String path) {
		StatFs statf = new StatFs(path);
		int count = statf.getBlockCount(); // ��ȡ��������
		int size = statf.getBlockSize(); // ��ȡ������С
		int avail_count = statf.getAvailableBlocks(); // ��ȡ��������ĸ���
		System.out.println(count + "-->" + size + "-->" + avail_count);
		return size * avail_count;
	}

	private void dismissPopupWindow() {
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
			popupWindow = null;
		}
	}

	/**
	 * ���ֶ�Ӧ�ĵ���¼�
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ll_share:
				shareApplication();
				break;
			case R.id.ll_start:
				startApplication();
				break;
			case R.id.ll_uninstall:
				uninstallAppliation();
				break;
			default:
				break;
		}
	}
	
	/**
	 * ����һ��Ӧ�ó���
	 */
	private void shareApplication() {
		Intent intent=new Intent();
		intent.setAction("android.intent.action.SEND");
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT,"�Ƽ���ʹ��һ�����,���ƽ�:"+appInfo.getName());
		startActivity(intent);
	}

	/**
	 * ����һ��Ӧ�ó���
	 */
	private void startApplication() {
		//��ѯ���Ӧ�ó�������Activity,������������
		PackageManager pm=getPackageManager();
		
		// Intent intent = new Intent();
		// intent.setAction("android.intent.action.MAIN");
		// intent.addCategory("android.intent.category.LAUNCHER");
		//��ѯ���������е��ֻ��Ͼ�������������activity
		//List<ResolveInfo> infos = pm.queryIntentActivities(intent,PackageManager.GET_INTENT_FILTERS);
		
		Intent intent=pm.getLaunchIntentForPackage(appInfo.getPackname());
		if(intent!=null){
			startActivity(intent);
		}else{
			Toast.makeText(this, "����������ǰӦ��", 0).show();
		}
	}
	
	/**
	 * ж��Ӧ��
	 */
	private void uninstallAppliation() {
		// <action android:name="android.intent.action.VIEW" />
		// <action android:name="android.intent.action.DELETE" />
		// <category android:name="android.intent.category.DEFAULT" />
		// <data android:scheme="package" />
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.setAction("android.intent.action.DELETE");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("package:"+appInfo.getPackname()));
		startActivityForResult(intent,0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//ˢ�½���
		fillData();
		super.onActivityResult(requestCode, resultCode, data);
	}
}
