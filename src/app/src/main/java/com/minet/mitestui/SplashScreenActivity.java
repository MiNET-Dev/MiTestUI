package com.minet.mitestui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EasySplashScreen config = new EasySplashScreen(SplashScreenActivity.this)
                .withFullScreen()
                .withTargetActivity(MainActivity.class)
                .withSplashTimeOut(5000)
                .withBackgroundColor(Color.parseColor("#FFFFFF"))
                .withHeaderText("MiDEVICE 7' Bus Touch")
                .withFooterText("Tel: 011 100 1500")
                .withBeforeLogoText("MiDEVICE Testing application - This is not for commercial use, for Testing purposes only")
                .withAfterLogoText("If you have any enquiries, or any feedback, please call us at the telephone number below")
                .withLogo(R.drawable.logo_white);

        config.getHeaderTextView().setTextColor(Color.BLACK);
        config.getHeaderTextView().setTextSize(25);
        config.getHeaderTextView().setPadding(50,50,50,0);
        config.getHeaderTextView().setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        config.getFooterTextView().setTextColor(Color.BLACK);
        config.getFooterTextView().setPadding(50,0,50,50);
        config.getFooterTextView().setTextSize(25);
        config.getFooterTextView().setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        config.getBeforeLogoTextView().setTextColor(Color.BLACK);
        config.getBeforeLogoTextView().setPadding(50,0,50,50);
        config.getBeforeLogoTextView().setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        config.getAfterLogoTextView().setTextColor(Color.BLACK);
        config.getAfterLogoTextView().setPadding(50,50,50,0);
        config.getAfterLogoTextView().setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        View easySplashScreen = config.create();
        setContentView(easySplashScreen);
    }
}
