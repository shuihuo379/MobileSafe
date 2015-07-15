package com.itheima.mobilesafe;

import com.itheima.mobilesafe.utils.MD5Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {
	private GridView list_home;
	private MyAdapter adapter;
	private SharedPreferences sp;
	private static String[] names={
		"�ֻ�����","ͨѶ��ʿ","�������",
		"���̹���","����ͳ��","�ֻ�ɱ��",
		"��������","�߼�����","��������"
	};
	private static int[] ids={
		R.drawable.safe,R.drawable.callmsgsafe,R.drawable.app,
		R.drawable.taskmanager,R.drawable.netmanager,R.drawable.trojan,
		R.drawable.sysoptimize,R.drawable.atools,R.drawable.settings
	};
	
	private EditText et_setup_pwd;
	private EditText et_setup_confirm;
	private Button ok;
	private Button cancel;
	private AlertDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		sp=getSharedPreferences("config",MODE_PRIVATE);
		list_home=(GridView) findViewById(R.id.list_home);
		adapter=new MyAdapter();
		list_home.setAdapter(adapter);
		list_home.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Intent intent;
				switch (position) {
					case 0: //�����ֻ�����ҳ��
						showLostFindDialog();
						break;
					case 1://���غ��������ؽ���
						intent = new Intent(HomeActivity.this,CallSmsSafeActivity.class);
						startActivity(intent);
						break;
					case 2: //�������������
						intent = new Intent(HomeActivity.this,AppManagerActivity.class);
						startActivity(intent);
						break;
					case 3: //���̹���������
						intent = new Intent(HomeActivity.this,TaskManagerActivity.class);
						startActivity(intent);
						break;
					case 4: //����ͳ��
						intent = new Intent(HomeActivity.this,TrafficManagerActivity.class);
						startActivity(intent);
						break;
					case 5: //�ֻ�ɱ��
						intent = new Intent(HomeActivity.this,AntiVirusActivity.class);
						startActivity(intent);
						break;
					case 6: //��������
						intent = new Intent(HomeActivity.this,CleanCacheActivity.class);
						startActivity(intent);
						break;
					case 7: //����߼�����
						intent=new Intent(HomeActivity.this,AtoolActivity.class);
						startActivity(intent);
						break;
					case 8: //������������
						intent = new Intent(HomeActivity.this,SettingActivity.class);
						startActivity(intent);
						break;
					default:
						break;
				}
			}
		});
	}
	
	private void showLostFindDialog() {
		//�ж��Ƿ����ù�����
		if(isSetupPwd()){
			//�Ѿ����������ˣ�������������Ի���
			showEnterDialog();
		}else{
			//û���������룬����������������Ի���
			showSetupPwdDialog();
		}
	}
	/**
	 * ��������Ի���
	 */
	private void showSetupPwdDialog() {
		AlertDialog.Builder builder=new Builder(HomeActivity.this);
		View view=View.inflate(this,R.layout.dialog_setup_password,null);
		et_setup_pwd = (EditText) view.findViewById(R.id.et_setup_pwd);
		et_setup_confirm = (EditText) view.findViewById(R.id.et_setup_confirm);
		ok = (Button) view.findViewById(R.id.ok);
		cancel = (Button) view.findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String password = et_setup_pwd.getText().toString().trim();
				String password_confirm = et_setup_confirm.getText().toString().trim();
				if(TextUtils.isEmpty(password) || TextUtils.isEmpty(password_confirm)){
					Toast.makeText(HomeActivity.this, "����Ϊ��", 0).show();
					return;
				}
				//�ж��Ƿ�һ�²�ȥ����
				if(password.equals(password_confirm)){
					Editor editor=sp.edit();
					editor.putString("password",MD5Utils.md5Password(password));  //������ܺ��
					editor.commit();
					dialog.dismiss();
				}else{
					Toast.makeText(HomeActivity.this, "���벻һ��", 0).show();
					return;
				}
			}
		});
		
		builder.setView(view);
		dialog=builder.show();  //�õ��Ի���ʵ��
	}

	private void showEnterDialog() {
		AlertDialog.Builder builder=new Builder(HomeActivity.this);
		View view=View.inflate(this,R.layout.dialog_enter_password,null); 
		et_setup_pwd = (EditText) view.findViewById(R.id.et_setup_pwd);
		et_setup_confirm = (EditText) view.findViewById(R.id.et_setup_confirm);
		ok = (Button) view.findViewById(R.id.ok);
		cancel = (Button) view.findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String password = et_setup_pwd.getText().toString().trim();
				String savePassword = sp.getString("password", "");//ȡ�����ܺ��
				if(TextUtils.isEmpty(password)){
					Toast.makeText(HomeActivity.this, "����Ϊ��", 1).show();
					return;
				}
				if(MD5Utils.md5Password(password).equals(savePassword)){
					dialog.dismiss();
					System.out.println("�ѶԻ��������������ֻ�����ҳ��");
					Intent intent=new Intent(HomeActivity.this,LostFindActivity.class);
					startActivity(intent);  //��ҳ�治��finish
				}else{
					Toast.makeText(HomeActivity.this, "�������", 1).show();
					et_setup_pwd.setText("");
					return;
				}
			}
		});
		
//		builder.setView(view);  //�����Զ���Ի���
//		dialog=builder.show();  //�õ��Ի���ʵ��
		dialog=builder.create();
		dialog.setView(view,0,0,0,0);  //���ݵͰ汾,�ڱ�����ɰ�ɫ��,ʹ���ܱ߼��Ϊ0
		dialog.show();
	}

	private boolean isSetupPwd(){
		String password=sp.getString("password",null);
		return !TextUtils.isEmpty(password);
	}
	
	private class MyAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			return names.length;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view=View.inflate(HomeActivity.this,R.layout.list_item_home, null);
			ImageView iv_item=(ImageView) view.findViewById(R.id.iv_item);
			TextView tv_item=(TextView) view.findViewById(R.id.tv_item);
//			iv_item.setImageResource(ids[position]);
			iv_item.setImageDrawable(getResources().getDrawable(ids[position]));
			tv_item.setText(names[position]);
			return view;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}
	}
}
