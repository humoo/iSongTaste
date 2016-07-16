package com.example.songtaste;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.songtaste.adapter.LoveListAdapter;
import com.example.songtaste.db.DBOpenHelper;
import com.example.songtaste.service.PlayerService;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class LoveSong extends Activity{

	ListView listView;
	List<Map<String, String>> data =new ArrayList<Map<String,String>>();
	Map<String, String> map;
	ImageView btnBack;
	DBOpenHelper dbHepler;
	SQLiteDatabase db;
	Intent serviceIntent;
	SharedPreferences sp;
	Editor editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lovelist_activity);
		
		btnBack=(ImageView) findViewById(R.id.iv_back);
		listView=(ListView) findViewById(R.id.lv_lovelist);
		
		dbHepler=new DBOpenHelper(this);
		db=dbHepler.getReadableDatabase();
		sp=getSharedPreferences("config", MODE_PRIVATE);
		editor=sp.edit();
		
		//初始化数据
		data=initSongData();
		
		//适配器
		if (data!=null) {
			LoveListAdapter adapter=new LoveListAdapter(this,data);
			listView.setAdapter(adapter);
		}

		
		serviceIntent =new Intent(LoveSong.this,PlayerService.class);//音乐播放服务
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				editor.putBoolean("isLovesonglist", true);
				editor.commit();
				
				map=new HashMap<String,String>();
				map=data.get(position);
				
				serviceIntent.putExtra("songName",map.get("songName"));
				serviceIntent.putExtra("songID",map.get("songID"));
				serviceIntent.putExtra("userName",map.get("userName"));
				serviceIntent.putExtra("userPic",map.get("userPic"));
				
				startService(serviceIntent);//启动一个播放音乐服务
				

			}
		});
		
		
		
		
		
		
		
		
		
		
		
		
		//点击退出
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}


	/**
	 * 初始化数据，从本地数据库读取列表
	 * @return
	 */
	private List<Map<String, String>> initSongData() {
		
		Cursor cursor=db.rawQuery("select * from lovesonglist", null);
		if (cursor.getCount()>0) {
			cursor.moveToFirst();
			data.clear();
			do{
				map=new HashMap<String, String>();
				Log.d("logd", cursor.getString(cursor.getColumnIndex("songName")));
				map.put("songName", cursor.getString(cursor.getColumnIndex("songName")));
				map.put("songID", cursor.getString(cursor.getColumnIndex("songID")));
				map.put("userName", cursor.getString(cursor.getColumnIndex("userName")));
				map.put("userPic", cursor.getString(cursor.getColumnIndex("userPic")));
				data.add(map);
				
			}while(cursor.moveToNext());
			
			return data;
		}
		return null;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
