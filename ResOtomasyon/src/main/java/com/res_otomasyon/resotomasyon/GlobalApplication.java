package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import Entity.MasaninSiparisleri;
import Entity.Siparis;
import TCPClientSide.CommonAsyncTask;
import TCPClientSide.TCPClient;
import ekclasslar.DepartmanMasalari;

/**
 * Created by Mustafa on 22.7.2014.
 */
public class GlobalApplication extends Application {

    public CommonAsyncTask commonAsyncTask;
    SharedPreferences preferences;
    TCPClient mTcpClient;
    Activity activity;
    public LocalBroadcastManager localBroadcastManager;
    public BroadcastReceiver broadcastReceiver;

    public ArrayList<DepartmanMasalari> secilenMasalar = new ArrayList<DepartmanMasalari>();
    public ArrayList<MasaninSiparisleri> masaninSiparisleri = new ArrayList<MasaninSiparisleri>();


    public ArrayList<Siparis> tamPorsiyon = new ArrayList<Siparis>();
    public ArrayList<Siparis> yarimPorsiyon = new ArrayList<Siparis>();
    public ArrayList<Siparis> ceyrekPorsiyon = new ArrayList<Siparis>();
    public ArrayList<Siparis> ucCeyrekPorsiyon = new ArrayList<Siparis>();
    public ArrayList<Siparis> birBucukPorsiyon = new ArrayList<Siparis>();

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
