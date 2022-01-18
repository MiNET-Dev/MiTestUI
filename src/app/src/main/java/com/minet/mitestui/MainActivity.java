package com.minet.mitestui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import za.co.megaware.MinetService.IMainService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // UI ELEMENTS
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    // BROADCAST RECEIVERS
    ExternalFragment.ExternalReaderReceiver externalReaderReceiver;

    // TAGS
    private static final String MAIN_TAG = "MainActivity";

    // PATHS
    private final String _DOWNLOAD_URL = "http://www.minet.co.za/boot/b.bmp";
    private final String _DESTINATION_PATH = "/storage/emulated/0/Download/b.bmp";
    private final String _DEVICE_NUMBER_PATH = "/storage/emulated/0/MiDEVICE/format.mi";

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService((ServiceConnection) ServiceHelper.getInstance());
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

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if (savedInstanceState == null){
            // SETTING UP BROADCAST RECEIVERS
            ExternalFragment externalFragment = new ExternalFragment();
            externalReaderReceiver = externalFragment.new ExternalReaderReceiver();
            IntentFilter externalFilter = new IntentFilter();
            externalFilter.addAction("UPLOADING_BMP");
            externalFilter.addAction("UPLOADING_WAV");
            externalFilter.addAction("BMP_INDEX");

            this.registerReceiver(externalReaderReceiver, externalFilter, null, null);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new QCChecklistFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_qc_check);
            setTitle("Home");
        }

        // to make the Navigation drawer icon always appear on the action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ServiceHelper.getInstance().initService(getApplicationContext());
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                setTitle("Home");
                break;
            case R.id.nav_printer:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PrinterFragment()).commit();
                setTitle("Printer");
                break;
            case R.id.nav_nfc:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NFCFragment()).commit();
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

    public void checkPermissions(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_DENIED){
            requestPermissions();
        }
    }

    public void requestPermissions(){
        ActivityCompat.requestPermissions(MainActivity.this, new String [] {
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

}