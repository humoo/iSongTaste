package com.example.songtaste.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import com.example.songtaste.R;
import com.example.songtaste.R.drawable;
import com.example.songtaste.R.id;
import com.example.songtaste.R.layout;
import com.example.songtaste.service.PlayerService;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SongListAdapter extends BaseAdapter{

	List<Map<String, String>> data=new ArrayList<Map<String,String>>();
	Context context;
	Map<String, String> map=new HashMap<String, String>();
	Bitmap bitmap;
	
	public SongListAdapter(Context context,
			List<Map<String, String>> data) {
		this.context=context;
		this.data=data;
		
	}

	public void setData(List<Map<String, String>> data){
		this.data=data;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View view = LayoutInflater.from(context).inflate(R.layout.item_songlist, null);
		TextView songName=(TextView) view.findViewById(R.id.item);
		TextView userName=(TextView) view.findViewById(R.id.tv_username);
		TextView postTime=(TextView) view.findViewById(R.id.tv_posttime);
		final TextView isplaying=(TextView) view.findViewById(R.id.tv_isplaying);
		ImageView icon=(ImageView) view.findViewById(R.id.iv_img);
		map=data.get(position);
		songName.setText(map.get("songName"));
		userName.setText(map.get("userName"));
		postTime.setText(map.get("postTime"));
			 
		if (map.get("userPic").contains("default.gif")) {
			bitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.defaulticon);
		}else {
			bitmap=BitmapFactory.decodeFile(map.get("userPic"));
		}
		
		if (PlayerService.player!=null) {
			if (PlayerService.songID.equals(map.get("songID"))) {
				isplaying.setVisibility(View.VISIBLE);
				
			}
		}
		
//		view.setOnClickListener(new OnClickListener() {			
//			@Override
//			public void onClick(View v) {				
//				isplaying.setVisibility(View.VISIBLE);
//			}
//		});
		
		icon.setImageBitmap(bitmap);
		
		
		return view;
	}

}
