package com.minet.mitestui;

import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
