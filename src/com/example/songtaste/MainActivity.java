package com.example.songtaste;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnScrollListener;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.songtaste.adapter.SongListAdapter;
import com.example.songtaste.db.DBOpenHelper;
import com.example.songtaste.service.PlayerService;
import com.example.songtaste.util.GetSongListThread;
import com.example.songtaste.util.Player;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
/**
 * 
 * @author zhengxumao
 *
 */
public class MainActivity extends Activity implements OnClickListener{

	Intent serviceIntent;

	static PullToRefreshListView songList;
	public static ProgressBar progressBar;
	AlertDialog.Builder builder;
	int num = 1;//��ǰҳ����
	static List<Map<String, String>> data =new ArrayList<Map<String,String>>();
	Map<String, String> map;
	static ImageView btnPlay,iconPic;
	ImageView setting,loveList;
	ImageView btnNext;
	ImageView btnPrev;
	ImageView btnSearch;
	static Bitmap bitmap;
	static TextView songinfo;
	NumberPicker numPicker;
	static SongListAdapter adapter;
	public static GetSongListThread getSongListThread;
	Boolean isPlaying=false;//�Ƿ��ڲ�������
	SharedPreferences sp;
	static Editor editor;
	
	
	public static Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			
			if(msg.what==0x001){
				
				progressBar.setProgress(msg.arg1);
				btnPlay.setImageResource(R.drawable.widget_control_pause_hover);
			}
			if (msg.what==0x002) {//�ı䲥��ͼ�꣬��ʼ����
				btnPlay.setImageResource(R.drawable.widget_control_pause_hover);
				
				String picPath=((String) msg.obj).split(",")[0];
				songinfo.setVisibility(View.VISIBLE);
				songinfo.setText("���ڲ��ţ�"+((String) msg.obj).split(",")[1]);
				bitmap=BitmapFactory.decodeFile(picPath);
				iconPic.setImageBitmap(bitmap);
			}
			if (msg.what==0x003) {//�ı�ҳ�����ݣ�ˢ��ҳ��
				try {
					
					data=getSongListThread.getSongList();
					//Log.d("logd", "data.size()"+data.size());

				} catch (Exception e) {
					e.printStackTrace();
				}
				
				String flag=(String)msg.obj.toString();
				//Log.d("logd", "flagflag---"+flag);
				
				if (flag=="A") {
					songList.getRefreshableView().setSelection(1);
				//	Log.d("logd", "flagflag--A-"+flag);
				}				
				adapter.setData(data);
				//adapter.notifyDataSetChanged();
				songList.onRefreshComplete();
			}
			if (msg.what==0x004) {
				songList.onRefreshComplete();//ȡ��ˢ��״̬
			}
			
			if (msg.what==0x005) {
				btnPlay.setImageResource(R.drawable.widget_control_pause_hover);
			}
			
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		songList = (PullToRefreshListView) findViewById(R.id.lv_songList);
		progressBar=(ProgressBar) findViewById(R.id.progressBar1);
		
		iconPic=(ImageView) findViewById(R.id.iv_songpic);
		btnPlay=(ImageView) findViewById(R.id.iv_center);
		btnPrev=(ImageView) findViewById(R.id.iv_pre);
		btnNext=(ImageView) findViewById(R.id.iv_next);
		loveList=(ImageView) findViewById(R.id.iv_songlist);
		songinfo=(TextView) findViewById(R.id.tv_songinfo);
		setting=(ImageView) findViewById(R.id.iv_setting);
		setting.setOnClickListener(this);
		iconPic.setOnClickListener(this);
		btnPlay.setOnClickListener(this);
		btnPrev.setOnClickListener(this);
		btnNext.setOnClickListener(this);
		loveList.setOnClickListener(this);
		btnSearch=(ImageView) findViewById(R.id.iv_search);
		btnSearch.setOnClickListener(this);
		sp=getSharedPreferences("config",MODE_PRIVATE);
		editor=sp.edit();
		
		//��һ�ν���
		if (sp.getBoolean("isFirst",true)) {
			Log.d("logd","��һ�ν������");
			editor.putBoolean("isFirst", false);
			editor.putBoolean("wifi", true);
			editor.commit();
		}
		
		try {
			getSongListThread=new GetSongListThread(this,"1");
			getSongListThread.start();
			data=getSongListThread.getSongList();
			Log.d("logd", "data.size()"+data.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		/*
		 * ����PullToRefreshˢ��ģʽ BOTH:����ˢ�º�����ˢ�¶�֧�� DISABLED��������������ˢ��
		 * PULL_FROM_START:��֧������ˢ�£�Ĭ�ϣ� PULL_FROM_END����֧������ˢ��
		 * MANUAL_REFRESH_ONLY��ֻ�����ֶ�����
		 */
		songList.setMode(Mode.BOTH);
		songList.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {

				if (refreshView.isHeaderShown()) {

					new Thread(new Runnable() {
						@Override
						public void run() {
							num=1;
							getSongListThread.RunTheThread(num+"");
							
						}
					}).start();
				}
				if (refreshView.isFooterShown()) {//ˢ����һҳ
					new Thread(new Runnable() {
						@Override
						public void run() {
							num=1+num;
							getSongListThread.RunTheThreadNext(""+num);
							
						}
					}).start();
				}
			}
		});
		serviceIntent =new Intent(MainActivity.this,PlayerService.class);//���ֲ��ŷ���
		//���item
		songList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				
				map=new HashMap<String,String>();
				map=data.get(position-1);
				
