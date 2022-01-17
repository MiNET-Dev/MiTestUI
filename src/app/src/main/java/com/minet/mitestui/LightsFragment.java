package com.minet.mitestui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.Serializable;

import za.co.megaware.MinetService.IMainService;

public class LightsFragment extends Fragment {

    private Button btnRed;
    private Button btnGreen;
    private Button btnWhite;
    private Button btnYellow;
    private Button btnBlue;
    private Button btnCyan;
    private Button btnOrange;
    private Button btnMagenta;
    private Button btnOff;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_lights, container, false);

        btnRed = view.findViewById(R.id.btn_red);
        btnGreen = view.findViewById(R.id.btn_green);
        btnWhite = view.findViewById(R.id.btn_white);
        btnYellow = view.findViewById(R.id.btn_yellow);
        btnBlue = view.findViewById(R.id.btn_blue);
        btnCyan = view.findViewById(R.id.btn_cyan);
        btnOrange = view.findViewById(R.id.btn_orange);
        btnMagenta = view.findViewById(R.id.btn_magenta);
        btnOff = view.findViewById(R.id.btn_off);

        btnRed.setOnClickListener(colorChange);
        btnGreen.setOnClickListener(colorChange);
        btnWhite.setOnClickListener(colorChange);
        btnYellow.setOnClickListener(colorChange);
        btnBlue.setOnClickListener(colorChange);
        btnCyan.setOnClickListener(colorChange);
        btnOrange.setOnClickListener(colorChange);
        btnMagenta.setOnClickListener(colorChange);
        btnOff.setOnClickListener(colorChange);

        return view;
    }


    View.OnClickListener colorChange = v -> {

        switch (v.getId()){
            case R.id.btn_red:
                ServiceHelper.getInstance().TestLights("red");
                break;
            case R.id.btn_green:
                ServiceHelper.getInstance().TestLights("green");
                break;
            case R.id.btn_white:
                ServiceHelper.getInstance().TestLights("white");
                break;
            case R.id.btn_yellow:
                ServiceHelper.getInstance().TestLights("yellow");
                break;
            case R.id.btn_blue:
                ServiceHelper.getInstance().TestLights("blue");
                break;
            case R.id.btn_cyan:
                ServiceHelper.getInstance().TestLights("cyan");
                break;
            case R.id.btn_orange:
                ServiceHelper.getInstance().TestLights("orange");
                break;
            case R.id.btn_magenta:
                ServiceHelper.getInstance().TestLights("magenta");
                break;
            case R.id.btn_off:
                ServiceHelper.getInstance().TestLights("off");
                break;
        }

    };

}
