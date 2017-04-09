package com.project.musicianhub.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.project.musicianhub.LoginActivity;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Manages the session information
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class SessionManager {

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Context context;
    int PRIVATE_MODE = 0;
    private static final String PREFER_NAME = "MusicianSession";
    private static final String IS_USER_LOGIN = "IsUserLoggedIn";
    public static final String KEY_ID = "id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_USER_IMAGE = "image";
    public static final String KEY_ARTIST_NAME = "artist";
    public static final String KEY_TITLE = "title";
    public static final String KEY_URL = "url";
    public static final String KEY_SONG_URL = "songUrl";


    public SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = preferences.edit();
    }

    /**
     * Creates the session for the user login
     *
     * @param id       user id
     * @param username user name
     * @param path     user image path
     */
    public void createUserLoginSession(int id, String username, String path) {
        editor.putBoolean(IS_USER_LOGIN, true);
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USER_IMAGE, path);
        editor.commit();
    }

    /**
     * Saves the current playing music information to session
     *
     * @param title   music title
     * @param name    music name
     * @param path    music album art path
     * @param songUrl music path
     */
    public void saveMusicInformation(String title, String name, String path, String songUrl) {
        editor.putString(KEY_TITLE, title);
        editor.putString(KEY_ARTIST_NAME, name);
        editor.putString(KEY_URL, path);
        editor.putString(KEY_SONG_URL, songUrl);
        editor.commit();
    }

    /**
     * Gets the artist name
     *
     * @return hashmap of artist name
     */
    public HashMap<String, String> getArtistName() {

        HashMap<String, String> artistName = new HashMap<>();
        artistName.put(KEY_ARTIST_NAME, preferences.getString(KEY_ARTIST_NAME, ""));
        return artistName;
    }

    /**
     * Gets the music title
     *
     * @return hashmap of music title
     */
    public HashMap<String, String> getTitle() {

        HashMap<String, String> title = new HashMap<>();
        title.put(KEY_TITLE, preferences.getString(KEY_TITLE, ""));
        return title;
    }

    /**
     * Gets the album art path
     *
     * @return hashmap of album art path
     */
    public HashMap<String, String> getAlbumArt() {

        HashMap<String, String> albumArt = new HashMap<>();
        albumArt.put(KEY_URL, preferences.getString(KEY_URL, ""));
        return albumArt;
    }

    /**
     * Gets the music url path
     *
     * @return hashmap of song user
     */
    public HashMap<String, String> getSongUrl() {

        HashMap<String, String> songURL = new HashMap<>();
        songURL.put(KEY_SONG_URL, preferences.getString(KEY_SONG_URL, ""));
        return songURL;
    }


    /**
     * Checks whether the user is logged in
     *
     * @return true or false on whether user is logged in
     */
    public boolean checkLogin() {
        if (!this.isLoggedIn()) {
            Intent i = new Intent(context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            return true;
        }
        return false;
    }


    /**
     * Gets the user details
     *
     * @return hashmap of user details
     */
    public HashMap<String, Integer> getUserDetails() {

        HashMap<String, Integer> user = new HashMap<>();
        user.put(KEY_ID, preferences.getInt(KEY_ID, 0));

        return user;
    }

    /**
     * Gets the user image path details
     *
     * @return hashmap of user image path details
     */
    public HashMap<String, String> getUserPathDetails() {

        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_USER_IMAGE, preferences.getString(KEY_USER_IMAGE, ""));

        return user;
    }

    /**
     * Gets the user name details
     *
     * @return hashmap of user name details
     */
    public HashMap<String, String> getUsernameDetails() {

        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_USERNAME, preferences.getString(KEY_USERNAME, ""));

        return user;
    }

    /**
     * Logs user out of the system.
     */
    public void logoutUser() {
        editor.clear();
        editor.commit();
        Intent i = new Intent(context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    /**
     * Checks if the user is logged in or not
     *
     * @return true or false if the user is logged in
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(IS_USER_LOGIN, false);
    }

}
