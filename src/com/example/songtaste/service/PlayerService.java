package com.example.songtaste.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.example.songtaste.MainActivity;
import com.example.songtaste.SongInfo;
import com.example.songtaste.db.DBOpenHelper;
import com.example.songtaste.util.Player;

public class PlayerService extends Service {

	public static Player player;
	ProgressBar progressBar;
	SeekBar seekBar;
	static String songUrl = "";
	DBOpenHelper dbHepler;
	static SharedPreferences sp;
	Editor editor;
	static SQLiteDatabase db;
	static List<Map<String, String>> data=new ArrayList<Map<String,String>>();
	static Map<String, String> map;
	static int _id=0;
	public static String songName;
	static String songID;
	static String userPic;
	static String userName;
	public static Handler handler=new Handler(){
		public void handleMessage(Message msg) {
			
			if (msg.what==0x001) {
				
				Intent intent=new Intent();
				_id+=1;
				
				Log.e("loge", "_id="+_id+"   data.size()="+data.size());
				
				if (_id<data.size()) {
					songName=data.get(_id).get("songName");
					songID=data.get(_id).get("songID");
					userName=data.get(_id).get("userName");
					userPic=data.get(_id).get("userPic");
					intent.putExtra("songID", data.get(_id).get("songID"));
					intent.putExtra("userPic", data.get(_id).get("userPic"));
					intent.putExtra("songName", data.get(_id).get("songName"));
					playMusic(intent);
//					Log.d("logd", "收到消息，位置为："+_id);
					
					//发送消息给SongInfo界面
					if (SongInfo.seekBar!=null) {
						Message msg1=new Message();
						msg1.what=0x001;
						Intent Aintent=new Intent();
						Aintent.putExtra("songInfo", currentSongInfo());
						msg1.obj=Aintent;
						if (SongInfo.handler!=null) {
							SongInfo.handler.sendMessage(msg1);
						}
						
					}
					
				}else {
					
					if (sp.getBoolean("isLovesonglist",false)) {//当前播放列表为Lovesonglist
						_id=-1;
						Message msg1=new Message();
						msg1.what=0x001;
						sendMessage(msg1);
								
					}else {
						//刷新下一列表并加入数据库
						MainActivity.getSongListThread.playNext=true;
						new Thread(new Runnable() {
							@Override
							public void run() {
								MainActivity.getSongListThread.RunTheThreadNext((Integer.parseInt(data.get(_id-6).get("currentNum"))+1)+"");
							}
						}).start();
					}
					
				}
			}
			
			if (msg.what==0x002) {
				Intent intent=new Intent();
				_id-=1;
				if (_id>=0) {
					player.stopMusic();
					songName=data.get(_id).get("songName");
					songID=data.get(_id).get("songID");
					userName=data.get(_id).get("userName");
					userPic=data.get(_id).get("userPic");
					intent.putExtra("songID", data.get(_id).get("songID"));
					intent.putExtra("userPic", data.get(_id).get("userPic"));
					intent.putExtra("songName", data.get(_id).get("songName"));
					playMusic(intent);
//					Log.d("logd", "收到消息，位置为："+_id);
					
					//发送消息给SongInfo界面
					if (SongInfo.seekBar!=null) {
						Message msg1=new Message();
						msg1.what=0x001;
						Intent Aintent=new Intent();
						Aintent.putExtra("songInfo", currentSongInfo());
						msg1.obj=Aintent;
						if (SongInfo.handler!=null) {
							SongInfo.handler.sendMessage(msg1);
						}
						
					}
				}else {
					_id=0;
				}
			}
			
			if (msg.what==0x003) {
				setDBData();
				_id=_id-1;
				Message message1=new Message();
				message1.what=0x001;
				sendMessage(message1);
				MainActivity.getSongListThread.playNext=false;
				

			}
		};
	};
	
	@Override
	public IBinder onBind(Intent intent) {

		Log.d("logd", "onBind");
		return null;
	}

	@Override
	public ComponentName startService(Intent service) {

		Log.d("logd", "startService");
		
		return null;

	}

