package com.res_otomasyon.resotomasyon;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import Entity.Departman;
import Entity.Employee;
import Entity.MasaDizayn;

/**
 * Created by Mustafa on 10.6.2014.
 */
public class CollectionPagerAdapter extends FragmentPagerAdapter {
    public ArrayList<Departman> lstDepartmanlar;
    public ArrayList<MasaDizayn> lstMasaDizayn;
    ArrayList<String> masaIsimleri;
    public String[] masaPlanIsmi;
    Fragment fragment;
    public Fragment[] fragments = null;
    public ArrayList<Employee> lstEmployees = null;

    public static FragmentMasaDesign newInstance(ArrayList<String> masalar, String DepartmanAdi,
                                                 ArrayList<Employee> lstEmployees) {
        FragmentMasaDesign myFragment = new FragmentMasaDesign();
        Bundle args = new Bundle();
        args.putStringArrayList("masalar", masalar);
        args.putString("departmanAdi", DepartmanAdi);
        args.putSerializable("lstEmp",lstEmployees);
        myFragment.setArguments(args);
        return myFragment;
    }

    public CollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        fragment = null;
        if (fragments == null)
            fragments = new Fragment[lstDepartmanlar.size()];
        for (int j = 0; j < lstDepartmanlar.size(); j++) {
            if (i == j) {
                for (Departman departman : lstDepartmanlar) {
                    if (departman.DepartmanEkrani.contentEquals(masaPlanIsmi[i])) {
                        masaIsimleri = new ArrayList<String>();
                        for (MasaDizayn masaDizayn : lstMasaDizayn) {
                            if (masaDizayn.MasaPlanAdi.contentEquals(departman.DepartmanEkrani)) {
                                masaIsimleri.add(masaDizayn.MasaAdi);
                            }
                        }
                    }
                }
                fragment = newInstance(masaIsimleri, lstDepartmanlar.get(i).DepartmanAdi,lstEmployees);
            }
        }
        fragments[i] = fragment;
        return fragment;
    }

    @Override
    public int getCount() {
        return lstDepartmanlar.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }
}
