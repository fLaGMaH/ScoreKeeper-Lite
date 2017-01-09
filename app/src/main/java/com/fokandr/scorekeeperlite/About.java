package com.fokandr.scorekeeperlite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;


public class About  extends AppCompatActivity {
    RatingBar ratingBar;
    SharedPreferences settingOption;
    public final static String pef_KEY_RATING = "RatingValue";
    InterstitialAd interstitial;//Рекламка
    ImageView playCommercial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        settingOption = getSharedPreferences("Settings", MODE_PRIVATE);
        ratingBar.setRating(Float.valueOf(settingOption.getFloat(pef_KEY_RATING, 5)));

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                SharedPreferences.Editor prefEditor = settingOption.edit();
                prefEditor.putFloat(pef_KEY_RATING, v);
                prefEditor.apply();
                openAppInGooglePlay(getApplicationContext());
            }
        });

        playCommercial=(ImageView) findViewById(R.id.playCommercial);
        playCommercial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (interstitial.isLoaded()) {interstitial.show();}
                 else Toast.makeText(view.getContext(),R.string.errCommercialWatch,Toast.LENGTH_LONG).show();
            }
        });


    }

    public void openAppInGooglePlay(Context context) {
        final String appPackageName = context.getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException e) { // if there is no Google Play on device
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    @Override
    public void onPause() {
       super.onPause();
    }

    @Override
    public void onResume() {

        /******МЕЖСТРАНИЧНЫЙ БАННЕР*****/
        // Создание межстраничного объявления.
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(getString(R.string.banner_ad_unit_id));
        // Создание запроса объявления.
        //AdRequest adRequest = new AdRequest.Builder().build();
        /****ТЕСТИРОВАНИЕ*****/
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) //Все ЭМУЛЯТОРЫ будут загружать тест рекламы
                .addTestDevice("B34C9624DF77ACF16A553DE7C79480A4")  //ID аппарата для тестирования
                .build();
        // Запуск загрузки межстраничного объявления.
        interstitial.loadAd(adRequest);
        /***************************/

        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
