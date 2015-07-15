package com.itheima.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class BootCompleteReceiver extends BroadcastReceiver {
	private SharedPreferences sp;
	private TelephonyManager tm;

	@Override
	public void onReceive(Context context, Intent intent) {
		sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		boolean protecting = sp.getBoolean("protecting", false);
		if (protecting) {
			tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			String saveSim = sp.getString("sim", ""); // 读取之前保存的SIM卡信息
			String realSim = tm.getSimSerialNumber(); // 读取当前SIM卡的信息

			if (saveSim.equals(realSim)) {

			} else {
				System.out.println("sim 已经变更");
				Toast.makeText(context, "sim 已经变更", 1).show();
				
				SmsManager.getDefault().sendTextMessage(
						sp.getString("safenumber", ""), null,
						"sim changing...", null, null);
			}
		}
	}
}
