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
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;

import Entity.Employee;
import Entity.Siparis;

public class HesapEkrani extends Activity {

    String departmanAdi, masaAdi;
    Employee employee;
    SharedPreferences preferences = null;
    boolean MasaKilitliMi = false;
    MyListAdapter adapterSecilenSiparisler, adapterHesap;
    ArrayList<Siparis> urunListesiToplam = new ArrayList<Siparis>();
    int selectedSiparisItemPosition = -1;
    GlobalApplication g;
    ArrayList<String> porsiyonlarPozitif = new ArrayList<String>();

    Integer kacAdet;
    Double  yemeginFiyati, toplamHesap = 0d;
    Button buttonSepet;
    Boolean ikramMi;
    AlertDialog alertDialog;
    Button hesapButton;
    AlertDialog builder;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_hesap_ekrani);
        Bundle extras = getIntent().getExtras();
        departmanAdi = extras.getString("DepartmanAdi");
        departmanAdi = extras.getString("DepartmanAdi");
        masaAdi = extras.getString("MasaAdi");
        employee = (Employee) extras.getSerializable("Employee");

        g = (GlobalApplication) getApplicationContext();

        preferences = this.getSharedPreferences("KilitliMasa", Context.MODE_PRIVATE);
        MasaKilitliMi = preferences.getBoolean("MasaKilitli", false);

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

        final String siparisler = extras.getString("siparisler");

        if(siparisler != null)
        {
            String[] Siparisler = siparisler.split("\\*");

            for (String aSiparisler : Siparisler) {
                String[] detaylari = aSiparisler.split("-");

                NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
                Number numberFiyat = null, numberPorsiyon = null;

                //burası virgüllü stringi double a convert etme çevirme kısmı
                try {
                    numberFiyat = format.parse(detaylari[0]);
                    numberPorsiyon = format.parse(detaylari[5]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (numberFiyat != null)
                    yemeginFiyati = numberFiyat.doubleValue();

                if (numberPorsiyon != null)
                    porsiyon = numberPorsiyon.doubleValue();

                kacAdet = Integer.parseInt(detaylari[1]);

                yemeginAdi = detaylari[2];
                ikramMi = Boolean.parseBoolean(detaylari[3]);

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
                        urunListesi.get(gruptaYeniGelenSiparisVarMi).siparisAdedi = urunListesi.get(gruptaYeniGelenSiparisVarMi).siparisAdedi + kacAdet;
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
                } else // ikramsa
                {
                    for (int j = 0; j < urunListesiIkram.size(); j++) {
                        if (yemeginAdi.contentEquals(urunListesiIkram.get(j).siparisYemekAdi) && porsiyon == urunListesi.get(j).siparisPorsiyonu) // listede yemek var
                        {
                            gruptaYeniGelenSiparisVarMi = j;
                            break;
                        }
                    }

                    if (gruptaYeniGelenSiparisVarMi != -1) // listede yemek var
                    {
                        urunListesiIkram.get(gruptaYeniGelenSiparisVarMi).siparisAdedi = urunListesiIkram.get(gruptaYeniGelenSiparisVarMi).siparisAdedi + kacAdet;
                    } else // listede yemek yok
                    {
                        Siparis yeniSiparis = new Siparis();
                        yeniSiparis.siparisAdedi = kacAdet;
                        yeniSiparis.siparisYemekAdi = yemeginAdi;
                        yeniSiparis.siparisFiyati = "ikram";
                        yeniSiparis.siparisPorsiyonu = porsiyon;
                        urunListesiIkram.add(yeniSiparis);
                    }
                }
            }
            urunListesiToplam.addAll(urunListesi);
            urunListesiToplam.addAll(urunListesiIkram);

            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
            Number indirimNumber = null, odemelerNumber = null;

            try {
                indirimNumber = format.parse(extras.getString("indirimler"));
                odemelerNumber = format.parse(extras.getString("alinanOdemeler"));
            } catch (Exception ignored) {
            }

            Double indirimler = 0d, alinanOdemeler = 0d;
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

            hesapButton.setText("Hesap İste  /  Toplam = " + String.format("%.2f", toplamHesap) + " TL");
        }
        else
        {
            hesapButton.setText("Hesap İste  /  Toplam = 0 TL");
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
            }
        });

        iptalNedeni.setFilters(new InputFilter[]{new InputFilter.LengthFilter(250),filter});

        AlertDialog.Builder aBuilder = new AlertDialog.Builder(HesapEkrani.this);
        aBuilder.setTitle("Sipariş Onayı");
        aBuilder.setMessage("Girdiğiniz siparişleri onaylıyor musunuz?")
                .setView(iptalNedeni)
                .setCancelable(false);

        aBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.cancel();
                alertDialog.dismiss();
            }
        });
        aBuilder.setPositiveButton("Onayla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (urunListesiToplam.size() < 1 && g.siparisListesi.size() > 0) {
                    g.commonAsyncTask.client.sendMessage("komut=masaAcildi&masa=" + masaAdi + "&departmanAdi=" + departmanAdi);
                }

                int sonSiparisMi = g.siparisListesi.size(), siparisSayisi = g.siparisListesi.size();

                for (int i = 0; i < siparisSayisi; i++) {
                    sonSiparisMi--;
                    g.commonAsyncTask.client.sendMessage("komut=siparis&masa=" + masaAdi + "&departmanAdi=" + departmanAdi + "&miktar=" + g.siparisListesi.get(0).siparisAdedi + "&yemekAdi=" + g.siparisListesi.get(0).siparisYemekAdi + "&siparisiGirenKisi=" + employee.Name + " " + employee.LastName + "&dusulecekDeger=" + g.siparisListesi.get(0).siparisFiyati + "&adisyonNotu=" + iptalNedeni.getText() + "&sonSiparisMi=" + sonSiparisMi + "&porsiyon=" + g.siparisListesi.get(0).siparisPorsiyonu);

                    boolean siparisVarMi = false;
                    for (Siparis anUrunListesiToplam : urunListesiToplam) {
                        if (anUrunListesiToplam.siparisYemekAdi.contentEquals(g.siparisListesi.get(0).siparisYemekAdi) && anUrunListesiToplam.siparisPorsiyonu == g.siparisListesi.get(0).siparisPorsiyonu) {
                            anUrunListesiToplam.siparisAdedi += g.siparisListesi.get(0).siparisAdedi;
                            siparisVarMi = true;
                            break;
                        }
                    }
                    if (!siparisVarMi) {
                        urunListesiToplam.add(g.siparisListesi.get(0));
                    }
                    toplamHesap +=g.siparisListesi.get(0).siparisAdedi * Double.parseDouble(g.siparisListesi.get(0).siparisFiyati);
                    g.siparisListesi.remove(g.siparisListesi.get(0));
                }
                adapterSecilenSiparisler.notifyDataSetChanged();
                adapterHesap.notifyDataSetChanged();

                hesapButton.setText("Hesap İste  /  Toplam = " + String.format("%.2f", toplamHesap) + " TL");
            }
        });
        alertDialog = aBuilder.create();

        buttonSepet.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
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
        if(!MasaKilitliMi)
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
    public boolean onContextItemSelected(MenuItem item) {
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
                    baslik = "İkram";
                    hint = "Kaç adet ürün ikram edilecek? Bulunan : " + urunListesiToplam.get(info.position).siparisAdedi + " adet";
                }
                else
                {
                    baslik = "İkram İptali";
                    hint = "Kaç adet ikram iptal edilecek? Bulunan : " + urunListesiToplam.get(info.position).siparisAdedi + " adet";
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
                            if (kacAdet > 0) {
                                //ürün ikram et

                            }
                            builder.dismiss();
                        } else // ürün iptali
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
                                if (kacAdet > 0) {
                                    //ürün iptal et
                                    String ikramMi;
                                    if (urunListesiToplam.get(info.position).siparisFiyati.contentEquals("ikram"))
                                        ikramMi = "0";//ikramsa 0 veya 1
                                    else
                                        ikramMi = "2";

                                    g.commonAsyncTask.client.sendMessage("komut=iptal&masa=" + masaAdi + "&departmanAdi=" + departmanAdi + "&miktar=" + kacAdet + "&yemekAdi=" + urunListesiToplam.get(info.position).siparisYemekAdi + "&siparisiGirenKisi=" + employee.Name + " " + employee.LastName + "&dusulecekDeger=" + urunListesiToplam.get(info.position).siparisFiyati + "&adisyonNotu=&ikramYeniMiEskiMi=" + ikramMi + "&porsiyon=" + urunListesiToplam.get(info.position).siparisPorsiyonu + "&iptalNedeni=" + iptalNedeni.getText());
                                    positiveButton.setEnabled(false);
                                    negativeButton.setEnabled(false);
                                } else {
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(rec);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(rec, new IntentFilter("myevent"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                case iptal:
                    if(collection.get("masa").contentEquals(masaAdi) && collection.get("departmanAdi").contentEquals(departmanAdi))
                    {
                        String yemekAdi = collection.get("yemekAdi");
                        Double fiyat = Double.parseDouble(collection.get("dusulecekDeger").replace(',','.'));
                        int ikramMi = Integer.parseInt(collection.get("ikramYeniMiEskiMi"));
                        Double porsiyon = Double.parseDouble(collection.get("porsiyon").replace(',','.'));
                        int miktar = Integer.parseInt(collection.get("miktar"));
                        for(int i=0;i<urunListesiToplam.size();i++)
                        {
                            if(urunListesiToplam.get(i).siparisYemekAdi.contentEquals(yemekAdi) && urunListesiToplam.get(i).siparisPorsiyonu == porsiyon)
                            {
                                urunListesiToplam.get(i).siparisAdedi -= miktar;

                                if (ikramMi == 2) // iptali istenilen ürün ikram değilse kalan hesaptan da düşülmeli
                                {
                                    toplamHesap -= (fiyat*miktar);
                                    hesapButton.setText("Hesap İste  /  Toplam = " + String.format("%.2f", toplamHesap) + " TL");
                                }

                                if (urunListesiToplam.get(i).siparisAdedi <= 0)
                                {
                                    urunListesiToplam.remove(i);
                                }
                                adapterHesap.notifyDataSetChanged();
                                builder.dismiss();
                                return;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        }
    };
}