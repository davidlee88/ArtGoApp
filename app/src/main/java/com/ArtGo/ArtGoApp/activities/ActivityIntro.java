package com.ArtGo.ArtGoApp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import com.ArtGo.ArtGoApp.R;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
/**
 * ActivityIntro is created to display introduction screen.
 * Created using paolortolo appintro.
 */
public class ActivityIntro extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Add slides
        addSlide(AppIntroFragment.newInstance("Welcome to ArtGo!", "The easiest way to discover public art", R.drawable.intro_slide1, Color.parseColor("#52BE80")));
        addSlide(AppIntroFragment.newInstance("Start ur journey", " Look for art,galleries or museums", R.drawable.intro_slide2, Color.parseColor("#5DADE2")));
        addSlide(AppIntroFragment.newInstance("Select one", " Get info and directions ", R.drawable.intro_slide3, Color.parseColor("#F4D03F")));
        addSlide(AppIntroFragment.newInstance("Also check festivals & Events ", "You are ready to go!", R.drawable.intro_slide4, Color.parseColor("#AF7AC5")));

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
