package ekclasslar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.res_otomasyon.resotomasyon.FragmentMasaEkrani;
import java.util.ArrayList;
import Entity.Departman;
import Entity.Employee;
import Entity.MasaDizayn;
import Entity.Menu;

public class CollectionPagerAdapter extends FragmentPagerAdapter {
    public ArrayList<Departman> lstDepartmanlar;
    public ArrayList<MasaDizayn> lstMasaDizayn;
    ArrayList<String> masaIsimleri;
    public String[] masaPlanIsmi;
    Fragment fragment;
    public Fragment[] fragments = null;
    public Employee employee = null;
    public String kilitliMasaAdi;
    public String kilitliDepartmanAdi;

    public static FragmentMasaEkrani newInstance(ArrayList<String> masalar, String DepartmanAdi, Employee employee, String kilitliMasaAdi, String kilitliDepartmanAdi, String departmanMenusu) {
        FragmentMasaEkrani myFragment = new FragmentMasaEkrani();
        Bundle args = new Bundle();
        args.putStringArrayList("masalar", masalar);
        args.putString("departmanAdi", DepartmanAdi);
        args.putString("kilitliDepartmanAdi", kilitliDepartmanAdi);
        args.putString("kilitliMasaAdi", kilitliMasaAdi);
        args.putSerializable("Employee", employee);
        args.putSerializable("departmanMenusu", departmanMenusu);
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
                            if (masaDizayn.MasaEkraniAdi.contentEquals(departman.DepartmanEkrani)) {
                                masaIsimleri.add(masaDizayn.MasaAdi);
                            }
                        }
                    }
                }
                fragment = newInstance(masaIsimleri, lstDepartmanlar.get(i).DepartmanAdi, employee, this.kilitliMasaAdi,
                        this.kilitliDepartmanAdi,lstDepartmanlar.get(i).DepartmanMenuAdi);
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
