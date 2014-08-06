package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

import Entity.Employee;
import Entity.Siparis;

public class HesapEkrani extends Activity {

    String departmanAdi, masaAdi;
    ArrayList<Employee> lstEmployee;
    ArrayList<Siparis> lstOrderedProducts = new ArrayList<Siparis>();
    SharedPreferences preferences = null;
    boolean MasaKilitliMi = false;
    MyListAdapter adapterSecilenSiparisler;
    ArrayList<Siparis> urunListesiToplam = new ArrayList<Siparis>();
    int selectedSiparisItemPosition = -1;

    Double kacPorsiyon, yemeginFiyati, toplamHesap = 0d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_hesap_ekrani);
        Bundle extras = getIntent().getExtras();
        departmanAdi = extras.getString("DepartmanAdi");
        masaAdi = extras.getString("MasaAdi");
        lstEmployee = (ArrayList<Employee>) extras.getSerializable("lstEmployees");
        lstOrderedProducts = (ArrayList<Siparis>) extras.getSerializable("lstOrderedProducts");

        preferences = this.getSharedPreferences("KilitliMasa", Context.MODE_PRIVATE);
        MasaKilitliMi = preferences.getBoolean("MasaKilitli", false);

        String yemeginAdi, Garson;
        Boolean ikramMi;

        ListView hesapListesi = (ListView) findViewById(R.id.listViewHesap);
        final ListView secilenSiparislerinListesi = (ListView) findViewById(R.id.listViewSecilenSiparisler);

        ArrayList<Siparis> urunListesi = new ArrayList<Siparis>();
        ArrayList<Siparis> urunListesiIkram = new ArrayList<Siparis>();

        Button hesapButton = (Button) findViewById(R.id.buttonHesap);
        Button buttonAzalt = (Button) findViewById(R.id.buttonAzalt);
        Button buttonArttir = (Button) findViewById(R.id.buttonArttir);
        final Button buttonPorsiyon = (Button) findViewById(R.id.buttonPorsiyon);

        final String siparisler = extras.getString("siparisler");

        if(siparisler != null)
        {
            String[] Siparisler = siparisler.split("\\*");

            for (int i = 0; i < Siparisler.length; i++) {
                String[] detaylari = Siparisler[i].split("-");

                NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
                Number number = null;

                //burası virgüllü stringi double a convert etme çevirme kısmı
                try {
                    number = format.parse(detaylari[0]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                yemeginFiyati = number.doubleValue();

                try {
                    number = format.parse(detaylari[1]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                kacPorsiyon = number.doubleValue();

                yemeginAdi = detaylari[2];
                ikramMi = Boolean.parseBoolean(detaylari[3]);
                Garson = detaylari[4];

                int gruptaYeniGelenSiparisVarMi = -1; // ürün hesapta bulunuyor mu ?

                if (!ikramMi) // ikram değilse
                {
                    for (int j = 0; j < urunListesi.size(); j++) {
                        if (yemeginAdi.contentEquals(urunListesi.get(j).yemekAdi)) // listede yemek var
                        {
                            gruptaYeniGelenSiparisVarMi = j;
                            break;
                        }
                    }

                    if (gruptaYeniGelenSiparisVarMi != -1) // listede yemek var
                    {
                        urunListesi.get(gruptaYeniGelenSiparisVarMi).miktar = (Double.parseDouble(urunListesi.get(gruptaYeniGelenSiparisVarMi).miktar) + kacPorsiyon) + "";
                    } else // listede yemek yok
                    {
                        Siparis yeniSiparis = new Siparis();
                        yeniSiparis.miktar = kacPorsiyon.toString();
                        yeniSiparis.yemekAdi = yemeginAdi;
                        yeniSiparis.porsiyonFiyati = String.format("%.2f", yemeginFiyati);
                        urunListesi.add(yeniSiparis);
                    }
                    toplamHesap += yemeginFiyati * kacPorsiyon;
                } else // ikramsa
                {
                    for (int j = 0; j < urunListesiIkram.size(); j++) {
                        if (yemeginAdi.contentEquals(urunListesiIkram.get(j).yemekAdi)) // listede yemek var
                        {
                            gruptaYeniGelenSiparisVarMi = j;
                            break;
                        }
                    }

                    if (gruptaYeniGelenSiparisVarMi != -1) // listede yemek var
                    {
                        urunListesiIkram.get(gruptaYeniGelenSiparisVarMi).miktar = (Double.parseDouble(urunListesiIkram.get(gruptaYeniGelenSiparisVarMi).miktar) + kacPorsiyon) + "";
                    } else // listede yemek yok
                    {
                        Siparis yeniSiparis = new Siparis();
                        yeniSiparis.miktar = kacPorsiyon.toString();
                        yeniSiparis.yemekAdi = yemeginAdi;
                        yeniSiparis.porsiyonFiyati = "ikram";
                        urunListesiIkram.add(yeniSiparis);
                    }
                }
            }
            urunListesiToplam.addAll(urunListesi);
            urunListesiToplam.addAll(urunListesiIkram);

            hesapButton.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                // BURASI YAPILACAK HESAP İSTEME BUTONU
                }
            });

            MyListAdapter adapterHesap = new MyListAdapter(urunListesiToplam, this,true);
            hesapListesi.setAdapter(adapterHesap);

            hesapButton.setText("Hesap İste  /  Toplam = " + toplamHesap + " TL");
        }
        else
        {
            hesapButton.setText("Hesap İste  /  Toplam = 0 TL");
            hesapButton.setEnabled(false);
        }

        adapterSecilenSiparisler = new MyListAdapter(lstOrderedProducts,this,false);
        secilenSiparislerinListesi.setAdapter(adapterSecilenSiparisler);

        registerForContextMenu(hesapListesi);

        secilenSiparislerinListesi.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                selectedSiparisItemPosition = position;
                adapterSecilenSiparisler.setSelectedIndex(position);
                if(lstOrderedProducts.get(position).porsiyonSinifi == 0)
                {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) buttonPorsiyon.getLayoutParams();
                    params.width = 0;
                    params.leftMargin = 0;
                    buttonPorsiyon.setLayoutParams(params);
                }
                else
                {
                    Resources r = HesapEkrani.this.getResources();
                    int px = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            60,
                            r.getDisplayMetrics()
                    );

                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) buttonPorsiyon.getLayoutParams();

                    params.leftMargin = px/12;
                    params.width = px;
                    buttonPorsiyon.setLayoutParams(params);
                }
            }
        });

        buttonPorsiyon.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
               if(buttonPorsiyon.getText().equals("Tam"))
               {
                   buttonPorsiyon.setText("Yar");
               }
               else if(buttonPorsiyon.getText().equals("Yar"))
               {
                   if(lstOrderedProducts.get(selectedSiparisItemPosition).porsiyonSinifi == 1)
                       buttonPorsiyon.setText("Tam");
                   else
                       buttonPorsiyon.setText("Çey");
               }
               else
               {
                   buttonPorsiyon.setText("Tam");
               }
            }
        });

        buttonArttir.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if(selectedSiparisItemPosition != -1)
                {
                    Double porsiyonSinifi=1d;
                    if(buttonPorsiyon.getText().equals("Yar"))
                        porsiyonSinifi = 0.5;
                    else if (buttonPorsiyon.getText().equals("Çey"))
                        porsiyonSinifi = 0.25;
                    lstOrderedProducts.get(selectedSiparisItemPosition).miktar = String.format("%.2f", Double.parseDouble(lstOrderedProducts.get(selectedSiparisItemPosition).miktar) + porsiyonSinifi);
                    adapterSecilenSiparisler.notifyDataSetChanged();
                }
            }
        });

        buttonAzalt.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if(selectedSiparisItemPosition != -1)
                {
                    Double porsiyonSinifi=1d;
                    if(buttonPorsiyon.getText().equals("Yar"))
                        porsiyonSinifi = 0.5;
                    else if (buttonPorsiyon.getText().equals("Çey"))
                        porsiyonSinifi = 0.25;

                    lstOrderedProducts.get(selectedSiparisItemPosition).miktar = String.format("%.2f", Double.parseDouble(lstOrderedProducts.get(selectedSiparisItemPosition).miktar) - porsiyonSinifi);
                    if(Double.parseDouble(lstOrderedProducts.get(selectedSiparisItemPosition).miktar) < 0)
                        lstOrderedProducts.get(selectedSiparisItemPosition).miktar = "0.00";

                    if(lstOrderedProducts.get(selectedSiparisItemPosition).miktar.contentEquals("0.00"))
                    {
                        lstOrderedProducts.remove(selectedSiparisItemPosition);
                        if(selectedSiparisItemPosition == lstOrderedProducts.size())
                        {
                            selectedSiparisItemPosition = lstOrderedProducts.size() - 1;
                            adapterSecilenSiparisler.setSelectedIndex(lstOrderedProducts.size() - 1);
                        }
                        else
                        {
                            adapterSecilenSiparisler.notifyDataSetChanged();
                        }
                    }
                    else
                    {
                        adapterSecilenSiparisler.notifyDataSetChanged();
                    }
                    if(lstOrderedProducts.size() > 0)
                    {
                        if(lstOrderedProducts.get(selectedSiparisItemPosition).porsiyonSinifi == 0)
                        {
                            buttonPorsiyon.setText("Tam");
                            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) buttonPorsiyon.getLayoutParams();
                            params.width = 0;
                            params.leftMargin = 0;
                            buttonPorsiyon.setLayoutParams(params);
                        }
                        else
                        {
                            Resources r = HesapEkrani.this.getResources();
                            int px = (int) TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    60,
                                    r.getDisplayMetrics()
                            );

                            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) buttonPorsiyon.getLayoutParams();

                            params.leftMargin = px/12;
                            params.width = px;
                            buttonPorsiyon.setLayoutParams(params);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(!MasaKilitliMi)
        {
            if (v.getId()==R.id.listViewHesap) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.menu_urun, menu);

                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
                menu.setHeaderTitle(urunListesiToplam.get(info.position).yemekAdi);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.iptal:
                // burada alertdialogla kaç adet olması gerektiğini sor
                return true;
            case R.id.ikram:
                // burada alertdialogla kaç adet olması gerektiğini sor
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
