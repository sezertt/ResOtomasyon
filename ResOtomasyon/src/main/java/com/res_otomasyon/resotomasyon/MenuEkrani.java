package com.res_otomasyon.resotomasyon;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import Entity.Siparis;
import ekclasslar.FileIO;
import ekclasslar.TryConnection;
import ekclasslar.UrunBilgileri;
import Entity.Employee;
import Entity.Urunler;
import HashPassword.passwordHash;
import XMLReader.ReadXML;


public class MenuEkrani extends ActionBarActivity {

    Menu menu;

    String departmanAdi, masaAdi;
    private String m_Text = "";
    ArrayList<Employee> lstEmployee;

    boolean masaKilitliMi = false, passCorrect = false, activityVisible = true;
    Context context = this;
    MenuItem item;
    ArrayList<Urunler> lstProducts;
    ArrayList<Siparis> lstOrderedProducts = new ArrayList<Siparis>();
    SharedPreferences preferences = null;
    GlobalApplication g;
    TryConnection t;
    // more efficient than HashMap for mapping integers to objects
    SparseArray<UrunBilgileri> groups = new SparseArray<UrunBilgileri>();
    MyExpandableListAdapter adapter;


    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    try {
                        try {
                            preferences = context.getSharedPreferences("KilitliMasa", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            if (!masaKilitliMi) {
                                try {
                                    masaKilitliMi = true;
                                    editor.putString("PinCode", lstEmployee.get(0).PinCode);
                                    editor.putString("Name", lstEmployee.get(0).Name);
                                    editor.putString("LastName", lstEmployee.get(0).LastName);
                                    editor.putBoolean("MasaKilitli", masaKilitliMi);
                                    editor.putString("Title", lstEmployee.get(0).Title);
                                    editor.putString("masaAdi", masaAdi);
                                    editor.putString("departmanAdi", departmanAdi);
                                    Set<String> mySet = new HashSet<String>(Arrays.asList(lstEmployee.get
                                            (0).Permissions));
                                    editor.putStringSet("Permission", mySet);
                                    editor.putString("UserName", lstEmployee.get(0).UserName);
                                    editor.apply();
                                    item.setTitle(R.string.masa_ac);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                passCorrect = passwordHash.validatePassword(m_Text, lstEmployee.get(0).PinCode);
                                if (passCorrect && masaKilitliMi) {
                                    masaKilitliMi = false;
                                    item.setTitle(R.string.masa_kilitle);
                                    editor.putBoolean("MasaKilitli", masaKilitliMi);
                                    editor.apply();
                                }
//                                if (masaKilitliMi) {
//                                    masaKilitliMi = false;
//                                    item.setTitle(R.string.masa_ac);
//                                    editor.putBoolean("MasaKilitli", masaKilitliMi);
//                                    editor.commit();
//                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case 2:
                    //Server ile bağlantı kurulup kurulmadığını kontrol etmek için gönderilen mesaj.
                    preferences = MenuEkrani.this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                    String girisKomutu = "komut=giris&nick=" + preferences.getString("TabletName", "Tablet");

                    if (g.commonAsyncTask.client != null) {
                        if (g.commonAsyncTask.client.out != null) {
                            g.commonAsyncTask.client.sendMessage(girisKomutu);
                            MenuEkrani.this.getSupportActionBar().setTitle(getString(R.string.app_name) + "(Bağlı)");

                            t.stopTimer();
                        } else {
                            hataVer();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void hataVer() {
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
        aBuilder.setTitle("Bağlantı Hatası");
        aBuilder.setMessage("Sunucuya bağlanırken bir hata ile karşılaşıldı. Lütfen tekrar deneyiniz")
                .setCancelable(false)
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MenuEkrani.this.finish();
                    }
                });
        AlertDialog alertDialog = aBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(rec, new IntentFilter("myevent"));

        if (!g.commonAsyncTask.client.mRun && !t.timerRunning) {
            t.startTimer();
        }

        groups.clear();
        lstOrderedProducts.clear();
        createData();

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
        adapter = new MyExpandableListAdapter(this, groups, this, g);
        adapter.bitmapDictionary = g.bitmapDictionary;
        listView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        activityVisible = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        g.isMenuEkraniRunning = false;
        activityVisible = false;
        if (!masaKilitliMi) {
            g.tamPorsiyon.clear();
            g.yarimPorsiyon.clear();
            g.ceyrekPorsiyon.clear();
            g.ucCeyrekPorsiyon.clear();
            g.birBucukPorsiyon.clear();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(rec);
    }

    @Override
    public void onBackPressed() {
        if (!masaKilitliMi)
            this.finish();
    }

    BroadcastReceiver rec = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //all events will be received here
            //get message
            String srvrMessage = intent.getStringExtra("message");

            if (srvrMessage != null) {
                String[] parametreler = srvrMessage.split("&");
                String[] esitlik;
                final Dictionary<String, String> collection = new Hashtable<String, String>(parametreler.length);
                for (String parametre : parametreler) {
                    esitlik = parametre.split("=");
                    if (esitlik.length == 2)
                        collection.put(esitlik[0], esitlik[1]);
                }

                String gelenkomut = collection.get("komut");
                GlobalApplication.Komutlar komut = GlobalApplication.Komutlar.valueOf(gelenkomut);

                switch (komut) {
                    case baglanti:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (activityVisible) {
                                    if (!t.timerRunning)
                                        t.startTimer();
                                    MenuEkrani.this.getSupportActionBar().setTitle(departmanAdi + " - " + masaAdi + "(Bağlantı yok)");
                                }
                            }
                        });
                        break;
                    case LoadSiparis:
                        String siparisler = collection.get("siparisBilgileri");
                        Intent hesapEkrani = new Intent(MenuEkrani.this, HesapEkrani.class);

                        hesapEkrani.putExtra("lstOrderedProducts", lstOrderedProducts); // yeni verilecek olan siparişler
                        hesapEkrani.putExtra("siparisler", siparisler); // eski siparişler
                        hesapEkrani.putExtra("DepartmanAdi", departmanAdi);
                        hesapEkrani.putExtra("MasaAdi", masaAdi);
                        hesapEkrani.putExtra("lstEmployees", lstEmployee);
                        startActivity(hesapEkrani);
                        break;
                }
            }
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        g = (GlobalApplication) getApplicationContext();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu_ekrani);
        Bundle extras = getIntent().getExtras();
        departmanAdi = extras.getString("DepartmanAdi");
        masaAdi = extras.getString("MasaAdi");
        lstEmployee = (ArrayList<Employee>) extras.getSerializable("lstEmployees");
        FileIO fileIO = new FileIO();
        List<File> files = null;

        try {
            files = fileIO.getListFiles(new File(Environment.getExternalStorageDirectory().getPath() + "/shared/Lenovo"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ReadXML readUrun = new ReadXML();
        lstProducts = readUrun.readUrunler(files);
        if (g.commonAsyncTask.client != null) {
            if (g.commonAsyncTask.client.out != null) {
                MenuEkrani.this.getSupportActionBar().setTitle(departmanAdi + " - " + masaAdi + "(Bağlı)");
            } else {
                MenuEkrani.this.getSupportActionBar().setTitle(departmanAdi + " - " + masaAdi + "(Bağlantı yok)");
            }
        }
        t = new TryConnection(g, myHandler);

        findViewById(R.id.buttonSepet).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                g.commonAsyncTask.client.sendMessage("komut=LoadSiparis&masa=" + masaAdi + "&departmanAdi=" + departmanAdi);
            }
        });
    }

