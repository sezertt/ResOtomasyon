package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.res_otomasyon.resotomasyon.R;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import Entity.Departman;
import Entity.MasaDizayn;
import XMLReader.ReadXML;
import ekclasslar.DepartmanMasalari;
import ekclasslar.FileIO;

public class MasaSecEkrani extends Activity {

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
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
        final MasaExpandableListAdapter masaExpandableListAdapter = new MasaExpandableListAdapter(this, dptMasalar, lstDepartmanlar);
        listView.setAdapter(masaExpandableListAdapter);
        listView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String dAdi = lstDepartmanlar.get(groupPosition).DepartmanAdi;
                String masaAdi = dptMasalar.get(groupPosition).Masalar.get(childPosition);
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
}
