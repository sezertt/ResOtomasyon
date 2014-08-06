package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ExpandableListView;

import java.io.File;
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
import TCPClientSide.ConnectTCP;
import TCPClientSide.TCPClient;
import XMLReader.ReadXML;


public class MenuEkrani extends Activity {

    Menu menu;

    String departmanAdi, masaAdi;
    private String m_Text = "";
    ArrayList<Employee> lstEmployee;

    boolean masaKilitliMi = false;
    boolean passCorrect = false;
    boolean activityVisible = true;
    Context context = this;
    MenuItem item;

    ArrayList<Urunler> lstProducts;
    ArrayList<Siparis> lstOrderedProducts = new ArrayList<Siparis>();
    SharedPreferences preferences = null;
    GlobalApplication g;
    TryConnection t;
    // more efficient than HashMap for mapping integers to objects
    SparseArray<UrunBilgileri> groups = new SparseArray<UrunBilgileri>();

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
                                    editor.commit();
                                    item.setTitle(R.string.masa_ac);
                                } catch (Exception e) {

                                }
                            } else {
                                passCorrect = passwordHash.validatePassword(m_Text, lstEmployee.get(0).PinCode);
                                if (passCorrect && masaKilitliMi) {
                                    masaKilitliMi = false;
                                    item.setTitle(R.string.masa_kilitle);
                                    editor.putBoolean("MasaKilitli", masaKilitliMi);
                                    editor.commit();
                                }
//                                if (masaKilitliMi) {
//                                    masaKilitliMi = false;
//                                    item.setTitle(R.string.masa_ac);
//                                    editor.putBoolean("MasaKilitli", masaKilitliMi);
//                                    editor.commit();
//                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (Exception ex) {


                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        if(!g.commonAsyncTask.client.mRun && !t.timerRunning)
        {
            t.startTimer();
        }
        activityVisible = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityVisible = false;
        if (!masaKilitliMi) {
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (masaKilitliMi)
            return;
        else
            this.finish();
    }

    //Tekrar bağlan durumunda.
    public Handler handlerTekrarBaglan = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    //Server ile bağlantı kurulup kurulmadığını kontrol etmek için gönderilen mesaj.
                    preferences = MenuEkrani.this.getSharedPreferences("MyPreferences",
                            Context.MODE_PRIVATE);
                    String girisKomutu = "<komut=giris&nick=" + preferences.getString("TabletName", "Tablet") + ">";
                    if (g.commonAsyncTask.client != null) {
                        if (g.commonAsyncTask.client.out != null) {
                            g.commonAsyncTask.client.sendMessage(girisKomutu);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

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
                                        getActionBar().setTitle(getString(R.string.app_name) + "(Bağlantı yok)");
                                        EditText e = (EditText) findViewById(R.id.editTextPin);
                                        e.setFocusable(false);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        g = (GlobalApplication) getApplicationContext();
        setContentView(R.layout.activity_menu_ekrani);
        Bundle extras = getIntent().getExtras();
        departmanAdi = extras.getString("DepartmanAdi");
        masaAdi = extras.getString("MasaAdi");
        lstEmployee = (ArrayList<Employee>) extras.getSerializable("lstEmployees");
        FileIO fileIO = new FileIO();
        List<File> files = null;

        try {
            files = fileIO.getListFiles(new File("/mnt/sdcard/shared/Lenovo"));
        } catch (Exception ex) {
        }

        ReadXML readUrun = new ReadXML();
        lstProducts = readUrun.readUrunler(files);
        LocalBroadcastManager.getInstance(this).registerReceiver(rec, new IntentFilter("myevent"));

        createData();
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
        MyExpandableListAdapter adapter = new MyExpandableListAdapter(this, groups, this);
        listView.setAdapter(adapter);
        if (g.commonAsyncTask.client != null) {
            if (g.commonAsyncTask.client.out != null) {
                getActionBar().setTitle(getString(R.string.app_name) + "(Bağlı)");
            } else {
                getActionBar().setTitle(getString(R.string.app_name) + "(Bağlantı yok)");

            }
        }
        t = new TryConnection(g, myHandler);
    }

    @Override
    protected void onStop() {
        activityVisible = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        activityVisible = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(rec);
        super.onDestroy();
    }

    public void createData() {
        int j = 0;
        for (int i = 0; i < lstProducts.size(); i++) {
            UrunBilgileri group = new UrunBilgileri(lstProducts.get(i).urunKategorisi);
            if (i + 1 < lstProducts.size()) {
                while (lstProducts.get(i).urunKategorisi.contentEquals(lstProducts.get(i + 1).urunKategorisi)) {
                    group.productName.add(lstProducts.get(i).urunAdi);
                    group.productPrice.add(lstProducts.get(i).porsiyonFiyati);
                    group.productInfo.add(lstProducts.get(i).urunAciklamasi);
                    group.productPortion.add(lstProducts.get(i).urunPorsiyonu);
                    group.productCount.add("0");

                    i++;
                    if (i + 1 >= lstProducts.size())
                        break;
                }
            }
            if(i+1<lstProducts.size())
            {
                group.productName.add(lstProducts.get(i).urunAdi);
                group.productPrice.add(lstProducts.get(i).porsiyonFiyati);
                group.productInfo.add(lstProducts.get(i).urunAciklamasi);
                group.productPortion.add(lstProducts.get(i).urunPorsiyonu);
                group.productCount.add("0");
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
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
//                    return false;
                } else if (item.getTitle().toString().contentEquals("Masa Kilidini Kaldır")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Title");

                    final EditText input = new EditText(context);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_Text = input.getText().toString();
                            myHandler.sendEmptyMessage(0);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
                break;

            case R.id.action_hesap:
                g.commonAsyncTask.client.sendMessage("<komut=LoadSiparis&masa=" + masaAdi + "&departmanAdi=" +
                        departmanAdi + ">");
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