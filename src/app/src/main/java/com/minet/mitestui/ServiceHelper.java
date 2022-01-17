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
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
        Log.d(TAG, "onServiceDisconnected() Disconnected");
        isConnected = false;
//        Toast.makeText(MainActivity.this, "Service Disconnected", Toast.LENGTH_LONG).show();
    }

    public void TestLights(String color){
        try {
            if (isConnected)
                service.DoBlinkStatic(color);
        } catch (RemoteException e){
            Log.e(TAG, "TestLights: error -> " + e.getLocalizedMessage() );
        }
    }

}