				serviceIntent.putExtra("songName",map.get("songName"));
				serviceIntent.putExtra("songID",map.get("songID"));
				serviceIntent.putExtra("userName",map.get("userName"));
				serviceIntent.putExtra("userPic",map.get("userPic"));
				serviceIntent.putExtra("postTime",map.get("postTime"));
				
				isPlaying=true;//���ڲ�������
				editor.putBoolean("isPlaying", true);
				editor.putBoolean("isLovesonglist", false);
				editor.commit();
				//startActivity(intent);
				startService(serviceIntent);//����һ���������ַ���
				
			
			}
		});
		
		adapter =new SongListAdapter(MainActivity.this,data);
		songList.setAdapter(adapter);
		
		
		
	}

	@Override
	protected void onStart() {
//		adapter =new SongListAdapter(MainActivity.this,data);
//		songList.setAdapter(adapter);
		super.onStart();
	}


	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.iv_search://��תҳ��
			selectNum();			
			break;

		case R.id.iv_center://�������/��ͣ��ť
			if (PlayerService.player!=null) {
				if (sp.getBoolean("isPlaying",false)) {//������ͣ��
					btnPlay.setImageResource(R.drawable.widget_control_play_hover);
					editor.putBoolean("isPlaying", false);
					editor.commit();
					PlayerService.stopPlay();//��ͣ��������
					
				}else {//���ڲ���
					btnPlay.setImageResource(R.drawable.widget_control_pause_hover);
					editor.putBoolean("isPlaying", true);
					editor.commit();
					PlayerService.resumePlay();//�ָ����ֲ���
				}
			}else {
				Toast.makeText(MainActivity.this,"�����б�Ϊ��", 1).show();
			}
			
			//Log.d("logd","���ڲ������֣�"+isPlaying+"---"+sp.getBoolean("isPlaying",false));
			break;
		case R.id.iv_next://������һ�׸�
			if (PlayerService.player!=null) {
				PlayerService.nextSong();	
			}else {
				Toast.makeText(MainActivity.this,"�����б�Ϊ��", 1).show();
			}
			
			break;
		case R.id.iv_pre:// ������һ�׸���
			if (PlayerService.player!=null) {
				PlayerService.preSong();
			}else {
				Toast.makeText(MainActivity.this,"�����б�Ϊ��", 1).show();
			}
			break;
		case R.id.iv_setting://�������
			startActivity(new Intent(MainActivity.this,SettingActivity.class));
			break;
		case R.id.iv_songpic:
			Intent intent=new Intent(MainActivity.this,SongInfo.class);
			intent.putExtra("songInfo", PlayerService.currentSongInfo());
			startActivity(intent);
			break;
		case R.id.iv_songlist:
			Intent loveIntent=new Intent(MainActivity.this,LoveSong.class);
			startActivity(loveIntent);
			break;
		}
		
	}


	@Override
	protected void onResume() {
		if (PlayerService.player!=null) {
			if (PlayerService.player.musicIsPlaying()) {
				btnPlay.setImageResource(R.drawable.widget_control_pause_hover);
			}else {
				btnPlay.setImageResource(R.drawable.widget_control_play_hover);			
			}
//			if (sp.getBoolean("isPlaying",false)) {//������ͣ��
//				btnPlay.setImageResource(R.drawable.widget_control_pause_hover);				
//			}else {//���ڲ���
//				btnPlay.setImageResource(R.drawable.widget_control_play_hover);
//			}
		}
		super.onResume();
	}


	/**
	 * ҳ����ת
	 */
	public void selectNum() {
		View view =LayoutInflater.from(this).inflate(R.layout.search_alert, null);//��ʾ�򲼾����
		numPicker=(NumberPicker) view.findViewById(R.id.num_picker);
		numPicker.setMaxValue(20+num);
		numPicker.setMinValue(1);
		numPicker.setValue(num);
		numPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);//��ֹNumberPicker����
		numPicker.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChange(NumberPicker view, int scrollState) {
				numPicker.setValue(num);
			}
		});
		
		
		numPicker.setOnValueChangedListener(new OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				num=newVal;
				picker.setValue(num);
			}
		});
		builder=new AlertDialog.Builder(this);
		builder.setTitle("��ѡ��ҳ��");
		builder.setView(view);
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						getSongListThread.RunTheThread(num+"");
						
					}
				}).start();
			}
		});
		builder.setNegativeButton("ȡ��",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.create().show();
	}


	@Override
	protected void onDestroy() {
		isPlaying=false;
		editor.putBoolean("isPlaying", false);
		editor.commit();
		
		if (serviceIntent!=null) {
			
			stopService(serviceIntent);
			Player.cancelNotification();
		}
		
		
		finish();
		super.onDestroy();
	}
	

	@Override
	public void onBackPressed() {
		
		
	}
	
	/**
	 * �����������
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        Intent home = new Intent(Intent.ACTION_MAIN);
	        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        home.addCategory(Intent.CATEGORY_HOME);
	        startActivity(home);
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		
		AlertDialog.Builder builder  =new AlertDialog.Builder(this);
		builder.setTitle("��ʾ");
		builder.setMessage("ȷ��Ҫ�˳���");
		builder.setPositiveButton("ȷ��",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				onDestroy();
			}
		});
		builder.setNegativeButton("ȡ��", null);
		builder.create().show();
		
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.main, menu);  
		return super.onCreateOptionsMenu(menu);
	}
}
