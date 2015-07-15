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
	private PopupWindow popupWindow; // 弹出窗体

	private List<AppInfo> appInfos; // 所有的应用程序包信息
	private List<AppInfo> userAppInfos; // 用户应用程序的集合
	private List<AppInfo> systemAppInfos; // 系统应用程序的集合
	private AppInfo appInfo; // 被点击的条目
	
	private LinearLayout ll_start; //开启
	private LinearLayout ll_share; //分享
	private LinearLayout ll_uninstall; //卸载
	
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
				.getAbsolutePath()); // 获取外置SD卡的可用空间(若没有外置SD卡,则获取内置SD卡的空间)
		long inner_sdsize=getAvailSpace(Environment.getExternalStorageDirectory()
				.getAbsolutePath().replace('0','1')); //获取内置SD卡的可用空间(路径为 /storage/sdacrd1)
		long romsize = getAvailSpace(Environment.getDataDirectory()
				.getAbsolutePath()); // 获取内部存储空间

		// 格式化存储空间大小,转化为Byte,KB,MB,GB等
		tv_avail_sd.setText("内部SD卡可用:" + Formatter.formatFileSize(this,inner_sdsize)); //内置+外置
		tv_avail_rom.setText("内存可用:"+ Formatter.formatFileSize(this, romsize));

		lv_app_manager = (ListView) findViewById(R.id.lv_app_manager);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);

		fillData();

		// 给listview注册一个滚动的监听器
		lv_app_manager.setOnScrollListener(new OnScrollListener() {
			// 当滚动状态发生变化时调用
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			// 滚动的时候调用的方法
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				dismissPopupWindow();
				if (userAppInfos != null && systemAppInfos != null) {
					// 产生错觉,始终保持TextView中的信息位置不变
					if (firstVisibleItem > userAppInfos.size()) {
						tv_status.setText("系统程序:" + systemAppInfos.size() + "个");
					} else {
						tv_status.setText("用户程序:" + userAppInfos.size() + "个");
					}
				}
			}
		});

		/**
		 * 点击事件弹出悬浮窗体
		 */
		lv_app_manager.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) { // 第一个小标签
					return;
				} else if (position == (userAppInfos.size() + 1)) { // 第二个小标签
					return;
				} else if (position <= userAppInfos.size()) { // 用户程序
					int newposition = position - 1;
					appInfo = userAppInfos.get(newposition);
				} else { // 系统程序
					int newposition = position - 1 - userAppInfos.size() - 1;
					appInfo = systemAppInfos.get(newposition);
				}

				dismissPopupWindow(); // 每点击一个条目,先关闭之前条目的弹出式窗体,再开启新的弹出式窗体
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

				 //注意:动画效果的播放必须要求窗体有背景颜色
			    //透明颜色也是颜色
				popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
				popupWindow.showAtLocation(parent, Gravity.LEFT | Gravity.TOP,
						px, location[1]);

				// 为弹出菜单添加动画效果
				ScaleAnimation sa = new ScaleAnimation(0.3f, 1.0f, 0.3f,
						1.0f, Animation.RELATIVE_TO_SELF, 0,
						Animation.RELATIVE_TO_SELF, 0.5f);
				sa.setDuration(500);
				AlphaAnimation aa=new AlphaAnimation(0.5f,1.0f);
				aa.setDuration(500);
				AnimationSet set=new AnimationSet(false);  //动画集
				set.addAnimation(aa);
				set.addAnimation(sa);
				contentView.startAnimation(set);
			}
		});
		
		/**
		 * 长点击事件为条目加锁
		 */
		lv_app_manager.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					return true;  
				} else if (position == (userAppInfos.size() + 1)) {
					return true;
				} else if (position <= userAppInfos.size()) {// 用户程序
					int newposition = position - 1;
					appInfo = userAppInfos.get(newposition);
				} else {// 系统程序
					int newposition = position - 1 - userAppInfos.size() - 1;
					appInfo = systemAppInfos.get(newposition);
				}
				
				ViewHolder holder=(ViewHolder) view.getTag();
				//判断条目是否存在在程序锁数据库里面
				if(dao.find(appInfo.getPackname())){
					//被锁定的程序，解除锁定，更新界面为打开的小锁图片,同时删除数据库中该条目的记录
					dao.delete(appInfo.getPackname());
					holder.iv_status.setImageResource(R.drawable.unlock);
				}else{
					dao.add(appInfo.getPackname());
					holder.iv_status.setImageResource(R.drawable.lock);
				}
				return true;  //返回值为true表示点击事件到此即终止,不会出现事件上浮,也就不会出现点击条目弹出悬浮窗体
			}
		});
	}

	private void fillData() {
		ll_loading.setVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				// 得到手机里面安装的所有的应用程序信息
				appInfos = AppInfoProvider.getAppInfos(AppManagerActivity.this);
				// 区分用户程序和系统程序
				userAppInfos = new ArrayList<AppInfo>();
				systemAppInfos = new ArrayList<AppInfo>();
				for (AppInfo info : appInfos) {
					if (info.isUserApp()) {
						userAppInfos.add(info);
					} else {
						systemAppInfos.add(info);
					}
				}
				// 加载listview的数据适配器
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
		dismissPopupWindow(); // 清理资源
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
				tv.setText("用户程序:" + userAppInfos.size() + "个");
				return tv;
			} else if (position == (userAppInfos.size() + 1)) {
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.WHITE);
				tv.setBackgroundColor(Color.GRAY);
				tv.setText("系统程序:" + systemAppInfos.size() + "个");
				return tv;
			} else if (position <= userAppInfos.size()) { // 用户程序
				int newposition = position - 1;
				appInfo = userAppInfos.get(newposition);
			} else { // 系统程序
				int newposition = position - 1 - userAppInfos.size() - 1;
				appInfo = systemAppInfos.get(newposition);
			}

			if (convertView != null && convertView instanceof RelativeLayout) {
				// 不仅需要检查是否为空,还要判断是否是合适的类型去复用
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
			holder.tv_name.setText(appInfo.getName()); // 设置应用程序的名称
			if (appInfo.isInRom()) {
				holder.tv_location.setText("手机内存"+"uid:"+appInfo.getUid());
			} else {
				holder.tv_location.setText("外部存储"+"uid:"+appInfo.getUid());
			}
			
			//每次启动此页面时,会去查询数据库中的记录,从而更新界面(软件锁),达到与数据库中对应记录的同步效果
			if(dao.find(appInfo.getPackname())){
				//查询到这个程序在数据库中有记录
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
	 * 获取某个目录的可用空间
	 * @param path
	 * @return
	 */
	private long getAvailSpace(String path) {
		StatFs statf = new StatFs(path);
		int count = statf.getBlockCount(); // 获取分区个数
		int size = statf.getBlockSize(); // 获取分区大小
		int avail_count = statf.getAvailableBlocks(); // 获取可用区块的个数
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
	 * 布局对应的点击事件
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
	 * 分享一个应用程序
	 */
	private void shareApplication() {
		Intent intent=new Intent();
		intent.setAction("android.intent.action.SEND");
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT,"推荐您使用一款软件,名称叫:"+appInfo.getName());
		startActivity(intent);
	}

	/**
	 * 开启一个应用程序
	 */
	private void startApplication() {
		//查询这个应用程序的入口Activity,把他开启起来
		PackageManager pm=getPackageManager();
		
		// Intent intent = new Intent();
		// intent.setAction("android.intent.action.MAIN");
		// intent.addCategory("android.intent.category.LAUNCHER");
		//查询出来了所有的手机上具有启动能力的activity
		//List<ResolveInfo> infos = pm.queryIntentActivities(intent,PackageManager.GET_INTENT_FILTERS);
		
		Intent intent=pm.getLaunchIntentForPackage(appInfo.getPackname());
		if(intent!=null){
			startActivity(intent);
		}else{
			Toast.makeText(this, "不能启动当前应用", 0).show();
		}
	}
	
	/**
	 * 卸载应用
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
		//刷新界面
		fillData();
		super.onActivityResult(requestCode, resultCode, data);
	}
}
