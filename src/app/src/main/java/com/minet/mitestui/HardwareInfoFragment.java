package com.minet.mitestui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

public class HardwareInfoFragment extends Fragment {

    private static final String TAG = "HardwareInfoFragment";

    private final String _DEVICE_NUMBER_PATH = "/storage/emulated/0/MiDEVICE/format.mi";

    // UI ELEMENTS
    public EditText editDeviceNumber;

    public Button btnSaveDevNumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hardware_info, container, false);

        editDeviceNumber = view.findViewById(R.id.inp_device_number);
        btnSaveDevNumber = view.findViewById(R.id.btn_save_device_number);

        btnSaveDevNumber.setOnClickListener(saveDevNumber);

        setDeviceNumber();

        return view;
    }

    private void setDeviceNumber(){
        File deviceNumberFile = new File(_DEVICE_NUMBER_PATH);

        if (deviceNumberFile.exists()){
            try {
                BufferedReader reader = new BufferedReader(new FileReader(deviceNumberFile));

                String currentLine;

                while ((currentLine = reader.readLine()) != null){
                    try {
                        int deviceNumber = Integer.parseInt(currentLine);

                        editDeviceNumber.setHint(String.format(Locale.getDefault(), "DEVICE %d", deviceNumber));
                    } catch (NumberFormatException exception){
                        Toast.makeText(getContext(), "Not a valid Device Number", Toast.LENGTH_LONG).show();
                        editDeviceNumber.setHint("Set Device Number");
                    }
                }

            } catch (IOException ex){
                ex.printStackTrace();
                editDeviceNumber.setHint("Error reading device number");
            }
        } else editDeviceNumber.setHint("Set Device Number");
    }

    View.OnClickListener saveDevNumber = v -> {
        try {
            File formatFile = new File("/storage/emulated/0/MiDEVICE/format.mi");

            if (formatFile.exists()){
                // WRITING VALUES TO FILE
                Utils.writeToFile(formatFile, String.valueOf(editDeviceNumber.getText()), false);
                setDeviceNumber();
                editDeviceNumber.setText("");
                editDeviceNumber.clearFocus();
            } else {
                try {
                    // Trying to create a new file
                    // We can start this thread dependent on if the file creation was successful or not
                    boolean didCreate = formatFile.createNewFile();

                    if (didCreate){
                        // WRITING VALUES TO FILE
                        Utils.writeToFile(formatFile, String.valueOf(editDeviceNumber.getText()), false);
                        setDeviceNumber();
                        editDeviceNumber.setText("");
                        editDeviceNumber.clearFocus();
                    }
                } catch (IOException ioException){
//                    ServiceLogger.getInstance().errorLog("DEV_NUMBER", ioException, this.sharePreferencesUtils);
                    Log.e(TAG, "saveNumber: ERROR -> " + ioException.getLocalizedMessage());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "saveNumber: ERROR -> " + e.getLocalizedMessage());
        }
    };

}
