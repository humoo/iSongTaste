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

	public MediaPlayer mediaPlayer; // ý�岥����
	private Timer mTimer = new Timer(); // ��ʱ��
	Context context;
	SharedPreferences sp;
	 private static final int NOTIFICATION_FLAG = 1;  
	 static NotificationManager manager;
	boolean flag=false;
	// ��ʼ��������
	public Player(Context context) {
		super();
		this.context=context;
		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);  
		
		sp=context.getSharedPreferences("config",context.MODE_PRIVATE);
		try {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// ����ý��������
			mediaPlayer.setOnBufferingUpdateListener(this);
			mediaPlayer.setOnPreparedListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// ÿһ�봥��һ��
		mTimer.schedule(timerTask, 0, 1000);
	}

	// ��ʱ��
	TimerTask timerTask = new TimerTask() {

		@Override
		public void run() {
			if (mediaPlayer == null)
				return;

			if (mediaPlayer.isPlaying()) {
				
				handler.sendEmptyMessage(0);
				
			}else if (flag) {//����ͣ��������һ��
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
						// ������ȣ���ȡ���������̶�*��ǰ���ֲ���λ�� / ��ǰ����ʱ����
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
	 *            url��ַ
	 */
	public void playUrl(String url) {
		flag=false;
		//NOTIFICATION��ʾ������Ϣ
		Intent intent=new Intent(context, SongInfo.class);
		intent.putExtra("songInfo", PlayerService.currentSongInfo());
		PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 1,  
				intent , PendingIntent.FLAG_CANCEL_CURRENT);  
        // ͨ��Notification.Builder������֪ͨ��ע��API Level  
        // API11֮���֧��  
        Notification notify2 = new Notification.Builder(context)  
                .setSmallIcon(R.drawable.st) // ����״̬���е�СͼƬ���ߴ�һ�㽨����24��24�����ͼƬͬ��Ҳ��������״̬��������ʾ�������������Ҫ���������ͼƬ������ʹ��setLargeIcon(Bitmap  
                                                    // icon)  
                .setTicker("���ڲ���:" + PlayerService.songName)// ������status  
                                                            // bar����ʾ����ʾ����  
                .setContentTitle("���ڲ���")// ����������status  
                                                        // bar��Activity���������е�NotififyMessage��TextView����ʾ�ı���  
                .setContentText(PlayerService.songName)// TextView����ʾ����ϸ����  
                .setContentIntent(pendingIntent2) // ����PendingIntent  
               // .setNumber(1) // ��TextView���ҷ���ʾ�����֣��ɷŴ�ͼƬ���������Ҳࡣ���numberͬʱҲ��һ�����кŵ����ң��������������֪ͨ��ͬһID��������ָ����ʾ��һ����  
                .getNotification(); // ��Ҫע��build()����API level  
        // 16��֮�����ӵģ���API11�п���ʹ��getNotificatin()������  
        notify2.flags |=  Notification.FLAG_NO_CLEAR;  
        manager.notify(NOTIFICATION_FLAG, notify2);  
		
		
		if (ConnectivityUtils.isMobileAvailable(context) && sp.getBoolean("wifi", true)) {//�����ǰΪ�ֻ����粢����������wifi��ʹ��
		
			AlertDialog.Builder builder=new AlertDialog.Builder(context);
			builder.setTitle("��ʾ");
			builder.setMessage("��ǰΪ�ֻ����磬����ʹ���赽�����ｫ��Wifi����ģʽ�ر�\n�Ƿ������");
			builder.setPositiveButton("��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent=new Intent(context,SettingActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
				}
			});
			builder.setNegativeButton("��", null);
			AlertDialog alertDialog = builder.create();
			// ��Ҫ����AlertDialog�����ͣ���֤�ڹ㲥�������п�����������
			alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			alertDialog.show();	
			
		}else {
			try {
				mediaPlayer.reset();
				mediaPlayer.setDataSource(url); // ��������Դ
				mediaPlayer.prepare(); // prepare�Զ�����
//				Log.d("logd", "�������ӣ�"+url);
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

	// ��ͣ
	public void pause() {
		flag=false;
		mediaPlayer.pause();
	}

	// ֹͣ
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
	 * �������
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
	 * ����ʱ�䳤��
	 * @return
	 */
	public String getSongLength() {
		if (mediaPlayer==null) {
			return "";
		}
		return mediaPlayer.getDuration()+"";
	}

	/**
	 * �������ڲ���
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
