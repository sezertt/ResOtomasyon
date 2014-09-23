package ekclasslar;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.res_otomasyon.resotomasyon.GlobalApplication;
import com.res_otomasyon.resotomasyon.R;

import java.util.ArrayList;

import Entity.MasaninSiparisleri;
import Entity.Siparis;

public class NotificationExpandableAdapter extends BaseExpandableListAdapter {


    public GlobalApplication g;
    public Activity activity;
    public LayoutInflater inflater;

    public NotificationExpandableAdapter(Activity act, GlobalApplication g) {
        this.g = g;

        this.activity = act;
        this.inflater = act.getLayoutInflater();
    }

    @Override
    public int getGroupCount() {
        return g.lstMasaninSiparisleri.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return g.lstMasaninSiparisleri.get(groupPosition).siparisler.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return g.lstMasaninSiparisleri.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return g.lstMasaninSiparisleri.get(groupPosition).siparisler.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.notification_kategori_gorunum, parent, false);
        convertView.setBackgroundColor(Color.rgb(180, 180, 180));
        MasaninSiparisleri group = (MasaninSiparisleri) getGroup(groupPosition);
        CheckedTextView textGroupDepartmanAdi;
        Button btnClearAll;
        textGroupDepartmanAdi = (CheckedTextView) convertView.findViewById(R.id.textViewProductGroupHeader);
        textGroupDepartmanAdi.setText(group.DepartmanAdi + "-" + group.MasaAdi);
        textGroupDepartmanAdi.setChecked(isExpanded);

        btnClearAll = (Button) convertView.findViewById(R.id.btnClearAll);
        btnClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder aBuilder = new AlertDialog.Builder(activity);
                aBuilder.setTitle("Siparişler Görüldü!");
                aBuilder.setMessage("TÜM SİPARİŞLERİ GÖRÜLDÜ OLARAK İŞARETLEMEK İSTEDİĞİNİZE EMİN MİSİNİZ?")
                        .setCancelable(true)
                        .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                g.commonAsyncTask.client.sendMessage("komut=bildirimGoruldu&masa=" + g.lstMasaninSiparisleri.get(groupPosition).MasaAdi + "&departmanAdi=" + g.lstMasaninSiparisleri.get(groupPosition).DepartmanAdi + "&yemekAdi=hepsi&adedi=hepsi&porsiyonu=hepsi");
                            }
                        }).setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = aBuilder.create();
                alertDialog.show();

            }
        });
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.notification_gorunumu_layout, parent, false);
        Siparis siparis = g.lstMasaninSiparisleri.get(groupPosition).siparisler.get(childPosition);
//        final Siparis siparis = (Siparis) getChild(groupPosition, childPosition);
        TextView textYemekAdi;
        TextView textAdet;
        TextView textPorsiyon;
        Button btnClear;
        textYemekAdi = (TextView) convertView.findViewById(R.id.textViewChildInfo);
        textAdet = (TextView) convertView.findViewById(R.id.textViewAdet);
        textPorsiyon = (TextView) convertView.findViewById(R.id.textPorsiyon);
        btnClear = (Button) convertView.findViewById(R.id.buttonClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MasaninSiparisleri masaninSiparisleri = g.lstMasaninSiparisleri.get(groupPosition);
                Siparis siparis1 = g.lstMasaninSiparisleri.get(groupPosition).siparisler.get(childPosition);
                if (siparis1.siparisYemekAdi.contentEquals("Garson İsteği")) {
                    g.commonAsyncTask.client.sendMessage("komut=GarsonGoruldu&masa=" + masaninSiparisleri.MasaAdi + "&departmanAdi=" + masaninSiparisleri.DepartmanAdi + "");
                } else if (siparis1.siparisYemekAdi.contentEquals("Masa Temizleme İsteği")) {
                    g.commonAsyncTask.client.sendMessage("komut=TemizlikGoruldu&masa=" + masaninSiparisleri.MasaAdi + "&departmanAdi=" + masaninSiparisleri.DepartmanAdi + "");
                } else if (siparis1.siparisYemekAdi.contentEquals("Hesap İsteği")) {
                    g.commonAsyncTask.client.sendMessage("komut=HesapGoruldu&masa=" + masaninSiparisleri.MasaAdi + "&departmanAdi=" + masaninSiparisleri.DepartmanAdi + "");
                } else {
                    g.commonAsyncTask.client.sendMessage("komut=bildirimGoruldu&masa=" + masaninSiparisleri.MasaAdi + "&departmanAdi=" + masaninSiparisleri.DepartmanAdi + "&yemekAdi=" + siparis1.siparisYemekAdi + "&adedi=" + siparis1.siparisAdedi + "&porsiyonu=" + siparis1.siparisPorsiyonu);
                }
            }
        });
        textYemekAdi.setText(siparis.siparisYemekAdi);
        if (siparis.siparisAdedi == 0) {
            textAdet.setText("");
            textPorsiyon.setText("");
        } else {
            textAdet.setText("x" + siparis.siparisAdedi + "");
            textPorsiyon.setText(siparis.siparisPorsiyonu + "");
        }
        if (siparis.siparisYemekAdi.contentEquals("Garson İsteği")) {
            ValueAnimator colorAnim = ObjectAnimator.ofInt(convertView, "backgroundColor", Color.rgb(255, 80, 80), Color.WHITE);
            colorAnim.setDuration(1500);
            colorAnim.setEvaluator(new ArgbEvaluator());
            colorAnim.setRepeatCount(ValueAnimator.INFINITE);
            colorAnim.setRepeatMode(ValueAnimator.REVERSE);
            colorAnim.start();
        } else if (siparis.siparisYemekAdi.contentEquals("Masa Temizleme İsteği")) {
            ValueAnimator colorAnim = ObjectAnimator.ofInt(convertView, "backgroundColor", Color.rgb(80, 255, 80), Color.WHITE);
            colorAnim.setDuration(1500);
            colorAnim.setEvaluator(new ArgbEvaluator());
            colorAnim.setRepeatCount(ValueAnimator.INFINITE);
            colorAnim.setRepeatMode(ValueAnimator.REVERSE);
            colorAnim.start();
        } else if (siparis.siparisYemekAdi.contentEquals("Hesap İsteği")) {
            ValueAnimator colorAnim = ObjectAnimator.ofInt(convertView, "backgroundColor", Color.rgb(232, 228, 104), Color.WHITE);
            colorAnim.setDuration(1500);
            colorAnim.setEvaluator(new ArgbEvaluator());
            colorAnim.setRepeatCount(ValueAnimator.INFINITE);
            colorAnim.setRepeatMode(ValueAnimator.REVERSE);
            colorAnim.start();
        } else {
            ValueAnimator colorAnim = ObjectAnimator.ofInt(convertView, "backgroundColor", Color.WHITE, Color.WHITE);
            colorAnim.setDuration(100);
            colorAnim.setEvaluator(new ArgbEvaluator());
            colorAnim.setRepeatMode(ValueAnimator.RESTART);
            colorAnim.start();
            colorAnim.cancel();
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
