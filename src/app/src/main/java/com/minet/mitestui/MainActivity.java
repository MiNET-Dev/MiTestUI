package com.minet.mitestui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

//import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.internal.NavigationMenu;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import za.co.megaware.MinetService.IMainService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoginDialogFragment.LoginDialogListener, UpdateService.UpdaterStatus, ServiceHelper.ServiceListener {

    // UI ELEMENTS
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    // BROADCAST RECEIVERS
    ExternalFragment.ExternalReaderReceiver externalReaderReceiver;
    NFCFragment.NFCReceiver nfcReceiver;
    HomeFragment.HomeReceiver homeReceiver;
    UpdatesFragment.UpdatesReceiver updatesReceiver;
    GPSFragment.GPSBroadcastReceiver gpsBroadcastReceiver;
    ServiceInfoFragment.ServiceEventReceiver serviceEventReceiver;

    // TAGS
    private static final String MAIN_TAG = "MainActivity";

    // PATHS
    private final String _DOWNLOAD_URL = "http://www.minet.co.za/boot/b.bmp";
    private final String _DESTINATION_PATH = "/storage/emulated/0/Download/b.bmp";
    private final String _DEVICE_NUMBER_PATH = "/storage/emulated/0/MiDEVICE/format.mi";

    // FRAGMENTS
    HomeFragment homeFragment;
    UpdatesFragment updatesFragment;
    NavigationView navigationView;
    Menu navigationMenu;

    Menu actionBarMenu;

    private Handler handler;

    public static TechnicianModel loggedInUser = null;

    APIStore apiStore;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(ServiceHelper.getInstance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_device_number);
        menuItem.setTitle("Device: " + setDeviceNumber());

        actionBarMenu = menu;

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    protected void onStop() {
        handler.removeCallbacksAndMessages(null);
        if (externalReaderReceiver != null)
            unregisterReceiver(externalReaderReceiver);
        if (nfcReceiver != null)
            unregisterReceiver(nfcReceiver);
        if (homeReceiver != null)
            unregisterReceiver(homeReceiver);
        if (updatesReceiver != null)
            unregisterReceiver(updatesReceiver);
        if (gpsBroadcastReceiver != null)
            unregisterReceiver(gpsBroadcastReceiver);
        super.onStop();
    }

    private String setDeviceNumber(){
        File deviceNumberFile = new File(_DEVICE_NUMBER_PATH);

        if (deviceNumberFile.exists()){
            try {
                BufferedReader reader = new BufferedReader(new FileReader(deviceNumberFile));

                String currentLine;

                while ((currentLine = reader.readLine()) != null){
                    try {
                        int deviceNumber = Integer.parseInt(currentLine);

                        return String.valueOf(deviceNumber);
                    } catch (NumberFormatException exception){
                        Toast.makeText(this, "Not a valid Device Number", Toast.LENGTH_LONG).show();
                        return "not set";
                    }
                }

            } catch (IOException ex){
                ex.printStackTrace();
                return "not set";
            }
        } else return "not set";

        return "not set";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationMenu = navigationView.getMenu();

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            homeReceiver = homeFragment.new HomeReceiver();
            IntentFilter homeFilter = new IntentFilter();
            homeFilter.addAction("TAMPER");
            homeFilter.addAction("POWER");
            homeFilter.addAction("POWER");
            homeFilter.addAction("PRINTER_STATUS");

            this.registerReceiver(homeReceiver, homeFilter, null, null);

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
            navigationView.setCheckedItem(R.id.nav_home);
            setTitle("Home");
        }

        // to make the Navigation drawer icon always appear on the action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ServiceHelper.getInstance().initService(this);

        handler = new Handler(Looper.getMainLooper());

        setLoggedIn(true);
    }

    // override the onOptionsItemSelected()
    // function to implement
    // the item click listener callback
    // to open and close the navigation
    // drawer when the icon is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {

            case R.id.menu_login:
                LoginDialogFragment loginDialogFragment = new LoginDialogFragment();
                loginDialogFragment.show(getSupportFragmentManager(), LoginDialogFragment.TAG);
                break;
            case R.id.menu_logout:
                actionBarMenu.findItem(R.id.menu_login).setVisible(true);
                actionBarMenu.findItem(R.id.menu_logout).setVisible(false);
