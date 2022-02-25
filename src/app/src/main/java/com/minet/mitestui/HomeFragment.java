package com.minet.mitestui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private View homeFragment;
    private LinearLayout updatesInfoLayout;

    ColorStateList colorStateList = new ColorStateList(
            new int[][]{
                    new int[]{-android.R.attr.state_checked}, //disabled
                    new int[]{android.R.attr.state_checked} //enabled
            },

            new int[]{
                    Color.RED,//disabled
                    Color.GREEN //enabled
            }
    );

    // PRINTER TAMPERS
    private RadioButton rbPrinterTray;
    private RadioButton rbPrinterPaper;
    private TextView txtPrinterTray;
    private TextView txtPrinterPaper;

    // SENSOR TAMPERS
    private RadioButton rbTamperInternal;
    private RadioButton rbTamperBracket;
    private RadioButton rbTamperPole;
    private TextView txtTamperInternal;
    private TextView txtTamperBracket;
    private TextView txtTamperPole;

    // DEVICE INFO
    private TextView txtIMEI;
    private TextView txtMACAddress;
    private TextView txtIPAddress;
    private TextView txtSerial;

    // APP VERSIONS
    private TextView txtServiceVersion;
    private TextView txtUIVersion;

    // TELEMETRY
    private TextView txtBattery;
    private TextView txtTemperature;
    private TextView txtInput;
    private TextView txtVersion;
    private TextView txtBatteryStatus;

    // FAB
    FloatingActionButton fabUpdate;

    // SNACK BAR
    Snackbar help;

    // MENU
    Menu navigationMenu;
    NavigationView navigationView;

    boolean needsToShow = false;
    LinkedHashMap<Integer, ArrayList<String>> mappedDevices;

    @SuppressLint("HardwareIds")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        homeFragment = inflater.inflate(R.layout.fragment_home, container, false);

        updatesInfoLayout = homeFragment.findViewById(R.id.home_update_notification_layout);

        rbPrinterPaper = homeFragment.findViewById(R.id.home_ic_printer_paper);
        rbPrinterPaper.setClickable(false);
        rbPrinterPaper.setButtonTintList(colorStateList);
        rbPrinterTray = homeFragment.findViewById(R.id.home_ic_printer_tray);
        rbPrinterTray.setClickable(false);
        rbPrinterTray.setButtonTintList(colorStateList);
        rbTamperBracket = homeFragment.findViewById(R.id.home_ic_bracket_tamper);
        rbTamperBracket.setClickable(false);
        rbTamperBracket.setButtonTintList(colorStateList);
        rbTamperInternal = homeFragment.findViewById(R.id.home_ic_internal_tamper);
        rbTamperInternal.setClickable(false);
        rbTamperInternal.setButtonTintList(colorStateList);
        rbTamperPole = homeFragment.findViewById(R.id.home_ic_pole_tamper);
        rbTamperPole.setClickable(false);
        rbTamperPole.setButtonTintList(colorStateList);

        txtBatteryStatus = homeFragment.findViewById(R.id.txt_home_battery_status);
        txtTemperature = homeFragment.findViewById(R.id.txt_home_temp);
        txtBattery = homeFragment.findViewById(R.id.txt_home_battery);
        txtInput = homeFragment.findViewById(R.id.txt_home_input);
        txtVersion = homeFragment.findViewById(R.id.txt_home_version);
        txtUIVersion = homeFragment.findViewById(R.id.txt_home_ui_info);
        txtIPAddress = homeFragment.findViewById(R.id.txt_home_ip);
        txtMACAddress = homeFragment.findViewById(R.id.txt_home_mac);
        txtIMEI = homeFragment.findViewById(R.id.txt_home_imei);
        txtSerial = homeFragment.findViewById(R.id.txt_home_serial);
        txtTamperPole = homeFragment.findViewById(R.id.txt_home_pole_tamper);
        txtTamperBracket = homeFragment.findViewById(R.id.txt_home_bracket_tamper);
        txtTamperInternal = homeFragment.findViewById(R.id.txt_home_internal_tamper);
        txtPrinterPaper = homeFragment.findViewById(R.id.txt_home_printer_paper);
        txtPrinterTray = homeFragment.findViewById(R.id.txt_home_printer_tray);
        txtServiceVersion = homeFragment.findViewById(R.id.txt_home_service_info);

        fabUpdate = homeFragment.findViewById(R.id.home_fab_update);

        navigationView = Objects.requireNonNull(getActivity()).findViewById(R.id.nav_view);

        navigationMenu = navigationView.getMenu();

        fabUpdate.setOnClickListener(onFABUpdateClicked);

        String localNewUI = Utils.getLocalAppVersion("com.minet.mitestui", getContext()) == null ? "not installed" : Utils.getLocalAppVersion("com.minet.mitestui", getContext());
        String localService = Utils.getLocalAppVersion("za.co.megaware.MinetService", getContext()) == null ? "not installed" : Utils.getLocalAppVersion("za.co.megaware.MinetService", getContext());

        txtUIVersion.setText(String.format("v%s", localNewUI));
        txtServiceVersion.setText(String.format("v%s", localService));

        txtSerial.setText(Build.SERIAL.substring(6));

        if (ServiceHelper.getInstance().isConnected()){
            syncWithService();
        }

        return homeFragment;
    }

    View.OnClickListener onFABUpdateClicked = v -> {
        help = Snackbar.make(v, "Updates available", Snackbar.LENGTH_INDEFINITE)
        .setAction("X", view -> help.dismiss());

        help.show();

        navigationMenu.performIdentifierAction(R.id.nav_updates, 0);
    };

    private void getAllVersions() {

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            updatesInfoLayout.setVisibility(View.GONE);
        });

        FTPHandler handler = new FTPHandler();

        String latestService = handler.getAppVersion(AppVersionType.SERVICE);
        String localService = Utils.getLocalAppVersion("za.co.megaware.MinetService", getContext()) == null ? "not installed" : Utils.getLocalAppVersion("za.co.megaware.MinetService", getContext());

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            if (!latestService.equals(localService)){
                TextView text = new TextView(getContext());
                text.setTextColor(ContextCompat.getColor(getContext(), R.color.btnGreen));
                text.setTypeface(text.getTypeface(), Typeface.BOLD);
                text.setGravity(Gravity.CENTER);
                text.setTextSize(12);
                text.setText("Service Updates Available -> " + latestService);
                updatesInfoLayout.addView(text);
                needsToShow = true;
            }
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String latestFirmware = handler.getAppVersion(AppVersionType.FIRMWARE);
        String localFirmware = getFirmwareVersion();

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            if (!latestFirmware.equals(localFirmware)){
                TextView text = new TextView(getContext());
                text.setTextColor(ContextCompat.getColor(getContext(), R.color.btnGreen));
                text.setTypeface(text.getTypeface(), Typeface.BOLD);
                text.setGravity(Gravity.CENTER);
                text.setTextSize(12);
                text.setText("Firmware Updates Available -> " + latestFirmware);
                updatesInfoLayout.addView(text);
                needsToShow = true;
            }
        });

        String latestOldUI = handler.getAppVersion(AppVersionType.OLD_UI);
        String localOldUI = Utils.getLocalAppVersion("com.silver.userinterfacealpha", getContext())== null ? "not installed" : Utils.getLocalAppVersion("com.silver.userinterfacealpha", getContext());

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            if (!latestOldUI.equals(localOldUI)){
                TextView text = new TextView(getContext());
                text.setTextColor(ContextCompat.getColor(getContext(), R.color.btnGreen));
                text.setTypeface(text.getTypeface(), Typeface.BOLD);
                text.setGravity(Gravity.CENTER);
                text.setTextSize(12);
                text.setText("Old UI Updates Available -> " + latestOldUI);
                updatesInfoLayout.addView(text);
                needsToShow = true;
            }
        });

        String latestNewUI = handler.getAppVersion(AppVersionType.NEW_UI);
        String localNewUI = Utils.getLocalAppVersion("com.minet.mitestui", getContext()) == null ? "not installed" : Utils.getLocalAppVersion("com.minet.mitestui", getContext());

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            if (!latestNewUI.equals(localNewUI)){
                TextView text = new TextView(getContext());
                text.setTextColor(ContextCompat.getColor(getContext(), R.color.btnGreen));
                text.setTypeface(text.getTypeface(), Typeface.BOLD);
                text.setGravity(Gravity.CENTER);
                text.setTextSize(12);
                text.setText("New UI Updates Available -> " + latestNewUI);
                updatesInfoLayout.addView(text);
                needsToShow = true;
            }
        });

        String latestBpass = handler.getAppVersion(AppVersionType.OPEN_TRANSIT);
        String localBpass = Utils.getLocalAppVersion("com.bps.bpass.mainpackage", getContext()) == null ? "not installed" : Utils.getLocalAppVersion("com.bps.bpass.mainpackage", getContext());

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            if (!latestBpass.equals(localBpass)){
                TextView text = new TextView(getContext());
                text.setTextColor(ContextCompat.getColor(getContext(), R.color.btnGreen));
                text.setTypeface(text.getTypeface(), Typeface.BOLD);
                text.setGravity(Gravity.CENTER);
                text.setTextSize(12);
                text.setText("Open Transit Updates Available -> " + latestBpass);
                updatesInfoLayout.addView(text);
                needsToShow = true;
            }
        });

        if (needsToShow) {
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                updatesInfoLayout.setVisibility(View.VISIBLE);
                fabUpdate.setVisibility(View.VISIBLE);
            });
        } else fabUpdate.setVisibility(View.GONE);
    }

    public String getFirmwareVersion(){

        mappedDevices = ServiceHelper.getInstance().deviceInfoData();

        String sw = "";
        String deviceId = "";

        for (Integer key : mappedDevices.keySet()) {
            try {

                if (mappedDevices.get(key) == null){
                    return "";
                }

                int counter = 1;
                for (String data : Objects.requireNonNull(mappedDevices.get(key))) {

                    if (counter == 2){
                        deviceId = data.split(" ")[1];
                    }

                    if (deviceId.equals("0") && counter == 4){
                        sw = data.split(" ")[1];
                    }

                    counter++;
                }

            } catch (NullPointerException exception){
                Log.e(TAG, "displayUI: NullPointerException -> " + exception.getLocalizedMessage());
            }
        }

        return sw;
    }

    private void ProcessTamperInfo(boolean[] tamperinfo){

        if (tamperinfo[0]){
            rbTamperInternal.setChecked(true);
            txtTamperInternal.setText("on");
        } else {
            rbTamperInternal.setChecked(false);
            txtTamperInternal.setText("off");
        }

        if (tamperinfo[1]){
            rbTamperBracket.setChecked(true);
            txtTamperBracket.setText("on");
        } else {
            rbTamperBracket.setChecked(false);
            txtTamperBracket.setText("off");
        }

        if (tamperinfo[2]){
            rbTamperPole.setChecked(true);
            txtTamperPole.setText("on");
        } else{
            rbTamperPole.setChecked(false);
            txtTamperPole.setText("off");
        }
    }

    private void ProcessPowerEvent(int chargeStatus,int batteryLevel,float inputVoltage,float temp){
        txtTemperature.setText(temp + " °C");
        txtBattery.setText(batteryLevel + " %");
        txtInput.setText(String.format("%.02f", inputVoltage) + "V");

        if(chargeStatus == 2)
            txtBatteryStatus.setText("Battery Full");
        else if (chargeStatus == 1)
            txtBatteryStatus.setText("Charging");
        else if (chargeStatus == 0)
            txtBatteryStatus.setText("Charge suspend");

        if(inputVoltage < 2)
            txtBatteryStatus.setText("No Power");

    }

    private void ProcessPrinterEvents(int status){
        if(status == 10000)
        {
            rbPrinterTray.setEnabled(false);
            rbPrinterPaper.setEnabled(false);
            rbPrinterTray.setChecked(false);
            rbPrinterPaper.setChecked(false);
            txtPrinterPaper.setText("PRINTER NOT FOUND");
            txtPrinterTray.setText("PRINTER NOT FOUND");

        }else{
            rbPrinterTray.setEnabled(true);
            rbPrinterPaper.setEnabled(true);
        }

        if((status & 0x02) == 0x02){
            rbPrinterPaper.setChecked(true);
            txtPrinterPaper.setText("Has paper");
        }else{
            rbPrinterPaper.setChecked(false);
            txtPrinterPaper.setText("Paper finished");
        }

        if((status & 0x01) == 0x01){
            rbPrinterTray.setChecked(true);
            txtPrinterTray.setText("Tray closed");
        }else{
            rbPrinterTray.setChecked(false);
            txtPrinterTray.setText("Tray open");
        }
    }

    public void syncWithService(){
        try {
            ProcessPrinterEvents(ServiceHelper.getInstance().getPrinterStates());
            ProcessTamperInfo(ServiceHelper.getInstance().getTamperStates());
            txtIMEI.setText(ServiceHelper.getInstance().getIMEI());
            txtMACAddress.setText(ServiceHelper.getInstance().getMacAddress());
            txtIPAddress.setText(ServiceHelper.getInstance().getIPAddress());
            new Thread() {
                @Override
                public void run() {
                    getAllVersions();
                }
            }.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public class HomeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            switch(intent.getAction()){
                case "TAMPER":
                    ProcessTamperInfo(intent.getBooleanArrayExtra("tampers"));
                    break;
                case "PRINTER_STATUS":
                    ProcessPrinterEvents(intent.getIntExtra("printer_status",0));
                    break;
                case "POWER":
                    ProcessPowerEvent(intent.getIntExtra("chargestatus",0),
                            intent.getIntExtra("batterylevel",0),
                            intent.getFloatExtra("inputlevel",0),
                            intent.getFloatExtra("temp",0));
                    break;
                default:
                    break;
            }
        }
    }

}
