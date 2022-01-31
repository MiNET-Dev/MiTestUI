package com.minet.mitestui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

@RequiresApi(api = Build.VERSION_CODES.N)
public class QCChecklistFragment extends Fragment implements FTPAsyncResponse {

    private static final String TAG = "QC_CHECKLIST_FRAGMENT";

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

    HashMap<Integer, Boolean> checkBoxMap = new HashMap<>();
    HashMap<Integer, Boolean> savedCheckBoxMap = new HashMap<>();

    CheckBox initCheck;

    // UI ELEMENT
    private ImageButton btnSave;
    private ImageButton btnRefresh;

    private Boolean isInitChecked = false;
    private LinearLayout qcLayout;
    View QCCheckView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        QCCheckView = inflater.inflate(R.layout.fragment_qc_check, container, false);

        btnSave = QCCheckView.findViewById(R.id.btn_save);
        btnRefresh = QCCheckView.findViewById(R.id.btn_refresh);

        initCheck = QCCheckView.findViewById(R.id.chk_init_dev_number);

        qcLayout = QCCheckView.findViewById(R.id.qc_full_checklist);

        btnSave.setOnClickListener(saveClicked);
        btnRefresh.setOnClickListener(refreshClicked);

        try {
            readFile();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        configureAllCheckBoxes(QCCheckView);

        return QCCheckView;
    }

    View.OnClickListener refreshClicked = v -> {
        btnSave.setEnabled(false);
        btnRefresh.setEnabled(false);

        btnSave.setAlpha(0.5F);
        btnRefresh.setAlpha(0.5F);

        btnSave.setClickable(false);
        btnRefresh.setClickable(false);
        for (Integer key : checkBoxMap.keySet()) {
            checkBoxMap.replace(key, false);
        }

        for (Integer key : savedCheckBoxMap.keySet()) {
            savedCheckBoxMap.replace(key, false);
        }

        configureAllCheckBoxes(QCCheckView);
        saveQCChecklist();

        btnSave.setEnabled(true);
        btnRefresh.setEnabled(true);

        btnSave.setAlpha(1F);
        btnRefresh.setAlpha(1F);

        btnSave.setClickable(true);
        btnRefresh.setClickable(true);
    };

    View.OnClickListener saveClicked = v -> {
        try {
            Toast.makeText(getContext(), "FTP SYNC STARTING", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(false);
            btnRefresh.setEnabled(false);

            btnSave.setAlpha(0.5F);
            btnRefresh.setAlpha(0.5F);

            btnSave.setClickable(false);
            btnRefresh.setClickable(false);

            Boolean didSave = saveQCChecklist();
            if (!didSave) return;

        } catch (Exception exception){
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            btnSave.setEnabled(true);
            btnRefresh.setEnabled(true);

            btnSave.setAlpha(1F);
            btnRefresh.setAlpha(1F);

            btnSave.setClickable(true);
            btnRefresh.setClickable(true);
        }

        FTPHandler ftpHandler = new FTPHandler();

        ftpHandler.delegate = this;

        File qcDoc = new File(Environment.getExternalStorageDirectory() + "/MiDEVICE/device-" + Utils.GetDeviceNumber() + "-qc.mi");

        ftpHandler.execute(qcDoc);
    };

    private void configureAllCheckBoxes(View view){
        ArrayList<View> returnViews = new ArrayList<View>();

        ArrayList<View> focusableViews = view.getTouchables();

        for (int i = 0; i < focusableViews.size(); i++) {

            View actualView = focusableViews.get(i);

            if(actualView instanceof CheckBox) {
                returnViews.add((CheckBox) actualView);
            }

        }

        for(View mView: returnViews){
            if(mView instanceof CheckBox){

                for (Integer key : savedCheckBoxMap.keySet()) {
                    if (mView.getId() == key){
                        try {
                            ((CheckBox) mView).setChecked(savedCheckBoxMap.get(key));

                            if (mView.getId() == R.id.chk_init_dev_number) {
                                isInitChecked = savedCheckBoxMap.get(key);
                                try {
                                    disableEnableControls(isInitChecked, qcLayout);
                                } catch (NullPointerException exception){

                                }
                            }

                        } catch (NullPointerException ex){
                        }
                    }
                }

                ((CheckBox) mView).setButtonTintList(colorStateList);

                ((CheckBox) mView).setOnCheckedChangeListener(checkChanged);

                checkBoxMap.put(mView.getId(), ((CheckBox) mView).isChecked());
            }
        }
    }

    private boolean saveQCChecklist(){
        if (Utils.CheckDirectory("/MiDEVICE/")){

            if (Utils.CheckFile("/MiDEVICE/device-" + Utils.GetDeviceNumber() + "-qc.mi")){

                ArrayList<String> _tempSet = new ArrayList<>();

                for (Integer key : checkBoxMap.keySet()) {
                    _tempSet.add(key + "," + checkBoxMap.get(key));
                }

                if (Utils.GetDeviceNumber() == -1){
                    Toast.makeText(getContext(), "Please make sure device number is set before saving", Toast.LENGTH_LONG).show();
                    return false;
                }

                Utils.writeToFile(new File(Environment.getExternalStorageDirectory() + "/MiDEVICE/device-" + Utils.GetDeviceNumber() + "-qc.mi"), _tempSet, false);
                return true;
            } else return false;
        } else return false;
    }

    public void readFile() throws IOException {
        FileInputStream is;
        BufferedReader reader;
        final File file = new File(Environment.getExternalStorageDirectory() + "/MiDEVICE/device-" + Utils.GetDeviceNumber() + "-qc.mi");

        if (file.exists()) {
            is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while(line != null){

                Log.d("StackOverflow", line);

                String[] _temp = line.split(",");

                if (_temp[1].equals("true"))
                    savedCheckBoxMap.put(Integer.valueOf(_temp[0]), true);
                else
                    savedCheckBoxMap.put(Integer.valueOf(_temp[0]), false);

                line = reader.readLine();
            }
        }
    }

    CompoundButton.OnCheckedChangeListener checkChanged = (compoundButton, isChecked) -> {
        try {
            Log.d(TAG, "ID -> " + compoundButton.getId());

            if (compoundButton.getId() == R.id.chk_init_dev_number) {
                isInitChecked = isChecked;
                disableEnableControls(isChecked, qcLayout);
            }

            checkBoxMap.replace(compoundButton.getId(), isChecked);
            Boolean didSave = saveQCChecklist();
            if (!didSave) Toast.makeText(getContext(), "Something went wrong with quick saving", Toast.LENGTH_LONG).show();
        } catch (Exception exception){
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
        }
    };

    private void disableEnableControls(boolean enable, ViewGroup vg){
        for (int i = 0; i < vg.getChildCount(); i++){
            View child = vg.getChildAt(i);
            child.setEnabled(enable);
            child.setAlpha(enable ? 1F : 0.5F);
            if (child instanceof ViewGroup){
                disableEnableControls(enable, (ViewGroup) child);
            }
        }
    }

    @Override
    public void processFinish(Boolean output) {
        btnSave.setEnabled(true);
        btnRefresh.setEnabled(true);

        btnSave.setAlpha(1F);
        btnRefresh.setAlpha(1F);

        btnSave.setClickable(true);
        btnRefresh.setClickable(true);

        if (output) {
            Toast.makeText(getContext(), "FTP SYNC SUCCESS", Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(getContext(), "FTP SYNC FAILED", Toast.LENGTH_LONG).show();
    }
}