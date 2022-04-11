package com.minet.mitestui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class UpdateService extends Thread {

    private static final String TAG = "UPDATER_SERVICE";
    private String _DOWNLOAD_URL = "http://www.minet.co.za/midevice/bus/";
    private String _DESTINATION_PATH = "/storage/emulated/0/Download/";
    private String fileToDownload;
    private String _FIMRWARE_DIR = "LatestFirmware/";
    private String _SERVICE_DIR = "LatestService/";
    private String _OLD_UI_DIR = "LatestUI/";
    private String _NEW_UI_DIR = "RefreshedLatestUI/";
    private String _BPASS_DIR = "LatestOpenTransit/";
    private AppVersionType appVersionType;
    private Context context;

    private boolean _isRunning = false;

    public interface UpdaterStatus {
        void onCreateLocalFile();
        void onDownloadFile();
        void onDownloadComplete();
        void onDownloadFailed();
        void onInstall();
        void onInstallComplete();
        void onInstallFailed();
    }

    UpdaterStatus listener;

    public UpdateService(Context context){
        listener = (UpdaterStatus) context;
        this.context = context;
    }

    @Override
    public void run() {
        _isRunning = true;

        File File = new File(_DESTINATION_PATH);

        if (!File.exists())
        {
            try
            {
                File.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        listener.onCreateLocalFile();

        downloadFile(_DOWNLOAD_URL, File);

        if (appVersionType != AppVersionType.FIRMWARE)
        {
            silentInstall(File);
        }

        _isRunning = false;
    }

    public AppVersionType getAppVersionType(){
        return appVersionType;
    }

    public void setUpdateType(AppVersionType type){
        switch (type){
            case SERVICE:
                fileToDownload = "service.apk";
                _DOWNLOAD_URL += _SERVICE_DIR;
                break;
            case FIRMWARE:
                fileToDownload = "firmware.bin";
                _DOWNLOAD_URL += _FIMRWARE_DIR;
                break;
            case OLD_UI:
                fileToDownload = "ui.apk";
                _DOWNLOAD_URL += _OLD_UI_DIR;
                break;
            case NEW_UI:
                fileToDownload = "refreshedui.apk";
                _DOWNLOAD_URL += _NEW_UI_DIR;
                break;
            case OPEN_TRANSIT:
                fileToDownload = "latest.apk";
                _DOWNLOAD_URL += _BPASS_DIR;
        }

        appVersionType = type;
        _DESTINATION_PATH += fileToDownload;
        _DOWNLOAD_URL += fileToDownload;
    }

    public boolean getRunning(){
        return _isRunning;
    }

    private void downloadFile(String url, File outputFile) {
        DataOutputStream fos = null;
        listener.onDownloadFile();
        try {
            Log.d(TAG, "downloadFile: STARTING DOWNLOAD");
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(u.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();

            fos = new DataOutputStream(new FileOutputStream(outputFile));
            fos.write(buffer);
            fos.flush();
            fos.close();
            Log.d(TAG, "downloadFile: FINISHED DOWNLOAD");
            listener.onDownloadComplete();
        } catch(IOException e) {
            Log.d(TAG, "downloadFile: DOWNLOAD FAILED -> " + e.getLocalizedMessage());
            listener.onDownloadFailed();
            return; // swallow a 404
        } finally {
            try {
                if (fos != null){
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                listener.onDownloadFailed();
            }
        }
    }

    public void silentInstall(File file) {
        listener.onInstall();
        if (file == null) {
            listener.onInstallFailed();
            return;
        }//from w w  w .ja  v  a2  s  .c o m
        Log.d(TAG, "silentInstall: STARTING INSTALL");
        boolean result = false;
        java.lang.Process process = null;
        OutputStream out = null;
        try {
            process = Runtime.getRuntime().exec("su");
            out = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(out);
            //dataOutputStream.writeBytes("chmod 777 " + file.getPath() + "\n");
            dataOutputStream
                    .writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -g -r "
                            + file.getPath());

            if (appVersionType == AppVersionType.NEW_UI){
                Uri packageURI = Uri.parse(context.getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, packageURI);
                intent.setDataAndType(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", new File(_DESTINATION_PATH)), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
                listener.onInstallComplete();
            } else {
                // ??????
                dataOutputStream.flush();
                // ???????
                dataOutputStream.close();
                out.close();
                int value = process.waitFor();

                // ?????
                if (value == 0) {
                    Log.d(TAG, "silentInstall: INSTALLED SUCCESSFULLY");
                    listener.onInstallComplete();
                    result = true;
                } else if (value == 1) { // ??
                    Log.e(TAG, "silentInstall: ERROR INSTALLING");
                    listener.onInstallFailed();
                    result = false;
                } else { // ????
                    Log.e(TAG, "silentInstall: ERROR INSTALLING");
                    listener.onInstallFailed();
                    result = false;
                }
            }
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "silentInstall: ERROR INSTALLING -> " + e.getLocalizedMessage());
            listener.onInstallFailed();
        }
    }
}
