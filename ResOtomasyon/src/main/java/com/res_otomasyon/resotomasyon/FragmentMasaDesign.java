package com.res_otomasyon.resotomasyon;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMasaDesign extends Fragment implements View.OnClickListener {

    String[] acikMasalar;
    public ArrayList<String> masalar;
    public ArrayList<Employee> lstEmployees;

    public String DepartmanAdi;
    public String acilanMasa;
    public String kapananMasa;

    View fragmentView;
    TableLayout tableView;
    ScrollView scrollView;
    LinearLayout linearLayout;

    Button btn;
    Button button;


    public void masaAc() {
        button = (Button) fragmentView.findViewWithTag(acilanMasa);
        button.setBackgroundResource(R.drawable.buttonstyleacikmasa);
        Log.e("MasaAc", "Tetiklendi");
    }

    public void masaKapat() {
        button = (Button) fragmentView.findViewWithTag(kapananMasa);
        button.setBackgroundResource(R.drawable.buttonstyle);
        Log.e("MasaKapat", "Tetiklendi");
    }

    //Buton rengini değiştir.
    public void acikMasalariGoster() {
        if (acikMasalar != null) {
            for (int i = 0; i < masalar.size(); i++) {
                button = (Button) fragmentView.findViewWithTag(masalar.get(i));
                button.setBackgroundResource(R.drawable.buttonstyle);
                for (int j = 0; j < acikMasalar.length; j++) {
                    String masaAdi = acikMasalar[j];
                    if (button.getTag().toString().contentEquals(masaAdi)) {
                        button.setBackgroundResource(R.drawable.buttonstyleacikmasa);
                        button.setOnClickListener(this);
                    }
                }
            }
        } else {
            for (int i = 0; i < masalar.size(); i++) {
                button = (Button) fragmentView.findViewWithTag(masalar.get(i));
                button.setBackgroundResource(R.drawable.buttonstyle);
                button.setOnClickListener(this);
            }
        }
//        for (int i = 0; i < masalar.size(); i++) {
//            if (acikMasalar != null) {
//                button = (Button) fragmentView.findViewWithTag(masalar.get(i));
//                button.setBackgroundResource(R.drawable.buttonstyle);
//                for (int j = 0; j < acikMasalar.length; j++) {
//                    String masaAdi = acikMasalar[j];
//                    if (button.getTag().toString().contentEquals(masaAdi)) {
//                        button.setBackgroundResource(R.drawable.buttonstyleacikmasa);
//                        button.setOnClickListener(this);
//                    }
//                }
//            }
//        }
    }

    public class getKapananMasa implements Runnable {
        public String _kapananMasa;

        public getKapananMasa(String masa) {
            this._kapananMasa = masa;
        }

        @Override
        public void run() {
            kapananMasa = this._kapananMasa;
        }
    }

    public class getAcilanMasa implements Runnable {
        public String _acilanMasa;

        public getAcilanMasa(String masa) {
            this._acilanMasa = masa;
        }

        @Override
        public void run() {
            acilanMasa = this._acilanMasa;
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
            acikMasalar = this.arrayAcikmasalar;
            DepartmanAdi = this._DepartmanAdi;
        }
    }

    public void startKapananMasa(String masa) {
        new Thread(new getKapananMasa(masa)).start();
    }

    public void startAcilanMasa(String acilanMasa) {
        new Thread(new getAcilanMasa(acilanMasa)).start();
    }

    //sendAcikMasalar class'ını TCP thread'inde tetikler.
    public void startSendAcikMasalar(String[] array, String departmanAdi) {
        new Thread(new sendAcikMasalar(array, departmanAdi)).start();
    }

    @Override
    public void onClick(View v) {
        String masaAdi = v.getTag().toString();
        Intent intent = new Intent(getActivity(), MasaEkrani.class);
        intent.putExtra("DepartmanAdi", this.DepartmanAdi);
        intent.putExtra("MasaAdi", v.getTag().toString());
        intent.putExtra("lstEmp", this.lstEmployees);
        startActivity(intent);
    }

    public FragmentMasaDesign() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_fragment_masa_design, container, false);
        this.masalar = (ArrayList<String>) getArguments().getStringArrayList("masalar");
        this.DepartmanAdi = getArguments().getString("departmanAdi");
        this.lstEmployees = (ArrayList<Employee>)getArguments().getSerializable("lstEmp");
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
        int numberOfRows = (int) Math.round(masalar.size() / 3);
        int masaCounter = 0;
        int numberOfButtonsForLastRow = masalar.size() - numberOfRows * 3;
        for (int i = 0; i < numberOfRows; i++) {
            tr = new TableRow(getActivity());
            tr.setLayoutParams(layoutParams);
            for (int j = 0; j < 3; j++) {
                btn = new Button(tr.getContext());
                btn.setBackgroundResource(R.drawable.buttonstyle);
                btn.setText(masalar.get(masaCounter));
                btn.setTag(masalar.get(masaCounter));
                btn.setWidth(160);
                btn.setHeight(160);
                btn.setLayoutParams(paramsBtn);
                btn.setOnClickListener(this);
                tr.addView(btn);
                masaCounter++;
            }
            tableView.addView(tr);
        }
        if (numberOfButtonsForLastRow > 0) {
            tr = new TableRow(tableView.getContext());
            tr.setLayoutParams(layoutParams);
            for (int j = 0; j < numberOfButtonsForLastRow; j++) {
                btn = new Button(fragmentView.getContext());
                btn.setBackgroundResource(R.drawable.buttonstyle);
                btn.setText(masalar.get(masaCounter));
                btn.setTag(masalar.get(masaCounter));
                btn.setHeight(160);
                btn.setLayoutParams(paramsBtn);
                btn.setOnClickListener(this);
                tr.addView(btn);
                masaCounter++;
            }
            tableView.addView(tr);
        }
        scrollView.addView(tableView);
        linearLayout.addView(scrollView);
        fragmentView = linearLayout;
        return fragmentView;
    }


}
