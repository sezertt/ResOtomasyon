package com.res_otomasyon.resotomasyon;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
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
import android.widget.ImageView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import Entity.MasaninSiparisleri;
import Entity.Siparis;
import Entity.UrunlerinListesi;
import ekclasslar.BildirimBilgileriIslemler;
import ekclasslar.FileIO;
import ekclasslar.GarsonIslemler;
import ekclasslar.HesapIste;
import ekclasslar.MasaTemizle;
import ekclasslar.SetViewGroupEnabled;
import ekclasslar.SiparisIslemler;
import ekclasslar.TryConnection;
import ekclasslar.UrunBilgileri;
import Entity.Employee;
import HashPassword.passwordHash;
import XMLReader.ReadXML;


public class MenuEkrani extends ActionBarActivity {

    Menu menu;

    String departmanAdi, masaAdi, siparisler;
    private String m_Text = "";
    Employee employee;

    boolean masaKilitliMi = false, passCorrect = false, activityVisible = true, hesapEkraniAcilicak = false;
    Context context = this;
    MenuItem item;
    ArrayList<UrunlerinListesi> urunListesi;
    SharedPreferences preferences = null;
    GlobalApplication g;
    TryConnection t;
    // more efficient than HashMap for mapping integers to objects
    SparseArray<UrunBilgileri> groups = new SparseArray<UrunBilgileri>();
    MyExpandableListAdapter adapter;
    Button buttonMasaAc;
    ImageView imageMasaAc;
    Boolean masaAcikMi;

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
                                    editor.putString("PinCode", employee.PinCode);
                                    editor.putString("Name", employee.Name);
                                    editor.putString("LastName", employee.LastName);
                                    editor.putBoolean("MasaKilitli", masaKilitliMi);
                                    editor.putString("Title", employee.Title);
                                    editor.putString("masaAdi", masaAdi);
                                    editor.putString("departmanAdi", departmanAdi);
                                    Set<String> mySet = new HashSet<String>(Arrays.asList(employee.Permissions));
                                    editor.putStringSet("Permission", mySet);
                                    editor.putString("UserName", employee.UserName);
                                    editor.apply();
                                    item.setTitle(R.string.masa_ac);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                passCorrect = passwordHash.validatePassword(m_Text, employee.PinCode);
                                if (passCorrect && masaKilitliMi) {
                                    masaKilitliMi = false;
                                    buttonMasaAc.setVisibility(View.INVISIBLE);
                                    imageMasaAc.setVisibility(View.INVISIBLE);
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
                            MenuEkrani.this.getSupportActionBar().setTitle(departmanAdi + " - " + masaAdi + "(Bağlı)");

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
        hesapEkraniAcilicak = false;

        if (g == null) {
            g = (GlobalApplication) getApplicationContext();
            g.isServerReachable = false;
        }
        if (t == null)
            t = new TryConnection(g, myHandler);

        g.isServerReachable = g.commonAsyncTask != null && g.commonAsyncTask.client != null && g.commonAsyncTask.client.mRun;

        if (!g.isServerReachable && !t.timerRunning) {
            t.startTimer();
        }

        groups.clear();
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
        if (!masaKilitliMi && !hesapEkraniAcilicak) {
            g.siparisListesi.clear();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(g.broadcastReceiverMenuEkrani);
        }
    }

