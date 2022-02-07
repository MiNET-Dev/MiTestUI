package com.minet.mitestui;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class FTPHandler {

    private static final String TAG = "FTP_HANDLER";
    public FTPAsyncResponse delegate = null;

    // FTP DETAILS
    private final String _FTP_ADDRESS = "www.minet.co.za";
    private final String _FTP_USERNAME = "minet_4";
    private final String _FTP_PASSWORD = "Vv522zCDKfTERxF24vj8";
    private final int _PORT = 21;
    private String _FIMRWARE_DIR = "LatestFirmware";
    private String _SERVICE_DIR = "LatestService";
    private String _OLD_UI_DIR = "LatestUI";
    private String _NEW_UI_DIR = "RefreshedLatestUI";
    private String _OPEN_TRANSIT_DIR = "LatestOpenTransit";
    private String fileToDownload;
    private static final String _VERSION_FILE = "version.txt";
    private boolean isCrashUpload = false;
    private boolean isErrorUpload = false;

    public String getAppVersion(AppVersionType type){

        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.connect(_FTP_ADDRESS, _PORT);
            ftpClient.login(_FTP_USERNAME, _FTP_PASSWORD);

            Log.d(TAG, "***** CONNECTED TO FTP CLIENT *****");

            // use local passive mode to pass firewall
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            Log.d(TAG, "Entered Local Passive Mode");

            switch (type){
                case SERVICE:
                    fileToDownload = "service.apk";
                    ftpClient.changeWorkingDirectory(_SERVICE_DIR);
                    break;
                case FIRMWARE:
                    fileToDownload = "firmware.bin";
                    ftpClient.changeWorkingDirectory(_FIMRWARE_DIR);
                    break;
                case OLD_UI:
                    fileToDownload = "ui.apk";
                    ftpClient.changeWorkingDirectory(_OLD_UI_DIR);
                    break;
                case NEW_UI:
                    fileToDownload = "refreshedui.apk";
                    ftpClient.changeWorkingDirectory(_NEW_UI_DIR);
                    break;
                case OPEN_TRANSIT:
                    fileToDownload = "latest.apk";
                    ftpClient.changeWorkingDirectory(_OPEN_TRANSIT_DIR);
                    break;
            }

            Log.e(TAG, "FILE TO DOWNLOAD -> " + _VERSION_FILE);

            // Download file to FTP Server
            InputStream inStream = ftpClient.retrieveFileStream(_VERSION_FILE);
            InputStreamReader isr = new InputStreamReader(inStream, "UTF8");

            int data = isr.read();
            String contents = "";
            while(data != -1){
                char theChar = (char) data;
                contents = contents + theChar;
                data = isr.read();
            }

            isr.close();

            ftpClient.disconnect();

            Log.e(TAG, "FILE CONTENTS -> " + contents);

            return contents;
        } catch (IOException ex){
            Log.d(TAG, Objects.requireNonNull(ex.getLocalizedMessage()));
            return null;
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    Log.d(TAG, "***** LOGGED OUT OF FTP CLIENT *****");
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
