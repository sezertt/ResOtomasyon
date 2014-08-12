package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;

import com.res_otomasyon.resotomasyon.R;

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
    ArrayList<ArrayList<Departman>> asd;
    ArrayList<MasaDizayn> lstMasaDizayn;
    String[] masaPlanIsmi;
    ArrayList<DepartmanMasalari> dptMasalar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masa_sec_ekrani);
        FileIO fileIO = new FileIO();
        List<File> files;
        files = fileIO.getListFiles(new File("/mnt/sdcard/shared/Lenovo"));
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
                        if (masaDizayn.MasaPlanAdi.contentEquals(departman.DepartmanEkrani)) {
                            departmanMasalari.Masalar.add(masaDizayn.MasaAdi);
                            departmanMasalari.mDurumu.add(false);
                        }
                    }
                }
            }
            dptMasalar.add(departmanMasalari);
        }

        Button btnMasaKaydet = (Button) findViewById(R.id.masaKaydet);
        btnMasaKaydet.setOnClickListener(this);
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
        final MasaExpandableListAdapter masaExpandableListAdapter = new MasaExpandableListAdapter(this, dptMasalar, lstDepartmanlar);
        listView.setAdapter(masaExpandableListAdapter);
        listView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.masaKaydet:
                GlobalApplication g = (GlobalApplication) getApplicationContext();
                ArrayList<DepartmanMasalari> secilenMasalar = new ArrayList<DepartmanMasalari>();
                int masaSayac = 0;
                int dptSayac=0;
                for (DepartmanMasalari dpt : dptMasalar) {
                    DepartmanMasalari departmanMasalari = new DepartmanMasalari();
                    masaSayac = 0;
                    for (String masa : dpt.Masalar) {
                        if (dpt.mDurumu.get(masaSayac)) {
                            departmanMasalari.Masalar.add(masa);
                            departmanMasalari.mDurumu.add(true);
                        }
                        masaSayac++;
                    }
                    departmanMasalari.DepartmanAdi = dpt.DepartmanAdi;
                    dptSayac++;
                    secilenMasalar.add(departmanMasalari);
                }
                g.secilenMasalar = secilenMasalar;
                break;
        }
    }
}
