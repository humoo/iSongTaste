package com.example.songtaste;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.songtaste.db.DBOpenHelper;
import com.example.songtaste.service.PlayerService;

public class SongInfo extends Activity implements OnClickListener {

	ImageView btnBack;
	static ImageView btnPlay;
	ImageView btnPre;
	ImageView btnNext;
	ImageView btnDownload;
	static ImageView btnLoveThis;
	public static SeekBar seekBar;
	static TextView showSongName, showTime;
	DBOpenHelper dbHepler;
	static SQLiteDatabase db;
	SharedPreferences sp;
	Editor editor;
	int mProgress=0;
	static Intent intent;
	public static boolean flag=true;//是否seekbar被按下
	public static Handler handler =new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0x001) {//改变播放状态
				intent=(Intent) msg.obj;
				Log.d("logd", intent.getStringExtra("songInfo"));
				showSongName.setText("正在播放："+intent.getStringExtra("songInfo").split(",zxm,")[0]);
				btnPlay.setImageResource(R.drawable.pauseatplaying_pressed);	
				
				//是否喜欢了这首歌
				Cursor cursor=db.rawQuery("SELECT * FROM lovesonglist WHERE songID = ?" ,
						new String[]{intent.getStringExtra("songInfo").split(",zxm,")[2]});
				if (cursor.getCount()>0) {
					btnLoveThis.setImageResource(R.drawable.global_comment_like_selected);
				}else {
					btnLoveThis.setImageResource(R.drawable.loveblack);
				}
			}
			
			int m1,m2,s1,s2,pos;
			
			if (msg.what == 0x002) {//改变seekbar状态
				btnPlay.setImageResource(R.drawable.pauseatplaying_pressed);		
				m2=(msg.arg2/1000)/60;
				s2=((msg.arg2/1000)%60);
				m1=(msg.arg1/1000)/60;
				s1=((msg.arg1/1000)%60);
				pos = seekBar.getMax() * msg.arg1
						/ msg.arg2 + 1;
				seekBar.setProgress(pos);
				showTime.setText(m1+":"+s1+"/"+m2+":"+s2);
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.songinfo_activity);

		intent=getIntent();
		
		sp=getSharedPreferences("config", MODE_PRIVATE);
		editor=sp.edit();
		dbHepler=new DBOpenHelper(this);
		db=dbHepler.getWritableDatabase();
		init();
		
		/**
		 * 进度条控制
		 */
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				flag=false;
				Log.e("loge", "onStopTrackingTouch");
				seekBar.setProgress(mProgress);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				Log.e("loge", "onStartTrackingTouch");
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mProgress=progress;
				Log.e("loge", "onProgressChanged");

				seekBar.setProgress(progress);
			}
		});
		
	}

	private void init() {

		btnBack = (ImageView) findViewById(R.id.iv_back);
		btnDownload = (ImageView) findViewById(R.id.iv_downloadsong);
		btnLoveThis = (ImageView) findViewById(R.id.iv_lovethis);
		btnPre = (ImageView) findViewById(R.id.iv_pre);
		btnPlay = (ImageView) findViewById(R.id.iv_play);
		btnNext = (ImageView) findViewById(R.id.iv_next);
		showSongName = (TextView) findViewById(R.id.tv_showsongname);
		showTime = (TextView) findViewById(R.id.iv_showtime);
		seekBar = (SeekBar) findViewById(R.id.seekBar1);

		btnBack.setOnClickListener(this);
		btnDownload.setOnClickListener(this);
		btnLoveThis.setOnClickListener(this);
		btnPre.setOnClickListener(this);
		btnPlay.setOnClickListener(this);
		btnNext.setOnClickListener(this);

		
		if (!intent.getStringExtra("songInfo").equals("")) {
			showSongName.setText("正在播放："+intent.getStringExtra("songInfo").split(",zxm,")[0]);
			//Log.d("logd", "getIntent().getStringExtra(\"songInfo\").split(\",zxm,\")[2]："+intent.getStringExtra("songInfo").split(",zxm,")[2]);

			Cursor cursor=db.rawQuery("SELECT * FROM lovesonglist WHERE songID = ?" ,
					new String[]{intent.getStringExtra("songInfo").split(",zxm,")[2]});

			if (cursor.getCount()>0) {
				cursor.moveToFirst();
				//Log.d("logd", "cursor.getString(cursor.getColumnIndex(\"songID\"):"+cursor.getString(cursor.getColumnIndex("songID")));
				if (cursor.getString(cursor.getColumnIndex("songID"))//如果数据库中的songID与当前songID一样
						.equals(intent.getStringExtra("songInfo").split(",zxm,")[2])) {
					btnLoveThis.setImageResource(R.drawable.global_comment_like_selected);
				}
				
			}
		}
		
		if (PlayerService.player!=null) {
			if (PlayerService.player.musicIsPlaying()) {//音乐播放中
				btnPlay.setImageResource(R.drawable.pauseatplaying_pressed);				
			}else {//暂停
				btnPlay.setImageResource(R.drawable.playatplaying);
			}
		}
		
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.iv_back:
			finish();
			break;
		case R.id.iv_downloadsong://下载当前歌曲
			if (PlayerService.player!=null) {
				downLoadSong();
				loveThisSong();
			}
			break;
		case R.id.iv_lovethis://喜欢这首歌
			if (PlayerService.player!=null) {
				loveThisSong();
			}
			break;
		case R.id.iv_pre://切换上一首歌
			preSong();
			break;
		case R.id.iv_play://播放或暂停
			playOrPause();
			break;
		case R.id.iv_next://切换下一首歌
			nextSong();
			break;
		}
	}

	private void nextSong() {
		if (PlayerService.player!=null) {
			PlayerService.nextSong();	
		}else {
			Toast.makeText(SongInfo.this,"播放列表为空", 1).show();
		}
	}

	private void playOrPause() {
		if (PlayerService.player!=null) {
			if (PlayerService.player.musicIsPlaying()) {
				btnPlay.setImageResource(R.drawable.playatplaying);
				editor.putBoolean("isPlaying", false);
				editor.commit();
				PlayerService.stopPlay();//暂停播放音乐
				
			}else {
				btnPlay.setImageResource(R.drawable.pauseatplaying_pressed);
				editor.putBoolean("isPlaying", true);
				editor.commit();
				PlayerService.resumePlay();//恢复音乐播放
			}
			
		}else {
			Toast.makeText(SongInfo.this,"播放列表为空", 1).show();
		}
	}

	private void preSong() {
		if (PlayerService.player!=null) {
			PlayerService.preSong();
		}else {
			Toast.makeText(SongInfo.this,"播放列表为空", 1).show();
		}
	}

	private void loveThisSong() {
		
		Cursor cursor=db.rawQuery("SELECT * FROM lovesonglist WHERE songID = ?" ,
				new String[]{intent.getStringExtra("songInfo").split(",zxm,")[2]});
		if (cursor.getCount()>0) {//取消喜欢，将其从数据库移除
			db.delete("lovesonglist", "songID=?", new String[]{intent.getStringExtra("songInfo").split(",zxm,")[2]});
			btnLoveThis.setImageResource(R.drawable.loveblack);
		}else {//喜欢这首歌，加入数据库
			ContentValues values=new ContentValues();
			values.put("songName", intent.getStringExtra("songInfo").split(",zxm,")[0]);
			values.put("songID", intent.getStringExtra("songInfo").split(",zxm,")[2]);
			values.put("userName", intent.getStringExtra("songInfo").split(",zxm,")[3]);
			values.put("userPic", intent.getStringExtra("songInfo").split(",zxm,")[4]);
			db.insert("lovesonglist", null, values);
			btnLoveThis.setImageResource(R.drawable.global_comment_like_selected);
		}
		
		
	}

	@Override
	protected void onDestroy() {
		seekBar=null;
		//handler=null;
		super.onDestroy();
	}
	
	private void downLoadSong() {
		// TODO Auto-generated method stub
		
	}
}
