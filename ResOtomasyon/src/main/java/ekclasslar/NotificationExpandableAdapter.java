package ekclasslar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

/**
 * Created by Mustafa on 23.8.2014.
 */
public class NotificationExpandableAdapter extends BaseExpandableListAdapter {


    public GlobalApplication g;
    public Activity activity;
    private ArrayList<MasaninSiparisleri> groups;
    public LayoutInflater inflater;

    public NotificationExpandableAdapter(Activity act, ArrayList<MasaninSiparisleri> groups, GlobalApplication g) {
        this.g = g;
        this.groups = groups;
        this.activity = act;
        this.inflater = act.getLayoutInflater();
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).siparisler.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).siparisler.get(childPosition);
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
                                g.commonAsyncTask.client.sendMessage("komut=bildirimGoruldu&masa=" + groups.get(groupPosition).MasaAdi + "&departmanAdi=" + groups.get(groupPosition).DepartmanAdi + "&yemekAdi=hepsi&adedi=hepsi&porsiyonu=hepsi");
                                groups.remove(groupPosition);
                                notifyDataSetChanged();
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
        if (convertView == null)
            convertView = inflater.inflate(R.layout.notification_gorunumu_layout, parent, false);
        Siparis siparis = (Siparis) getChild(groupPosition, childPosition);
        TextView textYemekAdi;
        TextView textAdet;
        Button btnClear;
        textYemekAdi = (TextView) convertView.findViewById(R.id.textViewChildInfo);
        textAdet = (TextView) convertView.findViewById(R.id.textViewAdet);
        btnClear = (Button) convertView.findViewById(R.id.buttonClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MasaninSiparisleri masaninSiparisleri = groups.get(groupPosition);
                Siparis siparis1 = groups.get(groupPosition).siparisler.get(childPosition);
                g.commonAsyncTask.client.sendMessage("komut=bildirimGoruldu&masa="+masaninSiparisleri.MasaAdi+"&departmanAdi="+masaninSiparisleri.DepartmanAdi+"&yemekAdi="+siparis1.siparisYemekAdi+"&adedi="+siparis1.siparisAdedi+"&porsiyonu="+siparis1.siparisPorsiyonu);
                groups.get(groupPosition).siparisler.remove(childPosition);
                if (groups.get(groupPosition).siparisler.size() == 0)
                    groups.remove(groupPosition);
                notifyDataSetChanged();
            }
        });
        textYemekAdi.setText(siparis.siparisYemekAdi);
        textAdet.setText(siparis.siparisAdedi+"");
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
