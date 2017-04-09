package com.project.musicianhub;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.project.musicianhub.util.SessionManager;

/**
 * Splash Screen for the application
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class SplashActivity extends AppCompatActivity {

    //splash screen image
    ImageView imageView;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        session = new SessionManager(getApplicationContext());

        //if current session is active
        if (session != null) {
            //and is logged in
            if (session.isLoggedIn()) {
                //skip all the activity and get to the main activity instead
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        }

        //initializes the UI components
        imageView = (ImageView) findViewById(R.id.logo);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splash_animation);
        imageView.setAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                finish();
                //session is active
                if (session != null) {
                    //not logged in
                    if (!session.isLoggedIn()) {
                        //go to the login activity
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    }
                    //if logged in go to main activity
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }

                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }
}
