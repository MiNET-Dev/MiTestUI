package com.minet.mitestui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GPSFragment extends Fragment {

    private static final String TAG = "GPS_FRAGMENT";

    ImageView gpsOn;
    ImageView gpsOff;

    TextView txtLatitude;
    TextView txtLongitude;
    TextView txtSpeed;
    TextView txtAltitude;

    LinearLayout layoutGPSData;
    RelativeLayout noGPSLock;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gps, container, false);

        gpsOff = view.findViewById(R.id.gps_ic_off);
        gpsOn = view.findViewById(R.id.gps_ic_on);

        txtAltitude = view.findViewById(R.id.txt_gps_altitude);
        txtLongitude = view.findViewById(R.id.txt_gps_longitude);
        txtSpeed = view.findViewById(R.id.txt_gps_speed);
        txtLatitude = view.findViewById(R.id.txt_gps_latitude);

        layoutGPSData = view.findViewById(R.id.gps_data);
        noGPSLock = view.findViewById(R.id.no_gps_lock);

        try {
            if (ServiceHelper.getInstance().isGPSWorking()) {
                gpsOn.setVisibility(View.VISIBLE);
                layoutGPSData.setVisibility(View.VISIBLE);
            }
            else {
                gpsOff.setVisibility(View.VISIBLE);
                noGPSLock.setVisibility(View.VISIBLE);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return view;
    }

    void ProcessGPSInfo(double lat, double lon,double alt,float speed){
        if (lat != 0 && lon != 0) {
            try {
                txtAltitude.setText(String.format("%.04f", alt) + " M");
                txtLatitude.setText(String.format("%.04f", lat));
                txtLongitude.setText(String.format("%.04f", lon));
                txtSpeed.setText(String.format("%.03f", speed) + " km/h");
            } catch (Exception ex){
                Log.e(TAG, "ProcessGPSInfo: " + ex.getLocalizedMessage());
            }
        }
        else {
            noGPSLock.setVisibility(View.VISIBLE);
            layoutGPSData.setVisibility(View.INVISIBLE);
        }
    }

    public class GPSBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case "GPS_UPDATE":
                    ProcessGPSInfo(intent.getDoubleExtra("lat",0),
                            intent.getDoubleExtra("long",0),
                            intent.getDoubleExtra("alt",0),
                            intent.getFloatExtra("speed",0));
                    break;
            }
        }
    }

}
