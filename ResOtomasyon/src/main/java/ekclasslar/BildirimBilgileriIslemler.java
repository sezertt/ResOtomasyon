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
        siparis = new Siparis();
    }

    public boolean bildirimBilgileri() {
        boolean secilenMasaMi = false;
        String collectionSiparis = collection.get("bildirimBilgileri");
        if(collectionSiparis ==null)
            return false;
        String[] siparisLer = collectionSiparis.split("\\*");
        String[] siparisDetay;
        for (String siparis : siparisLer) {
            siparisDetay = siparis.split("-");
            this.siparis = new Siparis();
            this.masaninSiparisleri = new MasaninSiparisleri();
            masaninSiparisleri.DepartmanAdi = siparisDetay[0];
            masaninSiparisleri.MasaAdi = siparisDetay[1];
            this.siparis.siparisFiyati = siparisDetay[2];
            this.siparis.siparisAdedi = siparisDetay[3];
            this.siparis.siparisYemekAdi = siparisDetay[4];
            if(siparisDetay[5].contains(","))
                siparisDetay[5] = siparisDetay[5].replace(",",".");
            this.siparis.siparisPorsiyonu = Double.valueOf(siparisDetay[5]);
            if (g.secilenMasalar.size() > 0) {
                for (DepartmanMasalari dptMasa : g.secilenMasalar) {
                    for (String masa : dptMasa.Masalar) {
                        if (masa.contentEquals(siparisDetay[1]) && dptMasa.DepartmanAdi.contentEquals(siparisDetay[2])) {
                            if (g.lstMasaninSiparisleri.size() > 0) {
                                boolean ayniMasaVarMi = false;
                                for (MasaninSiparisleri msp : g.lstMasaninSiparisleri) {
                                    if (msp.DepartmanAdi.contentEquals(siparisDetay[0]) && msp.MasaAdi.contentEquals(siparisDetay[1])) {
                                        msp.siparisler.add(this.siparis);
                                        ayniMasaVarMi = true;
                                        break;
                                    }
                                }
                                if (!ayniMasaVarMi) {
                                    masaninSiparisleri.siparisler.add(this.siparis);
                                    g.lstMasaninSiparisleri.add(masaninSiparisleri);
                                }
                            } else {
                                masaninSiparisleri.siparisler.add(this.siparis);
                                g.lstMasaninSiparisleri.add(masaninSiparisleri);
                            }
                            secilenMasaMi = true;
                        }
                    }
                }
            }
            else {
                if (g.lstMasaninSiparisleri.size() > 0) {
                    boolean ayniMasaVarMi = false;
                    for (MasaninSiparisleri msp : g.lstMasaninSiparisleri) {
                        if (msp.DepartmanAdi.contentEquals(siparisDetay[0]) && msp.MasaAdi.contentEquals(siparisDetay[1])) {
                            msp.siparisler.add(this.siparis);
                            ayniMasaVarMi = true;
                            break;
                        }
                    }
                    if (!ayniMasaVarMi) {
                        masaninSiparisleri.siparisler.add(this.siparis);
                        g.lstMasaninSiparisleri.add(masaninSiparisleri);
                    }
                } else {
                    masaninSiparisleri.siparisler.add(this.siparis);
                    g.lstMasaninSiparisleri.add(masaninSiparisleri);
                }
                secilenMasaMi = true;
            }
        }
        return  secilenMasaMi;
    }
}
