package com.example.songtaste;

import java.util.zip.Inflater;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingActivity extends Activity implements OnClickListener{

	ImageView btnBack,btnWifi;
	TextView btnAbout;
	
	SharedPreferences sp;
	Editor editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_activity);
		
		btnAbout=(TextView) findViewById(R.id.tv_about);
		btnBack=(ImageView) findViewById(R.id.iv_back);
		btnWifi=(ImageView) findViewById(R.id.iv_wifi);
		
		btnAbout.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		btnWifi.setOnClickListener(this);
		
		sp=getSharedPreferences("config",MODE_PRIVATE);
		editor=sp.edit();
		
		if (sp.getBoolean("wifi", true)) {//默认wifi状态开启
			btnWifi.setImageResource(R.drawable.switch_on_normal);
		}else {
			btnWifi.setImageResource(R.drawable.switching_off);
		}
		
		
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.iv_back:
			finish();
			break;

		case R.id.tv_about:
			AlertDialog.Builder builder=new AlertDialog.Builder(this);
			View view=LayoutInflater.from(getApplicationContext()).inflate(R.layout.about, null);
			builder.setView(view);
			builder.create().show();
			break;
		case R.id.iv_wifi:
			if (sp.getBoolean("wifi", false)) {//当仅wifi已开启，点击关闭
				
				AlertDialog.Builder builder2=new AlertDialog.Builder(this);
				builder2.setTitle("提示");
				builder2.setMessage("当前模式为仅Wifi网络下听歌，取消可能会产生大量的手机网络资费\n确定关闭？");
				builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						editor.putBoolean("wifi", false);
						editor.commit();
						btnWifi.setImageResource(R.drawable.switching_off);
					}
				});
				builder2.setNegativeButton("取消", null);
				builder2.create().show();
				
			}else {//当仅wifi已关闭，点击开启
				editor.putBoolean("wifi", true);
				editor.commit();
				btnWifi.setImageResource(R.drawable.switch_on_normal);
			}
			break;
		}
		
	}

	
	
}
