package ekclasslar;

import com.res_otomasyon.resotomasyon.GlobalApplication;

import java.util.Dictionary;

import Entity.MasaninSiparisleri;
import Entity.Siparis;

/**
 * Created by Mustafa on 9.9.2014.
 */
public class MasaTemizleIslemler {

    Dictionary<String, String> collection;
    Siparis siparis;
    MasaninSiparisleri masaninSiparisleri;
    GlobalApplication g;

    public MasaTemizleIslemler(Dictionary<String, String> collection, GlobalApplication g) {
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
        siparis.siparisYemekAdi = "Masa Temizleme İsteği";
        if (g.secilenMasalar.size() > 0) {
            for (DepartmanMasalari dptMasa : g.secilenMasalar) {
                for (String masa : dptMasa.Masalar) {
                    if (masa.contentEquals(collection.get("masa")) && dptMasa.DepartmanAdi.contentEquals(collection.get("departmanAdi"))) {
                        if (g.lstMasaninSiparisleri.size() > 0) {
                            boolean ayniMasaVarMi = false;
                            for (MasaninSiparisleri msp : g.lstMasaninSiparisleri) {
                                if (msp.DepartmanAdi.contentEquals(collection.get("departmanAdi")) && msp.MasaAdi.contentEquals(collection.get("masa"))) {
                                    for (Siparis siparis1 : msp.siparisler) {
                                        if (siparis1.siparisYemekAdi.contentEquals("Masa Temizleme İsteği")) {
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
                            if (siparis1.siparisYemekAdi.contentEquals("Masa Temizleme İsteği")) {
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
    public boolean Goruldu()
    {
        boolean silindi = false;
        for (int j=0 ; j < g.lstMasaninSiparisleri.size() ; j++) {
            if (g.lstMasaninSiparisleri.get(j).DepartmanAdi.contentEquals(collection.get("departmanAdi")) && g.lstMasaninSiparisleri.get(j).MasaAdi.contentEquals(collection.get("masa"))) {
                int siparisSize = g.lstMasaninSiparisleri.get(j).siparisler.size();
                for (int i = 0; i < siparisSize; i++) {
                    Siparis siparis = g.lstMasaninSiparisleri.get(j).siparisler.get(i);
                    if (siparis.siparisYemekAdi.contentEquals("Masa Temizleme İsteği")) {
                        g.lstMasaninSiparisleri.get(j).siparisler.remove(i);
                        if(g.lstMasaninSiparisleri.get(j).siparisler.size() == 0)
                            g.lstMasaninSiparisleri.remove(j);
                        silindi = true;
                        break;
                    }
                }
            }
        }
        return silindi;
    }
}
