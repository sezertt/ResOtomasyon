package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import Entity.Siparis;
import ekclasslar.UrunBilgileri;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private SparseArray<UrunBilgileri> groups;
    public LayoutInflater inflater;
    public Activity activity;
    MenuEkrani menuEkrani;
    String productCount;
    GlobalApplication g;

    ArrayList<String> porsiyonlarPozitif = new ArrayList<String>();
    ArrayList<String> porsiyonlarNegatif = new ArrayList<String>();

    public MyExpandableListAdapter(Activity act, SparseArray<UrunBilgileri> groups, MenuEkrani menuEkrani, GlobalApplication g) {
        activity = act;
        this.g = g;
        this.groups = groups;
        this.menuEkrani = menuEkrani;
        inflater = act.getLayoutInflater();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).productName.get(childPosition);
    }

    public Object getChild2(int groupPosition, int childPosition) {
        return groups.get(groupPosition).productPrice.get(childPosition);
    }

    public Object getChild3(int groupPosition, int childPosition) {
        return groups.get(groupPosition).productInfo.get(childPosition);
    }

    public Object getChild4(int groupPosition, int childPosition) {
        return groups.get(groupPosition).productCount.get(childPosition);
    }

    public Object getChild5(int groupPosition, int childPosition) {
        return groups.get(groupPosition).productPortion.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String productName = (String) getChild(groupPosition, childPosition);
        final String productPrice = ((String) getChild2(groupPosition, childPosition)).replaceAll(",", "\\.");
        final String productInfo = (String) getChild3(groupPosition, childPosition);
        productCount = (String) getChild4(groupPosition, childPosition);
        final Double productPortion = (Double)getChild5(groupPosition, childPosition);

        final TextView text;
        final ImageView image;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.urun_gorunumu, parent, false);
        }

        final TextView textFiyat = (TextView) convertView.findViewById(R.id.textViewChildPrice);
        textFiyat.setText(productPrice + " TL");

        text = (TextView) convertView.findViewById(R.id.textViewChildInfo);
        text.setText(productInfo);

        image = (ImageView) convertView.findViewById(R.id.imageView);
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/shared/Lenovo/Resimler/" + productName + ".png");
        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        image.setImageBitmap(bmp);
        final TextView textName = (TextView) convertView.findViewById(R.id.textViewChildHeader);
        textName.setText(productName);

        final TextView textAdet = (TextView) convertView.findViewById(R.id.textViewAdet);
        textAdet.setText(productCount);

        convertView.findViewById(R.id.buttonPlus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(productPortion == 0)
                    porsiyonEkle(groupPosition,childPosition,textAdet,textName,textFiyat,productPortion,1d);
                else
                {
                    int varMi = -1;
                    for(int i=0;i<g.birBucukPorsiyon.size();i++)
                    {
                        if(g.birBucukPorsiyon.get(i).yemekAdi.contentEquals(textName.getText()))
                        {
                            porsiyonlarPozitif.add("1.5 Porsiyon x" + g.birBucukPorsiyon.get(i).miktar);
                            varMi = i;
                            break;
                        }
                    }

                    if(varMi == -1)
                        porsiyonlarPozitif.add("1.5 Porsiyon ");

                    varMi = -1;

                    for(int i=0;i<g.tamPorsiyon.size();i++)
                    {
                        if(g.tamPorsiyon.get(i).yemekAdi.contentEquals(textName.getText()))
                        {
                            porsiyonlarPozitif.add("1 Porsiyon x" + g.tamPorsiyon.get(i).miktar);
                            varMi = i;
                            break;
                        }
                    }

                    if(varMi == -1)
                        porsiyonlarPozitif.add("1 Porsiyon ");

                    if(productPortion == 2)
                    {
                        varMi = -1;

                        for(int i=0;i<g.ucCeyrekPorsiyon.size();i++)
                        {
                            if(g.ucCeyrekPorsiyon.get(i).yemekAdi.contentEquals(textName.getText()))
                            {
                                porsiyonlarPozitif.add("0.75 Porsiyon x" + g.ucCeyrekPorsiyon.get(i).miktar);
                                varMi = i;
                                break;
                            }
                        }

                        if(varMi == -1)
                            porsiyonlarPozitif.add("0.75 Porsiyon ");
                    }

                    varMi = -1;

                    for(int i=0;i<g.yarimPorsiyon.size();i++)
                    {
                        if(g.yarimPorsiyon.get(i).yemekAdi.contentEquals(textName.getText()))
                        {
                            porsiyonlarPozitif.add("0.5 Porsiyon x" + g.yarimPorsiyon.get(i).miktar);
                            varMi = i;
                            break;
                        }
                    }

                    if(varMi == -1)
                        porsiyonlarPozitif.add("0.5 Porsiyon ");

                    if(productPortion == 2)
                    {
                        varMi = -1;

                        for(int i=0;i<g.ceyrekPorsiyon.size();i++)
                        {
                            if(g.ceyrekPorsiyon.get(i).yemekAdi.contentEquals(textName.getText()))
                            {
                                porsiyonlarPozitif.add("0.25 Porsiyon x" + g.ceyrekPorsiyon.get(i).miktar);
                                varMi = i;
                                break;
                            }
                        }

                        if(varMi == -1)
                            porsiyonlarPozitif.add("0.25 Porsiyon ");
                    }

                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1,porsiyonlarPozitif);

                    final AlertDialog alert = new AlertDialog.Builder(activity)
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

                                    porsiyonEkle(groupPosition, childPosition, textAdet, textName, textFiyat, productPortion, porsiyon);

                                    String [] urunBilgileri = porsiyonlarPozitif.get(item).split("x");

                                    int adet = 0;

                                    if(urunBilgileri.length > 1)
                                        adet = Integer.parseInt(urunBilgileri[1]);

                                    adet++;

                                    porsiyonlarPozitif.set(item, urunBilgileri[0] +"x" + adet);
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .create();

                    alert.show();
                }
            }
        });

        convertView.findViewById(R.id.buttonMinus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(productPortion == 0)
                    porsiyonCikar(groupPosition,childPosition,textAdet,textName,1d);
                else if(!textAdet.getText().toString().contentEquals("0"))
                {
                    if(g.birBucukPorsiyon.size()>0) // eğer önceden eklenmiş 1.5 porsiyon varsa
                    {
                        for (Siparis aBirBucukPorsiyon : g.birBucukPorsiyon) {
                            if (aBirBucukPorsiyon.yemekAdi.contentEquals(textName.getText())) {
                                porsiyonlarNegatif.add("-1.5 Porsiyon x" + aBirBucukPorsiyon.miktar);
                                break;
                            }
                        }
                    }

                    if(g.tamPorsiyon.size()>0)  // eğer önceden eklenmiş 1 porsiyon varsa
                    {
                        for (Siparis aTamPorsiyon : g.tamPorsiyon) {
                            if (aTamPorsiyon.yemekAdi.contentEquals(textName.getText())) {
                                porsiyonlarNegatif.add("-1 Porsiyon x" + aTamPorsiyon.miktar);
                                break;
                            }
                        }
                    }

                    if(g.ucCeyrekPorsiyon.size()>0) // eğer önceden eklenmiş 0.75 porsiyon varsa
                    {
                        for (Siparis anUcCeyrekPorsiyon : g.ucCeyrekPorsiyon) {
                            if (anUcCeyrekPorsiyon.yemekAdi.contentEquals(textName.getText())) {
                                porsiyonlarNegatif.add("-0.75 Porsiyon x" + anUcCeyrekPorsiyon.miktar);
                                break;
                            }
                        }
                    }

                    if(g.yarimPorsiyon.size()>0) // eğer önceden eklenmiş 0.5 porsiyon varsa
                    {
                        for (Siparis aYarimPorsiyon : g.yarimPorsiyon) {
                            if (aYarimPorsiyon.yemekAdi.contentEquals(textName.getText())) {
                                porsiyonlarNegatif.add("-0.5 Porsiyon x" + aYarimPorsiyon.miktar);
                                break;
                            }
                        }
                    }

                    if(g.ceyrekPorsiyon.size()>0)  // eğer önceden eklenmiş 0.25 porsiyon varsa
                    {
                        for (Siparis aCeyrekPorsiyon : g.ceyrekPorsiyon) {
                            if (aCeyrekPorsiyon.yemekAdi.contentEquals(textName.getText())) {
                                porsiyonlarNegatif.add("-0.25 Porsiyon x" + aCeyrekPorsiyon.miktar);
                                break;
                            }
                        }
                    }

                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1,porsiyonlarNegatif);

                    final AlertDialog alert = new AlertDialog.Builder(activity)
                            .setTitle("Porsiyon Seçiniz")
                            .setCancelable(false)
                            .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    // porsiyon seçimi tamamlanınca yapılacaklar
                                    porsiyonlarNegatif.clear();
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    Double porsiyon;
                                    try {
                                        porsiyon = Double.parseDouble(porsiyonlarNegatif.get(item).substring(0, 4));
                                    } catch (Exception ex) {
                                        porsiyon = -1d;
                                    }

                                    porsiyon *= -1;

                                    if (porsiyon == 0.7)
                                        porsiyon = 0.75;
                                    else if (porsiyon == 0.2)
                                        porsiyon = 0.25;
                                    porsiyonCikar(groupPosition, childPosition, textAdet, textName, porsiyon);

                                    String[] urunBilgileri = porsiyonlarNegatif.get(item).split("x");

                                    int adet = 1;

                                    if (urunBilgileri.length > 1)
                                        adet = (int)(Double.parseDouble(urunBilgileri[1]));

                                    adet--;

                                    if (adet != 0)
                                    {
                                        porsiyonlarNegatif.set(item, urunBilgileri[0] + "x" + adet);
                                    }
                                    else
                                    {
                                        porsiyonlarNegatif.remove(item);
                                    }
                                    adapter.notifyDataSetChanged();

                                    if(porsiyonlarNegatif.size() < 1)
                                    {
                                        dialog.dismiss();
                                    }
                                }
                            })
                            .create();

                    alert.show();
                }
            }
        });
        return convertView;
    }

    private void porsiyonCikar(int groupPosition, int childPosition, TextView textAdet, TextView textName, Double azaltmaMiktari)
    {
        if (!textAdet.getText().toString().contentEquals("0"))
        {
            if(azaltmaMiktari == 1)
            {
                // eğer varsa azalt
                porsiyonAzaltmaliMi(g.tamPorsiyon, textName.getText().toString());
            }
            else if(azaltmaMiktari == 0.5)
            {
                // eğer varsa azalt
                porsiyonAzaltmaliMi(g.yarimPorsiyon,textName.getText().toString());
            }
            else if(azaltmaMiktari == 0.25)
            {
                // eğer varsa azalt
                porsiyonAzaltmaliMi(g.ceyrekPorsiyon,textName.getText().toString());
            }
            else if(azaltmaMiktari == 1.5)
            {
                // eğer varsa azalt
                porsiyonAzaltmaliMi(g.birBucukPorsiyon,textName.getText().toString());
            }
            else
            {
                // eğer varsa azalt
                porsiyonAzaltmaliMi(g.ucCeyrekPorsiyon,textName.getText().toString());
            }

            for(int i = 0 ; i< menuEkrani.lstOrderedProducts.size();i++)
            {
                if(menuEkrani.lstOrderedProducts.get(i).yemekAdi.contentEquals(textName.getText().toString()))
                {
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    df.setMinimumFractionDigits(0);
                    df.setGroupingUsed(false);

                    String miktar = df.format(Double.parseDouble(textAdet.getText().toString()) - azaltmaMiktari);

                    if(Double.parseDouble(textAdet.getText().toString()) - azaltmaMiktari < 0)
                        miktar = "0";

                    if(miktar.contentEquals("0"))
                        menuEkrani.lstOrderedProducts.remove(i);
                    else
                        menuEkrani.lstOrderedProducts.get(i).miktar = miktar;
                    textAdet.setText(miktar);
                    groups.get(groupPosition).productCount.set(childPosition,miktar);
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    private void porsiyonAzaltmaliMi(ArrayList<Siparis> porsiyonArrayi, String yemekAdi)
    {
        for(int i=0;i<porsiyonArrayi.size();i++)
        {
            if(porsiyonArrayi.get(i).yemekAdi.contentEquals(yemekAdi))
            {
                porsiyonArrayi.get(i).miktar = String.valueOf(Double.parseDouble(porsiyonArrayi.get(i).miktar)-1);
                if(Double.parseDouble(porsiyonArrayi.get(i).miktar) == 0d)
                    porsiyonArrayi.remove(i);
                break;
            }
        }
    }

    private void porsiyonEkle(int groupPosition, int childPosition, TextView textAdet, TextView textName, TextView textFiyat,Double productPortion, Double arttirmaMiktari)
    {
        Siparis siparis = new Siparis();

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);

        String miktar = df.format(Double.parseDouble(textAdet.getText().toString()) + arttirmaMiktari);

        textAdet.setText(miktar);
        groups.get(groupPosition).productCount.set(childPosition,miktar);
        notifyDataSetChanged();

        if(arttirmaMiktari == 1d)
        {
            porsiyonArttir(g.tamPorsiyon,textName.getText().toString());
        }
        else if(arttirmaMiktari == 0.5)
        {
            porsiyonArttir(g.yarimPorsiyon,textName.getText().toString());
        }
        else if(arttirmaMiktari == 0.25)
        {
            porsiyonArttir(g.ceyrekPorsiyon,textName.getText().toString());
        }
        else if(arttirmaMiktari == 1.5)
        {
            porsiyonArttir(g.birBucukPorsiyon,textName.getText().toString());
        }
        else
        {
            porsiyonArttir(g.ucCeyrekPorsiyon,textName.getText().toString());
        }

        for(int i = 0 ; i< menuEkrani.lstOrderedProducts.size();i++) {
            if (menuEkrani.lstOrderedProducts.get(i).yemekAdi.contentEquals(textName.getText().toString())) {
                menuEkrani.lstOrderedProducts.get(i).miktar = miktar;
                return;
            }
        }

        siparis.porsiyonSinifi = productPortion;
        siparis.miktar = miktar;
        siparis.porsiyonFiyati = textFiyat.getText().toString().substring(0,textFiyat.getText().length() - 3);
        siparis.yemekAdi = textName.getText().toString();
        menuEkrani.lstOrderedProducts.add(siparis);
    }

    private void porsiyonArttir(ArrayList<Siparis> porsiyonArrayi,String yemekAdi)
    {
        Integer siparisVarMi= -1;
        for(int i=0;i<porsiyonArrayi.size();i++)
        {
            if(porsiyonArrayi.get(i).yemekAdi.contentEquals(yemekAdi))
            {
                siparisVarMi = i;
                break;
            }
        }
        if(siparisVarMi != -1) // siparis listede var
        {
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            df.setMinimumFractionDigits(0);
            df.setGroupingUsed(false);

            porsiyonArrayi.get(siparisVarMi).miktar = String.valueOf(df.format(Double.parseDouble(porsiyonArrayi.get(siparisVarMi).miktar) + 1));
        }
        else// listede yok
        {
            Siparis siparis = new Siparis();
            siparis.yemekAdi = yemekAdi;
            siparis.miktar = "1";
            porsiyonArrayi.add(siparis);
        }
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).productName.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.kategori_gorunumu, parent, false);
        }
        UrunBilgileri group = (UrunBilgileri) getGroup(groupPosition);

        CheckedTextView textGroupProductName;
        textGroupProductName = (CheckedTextView) convertView.findViewById(R.id.textViewProductGroupHeader);
        textGroupProductName.setText(group.productGroupName);
        textGroupProductName.setChecked(isExpanded);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}