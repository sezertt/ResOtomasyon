package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

public class MasaSecEkrani extends Activity {

    ArrayList<Departman> lstDepartmanlar;
    ArrayList<ArrayList<Departman>> asd;
    ArrayList<MasaDizayn> lstMasaDizayn;
    String[] masaPlanIsmi;
    ArrayList<String> masaIsimleri;

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
        DepartmanMasalari departmanMasalari = new DepartmanMasalari();
        for (int j = 0; j < lstDepartmanlar.size(); j++) {
            for (Departman departman : lstDepartmanlar) {
                if (departman.DepartmanEkrani.contentEquals(masaPlanIsmi[j])) {
                    masaIsimleri = new ArrayList<String>();
                    for (MasaDizayn masaDizayn : lstMasaDizayn) {
                        if (masaDizayn.MasaPlanAdi.contentEquals(departman.DepartmanEkrani)) {
                            masaIsimleri.add(masaDizayn.MasaAdi);
                        }
                    }
                    departmanMasalari.DepartmanWithMasa.add(masaIsimleri);
                }
            }
        }
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
        MasaExpandableListAdapter masaExpandableListAdapter = new MasaExpandableListAdapter(this, departmanMasalari.DepartmanWithMasa,lstDepartmanlar);
        listView.setAdapter(masaExpandableListAdapter);

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
