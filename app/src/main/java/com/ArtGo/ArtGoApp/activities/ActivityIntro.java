package com.ArtGo.ArtGoApp.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ArtGo.ArtGoApp.R;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
/**
 * Moaoran Li
 * ActivityIntro is created to display introduction screen.
 * Created using paolortolo appintro.
 */
public class ActivityIntro extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Add slides
        addSlide(AppIntroFragment.newInstance("First slide", "Description1", R.drawable.intro_slide1, Color.parseColor("#52BE80")));
        addSlide(AppIntroFragment.newInstance("Second slide", "Description2", R.drawable.intro_slide2, Color.parseColor("#5DADE2")));
        addSlide(AppIntroFragment.newInstance("Third slide", "Description3", R.drawable.intro_slide3, Color.parseColor("#F4D03F")));
        addSlide(AppIntroFragment.newInstance("Fourth slide", "Description4", R.drawable.intro_slide4, Color.parseColor("#AF7AC5")));

        //Setting up
        setBarColor(Color.parseColor("#1e4872"));
        setSeparatorColor(Color.parseColor("#2196F3"));
        showSkipButton(true);
        setFadeAnimation();
        setVibrate(true);
        setVibrateIntensity(30);
    }
    private void loadMainActivity(){
        Intent intent = new Intent(this, ActivityHome.class);
        startActivity(intent);
    }
    @Override
    public void onSkipPressed() {
        loadMainActivity();
    }

    @Override
    public void onDonePressed() {
        loadMainActivity();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when slide is changed
    }
}