    @Override
    public void onBackPressed() {
        if (!masaKilitliMi)
        {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(g.broadcastReceiverMenuEkrani);
            this.finish();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu_ekrani);


        try {
            g = (GlobalApplication) getApplicationContext();

            if (g.broadcastReceiverMenuEkrani != null) {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(g.broadcastReceiverMenuEkrani);
            }
            g.broadcastReceiverMenuEkrani = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String srvrMessage = intent.getStringExtra("message");
//                    srvrMessage = "komut=garsonIstendi&departmanAdi=Departman&masa=RP20";
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
                            siparisler = collection.get("siparisBilgileri");
                            g.commonAsyncTask.client.sendMessage("komut=OdemeBilgileriTablet&masa=" + masaAdi + "&departmanAdi=" + departmanAdi);
                            break;
                        case OdemeBilgileriTablet:
                            Intent hesapEkrani = new Intent(MenuEkrani.this, HesapEkrani.class);
                            hesapEkrani.putExtra("siparisler", siparisler); // eski siparişler
                            hesapEkrani.putExtra("DepartmanAdi", departmanAdi);
                            hesapEkrani.putExtra("MasaAdi", masaAdi);
                            hesapEkrani.putExtra("Employee", employee);
                            hesapEkrani.putExtra("alinanOdemeler", collection.get("alinanOdemeler"));
                            hesapEkrani.putExtra("indirimler", collection.get("indirimler"));
                            hesapEkraniAcilicak = true;
                            startActivity(hesapEkrani);
                            break;
                        case masaKapandi:
                            g.siparisListesi.clear();
                            if(masaKilitliMi)
                            {
                                groups.clear();
                                createData();
                                adapter.notifyDataSetChanged();
                                //resim ve buton visible yapılacak
                                buttonMasaAc.setVisibility(View.VISIBLE);
                                imageMasaAc.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                onBackPressed();
                            }
                            break;
                        case masaAcildi:
                            //resim ve button invisible yapılacak
                            buttonMasaAc.setVisibility(View.INVISIBLE);
                            imageMasaAc.setVisibility(View.INVISIBLE);

                        default:
                            break;
                    }

                }
            };
            LocalBroadcastManager.getInstance(context).registerReceiver(g.broadcastReceiverMenuEkrani, new IntentFilter("myevent"));
        } catch (Exception ignored) {

        }


        Bundle extras = getIntent().getExtras();
        departmanAdi = extras.getString("DepartmanAdi");
        masaAdi = extras.getString("MasaAdi");
        employee = (Employee) extras.getSerializable("Employee");
        masaAcikMi = extras.getBoolean("MasaAcikMi");

        FileIO fileIO = new FileIO();
        List<File> files = null;

        preferences = this.getSharedPreferences("KilitliMasa", Context.MODE_PRIVATE);
        masaKilitliMi = preferences.getBoolean("MasaKilitli", false);

        buttonMasaAc = (Button) findViewById(R.id.buttonMasaAc);
        imageMasaAc = (ImageView) findViewById(R.id.imageView);

        if(masaKilitliMi && !masaAcikMi)
        {
            buttonMasaAc.setVisibility(View.VISIBLE);
            imageMasaAc.setVisibility(View.VISIBLE);
        }

        buttonMasaAc.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                buttonMasaAc.setVisibility(View.INVISIBLE);
                imageMasaAc.setVisibility(View.INVISIBLE);
                g.commonAsyncTask.client.sendMessage("komut=masaAcildi&masa=" + masaAdi + "&departmanAdi=" + departmanAdi);
                g.commonAsyncTask.client.sendMessage("komut=masayiAc&masa=" + masaAdi + "&departmanAdi=" + departmanAdi);
            }
        });

        try {
            files = fileIO.getListFiles(new File(Environment.getExternalStorageDirectory().getPath() + "/shared/Lenovo"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ReadXML readUrun = new ReadXML();
        urunListesi = readUrun.readUrunler(files);
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
        if (!masaKilitliMi && !hesapEkraniAcilicak) {
            g.siparisListesi.clear();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(g.broadcastReceiverMenuEkrani);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        activityVisible = false;
        g.isMenuEkraniRunning = false;
        if (!masaKilitliMi && !hesapEkraniAcilicak) {
            g.siparisListesi.clear();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(g.broadcastReceiverMenuEkrani);
        super.onDestroy();
    }

    public void createData() {
        int j = 0;

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);

        for (int i = 0; i < urunListesi.size(); i++) {
            UrunBilgileri group = new UrunBilgileri(urunListesi.get(i).urunKategorisi);
            if (i + 1 < urunListesi.size()) {
                while (urunListesi.get(i).urunKategorisi.contentEquals(urunListesi.get(i + 1).urunKategorisi)) // bir sonraki ürünün kategorisi, eklenen ürünün kategorisi ile aynı olduğu sürece devam et
                {
                    group.productName.add(urunListesi.get(i).urunAdi);
                    group.productPrice.add(urunListesi.get(i).urunFiyati);
                    group.productInfo.add(urunListesi.get(i).urunAciklamasi);
                    group.productPortionClass.add(urunListesi.get(i).urunPorsiyonSinifi);

                    String miktar = "0";
                    for (int k = 0; k < g.siparisListesi.size(); k++) {
                        if (g.siparisListesi.get(k).siparisYemekAdi.contentEquals(urunListesi.get(i).urunAdi)) {
                            miktar = df.format(Double.parseDouble(miktar) + g.siparisListesi.get(k).siparisAdedi * g.siparisListesi.get(k).siparisPorsiyonu);
                        }
                    }

                    group.productCount.add(miktar);

                    i++;
                    if (i + 1 >= urunListesi.size())
                        break;
                }
            }
            // bir sonraki ürünün kategorisi, eklenen ürünün kategorisinden farklı ise kategori değişiyor demektir. o zaman bu ürün kategorisinin son ürünüdür. ekliyoruz.
            if (i + 1 < urunListesi.size()) {
                group.productName.add(urunListesi.get(i).urunAdi);
                group.productPrice.add(urunListesi.get(i).urunFiyati);
                group.productInfo.add(urunListesi.get(i).urunAciklamasi);
                group.productPortionClass.add(urunListesi.get(i).urunPorsiyonSinifi);

                String miktar = "0";
                for (int k = 0; k < g.siparisListesi.size(); k++) {
                    if (g.siparisListesi.get(k).siparisYemekAdi.contentEquals(urunListesi.get(i).urunAdi)) {
                        miktar = df.format(Double.parseDouble(miktar) + g.siparisListesi.get(k).siparisAdedi * g.siparisListesi.get(k).siparisPorsiyonu);
                    }
                }

                group.productCount.add(miktar);
            }
            groups.append(j, group);
            j++;
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
        String komut;
        switch (id) {
            case R.id.action_lockTable:
                if (item.getTitle().toString().contentEquals("Masayı Kilitle")) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Masayı Kilitle");
                    builder.setPositiveButton("Masayı Kilitle", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            myHandler.sendEmptyMessage(0);
                            if(!masaAcikMi)
                            {
                                buttonMasaAc.setVisibility(View.VISIBLE);
                                imageMasaAc.setVisibility(View.VISIBLE);
                            }
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
                    builder.setTitle("Masa kilidini kaldır");

                    final EditText input = new EditText(context);
                    input.setHint("Şifre");
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
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
                komut = "komut=TemizlikIstendi&departmanAdi=" + departmanAdi + "&masa=" + masaAdi;
                g.commonAsyncTask.client.sendMessage(komut);
                break;

            case R.id.action_gorusOneri:

                break;

            case R.id.action_garsonIstiyorum:
                komut = "komut=GarsonIstendi&departmanAdi=" + departmanAdi + "&masa=" + masaAdi;
                g.commonAsyncTask.client.sendMessage(komut);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}