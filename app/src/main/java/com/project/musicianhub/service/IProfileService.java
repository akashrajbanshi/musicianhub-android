package com.project.musicianhub.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.project.musicianhub.adapter.ProfileMusicAdapter;
import com.project.musicianhub.model.Music;
import com.project.musicianhub.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Profile Service interface
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public interface IProfileService {

    /**
     * Gets the user information
     *
     * @param id
     * @param imageView
     * @param imagePath
     * @param context
     * @param musicList
     * @param musicAdapter
     * @param textViewList
     * @param progressBar
     * @param from
     * @param emptyProfile
     *@param recyclerView @author Akash Rajbanshi
     * @since 1.0
     */
    public void getUserInfo(int id, CircleImageView imageView, String imagePath, Context context, List<Music> musicList, ProfileMusicAdapter musicAdapter, List<TextView> textViewList, ProgressBar progressBar, String from, TextView emptyProfile, RecyclerView recyclerView);

    /**
     * gets the edit profile information
     *
     * @param id
     * @param imageView
     * @param editTextList
     * @param genderSpinner
     * @param context
     * @param imagePath
     * @param progressDialog
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void getEditProfileInfo(int id, CircleImageView imageView, List<EditText> editTextList, Spinner genderSpinner, Context context, String imagePath, ProgressDialog progressDialog);

    /**
     * Uploads the user images and updates the profile
     *
     * @param user
     * @param imagePath
     * @param imageView
     * @param applicationContext
     * @param progressDialog
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void uploadImageAndUpdateProfile(User user, String imagePath, CircleImageView imageView, Context applicationContext, ProgressDialog progressDialog);


}
