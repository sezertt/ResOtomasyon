package com.res_otomasyon.resotomasyon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import java.util.List;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        /*
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        String networkSSID = "AirTies_Air5650_74XD";
        String networkPass = "m63uTpM7F6";
        String ssid;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        ssid = wifiInfo.getSSID();

        if (wifi.isAvailable())
        {

            if(wifi.isConnectedOrConnecting())
            {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        onReceive(context,intent);
                    }
                }, 5000);
            }

            if(!ssid.contentEquals(networkSSID)) // bizim ağımızdan farklı bir ağa bağlı ise bağlantıyı kopar.
            {
                wifiManager.disconnect();
            }
            else
            {
                return;
            }

            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = "\"" + networkSSID + "\"";

            //WPA
            wifiConfig.preSharedKey = "\""+ networkPass +"\"";

            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for( WifiConfiguration i : list ) {
                if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    if(!ssid.contentEquals("\"" + networkSSID + "\""))
                    {
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(i.networkId, true);
                        wifiManager.reconnect();
                        break;
                    }
                }
            }
        }*/
    }
}