//                LoginDialogFragment loginDialogFragment = new LoginDialogFragment();
//                loginDialogFragment.show(getSupportFragmentManager(), LoginDialogFragment.TAG);
                break;
            case R.id.menu_start_service:
                startMiNETService();
                break;
            case R.id.menu_stop_service:
                stopMiNETService();
                break;
            case R.id.menu_grant_service_perms:

                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startMiNETService() {
        try {
            // START SERVICE
            boolean result = false;
            java.lang.Process process = null;
            OutputStream out = null;
            process = Runtime.getRuntime().exec("su");
            out = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(out);
            dataOutputStream
                    .writeBytes("am startservice za.co.megaware.MinetService/.MainService");
            // ??????
            dataOutputStream.flush();
            // ???????
            dataOutputStream.close();
            out.close();
            int value = process.waitFor();

            // ?????
            if (value == 0) {
                result = true;
                Toast.makeText(getApplicationContext(), "Started Service", Toast.LENGTH_LONG).show();
            } else if (value == 1) { // ??
                result = false;
                Toast.makeText(getApplicationContext(), "Could not Start Service", Toast.LENGTH_LONG).show();
            } else { // ????
                result = false;
                Toast.makeText(getApplicationContext(), "Could not Start Service", Toast.LENGTH_LONG).show();
            }
        } catch (IOException | InterruptedException e) {
            Toast.makeText(getApplicationContext(), "Error Starting Service", Toast.LENGTH_LONG).show();
            Log.e(MAIN_TAG, "STARTING_SERVICE: ERROR STARTING SERVICE -> " + e.getLocalizedMessage());
        }
    }

    private void stopMiNETService() {
        try {
            // START SERVICE
            boolean result = false;
            java.lang.Process process = null;
            OutputStream out = null;
            process = Runtime.getRuntime().exec("su");
            out = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(out);
            dataOutputStream
                    .writeBytes("am stopservice za.co.megaware.MinetService/.MainService");
            // ??????
            dataOutputStream.flush();
            // ???????
            dataOutputStream.close();
            out.close();
            int value = process.waitFor();

            // ?????
            if (value == 0) {
                result = true;
                Toast.makeText(getApplicationContext(), "Stopped Service", Toast.LENGTH_LONG).show();
            } else if (value == 1) { // ??
                result = false;
                Toast.makeText(getApplicationContext(), "Could not Stop Service", Toast.LENGTH_LONG).show();
            } else { // ????
                result = false;
                Toast.makeText(getApplicationContext(), "Could not Stop Service", Toast.LENGTH_LONG).show();
            }
        } catch (IOException | InterruptedException e) {
            Toast.makeText(getApplicationContext(), "Error Stopping Service", Toast.LENGTH_LONG).show();
            Log.e(MAIN_TAG, "STARTING_SERVICE: ERROR STARTING SERVICE -> " + e.getLocalizedMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_home:
                homeFragment = new HomeFragment();
                homeReceiver = homeFragment.new HomeReceiver();
                IntentFilter homeFilter = new IntentFilter();
                homeFilter.addAction("TAMPER");
                homeFilter.addAction("POWER");
                homeFilter.addAction("POWER");
                homeFilter.addAction("PRINTER_STATUS");

                this.registerReceiver(homeReceiver, homeFilter, null, null);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();

                setTitle("Home");
                break;
            case R.id.nav_profile:
                ProfileFragment profileFragment = new ProfileFragment();

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profileFragment).commit();

                profileFragment.setTechnician(loggedInUser);
                setLoggedIn(true);

                setTitle("Profile");
                break;
            case R.id.nav_printer:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PrinterFragment()).commit();
                setTitle("Printer");
                break;
            case R.id.nav_nfc:
                // SETTING UP BROADCAST RECEIVERS
                NFCFragment nfcFragment = new NFCFragment();
                nfcReceiver = nfcFragment.new NFCReceiver();
                IntentFilter nfcFilter = new IntentFilter();
                nfcFilter.addAction("MIFARE_PRESENT");
                nfcFilter.addAction("MIFARE_REMOVED");

                this.registerReceiver(nfcReceiver, nfcFilter, null, null);

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, nfcFragment).commit();
                setTitle("NFC's");
                break;
            case R.id.nav_external:
                // SETTING UP BROADCAST RECEIVERS
                ExternalFragment externalFragment = new ExternalFragment();
                externalReaderReceiver = externalFragment.new ExternalReaderReceiver();
                IntentFilter externalFilter = new IntentFilter();
                externalFilter.addAction("UPLOADING_BMP");
                externalFilter.addAction("UPLOADING_WAV");
                externalFilter.addAction("BMP_INDEX");

                this.registerReceiver(externalReaderReceiver, externalFilter, null, null);

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, externalFragment).commit();
                setTitle("External Reader");
                break;
            case R.id.nav_gps:
                GPSFragment gpsFragment = new GPSFragment();
                gpsBroadcastReceiver = gpsFragment.new GPSBroadcastReceiver();

                IntentFilter gpsFilter = new IntentFilter();
                gpsFilter.addAction("GPS_UPDATE");

                this.registerReceiver(gpsBroadcastReceiver, gpsFilter, null, null);

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, gpsFragment).commit();
                setTitle("GPS");
                break;
            case R.id.nav_lights:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LightsFragment()).commit();
                setTitle("Lights");
                break;
            case R.id.nav_camera:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CameraFragment()).commit();
                setTitle("Camera");
                break;
            case R.id.nav_hardware_info:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HardwareInfoFragment()).commit();
                setTitle("Hardware Info");
                break;
            case R.id.nav_about:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
                setTitle("About");
                break;
