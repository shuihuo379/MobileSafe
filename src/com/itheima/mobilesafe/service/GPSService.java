package com.itheima.mobilesafe.service;

import java.io.InputStream;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class GPSService extends Service {
	// 用到位置服务
	private LocationManager lm;
	private MyLocationListener listener;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		listener = new MyLocationListener();
		// 给位置提供者设置条件
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String provider = lm.getBestProvider(criteria, true);
		lm.requestLocationUpdates(provider, 0, 0, listener);
	}

	@Override
	public void onDestroy() {
		lm.removeUpdates(listener); // 取消位置监听服务
		listener = null;
	}

	class MyLocationListener implements LocationListener {
		/**
		 * 当位置改变的时候回调
		 */
		@Override
		public void onLocationChanged(Location location) {
			String longitude = "j:" + location.getLongitude() + "\n";
			String latitude = "w:" + location.getLatitude() + "\n";
			String accuracy = "a:" + location.getAccuracy() + "\n";

			// 发送短信给安全号码

			// 把标准的GPS坐标转化为火星坐标
			try {
				InputStream in = getResources().getAssets().open("axisoffset.dat");
				ModifyOffset offset = ModifyOffset.getInstance(in);
				PointDouble pd = offset.c2s(new PointDouble(location
						.getLongitude(), location.getLatitude()));
				longitude="j:"+offset.X+"\n";
				latitude= "w:"+offset.Y+"\n";
			} catch (Exception e) {
				e.printStackTrace();
			}

			SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putString("lastlocation", longitude + latitude + accuracy);
			editor.commit();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

	}
}
