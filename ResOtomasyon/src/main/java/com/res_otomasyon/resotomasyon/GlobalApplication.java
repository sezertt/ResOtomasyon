package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import TCPClientSide.CommonAsyncTask;
import TCPClientSide.TCPClient;

/**
 * Created by Mustafa on 22.7.2014.
 */
public class GlobalApplication extends Application {

    public CommonAsyncTask commonAsyncTask;
    SharedPreferences preferences;
    TCPClient mTcpClient;
    Activity activity;

    public enum Komutlar {
        Default,siparis, iptal, hesapOdeniyor, masaGirilebilirMi, masaDegistir, urunTasindi, ikram, ikramIptal,
        BulunanYazicilar, giris, IndirimOnay, OdemeOnay, LoadSiparis, OdenenleriGonder, toplumesaj, departman,
        masaAcildi, masaKapandi, AdisyonNotu, IslemHatasi, dosyalar, guncellemeyiBaslat,aktarimTamamlandi, baglanti;
    }

    public void connectServer(Handler myHandler) {
        try {
            preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            mTcpClient.SERVERIP = preferences.getString("IPAddress", "0");
            mTcpClient.SERVERPORT = Integer.parseInt(preferences.getString("Port", "13759"));
            commonAsyncTask = (CommonAsyncTask) new CommonAsyncTask(activity,
                    myHandler).execute((Handler[]) null);
        } catch (Exception e) { }
    }
}