	@Override
	public void onDestroy() {
		Log.d("logd", "onDestroy()");
		player.stop();
		player=null;
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		Log.d("logd", "onStartCommand");
		if (intent==null) {
			
		}else {
			Log.d("logd", "songName:" + intent.getStringExtra("songName"));
			Log.d("logd", "songID:" + intent.getStringExtra("songID"));
			Log.d("logd", "userName:" + intent.getStringExtra("userName"));
			Log.d("logd", "userPic:" + intent.getStringExtra("userPic"));
			Log.d("logd", "postTime:" + intent.getStringExtra("postTime"));
			songName=intent.getStringExtra("songName");
			songID=intent.getStringExtra("songID");
			userName=intent.getStringExtra("userName");
			userPic=intent.getStringExtra("userPic");
			setDBData();//设置将数据库歌曲列表载入内存
			player.stopMusic();
			
			if (false) {// 判断切换歌曲或暂停
				
				
			} else { // 否则直接播放
				
				playMusic(intent);
				
			}
		}
		

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 播放音乐
	 * @param intent
	 */
	public static void playMusic(final Intent intent) {
		
		
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Looper.prepare();
				
				Message message =new Message();
				message.what=0x002;//改变播放状态
				message.obj=intent.getStringExtra("userPic")+","+intent.getStringExtra("songName");
				MainActivity.handler.sendMessage(message);
				
				URL musicList;
				try {
					musicList = new URL("http://songtaste.com/song/"
							+ intent.getStringExtra("songID"));

					//查询当前歌曲在数据库中的位置
					for (int i = 0; i < data.size(); i++) {
						if (data.get(i).get("songID").contains(intent.getStringExtra("songID"))) {
							_id=i;
						}
					}
					
					HttpURLConnection conn = (HttpURLConnection) musicList
							.openConnection();

					conn.setConnectTimeout(5000);
					conn.setRequestMethod("GET");

					conn.setRequestProperty("User-Agent", "Mozilla/4.0");

					InputStream inputStream = conn.getInputStream();

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(inputStream));

					StringBuilder builder = new StringBuilder();

					String LINE = "";
					while ((LINE = reader.readLine()) != null) {

						builder.append(LINE + "\n");
					}

					reader.close();
					inputStream.close();

					String[] arr = builder.toString().split("\n");
					String str = "";
					String sid = "";
					String t = "";

					for (int i = 0; i < arr.length; i++) {

						if (arr[i]
								.contains("<div id=\"playicon\" style=\"margin-left:-12px\">")) {
							Log.w("logw", "arr[i + 1]" + arr[i + 1]);
							str = arr[i + 1].split(",")[2].substring(2,
									arr[i + 1].split(",")[2].length() - 1);
							sid = arr[i + 1].split(",")[5].substring(2,
									arr[i + 1].split(",")[5].length() - 1);
							t = arr[i + 1].split(",")[8].substring(0, 1);// 已知为0，不知是否可以为多位数字

						}

					}

					// 获取url
					URL url = new URL("http://songtaste.com/time.php?str="
							+ str + "&sid=" + sid + "&t=" + t);
					HttpURLConnection urlConn = (HttpURLConnection) url
							.openConnection();

					urlConn.setConnectTimeout(5000);
					urlConn.setRequestMethod("POST");
					InputStream in = urlConn.getInputStream();

					BufferedReader reader2 = new BufferedReader(
							new InputStreamReader(in));

					String LINE2 = "";
					while ((LINE2 = reader2.readLine()) != null) {

						songUrl = LINE2;
					}
					System.out.println(songUrl);// 下载链接

					reader2.close();
					in.close();
					
					player.playUrl(songUrl);//在线播放音乐
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				 
				Looper.loop();
			}
		}).start();
	}

	@Override
	public void onCreate() {
		Log.d("logd", "Service onCreate");
		player=new Player(this);

		dbHepler=new DBOpenHelper(this);
		db=dbHepler.getWritableDatabase();
		
		sp=getSharedPreferences("config",MODE_PRIVATE);
		editor=sp.edit();
		
		super.onCreate();
	}
	
	@Override
	public boolean stopService(Intent name) {
		Log.e("logd", "stopService");
		player.stop();;
		player=null;
		return super.stopService(name);
	}
	

	/**
	 * 歌曲被点击，载入当前歌单
	 */
	public static void setDBData() {
		
		if (sp.getBoolean("isLovesonglist",false)) {
			Cursor cursor=db.rawQuery("select * from lovesonglist", null);
			cursor.moveToFirst();
			data.clear();
			do{
				map=new HashMap<String, String>();
//				Log.d("logd", cursor.getString(cursor.getColumnIndex("songName")));
				map.put("id", cursor.getString(cursor.getColumnIndex("id")).toString());
				map.put("songName", cursor.getString(cursor.getColumnIndex("songName")));
				map.put("songID", cursor.getString(cursor.getColumnIndex("songID")));
				map.put("userName", cursor.getString(cursor.getColumnIndex("userName")));
				map.put("userPic", cursor.getString(cursor.getColumnIndex("userPic")));
				data.add(map);
				
			}while(cursor.moveToNext());
			
		}else {
			Cursor cursor=db.rawQuery("select * from netsonglist", null);
			cursor.moveToFirst();
			data.clear();
			do{
				map=new HashMap<String, String>();
//				Log.d("logd", cursor.getString(cursor.getColumnIndex("songName")));
				map.put("id", cursor.getString(cursor.getColumnIndex("id")).toString());
				map.put("songName", cursor.getString(cursor.getColumnIndex("songName")));
				map.put("songID", cursor.getString(cursor.getColumnIndex("songID")));
				map.put("userName", cursor.getString(cursor.getColumnIndex("userName")));
				map.put("userPic", cursor.getString(cursor.getColumnIndex("userPic")));
				map.put("currentNum", cursor.getString(cursor.getColumnIndex("currentNum")));
				data.add(map);
				
			}while(cursor.moveToNext());
		}
		
	}
	
	
	/**
	 * 暂停播放音乐
	 */
	public static void stopPlay() {
		
		player.pause();
		
	}

	/**
	 * 继续播放音乐
	 */
	public static void resumePlay() {
		if (player!=null) {
			player.play();
		}
	}

	
	/**
	 * 播放下一首歌曲
	 */
	public static void nextSong() {
		
		player.stopMusic();
		Message message1=new Message();
		message1.what=0x001;
		handler.sendMessage(message1);
		
	}

	/**
	 * 播放上一首歌曲
	 */
	public static void preSong() {
		
		Message message1=new Message();
		message1.what=0x002;
		handler.sendMessage(message1);
		
	}
	
	/**
	 * 当前歌曲信息
	 */
	public static String currentSongInfo() {
		//歌名，歌曲链接，歌曲时长
		if (songName!=null) {
			Log.d("logd",songName+","+songUrl+","+songID+","+userName+","+userPic+","+player.getSongLength());
			return songName+",zxm,"+songUrl+",zxm,"+songID+",zxm,"+userName+",zxm,"+userPic+",zxm,"+player.getSongLength();
		}
		return "";
	}
}