//            case R.id.nav_tampers:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TampersFragment()).commit();
//                setTitle("Tampers");
//                break;
//            case R.id.nav_telemetry:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TelemetryFragment()).commit();
//                setTitle("Telemetry");
//                break;
            case R.id.nav_service_info:
                // SETTING UP BROADCAST RECEIVERS
                ServiceInfoFragment serviceInfo = new ServiceInfoFragment();
                serviceEventReceiver = serviceInfo.new ServiceEventReceiver();
                IntentFilter serviceEventFilter = new IntentFilter();
                serviceEventFilter.addAction("MiDEVICE_EVENT");

                this.registerReceiver(serviceEventReceiver, serviceEventFilter, null, null);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, serviceInfo).commit();
                setTitle("Service Info");
                break;
            case R.id.nav_updates:
                // SETTING UP BROADCAST RECEIVERS
                updatesFragment = new UpdatesFragment();
                updatesReceiver = updatesFragment.new UpdatesReceiver();
                IntentFilter updatesFilter = new IntentFilter();
                updatesFilter.addAction("UPLOADING_FIRMWARE");

                this.registerReceiver(updatesReceiver, updatesFilter, null, null);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, updatesFragment).commit();
                setTitle("Updates");
                break;
            case R.id.nav_qc_check:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new QCChecklistFragment()).commit();
                setTitle("QC Checklist");
                break;
            case R.id.nav_feedback:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FeedbackFragment()).commit();
                setTitle("Feedback");
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions();
        }
    }

    public void requestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_WIFI_STATE
        }, 1000);
    }

    @Override
    public void onLoginDialogPositiveClick(DialogFragment dialog, String username, String pin) {
        apiStore = new APIStore();
        try {
            apiStore.Login(username, pin, loginResponse);
//            apiStore.Login("Nikitha", "1234", loginResponse);
//            apiStore.Login("lukegeyser", "1964", loginResponse);
        } catch (Exception ex){
            //
        }
    }

    Callback<ResponseBody> loginResponse = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            Log.d("retrofit", "Send Data success");
            try {

                if (response.isSuccessful()){
                    String  bodyString = new String(response.body().bytes());

                    Gson gson = new GsonBuilder().create();
                    try {
                        loggedInUser = gson.fromJson(bodyString, TechnicianModel.class);
                        setLoggedIn(true);
                        actionBarMenu.findItem(R.id.menu_login).setVisible(false);
                        actionBarMenu.findItem(R.id.menu_logout).setVisible(true);
                        navigationMenu.findItem(R.id.nav_profile).setEnabled(true);
                    } catch (Exception e) {
                        // handle failure to read error
                        Log.v("gson error","error when gson process");
                        Toast.makeText(getApplicationContext(), "error logging in", Toast.LENGTH_LONG).show();
                    }

                } else {
                    // FAILED
                    Log.e(MAIN_TAG, "onResponse: error logging in");
                    Toast.makeText(getApplicationContext(), "error logging in", Toast.LENGTH_LONG).show();
                }

            } catch (IOException e) {
                Log.e(MAIN_TAG, "onResponse: error -> " + e.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), "error logging in", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.d("retrofit", "Send Data failure");
            Toast.makeText(getApplicationContext(), "error logging in", Toast.LENGTH_LONG).show();
        }
    };

    private void setLoggedIn(boolean isLoggedIn) {
        navigationMenu.findItem(R.id.nav_printer).setEnabled(isLoggedIn);
//        navigationMenu.findItem(R.id.nav_profile).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_nfc).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_external).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_gps).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_lights).setEnabled(isLoggedIn);
//        navigationMenu.findItem(R.id.nav_camera).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_hardware_info).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_about).setEnabled(isLoggedIn);
//        navigationMenu.findItem(R.id.nav_tampers).setEnabled(isLoggedIn);
//        navigationMenu.findItem(R.id.nav_telemetry).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_service_info).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_updates).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_qc_check).setEnabled(isLoggedIn);
//        navigationMenu.findItem(R.id.nav_feedback).setEnabled(isLoggedIn);
    }

    @Override
    public void onLoginDialogNegativeClick(DialogFragment dialog) {
        dialog.getDialog().cancel();
    }

    @Override
    public void onCreateLocalFile() {
        updatesFragment.onCreateLocalFile();
    }

    @Override
    public void onDownloadFile() {
        updatesFragment.onDownloadFile();
    }

    @Override
    public void onDownloadComplete() {
        updatesFragment.onDownloadComplete();
    }

    @Override
    public void onDownloadFailed() {
        updatesFragment.onDownloadFailed();
    }

    @Override
    public void onInstall() {
        updatesFragment.onInstall();
    }

    @Override
    public void onInstallComplete() {
        updatesFragment.onInstallComplete();
    }

    @Override
    public void onInstallFailed() {
        updatesFragment.onInstallFailed();
    }

    @Override
    public void onServiceHelperConnected() {
        homeFragment.syncWithService();
    }
}