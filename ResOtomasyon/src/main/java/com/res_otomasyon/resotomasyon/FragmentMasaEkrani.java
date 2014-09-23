package com.res_otomasyon.resotomasyon;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;

import Entity.Employee;

public class FragmentMasaEkrani extends Fragment implements View.OnClickListener {

    String[] acikMasalar;
    public ArrayList<String> masalar;
    public Employee employee;

    public String departmanAdi, departmanMenusu;
    public String acilanMasa;
    public String kapananMasa;
    public String acilanMasaDepartman;
    GlobalApplication g;
    View fragmentView;
    TableLayout tableView;
    ScrollView scrollView;
    LinearLayout linearLayout;
    boolean masaKilitliMi = false;
    Button masaButton;

    public class getKapananMasa implements Runnable {
        public String _kapananMasa;
        public String _acilanMasaDepartman;

        public getKapananMasa(String masa, String acilanMasaDepartman) {
            this._kapananMasa = masa;
            this._acilanMasaDepartman = acilanMasaDepartman;
        }

        @Override
        public void run() {
            while (_kapananMasa == null) {

            }

            kapananMasa = this._kapananMasa;
            acilanMasaDepartman = this._acilanMasaDepartman;

            if (departmanAdi.contentEquals(acilanMasaDepartman))
                myHandler.sendEmptyMessage(1);
        }
    }

    public class getAcilanMasa implements Runnable {
        public String _acilanMasa;
        public String _acilanMasaDepartman;

        public getAcilanMasa(String masa, String acilanMasaDepartman) {
            this._acilanMasa = masa;
            this._acilanMasaDepartman = acilanMasaDepartman;
        }

        @Override
        public void run() {
            while (_acilanMasa == null) {

            }
            acilanMasa = this._acilanMasa;
            acilanMasaDepartman = this._acilanMasaDepartman;

            if (departmanAdi.contentEquals(acilanMasaDepartman))
                myHandler.sendEmptyMessage(2);
        }
    }

    //TCP thread'inde açık masaları fragment a gönder.
    public class sendAcikMasalar implements Runnable {
        public String[] arrayAcikmasalar;
        public String _DepartmanAdi;

        public sendAcikMasalar(String[] _arrayAcikmasalar, String _DepartmanAdi) {
            this.arrayAcikmasalar = _arrayAcikmasalar;
            this._DepartmanAdi = _DepartmanAdi;
        }

        @Override
        public void run() {
            while (_DepartmanAdi == null) {
            }
            acikMasalar = this.arrayAcikmasalar;
            departmanAdi = this._DepartmanAdi;
            myHandler.sendEmptyMessage(0);
        }
    }

