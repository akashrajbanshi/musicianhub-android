package com.project.musicianhub;

import android.app.Application;

import com.bumptech.glide.request.target.ViewTarget;

/**
 * App class for ignoring the tag attributes for the image view
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ViewTarget.setTagId(R.id.player_status);
    }
}