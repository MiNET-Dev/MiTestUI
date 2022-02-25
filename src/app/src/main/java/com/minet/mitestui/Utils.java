package com.minet.mitestui;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;

public class Utils {

    private static final String TAG = "UTILS";

    private static final String _DEVICE_NUMBER_PATH = "/storage/emulated/0/MiDEVICE/format.mi";

    public static void downloadFile(String url, File outputFile, @NonNull Button btn, String originalText) {
        DataOutputStream fos = null;
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
            btn.setEnabled(true);
            btn.setText(originalText);
        } catch(IOException e) {
            Log.d(TAG, "downloadFile: DOWNLOAD FAILED -> " + e.getLocalizedMessage());
            btn.setTextSize(8);
            btn.setEnabled(false);
            btn.setText("FAILED");
            return; // swallow a 404
        } finally {
            try {
                if (fos != null){
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] ReadDataFromFile(File file, int offset, int size) {

        byte[] data = new byte[size];

        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            //Header + BMP Signature + Filesize
//                dis.skipBytes(54 + 2 + 2);
            dis.skipBytes(offset);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            dis.readFully(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static byte[] getByteArray(byte[] buffer,int start,int len){
        byte[] data = new byte[len];
        System.arraycopy(buffer, start, data, 0, len);
        return data;
    }

    public static String byteArrayToHexString(byte[] a, int offset, int maxlen) {
        StringBuilder sb = new StringBuilder(maxlen * 2);
        for (int i = 0; i < maxlen; i++)
            sb.append(String.format("%02x", a[i + offset]));

        return sb.toString();
    }

    public static void writeToFile(File outputFile, String data, boolean append) {
        try {
            // INITIALIZING STREAMS
            FileOutputStream fOut = new FileOutputStream(outputFile, append);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fOut));

            // WRITING DATA TO CACHE FILE
            bw.write(data);
            // ADDING A NEW LINE
            bw.newLine();

            // CLOSING STREAMS
            bw.close();
            fOut.close();
        }
        catch (IOException e) {
            Log.e(TAG, "writeToFile: ERROR -> " + e.getLocalizedMessage());
        }
    }

    public static void writeToFile(File outputFile, ArrayList<String> data, boolean append) {
        try {
            // INITIALIZING STREAMS
            FileOutputStream fOut = new FileOutputStream(outputFile, append);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fOut));

            for (String line : data) {
                // WRITING DATA TO CACHE FILE
                bw.write(line);
                // ADDING A NEW LINE
                bw.newLine();
            }

            // CLOSING STREAMS
            bw.close();
            fOut.close();
        }
        catch (IOException e) {
            Log.e(TAG, "writeToFile: ERROR -> " + e.getLocalizedMessage());
        }
    }

    public static boolean CheckDirectory(String pathName){
        try {
            // Setting a known location for MiNETServiceData logs
            File _DIRECTORY = new File(Environment.getExternalStorageDirectory() + pathName);
            // Checks to see if directory exists
            if (!_DIRECTORY.exists()){
                // If dir does not exist, try create one and return the status of if the dir was created
                return _DIRECTORY.mkdirs();
            } else return true;
        } catch (Exception exec){
            // Error occurred when creating the new dir
            Log.e(TAG, "CheckDirectory: " + exec.getLocalizedMessage());
            return false;
        }
    }

    public static boolean CheckFile(String pathName){
        try {
            // Setting a known location for MiNETServiceData logs
            File _DIRECTORY = new File(Environment.getExternalStorageDirectory() + pathName);
            // Checks to see if directory exists
            if (!_DIRECTORY.exists()){
                // If dir does not exist, try create one and return the status of if the dir was created
                return _DIRECTORY.createNewFile();
            } else return true;
        } catch (Exception exec){
            // Error occurred when creating the new dir
            Log.e(TAG, "CheckDirectory: " + exec.getLocalizedMessage());
            return false;
        }
    }

    public static String getLocalAppVersion(String appName, Context context){
        PackageInfo packageInfo = null;
        try {
            packageInfo = Objects.requireNonNull(context).getPackageManager().getPackageInfo(appName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        return Objects.requireNonNull(packageInfo).versionName;
    }

    public static int GetDeviceNumber(){
        File deviceNumberFile = new File(_DEVICE_NUMBER_PATH);

        int deviceNumber = -1;

        if (deviceNumberFile.exists()){
            try {
                BufferedReader reader = new BufferedReader(new FileReader(deviceNumberFile));

                String currentLine;

                while ((currentLine = reader.readLine()) != null){
                    try {
                        deviceNumber = Integer.parseInt(currentLine);
                    } catch (NumberFormatException exception){
                        deviceNumber = -1;
                    }
                }

            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
        return deviceNumber;
    }

    public static ConnectivityState hasActiveInternetConnection() {
        try {
            HttpURLConnection url = (HttpURLConnection)
                    (new URL("http://clients3.google.com/generate_204")
                            .openConnection());
            return (url.getResponseCode() == 204 && url.getContentLength() == 0) ? ConnectivityState.CONNECTED : ConnectivityState.NOT_CONNECTED;
        } catch (IOException e) {
            Log.e(TAG, "hasActiveInternetConnection: ERROR -> " + e.getLocalizedMessage());
            return ConnectivityState.NOT_CONNECTED;
        }
    }

    public static class ByteStream{
        private byte[] m_datastream;
        private int m_len;
        private int m_nPosition;
        private int m_nMaxPos;

        public ByteStream(byte[] data,int offset,int max){
            m_datastream = data;
            m_len = m_datastream.length;
            m_nPosition = offset;
            m_nMaxPos = max;
        }

        public ByteStream(){
            m_datastream = null;
            m_len = 0;
            m_nPosition = 0;
            m_nMaxPos = 0;
        }

        public int AssignData(byte[] data,int offset,int max){
            m_datastream = data;
            m_len = m_datastream.length;
            m_nPosition = offset;
            m_nMaxPos = max;

            return 0;
        }

        public int AssignAbsLen(int max){
            m_nMaxPos = m_nPosition+max;
            return 0;
        }

        public byte[] popbytearray(int len){
            byte[] data = null;
            if(isEmpty())
                return data;

            //POP remainder
            if(len == -1){
                data = getByteArray(m_datastream,m_nPosition,m_nMaxPos-m_nPosition);
            }else{
                data = getByteArray(m_datastream,m_nPosition,len);
                m_nPosition+=len;
            }

            return data;
        }

        public byte popbyte(){
            byte data = 0;
            if(isEmpty())
                return data;

            data = m_datastream[m_nPosition];
            m_nPosition++;
            return data;
        }

        public short popword(){
            short data = 0;
            if(isEmpty())
                return data;

            data = (short) (((m_datastream[m_nPosition] & 0xFF) | (m_datastream[m_nPosition+1] << 8)) & 0xFFFF);

            m_nPosition+=2;
            return data;
        }

        public boolean isEmpty(){
            if(m_nPosition >= m_len)
                return true;

            if(m_nPosition >= m_nMaxPos)
                return true;
            else
                return false;

        }
    }

}
