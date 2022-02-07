package com.minet.mitestui;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FTPAsyncHandler extends AsyncTask<File, Void, Boolean> {

    private static final String TAG = "FTP_ASYNC_HANDLER";
    public FTPAsyncResponse delegate = null;

    // FTP DETAILS
    private final String _FTP_ADDRESS = "www.minet.co.za";
    private final String _FTP_USERNAME = "minet_4";
    private final String _FTP_PASSWORD = "Vv522zCDKfTERxF24vj8";
    private final int _PORT = 21;
    private String _ERROR_DIR = "ErrorLogs";
    private String _CRASH_DIR = "CrashLogs";
    private String _QC_CHECKLISTS = "MiDEVICE_QC";
    private boolean isCrashUpload = false;
    private boolean isErrorUpload = false;

    @Override
    protected Boolean doInBackground(File... fileToUpload) {
        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.connect(_FTP_ADDRESS, _PORT);
            ftpClient.login(_FTP_USERNAME, _FTP_PASSWORD);

            Log.d(TAG, "***** CONNECTED TO FTP CLIENT *****");

            // use local passive mode to pass firewall
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            Log.d(TAG, "Entered Local Passive Mode");

            if (isCrashUpload)
                ftpClient.changeWorkingDirectory(_CRASH_DIR);
            else if (isErrorUpload)
                ftpClient.changeWorkingDirectory(_ERROR_DIR);

            ftpClient.changeWorkingDirectory(_QC_CHECKLISTS);

            Log.e(TAG, "FILE TO UPLOAD -> " + fileToUpload[0].getAbsolutePath());

            // Prepare file to be uploaded to FTP Server
            FileInputStream ifile = new FileInputStream(fileToUpload[0]);

            // Upload file to FTP Server
            boolean didSave = ftpClient.storeFile(fileToUpload[0].getName(), ifile);

            Log.e(TAG, "FILE TO UPLOAD STATUS -> " + didSave);

            ifile.close();
            return didSave;
        } catch (IOException ex){
            Log.d(TAG, ex.getLocalizedMessage());
            return false;
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

    @Override
    protected void onPostExecute(Boolean result) {
        delegate.processFinish(result);
    }

}
