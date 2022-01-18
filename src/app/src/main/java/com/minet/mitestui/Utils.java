package com.minet.mitestui;

import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class Utils {

    private static final String TAG = "UTILS";

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

}
