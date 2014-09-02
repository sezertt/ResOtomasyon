package ekclasslar;

import com.res_otomasyon.resotomasyon.GlobalApplication;

import java.util.Dictionary;

import Entity.MasaninSiparisleri;
import Entity.Siparis;

/**
 * Created by Mustafa on 2.9.2014.
 */
public class BildirimBilgileriIslemler {
    Dictionary<String, String> collection;
    MasaninSiparisleri masaninSiparisleri;
    GlobalApplication g;
    Siparis siparis;

    public BildirimBilgileriIslemler(Dictionary<String, String> collection, GlobalApplication g) {
        this.g = g;
        this.collection = collection;
        masaninSiparisleri = new MasaninSiparisleri();
        siparis = new Siparis();
    }

    public void bildirimBilgileri() {
        String siparis = collection.get("bildirimBilgileri");

        String[] siparisLer = siparis.split("\\*");
        String[] siparisDetay;

    }

}
