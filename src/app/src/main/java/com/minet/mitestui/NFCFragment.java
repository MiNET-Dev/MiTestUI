package com.minet.mitestui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

public class NFCFragment extends Fragment {

    private static final String TAG = "NFC_FRAGMENT";

    // UI ELEMENTS
    View nfcView;
    ImageButton btnRefresh;
    LinearLayout linearLayout;
    LinkedHashMap<Integer, ArrayList<String>> mappedDevices;
    LinkedHashMap<Integer, View> deviceViews = new LinkedHashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        nfcView = inflater.inflate(R.layout.fragment_nfc, container, false);

        btnRefresh = nfcView.findViewById(R.id.btn_get_device_info);
        linearLayout = nfcView.findViewById(R.id.linear_device_nfc_list);

        btnRefresh.setOnClickListener(refreshDeviceInfo);

        displayUI();

        return nfcView;
    }

    View.OnClickListener refreshDeviceInfo = v -> {
        ServiceHelper.getInstance().getAllDeviceInfo();
        displayUI();
    };
    
    private void displayUI(){
        linearLayout.removeAllViewsInLayout();

        deviceViews.clear();

        mappedDevices = ServiceHelper.getInstance().deviceInfoData();

        for (Integer key : mappedDevices.keySet()) {
            try {

                String deviceId = "";
                String uuid = "";
                String sw = "";
                String hw = "";
                StringBuilder capabilities = new StringBuilder();

                if (mappedDevices.get(key) == null){
                     return;
                }

                int counter = 1;
                for (String data : Objects.requireNonNull(mappedDevices.get(key))) {

                    if (counter == 2){
                        deviceId = data.split(" ")[1];
                    }

                    if (counter == 3){
                        uuid = data.split(" ")[1];
                    }

                    if (counter == 4){
                        sw = data.split(" ")[1];
                    }

                    if (counter >= 5){
                        capabilities.append(data).append(" ");
                    }

                    counter++;
                }

                if (!deviceId.equals("0")){
                    NFCDeviceInfoFragment infoFragment = new NFCDeviceInfoFragment(getContext(), deviceId, hw, sw, uuid, capabilities.toString(), R.id.device_one);

                    linearLayout.addView(infoFragment);
                    deviceViews.put(Integer.valueOf(deviceId), infoFragment);
                }

            } catch (NullPointerException exception){
                Log.e(TAG, "displayUI: NullPointerException -> " + exception.getLocalizedMessage());
            }
        }
        
    }

    public class NFCReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            switch(intent.getAction()){
                case "MIFARE_PRESENT":

                    Log.d("INTENT_INFO", intent.toString());

                    byte m_mifareReaderNo = intent.getByteExtra("deviceID", (byte) 1);
                    byte[] m_mifareUUID = intent.getByteArrayExtra("uuid");
                    byte[] m_mifare_data = intent.getByteArrayExtra("data");
                    boolean MiFarePresent = true;
                    for (Integer key : deviceViews.keySet()) {
                        if (m_mifareReaderNo == key){
                            NFCDeviceInfoFragment temp = (NFCDeviceInfoFragment) deviceViews.get(key);
                            if (temp != null){
                                temp.setNFCPresent(true, m_mifare_data);
                                temp.showCardData(true);
                            }
                        }
                    }
                    break;
                case "MIFARE_REMOVED":
                    int m_mifareReaderNoRemoved =  intent.getIntExtra("deviceID",1);
                    for (Integer key : deviceViews.keySet()) {
                        if (m_mifareReaderNoRemoved == key){
                            NFCDeviceInfoFragment temp = (NFCDeviceInfoFragment) deviceViews.get(key);
                            if (temp != null){
                                temp.setNFCPresent(false, null);
                                temp.showCardData(false);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
