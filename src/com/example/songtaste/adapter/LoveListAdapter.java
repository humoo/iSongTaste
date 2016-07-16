package com.example.songtaste.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.songtaste.R;

public class LoveListAdapter extends BaseAdapter {

	List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	Map<String, String> map=new HashMap<String, String>();
	Bitmap bitmap;
	Context context;
	
	public LoveListAdapter(Context context, List<Map<String, String>> data) {
		this.data = data;
		this.context=context;
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

		View view = LayoutInflater.from(context).inflate(
				R.layout.item_songlist, null);
		TextView songName = (TextView) view.findViewById(R.id.item);
		TextView userName = (TextView) view.findViewById(R.id.tv_username);
		TextView postTime = (TextView) view.findViewById(R.id.tv_posttime);
		final TextView isplaying = (TextView) view
				.findViewById(R.id.tv_isplaying);
		ImageView icon = (ImageView) view.findViewById(R.id.iv_img);
		map = data.get(position);
		songName.setText(map.get("songName"));
		userName.setText(map.get("userName"));
		postTime.setVisibility(View.GONE);
		if (map.get("userPic").contains("default.gif")) {
			bitmap = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.defaulticon);
		} else {
			try {
				bitmap = BitmapFactory.decodeFile(map.get("userPic"));
			} catch (Exception e) {
				Log.e("loge", "LoVElistAdapter头像文件不存在");
			}
			
		}
		if (bitmap!=null) {
			icon.setImageBitmap(bitmap);
		}

		return view;
	}

}
