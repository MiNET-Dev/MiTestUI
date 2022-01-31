package com.minet.mitestui;

import android.app.ActivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ListAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class ServiceInfoFragment extends Fragment {

    private static final String TAG = "SERVICE_INFO_FRAGMENT";

    // UI ELEMENTS
    View serviceInfoFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        serviceInfoFragment = inflater.inflate(R.layout.fragment_service_info, container, false);

//        logcat();

        return serviceInfoFragment;
    }

    public void logcat(){
        try {
//            ps | grep your.app.name
            String[] commands = new String[]{  };
            Process process = Runtime.getRuntime().exec("logcat --pid=" + getPID());
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }
//            TextView tv = (TextView)serviceInfoFragment.findViewById(R.id.textView1);
//            tv.setText(log.toString());
        }
        catch (IOException e) {
            // LOG ERROR
            Log.e(TAG, "logcat: IOException -> " + e.getLocalizedMessage());
        }
    }

    private int getPID() {

        ActivityManager activityManager = (ActivityManager) getContext().getSystemService( getContext().ACTIVITY_SERVICE );
        List<ActivityManager.RunningServiceInfo> procInfos = activityManager.getRunningServices(100);
        for(int i = 0; i < procInfos.size(); i++)
        {
            if(procInfos.get(i).process.equals("za.co.megaware.MinetService"))
            {
                Toast.makeText(getContext(), "Browser is running at PID = " + procInfos.get(i).pid, Toast.LENGTH_LONG).show();
                return procInfos.get(i).pid;
            }
        }
        return -1;
    }
}
