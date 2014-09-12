package ekclasslar;

import com.res_otomasyon.resotomasyon.GlobalApplication;

import java.util.ArrayList;
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

        if (collectionSiparis != null) {
            String[] siparisLer = collectionSiparis.split("\\*");
            String[] siparisDetay;
            for (String siparis : siparisLer) {
                siparisDetay = siparis.split("-");
                this.siparis = new Siparis();
                this.masaninSiparisleri = new MasaninSiparisleri();
                masaninSiparisleri.DepartmanAdi = siparisDetay[0];
                masaninSiparisleri.MasaAdi = siparisDetay[1];
                this.siparis.siparisFiyati = siparisDetay[2];
                this.siparis.siparisAdedi = Integer.parseInt(siparisDetay[3]);
                this.siparis.siparisYemekAdi = siparisDetay[4];
                if (siparisDetay[5].contains(","))
                    siparisDetay[5] = siparisDetay[5].replace(",", ".");
                this.siparis.siparisPorsiyonu = Double.valueOf(siparisDetay[5]);
                if (g.secilenMasalar.size() > 0) {
                    for (DepartmanMasalari dptMasa : g.secilenMasalar) {
                        for (String masa : dptMasa.Masalar) {
                            if (masa.contentEquals(siparisDetay[1]) && dptMasa.DepartmanAdi.contentEquals(siparisDetay[0])) {
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
                } else {
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
        if (collection.get("ozelBildirimBilgileri") == null)
            return secilenMasaMi;
        else {
            String collectionOzelBildirimler = collection.get("ozelBildirimBilgileri");
            String[] ozelBildirimBilgileri = collectionOzelBildirimler.split("\\*");
            String[] ozelBildirimDetay;
            Siparis siparisHesap = new Siparis();
            Siparis siparisTemizlik = new Siparis();
            Siparis siparisGarson = new Siparis();

            siparisGarson.siparisYemekAdi = "Garson İsteği";
            siparisTemizlik.siparisYemekAdi = "Masa Temizleme İsteği";
            siparisHesap.siparisYemekAdi = "Hesap İsteği";

            for (String str : ozelBildirimBilgileri) {
                ArrayList<Siparis> lstOzelBildirimler = new ArrayList<Siparis>();
                ozelBildirimDetay = str.split("-");
                this.siparis = new Siparis();
                this.masaninSiparisleri = new MasaninSiparisleri();
                masaninSiparisleri.DepartmanAdi = ozelBildirimDetay[1];
                masaninSiparisleri.MasaAdi = ozelBildirimDetay[2];
                if (ozelBildirimDetay[0].contentEquals("4")) {
                    lstOzelBildirimler.add(siparisGarson);
                } else if (ozelBildirimDetay[0].contentEquals("2")) {
                    lstOzelBildirimler.add(siparisTemizlik);
                } else if (ozelBildirimDetay[0].contentEquals("1")) {
                    lstOzelBildirimler.add(siparisHesap);
                } else if (ozelBildirimDetay[0].contentEquals("3")) {
                    lstOzelBildirimler.add(siparisTemizlik);
                    lstOzelBildirimler.add(siparisHesap);
                } else if (ozelBildirimDetay[0].contentEquals("5")) {
                    lstOzelBildirimler.add(siparisGarson);
                    lstOzelBildirimler.add(siparisHesap);
                } else if (ozelBildirimDetay[0].contentEquals("6")) {
                    lstOzelBildirimler.add(siparisTemizlik);
                    lstOzelBildirimler.add(siparisGarson);
                } else if (ozelBildirimDetay[0].contentEquals("7")) {
                    lstOzelBildirimler.add(siparisTemizlik);
                    lstOzelBildirimler.add(siparisHesap);
                    lstOzelBildirimler.add(siparisGarson);
                }
                if (g.secilenMasalar.size() > 0) {
                    for (DepartmanMasalari dptMasa : g.secilenMasalar) {
                        for (String masa : dptMasa.Masalar) {
                            if (masa.contentEquals(ozelBildirimDetay[2]) && dptMasa.DepartmanAdi.contentEquals(ozelBildirimDetay[1])) {
                                if (g.lstMasaninSiparisleri.size() > 0) {
                                    boolean ayniMasaVarMi = false;
                                    for (MasaninSiparisleri msp : g.lstMasaninSiparisleri) {
                                        if (msp.DepartmanAdi.contentEquals(ozelBildirimDetay[1]) && msp.MasaAdi.contentEquals(ozelBildirimDetay[2])) {
                                            for (Siparis ozelSiparis : lstOzelBildirimler) {
                                                msp.siparisler.add(ozelSiparis);
                                            }
                                            ayniMasaVarMi = true;
                                            break;
                                        }
                                    }
                                    if (!ayniMasaVarMi) {
                                        for (Siparis ozelSiparis : lstOzelBildirimler) {
                                            masaninSiparisleri.siparisler.add(ozelSiparis);
                                        }
                                        g.lstMasaninSiparisleri.add(masaninSiparisleri);
                                    }
                                } else {
                                    for (Siparis ozelSiparis : lstOzelBildirimler) {
                                        masaninSiparisleri.siparisler.add(ozelSiparis);
                                    }
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
                            if (msp.DepartmanAdi.contentEquals(ozelBildirimDetay[1]) && msp.MasaAdi.contentEquals(ozelBildirimDetay[2])) {
                                for (Siparis ozelSiparis : lstOzelBildirimler) {
                                    msp.siparisler.add(ozelSiparis);
                                }
                                ayniMasaVarMi = true;
                                break;
                            }
                        }
                        if (!ayniMasaVarMi) {
                            for (Siparis ozelSiparis : lstOzelBildirimler) {
                                masaninSiparisleri.siparisler.add(ozelSiparis);
                            }
                            g.lstMasaninSiparisleri.add(masaninSiparisleri);
                        }
                    } else {
                        for (Siparis ozelSiparis : lstOzelBildirimler) {
                            masaninSiparisleri.siparisler.add(ozelSiparis);
                        }
                        g.lstMasaninSiparisleri.add(masaninSiparisleri);
                    }
                    secilenMasaMi = true;
                }
            }
        }
        return secilenMasaMi;
    }
}
