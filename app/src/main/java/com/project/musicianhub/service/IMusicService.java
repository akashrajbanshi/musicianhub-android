package com.project.musicianhub.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.widget.EditText;

import com.project.musicianhub.MusicPostActivity;
import com.project.musicianhub.adapter.MusicPostAdapter;
import com.project.musicianhub.adapter.ProfileMusicAdapter;
import com.project.musicianhub.model.Music;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Music Service Interface class
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public interface IMusicService {

    /**
     * Uploads and saves the music for the users
     *
     * @param music
     * @param imagePath
     * @param musicPath
     * @param imageView
     * @param context
     * @param progressDialog
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void uploadAndCreateMusic(Music music, String imagePath, String musicPath, CircleImageView imageView, Context context, ProgressDialog progressDialog);

    /**
     * Deletes the music from the user's profile
     *
     * @param id
     * @param context
     * @param fragmentManager
     * @param profileMusicAdapter
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void deleteMusic(int id, Context context, final FragmentManager fragmentManager, ProfileMusicAdapter profileMusicAdapter);

    /**
     * Uploads and edits the music for the users
     *
     * @param music
     * @param imagePath
     * @param musicPath
     * @param imageView
     * @param context
     * @param progressDialog
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void uploadAndUpdateMusic(Music music, String imagePath, String musicPath, CircleImageView imageView, Context context, ProgressDialog progressDialog);

    /**
     * Gets the music information for the users to edit them.
     *
     * @param id
     * @param imageView
     * @param editTextList
     * @param strings
     * @param context
     * @param progressDialog
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void getEditMusicInto(int id, CircleImageView imageView, List<EditText> editTextList, List<String> strings, Context context, ProgressDialog progressDialog);

    /**
     * Creates the like for the music posts in the profile view of the user
     *
     * @param music
     * @param holder
     * @param context
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void createMusicLike(Music music, ProfileMusicAdapter.MyViewHolder holder, Context context);

    /**
     * Creates the music like for the posts in the single music post view
     *
     * @param music
     * @param holder
     * @param context
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void createMusicLike(Music music, MusicPostAdapter.MyViewHolder holder, Context context);

    /**
     * loads the music information for the single post music view
     *
     * @param id
     * @param musicPostActivity
     * @param musicList
     * @param musicPostAdapter
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void loadMusicInfo(int id, MusicPostActivity musicPostActivity, List<Music> musicList, MusicPostAdapter musicPostAdapter);

}
