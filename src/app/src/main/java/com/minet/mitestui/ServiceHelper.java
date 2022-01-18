package com.minet.mitestui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import za.co.megaware.MinetService.IMainService;

public class ServiceHelper implements ServiceConnection {

    private static final String TAG = "ServiceHelper";

    private IMainService service;

    // LATE INITIALIZATION SINGLETON
    private static volatile ServiceHelper INSTANCE;

    private Context context;

    private boolean isConnected = false;

    // PRIVATE CONSTRUCTOR TO FORCE USE OF getInstance() TO CREATE SINGLETON OBJECT
    private ServiceHelper() { }

    // ONLY ONE THREAD CAN EXECUTE THIS AT A TIME
    public static ServiceHelper getInstance() {

        if (INSTANCE == null){

            synchronized (ServiceHelper.class){
                // CHECK AGAIN AS MULTIPLE THREADS
                // CAN REACH THIS POINT
                if (INSTANCE == null)
                    INSTANCE = new ServiceHelper();
            }

        }
        return INSTANCE;
    }

    public void initService(Context context){
        this.context = context;
        Intent i = new Intent();
        i.setClassName("za.co.megaware.MinetService", "za.co.megaware.MinetService.MainService");
        boolean ret = context.bindService(i, INSTANCE, Context.BIND_IMPORTANT);
        Log.d(TAG, "initService() bound with " + ret);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder boundService) {
        service = IMainService.Stub.asInterface((IBinder) boundService);

        isConnected = true;

//        startBootStrapTimer(false);

        Log.d(TAG, "onServiceConnected() connected");
        Toast.makeText(context.getApplicationContext(), "Service Connected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
        Log.d(TAG, "onServiceDisconnected() Disconnected");
        isConnected = false;
        Toast.makeText(context.getApplicationContext(), "Service Disconnected", Toast.LENGTH_LONG).show();
    }

    public void testLights(String color){
        try {
            if (isConnected)
                service.DoBlinkStatic(color);
        } catch (RemoteException e){
            Log.e(TAG, "TestLights: error -> " + e.getLocalizedMessage());
        }
    }

    public void doBlink(int readerNo, String color, int iterations){
        try {
            service.DoBlink(readerNo, color, iterations);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void getBMPIndexes(){
        try {
            service.GetBMPIndexes();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void getWAVIndexes(){
        try {
            service.GetWAVIndexes();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void uploadBMP(int index, String path, String name){
        try {
            service.ConfigureBMP(index, path, name);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void uploadWAV(int index, String path, String name){
        try {
            service.ConfigureAudio(index, path, name);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void displayExternalReader() throws RemoteException {
        service.DisplayExternalReader("v1000", 1, 1, 4);
    }

}
