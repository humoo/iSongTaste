package com.example.songtaste.receiver;

import com.example.songtaste.service.PlayerService;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// 如果是去电
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			
			if (PlayerService.player!=null && PlayerService.player.musicIsPlaying()) {
				Log.e("loge","去电======================================");
				PlayerService.player.pause();
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Service.TELEPHONY_SERVICE);
				tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
			}
			
			
		} else {
			// 查了下android文档，貌似没有专门用于接收来电的action,所以，非去电即来电.
			// 如果我们想要监听电话的拨打状况，需要这么几步 :
			if (PlayerService.player!=null && PlayerService.player.musicIsPlaying()) {
				Log.e("loge","来电======================================");
				PlayerService.player.pause();
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Service.TELEPHONY_SERVICE);
				tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
			}
			
		
			
			

		}
	}
	
	// 设置一个监听器
	PhoneStateListener listener = new PhoneStateListener() {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// 注意，方法必须写在super方法后面，否则incomingNumber无法获取到值。
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				System.out.println("挂断");
				if (PlayerService.player!=null) {
					PlayerService.player.play();;
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				System.out.println("接听");
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				System.out.println("响铃:来电号码" + incomingNumber);
				// 输出来电号码
				break;
			}
		}
	};
}
