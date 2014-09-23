package com.res_otomasyon.resotomasyon;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import Entity.Employee;
import Entity.Siparis;
import Entity.UrunTasimaListesi;
import ekclasslar.SetViewGroupEnabled;

public class HesapEkrani extends ActionBarActivity {

    String departmanAdi, masaAdi, aktarmaBilgileri = "";
    Employee employee;
    SharedPreferences preferences = null;
    boolean masaKilitliMi = false,masaAcikMi;
    MyListAdapter adapterSecilenSiparisler, adapterHesap;
    UrunTasimaListAdapter adapterUrunTasima;
    ArrayList<Siparis> urunListesiToplam = new ArrayList<Siparis>();
    int selectedSiparisItemPosition = -1,gelenDepartman = 0;
    GlobalApplication g;
    ArrayList<String> porsiyonlarPozitif = new ArrayList<String>();
    ArrayList<UrunTasimaListesi> urunTasimaListesi = new ArrayList<UrunTasimaListesi>();
    Integer kacAdet;
    Double  yemeginFiyati, toplamHesap = 0d;
    Button buttonSepet,hesapButton;
    Boolean ikramMi;
    TasimaIcinMasaSecimiExpandableListAdapter tasimaIcinMasaExpandableListAdapter;

    AlertDialog builder,alertDialog,alertDialog2, urunTasimaIcinMasaSecimiAlertDialog,urunTasimaAlertDialog;
    AlertDialog.Builder aBuilder;

