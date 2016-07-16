package com.example.songtaste.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class DownloadSongThread extends Thread {

	String songID;
	String songName;
	String songUrl;
	Context context;
	public DownloadSongThread(Context context,String songName, String songID) {
		this.songID = songID;
		this.songName = songName;
		this.context=context;
		//System.out.println("进入DownloadSongThread");

	}

	@Override
	public void run() {

		try {
			URL musicList = new URL("http://songtaste.com/song/"+songID);

			HttpURLConnection conn = (HttpURLConnection) musicList.openConnection();

			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");

			conn.setRequestProperty("User-Agent", "Mozilla/4.0");	
			
			InputStream inputStream = conn.getInputStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));

			StringBuilder builder = new StringBuilder();

			String LINE = "";
			while ((LINE = reader.readLine()) != null) {

				builder.append(LINE + "\n");
			}

			reader.close();
			inputStream.close();

			//System.out.println(builder.toString());
			
			String[] arr = builder.toString().split("\n");
			String str = "";
			String sid = "";
			String t = "";

			for (int i = 0; i < arr.length; i++) {

				if (arr[i]
						.contains("<div id=\"playicon\" style=\"margin-left:-12px\">")) {
					Log.w("logw","arr[i + 1]"+ arr[i + 1]);
					str = arr[i + 1].split(",")[2].substring(2,
							arr[i + 1].split(",")[2].length() - 1);
					sid = arr[i + 1].split(",")[5].substring(2,
							arr[i + 1].split(",")[5].length() - 1);
					t = arr[i + 1].split(",")[8].substring(0, 1);// 已知为0，不知是否可以为多位数字

					
//					System.out.println(arr[i + 1] + "\n" + str + "\n" + sid + "\n"
//							+ t);
				}

			}

			// 获取url
			URL url = new URL("http://songtaste.com/time.php?str=" + str + "&sid="
					+ sid + "&t=" + t);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

			
			urlConn.setConnectTimeout(5000);
			urlConn.setRequestMethod("POST");
			InputStream in = urlConn.getInputStream();

			BufferedReader reader2 = new BufferedReader(new InputStreamReader(in));

			
			String LINE2 = "";
			while ((LINE2 = reader2.readLine()) != null) {

				songUrl=LINE2;
			}
			System.out.println(songUrl);//下载链接
			
			reader2.close();
			in.close();

			//下载歌曲
			URL downUrl =new URL(songUrl);
			HttpURLConnection downConn=(HttpURLConnection) downUrl.openConnection();
			downConn.setConnectTimeout(5000);
			downConn.setRequestMethod("GET");
			
			//存在内存卡
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				
				File saveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
				
				if (!saveDir.exists()) {
					saveDir.mkdirs();
				}
				
				File songFile=new File(saveDir,songName+songUrl.substring(songUrl.length()-5));
				
				if (songFile.exists()) {
					//文件已存在则不重新下载
				}else {

					InputStream ins=downConn.getInputStream();
					
					FileOutputStream fos=new FileOutputStream(songFile);
					
					int len=-1;
					byte[] buffer=new byte[1024];
					while ((len=ins.read(buffer))!=-1) {
						fos.write(buffer,0,len);
					}
					
					fos.close();
					ins.close();
					
				}
				
				MediaPlayer mediaPlayer =  new MediaPlayer();//媒体播放器对象
				mediaPlayer.reset();
				mediaPlayer.setDataSource(songFile.getAbsolutePath());
				mediaPlayer.prepare();
				mediaPlayer.start();
				
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getSongUrl() throws Exception {
		
		return songUrl;
		
	}
	
}
