package com.minet.mitestui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class ExternalFragment extends Fragment {

    private static final String TAG = "EXTERNAL_FRAGMENT";

    ExternalReaderReceiver readerReceiver;

    // UI ELEMENTS
    public Button btnGetBMPIndexes;
    public Button btnGetWAVIndexes;
    public Button btnDisplayExternal;

    public Button btnUploadBackground;
    public Button btnUploadOverlay;
    public Button btnUploadSound;

    public TextView txtUploadProgress;

    public ProgressBar prgUploadProgress;

    public TableLayout tblConfig;

    private final String _BACKGROUND_DOWNLOAD_URL = "http://www.minet.co.za/midevice/bus/SnipImage.bmp.raw";
    private final String _BACKGROUND_DESTINATION_PATH = "/storage/emulated/0/Download/SnipImage.bmp.raw";

    private final String _OVERLAY_DOWNLOAD_URL = "http://www.minet.co.za/midevice/bus/AppErConfigOt/BackgroundFiles/bgok.raw";
    private final String _OVERLAY_DESTINATION_PATH = "/storage/emulated/0/Download/bgok.raw";

    private final String _SOUND_DOWNLOAD_URL = "http://www.minet.co.za/midevice/bus/AppErConfigOt/SoundFiles/beep1.raw";
    private final String _SOUND_DESTINATION_PATH = "/storage/emulated/0/Download/beep1.raw";

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_external, container, false);

        btnGetBMPIndexes = view.findViewById(R.id.btn_get_bmp_indexes);
        btnGetWAVIndexes = view.findViewById(R.id.btn_get_wav_indexes);
        btnDisplayExternal = view.findViewById(R.id.btn_display_external);

        btnUploadBackground = view.findViewById(R.id.btn_upload_back);
        btnUploadOverlay = view.findViewById(R.id.btn_upload_overlay);
        btnUploadSound = view.findViewById(R.id.btn_upload_sound);

        txtUploadProgress = view.findViewById(R.id.txt_progress);

        prgUploadProgress = view.findViewById(R.id.upload_progress);

        tblConfig = view.findViewById(R.id.tbl_config);

        btnGetBMPIndexes.setOnClickListener(getBMPIndexes);
        btnGetWAVIndexes.setOnClickListener(getWAVIndexes);
        btnDisplayExternal.setOnClickListener(displayExternal);

        btnUploadBackground.setOnClickListener(uploadBackground);
        btnUploadOverlay.setOnClickListener(uploadOverlay);
        btnUploadSound.setOnClickListener(uploadSound);

        File backgroundFile = new File(_BACKGROUND_DESTINATION_PATH);

        if (!backgroundFile.exists()){
            try {
                btnUploadBackground.setText("DOWNLOADING");
                btnUploadBackground.setEnabled(false);
                backgroundFile.createNewFile();
                Utils.downloadFile(_BACKGROUND_DOWNLOAD_URL, backgroundFile, btnUploadBackground, "UPLOAD BACKGROUND");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File overlayFile = new File(_OVERLAY_DESTINATION_PATH);

        if (!overlayFile.exists()){
            try {
                btnUploadOverlay.setText("DOWNLOADING");
                btnUploadOverlay.setEnabled(false);
                overlayFile.createNewFile();
                Utils.downloadFile(_OVERLAY_DOWNLOAD_URL, overlayFile, btnUploadOverlay, "UPLOAD OVERLAY");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File soundFile = new File(_SOUND_DESTINATION_PATH);

        if (!soundFile.exists()){
            try {
                btnUploadSound.setText("DOWNLOADING");
                btnUploadSound.setEnabled(false);
                soundFile.createNewFile();
                Utils.downloadFile(_SOUND_DOWNLOAD_URL, soundFile, btnUploadSound, "UPLOAD SOUND");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return view;
    }

    View.OnClickListener getBMPIndexes = v -> {
        try {
            ServiceHelper.getInstance().getBMPIndexes();
            btnGetBMPIndexes.setEnabled(false);
            btnGetWAVIndexes.setEnabled(false);
            tblConfig.removeAllViews();
        } catch (RemoteException exception){
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
        }
    };

    View.OnClickListener getWAVIndexes = v -> {
        try {
            ServiceHelper.getInstance().getWAVIndexes();
            btnGetBMPIndexes.setEnabled(false);
            btnGetWAVIndexes.setEnabled(false);
            tblConfig.removeAllViews();
        } catch (RemoteException exception){
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
        }
    };

    View.OnClickListener displayExternal = v -> {
        try {
            ServiceHelper.getInstance().displayExternalReader();
        } catch (RemoteException ex){
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
        }
    };

    View.OnClickListener uploadBackground = v -> {
        txtUploadProgress.setText("Starting Background Upload");
        prgUploadProgress.setVisibility(View.VISIBLE);
        ServiceHelper.getInstance().uploadBMP(0, _BACKGROUND_DESTINATION_PATH, "background");
        btnUploadOverlay.setEnabled(false);
        btnUploadSound.setEnabled(false);
        btnUploadBackground.setEnabled(false);
    };

    View.OnClickListener uploadOverlay = v -> {
        txtUploadProgress.setText("Starting Overlay Upload");
        prgUploadProgress.setVisibility(View.VISIBLE);
        ServiceHelper.getInstance().uploadBMP(1, _OVERLAY_DESTINATION_PATH, "overlay1");
        btnUploadOverlay.setEnabled(false);
        btnUploadSound.setEnabled(false);
        btnUploadBackground.setEnabled(false);
    };

    View.OnClickListener uploadSound = v -> {
        txtUploadProgress.setText("Starting Sound Upload");
        prgUploadProgress.setVisibility(View.VISIBLE);
        ServiceHelper.getInstance().uploadWAV(1, _SOUND_DESTINATION_PATH, "sound1");
        btnUploadOverlay.setEnabled(false);
        btnUploadSound.setEnabled(false);
        btnUploadBackground.setEnabled(false);
    };

    private void populateConfigTable(String config){
        try {
            JSONObject jsonObject = new JSONObject(config);
            JSONArray test = new JSONArray(jsonObject.getString("readers"));
            JSONObject textObj = test.getJSONObject(0);
            JSONArray textArr = new JSONArray(textObj.getString("config"));

            TableRow tableRow = new TableRow(getContext());

            for (int i = 0; i < textArr.length(); i++) {
                JSONObject row = textArr.getJSONObject(i);
                String name = row.getString("Name");
                TextView attName = new TextView(getContext());
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setButtonTintList(colorStateList);
                radioButton.setClickable(false);
                if (!name.equals("������������")){
                    attName.setText(name);
                    radioButton.setChecked(true);
                }

                if ((i % 2 == 0) && i > 0){
                    tblConfig.addView(tableRow);
                    tableRow = new TableRow(getContext());
                }

                TextView sample = new TextView(getContext());

                sample.setText("Index " + i + ":");
                sample.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                sample.setTextColor(getResources().getColor(R.color.black, getResources().newTheme()));
                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 20f);
                sample.setLayoutParams(params);
                tableRow.addView(sample);
                tableRow.addView(attName);
                tableRow.addView(radioButton);

                tableRow.setPadding(0, 5,0,5);
            }
            tblConfig.addView(tableRow);
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }

        try {
            btnGetBMPIndexes.setEnabled(true);
            btnGetWAVIndexes.setEnabled(true);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public class ExternalReaderReceiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {

            switch(intent.getAction()){
                case "UPLOADING_BMP":
                    prgUploadProgress.setVisibility(View.VISIBLE);
                    Log.d("UPLOADING_BMP", String.valueOf(intent.getIntExtra("status", 0)));
                    Log.d("UPLOADING_BMP", String.valueOf(intent.getIntExtra("totalFrames",0)));
                    Log.d("UPLOADING_BMP", String.valueOf(intent.getIntExtra("framesCompleted",0)));
                    Log.d("UPLOADING_BMP", String.valueOf(intent.getIntExtra("retriedFrames",0)));

                    prgUploadProgress.setMax(intent.getIntExtra("totalFrames",0));

                    if (intent.getIntExtra("framesCompleted",0) != 0){
                        prgUploadProgress.setProgress(intent.getIntExtra("framesCompleted",0), true);
                        txtUploadProgress.setText("Uploading " + intent.getIntExtra("framesCompleted",0) + "/" + intent.getIntExtra("totalFrames",0) + " frames");
                    }

                    if (intent.getIntExtra("status", 0) == 3){
                        txtUploadProgress.setText("COMPLETE");
                        btnUploadOverlay.setEnabled(true);
                        btnUploadSound.setEnabled(true);
                        btnUploadBackground.setEnabled(true);
                        try {
                            ServiceHelper.getInstance().doBlink(1, "green", 10);
                            Thread.sleep(100);
                            ServiceHelper.getInstance().doBlink(2, "green", 10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "UPLOADING_WAV":
                    prgUploadProgress.setVisibility(View.VISIBLE);
                    Log.d("UPLOADING_WAV", String.valueOf(intent.getIntExtra("status", 0)));
                    Log.d("UPLOADING_WAV", String.valueOf(intent.getIntExtra("totalFrames",0)));
                    Log.d("UPLOADING_WAV", String.valueOf(intent.getIntExtra("framesCompleted",0)));
                    Log.d("UPLOADING_WAV", String.valueOf(intent.getIntExtra("retriedFrames",0)));

                    prgUploadProgress.setMax(intent.getIntExtra("totalFrames",0));

                    if (intent.getIntExtra("framesCompleted",0) != 0){
                        prgUploadProgress.setProgress(intent.getIntExtra("framesCompleted",0), true);
                        txtUploadProgress.setText("Uploading " + intent.getIntExtra("framesCompleted",0) + "/" + intent.getIntExtra("totalFrames",0) + " frames");
                    }

                    if (intent.getIntExtra("status", 0) == 3){
                        txtUploadProgress.setText("COMPLETE");
                        btnUploadOverlay.setEnabled(true);
                        btnUploadSound.setEnabled(true);
                        btnUploadBackground.setEnabled(true);
                        try {
                            ServiceHelper.getInstance().doBlink(1, "green", 10);
                            Thread.sleep(100);
                            ServiceHelper.getInstance().doBlink(2, "green", 10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case "BMP_INDEX":
                    String config = intent.getStringExtra("indexData");
                    populateConfigTable(config);
                    break;
            }
        }
    }

}
