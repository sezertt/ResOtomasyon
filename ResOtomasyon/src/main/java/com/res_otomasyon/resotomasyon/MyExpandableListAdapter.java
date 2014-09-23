package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

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

    Dictionary<String,Bitmap> bitmapDictionary = new Hashtable<String, Bitmap>();

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
        return groups.get(groupPosition).productPortionClass.get(childPosition);
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
            convertView = inflater.inflate(R.layout.expandable_urun_gorunumu, parent, false);
        }

        final TextView textFiyat = (TextView) convertView.findViewById(R.id.textViewChildPrice);
        textFiyat.setText(productPrice + " TL");

        text = (TextView) convertView.findViewById(R.id.textViewChildInfo);
        text.setText(productInfo);

        image = (ImageView) convertView.findViewById(R.id.imageView);
//        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/shared/Lenovo/Resimler/" + productName + ".png");
//        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        if(bitmapDictionary != null && (!productName.isEmpty() && productName != null))
            image.setImageBitmap(bitmapDictionary.get(productName));

        final TextView textName = (TextView) convertView.findViewById(R.id.textViewChildHeader);
        textName.setText(productName);

        final TextView textAdet = (TextView) convertView.findViewById(R.id.textViewAdet);
        textAdet.setText(productCount);

        convertView.findViewById(R.id.buttonPlus).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                porsiyonEkle(groupPosition, childPosition, textAdet, textName, textFiyat, productPortion, 2d);
                return false;
            }
        });

        convertView.findViewById(R.id.buttonPlus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (productPortion == 0)
                    porsiyonEkle(groupPosition, childPosition, textAdet, textName, textFiyat, productPortion, 1d);
                else {
                    int[] selectedSiparisItemPosition = {-1, -1, -1, -1, -1}; // 0-1.5 porsiyon --- 1-1 porsiyon --- 2-0.75 porsiyon --- 3-0.5 porsiyon --- 4-0.25 porsiyon
                    for (int i = 0; i < g.siparisListesi.size(); i++) {
                        if (g.siparisListesi.get(i).siparisYemekAdi.contentEquals(textName.getText())) {
                            if (g.siparisListesi.get(i).siparisPorsiyonu == 1.5d) {
                                selectedSiparisItemPosition[0] = i;
                            } else if (g.siparisListesi.get(i).siparisPorsiyonu == 1) {
                                selectedSiparisItemPosition[1] = i;
                            } else if (g.siparisListesi.get(i).siparisPorsiyonu == 0.75d) {
                                selectedSiparisItemPosition[2] = i;
                            } else if (g.siparisListesi.get(i).siparisPorsiyonu == 0.5d) {
                                selectedSiparisItemPosition[3] = i;
                            } else if (g.siparisListesi.get(i).siparisPorsiyonu == 0.25d) {
                                selectedSiparisItemPosition[4] = i;
                            }
                        }
                    }

                    for (int i = 0; i < selectedSiparisItemPosition.length; i++) {
                        if (groups.get(groupPosition).productPortionClass.get(childPosition) == 1d && (i == 2 || i == 4)) // ürün yarım porsiyonluk ise 0.25 ve 0.75 eklenmemeli
                            continue; // eğer ürünün çeyrek porsiyon özelliği yoksa, sipariş porsiyonuna 0.75 ve 0.25 eklenmesin

                        if (selectedSiparisItemPosition[i] != -1) {
                            switch (i) {
                                case 0:
                                    porsiyonlarPozitif.add("1.5 Porsiyon x" + g.siparisListesi.get(selectedSiparisItemPosition[i]).siparisAdedi);
                                    break;
                                case 1:
                                    porsiyonlarPozitif.add("1 Porsiyon x" + g.siparisListesi.get(selectedSiparisItemPosition[i]).siparisAdedi);
                                    break;
                                case 2:
                                    porsiyonlarPozitif.add("0.75 Porsiyon x" + g.siparisListesi.get(selectedSiparisItemPosition[i]).siparisAdedi);
                                    break;
                                case 3:
                                    porsiyonlarPozitif.add("0.5 Porsiyon x" + g.siparisListesi.get(selectedSiparisItemPosition[i]).siparisAdedi);
                                    break;
                                case 4:
                                    porsiyonlarPozitif.add("0.25 Porsiyon x" + g.siparisListesi.get(selectedSiparisItemPosition[i]).siparisAdedi);
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            switch (i) {
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

                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, porsiyonlarPozitif);

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

                                    String[] urunBilgileri = porsiyonlarPozitif.get(item).split("x");

                                    int adet = 0;

                                    if (urunBilgileri.length > 1)
                                        adet = Integer.parseInt(urunBilgileri[1]);

                                    adet++;

                                    porsiyonlarPozitif.set(item, urunBilgileri[0] + "x" + adet);
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .create();

                    alert.show();
                }
            }
        });

        convertView.findViewById(R.id.buttonMinus).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                porsiyonCikar(groupPosition, childPosition, textAdet, textName, 2d);
                return false;
            }
        });

        convertView.findViewById(R.id.buttonMinus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(productPortion == 0)
                    porsiyonCikar(groupPosition,childPosition,textAdet,textName,1d);
                else if(Double.parseDouble(textAdet.getText().toString())>0)
                {
                    int [] selectedSiparisItemPosition = {-1,-1,-1,-1,-1}; // 0-1.5 porsiyon --- 1-1 porsiyon --- 2-0.75 porsiyon --- 3-0.5 porsiyon --- 4-0.25 porsiyon

                    for(int i=0; i < g.siparisListesi.size(); i++)
                    {
                        if(g.siparisListesi.get(i).siparisYemekAdi.contentEquals(textName.getText()))
                        {
                            if (g.siparisListesi.get(i).siparisPorsiyonu == 1.5d) {
                                selectedSiparisItemPosition[0] = i;
                            }
                            else if (g.siparisListesi.get(i).siparisPorsiyonu == 1) {
                                selectedSiparisItemPosition[1] = i;
                            }
                            else if (g.siparisListesi.get(i).siparisPorsiyonu == 0.75d) {
                                selectedSiparisItemPosition[2] = i;
                            }
                            else if (g.siparisListesi.get(i).siparisPorsiyonu == 0.5d) {
                                selectedSiparisItemPosition[3] = i;
                            }
                            else if (g.siparisListesi.get(i).siparisPorsiyonu == 0.25d) {
                                selectedSiparisItemPosition[4] = i;
                            }
                        }
                    }

                    for(int i = 0; i < selectedSiparisItemPosition.length; i++)
                    {
                        if (groups.get(groupPosition).productPortionClass.get(childPosition) == 1d && (i == 2 || i == 4)) // ürün yarım porsiyonluk ise 0.25 ve 0.75 eklenmemeli
                            continue; // eğer ürünün çeyrek porsiyon özelliği yoksa, sipariş porsiyonuna 0.75 ve 0.25 eklenmesin

                        if(selectedSiparisItemPosition[i] != -1)
                        {
                            switch (i)
                            {
                                case 0:
                                    porsiyonlarNegatif.add("-1.5 Porsiyon x" + g.siparisListesi.get(selectedSiparisItemPosition[i]).siparisAdedi);
                                    break;
                                case 1:
                                    porsiyonlarNegatif.add("-1 Porsiyon x" + g.siparisListesi.get(selectedSiparisItemPosition[i]).siparisAdedi);
                                    break;
                                case 2:
                                    porsiyonlarNegatif.add("-0.75 Porsiyon x" + g.siparisListesi.get(selectedSiparisItemPosition[i]).siparisAdedi);
                                    break;
                                case 3:
                                    porsiyonlarNegatif.add("-0.5 Porsiyon x" + g.siparisListesi.get(selectedSiparisItemPosition[i]).siparisAdedi);
                                    break;
                                case 4:
                                    porsiyonlarNegatif.add("-0.25 Porsiyon x" + g.siparisListesi.get(selectedSiparisItemPosition[i]).siparisAdedi);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, porsiyonlarNegatif);

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
            for(int i = 0 ; i< g.siparisListesi.size();i++)
            {
                if(g.siparisListesi.get(i).siparisYemekAdi.contentEquals(textName.getText().toString()) && g.siparisListesi.get(i).siparisPorsiyonu == azaltmaMiktari)
                {
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    df.setMinimumFractionDigits(0);
                    df.setGroupingUsed(false);

                    String miktar = df.format(Double.parseDouble(textAdet.getText().toString()) - azaltmaMiktari);

                    textAdet.setText(miktar);
                    groups.get(groupPosition).productCount.set(childPosition,miktar);
                    notifyDataSetChanged();

                    g.siparisListesi.get(i).siparisAdedi --;
                    if(g.siparisListesi.get(i).siparisAdedi <= 0)
                        g.siparisListesi.remove(i);
                    break;
                }
            }
        }
    }

    private void porsiyonEkle(int groupPosition, int childPosition, TextView textAdet, TextView textName, TextView textFiyat,Double productPortionClass, Double porsiyon)
    {
        Siparis siparis = new Siparis();

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);

        String miktar = df.format(Double.parseDouble(textAdet.getText().toString()) + porsiyon);

        textAdet.setText(miktar);
        groups.get(groupPosition).productCount.set(childPosition,miktar);
        notifyDataSetChanged();

        for(int i = 0 ; i< g.siparisListesi.size();i++) {
            if (g.siparisListesi.get(i).siparisYemekAdi.contentEquals(textName.getText().toString())&& g.siparisListesi.get(i).siparisPorsiyonu == porsiyon) {
                g.siparisListesi.get(i).siparisAdedi ++;
                return;
            }
        }
        siparis.siparisPorsiyonSinifi = productPortionClass;
        siparis.siparisAdedi = 1;
        siparis.siparisFiyati = String.valueOf(Double.parseDouble(textFiyat.getText().toString().substring(0, textFiyat.getText().length() - 3)) * porsiyon);
        siparis.siparisYemekAdi = textName.getText().toString();
        siparis.siparisPorsiyonu = porsiyon;
        g.siparisListesi.add(siparis);
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