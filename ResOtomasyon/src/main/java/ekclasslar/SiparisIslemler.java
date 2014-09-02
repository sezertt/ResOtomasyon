package ekclasslar;

import android.app.Activity;
import android.support.v4.view.ViewPager;

import com.res_otomasyon.resotomasyon.FragmentMasaEkrani;
import com.res_otomasyon.resotomasyon.GlobalApplication;
import com.res_otomasyon.resotomasyon.MasaEkrani;
import com.res_otomasyon.resotomasyon.R;

import java.util.Dictionary;
import java.util.Hashtable;

import Entity.MasaninSiparisleri;
import Entity.Siparis;

import static android.provider.Settings.Global.getString;

/**
 * Created by Mustafa on 1.9.2014.
 */
public class SiparisIslemler {

    Dictionary<String, String> collection;
    Siparis siparis;
    MasaninSiparisleri masaninSiparisleri;
    GlobalApplication g;

    public SiparisIslemler(Dictionary<String, String> collection, GlobalApplication g) {
        this.collection = collection;
        siparis = new Siparis();
        this.g = g;
        masaninSiparisleri = new MasaninSiparisleri();
    }

    public void Islem() {
        String gelenkomut = collection.get("komut");
        GlobalApplication.Komutlar komut = GlobalApplication.Komutlar.valueOf(gelenkomut);
        switch (komut) {
            case siparis:
                siparis = new Siparis();
                masaninSiparisleri = new MasaninSiparisleri();
                masaninSiparisleri.DepartmanAdi = collection.get("departmanAdi");
                masaninSiparisleri.MasaAdi = collection.get("masa");
                siparis.siparisAdedi = collection.get("miktar");
                siparis.siparisYemekAdi = collection.get("yemekAdi");
                if (g.lstMasaninSiparisleri.size() > 0) {
                    boolean ayniMasaVarMi = false;
                    for (MasaninSiparisleri msp : g.lstMasaninSiparisleri) {
                        if (msp.DepartmanAdi.contentEquals(collection.get("departmanAdi")) && msp.MasaAdi.contentEquals(collection.get("masa"))) {
                            msp.siparisler.add(siparis);
                            ayniMasaVarMi = true;
                            break;
                        }
                    }
                    if (!ayniMasaVarMi) {
                        masaninSiparisleri.siparisler.add(siparis);
                        g.lstMasaninSiparisleri.add(masaninSiparisleri);
                    }
                } else {
                    masaninSiparisleri.siparisler.add(siparis);
                    g.lstMasaninSiparisleri.add(masaninSiparisleri);
                }

            case bildirim:
                break;
        }
    }
}
