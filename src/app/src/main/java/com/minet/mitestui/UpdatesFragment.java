package com.minet.mitestui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

public class UpdatesFragment extends Fragment implements FTPAsyncResponse {

    private static final String TAG = "UPDATES_FRAGMENT";

    TextView serviceVersion;
    TextView firmwareVersion;
    TextView OldUIVersion;
    TextView NewUIVersion;
    TextView bpassVersion;
    TextView txtProgressValue;

    ImageView serviceIcon;
    ImageView firmwareIcon;
    ImageView OldUIIcon;
    ImageView NewUIIcon;
    ImageView bpassIcon;

    ImageButton btnService;
    ImageButton btnFirmware;
    ImageButton btnOldUI;
    ImageButton btnNewUI;
    ImageButton btnBpass;

    LinkedHashMap<Integer, ArrayList<String>> mappedDevices;

    UpdateService updateService;

    ProgressBar progressUpdates;

    LinearLayout updatesLoading;
    ScrollView updatesMainContent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_updates, container, false);

        serviceVersion = view.findViewById(R.id.txt_update_service_version);
        firmwareVersion = view.findViewById(R.id.txt_update_firmware_version);
        OldUIVersion = view.findViewById(R.id.txt_update_ui_old_version);
        NewUIVersion = view.findViewById(R.id.txt_update_new_ui_version);
        txtProgressValue = view.findViewById(R.id.txt_progress_value);
        bpassVersion = view.findViewById(R.id.txt_update_bpass_version);

        serviceIcon = view.findViewById(R.id.updates_ic_check_service);
        firmwareIcon = view.findViewById(R.id.updates_ic_firmware);
        OldUIIcon = view.findViewById(R.id.updates_ic_ui_old);
        NewUIIcon = view.findViewById(R.id.updates_ic_ui_new);
        bpassIcon = view.findViewById(R.id.updates_ic_bpass);

        btnService = view.findViewById(R.id.btn_update_service);
        btnFirmware = view.findViewById(R.id.btn_update_firmware);
        btnOldUI = view.findViewById(R.id.btn_update_ui_old);
        btnNewUI = view.findViewById(R.id.btn_update_new_ui);
        btnBpass = view.findViewById(R.id.btn_update_bpass);

        progressUpdates = view.findViewById(R.id.progress_updates);

        updatesLoading = view.findViewById(R.id.updated_info_loading);
        updatesMainContent = view.findViewById(R.id.updates_main_content);

        btnService.setOnClickListener(onServiceUpdate);
        btnFirmware.setOnClickListener(onFirmwareUpdate);
        btnOldUI.setOnClickListener(onOldUIUpdate);
        btnNewUI.setOnClickListener(onNewUIUpdate);
        btnBpass.setOnClickListener(onBpassUpdate);

        progressUpdates.setMax(5);

        new Thread() {
            @Override
            public void run() {
                getAllVersions();
            }
        }.start();

        return view;
    }

    View.OnClickListener onServiceUpdate = v -> {
        updateService = new UpdateService(getContext());
        updateService.setUpdateType(AppVersionType.SERVICE);
        updateService.start();
        disableButtons(true);
    };

    View.OnClickListener onFirmwareUpdate = v ->
    {
        File BetaFile = new File("/storage/emulated/0/Download/firmware_beta.bin");
        if (BetaFile.exists())
        {
            Toast.makeText(getContext(), "BETA FIRMWARE DETECTED", Toast.LENGTH_LONG).show();
        }

        updateService = new UpdateService(getContext());
        updateService.setUpdateType(AppVersionType.FIRMWARE);
        updateService.start();
        disableButtons(true);
    };

    View.OnClickListener onOldUIUpdate = v -> {
        updateService = new UpdateService(getContext());
        updateService.setUpdateType(AppVersionType.OLD_UI);
        updateService.start();
        disableButtons(true);
    };

    View.OnClickListener onNewUIUpdate = v -> {
        updateService = new UpdateService(getContext());
        updateService.setUpdateType(AppVersionType.NEW_UI);
        updateService.start();
        disableButtons(true);
    };

    View.OnClickListener onBpassUpdate = v -> {
        updateService = new UpdateService(getContext());
        updateService.setUpdateType(AppVersionType.OPEN_TRANSIT);
        updateService.start();
        disableButtons(true);
    };

    private void disableButtons(Boolean disable){
        btnNewUI.setEnabled(!disable);
//        btnNewUI.tint(ContextCompat.getColor(getContext(), R.color.btnGreen));
        btnFirmware.setEnabled(!disable);
        btnOldUI.setEnabled(!disable);
        btnService.setEnabled(!disable);
    }

    private void getAllVersions() {

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            updatesMainContent.setVisibility(View.GONE);
            updatesLoading.setVisibility(View.VISIBLE);
        });

        FTPHandler handler = new FTPHandler();

        String latestService = handler.getAppVersion(AppVersionType.SERVICE);
        String localService = Utils.getLocalAppVersion("za.co.megaware.MinetService", getContext()) == null ? "not installed" : Utils.getLocalAppVersion("za.co.megaware.MinetService", getContext());

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            serviceVersion.setText(localService);
            if (latestService.equals(localService)){
                serviceVersion.setTextColor(ContextCompat.getColor(getContext(), R.color.btnGreen));
                serviceIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_check_green));
            } else {
                serviceVersion.setTextColor(ContextCompat.getColor(getContext(), R.color.btnRed));
                serviceIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_priority));
            }
        });

        String latestFirmware = handler.getAppVersion(AppVersionType.FIRMWARE);
        String localFirmware = getFirmwareVersion();

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            firmwareVersion.setText(localFirmware);
            if (latestFirmware.equals(localFirmware)){
                firmwareVersion.setTextColor(ContextCompat.getColor(getContext(), R.color.btnGreen));
                firmwareIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_check_green));
            } else {
                firmwareVersion.setTextColor(ContextCompat.getColor(getContext(), R.color.btnRed));
                firmwareIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_priority));
            }
        });

        String latestOldUI = handler.getAppVersion(AppVersionType.OLD_UI);
        String localOldUI = Utils.getLocalAppVersion("com.silver.userinterfacealpha", getContext())== null ? "not installed" : Utils.getLocalAppVersion("com.silver.userinterfacealpha", getContext());

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            OldUIVersion.setText(localOldUI);
            if (latestOldUI.equals(localOldUI)){
                OldUIVersion.setTextColor(ContextCompat.getColor(getContext(), R.color.btnGreen));
                OldUIIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_check_green));
            } else {
                OldUIVersion.setTextColor(ContextCompat.getColor(getContext(), R.color.btnRed));
                OldUIIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_priority));
            }
        });

        String latestNewUI = handler.getAppVersion(AppVersionType.NEW_UI);
        String localNewUI = Utils.getLocalAppVersion("com.minet.mitestui", getContext()) == null ? "not installed" : Utils.getLocalAppVersion("com.minet.mitestui", getContext());

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            NewUIVersion.setText(localNewUI);
            if (latestNewUI.equals(localNewUI)){
                NewUIVersion.setTextColor(ContextCompat.getColor(getContext(), R.color.btnGreen));
                NewUIIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_check_green));
            } else {
                NewUIVersion.setTextColor(ContextCompat.getColor(getContext(), R.color.btnRed));
                NewUIIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_priority));
            }


        });

        String latestBpass = handler.getAppVersion(AppVersionType.OPEN_TRANSIT);
        String localBpass = Utils.getLocalAppVersion("com.bps.bpass.mainpackage", getContext()) == null ? "not installed" : Utils.getLocalAppVersion("com.bps.bpass.mainpackage", getContext());

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            bpassVersion.setText(localBpass);
            if (latestBpass.equals(localBpass)){
                bpassVersion.setTextColor(ContextCompat.getColor(getContext(), R.color.btnGreen));
                bpassIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_check_green));
            } else {
                bpassVersion.setTextColor(ContextCompat.getColor(getContext(), R.color.btnRed));
                bpassIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_priority));
            }
        });

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            updatesMainContent.setVisibility(View.VISIBLE);
            updatesLoading.setVisibility(View.GONE);
        });
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

    public void onCreateLocalFile() {
        Log.e(TAG, "onCreateLocalFile: ");
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            progressUpdates.setVisibility(View.VISIBLE);
            progressUpdates.setProgress(1);
            txtProgressValue.setText("Created Local File");
            txtProgressValue.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        });
    }

    public void onDownloadFile() {
        Log.e(TAG, "onDownloadFile: ");

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            progressUpdates.setProgress(2);
            txtProgressValue.setText("Downloading...");
            txtProgressValue.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        });
    }

    public void onDownloadComplete() {
        Log.e(TAG, "onDownloadComplete: ");

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            txtProgressValue.setText("Download Complete");
            txtProgressValue.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        });

        if (updateService.getAppVersionType() == AppVersionType.FIRMWARE){
            try {
                ServiceHelper.getInstance().updateFirmware();
            } catch (RemoteException e) {
                Log.d(TAG, "Error with service uploading firmware");
            }
        }
    }

    public void onDownloadFailed() {
        Log.e(TAG, "onDownloadFailed: ");

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            txtProgressValue.setText("Download Failed");
            txtProgressValue.setTextColor(ContextCompat.getColor(getContext(), R.color.btnRed));
            new Thread() {
                @Override
                public void run() {
                    getAllVersions();
                }
            }.start();
            disableButtons(false);
        });
    }

    public void onInstall() {
        Log.e(TAG, "onInstall: ");
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            progressUpdates.setProgress(4);
            txtProgressValue.setText("Installing...");
            txtProgressValue.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        });
    }

    public void onInstallComplete() {
        Log.e(TAG, "onInstallComplete: ");
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            progressUpdates.setProgress(5);
            txtProgressValue.setText("Install Complete");
            txtProgressValue.setTextColor(ContextCompat.getColor(getContext(), R.color.btnGreen));
            new Thread() {
                @Override
                public void run() {
                    getAllVersions();
                }
            }.start();
            disableButtons(false);
        });
    }

    public void onInstallFailed() {
        Log.e(TAG, "onInstallFailed: ");
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            txtProgressValue.setText("Install Failed");
            txtProgressValue.setTextColor(ContextCompat.getColor(getContext(), R.color.btnRed));
            new Thread() {
                @Override
                public void run() {
                    getAllVersions();
                }
            }.start();
            disableButtons(false);
        });
    }

    @Override
    public void processFinish(Boolean output) {

    }

    public class UpdatesReceiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {

            switch(intent.getAction()){
                case "UPLOADING_FIRMWARE":

                    String totalFrames = String.valueOf(intent.getIntExtra("totalFrames",0));
                    String framesCompleted = String.valueOf(intent.getIntExtra("framesCompleted",0));

                    if (!framesCompleted.equals("0")){
                        Log.d("UPLOADING_FIRMWARE", intent.getStringExtra("status"));
                        Log.d("UPLOADING_FIRMWARE", totalFrames);
                        Log.d("UPLOADING_FIRMWARE", framesCompleted);
                        Log.d("UPLOADING_FIRMWARE", String.valueOf(intent.getIntExtra("retriedFrames",0)));
                        String btnText = framesCompleted + "/" + totalFrames;
                        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                            progressUpdates.setVisibility(View.VISIBLE);
                            progressUpdates.setMax(Integer.parseInt(totalFrames));
                            progressUpdates.setProgress(Integer.parseInt(framesCompleted));
                            txtProgressValue.setText("Uploading...");
                            txtProgressValue.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                        });
                    }

                    if (intent.getStringExtra("status").equals("Write Process Complete")) {
                        Toast.makeText(getContext(), "FIRMWARE UPGRADE COMPLETE", Toast.LENGTH_LONG).show();
                        try {
                            ServiceHelper.getInstance().doBlink(1, "green", 10);
                            Thread.sleep(100);
                            ServiceHelper.getInstance().doBlink(2, "green", 10);

                            Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                                progressUpdates.setProgress(Integer.parseInt(framesCompleted));
                                txtProgressValue.setText("Install Complete");
                                txtProgressValue.setTextColor(ContextCompat.getColor(getContext(), R.color.btnGreen));
                                new Thread() {
                                    @Override
                                    public void run() {
                                        getAllVersions();
                                    }
                                }.start();
                                disableButtons(false);
                            });

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (intent.getStringExtra("status").equals("Exiting the Write")){
                        Toast.makeText(getContext(), "FIRMWARE UPGRADE FAILED", Toast.LENGTH_LONG).show();
                        try {
                            ServiceHelper.getInstance().doBlink(1, "red", 10);
                            Thread.sleep(100);
                            ServiceHelper.getInstance().doBlink(2, "red", 10);
                            Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                                txtProgressValue.setText("Install Failed");
                                txtProgressValue.setTextColor(ContextCompat.getColor(getContext(), R.color.btnRed));
                                new Thread() {
                                    @Override
                                    public void run() {
                                        getAllVersions();
                                    }
                                }.start();
                                disableButtons(false);
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (intent.getStringExtra("status").equals("File Not Found")){
                        Toast.makeText(getContext(), "FILE NOT FOUND", Toast.LENGTH_LONG).show();
                        try {
                            ServiceHelper.getInstance().doBlink(1, "magenta", 10);
                            Thread.sleep(100);
                            ServiceHelper.getInstance().doBlink(2, "magenta", 10);
                            Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                                txtProgressValue.setText("Firmware file not found");
                                txtProgressValue.setTextColor(ContextCompat.getColor(getContext(), R.color.btnRed));
                                new Thread() {
                                    @Override
                                    public void run() {
                                        getAllVersions();
                                    }
                                }.start();
                                disableButtons(false);
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
            }
        }
    }

}
