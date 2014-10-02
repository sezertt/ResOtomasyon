package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import Entity.Departman;
import Entity.GlobalDepartman;
import Entity.GlobalMasalar;
import Entity.MasaDizayn;
import TCPClientSide.CommonAsyncTask;
import XMLReader.ReadXML;
import ekclasslar.FileIO;

public class StartScreen extends Activity implements CommonAsyncTask.OnAsyncRequestComplete {

    SharedPreferences preferences;
    GlobalApplication g;
    ArrayList<Departman> lstDepartmanlar;
    ArrayList<MasaDizayn> lstMasaDizayn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.setVisible(false);
        String sdcardReady = Environment.getExternalStorageState();

        while (!sdcardReady.contentEquals("mounted")) {
            sdcardReady = Environment.getExternalStorageState();
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        g = (GlobalApplication) getApplicationContext();
        preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        if (preferences == null) {
            Intent intent = new Intent(StartScreen.this, Settings.class);
            startActivity(intent);
        } else {
            if (g.commonAsyncTask == null) {
                g.activity = this;
                try {
                    g.connectServer(myHandler);
                } catch (Exception ignored) {
                }
            }
        }
        File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/shared/Lenovo");
        folder.mkdirs();
        if (g.bitmapDictionary == null)
            g.bitmapDictionary = new Hashtable<String, Bitmap>();
        if (g.bitmapDictionary.size() == 0)
            g.bitmapDictionary = g.getImages();

        g.canPlayGame = preferences.getBoolean("canPlayGame", false);
        FileIO fileIO = new FileIO();
        List<File> files;
        files = fileIO.getListFiles(new File(Environment.getExternalStorageDirectory().getPath() + "/shared/Lenovo"));
        ReadXML readXML = new ReadXML();
        lstDepartmanlar = readXML.readDepartmanlar(files);
        lstMasaDizayn = readXML.readMasaDizayn(files);
        if (g.globalDepartmanlar.size() > 0)
            g.globalDepartmanlar.clear();
        for (Departman aLstDepartmanlar : lstDepartmanlar) {
            GlobalDepartman departman = new GlobalDepartman();
            departman.globalDepartmanAdi = aLstDepartmanlar.DepartmanAdi;
            departman.globalMasalar = new ArrayList<GlobalMasalar>();

            for (MasaDizayn aLstMasaDizayn : lstMasaDizayn) {
                if (aLstMasaDizayn.MasaEkraniAdi.contentEquals(aLstDepartmanlar.DepartmanEkrani)) {
                    GlobalMasalar masa = new GlobalMasalar();
                    masa.globalMasaAdi = aLstMasaDizayn.MasaAdi;
                    masa.globalMasaAcikMi = false;
                    departman.globalMasalar.add(masa);
                } else {
                    if (departman.globalMasalar.size() > 0)
                        break;
                }
            }
            g.globalDepartmanlar.add(departman);
        }
    }

    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    //Server ile bağlantı kurulup kurulmadığını kontrol etmek için gönderilen mesaj.
                    String girisKomutu = "komut=giris&nick=" + preferences.getString("TabletName", "Tablet");
                    if (g.commonAsyncTask.client != null) {
                        if (g.commonAsyncTask.client.out != null)
                            g.commonAsyncTask.client.sendMessage(girisKomutu);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start_screen, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setVisible(false);
//        Intent intent = new Intent(getApplicationContext(), MasaSecEkrani.class);
        Intent intent = new Intent(getApplicationContext(), LoginScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
         */
    }

    @Override
    public void asyncResponse(String mesaj) {

    }
}