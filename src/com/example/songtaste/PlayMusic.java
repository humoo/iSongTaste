package com.example.songtaste;

import com.example.songtaste.util.DownloadSongThread;

import android.app.Activity;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.TextView;

public class PlayMusic extends Activity{

	TextView songName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ati_playmusic);
		
		songName=(TextView) findViewById(R.id.tv_songname);
		
		songName.setText(getIntent().getStringExtra("songName"));
		
		//线程下载歌曲
		DownloadSongThread downloadSongThread=new DownloadSongThread(this,getIntent().getStringExtra("songName"),
				getIntent().getStringExtra("songID"));
		System.out.println("进入PlayMusic.class");
		downloadSongThread.start();
		
	}
	
	
	
}
