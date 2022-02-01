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
import android.os.IBinder;
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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import za.co.megaware.MinetService.IMainService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoginDialogFragment.LoginDialogListener {

    // UI ELEMENTS
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    // BROADCAST RECEIVERS
    ExternalFragment.ExternalReaderReceiver externalReaderReceiver;
    NFCFragment.NFCReceiver nfcReceiver;

    // TAGS
    private static final String MAIN_TAG = "MainActivity";

    // PATHS
    private final String _DOWNLOAD_URL = "http://www.minet.co.za/boot/b.bmp";
    private final String _DESTINATION_PATH = "/storage/emulated/0/Download/b.bmp";
    private final String _DEVICE_NUMBER_PATH = "/storage/emulated/0/MiDEVICE/format.mi";

    // FRAGMENTS
    HomeFragment homeFragment;
    NavigationView navigationView;
    Menu navigationMenu;

    public static TechnicianModel loggedInUser = null;

    APIStore apiStore;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(ServiceHelper.getInstance());
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
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
            navigationView.setCheckedItem(R.id.nav_home);
            setTitle("Home");
        }

        // to make the Navigation drawer icon always appear on the action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ServiceHelper.getInstance().initService(getApplicationContext());

        setLoggedIn(false);
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

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();

                homeFragment.setTechnician(loggedInUser);
                setLoggedIn(true);

                setTitle("Home");
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
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new GPSFragment()).commit();
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
            case R.id.nav_tampers:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TampersFragment()).commit();
                setTitle("Tampers");
                break;
            case R.id.nav_telemetry:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TelemetryFragment()).commit();
                setTitle("Telemetry");
                break;
            case R.id.nav_service_info:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ServiceInfoFragment()).commit();
                setTitle("Service Info");
                break;
            case R.id.nav_updates:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UpdatesFragment()).commit();
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
        homeFragment.setLoggingIn(true);
        try {
//            apiStore.Login("Nikitha", "1234", loginResponse);
            apiStore.Login("lukegeyser", "1964", loginResponse);
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
                    homeFragment.setLoggingIn(false);

                    Gson gson = new GsonBuilder().create();
                    try {
                        loggedInUser = gson.fromJson(bodyString, TechnicianModel.class);
                        homeFragment.setTechnician(loggedInUser);
                        setLoggedIn(true);
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
            homeFragment.setLoggingIn(false);
            Toast.makeText(getApplicationContext(), "error logging in", Toast.LENGTH_LONG).show();
        }
    };

    private void setLoggedIn(boolean isLoggedIn) {
        navigationMenu.findItem(R.id.nav_printer).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_nfc).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_external).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_gps).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_lights).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_camera).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_hardware_info).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_about).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_tampers).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_telemetry).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_service_info).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_updates).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_qc_check).setEnabled(isLoggedIn);
        navigationMenu.findItem(R.id.nav_feedback).setEnabled(isLoggedIn);
    }

    @Override
    public void onLoginDialogNegativeClick(DialogFragment dialog) {
        dialog.getDialog().cancel();
    }
}