    boolean timerRunning = false;
    boolean activityVisible = true;
    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if(timerRunning)
                    {
                        timerRunning = false;
                        progressDialog.dismiss();
                        AlertDialog.Builder aBuilder = new AlertDialog.Builder(HesapEkrani.this);
                        aBuilder.setTitle("Tekrar Hesap İste");
                        aBuilder.setMessage("Hesap istedikten sonra sipariş veremezsiniz. Hesabı istediğinize emin misiniz?")
                                .setCancelable(false)
                                .setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String komut = "komut=HesapIstendi&departmanAdi=" + departmanAdi + "&masa=" + masaAdi + "&kalanHesap=" + toplamHesap;
                                        g.commonAsyncTask.client.sendMessage(komut);
                                        SetViewGroupEnabled.setViewGroupEnabled((ViewGroup) findViewById(R.id.hesapEkrani), false);
                                        callHesapIste();
                                        progressDialog = ProgressDialog.show(HesapEkrani.this, "Hesap isteği iletildi", "Hesap bekleniyor...", false);
                                    }
                                }).setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                            stopHesapIste();
                                SetViewGroupEnabled.setViewGroupEnabled((ViewGroup) findViewById(R.id.hesapEkrani), true);
                            }
                        });
                        AlertDialog alertDialog = aBuilder.create();
                        alertDialog.show();
                    }
                    break;
					case 2:
                    if (g.commonAsyncTask.client == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder aBuilder = new AlertDialog.Builder(HesapEkrani.this);
                                    aBuilder.setTitle("Bağlantı Sorunu")
                                        .setMessage("Sunucu bağlantısı kesildi, lütfen bekleyiniz")
                                        .setCancelable(false)
                                        .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int item) {
                                                HesapEkrani.this.finish();
                                            }
                                        })
                                        .create();
                                alertDialog2 = aBuilder.create();
                                alertDialog2.show();
                            }
                        });
                        if (g.commonAsyncTask.client.out == null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder aBuilder = new AlertDialog.Builder(HesapEkrani.this);
                                    aBuilder.setTitle("Bağlantı Sorunu")
                                            .setMessage("Sunucu bağlantısı kesildi, lütfen bekleyiniz")
                                            .setCancelable(false)
                                            .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int item) {
                                                    HesapEkrani.this.finish();
                                                }
                                            })
                                            .create();
                                    alertDialog2 = aBuilder.create();
                                    alertDialog2.show();
                                }
                            });
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    Timer timer = new Timer();
    ProgressDialog progressDialog;

    class hesapIsteTimer extends TimerTask {
        @Override
        public void run() {
            myHandler.sendEmptyMessage(1);
        }
    }


    void callHesapIste() {
        timer.schedule(new hesapIsteTimer(), 5000);
        timerRunning = true;
    }

    @Override
    public void onBackPressed() {
        if (!timerRunning)
            super.onBackPressed();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_hesap_ekrani);

        preferences = this.getSharedPreferences("KilitliMasa", Context.MODE_PRIVATE);

        masaKilitliMi = preferences.getBoolean("MasaKilitli", false);


        invalidateOptionsMenu();

        Bundle extras = getIntent().getExtras();
        departmanAdi = extras.getString("DepartmanAdi");
        departmanAdi = extras.getString("DepartmanAdi");
        masaAdi = extras.getString("MasaAdi");
        employee = (Employee) extras.getSerializable("Employee");
        masaAcikMi = extras.getBoolean("MasaAcikMi");

        g = (GlobalApplication) getApplicationContext();

        String yemeginAdi;
        Boolean ikramMi;
        Double porsiyon = 1d;

        ListView hesapListesi = (ListView) findViewById(R.id.listViewHesap);
        final ListView secilenSiparislerinListesi = (ListView) findViewById(R.id.listViewSecilenSiparisler);

        final ArrayList<Siparis> urunListesi = new ArrayList<Siparis>();
        ArrayList<Siparis> urunListesiIkram = new ArrayList<Siparis>();

        hesapButton = (Button) findViewById(R.id.buttonHesap);
        Button buttonAzalt = (Button) findViewById(R.id.buttonAzalt);
        Button buttonArttir = (Button) findViewById(R.id.buttonArttir);
        buttonSepet = (Button) findViewById(R.id.buttonSepetiOnayla);

        final String hesaptakiSiparisler = extras.getString("siparisler"); // hesaptaki siparisler

        if(hesaptakiSiparisler != null)
        {
            String[] hesabinSiparisleri = hesaptakiSiparisler.split("\\*");

            for (String aSiparisler : hesabinSiparisleri) {
                String[] hesaptakiSiparis = aSiparisler.split("-");

                NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
                Number numberFiyat = null, numberPorsiyon = null;

                //burası virgüllü stringi double a convert etme çevirme kısmı
                try {
                    numberFiyat = format.parse(hesaptakiSiparis[0]);
                    numberPorsiyon = format.parse(hesaptakiSiparis[5]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (numberFiyat != null)
                    yemeginFiyati = numberFiyat.doubleValue();

                if (numberPorsiyon != null)
                    porsiyon = numberPorsiyon.doubleValue();

                kacAdet = Integer.parseInt(hesaptakiSiparis[1]);

                yemeginAdi = hesaptakiSiparis[2];
                ikramMi = Boolean.parseBoolean(hesaptakiSiparis[3]);

                int gruptaYeniGelenSiparisVarMi = -1; // ürün hesapta bulunuyor mu ?

                if (!ikramMi) // ikram değilse
                {
                    for (int j = 0; j < urunListesi.size(); j++) {
                        if (yemeginAdi.contentEquals(urunListesi.get(j).siparisYemekAdi) && porsiyon == urunListesi.get(j).siparisPorsiyonu) // listede yemek var
                        {
                            gruptaYeniGelenSiparisVarMi = j;
                            break;
                        }
                    }

                    if (gruptaYeniGelenSiparisVarMi != -1) // listede yemek var
                    {
                        urunListesi.get(gruptaYeniGelenSiparisVarMi).siparisAdedi += kacAdet;
                    } else // listede yemek yok
                    {
                        Siparis yeniSiparis = new Siparis();
                        yeniSiparis.siparisAdedi = kacAdet;
                        yeniSiparis.siparisYemekAdi = yemeginAdi;
                        yeniSiparis.siparisFiyati = String.format("%.2f", yemeginFiyati);
                        yeniSiparis.siparisPorsiyonu = porsiyon;
                        urunListesi.add(yeniSiparis);
                    }
                    toplamHesap += yemeginFiyati * kacAdet;
                }
                else // ikramsa
                {
                    for (int j = 0; j < urunListesiIkram.size(); j++) {
                        if (yemeginAdi.contentEquals(urunListesiIkram.get(j).siparisYemekAdi) && porsiyon == urunListesiIkram.get(j).siparisPorsiyonu) // listede yemek var
                        {
                            gruptaYeniGelenSiparisVarMi = j;
                            break;
                        }
                    }

                    if (gruptaYeniGelenSiparisVarMi != -1) // listede yemek var
                    {
                        urunListesiIkram.get(gruptaYeniGelenSiparisVarMi).siparisAdedi += kacAdet;
                    } else // listede yemek yok
                    {
                        Siparis yeniSiparis = new Siparis();
                        yeniSiparis.siparisAdedi = kacAdet;
                        yeniSiparis.siparisYemekAdi = yemeginAdi;
                        yeniSiparis.siparisFiyati = "ikram";
                        yeniSiparis.siparisPorsiyonSinifi = yemeginFiyati;
                        yeniSiparis.siparisPorsiyonu = porsiyon;
                        urunListesiIkram.add(yeniSiparis);
                    }
                }
            }
            urunListesiToplam.addAll(urunListesi);
            urunListesiToplam.addAll(urunListesiIkram);

            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
            Number indirimNumber = null, odemelerNumber = null;

            Double indirimler = 0d, alinanOdemeler = 0d;
            try {
                indirimNumber = format.parse(extras.getString("indirimler"));
            } catch (Exception ignored) {
            }

            try {
                odemelerNumber = format.parse(extras.getString("alinanOdemeler"));
            } catch (Exception ignored) { }

            if (indirimNumber != null)
                indirimler -= indirimNumber.doubleValue();

            if (odemelerNumber != null)
                alinanOdemeler -= odemelerNumber.doubleValue();

            if(indirimler != 0)
            {
                Siparis indirim = new Siparis();
                indirim.siparisFiyati = indirimler.toString();
                indirim.siparisYemekAdi = "İndirim";
                urunListesiToplam.add(indirim);
            }

            if(alinanOdemeler != 0)
            {
                Siparis odemeler = new Siparis();
                odemeler.siparisFiyati = alinanOdemeler.toString();
                odemeler.siparisYemekAdi = "Ödemeler";
                urunListesiToplam.add(odemeler);
            }
            toplamHesap = toplamHesap + alinanOdemeler + indirimler;

            hesapButton.setText("Hesap İste  /  Kalan = " + String.format("%.2f", toplamHesap) + " TL");
        }
        else
        {
            hesapButton.setText("Hesap İste  /  Kalan = 0 TL");
            if(!masaAcikMi)
                hesapButton.setEnabled(false);
        }

        final EditText iptalNedeni = new EditText(HesapEkrani.this);
        iptalNedeni.setInputType(InputType.TYPE_CLASS_TEXT);
        iptalNedeni.setHint("Siparişinize düşmek istediğiniz notunuzu yazınız");

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (source.charAt(i) == '<' || source.charAt(i) == '>' || source.charAt(i) == '&' || source.charAt(i) == '=' || source.charAt(i) == '*' || source.charAt(i) == '-') {
                        return "";
                    }
                }
                return null;
            }
        };

        hesapButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // BURASI YAPILACAK HESAP İSTEME BUTONU
                AlertDialog.Builder aBuilder = new AlertDialog.Builder(HesapEkrani.this);
                aBuilder.setTitle("Hesap İste");
                aBuilder.setMessage("Hesap istedikten sonra sipariş veremezsiniz. Hesabı istediğinize emin misiniz?")
                        .setCancelable(false)
                        .setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String komut = "komut=HesapIstendi&departmanAdi=" + departmanAdi + "&masa=" + masaAdi + "&kalanHesap=" + toplamHesap;
                                g.commonAsyncTask.client.sendMessage(komut);
                                SetViewGroupEnabled.setViewGroupEnabled((ViewGroup) findViewById(R.id.hesapEkrani), false);
                                callHesapIste();
                                progressDialog = ProgressDialog.show(HesapEkrani.this, "Hesap isteği iletildi",
                                        "Hesap bekleniyor...", false);
                            }
                        }).setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = aBuilder.create();
                alertDialog.show();
            }
        });

        iptalNedeni.setFilters(new InputFilter[]{new InputFilter.LengthFilter(250),filter});

        AlertDialog.Builder siparisOnayAlertBuilder = new AlertDialog.Builder(HesapEkrani.this);
        siparisOnayAlertBuilder.setTitle("Sipariş Onayı");
        siparisOnayAlertBuilder.setMessage("Girdiğiniz siparişleri onaylıyor musunuz?")
                .setView(iptalNedeni)
                .setCancelable(false);

        siparisOnayAlertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.cancel();
                alertDialog.dismiss();
            }
        });
        siparisOnayAlertBuilder.setPositiveButton("Onayla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (urunListesiToplam.size() < 1 && g.siparisListesi.size() > 0) {
                    g.commonAsyncTask.client.sendMessage("komut=masaAcildi&masa=" + masaAdi + "&departmanAdi=" + departmanAdi);
                }

                int sonSiparisMi = g.siparisListesi.size(), siparisSayisi = g.siparisListesi.size();

                for (int i = 0; i < siparisSayisi; i++) {
                    sonSiparisMi--;
                    if (masaKilitliMi) {
                        g.commonAsyncTask.client.sendMessage("komut=siparis&masa=" + masaAdi + "&departmanAdi=" + departmanAdi + "&miktar=" + g.siparisListesi.get(0).siparisAdedi + "&yemekAdi=" + g.siparisListesi.get(0).siparisYemekAdi + "&siparisiGirenKisi=Müşteri&dusulecekDeger=" + g.siparisListesi.get(0).siparisFiyati + "&adisyonNotu=" + iptalNedeni.getText() + "&sonSiparisMi=" + sonSiparisMi + "&porsiyon=" + new DecimalFormat("#.##").format(g.siparisListesi.get(0).siparisPorsiyonu) + "&porsiyonSinifi="+ g.siparisListesi.get(0).siparisPorsiyonSinifi);
                    } else {
                        g.commonAsyncTask.client.sendMessage("komut=siparis&masa=" + masaAdi + "&departmanAdi=" + departmanAdi + "&miktar=" + g.siparisListesi.get(0).siparisAdedi + "&yemekAdi=" + g.siparisListesi.get(0).siparisYemekAdi + "&siparisiGirenKisi=" + employee.Name + " " + employee.LastName + "&dusulecekDeger=" + g.siparisListesi.get(0).siparisFiyati + "&adisyonNotu=" + iptalNedeni.getText() + "&sonSiparisMi=" + sonSiparisMi + "&porsiyon=" + new DecimalFormat("#.##").format(g.siparisListesi.get(0).siparisPorsiyonu) + "&porsiyonSinifi="+ g.siparisListesi.get(0).siparisPorsiyonSinifi);
                    }
                    g.siparisListesi.remove(g.siparisListesi.get(0));
                }
                adapterSecilenSiparisler.notifyDataSetChanged();
            }
        });
        alertDialog = siparisOnayAlertBuilder.create();

        buttonSepet.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                ListView listViewSecilenSiparisler = (ListView) findViewById(R.id.listViewSecilenSiparisler);
                if (listViewSecilenSiparisler.getCount() != 0)
                    alertDialog.show();
            }
        });

        adapterHesap = new MyListAdapter(urunListesiToplam, this,true);
        hesapListesi.setAdapter(adapterHesap);

        adapterSecilenSiparisler = new MyListAdapter(g.siparisListesi, this, false);
        secilenSiparislerinListesi.setAdapter(adapterSecilenSiparisler);

        registerForContextMenu(hesapListesi);

        secilenSiparislerinListesi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                selectedSiparisItemPosition = position;
                adapterSecilenSiparisler.setSelectedIndex(position);
            }
        });

        buttonArttir.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if(selectedSiparisItemPosition != -1)
                {
                    if (g.siparisListesi.get(selectedSiparisItemPosition).siparisPorsiyonSinifi == 0) // tam porsiyon
                    {
                        porsiyonEkle(1d);
                    }
                    else // diğer porsiyonlar
                    {
                        int [] selectedSiparisItemPositionsOnSiparisListesi = {-1,-1,-1,-1,-1}; // 0-1.5 porsiyon --- 1-1 porsiyon --- 2-0.75 porsiyon --- 3-0.5 porsiyon --- 4-0.25 porsiyon
                        for(int i=0;i<g.siparisListesi.size();i++)
                        {
                            if(g.siparisListesi.get(i).siparisYemekAdi.contentEquals(urunListesi.get(selectedSiparisItemPosition).siparisYemekAdi))
                            {
                                if (g.siparisListesi.get(i).siparisPorsiyonu == 1.5d) {
                                    selectedSiparisItemPositionsOnSiparisListesi[0] = i;
                                }
                                else if (g.siparisListesi.get(i).siparisPorsiyonu == 1) {
                                    selectedSiparisItemPositionsOnSiparisListesi[1] = i;
                                }
                                else if (g.siparisListesi.get(i).siparisPorsiyonu == 0.75d) {
                                    selectedSiparisItemPositionsOnSiparisListesi[2] = i;
                                }
                                else if (g.siparisListesi.get(i).siparisPorsiyonu == 0.5d) {
                                    selectedSiparisItemPositionsOnSiparisListesi[3] = i;
                                }
                                else if (g.siparisListesi.get(i).siparisPorsiyonu == 0.25d) {
                                    selectedSiparisItemPositionsOnSiparisListesi[4] = i;
                                }
                            }
                        }

                        for(int i = 0; i < selectedSiparisItemPositionsOnSiparisListesi.length; i++)
                        {
                            if (g.siparisListesi.get(selectedSiparisItemPosition).siparisPorsiyonSinifi == 1d && (i == 2 || i == 4)) // ürün yarım porsiyonluk ise 0.25 ve 0.75 eklenmemeli
                                continue; // eğer ürünün çeyrek porsiyon özelliği yoksa, sipariş porsiyonuna 0.75 ve 0.25 eklenmesin

                            if(selectedSiparisItemPositionsOnSiparisListesi[i] != -1)
                            {
                                switch (i)
                                {
                                    case 0:
                                        porsiyonlarPozitif.add("1.5 Porsiyon x" + g.siparisListesi.get(selectedSiparisItemPositionsOnSiparisListesi[i]).siparisAdedi);
                                        break;
                                    case 1:
                                        porsiyonlarPozitif.add("1 Porsiyon x" + g.siparisListesi.get(selectedSiparisItemPositionsOnSiparisListesi[i]).siparisAdedi);
                                        break;
                                    case 2:
                                        porsiyonlarPozitif.add("0.75 Porsiyon x" + g.siparisListesi.get(selectedSiparisItemPositionsOnSiparisListesi[i]).siparisAdedi);
                                        break;
                                    case 3:
                                        porsiyonlarPozitif.add("0.5 Porsiyon x" + g.siparisListesi.get(selectedSiparisItemPositionsOnSiparisListesi[i]).siparisAdedi);
                                        break;
                                    case 4:
                                        porsiyonlarPozitif.add("0.25 Porsiyon x" + g.siparisListesi.get(selectedSiparisItemPositionsOnSiparisListesi[i]).siparisAdedi);
                                        break;
                                    default:
                                        break;
                                }
                            }
                            else
                            {
                                switch (i)
                                {
                                    case 0:
                                        porsiyonlarPozitif.add("1.5 Porsiyon ");
                                        break;
                                    case 1:
                                        porsiyonlarPozitif.add("1 Porsiyon ");
                                        break;
                                    case 2:
                                        porsiyonlarPozitif.add("0.75 Porsiyon ");
                                        break;
                                    case 3:
                                        porsiyonlarPozitif.add("0.5 Porsiyon ");
                                        break;
                                    case 4:
                                        porsiyonlarPozitif.add("0.25 Porsiyon ");
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }

                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(HesapEkrani.this, android.R.layout.simple_list_item_1,porsiyonlarPozitif);

                        final AlertDialog alert = new AlertDialog.Builder(HesapEkrani.this)
                                .setTitle("Porsiyon Seçiniz")
                                .setCancelable(false)
                                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int item) {
                                        // porsiyon seçimi tamamlanınca yapılacaklar
                                        porsiyonlarPozitif.clear();
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int item) {
                                        Double porsiyon;

                                        try {
                                            porsiyon = Double.parseDouble(porsiyonlarPozitif.get(item).substring(0, 3));
                                        } catch (Exception ex) {
                                            porsiyon = 1d;
                                        }

                                        if (porsiyon == 0.7)
                                            porsiyon = 0.75;
                                        else if (porsiyon == 0.2)
                                            porsiyon = 0.25;

                                        porsiyonEkle(porsiyon);

                                        String[] urunPorsiyonSecimi = porsiyonlarPozitif.get(item).split("x");

                                        int adet = 0;

                                        if (urunPorsiyonSecimi.length > 1)
                                            adet = Integer.parseInt(urunPorsiyonSecimi[1]);

                                        adet++;

                                        porsiyonlarPozitif.set(item, urunPorsiyonSecimi[0] + "x" + adet);
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .create();

                        alert.show();
                    }
                }
            }
        });

        buttonAzalt.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if(selectedSiparisItemPosition != -1)
                    porsiyonCikar();
            }
        });

        if (g.commonAsyncTask.client != null) {
            if (g.commonAsyncTask.client.out != null) {
                HesapEkrani.this.getSupportActionBar().setTitle(departmanAdi + " - " + masaAdi + "(Bağlı)");
            } else {
                HesapEkrani.this.getSupportActionBar().setTitle(departmanAdi + " - " + masaAdi + "(Bağlantı yok)");
            }
        }

        if (g.siparisListesi.size() < 1) {
            buttonSepet.setEnabled(false);
        }
    }

    private void porsiyonCikar()
    {
        g.siparisListesi.get(selectedSiparisItemPosition).siparisAdedi --;

        if (g.siparisListesi.get(selectedSiparisItemPosition).siparisAdedi == 0)
        {
            g.siparisListesi.remove(selectedSiparisItemPosition);
            if (selectedSiparisItemPosition == g.siparisListesi.size())
            {
                selectedSiparisItemPosition = g.siparisListesi.size() - 1;
                adapterSecilenSiparisler.setSelectedIndex(g.siparisListesi.size() - 1);
            }
            if (g.siparisListesi.size() < 1) {
                buttonSepet.setEnabled(false);
            }
        }
        adapterSecilenSiparisler.notifyDataSetChanged();
    }

    private void porsiyonEkle(Double arttirmaMiktari)
    {
        Siparis siparis = new Siparis();

        for(int i = 0 ; i< g.siparisListesi.size();i++) {
            if (g.siparisListesi.get(i).siparisYemekAdi.contentEquals(g.siparisListesi.get(selectedSiparisItemPosition).siparisYemekAdi)&& g.siparisListesi.get(i).siparisPorsiyonu == arttirmaMiktari) {
                g.siparisListesi.get(i).siparisAdedi ++;
                adapterSecilenSiparisler.notifyDataSetChanged();
                return;
            }
        }

        siparis.siparisPorsiyonSinifi = g.siparisListesi.get(selectedSiparisItemPosition).siparisPorsiyonSinifi;
        siparis.siparisAdedi = 1;
        siparis.siparisFiyati = g.siparisListesi.get(selectedSiparisItemPosition).siparisFiyati;
        siparis.siparisYemekAdi = g.siparisListesi.get(selectedSiparisItemPosition).siparisYemekAdi;
        siparis.siparisPorsiyonu = arttirmaMiktari;

        g.siparisListesi.add(siparis);

        adapterSecilenSiparisler.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(!masaKilitliMi)
        {
            if (v.getId()==R.id.listViewHesap && !urunListesiToplam.get(((AdapterView.AdapterContextMenuInfo)menuInfo).position).siparisYemekAdi.contentEquals("İndirim") && !urunListesiToplam.get(((AdapterView.AdapterContextMenuInfo)menuInfo).position).siparisYemekAdi.contentEquals("Ödemeler")) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.menu_urun, menu);

                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
                menu.setHeaderTitle(urunListesiToplam.get(info.position).siparisYemekAdi);

                if (urunListesiToplam.get(info.position).siparisFiyati.contentEquals("ikram"))
                    menu.getItem(1).setTitle("İkramı İptal Et");
                else
                    menu.getItem(1).setTitle("Ürünü İkram Et");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        String baslik,hint;

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);

        switch(item.getItemId()) {
            case R.id.iptal:
                ikramMi = false;
                baslik = "Ürün İptali";
                hint = "Kaç adet ürün iptal edilecek? Bulunan : " + urunListesiToplam.get(info.position).siparisAdedi + " adet";
                break;
            case R.id.ikram:
                ikramMi = true;
                if(item.getTitle().toString().contentEquals("İkramı İptal Et"))
                {
                    baslik = "İkram İptali";
                    hint = "Kaç adet ikram iptal edilecek? Bulunan : " + urunListesiToplam.get(info.position).siparisAdedi + " adet";
                }
                else
                {
                    baslik = "İkram";
                    hint = "Kaç adet ürün ikram edilecek? Bulunan : " + urunListesiToplam.get(info.position).siparisAdedi + " adet";
                }
                break;
            default:
                return super.onContextItemSelected(item);
        }

        final EditText kacAdetIptalveyaIkram = new EditText(HesapEkrani.this);
        kacAdetIptalveyaIkram.setInputType(InputType.TYPE_CLASS_NUMBER);

        kacAdetIptalveyaIkram.setHint(hint);

        final EditText iptalNedeni = new EditText(HesapEkrani.this);
        iptalNedeni.setInputType(InputType.TYPE_CLASS_TEXT);
        iptalNedeni.setHint("İptal nedeninizi yazınız");

        View builderView;
        if(baslik.contentEquals("Ürün İptali"))
        {
            LinearLayout linearLayout=new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(kacAdetIptalveyaIkram);
            linearLayout.addView(iptalNedeni);
            builderView = linearLayout;
        }
        else
        {
            builderView = kacAdetIptalveyaIkram;
        }

        builder = new AlertDialog.Builder(HesapEkrani.this)
                .setTitle(baslik)
                .setView(builderView)
                .setPositiveButton("Tamam", null)
                .setNegativeButton("Vazgeç", null)
                .create();

        builder.setCanceledOnTouchOutside(false);

        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                final Button positiveButton = builder.getButton(AlertDialog.BUTTON_POSITIVE);
                final Button negativeButton = builder.getButton(AlertDialog.BUTTON_NEGATIVE);

                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        int kacAdet = Integer.parseInt(kacAdetIptalveyaIkram.getText().toString());

                        if (ikramMi) // ürün ikram edilecek
                        {
                            if (kacAdet > 0)
                            {
                                if(item.getTitle().toString().contentEquals("İkramı İptal Et"))
                                {
                                    // ikramı iptal et
                                    g.commonAsyncTask.client.sendMessage("komut=ikramIptal&masa=" + masaAdi + "&departmanAdi=" + departmanAdi + "&miktar=" + kacAdet + "&yemekAdi=" + urunListesiToplam.get(info.position).siparisYemekAdi + "&siparisiGirenKisi=" + employee.Name + " " + employee.LastName + "&dusulecekDeger=" + String.format("%.2f", urunListesiToplam.get(info.position).siparisPorsiyonSinifi) + "&adisyonNotu=&ikramYeniMiEskiMi=1,0&porsiyon=" + new DecimalFormat("#.##").format(urunListesiToplam.get(info.position).siparisPorsiyonu));

                                    positiveButton.setEnabled(false);
                                    negativeButton.setEnabled(false);
                                }
                                else
                                {
                                    // ikram et
                                    if(Double.parseDouble(urunListesiToplam.get(info.position).siparisFiyati)*kacAdet > toplamHesap) // kalan hesaptan fazla ise ikram edilemez
                                    {
                                        AlertDialog.Builder aBuilder = new AlertDialog.Builder(HesapEkrani.this);
                                        aBuilder.setTitle("İkram Hatası");
                                        aBuilder.setMessage("Ürün fiyatı kalan hesaptan büyük olduğu için ürün ikram edilemez")
                                                .setCancelable(false)
                                                .setPositiveButton("Tamam",null);
                                        AlertDialog alertDialog = aBuilder.create();
                                        alertDialog.show();
                                        return;
                                    }

                                    g.commonAsyncTask.client.sendMessage("komut=ikram&masa=" + masaAdi + "&departmanAdi=" + departmanAdi + "&miktar=" + kacAdet + "&yemekAdi=" + urunListesiToplam.get(info.position).siparisYemekAdi + "&siparisiGirenKisi=" + employee.Name + " " + employee.LastName + "&dusulecekDeger=" + urunListesiToplam.get(info.position).siparisFiyati + "&adisyonNotu=&porsiyon=" + new DecimalFormat("#.##").format(urunListesiToplam.get(info.position).siparisPorsiyonu));

                                    positiveButton.setEnabled(false);
                                    negativeButton.setEnabled(false);
                                }
                            }
                            else
                            {
                                builder.dismiss();
                            }
                        }
                        else // ürün iptali
                        {
                            if (iptalNedeni.getText().toString().trim().contentEquals("")) {
                                AlertDialog.Builder aBuilder = new AlertDialog.Builder(HesapEkrani.this);
                                aBuilder.setTitle("İptal Hatası")
                                        .setMessage("İptal nedeni boş bırakılamaz").setCancelable(false)
                                        .setPositiveButton("Tamam", null)
                                        .create();

                                AlertDialog alertDialog = aBuilder.create();
                                alertDialog.show();
                            }
                            else // ürün iptal edilecek
                            {
                                if (kacAdet > 0)
                                {
                                    //ürün iptal et
                                    if(Double.parseDouble(urunListesiToplam.get(info.position).siparisFiyati)*kacAdet > toplamHesap) // kalan hesaptan fazla ise iptal edilemez
                                    {
                                        AlertDialog.Builder aBuilder = new AlertDialog.Builder(HesapEkrani.this);
                                        aBuilder.setTitle("İptal Hatası");
                                        aBuilder.setMessage("Ürün fiyatı kalan hesaptan büyük olduğu için ürün iptal edilemez")
                                                .setCancelable(false)
                                                .setPositiveButton("Tamam",null);
                                        AlertDialog alertDialog = aBuilder.create();
                                        alertDialog.show();
                                        return;
                                    }

                                    String fiyat;

                                    String ikramMi;
                                    if (urunListesiToplam.get(info.position).siparisFiyati.contentEquals("ikram"))
                                    {
                                        ikramMi = "0";//ikramsa 0 veya 1
                                        fiyat = String.format("%.2f", urunListesiToplam.get(info.position).siparisPorsiyonSinifi);
                                    }
                                    else {
                                        ikramMi = "2";
                                        fiyat = urunListesiToplam.get(info.position).siparisFiyati;
                                    }

                                    g.commonAsyncTask.client.sendMessage("komut=iptal&masa=" + masaAdi + "&departmanAdi=" + departmanAdi + "&miktar=" + kacAdet + "&yemekAdi=" + urunListesiToplam.get(info.position).siparisYemekAdi + "&siparisiGirenKisi=" + employee.Name + " " + employee.LastName + "&dusulecekDeger=" + fiyat + "&adisyonNotu=&ikramYeniMiEskiMi=" + ikramMi + "&porsiyon=" + new DecimalFormat("#.##").format(urunListesiToplam.get(info.position).siparisPorsiyonu) + "&iptalNedeni=" + iptalNedeni.getText());
                                    positiveButton.setEnabled(false);
                                    negativeButton.setEnabled(false);
                                }
                                else
                                {
                                    builder.dismiss();
                                }
                            }
                        }
                    }
                });
            }
        });

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (source.charAt(i) == '<' || source.charAt(i) == '>' || source.charAt(i) == '&' || source.charAt(i) == '=' || source.charAt(i) == '*' || source.charAt(i) == '-') {
                        return "";
                    }
                }
                return null;
            }
        };

        iptalNedeni.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200),filter});

        kacAdetIptalveyaIkram.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

                Integer girilenSayi;
                try {
                    String x = s.toString();
                    girilenSayi = Integer.parseInt(x);
                } catch (Exception ex) {
                    return;
                }

                if (girilenSayi > urunListesiToplam.get(info.position).siparisAdedi) {
                    kacAdetIptalveyaIkram.setText(urunListesiToplam.get(info.position).siparisAdedi + "");
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        builder.show();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hesap_ekrani, menu);
        if(masaKilitliMi)
        {
            MenuItem item = menu.findItem(R.id.action_uruntasi);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_uruntasi) {
            if(urunListesiToplam.size() > 0)
            {
                //Ürün taşıma listesini göster
                for (Siparis anUrunListesiToplam : urunListesiToplam) {
                    if (anUrunListesiToplam.siparisAdedi != 0) {
                        UrunTasimaListesi listeyeEkle = new UrunTasimaListesi();
                        listeyeEkle.tasinacakUrunAdedi = anUrunListesiToplam.siparisAdedi;
                        listeyeEkle.tasinacakUrunFiyati = anUrunListesiToplam.siparisFiyati;
                        listeyeEkle.tasinacakUrunPorsiyonu = anUrunListesiToplam.siparisPorsiyonu;
                        listeyeEkle.tasinacakUrunYemekAdi = anUrunListesiToplam.siparisYemekAdi;
                        listeyeEkle.tasinacakUrunSecilenAdet = 0;
                        if(anUrunListesiToplam.siparisFiyati.contentEquals("ikram"))
                            listeyeEkle.siparisPorsiyonSinifi = anUrunListesiToplam.siparisPorsiyonSinifi;
                        urunTasimaListesi.add(listeyeEkle);
                    }
                }

                adapterUrunTasima = new UrunTasimaListAdapter(urunTasimaListesi, this);

                LayoutInflater inflater = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE));

                ListView tasinacakUrunleriGosterenListe = (ListView) inflater.inflate(R.layout.listviewalert, null, false);

                tasinacakUrunleriGosterenListe.setItemsCanFocus(true);

                tasinacakUrunleriGosterenListe.setAdapter(adapterUrunTasima);

                urunTasimaAlertDialog = new AlertDialog.Builder(HesapEkrani.this)
                        .setTitle("Ürün Taşıma")
                        .setCancelable(false)
                        .setView(tasinacakUrunleriGosterenListe)
                        .setPositiveButton("Tamam", null)
                        .setNegativeButton("Vazgeç", null)
                        .create();

                urunTasimaAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        final Button positiveButton = urunTasimaAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        final Button negativeButton = urunTasimaAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                aktarmaBilgileri = "";

                                Double tasinacakUrunlerinToplamFiyati = 0d;
                                for (UrunTasimaListesi anUrunTasimaListesi : urunTasimaListesi) {
                                    int adet = anUrunTasimaListesi.tasinacakUrunSecilenAdet;

                                    //Eğer taşınmak istenen ürünlerin  miktarı 0 yapılmışsa, 0 yapılanları taşımaya çalışma
                                    if (adet == 0)
                                        continue;

                                    if (adet > anUrunTasimaListesi.tasinacakUrunAdedi) {
                                        aBuilder = new AlertDialog.Builder(HesapEkrani.this);
                                        aBuilder.setTitle("Ürün Taşıma")
                                                .setMessage("Seçilen taşıma miktarı, bulunan ürün miktarını geçmektedir. Lütfen geçerli bir miktar giriniz")
                                                .setCancelable(false)
                                                .setPositiveButton("Tamam", null)
                                                .create();
                                        alertDialog2 = aBuilder.create();
                                        alertDialog2.show();
                                        return;
                                    }

                                    String yemekAdi = anUrunTasimaListesi.tasinacakUrunYemekAdi;
                                    Double porsiyonu = anUrunTasimaListesi.tasinacakUrunPorsiyonu;
                                    String fiyati = anUrunTasimaListesi.tasinacakUrunFiyati;

                                    if (fiyati.contentEquals("ikram")) {
                                        aktarmaBilgileri += "*" + yemekAdi + "-" + anUrunTasimaListesi.siparisPorsiyonSinifi + "-" + adet + "-1-" + porsiyonu;
                                    } else {
                                        aktarmaBilgileri += "*" + yemekAdi + "-" + fiyati + "-" + adet + "-0-" + porsiyonu;
                                    }

                                    try {
                                        tasinacakUrunlerinToplamFiyati += Double.parseDouble(fiyati) * adet;
                                    } catch (Exception ignored) {
                                    }
                                }

                                if(aktarmaBilgileri.length() > 0)
                                {
                                    aktarmaBilgileri = aktarmaBilgileri.substring(1, aktarmaBilgileri.length());
                                }
                                else
                                {
                                    //Eğer taşınması gereken ürün sayılarında 0 yapılanlar varsa ve onların dışında taşınacak ürün yoksa, ürün taşıma
                                    urunTasimaListesi.clear();
                                    urunTasimaAlertDialog.dismiss();
                                    return;
                                }

                                //Eğer taşınmak istenen ürünlerin fiyatları hesabı aşıyorsa ürünleri taşıma
                                if(tasinacakUrunlerinToplamFiyati > toplamHesap)
                                {
                                    aBuilder = new AlertDialog.Builder(HesapEkrani.this);
                                    aBuilder.setTitle("Ürün Taşıma")
                                            .setMessage("Taşınmak istenen ürünlerin toplam fiyatı kalan hesabı geçmektedir. Lütfen geçerli bir miktar giriniz")
                                            .setCancelable(false)
                                            .setPositiveButton("Tamam",null)
                                            .create();
                                    alertDialog2 = aBuilder.create();
                                    alertDialog2.show();
                                    return;
                                }

                                //açık masalar için tüm departmanları iste
                                gelenDepartman = 0;
                                for(int i=0;i<g.globalDepartmanlar.size();i++)
                                    g.commonAsyncTask.client.sendMessage("komut=departmanMasaSecimiIcin&departmanAdi=" + g.globalDepartmanlar.get(i).globalDepartmanAdi);
                                urunTasimaAlertDialog.dismiss();
                            }
                        });

                        negativeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // edittext boşsa 0 olarak al
                                urunTasimaListesi.clear();
                                urunTasimaAlertDialog.dismiss();
                            }
                        });
                    }
                });
                urunTasimaAlertDialog.show();
                urunTasimaAlertDialog.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                urunTasimaAlertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        }

        return id == R.id.action_uruntasi || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
		activityVisible = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(rec);
    }

    @Override
    protected void onStop() {
        super.onStop();
       activityVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
		activityVisible = true;
        LocalBroadcastManager.getInstance(this).registerReceiver(rec, new IntentFilter("myevent"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
		activityVisible=false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(rec);
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
                    case siparis:
                        if(collection.get("masa").contentEquals(masaAdi) && collection.get("departmanAdi").contentEquals(departmanAdi))
                        {
                            String yemekAdi = collection.get("yemekAdi");
                            Double fiyat = Double.parseDouble(collection.get("dusulecekDeger").replace(',', '.'));
                            Double porsiyon = Double.parseDouble(collection.get("porsiyon").replace(',','.'));
                            int miktar = Integer.parseInt(collection.get("miktar").replace(',', '.'));

                            int gruptaYeniGelenSiparisVarmi = siparisGruptaVarMi(yemekAdi, porsiyon); //ürün cinsi hesapta var mı bak

                            if (gruptaYeniGelenSiparisVarmi == -1) //yoksa ürünü hesaba ekle
                            {
                                Siparis gelenSiparis = new Siparis();
                                gelenSiparis.siparisAdedi = miktar;
                                gelenSiparis.siparisYemekAdi = yemekAdi;
                                gelenSiparis.siparisFiyati = String.format("%.2f", fiyat);
                                gelenSiparis.siparisPorsiyonu = porsiyon;
                                urunListesiToplam.add(gelenSiparis);
                            }
                            else // varsa ürünün hesaptaki değerlerini istenilene göre arttır
                            {
                                urunListesiToplam.get(gruptaYeniGelenSiparisVarmi).siparisAdedi += miktar;
                            }
                            toplamHesap += (fiyat*miktar);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapterHesap.notifyDataSetChanged();
                                    hesapButton.setText("Hesap İste  /  Kalan = " + String.format("%.2f", toplamHesap) + " TL");
                                }
                            });
                        }
                        break;
                    case iptal:
                        if(collection.get("masa").contentEquals(masaAdi) && collection.get("departmanAdi").contentEquals(departmanAdi))
                        {
                            String yemekAdi = collection.get("yemekAdi");
                            Double fiyat = Double.parseDouble(collection.get("dusulecekDeger").replace(',','.'));
                            int ikramMi = Integer.parseInt(collection.get("ikramYeniMiEskiMi"));
                            Double porsiyon = Double.parseDouble(collection.get("porsiyon").replace(',','.'));
                            int miktar = Integer.parseInt(collection.get("miktar").replace(',', '.'));

                            for(int i=0;i<urunListesiToplam.size();i++)
                            {
                                if(urunListesiToplam.get(i).siparisYemekAdi.contentEquals(yemekAdi) && urunListesiToplam.get(i).siparisPorsiyonu == porsiyon)
                                {
                                    if (ikramMi == 2) // iptali istenilen ürün ikram değilse kalan hesaptan da düşülmeli
                                    {
                                        if(urunListesiToplam.get(i).siparisFiyati.contentEquals("ikram"))
                                            continue;

                                        toplamHesap -= (fiyat*miktar);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                hesapButton.setText("Hesap İste  /  Kalan = " + String.format("%.2f", toplamHesap) + " TL");
                                            }
                                        });
                                    }
                                    else
                                    {
                                        if(!urunListesiToplam.get(i).siparisFiyati.contentEquals("ikram"))
                                            continue;
                                    }

                                    urunListesiToplam.get(i).siparisAdedi -= miktar;

                                    if (urunListesiToplam.get(i).siparisAdedi <= 0)
                                    {
                                        urunListesiToplam.remove(i);
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapterHesap.notifyDataSetChanged();
                                        }
                                    });

                                    if(builder != null)
                                        builder.dismiss();
                                    return;
                                }
                            }
                        }
                        break;
                    case ikram:
                        if(collection.get("masa").contentEquals(masaAdi) && collection.get("departmanAdi").contentEquals(departmanAdi))
                        {
                            String yemekAdi = collection.get("yemekAdi");
                            Double fiyat = Double.parseDouble(collection.get("dusulecekDeger").replace(',', '.'));
                            Double porsiyon = Double.parseDouble(collection.get("porsiyon").replace(',','.'));
                            int miktar = Integer.parseInt(collection.get("miktar").replace(',', '.'));

                            for (int i = 0; i < urunListesiToplam.size(); i++)
                            {
                                if (yemekAdi.equals(urunListesiToplam.get(i).siparisYemekAdi) && porsiyon == urunListesiToplam.get(i).siparisPorsiyonu && !urunListesiToplam.get(i).siparisFiyati.contentEquals("ikram"))
                                {
                                    urunListesiToplam.get(i).siparisAdedi -= miktar;

                                    toplamHesap -= (fiyat*miktar);

                                    Boolean ikramYok = true; // ikram yeni ikramlar listesinde var mı diye bak

                                    for (Siparis anUrunListesiToplam : urunListesiToplam)
                                    {
                                        if (yemekAdi.equals(anUrunListesiToplam.siparisYemekAdi) && porsiyon == anUrunListesiToplam.siparisPorsiyonu && anUrunListesiToplam.siparisFiyati.contentEquals("ikram")) {
                                            anUrunListesiToplam.siparisAdedi += miktar;
                                            ikramYok = false;
                                            break;
                                        }
                                    }

                                    if (ikramYok) // yok yeni ikramı listeye ekle
                                    {
                                        Siparis gelenSiparis = new Siparis();
                                        gelenSiparis.siparisAdedi = miktar;
                                        gelenSiparis.siparisYemekAdi = yemekAdi;
                                        gelenSiparis.siparisFiyati = "ikram";
                                        gelenSiparis.siparisPorsiyonSinifi = fiyat;
                                        gelenSiparis.siparisPorsiyonu = porsiyon;
                                        urunListesiToplam.add(gelenSiparis);
                                    }

                                    if (urunListesiToplam.get(i).siparisAdedi <= 0)
                                    {
                                        urunListesiToplam.remove(i);
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hesapButton.setText("Hesap İste  /  Kalan = " + String.format("%.2f", toplamHesap) + " TL");
                                            adapterHesap.notifyDataSetChanged();
                                        }
                                    });

                                    if(builder != null)
                                        builder.dismiss();
                                }
                            }
                        }
                        break;
                    case ikramIptal:
                        if(collection.get("masa").contentEquals(masaAdi) && collection.get("departmanAdi").contentEquals(departmanAdi))
                        {
                            String yemekAdi = collection.get("yemekAdi");
                            Double fiyat = Double.parseDouble(collection.get("dusulecekDeger").replace(',', '.'));
                            Double porsiyon = Double.parseDouble(collection.get("porsiyon").replace(',','.'));
                            int miktar = Integer.parseInt(collection.get("miktar").replace(',', '.'));

                            for(int i=0;i<urunListesiToplam.size();i++)
                            {
                                if (urunListesiToplam.get(i).siparisYemekAdi.contentEquals(yemekAdi) && urunListesiToplam.get(i).siparisPorsiyonu == porsiyon && urunListesiToplam.get(i).siparisFiyati.contentEquals("ikram"))
                                {
                                    urunListesiToplam.get(i).siparisAdedi -= miktar;
                                    toplamHesap -= (fiyat*miktar);

                                    Boolean urunYok = true;

                                    for (Siparis anUrunListesiToplam : urunListesiToplam)
                                    {
                                        if (yemekAdi.equals(anUrunListesiToplam.siparisYemekAdi) && porsiyon == anUrunListesiToplam.siparisPorsiyonu && !anUrunListesiToplam.siparisFiyati.contentEquals("ikram")) {
                                            anUrunListesiToplam.siparisAdedi += miktar;
                                            urunYok = false;
                                            break;
                                        }
                                    }

                                    if (urunYok)
                                    {
                                        Siparis gelenSiparis = new Siparis();
                                        gelenSiparis.siparisAdedi = miktar;
                                        gelenSiparis.siparisYemekAdi = yemekAdi;
                                        gelenSiparis.siparisFiyati = String.format("%.2f", fiyat);
                                        gelenSiparis.siparisPorsiyonu = porsiyon;
                                        urunListesiToplam.add(gelenSiparis);
                                    }

                                    if (urunListesiToplam.get(i).siparisAdedi <= 0)
                                    {
                                        urunListesiToplam.remove(i);
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hesapButton.setText("Hesap İste  /  Kalan = " + String.format("%.2f", toplamHesap) + " TL");
                                            adapterHesap.notifyDataSetChanged();
                                        }
                                    });

                                    if(builder != null)
                                        builder.dismiss();
                                }
                            }
                        }
                        break;
                    case hesapGeliyor:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                aBuilder = new AlertDialog.Builder(HesapEkrani.this);
                                aBuilder.setTitle("Hesap Denetimi")
                                        .setMessage("Hesap istediğiniz için işleminiz gerçekleştirilememektedir")
                                        .setCancelable(false)
                                        .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int item) {
                                                if(progressDialog == null)
                                                    progressDialog = ProgressDialog.show(HesapEkrani.this, "Hesap isteği iletildi", "Hesap bekleniyor...", false);
                                                else if (!progressDialog.isShowing())
                                                    progressDialog = ProgressDialog.show(HesapEkrani.this, "Hesap İsteği İletildi", "Hesap bekleniyor...", false);
                                                callHesapIste();                                            }
                                        })
                                        .create();
                                alertDialog2 = aBuilder.create();
                                alertDialog2.show();
                            }
                        });
                        break;
                    case hesapIslemde:
                        timerRunning = false;

                        if(collection.get("yemekAdi")!= null) {
                            Siparis siparis = new Siparis();

                            siparis.siparisPorsiyonSinifi = Double.parseDouble(collection.get("porsiyonSinifi"));
                            siparis.siparisAdedi = Integer.parseInt(collection.get("miktar"));
                            siparis.siparisFiyati = collection.get("fiyat");
                            siparis.siparisYemekAdi = collection.get("yemekAdi");
                            siparis.siparisPorsiyonu = Double.parseDouble(collection.get("porsiyon"));

                            g.siparisListesi.add(siparis);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapterSecilenSiparisler.notifyDataSetChanged();
                                }
                            });
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                aBuilder = new AlertDialog.Builder(HesapEkrani.this);
                                aBuilder.setTitle("Hesap Denetimi")
                                        .setMessage("Hesabınızla ilgili işlem yapılmaktadır, lütfen kısa süre sonra tekrar deneyiniz")
                                        .setCancelable(false)
                                        .setPositiveButton("Tamam", null)
                                        .create();
                                alertDialog2 = aBuilder.create();
                                alertDialog2.show();
                                if(progressDialog != null)
                                {
                                    if(progressDialog.isShowing())
                                        progressDialog.dismiss();
                                }
                                SetViewGroupEnabled.setViewGroupEnabled((ViewGroup) findViewById(R.id.hesapEkrani), true);
                            }
                        });
                        break;
                    case masaKapandi:
                        if(collection.get("masa").contentEquals(masaAdi) && collection.get("departmanAdi").contentEquals(departmanAdi)) {
                            urunTasimaListesi.clear();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(urunTasimaAlertDialog != null)
                                        urunTasimaAlertDialog.dismiss();
                                    if(urunTasimaIcinMasaSecimiAlertDialog != null)
                                        urunTasimaIcinMasaSecimiAlertDialog.dismiss();
                                    if(progressDialog != null)
                                        if(progressDialog.isShowing())
                                            progressDialog.dismiss();
                                    if(alertDialog2 != null)
                                        alertDialog2.dismiss();
                                    timerRunning = false;
                                    g.siparisListesi.clear();
                                    onBackPressed();
                                }
                            });
                        }
                        else
                        {
                            if(tasimaIcinMasaExpandableListAdapter != null)
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tasimaIcinMasaExpandableListAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                        break;
                    case masaAcildi:
                        if(collection.get("masa").contentEquals(masaAdi) && collection.get("departmanAdi").contentEquals(departmanAdi)) {
                            masaAcikMi = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hesapButton.setEnabled(true);
                                }
                            });
                        }
                        if(tasimaIcinMasaExpandableListAdapter != null)
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tasimaIcinMasaExpandableListAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                        break;
                    case departmanMasaSecimiIcin:
                        String [] acikMasalar;
                        try {
                            acikMasalar = collection.get("masa").split("\\*");
                        } catch (Exception e) {
                            acikMasalar = null;
                        }

                        if(acikMasalar != null)
                        {
                            for (String anAcikMasalar : acikMasalar) {
                                for (int i = 0; i < g.globalDepartmanlar.get(gelenDepartman).globalMasalar.size(); i++) {
                                    if (anAcikMasalar.contentEquals(g.globalDepartmanlar.get(gelenDepartman).globalMasalar.get(i).globalMasaAdi)) {
                                        g.globalDepartmanlar.get(gelenDepartman).globalMasalar.get(i).globalMasaAcikMi = true;
                                        break;
                                    }
                                }
                            }
                        }
                        gelenDepartman++;

                        //tüm departmanlar geldiyse ürünlerin taşınacağı masanın seçilmesi için masaları göster
                        if(g.globalDepartmanlar.size() == gelenDepartman)
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    LayoutInflater inflater = ((LayoutInflater) HesapEkrani.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE));

                                    ExpandableListView expandableListViewTasimaIcinMasaSec = (ExpandableListView) inflater.inflate(R.layout.expandaplelistviewalert, null, false);

                                    tasimaIcinMasaExpandableListAdapter = new TasimaIcinMasaSecimiExpandableListAdapter(HesapEkrani.this, g);
                                    expandableListViewTasimaIcinMasaSec.setAdapter(tasimaIcinMasaExpandableListAdapter);
                                    expandableListViewTasimaIcinMasaSec.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                                        @Override
                                        public boolean onChildClick(ExpandableListView parent, View v,final int groupPosition,final int childPosition, long id) {
                                            if(!urunTasimaIcinMasaSecimiAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled())
                                                return true;

                                            //ürünler şu masaya taşınıyor emin misiniz mesajı göster ok basılırsa yolla
                                            // SEÇİLEN DEPARTMAN g.globalDepartmanlar.get(groupPosition).globalDepartmanAdi
                                            // SEÇİLEN MASA g.globalDepartmanlar.get(groupPosition).globalMasalar.get(childPosition).globalMasaAdi

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    AlertDialog.Builder aBuilder = new AlertDialog.Builder(HesapEkrani.this);
                                                    aBuilder.setTitle("Ürün Taşıma")
                                                            .setMessage("Ürünler "+departmanAdi+" departmanındaki "+masaAdi+" masasından "+g.globalDepartmanlar.get(groupPosition).globalDepartmanAdi+" departmanındaki "+g.globalDepartmanlar.get(groupPosition).globalMasalar.get(childPosition).globalMasaAdi+ " masasına aktarılacaktır. Onaylıyor musunuz?")
                                                            .setCancelable(false)
                                                            .setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int item) {
                                                                    if(departmanAdi.contentEquals(g.globalDepartmanlar.get(groupPosition).globalDepartmanAdi) && masaAdi.contentEquals(g.globalDepartmanlar.get(groupPosition).globalMasalar.get(childPosition).globalMasaAdi)) {
                                                                        urunTasimaListesi.clear();
                                                                        urunTasimaIcinMasaSecimiAlertDialog.dismiss();
                                                                        return;
                                                                    }
                                                                    g.commonAsyncTask.client.sendMessage("komut=urunuTasiTablet&masa=" + masaAdi + "&departmanAdi=" + departmanAdi + "&yeniMasa=" + g.globalDepartmanlar.get(groupPosition).globalMasalar.get(childPosition).globalMasaAdi + "&yeniDepartmanAdi=" + g.globalDepartmanlar.get(groupPosition).globalDepartmanAdi + "&siparisiGirenKisi=" + employee.Name + " " + employee.LastName + "&aktarmaBilgileri=" + aktarmaBilgileri);
                                                                    urunTasimaListesi.clear();
                                                                    urunTasimaIcinMasaSecimiAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                                                                }
                                                            })
                                                            .setNegativeButton("Hayır",null)
                                                            .create();
                                                    alertDialog2 = aBuilder.create();
                                                    alertDialog2.show();
                                                }
                                            });
                                            return true;
                                        }
                                    });

                                    urunTasimaIcinMasaSecimiAlertDialog = new AlertDialog.Builder(HesapEkrani.this)
                                            .setTitle("Ürünlerin taşınacağı masayı seçiniz("+ departmanAdi + " - " + masaAdi + ")")
                                            .setView(expandableListViewTasimaIcinMasaSec)
                                            .setCancelable(false)
                                            .setNegativeButton("Vazgeç", null)
                                            .create();

                                    urunTasimaIcinMasaSecimiAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialog) {
                                            final Button negativeButton = urunTasimaIcinMasaSecimiAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                                            negativeButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    urunTasimaListesi.clear();
                                                    urunTasimaIcinMasaSecimiAlertDialog.dismiss();
                                                }
                                            });
                                        }
                                    });
                                    urunTasimaIcinMasaSecimiAlertDialog.show();
                                }
                            });
                        }
                        break;
                    case urunuTasiTablet:
                        if(collection.get("masa").contentEquals(masaAdi) && collection.get("departmanAdi").contentEquals(departmanAdi)) { // ürün taşınan masa ise taşınan ürünleri çıkar
                            String[] aktarmalar = collection.get("aktarmaBilgileri").split("\\*");
                            for (String anAktarmalar : aktarmalar) {
                                String yemekAdi;
                                double fiyat;
                                int istenilenTasimaMiktari, tasinacakUrunIkramMi;
                                Double porsiyon;

                                String[] detaylari = anAktarmalar.split("-");
                                yemekAdi = detaylari[0];
                                fiyat = Double.parseDouble(detaylari[1].replace(',', '.'));
                                istenilenTasimaMiktari = Integer.parseInt(detaylari[2].replace(',', '.'));
                                tasinacakUrunIkramMi = Integer.parseInt(detaylari[3]);
                                porsiyon = Double.parseDouble(detaylari[4].replace(',', '.'));

                                for (int x = 0; x < urunListesiToplam.size(); x++) {
                                    if (urunListesiToplam.get(x).siparisYemekAdi.contentEquals(yemekAdi) && urunListesiToplam.get(x).siparisPorsiyonu == porsiyon) {
                                        if (tasinacakUrunIkramMi == 0) {
                                            if (urunListesiToplam.get(x).siparisFiyati.contentEquals("ikram"))
                                                continue;

                                            toplamHesap -= (fiyat * istenilenTasimaMiktari);
                                        } else {
                                            if (!urunListesiToplam.get(x).siparisFiyati.contentEquals("ikram"))
                                                continue;
                                        }

                                        urunListesiToplam.get(x).siparisAdedi -= istenilenTasimaMiktari;

                                        if (urunListesiToplam.get(x).siparisAdedi <= 0) {
                                            urunListesiToplam.remove(x);
                                        }
                                        break;
                                    }
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hesapButton.setText("Hesap İste  /  Kalan = " + String.format("%.2f", toplamHesap) + " TL");
                                    adapterHesap.notifyDataSetChanged();
                                }
                            });
                            if(urunTasimaIcinMasaSecimiAlertDialog != null)
                                urunTasimaIcinMasaSecimiAlertDialog.dismiss();
                        }
                        else if(collection.get("yeniMasa").contentEquals(masaAdi) && collection.get("yeniDepartmanAdi").contentEquals(departmanAdi)) { // ürünlerin taşındığı masa ise ürünleri ekle
                            String[] aktarmalar = collection.get("aktarmaBilgileri").split("\\*");
                            for (String anAktarmalar : aktarmalar) {
                                String yemekAdi;
                                double fiyat;
                                int istenilenTasimaMiktari, tasinacakUrunIkramMi;
                                Double porsiyon;

                                String[] detaylari = anAktarmalar.split("-");
                                yemekAdi = detaylari[0];
                                fiyat = Double.parseDouble(detaylari[1].replace(',', '.'));
                                istenilenTasimaMiktari = Integer.parseInt(detaylari[2].replace(',', '.'));
                                tasinacakUrunIkramMi = Integer.parseInt(detaylari[3]);
                                porsiyon = Double.parseDouble(detaylari[4].replace(',', '.'));

                                int gruptaYeniGelenSiparisVarmi;

                                if (tasinacakUrunIkramMi == 0) {
                                    gruptaYeniGelenSiparisVarmi = siparisGruptaVarMi(yemekAdi, porsiyon); //ürün cinsi hesapta var mı bak
                                } else {
                                    gruptaYeniGelenSiparisVarmi = ikramGruptaVarMi(yemekAdi, porsiyon); //ürün cinsi hesapta var mı bak
                                }

                                if (gruptaYeniGelenSiparisVarmi == -1) //yoksa ürünü hesaba ekle
                                {
                                    Siparis gelenSiparis = new Siparis();
                                    gelenSiparis.siparisAdedi = istenilenTasimaMiktari;
                                    gelenSiparis.siparisYemekAdi = yemekAdi;
                                    if (tasinacakUrunIkramMi == 0)
                                        gelenSiparis.siparisFiyati = String.format("%.2f", fiyat);
                                    else
                                        gelenSiparis.siparisFiyati = "ikram";
                                    gelenSiparis.siparisPorsiyonu = porsiyon;
                                    urunListesiToplam.add(gelenSiparis);
                                } else // varsa ürünün hesaptaki değerlerini istenilene göre arttır
                                {
                                    urunListesiToplam.get(gruptaYeniGelenSiparisVarmi).siparisAdedi += istenilenTasimaMiktari;
                                }
                                toplamHesap += (fiyat * istenilenTasimaMiktari);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapterHesap.notifyDataSetChanged();
                                    hesapButton.setText("Hesap İste  /  Kalan = " + String.format("%.2f", toplamHesap) + " TL");
                                    if(urunTasimaIcinMasaSecimiAlertDialog != null)
                                        urunTasimaIcinMasaSecimiAlertDialog.dismiss();
                                }
                            });
                        }
                        break;
                    case OdemeIndirimOnayTablet:
                        if(collection.get("masaAdi").contentEquals(masaAdi) && collection.get("departmanAdi").contentEquals(departmanAdi)) {
                            g.commonAsyncTask.client.sendMessage("komut=OdemeBilgileriGuncelleTablet&masa=" + masaAdi + "&departmanAdi=" + departmanAdi);
                        }
                        break;
                    case OdemeBilgileriGuncelleTablet:

                        for(int i =0;i<urunListesiToplam.size();i++)
                        {
                            if(urunListesiToplam.get(i).siparisYemekAdi.contentEquals("İndirim"))
                            {
                                toplamHesap -= Double.parseDouble(urunListesiToplam.get(i).siparisFiyati);
                                urunListesiToplam.remove(i);
                                i--;
                            }

                            if(urunListesiToplam.get(i).siparisYemekAdi.contentEquals("Ödemeler"))
                            {
                                toplamHesap -= Double.parseDouble(urunListesiToplam.get(i).siparisFiyati);
                                urunListesiToplam.remove(i);
                                i--;
                            }
                        }

                        NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
                        Number indirimNumber = null, odemelerNumber = null;

                        Double indirimler = 0d, alinanOdemeler = 0d;
                        try {
                            indirimNumber = format.parse(collection.get("indirimler"));
                        } catch (Exception ignored) {
                        }

                        try {
                            odemelerNumber = format.parse(collection.get("alinanOdemeler"));
                        } catch (Exception ignored) {
                        }

                        if (indirimNumber != null)
                            indirimler -= indirimNumber.doubleValue();

                        if (odemelerNumber != null)
                            alinanOdemeler -= odemelerNumber.doubleValue();

                        if(indirimler != 0)
                        {
                            Siparis indirim = new Siparis();
                            indirim.siparisFiyati = indirimler.toString();
                            indirim.siparisYemekAdi = "İndirim";
                            urunListesiToplam.add(indirim);
                        }

                        if(alinanOdemeler != 0)
                        {
                            Siparis odemeler = new Siparis();
                            odemeler.siparisFiyati = alinanOdemeler.toString();
                            odemeler.siparisYemekAdi = "Ödemeler";
                            urunListesiToplam.add(odemeler);
                        }
                        toplamHesap = toplamHesap + alinanOdemeler + indirimler;

                        if(collection.get("odenenUrunBilgileri") != null)
                        {
                            if(!collection.get("odenenUrunBilgileri").isEmpty())
                            {
                                Double fiyat=0d, porsiyon=0d;
                                int adet;
                                String yemekAdi;

                                String[] odenenSiparisler = collection.get("odenenUrunBilgileri").split("\\*");

                                //fiyat - adet -porsiyon - yemekadi
                                for (String aSiparisler : odenenSiparisler) {
                                    String[] odenenSiparis = aSiparisler.split("-");

                                    format = NumberFormat.getInstance(Locale.FRANCE);
                                    Number numberFiyat = null, numberPorsiyon = null;

                                    //burası virgüllü stringi double a convert etme çevirme kısmı
                                    try {
                                        numberFiyat = format.parse(odenenSiparis[0]);
                                        numberPorsiyon = format.parse(odenenSiparis[2]);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    if (numberFiyat != null)
                                        fiyat = numberFiyat.doubleValue();

                                    if (numberPorsiyon != null)
                                        porsiyon = numberPorsiyon.doubleValue();

                                    adet = Integer.parseInt(odenenSiparis[1]);
                                    yemekAdi = odenenSiparis[3];

                                    for (int i = 0; i < urunListesiToplam.size(); i++) {
                                        if (yemekAdi.contentEquals(urunListesiToplam.get(i).siparisYemekAdi) && porsiyon == urunListesiToplam.get(i).siparisPorsiyonu && !urunListesiToplam.get(i).siparisFiyati.contentEquals("ikram")) // listede yemek var
                                        {
                                            toplamHesap -= (fiyat*adet);

                                            urunListesiToplam.get(i).siparisAdedi -= adet;

                                            if (urunListesiToplam.get(i).siparisAdedi <= 0)
                                            {
                                                urunListesiToplam.remove(i);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hesapButton.setText("Hesap İste  /  Kalan = " + String.format("%.2f", toplamHesap) + " TL");
                                adapterHesap.notifyDataSetChanged();
                            }
                        });
                        break;
                    case siparisListesineGeriEkle:
                        Siparis geriGelenSiparis = new Siparis();

                        geriGelenSiparis.siparisPorsiyonSinifi = Double.parseDouble(collection.get("porsiyonSinifi"));
                        geriGelenSiparis.siparisAdedi = Integer.parseInt(collection.get("miktar"));
                        geriGelenSiparis.siparisFiyati = collection.get("fiyat");
                        geriGelenSiparis.siparisYemekAdi = collection.get("yemekAdi");
                        geriGelenSiparis.siparisPorsiyonu = Double.parseDouble(collection.get("porsiyon"));

                        g.siparisListesi.add(geriGelenSiparis);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapterSecilenSiparisler.notifyDataSetChanged();
                            }
                        });
                        break;
                    case masaDegistir:
                        if((collection.get("masa").contentEquals(masaAdi) && collection.get("departmanAdi").contentEquals(departmanAdi)) || (collection.get("yeniMasa").contentEquals(masaAdi) && collection.get("yeniDepartmanAdi").contentEquals(departmanAdi)))
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    aBuilder = new AlertDialog.Builder(HesapEkrani.this);
                                    aBuilder.setTitle("Masa Değişimi")
                                            .setMessage("Masa değişimi gerçekleştirildi, lütfen hesabınıza tekrar giriniz")
                                            .setCancelable(false)
                                            .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    HesapEkrani.this.finish();
                                                }
                                            })
                                            .create();
                                    alertDialog2 = aBuilder.create();
                                    alertDialog2.show();
                                }
                            });
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    };

    private int siparisGruptaVarMi(String yemekAdi, Double porsiyonu)
    {
        //ürün cinsi hesapta var mı bak
        for (int i = 0; i < urunListesiToplam.size(); i++)
        {
            if (yemekAdi.equals(urunListesiToplam.get(i).siparisYemekAdi) && porsiyonu == urunListesiToplam.get(i).siparisPorsiyonu)
            {
                return i;
            }
        }
        return -1;
    }

    private int ikramGruptaVarMi(String yemekAdi, Double porsiyonu)
    {
        //ürün cinsi hesapta var mı bak
        for (int i = 0; i < urunListesiToplam.size(); i++)
        {
            if (yemekAdi.equals(urunListesiToplam.get(i).siparisYemekAdi) && porsiyonu == urunListesiToplam.get(i).siparisPorsiyonu && urunListesiToplam.get(i).siparisFiyati.contentEquals("ikram"))
            {
                return i;
            }
        }
        return -1;
    }
}