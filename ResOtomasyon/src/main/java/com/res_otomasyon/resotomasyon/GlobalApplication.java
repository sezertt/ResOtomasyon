package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import TCPClientSide.CommonAsyncTask;
import TCPClientSide.TCPClient;

/**
 * Created by Mustafa on 22.7.2014.
 */
public class GlobalApplication extends Application {

    CommonAsyncTask commonAsyncTask;
    SharedPreferences preferences;
    TCPClient mTcpClient;
    Activity activity;

    public enum Komutlar {
        siparis, iptal, hesapOdeniyor, masaGirilebilirMi, masaDegistir, urunTasindi, ikram, ikramIptal,
        BulunanYazicilar, giris, IndirimOnay, OdemeOnay, LoadSiparis, OdenenleriGonder, toplumesaj, departman,
        masaAcildi, masaKapandi, AdisyonNotu, IslemHatasi, dosyalar, guncellemeyiBaslat,aktarimTamamlandi;
    }

    public void connectServer(Handler myHandler,BroadcastReceiver rec) {
        try {
            commonAsyncTask = new CommonAsyncTask(activity, myHandler);
            preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            mTcpClient.SERVERIP = preferences.getString("IPAddress", "0");
            mTcpClient.SERVERPORT = Integer.parseInt(preferences.getString("Port", "13759"));
            commonAsyncTask.execute((android.os.Handler[]) null);
        } catch (Exception e) {

        }
    }
}