    @Override
    protected void onStop() {
        g.isMenuEkraniRunning = false;
        activityVisible = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        activityVisible = false;
        g.isMenuEkraniRunning = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(rec);
        super.onDestroy();
    }

    public void createData() {
        int j = 0;

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);

        for (int i = 0; i < lstProducts.size(); i++) {
            UrunBilgileri group = new UrunBilgileri(lstProducts.get(i).urunKategorisi);
            if (i + 1 < lstProducts.size()) {
                while (lstProducts.get(i).urunKategorisi.contentEquals(lstProducts.get(i + 1).urunKategorisi)) {
                    group.productName.add(lstProducts.get(i).urunAdi);
                    group.productPrice.add(lstProducts.get(i).porsiyonFiyati);
                    group.productInfo.add(lstProducts.get(i).urunAciklamasi);
                    group.productPortion.add(lstProducts.get(i).urunPorsiyonu);

                    int siparisYeri = -1;
                    for (int k = 0; k < g.tamPorsiyon.size(); k++) {
                        if (g.tamPorsiyon.get(k).yemekAdi.contentEquals(lstProducts.get(i).urunAdi)) {
                            siparisYeri = k;
                            break;
                        }
                    }
                    if (siparisYeri != -1) {
                        group.productCount.add(g.tamPorsiyon.get(siparisYeri).miktar);

                        int siparisVarmi = -1;
                        for (int l = 0; l < lstOrderedProducts.size(); l++) {
                            if (lstOrderedProducts.get(l).yemekAdi.contentEquals(lstProducts.get(i).urunAdi)) {
                                siparisVarmi = l;
                                break;
                            }
                        }
                        if (siparisVarmi == -1) {
                            Siparis siparis = new Siparis();
                            siparis.porsiyonSinifi = lstProducts.get(i).urunPorsiyonu;
                            siparis.miktar = g.tamPorsiyon.get(siparisYeri).miktar;
                            siparis.porsiyonFiyati = lstProducts.get(i).porsiyonFiyati;
                            siparis.yemekAdi = lstProducts.get(i).urunAdi;
                            lstOrderedProducts.add(siparis);
                        } else {
                            lstOrderedProducts.get(siparisVarmi).miktar = g.tamPorsiyon.get(siparisYeri).miktar;
                        }
                    } else {
                        group.productCount.add("0");
                    }

                    if (lstProducts.get(i).urunPorsiyonu != 0) //yarım & çeyrek porsiyon
                    {
                        setLstOrderedProducts(group, df, i, g.yarimPorsiyon, 0.5);
                        setLstOrderedProducts(group, df, i, g.birBucukPorsiyon, 1.5);

                        if (lstProducts.get(i).urunPorsiyonu == 2) // çeyrek porsiyon
                        {
                            setLstOrderedProducts(group, df, i, g.ucCeyrekPorsiyon, 0.75);
                            setLstOrderedProducts(group, df, i, g.ceyrekPorsiyon, 0.25);
                        }
                    }

                    i++;
                    if (i + 1 >= lstProducts.size())
                        break;
                }
            }
            if (i + 1 < lstProducts.size()) {
                group.productName.add(lstProducts.get(i).urunAdi);
                group.productPrice.add(lstProducts.get(i).porsiyonFiyati);
                group.productInfo.add(lstProducts.get(i).urunAciklamasi);
                group.productPortion.add(lstProducts.get(i).urunPorsiyonu);

                int siparisYeri = -1;
                for (int k = 0; k < g.tamPorsiyon.size(); k++) {
                    if (g.tamPorsiyon.get(k).yemekAdi.contentEquals(lstProducts.get(i).urunAdi)) {
                        siparisYeri = k;
                        break;
                    }
                }
                if (siparisYeri != -1) {
                    group.productCount.add(g.tamPorsiyon.get(siparisYeri).miktar);

                    int siparisVarmi = -1;
                    for (int l = 0; l < lstOrderedProducts.size(); l++) {
                        if (lstOrderedProducts.get(l).yemekAdi.contentEquals(lstProducts.get(i).urunAdi)) {
                            siparisVarmi = l;
                            break;
                        }
                    }
                    if (siparisVarmi == -1) {
                        Siparis siparis = new Siparis();
                        siparis.porsiyonSinifi = lstProducts.get(i).urunPorsiyonu;
                        siparis.miktar = g.tamPorsiyon.get(siparisYeri).miktar;
                        siparis.porsiyonFiyati = lstProducts.get(i).porsiyonFiyati;
                        siparis.yemekAdi = lstProducts.get(i).urunAdi;
                        lstOrderedProducts.add(siparis);
                    } else {
                        lstOrderedProducts.get(siparisVarmi).miktar = g.tamPorsiyon.get(siparisYeri).miktar;
                    }
                } else {
                    group.productCount.add("0");
                }

                if (lstProducts.get(i).urunPorsiyonu != 0) //yarım & çeyrek porsiyon
                {
                    setLstOrderedProducts(group, df, i, g.yarimPorsiyon, 0.5);
                    setLstOrderedProducts(group, df, i, g.birBucukPorsiyon, 1.5);

                    if (lstProducts.get(i).urunPorsiyonu == 2) // çeyrek porsiyon
                    {
                        setLstOrderedProducts(group, df, i, g.ucCeyrekPorsiyon, 0.75);
                        setLstOrderedProducts(group, df, i, g.ceyrekPorsiyon, 0.25);
                    }
                }
            }
            groups.append(j, group);
            j++;
        }
    }

    private void setLstOrderedProducts(UrunBilgileri group, DecimalFormat df, int i, ArrayList<Siparis> arrayListPorsiyon, Double carpan) {
        for (Siparis anArrayListPorsiyon : arrayListPorsiyon) {
            if (anArrayListPorsiyon.yemekAdi.contentEquals(lstProducts.get(i).urunAdi)) {
                group.productCount.set(group.productCount.size() - 1, df.format(Double.parseDouble(anArrayListPorsiyon.miktar) * carpan + (Double.parseDouble(group.productCount.get(i)))));

                int siparisVarmi = -1;
                for (int l = 0; l < lstOrderedProducts.size(); l++) {
                    if (lstOrderedProducts.get(l).yemekAdi.contentEquals(lstProducts.get(i).urunAdi)) {
                        siparisVarmi = l;
                        break;
                    }
                }
                if (siparisVarmi == -1) {
                    Siparis siparis = new Siparis();
                    siparis.porsiyonSinifi = lstProducts.get(i).urunPorsiyonu;
                    siparis.miktar = carpan.toString();
                    siparis.porsiyonFiyati = lstProducts.get(i).porsiyonFiyati;
                    siparis.yemekAdi = lstProducts.get(i).urunAdi;
                    lstOrderedProducts.add(siparis);
                } else {
                    lstOrderedProducts.get(siparisVarmi).miktar = df.format(Double.parseDouble(anArrayListPorsiyon.miktar) * carpan + (Double.parseDouble(lstOrderedProducts.get(siparisVarmi).miktar)));
                }
                break;
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ekrani, menu);
        this.menu = menu;
        preferences = this.getSharedPreferences("KilitliMasa", Context.MODE_PRIVATE);
        masaKilitliMi = preferences.getBoolean("MasaKilitli", false);
//        masaKilitliMi = context.getSharedPreferences("KilitliMasa", Context.MODE_PRIVATE).getBoolean
//                ("MasaKilitli", masaKilitliMi);
        if (masaKilitliMi) {
            this.item = menu.findItem(R.id.action_lockTable);
            item.setTitle(R.string.masa_ac);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        this.item = item;
        switch (id) {
            case R.id.action_lockTable:
                if (item.getTitle().toString().contentEquals("Masayı Kilitle")) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Masa Kilitle");
                    builder.setPositiveButton("Masayı Kilitle", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            myHandler.sendEmptyMessage(0);
                        }
                    });
                    builder.setNegativeButton("Çıkış", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
//                    return false;
                } else if (item.getTitle().toString().contentEquals("Masa Kilidini Kaldır")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Masa kilitlensin mi?");

                    final EditText input = new EditText(context);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);

                    builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_Text = input.getText().toString();
                            myHandler.sendEmptyMessage(0);
                        }
                    });
                    builder.setNegativeButton("Çıkış", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
                break;

            case R.id.action_masaTemizleyin:

                break;

            case R.id.action_gorusOneri:

                break;

            case R.id.action_garsonIstiyorum:

                break;
        }

        return super.onOptionsItemSelected(item);
    }
}