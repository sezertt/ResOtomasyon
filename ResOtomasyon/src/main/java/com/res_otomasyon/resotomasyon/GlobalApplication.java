package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import java.util.ArrayList;
import Entity.MasaninSiparisleri;
import Entity.Siparis;
import TCPClientSide.CommonAsyncTask;
import TCPClientSide.TCPClient;
import ekclasslar.DepartmanMasalari;

public class GlobalApplication extends Application {

    public CommonAsyncTask commonAsyncTask;
    SharedPreferences preferences;
    Activity activity;
    public BroadcastReceiver broadcastReceiver;
    public boolean isMenuEkraniRunning = false;

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
        masaAcildi, masaKapandi, AdisyonNotu, IslemHatasi, dosyalar, guncellemeyiBaslat, aktarimTamamlandi, baglanti, modemBilgileri
    }

    public void connectServer(Handler myHandler) throws Exception {
        preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        TCPClient.SERVERIP = preferences.getString("IPAddress", "0");
        TCPClient.SERVERPORT = Integer.parseInt(preferences.getString("Port", "13759"));
        commonAsyncTask = (CommonAsyncTask) new CommonAsyncTask(activity,
                myHandler).execute((Handler[]) null);
    }
}
