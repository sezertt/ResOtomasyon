package com.res_otomasyon.resotomasyon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by sezer on 12.08.2014.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        String networkSSID = "AirTies_Air5650_74XD";
        String networkPass = "m63uTpM7F6";

        if (wifi.isAvailable() && !wifi.isConnectedOrConnecting()) {

            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes

            //WPA
            wifiConfig.preSharedKey = "\""+ networkPass +"\"";

            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

            int netId = wifiManager.addNetwork(wifiConfig);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
        }
        else
        {
            String ssid = "";
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED) {
                ssid = wifiInfo.getSSID();
            }
            if(!ssid.contentEquals(networkSSID)) // bizim ağımızdan farklı bir ağa bağlı ise bağlantıyı kopar.
            {
                wifiManager.disconnect();
                onReceive(context,intent);
            }
        }
    }
}
