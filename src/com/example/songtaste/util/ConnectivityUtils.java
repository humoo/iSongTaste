package com.example.songtaste.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class ConnectivityUtils {

	private static ConnectivityManager connMgr = null;
	private static NetworkInfo info = null;
	private static int type = -1; // -1 ��ʾ������ ConnectivityManager.TYPE_NONE

	public static boolean isConnectivityAvailable(Context context) {
		connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		info = connMgr.getActiveNetworkInfo();
		if (info == null) {
			return false;
		}
		type = info.getType();
		return info.isAvailable();
	}

	/**
	 * wifi�Ƿ����
	 * @param context
	 * @return
	 */
	public static boolean isWifiAvailable(Context context) {
		if (!isConnectivityAvailable(context)) {
			return false;
		}
		return type == ConnectivityManager.TYPE_WIFI;
	}

	/**
	 * �ֻ�����
	 * @param context
	 * @return
	 */
	public static boolean isMobileAvailable(Context context) {
		if (!isConnectivityAvailable(context)) {
			return false;
		}
		return type == ConnectivityManager.TYPE_MOBILE;
	}
}