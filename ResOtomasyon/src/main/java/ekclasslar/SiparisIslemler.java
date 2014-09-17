package ekclasslar;

import com.res_otomasyon.resotomasyon.GlobalApplication;
import java.util.Dictionary;
import Entity.MasaninSiparisleri;
import Entity.Siparis;

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
    public boolean Islem() {
        boolean secilenMasaMi = false;
        siparis = new Siparis();
        masaninSiparisleri.DepartmanAdi = collection.get("departmanAdi");
        masaninSiparisleri.MasaAdi = collection.get("masa");
        siparis.siparisAdedi = Integer.parseInt(collection.get("miktar").replace(',','.'));
        siparis.siparisYemekAdi = collection.get("yemekAdi");
        String porsiyon = collection.get("porsiyon").replace(",",".");
        siparis.siparisPorsiyonu =Double.parseDouble(porsiyon);
        if (g.secilenMasalar.size() > 0) {
            for (DepartmanMasalari dptMasa : g.secilenMasalar) {
                for (String masa : dptMasa.Masalar) {
                    if (masa.contentEquals(collection.get("masa")) && dptMasa.DepartmanAdi.contentEquals(collection.get("departmanAdi"))) {
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
                        secilenMasaMi = true;
                    }
                }
            }
        } else {
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
            secilenMasaMi = true;
        }
        return secilenMasaMi;
    }
}