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
		// �����ȥ��
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			
			if (PlayerService.player!=null && PlayerService.player.musicIsPlaying()) {
				Log.e("loge","ȥ��======================================");
				PlayerService.player.pause();
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Service.TELEPHONY_SERVICE);
				tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
			}
			
			
		} else {
			// ������android�ĵ���ò��û��ר�����ڽ��������action,���ԣ���ȥ�缴����.
			// ���������Ҫ�����绰�Ĳ���״������Ҫ��ô���� :
			if (PlayerService.player!=null && PlayerService.player.musicIsPlaying()) {
				Log.e("loge","����======================================");
				PlayerService.player.pause();
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Service.TELEPHONY_SERVICE);
				tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
			}
			
		
			
			

		}
	}
	
	// ����һ��������
	PhoneStateListener listener = new PhoneStateListener() {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// ע�⣬��������д��super�������棬����incomingNumber�޷���ȡ��ֵ��
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				System.out.println("�Ҷ�");
				if (PlayerService.player!=null) {
					PlayerService.player.play();;
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				System.out.println("����");
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				System.out.println("����:�������" + incomingNumber);
				// ����������
				break;
			}
		}
	};
}
