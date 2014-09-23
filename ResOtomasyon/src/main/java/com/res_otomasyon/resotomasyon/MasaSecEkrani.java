package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Entity.Departman;
import Entity.MasaDizayn;
import XMLReader.ReadXML;
import ekclasslar.DepartmanMasalari;
import ekclasslar.FileIO;

public class MasaSecEkrani extends Activity implements View.OnClickListener {

    ArrayList<Departman> lstDepartmanlar;
    ArrayList<MasaDizayn> lstMasaDizayn;
    String[] masaPlanIsmi;
    ArrayList<DepartmanMasalari> dptMasalar;
    GlobalApplication g;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        g = (GlobalApplication) getApplicationContext();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masa_sec_ekrani);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FileIO fileIO = new FileIO();
        List<File> files;
        files = fileIO.getListFiles(new File(Environment.getExternalStorageDirectory().getPath() + "/shared/Lenovo"));
        ReadXML readXML = new ReadXML();
        lstDepartmanlar = readXML.readDepartmanlar(files);
        lstMasaDizayn = readXML.readMasaDizayn(files);
        this.masaPlanIsmi = readXML.masaPlanIsimleri;
        dptMasalar = new ArrayList<DepartmanMasalari>();
        for (int j = 0; j < lstDepartmanlar.size(); j++) {
            DepartmanMasalari departmanMasalari = new DepartmanMasalari();
            departmanMasalari.DepartmanAdi = lstDepartmanlar.get(j).DepartmanAdi;
            for (Departman departman : lstDepartmanlar) {
                if (departman.DepartmanEkrani.contentEquals(masaPlanIsmi[j])) {
                    departmanMasalari.Masalar = new ArrayList<String>();
                    departmanMasalari.mDurumu = new ArrayList<Boolean>();
                    for (MasaDizayn masaDizayn : lstMasaDizayn) {
                        if (masaDizayn.MasaEkraniAdi.contentEquals(departman.DepartmanEkrani)) {
                            departmanMasalari.Masalar.add(masaDizayn.MasaAdi);
                            departmanMasalari.mDurumu.add(false);
                        }
                    }
                }
            }
            dptMasalar.add(departmanMasalari);
        }
        if (g.secilenMasalar.size() > 0) {

            for (int i=0; i < g.secilenMasalar.size(); i++)
            {
                for (int j = 0; j < g.secilenMasalar.get(i).Masalar.size(); j++)
                {
                    for (int k =0; k < dptMasalar.get(i).Masalar.size();k++)
                    {
                        if(g.secilenMasalar.get(i).Masalar.get(j).contentEquals(dptMasalar.get(i).Masalar.get(k)))
                        {
                            dptMasalar.get(i).mDurumu.set(k,true);
                        }
                    }
                }
            }
        }

        Button btnMasaKaydet = (Button) findViewById(R.id.masaKaydet);
        btnMasaKaydet.setOnClickListener(this);
        ExpandableListView expandableListViewMasaSec = (ExpandableListView) findViewById(R.id.expandableListViewMasaSec);
        final MasaExpandableListAdapter masaExpandableListAdapter = new MasaExpandableListAdapter(this, dptMasalar, lstDepartmanlar, g);
        expandableListViewMasaSec.setAdapter(masaExpandableListAdapter);
        expandableListViewMasaSec.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
        expandableListViewMasaSec.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (!dptMasalar.get(groupPosition).mDurumu.get(childPosition)) {
                    dptMasalar.get(groupPosition).mDurumu.set(childPosition, true);

                } else {
                    dptMasalar.get(groupPosition).mDurumu.set(childPosition, false);
                }
                masaExpandableListAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.masa_sec_ekrani, menu);
        return true;
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.masaKaydet:
                ArrayList<DepartmanMasalari> secilenMasalar = new ArrayList<DepartmanMasalari>();
                for (DepartmanMasalari dpt : dptMasalar) {
                    DepartmanMasalari departmanMasalari = new DepartmanMasalari();
                    for (int j=0 ; j < dpt.Masalar.size();j++) {
                        if (dpt.mDurumu.get(j)) {
                            departmanMasalari.Masalar.add(dpt.Masalar.get(j));
                            departmanMasalari.mDurumu.add(true);
                        }
                    }
                    if(departmanMasalari.Masalar.size() >0) {
                        departmanMasalari.DepartmanAdi = dpt.DepartmanAdi;
                        secilenMasalar.add(departmanMasalari);
                    }
                }
                g.secilenMasalar = secilenMasalar;
                AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);
                aBuilder.setTitle("Kayıt Başarılı");
                aBuilder.setMessage("Seçtiğiniz masalar kayıt edildi.")
                        .setCancelable(false)
                        .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MasaSecEkrani.this.finish();
                            }
                        });
                AlertDialog alertDialog = aBuilder.create();
                alertDialog.show();
                break;
            default:
                break;
        }
    }
}