    public Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (acikMasalar != null) {
                        for (String aMasalar : masalar) {
                            masaButton = (Button) fragmentView.findViewWithTag(aMasalar);
                            masaButton.setBackgroundResource(R.drawable.buttonstyle);
                            masaButton.setTag(R.drawable.buttonstyle,R.drawable.buttonstyle);
                            for (String masaAdi : acikMasalar) {
                                if (masaButton.getTag().toString().contentEquals(masaAdi)) {
                                    masaButton.setBackgroundResource(R.drawable.buttonstyleacikmasa);
                                    masaButton.setOnClickListener(FragmentMasaEkrani.this);
                                    masaButton.setTag(R.drawable.buttonstyle,R.drawable.buttonstyleacikmasa);
                                }
                            }
                        }
                    } else {
                        for (String aMasalar : masalar) {
                            masaButton = (Button) fragmentView.findViewWithTag(aMasalar);
                            masaButton.setBackgroundResource(R.drawable.buttonstyle);
                            masaButton.setTag(R.drawable.buttonstyle,R.drawable.buttonstyle);
                            masaButton.setOnClickListener(FragmentMasaEkrani.this);
                        }
                    }
                    preferences = getActivity().getSharedPreferences("KilitliMasa",
                            Context.MODE_PRIVATE);
                    departmanAdi = getArguments().getString("departmanAdi");
                    departmanMenusu = getArguments().getString("departmanMenusu");
                    employee = (Employee) getArguments().getSerializable("Employee");
                    masaKilitliMi = preferences.getBoolean("MasaKilitli", false);
                    if (preferences.getString("departmanAdi", "asdfsdgfgdf").contentEquals(departmanAdi)) {
                        if (masaKilitliMi) {
                            Boolean masaAcikMi = false;

                            masaButton = (Button) fragmentView.findViewWithTag(preferences.getString("masaAdi", ""));

                            if (masaButton.getTag(R.drawable.buttonstyle).equals(R.drawable.buttonstyleacikmasa))
                                masaAcikMi = true;

                            if (g == null)
                                g = (GlobalApplication) getActivity().getApplicationContext();
                            Intent intent = new Intent(getActivity(), MenuEkrani.class);
                            intent.putExtra("DepartmanAdi", departmanAdi);
                            intent.putExtra("MasaAdi", preferences.getString("masaAdi", ""));
                            intent.putExtra("Employee", employee);
                            intent.putExtra("MasaAcikMi", masaAcikMi);
                            intent.putExtra("departmanMenusu", departmanMenusu);

                            startActivity(intent);
                            g.isMenuEkraniRunning = true;
                        }
                    }
                    break;
                case 1:
                    masaButton = (Button) fragmentView.findViewWithTag(kapananMasa);
                    masaButton.setBackgroundResource(R.drawable.buttonstyle);
                    masaButton.setTag(R.drawable.buttonstyle,R.drawable.buttonstyle);
                    break;
                case 2:
                    masaButton = (Button) fragmentView.findViewWithTag(acilanMasa);
                    masaButton.setBackgroundResource(R.drawable.buttonstyleacikmasa);
                    masaButton.setTag(R.drawable.buttonstyle,R.drawable.buttonstyleacikmasa);
                    break;
                default:
                    break;
            }
        }
    };

    SharedPreferences preferences;

    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
    }

    public void startKapananMasa(String masa, String departmanAdi) {
        new Thread(new getKapananMasa(masa, departmanAdi)).start();
    }

    public void startAcilanMasa(String acilanMasa, String departmanAdi) {
        new Thread(new getAcilanMasa(acilanMasa, departmanAdi)).start();
    }

    //sendAcikMasalar class'ını TCP thread'inde tetikler.
    public void startSendAcikMasalar(String[] array, String departmanAdi) {
        new Thread(new sendAcikMasalar(array, departmanAdi)).start();
    }

    @Override
    public void onClick(View v) {
        if (!g.isMenuEkraniRunning) {
            if(preferences == null)
            {
                preferences = getActivity().getSharedPreferences("KilitliMasa", Context.MODE_PRIVATE);
            }
            masaKilitliMi = preferences.getBoolean("MasaKilitli", false);

            if(masaKilitliMi)
                return;

            Boolean masaAcikMi = false;

            masaButton = (Button) fragmentView.findViewWithTag(v.getTag().toString());

            if (masaButton.getTag(R.drawable.buttonstyle).equals(R.drawable.buttonstyleacikmasa))
                masaAcikMi = true;

            Intent intent = new Intent(getActivity(), MenuEkrani.class);
            intent.putExtra("DepartmanAdi", departmanAdi);
            intent.putExtra("MasaAdi", v.getTag().toString());
            intent.putExtra("Employee", employee);
            intent.putExtra("MasaAcikMi", masaAcikMi);
            intent.putExtra("departmanMenusu", departmanMenusu);

            startActivity(intent);
            g.isMenuEkraniRunning = true;
        }
    }

    public FragmentMasaEkrani() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        g = (GlobalApplication) getActivity().getApplicationContext();

        fragmentView = inflater.inflate(R.layout.fragment_fragment_masa_design, container, false);
        this.masalar = getArguments().getStringArrayList("masalar");
        this.departmanAdi = getArguments().getString("departmanAdi");
        this.employee = (Employee) getArguments().getSerializable("Employee");
        this.departmanMenusu = getArguments().getString("departmanMenusu");

        linearLayout = new LinearLayout(getActivity());
        scrollView = new ScrollView(getActivity());
        tableView = new TableLayout(getActivity());
        TableRow tr;
        tableView.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams
                .MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

        scrollView.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams
                .MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableRow.LayoutParams
                .MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT, 1
        );
        layoutParams.setMargins(0, 20, 0, 20);

        TableRow.LayoutParams paramsBtn = new TableRow.LayoutParams(TableRow.LayoutParams
                .MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        paramsBtn.setMargins(20, 0, 20, 0);
        int numberOfRows = Math.round(masalar.size() / 3);
        int masaCounter = 0;
        int numberOfButtonsForLastRow = masalar.size() - numberOfRows * 3;
        for (int i = 0; i < numberOfRows; i++) {
            tr = new TableRow(getActivity());
            tr.setLayoutParams(layoutParams);
            for (int j = 0; j < 3; j++) {
                masaButton = new Button(tr.getContext());
                masaButton.setBackgroundResource(R.drawable.buttonstyle);
                masaButton.setTag(R.drawable.buttonstyle,R.drawable.buttonstyle);
                masaButton.setText(masalar.get(masaCounter));
                masaButton.setTag(masalar.get(masaCounter));
                masaButton.setWidth(160);
                masaButton.setHeight(160);
                masaButton.setLayoutParams(paramsBtn);
                masaButton.setOnClickListener(this);
                tr.addView(masaButton);
                masaCounter++;

                masaButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        for(int i=0;i<g.globalDepartmanlar.size();i++) {
                            if (g.globalDepartmanlar.get(i).globalDepartmanAdi.contentEquals(departmanAdi)) {
                                for (int k = 0; k < g.globalDepartmanlar.get(i).globalMasalar.size(); k++) {
                                    if (g.globalDepartmanlar.get(i).globalMasalar.get(k).globalMasaAdi.contentEquals(v.getTag().toString())) {
                                        if(g.globalDepartmanlar.get(i).globalMasalar.get(k).globalMasaAcikMi)
                                            break;
                                        else {
                                            if (!g.isMenuEkraniRunning) {
                                                masaKilitliMi = preferences.getBoolean("MasaKilitli", false);
                                                if(masaKilitliMi)
                                                    return true;

                                                Boolean masaAcikMi = false;

                                                masaButton = (Button) fragmentView.findViewWithTag(v.getTag().toString());

                                                if (masaButton.getTag(R.drawable.buttonstyle).equals(R.drawable.buttonstyleacikmasa))
                                                    masaAcikMi = true;

                                                Intent intent = new Intent(getActivity(), MenuEkrani.class);
                                                intent.putExtra("DepartmanAdi", departmanAdi);
                                                intent.putExtra("MasaAdi", v.getTag().toString());
                                                intent.putExtra("Employee", employee);
                                                intent.putExtra("MasaAcikMi", masaAcikMi);
                                                intent.putExtra("departmanMenusu", departmanMenusu);

                                                startActivity(intent);
                                                g.isMenuEkraniRunning = true;
                                            }
                                            return true;
                                        }
                                    }
                                }
                                break;
                            }
                        }

                        for(int i=0;i<g.globalDepartmanlar.size();i++)
                        {
                            if(i == g.globalDepartmanlar.size()-1)
                            {
                                g.commonAsyncTask.client.sendMessage("komut=departmanMasaTasimaIcin&departmanAdi=" + g.globalDepartmanlar.get(i).globalDepartmanAdi + "&masaDepartman=" + v.getTag().toString()+"-" + departmanAdi);
                            }
                            else
                                g.commonAsyncTask.client.sendMessage("komut=departmanMasaTasimaIcin&departmanAdi=" + g.globalDepartmanlar.get(i).globalDepartmanAdi);
                        }
                        return true;
                    }
                });
            }
            tableView.addView(tr);
        }
        if (numberOfButtonsForLastRow > 0) {
            tr = new TableRow(tableView.getContext());
            tr.setLayoutParams(layoutParams);
            for (int j = 0; j < numberOfButtonsForLastRow; j++) {
                masaButton = new Button(fragmentView.getContext());
                masaButton.setBackgroundResource(R.drawable.buttonstyle);
                masaButton.setTag(R.drawable.buttonstyle,R.drawable.buttonstyle);
                masaButton.setText(masalar.get(masaCounter));
                masaButton.setTag(masalar.get(masaCounter));
                masaButton.setHeight(160);
                masaButton.setLayoutParams(paramsBtn);
                masaButton.setOnClickListener(this);
                tr.addView(masaButton);
                masaCounter++;

                masaButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        for(int i=0;i<g.globalDepartmanlar.size();i++) {
                            if (g.globalDepartmanlar.get(i).globalDepartmanAdi.contentEquals(departmanAdi)) {
                                for (int k = 0; k < g.globalDepartmanlar.get(i).globalMasalar.size(); k++) {
                                    if (g.globalDepartmanlar.get(i).globalMasalar.get(k).globalMasaAdi.contentEquals(v.getTag().toString())) {
                                        if(g.globalDepartmanlar.get(i).globalMasalar.get(k).globalMasaAcikMi)
                                            break;
                                        else {
                                            if (!g.isMenuEkraniRunning) {
                                                masaKilitliMi = preferences.getBoolean("MasaKilitli", false);
                                                if (masaKilitliMi)
                                                    return true;

                                                Boolean masaAcikMi = false;

                                                masaButton = (Button) fragmentView.findViewWithTag(v.getTag().toString());

                                                if (masaButton.getTag(R.drawable.buttonstyle).equals(R.drawable.buttonstyleacikmasa))
                                                    masaAcikMi = true;

                                                Intent intent = new Intent(getActivity(), MenuEkrani.class);
                                                intent.putExtra("DepartmanAdi", departmanAdi);
                                                intent.putExtra("MasaAdi", v.getTag().toString());
                                                intent.putExtra("Employee", employee);
                                                intent.putExtra("MasaAcikMi", masaAcikMi);
                                                intent.putExtra("departmanMenusu", departmanMenusu);

                                                startActivity(intent);
                                                g.isMenuEkraniRunning = true;
                                            }
                                            return true;
                                        }
                                    }
                                }
                                break;
                            }
                        }

                        for(int i=0;i<g.globalDepartmanlar.size();i++)
                        {
                            if(i == g.globalDepartmanlar.size()-1)
                            {
                                g.commonAsyncTask.client.sendMessage("komut=departmanMasaTasimaIcin&departmanAdi=" + g.globalDepartmanlar.get(i).globalDepartmanAdi + "&masaDepartman=" + v.getTag()+"-" + departmanAdi);
                            }
                            else
                                g.commonAsyncTask.client.sendMessage("komut=departmanMasaTasimaIcin&departmanAdi=" + g.globalDepartmanlar.get(i).globalDepartmanAdi);

                        }
                        return true;
                    }
                });
            }
            tableView.addView(tr);
        }
        scrollView.addView(tableView);
        linearLayout.addView(scrollView);
        fragmentView = linearLayout;
        return fragmentView;
    }


}
