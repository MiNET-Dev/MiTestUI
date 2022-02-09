package com.minet.mitestui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import za.co.megaware.MinetService.ByteParceAble;
import za.co.megaware.MinetService.IMainService;

public class ServiceHelper implements ServiceConnection {

    private static final String TAG = "ServiceHelper";

    private IMainService service;

    // LATE INITIALIZATION SINGLETON
    private static volatile ServiceHelper INSTANCE;
    public static final byte MOTHERBOARD = 0;
    public static final byte NFC = 1;
    public static final byte LED = 2;
    public static final byte LCD = 3;
    public static final byte BUZZER = 4;
    public static final byte AUDIO = 5;
    public static final byte PRINTER = 6;

    // DEVICE INFO DETAILS
    private LinkedHashMap<Integer, ArrayList<String>> map_device_info_display = new LinkedHashMap<>();

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
        try{
            this.context = context;
            Intent i = new Intent();
            i.setClassName("za.co.megaware.MinetService", "za.co.megaware.MinetService.MainService");
            boolean ret = context.bindService(i, INSTANCE, Context.BIND_IMPORTANT);
            Log.d(TAG, "initService() bound with " + ret);

            getAllDeviceInfo();
        } catch (Exception ex){
//            Toast.makeText(context.getApplicationContext(), "Error on Service connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder boundService) {
        service = IMainService.Stub.asInterface((IBinder) boundService);

        isConnected = true;

        Log.d(TAG, "onServiceConnected() connected");
//        Toast.makeText(context.getApplicationContext(), "Service Connected", Toast.LENGTH_LONG).show();
        try {
            ProcessDeviceInfo(service.GetAllDeviceInfo());
        } catch (RemoteException exception) {
//            Toast.makeText(context.getApplicationContext(), "Could not get device info", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
        Log.d(TAG, "onServiceDisconnected() Disconnected");
        isConnected = false;
        Toast.makeText(context.getApplicationContext(), "Service Disconnected", Toast.LENGTH_LONG).show();
    }

    public LinkedHashMap<Integer, ArrayList<String>> deviceInfoData(){
        return map_device_info_display;
    }

    private void ProcessDeviceInfo(byte[] buff) {

        try {
            map_device_info_display.clear();
            // Auto Populating the Spinner list for NFC's that are available
            ArrayList<String> arraySpinner = new ArrayList<String>();

            // Local Variables to keep track of the Version Building
            int major = 19;
            int minor = 20;
            int app1 = 21;
            int app2 = 22;
            Boolean inital = true;

            byte[] uuid = null;
            byte[] capabilities = null;
            byte[] version_info = null;
            byte port_id = 0;
            String swVersion = null;
            byte no_devices =  buff[0];
            byte len = 0;
            Utils.ByteStream bs = new Utils.ByteStream();
            int n = 0;
            int offset = 0;
            int totalOffset = 2;
            byte deviceId = 0;

            for(int i = 0; i < no_devices; i++){

                offset += len + 1;

                bs.AssignData(buff, offset, buff.length);
                len = bs.popbyte();

                if (inital){
                    swVersion = buff[major] + "." + buff[minor]+ "." + (buff[app1] | (buff[app2] << 8));
                    deviceId = buff[totalOffset];
                    inital = false;
                }
                else {
                    swVersion = buff[major += (31 + n)] + "." + buff[minor += (31 + n)] + "." + (buff[app1 += (31 + n)] | (buff[app2 += (31 + n)] << 8));
                    deviceId = buff[totalOffset += (31 + n)];
                    int d = 2;
                }

                bs.AssignAbsLen(len);
                port_id = bs.popbyte(); // --> Textual presentation of which port id is the device connected
                uuid = bs.popbytearray(4); // --> Display of the UUID of the device
                ArrayList<String> values = new ArrayList<String>();

                values.add("Device");
                values.add("Device " + deviceId);
                values.add("UUID: " + Utils.byteArrayToHexString(uuid, 0, uuid.length));
                values.add("SW: " + swVersion);

                version_info = bs.popbytearray(25); //version INFO we are hiding NO don't need it

                if(!bs.isEmpty()){
                    n = 0;
                    capabilities = bs.popbytearray(-1);
                    while(capabilities.length > n){
                        //IF THE FIRMWARE IS FIXED THIS SHOULD WORK
                        switch(capabilities[n]){
                            case MOTHERBOARD:  //The device is a mother board and will most probably have no other capabilities display this is a motherboard device
                                values.add("MotherBoard"); // Assigning the Device Type
                                break;
                            case NFC://The device is a NFC board and will most probably have other stuff
                                values.add("NFC"); // Assigning the Device Type
                                arraySpinner.add(String.valueOf(port_id));
                                break;
                            case LED://The device can do LED stuff
                                values.add("LED"); // Assigning the Device Type
                                break;
                            case LCD://The device can do LCD stuff
                                values.add("LCD"); // Assigning the Device Type
                                break;
                            case BUZZER://The device can do BUZZER stuff
                                values.add("BUZZER"); // Assigning the Device Type
                                break;
                            case AUDIO://The device can do AUDIO stuff
                                values.add("AUDIO"); // Assigning the Device Type
                                break;
                            case PRINTER://The device can do PRINTER stuff
                                values.add("PRINTER"); // Assigning the Device Type
                                break;
                            default:
                                break;
                        }
                        n++;
                    }
                }

                map_device_info_display.put(i,values);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context.getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, arraySpinner);
                adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
            }
        } catch (Exception ex){
            Log.d("UI_ERROR", ex.getLocalizedMessage());
        }

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

    public void getBMPIndexes() throws RemoteException {
        service.GetBMPIndexes();
    }

    public void getWAVIndexes() throws RemoteException {
        service.GetWAVIndexes();
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

    public void print(ByteParceAble _data, int feedlines, boolean cutpartial, int printID) throws RemoteException {
        service.PrintFunction(_data, feedlines, cutpartial, "MiDEVICE_PRINT-" + printID);
    }

    public String getDeviceIMEI() throws RemoteException {
        return service.GetDeviceIMEI();
    }

    public String getDeviceMacAddress() throws RemoteException {
        return service.GetDeviceMACAddress();
    }

    public String getDeviceIP() throws RemoteException {
        return service.GetDeviceIPAddress();
    }

    public int getPrinterStates() throws RemoteException {
        return service.GetPrinterStates();
    }

    public int[] getHealthStates() throws RemoteException {
        return service.GetHealthStatus();
    }

    public boolean[] getTamperStates() throws RemoteException {
        return service.GetTamperStatus();
    }

    public void getAllDeviceInfo(){
        try {
            ProcessDeviceInfo(service.GetAllDeviceInfo());
        } catch (RemoteException exception) {
            Toast.makeText(context.getApplicationContext(), "Could not get device info", Toast.LENGTH_LONG).show();
        }
    }

    public void updateFirmware() throws RemoteException {
        service.UpgradeFirmware("/storage/emulated/0/Download/firmware.bin");
    }

    public boolean isGPSWorking() throws RemoteException {
        int result = service.GPSTroubleShootStatus();

        return result == 0;
    }

}
