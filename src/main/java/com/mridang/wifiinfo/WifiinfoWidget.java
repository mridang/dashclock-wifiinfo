package com.mridang.wifiinfo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

import com.google.android.apps.dashclock.api.ExtensionData;

import org.acra.ACRA;

import java.util.List;

/*
 * This class is the main class that provides the widget
 */
public class WifiinfoWidget extends ImprovedExtension {

	/*
	 * (non-Javadoc)
	 * @see com.mridang.battery.ImprovedExtension#getIntents()
	 */
	@Override
	protected IntentFilter getIntents() {

		IntentFilter itfIntents = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		itfIntents.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		itfIntents.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		return itfIntents;

	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.battery.ImprovedExtension#getTag()
	 */
	@Override
	protected String getTag() {
		return getClass().getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.battery.ImprovedExtension#getUris()
	 */
	@Override
	protected String[] getUris() {
		return null;
	}

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
	 * (int)
	 */
	@Override
	protected void onUpdateData(int intReason) {

		Log.d(getTag(), "Fetching wireless network information");
		ExtensionData edtInformation = new ExtensionData();
		setUpdateWhenScreenOn(true);

		try {

			Log.d(getTag(), "Checking if connected to a wifi network");
			ConnectivityManager mgrConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			if (mgrConnect != null) {

				NetworkInfo nifWifi = mgrConnect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (nifWifi != null && nifWifi.isConnected()) {

					Log.d(getTag(), "Connected to a wireless network");
					WifiManager wifManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
					WifiInfo wifInfo = wifManager.getConnectionInfo();
					List<ScanResult> networks = wifManager.getScanResults();
					String security = null;
					if (networks != null) {
						for (ScanResult network : networks) {
							if (network.BSSID.equalsIgnoreCase(wifInfo.getBSSID())) {
								String capabilities = network.capabilities;
								Log.d(getTag(), "Current network security is " + capabilities);
								if (capabilities.contains("WEP")) {
									security = "WEP";
								} else if (capabilities.contains("WPA2-PSK")) {
									security = "WPA2-PSK";
								} else if (capabilities.contains("WPA2")) {
									security = "WPA2";
								} else if (capabilities.contains("WPA")) {
									security = "WPA";
								} else {
									security = null;
								}
							}
						}
					}

					if (wifInfo != null && !wifInfo.getSSID().trim().isEmpty()) {

						Log.d(getTag(), wifInfo.getSSID().replaceAll("^\"|\"$", ""));
						edtInformation.visible(true);
						edtInformation.status(wifInfo.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS);
						edtInformation.expandedTitle(wifInfo.getSSID().replaceAll("^\"|\"$", ""));
						if (security == null) {
							edtInformation.expandedBody(getString(R.string.unsecured_network, wifInfo.getLinkSpeed(), WifiInfo.LINK_SPEED_UNITS));
						} else {
							edtInformation.expandedBody(getString(R.string.secured_network, security, wifInfo.getLinkSpeed(), WifiInfo.LINK_SPEED_UNITS));
						}
						edtInformation.clickIntent(new Intent(Settings.ACTION_WIFI_SETTINGS));

					}

				} else {
					Log.d(getTag(), "Not connected to a wireless network");
				}

			} else {
				Log.d(getTag(), "No wireless connection available");
			}

		} catch (Exception e) {
			edtInformation.visible(false);
			Log.e(getTag(), "Encountered an error", e);
			ACRA.getErrorReporter().handleSilentException(e);
		}

		edtInformation.icon(R.drawable.ic_dashclock);
		doUpdate(edtInformation);

	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.alarmer.ImprovedExtension#onReceiveIntent(android.content.Context, android.content.Intent)
	 */
	@Override
	protected void onReceiveIntent(Context ctxContext, Intent ittIntent) {
		onUpdateData(UPDATE_REASON_MANUAL);
	}

}