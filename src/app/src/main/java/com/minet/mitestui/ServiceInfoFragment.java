package com.minet.mitestui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ListAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

import za.co.megaware.MinetService.EventTag;
import za.co.megaware.MinetService.EventType;

public class ServiceInfoFragment extends Fragment {

    private static final String TAG = "SERVICE_INFO_FRAGMENT";

    // UI ELEMENTS
    View serviceInfoFragment;
    LinearLayout serviceEvents;
    Spinner datePicker;
    File _DIRECTORY;
    File[] eventFiles;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        serviceInfoFragment = inflater.inflate(R.layout.fragment_service_info, container, false);

        serviceEvents = serviceInfoFragment.findViewById(R.id.service_linear_layout);
        datePicker = serviceInfoFragment.findViewById(R.id.events_date_picker);

        datePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                populateEventsFromDatePicked(eventFiles[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        getEventDatesFromLocal();

        return serviceInfoFragment;
    }

    public void getEventDatesFromLocal(){

        _DIRECTORY = new File(Environment.getExternalStorageDirectory() + File.separator + "MiDEVICE" + File.separator + "EventsLog");
        String[] arraySpinner;

        if (_DIRECTORY.isDirectory()){
            eventFiles = _DIRECTORY.listFiles();
            if (eventFiles != null){
                arraySpinner = new String[eventFiles.length];
                for (int i = 0; i < eventFiles.length; i++) {
                    String[] split = eventFiles[i].getName().split("_");
                    arraySpinner[i] = split[split.length - 1];
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, arraySpinner);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                datePicker.setAdapter(adapter);
            }
        }
    }

    public void populateEventsFromDatePicked(File dateFile){

        serviceEvents.removeAllViewsInLayout();

        if (dateFile.canRead()){
            FileInputStream is = null;
            try {
                is = new FileInputStream(dateFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();

                while(line != null){
                    String[] _temp = line.split(",");

                    serviceEvents.addView(new EventFragment(
                            getContext(),
                            EventType.valueOf(_temp[0]),
                            EventTag.valueOf(_temp[1]),
                            _temp[2],
                            _temp[3]
                    ), 0);

                    line = reader.readLine();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public class ServiceEventReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            switch(intent.getAction()){
                case "MiDEVICE_EVENT":
                    Log.e(TAG, "onReceive: " + intent.getSerializableExtra("eventType"));
                    Log.e(TAG, "onReceive: " + intent.getSerializableExtra("eventTag"));
                    Log.e(TAG, "onReceive: " + intent.getStringExtra("eventMessage"));
                    Log.e(TAG, "onReceive: " + intent.getStringExtra("eventDate"));

                    serviceEvents.addView(new EventFragment(
                            getContext(),
                            (EventType) intent.getSerializableExtra("eventType"),
                            (EventTag) intent.getSerializableExtra("eventTag"),
                            intent.getStringExtra("eventMessage"),
                            intent.getStringExtra("eventDate")
                    ), 0);

                    break;
                default:
                    break;
            }
        }
    }

}
