package com.example.songtaste.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.example.songtaste.MainActivity;
import com.example.songtaste.R;
import com.example.songtaste.SettingActivity;
import com.example.songtaste.SongInfo;
import com.example.songtaste.db.DBOpenHelper;
import com.example.songtaste.service.PlayerService;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.AvoidXfermode.Mode;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

public class Player implements OnBufferingUpdateListener, OnCompletionListener,
		OnPreparedListener {

	public MediaPlayer mediaPlayer; // 媒体播放器
	private Timer mTimer = new Timer(); // 计时器
	Context context;
	SharedPreferences sp;
	 private static final int NOTIFICATION_FLAG = 1;  
	 static NotificationManager manager;
	boolean flag=false;
	// 初始化播放器
	public Player(Context context) {
		super();
		this.context=context;
		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);  
		
		sp=context.getSharedPreferences("config",context.MODE_PRIVATE);
		try {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置媒体流类型
			mediaPlayer.setOnBufferingUpdateListener(this);
			mediaPlayer.setOnPreparedListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 每一秒触发一次
		mTimer.schedule(timerTask, 0, 1000);
	}

	// 计时器
	TimerTask timerTask = new TimerTask() {

		@Override
		public void run() {
			if (mediaPlayer == null)
				return;

			if (mediaPlayer.isPlaying()) {
				
				handler.sendEmptyMessage(0);
				
			}else if (flag) {//非暂停，播放下一曲
				mediaPlayer.stop();
				Message message1=new Message();
				message1.what=0x001;
				onCompletion(mediaPlayer);
				PlayerService.handler.sendMessage(message1);
			}
				
			
			
		}
	};

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what==0) {
				if (mediaPlayer!=null) {
					int position = mediaPlayer.getCurrentPosition();
					int duration = mediaPlayer.getDuration();
					if (duration > 0) {
						// 计算进度（获取进度条最大刻度*当前音乐播放位置 / 当前音乐时长）
						// long pos = seekBar.getMax() * position / duration;
						// seekBar.setProgress((int) pos);
						long pos = 0;
						
						if (SongInfo.seekBar != null) {
							if (SongInfo.flag==false) {
								long seekTime=(long) ((float)SongInfo.seekBar.getProgress()/100 * duration);
								mediaPlayer.seekTo( (int) seekTime);
								position = mediaPlayer.getCurrentPosition();
								SongInfo.flag=true;
								//Log.e("loge", "SongInfo.seekBar.isPressed()");

								pos = SongInfo.seekBar.getMax() * position
										/ duration + 1;
								Message message1=new Message();
								message1.what=0x002;
								message1.arg1=position;
								message1.arg2=duration;
								SongInfo.handler.sendMessage(message1);
							}else {
								//Log.e("loge", "---------------");

								pos = SongInfo.seekBar.getMax() * position
										/ duration + 1;
								Message message1=new Message();
								message1.what=0x002;
								message1.arg1=position;
								message1.arg2=duration;
								SongInfo.handler.sendMessage(message1);
							}
							
						}else {
							pos = MainActivity.progressBar.getMax() * position
									/ duration + 1;
							Message message = new Message();
							message.what = 0x001;
							message.arg1 = (int) pos;
							MainActivity.handler.sendMessage(message);
						
						}
						
						flag=true;
						Log.e("loge","pos:"+pos+"  position:"+position+"  duration:"+duration);

					} 
				}
				
			}
		};
	};

	public void play() {
		mediaPlayer.start();
	}

	/**
	 * 
	 * @param url
	 *            url地址
	 */
	public void playUrl(String url) {
		flag=false;
		//NOTIFICATION提示播放信息
		Intent intent=new Intent(context, SongInfo.class);
		intent.putExtra("songInfo", PlayerService.currentSongInfo());
		PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 1,  
				intent , PendingIntent.FLAG_CANCEL_CURRENT);  
        // 通过Notification.Builder来创建通知，注意API Level  
        // API11之后才支持  
        Notification notify2 = new Notification.Builder(context)  
                .setSmallIcon(R.drawable.st) // 设置状态栏中的小图片，尺寸一般建议在24×24，这个图片同样也是在下拉状态栏中所显示，如果在那里需要更换更大的图片，可以使用setLargeIcon(Bitmap  
                                                    // icon)  
                .setTicker("正在播放:" + PlayerService.songName)// 设置在status  
                                                            // bar上显示的提示文字  
                .setContentTitle("正在播放")// 设置在下拉status  
                                                        // bar后Activity，本例子中的NotififyMessage的TextView中显示的标题  
                .setContentText(PlayerService.songName)// TextView中显示的详细内容  
                .setContentIntent(pendingIntent2) // 关联PendingIntent  
               // .setNumber(1) // 在TextView的右方显示的数字，可放大图片看，在最右侧。这个number同时也起到一个序列号的左右，如果多个触发多个通知（同一ID），可以指定显示哪一个。  
                .getNotification(); // 需要注意build()是在API level  
        // 16及之后增加的，在API11中可以使用getNotificatin()来代替  
        notify2.flags |=  Notification.FLAG_NO_CLEAR;  
        manager.notify(NOTIFICATION_FLAG, notify2);  
		
		
		if (ConnectivityUtils.isMobileAvailable(context) && sp.getBoolean("wifi", true)) {//如果当前为手机网络并且设置网仅wifi下使用
		
			AlertDialog.Builder builder=new AlertDialog.Builder(context);
			builder.setTitle("提示");
			builder.setMessage("当前为手机网络，继续使用需到设置里将仅Wifi网络模式关闭\n是否继续？");
			builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent=new Intent(context,SettingActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
				}
			});
			builder.setNegativeButton("否", null);
			AlertDialog alertDialog = builder.create();
			// 需要设置AlertDialog的类型，保证在广播接收器中可以正常弹出
			alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			alertDialog.show();	
			
		}else {
			try {
				mediaPlayer.reset();
				mediaPlayer.setDataSource(url); // 设置数据源
				mediaPlayer.prepare(); // prepare自动播放
//				Log.d("logd", "下载链接："+url);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//flag=true;
	}

	// 暂停
	public void pause() {
		flag=false;
		mediaPlayer.pause();
	}

	// 停止
	public void stop() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
	
		}
		
	}
	
	public void stopMusic() {
		mediaPlayer.stop();
		onCompletion(mediaPlayer);
		mediaPlayer.reset();
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
			
		mp.start();
		flag=true;
		Log.e("mediaPlayer", "onPrepared");
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.e("mediaPlayer", "onCompletion");
		flag=false;
		mp.stop();
	}

	/**
	 * 缓冲更新
	 */
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		if (SongInfo.seekBar!=null) {
			SongInfo.seekBar.setSecondaryProgress(percent);
			int currentProgress = SongInfo.seekBar.getMax()
					* mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration();
//			Log.e(currentProgress + "% play", percent + " buffer");
		}
	
	}
	

	/**
	 * 歌曲时间长度
	 * @return
	 */
	public String getSongLength() {
		if (mediaPlayer==null) {
			return "";
		}
		return mediaPlayer.getDuration()+"";
	}

	/**
	 * 音乐正在播放
	 * @return
	 */
	public boolean musicIsPlaying() {
		return mediaPlayer.isPlaying();
	}
	
	public static void cancelNotification() {
		if (PlayerService.player!=null) {
			manager.cancel(NOTIFICATION_FLAG);
		}
		
	}
}
