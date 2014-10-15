package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import Entity.GlobalDepartman;
import Entity.MasaninSiparisleri;
import Entity.Siparis;
import TCPClientSide.CommonAsyncTask;
import TCPClientSide.TCPClient;
import ekclasslar.DepartmanMasalari;
import ekclasslar.NotificationExpandableAdapter;

public class GlobalApplication extends Application {

    public CommonAsyncTask commonAsyncTask;
    SharedPreferences preferences;
    Activity activity;
    public BroadcastReceiver broadcastReceiver,broadcastReceiverMenuEkrani;
    public boolean isMenuEkraniRunning = false;

    public boolean canPlayGame;

    public ArrayList<GlobalDepartman> globalDepartmanlar = new ArrayList<GlobalDepartman>();

    public ArrayList<DepartmanMasalari> secilenMasalar = new ArrayList<DepartmanMasalari>();

    public ArrayList<Siparis> siparisListesi = new ArrayList<Siparis>();

    Dictionary<String,Bitmap> bitmapDictionary = new Hashtable<String, Bitmap>();
    public ArrayList<MasaninSiparisleri> lstMasaninSiparisleri = new ArrayList<MasaninSiparisleri>();

    NotificationExpandableAdapter adapter;

    public boolean baglantiVarMi= false;

    public enum Komutlar {
        Default, OdemeBilgileriTablet, siparis, iptal, hesapOdeniyor, masaGirilebilirMi, masaDegistir, urunTasindi, ikram, ikramIptal,
        BulunanYazicilar, giris, IndirimOnay, OdemeOnay, LoadSiparis, OdenenleriGonder, departman,masaAcildi, masaKapandi, AdisyonNotu,
        IslemHatasi, dosyalar, guncellemeyiBaslat, aktarimTamamlandi, baglanti, modemBilgileri, bildirim, bildirimBilgileri, toplumesaj,
        GarsonIstendi, TemizlikIstendi, HesapIstendi, bildirimGoruldu, GarsonGoruldu, HesapGoruldu, TemizlikGoruldu, hesapGeliyor,
        hesapIslemde, departmanMasaSecimiIcin, urunuTasiTablet, departmanMasaTasimaIcin, OdemeBilgileriGuncelleTablet, masaDegistirTablet,
        OdemeIndirimOnayTablet, siparisListesineGeriEkle, masaIslemde, masaGeliyor, anketIstegi
    }

    public void connectServer(Handler myHandler) throws Exception {
        preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        TCPClient.SERVERIP = preferences.getString("IPAddress", "0");
        TCPClient.SERVERPORT = Integer.parseInt(preferences.getString("Port", "13759"));
        commonAsyncTask = (CommonAsyncTask) new CommonAsyncTask(activity,myHandler,this).execute((Handler[]) null);
    }

    public Dictionary<String, Bitmap> getImages() {
        Dictionary<String, Bitmap> bmpCollection = new Hashtable<String, Bitmap>();
        try {
            File filesDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/shared/Lenovo/Resimler/");
            File[] files = filesDirectory.listFiles();
            for (File file : files) {
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inPurgeable = true;
                bmOptions.inSampleSize = 2;
                Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions);
                String key = file.getName();
                key = key.substring(0, key.length() - 4);
                bmpCollection.put(key, bmp);
            }
            return bmpCollection;
        } catch (Exception ex) {
            return null;
        }
    }
}
