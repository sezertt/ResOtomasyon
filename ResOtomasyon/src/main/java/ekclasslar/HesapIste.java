package ekclasslar;

import com.res_otomasyon.resotomasyon.GlobalApplication;

import java.util.Dictionary;

import Entity.MasaninSiparisleri;
import Entity.Siparis;

/**
 * Created by Mustafa on 9.9.2014.
 */
public class HesapIste {
    Dictionary<String, String> collection;
    Siparis siparis;
    MasaninSiparisleri masaninSiparisleri;
    GlobalApplication g;

    public HesapIste(Dictionary<String, String> collection, GlobalApplication g) {
        this.collection = collection;
        siparis = new Siparis();
        this.g = g;
        masaninSiparisleri = new MasaninSiparisleri();
    }

    public boolean Islem() {
        boolean secilenMasaMi = false;
        siparis = new Siparis();
        masaninSiparisleri.DepartmanAdi = collection.get("departmanAdi");
        masaninSiparisleri.MasaAdi = collection.get("masa");
        siparis.siparisYemekAdi = "Hesap İsteği";
        if (g.secilenMasalar.size() > 0) {
            for (DepartmanMasalari dptMasa : g.secilenMasalar) {
                for (String masa : dptMasa.Masalar) {
                    if (masa.contentEquals(collection.get("masa")) && dptMasa.DepartmanAdi.contentEquals(collection.get("departmanAdi"))) {
                        if (g.lstMasaninSiparisleri.size() > 0) {
                            boolean ayniMasaVarMi = false;
                            for (MasaninSiparisleri msp : g.lstMasaninSiparisleri) {
                                if (msp.DepartmanAdi.contentEquals(collection.get("departmanAdi")) && msp.MasaAdi.contentEquals(collection.get("masa"))) {
                                    for (Siparis siparis1 : msp.siparisler) {
                                        if (siparis1.siparisYemekAdi.contentEquals("Hesap İsteği")) {
                                            return false;
                                        }
                                    }
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
                        secilenMasaMi = true;
                    }
                }
            }
        } else {
            if (g.lstMasaninSiparisleri.size() > 0) {
                boolean ayniMasaVarMi = false;
                for (MasaninSiparisleri msp : g.lstMasaninSiparisleri) {
                    if (msp.DepartmanAdi.contentEquals(collection.get("departmanAdi")) && msp.MasaAdi.contentEquals(collection.get("masa"))) {
                        for (Siparis siparis1 : msp.siparisler) {
                            if (siparis1.siparisYemekAdi.contentEquals("Hesap İsteği")) {
//                                garsonIstegiMevcut = true;
                                return false;
                            }
                        }
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
            secilenMasaMi = true;
        }
        return secilenMasaMi;
    }
}
