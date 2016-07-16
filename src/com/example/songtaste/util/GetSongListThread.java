package com.example.songtaste.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.songtaste.MainActivity;
import com.example.songtaste.db.DBOpenHelper;
import com.example.songtaste.service.PlayerService;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class GetSongListThread extends Thread{

	List<Map<String, String>> data =new ArrayList<Map<String,String>>();
	Map<String, String> map;
	File imgFile;
	File stHeadDir=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+"/stHead/");//存放图片目录
	String userPic = "";
	String num="";
	String flag="0";//判断是否刷新下一页
	Context context;
	DBOpenHelper dbHelper;
	SQLiteDatabase db;
	boolean isFromUI=true;
	public static boolean playNext=false;
	public GetSongListThread(Context context, String num) {
		this.num=num;
		this.context=context;
		dbHelper=new DBOpenHelper(context);//创建数据库
		db=dbHelper.getWritableDatabase();
	}


	@Override
	public void run() {
		
		Looper.prepare();
		try {
			
			
			URL musicList = new URL("http://songtaste.com/music/"+num);

			Log.d("logd", "http://songtaste.com/music/"+num);
			
			HttpURLConnection conn = (HttpURLConnection) musicList.openConnection();

			conn.setConnectTimeout(5000);
			conn.setRequestMethod("POST");

			InputStream inputStream = conn.getInputStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream,"gb2312"));

			StringBuilder builder = new StringBuilder();

			int len = -1;
			String LINE = "";
			while ((LINE = reader.readLine()) != null) {

				builder.append(LINE + "\n");
			}

			reader.close();
			inputStream.close();

			String[] arr = builder.toString().split("\n");

			String songName = "";
			String songID = "";
			String userName = "";
			
			String postTime = "";
			String[] songArr = null;

			if (flag.equals("A")) {
				data.clear();
			}
			// 获取歌曲列表信息
			for (int i = 0; i < arr.length; i++) {
				if (arr[i].contains("MSL")) {

					songArr = arr[i].split("\", \"");

					songName = songArr[0].substring(5, songArr[0].length() - 1);
					songID = songArr[1];
					userName = songArr[2];
					userPic = songArr[4];
					postTime = songArr[7];

					// 时间提取
					if (postTime.contains("一个月")) {
						
					}else if (postTime.contains("秒")) {
						postTime=postTime.substring(35,postTime.length()-17)+"秒前";
//						Log.d("logd","秒时间："+postTime );
					}else if (postTime.contains("分钟")) {
						postTime=postTime.substring(35,postTime.length()-17)+"分钟前";
//						Log.d("logd","分钟时间："+postTime );
					}else if (postTime.contains("小时")) {
						postTime=postTime.substring(17,postTime.length()-10)+"小时前";
//						Log.d("logd","小时时间："+postTime );
					}else if (postTime.contains("天")) {
						postTime=postTime.substring(17,postTime.length()-9)+"天前";
//						Log.d("logd","天时间："+postTime );
					}else {
						postTime="";
//						Log.d("logd","无时间："+postTime );
					}
//					Log.d("logd","userPic："+userPic );
					Log.d("logd","postTime："+postTime +"   System.currentTimeMillis():"+System.currentTimeMillis());
					
					//下载头像
					if (!stHeadDir.exists()) {
						stHeadDir.mkdirs();
					}
					
//					Log.d("logd", "imgFilePre:"+userPic);
					if (!userPic.contains("default")) {
						imgFile=new File(stHeadDir+"/"+userPic.split("/")[1]);
					}else {
						imgFile=new File(stHeadDir+"/"+"default.gif");

					}
//					Log.d("logd", "imgFile:"+imgFile.getAbsolutePath());
					
					if (!imgFile.exists()||imgFile.length()<=0) {//不存在头像，则下载
								
						try {
							URL iconUrl=new URL("http://image.songtaste.com/images/usericon/s/"+userPic);
							HttpURLConnection iconConn=(HttpURLConnection) iconUrl.openConnection();
							iconConn.setConnectTimeout(5000);
							iconConn.setRequestMethod("GET");
							if (iconConn.getResponseCode()==200) {
								InputStream inStream=iconConn.getInputStream();
								FileOutputStream fos=new FileOutputStream(imgFile);
								
								int len2=-1;
								byte[] buf=new byte[1024];
								while ((len2=inStream.read(buf))!=-1) {
									fos.write(buf,0,len2);
								}
								
								fos.close();
								inputStream.close();
							}
							
							
						} catch (Exception e) {
							e.printStackTrace();
						}
								
								
					}

					map=new HashMap<String, String>();
					map.put("songName", songName);
					map.put("songID", songID);
					map.put("userName", userName);
					map.put("userPic", imgFile.getAbsolutePath());
					map.put("postTime", postTime);

					if (imgFile==null) {
						Log.d("logd", "为空 default:"+imgFile.getAbsolutePath());
					}
					
					
					
					data.add(map);
					System.out.println("songName:" + songName + "\nID:" + songID
							+ "\nuserName:" + userName + "\nuserPic:" + userPic);
					System.out.println("\n");
				}
			}
			
		//	Toast.makeText(context, "当前页数  "+num, 1).show();
			
		} catch (Exception e) {
			e.printStackTrace();
			// 发送消息改变页面数据,取消刷新，刷新失败
			Message message = new Message();
			message.what = 0x004;
			MainActivity.handler.sendMessage(message);
			Toast.makeText(context, "获取列表失败，请重新刷新", 1).show();
		}
		
		

		if (isFromUI) {//由主线程发起de请求
			// 发送消息改变页面数据
			Message message = new Message();
			message.what = 0x003;
			message.obj=flag;
			MainActivity.handler.sendMessage(message);
			
			//将当前页刷新的数据存放到数据库
			//更新数据库表为当前页列表
			db.delete("netsonglist",null, null);
			for (int i = 0; i < data.size(); i++) {
				ContentValues values=new ContentValues();
				values.put("songName", data.get(i).get("songName"));
				values.put("songID", data.get(i).get("songID"));
				values.put("userName", data.get(i).get("userName"));
				values.put("userPic", data.get(i).get("userPic"));
				values.put("currentNum", num);
				db.insert("netsonglist", null, values);
			}
			
			Log.e("loge", "加载下一页");
			if (playNext) {
				Message message1=new Message();
				message1.what=0x003;
				PlayerService.handler.sendMessage(message1);
			}
			
		}else { //不是由主线程发起的请求，音乐播放自动加载下一页产生
			
			
			//不是由主线程发起的情求，合并播放列表
			for (int i = 0; i < data.size(); i++) {
				ContentValues values=new ContentValues();
				values.put("songName", data.get(i).get("songName"));
				values.put("songID", data.get(i).get("songID"));
				values.put("userName", data.get(i).get("userName"));
				values.put("userPic", data.get(i).get("userPic"));
				values.put("currentNum", num);
				db.insert("netsonglist", null, values);
			}
			Log.e("loge", "加载下一页222");

		}
				
		Looper.loop();
		
	}

	//run线程
	public void RunTheThread(String num) {
		flag="A";
		isFromUI=true;
		//data.clear();
		this.num=num;
		run();

	}
	//run线程
	public void RunTheThreadNext(String num) {
		flag="B";
		isFromUI=true;
		this.num = num;
		run();
	}

	/**
	 * 获取歌曲列表
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, String>> getSongList() throws Exception {
		
		return data;
	}
	
}
