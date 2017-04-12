package com.project.musicianhub.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.project.musicianhub.MusicPostActivity;
import com.project.musicianhub.ProfileFragment;
import com.project.musicianhub.R;
import com.project.musicianhub.adapter.MusicAdapter;
import com.project.musicianhub.adapter.MusicPostAdapter;
import com.project.musicianhub.adapter.ProfileMusicAdapter;
import com.project.musicianhub.model.CustomResponse;
import com.project.musicianhub.model.Music;
import com.project.musicianhub.singleton.Singleton;
import com.project.musicianhub.util.CustomMultipartRequest;
import com.project.musicianhub.util.SessionManager;
import com.project.musicianhub.util.Utils;
import com.project.musicianhub.util.VolleyUtil;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Music Service Implementation class
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class MusicServiceImpl implements IMusicService {


    String imagePath;
    String musicPath;

    private static final String IMAGE_FILE_UPLOAD_URL = "http://192.168.0.13:8080/musicianhub/webapi/music/uploadUserImage";
    private static final String MUSIC_FILE_UPLOAD_URL = "http://192.168.0.13:8080/musicianhub/webapi/music/uploadUserMusic";
    private static final String ADD_MUSIC_URL = "http://192.168.0.13:8080/musicianhub/webapi/music/create";
    private static final String UPDATE_MUSIC_URL = "http://192.168.0.13:8080/musicianhub/webapi/music/update";
    private static final String DELETE_MUSIC_URL = "http://192.168.0.13:8080/musicianhub/webapi/music/delete/";
    private static final String MUSIC_URL = "http://192.168.0.13:8080/musicianhub/webapi/music/";
    private static final String NOTIFICATION_URL = "http://192.168.0.13:8080/musicianhub/webapi/users/";


    NotificationServiceImpl notificationService;


    /**
     * Uploads and saves the music for the users
     *
     * @param music          music objecy
     * @param imagePath      image path
     * @param musicPath      music path
     * @param imageView      image view for the album art
     * @param context        current context
     * @param progressDialog progress dialog
     */
    @Override
    public void uploadAndCreateMusic(Music music, String imagePath, String musicPath, CircleImageView imageView, Context context, ProgressDialog progressDialog) {
        try {
            if (imagePath != null && musicPath == null) {
                //if image needs to uploaded and image is not required
                String finalPath = Utils.decodeFile(imagePath);
                File f = new File(finalPath);
                //gets the image multipart entity
                MultipartEntity entity = this.getImageMultipartEntity(f, music);
                //upload image
                this.uploadImageFromVolley(music, context, entity, progressDialog);

            } else if (imagePath == null && musicPath != null) {
                //if music needs to be uploaded
                File f = new File(musicPath);
                //gets the music multipart entity
                MultipartEntity entity = this.getMusicMultipartEntity(f, music);
                //upload music
                this.uploadMusicFromVolley(music, context, entity, progressDialog);

            } else if (imagePath != null && musicPath != null) {
                //if both image and music needs to be uploaded
                String finalPath = Utils.decodeFile(imagePath);
                File f = new File(finalPath);
                //gets the image multipart entity, music multipart entity will be added inside the method inside this block
                MultipartEntity entity = this.getImageMultipartEntity(f, music);
                //uploads both image and music
                this.uploadImageAndMusicFromVolley(musicPath, music, context, entity, progressDialog);

            } else {
                //creates the music without music and image upload
                this.createMusic(music, "", "", context.getApplicationContext(), progressDialog);
            }

        } catch (NoSuchMethodError e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Uploads the image and music using the custom multipart request from the volley
     *
     * @param musicPath      music path
     * @param music          music object
     * @param context        current context
     * @param entity         file multipart entity
     * @param progressDialog progress dialog
     */
    private void uploadImageAndMusicFromVolley(final String musicPath, final Music music, final Context context, MultipartEntity entity, final ProgressDialog progressDialog) {
        final CustomResponse[] finalResponse = {new CustomResponse()};
        CustomMultipartRequest req = new CustomMultipartRequest(
                IMAGE_FILE_UPLOAD_URL,  //url where you want to send the multipart request
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers));

                            } catch (UnsupportedEncodingException e1) {
                                // Couldn't properly decode data to custom response
                                e1.printStackTrace();
                            }
                        }
                    }
                }, //instance of onErrorResponse Listener
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //sets the image path
                        imagePath = response;
                        //gets the music file
                        File musicFile = new File(musicPath);
                        MultipartEntity musicEntity = null;
                        try {
                            //gets the music multipart entity
                            musicEntity = getMusicMultipartEntity(musicFile, music);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        //uploads only music from custom volley request
                        uploadOnlyMusicFromVolley(music, context, musicEntity, progressDialog);

                    }

                },  //instance of onResponse Listener
                entity);
        VolleyUtil.setRetryPolicyForVolley(req);

        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * Uploads only music from the custom volley request
     *
     * @param music          music object
     * @param context        current context
     * @param entity         file multipart entity
     * @param progressDialog progress dialog
     */
    private void uploadOnlyMusicFromVolley(final Music music, final Context context, MultipartEntity entity, final ProgressDialog progressDialog) {
        final CustomResponse[] finalResponse = {new CustomResponse()};
        CustomMultipartRequest req = new CustomMultipartRequest(
                MUSIC_FILE_UPLOAD_URL,  //url where you want to send the multipart request
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers));

                            } catch (UnsupportedEncodingException e1) {
                                // Couldn't properly decode data to string
                                e1.printStackTrace();
                            }
                        }
                    }
                }, //instance of onErrorResponse Listener
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //creates the music passing the image and music path;
                        musicPath = response;
                        createMusic(music, imagePath, musicPath, context, progressDialog);
                    }
                },  //instance of onResponse Listener
                entity);
        VolleyUtil.setRetryPolicyForVolley(req);

        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * Uploads the image using custom volley request
     *
     * @param music          music object
     * @param context        current context
     * @param entity         file music entity
     * @param progressDialog progress dialog
     */
    private void uploadImageFromVolley(final Music music, final Context context, MultipartEntity entity, final ProgressDialog progressDialog) {
        final CustomResponse[] finalResponse = {new CustomResponse()};
        CustomMultipartRequest req = new CustomMultipartRequest(
                IMAGE_FILE_UPLOAD_URL,  //url where you want to send the multipart request
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers));

                            } catch (UnsupportedEncodingException e1) {
                                // Couldn't properly decode data to string
                                e1.printStackTrace();
                            }
                        }
                    }
                }, //instance of onErrorResponse Listener
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //creates the music post
                        imagePath = response;
                        createMusic(music, imagePath, "", context, progressDialog);
                    }


                },  //instance of onResponse Listener
                entity);
        VolleyUtil.setRetryPolicyForVolley(req);

        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * Uploads music from customs volley request
     *
     * @param music          music object
     * @param context        current context
     * @param entity         file multipart entity
     * @param progressDialog progress dialog
     */
    private void uploadMusicFromVolley(final Music music, final Context context, MultipartEntity entity, final ProgressDialog progressDialog) {
        final CustomResponse[] finalResponse = {new CustomResponse()};
        CustomMultipartRequest req = new CustomMultipartRequest(
                MUSIC_FILE_UPLOAD_URL,  //url where you want to send the multipart request
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers));

                            } catch (UnsupportedEncodingException e1) {
                                // Couldn't properly decode data to string
                                e1.printStackTrace();
                            }
                        }
                    }
                }, //instance of onErrorResponse Listener
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        musicPath = response;
                        createMusic(music, "", musicPath, context, progressDialog);
                    }
                },  //instance of onResponse Listener
                entity);
        VolleyUtil.setRetryPolicyForVolley(req);

        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * Sets and gets the image multipart entity for image
     *
     * @param f     file
     * @param music music object
     * @return file multipart entity
     * @throws UnsupportedEncodingException
     */
    private MultipartEntity getImageMultipartEntity(File f, Music music) throws UnsupportedEncodingException {
        MultipartEntity entity = new MultipartEntity();
        FileBody fileBody = new FileBody(f);
        entity.addPart("photo_file", fileBody);
        entity.addPart("file_name", new StringBody(fileBody.getFilename()));
        entity.addPart("from", new StringBody("addMusic"));
        entity.addPart("userId", new StringBody(String.valueOf(music.getUser().getId())));
        return entity;
    }

    /**
     * Sets and gets the music multipart entity for music
     *
     * @param f     file object
     * @param music music object
     * @return file multipart entity
     * @throws UnsupportedEncodingException
     */
    private MultipartEntity getMusicMultipartEntity(File f, Music music) throws UnsupportedEncodingException {
        MultipartEntity entity = new MultipartEntity();
        FileBody fileBody = new FileBody(f);
        entity.addPart("music_file", fileBody);
        entity.addPart("file_name", new StringBody(fileBody.getFilename()));
        entity.addPart("from", new StringBody("addMusic"));
        entity.addPart("userId", new StringBody(String.valueOf(music.getUser().getId())));
        return entity;
    }

    /**
     * Sets and gets the edit image multipart entity
     *
     * @param f     file object
     * @param music music object
     * @return multipart entity
     * @throws UnsupportedEncodingException
     */
    private MultipartEntity getEditImageMultipartEntity(File f, Music music) throws UnsupportedEncodingException {
        MultipartEntity entity = new MultipartEntity();
        FileBody fileBody = new FileBody(f);
        entity.addPart("photo_file", fileBody);
        entity.addPart("file_name", new StringBody(fileBody.getFilename()));
        entity.addPart("from", new StringBody("editMusic"));
        entity.addPart("userId", new StringBody(String.valueOf(music.getUserId())));
        entity.addPart("id", new StringBody(String.valueOf(music.getId())));
        return entity;
    }

    /**
     * Sets and gets the  edit music multipart entity
     *
     * @param f     file object
     * @param music music object
     * @return multipart entity
     * @throws UnsupportedEncodingException
     */
    private MultipartEntity getEditMusicMultipartEntity(File f, Music music) throws UnsupportedEncodingException {
        MultipartEntity entity = new MultipartEntity();
        FileBody fileBody = new FileBody(f);
        entity.addPart("music_file", fileBody);
        entity.addPart("file_name", new StringBody(fileBody.getFilename()));
        entity.addPart("from", new StringBody("editMusic"));
        entity.addPart("userId", new StringBody(String.valueOf(music.getUserId())));
        entity.addPart("id", new StringBody(String.valueOf(music.getId())));
        return entity;
    }


    /**
     * Creates the music
     *
     * @param music          music object
     * @param imagePath      image path
     * @param musicPath      music path
     * @param context        current context
     * @param progressDialog progress dialog
     */
    private void createMusic(final Music music, final String imagePath, final String musicPath, final Context context, final ProgressDialog progressDialog) {

        //sets the json object for the music
        JSONObject params = new JSONObject();
        try {
            params.put("title", music.getTitle());
            params.put("album_art_path", imagePath);
            params.put("genre", music.getGenre());
            params.put("published_date", new Date());
            params.put("music_path", musicPath);
            JSONObject userParam = new JSONObject();
            userParam.put("id", music.getUser().getId());
            params.put("user", userParam);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, ADD_MUSIC_URL, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String code = response.getString("code").toString();
                            String message = response.getString("message").toString();

                            if (code.equals("create_success")) {
                                progressDialog.dismiss();
                                Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()

                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        VolleyUtil.setRetryPolicyForVolley(postRequest);
        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(postRequest);
    }

    /**
     * Gets the music information for the users to edit them.
     *
     * @param id             user id
     * @param imageView      image view
     * @param editTextList   edit text list
     * @param strings        string list
     * @param context        current context
     * @param progressDialog progress dialog
     */
    @Override
    public void getEditMusicInto(int id, CircleImageView imageView, List<EditText> editTextList, List<String> strings, Context context, ProgressDialog progressDialog) {

        if (strings.get(2).equals("")) {
            Glide.with(context.getApplicationContext()).load(R.drawable.logo).placeholder(R.drawable.logo).override(128, 128).into(imageView);
        } else {
            Glide.with(context.getApplicationContext()).load(strings.get(2)).diskCacheStrategy(DiskCacheStrategy.NONE).override(128, 128).into(imageView);
        }
        //sets the edit text value
        editTextList.get(0).setText(String.valueOf(id));
        editTextList.get(1).setText(strings.get(0));
        editTextList.get(2).setText(strings.get(1));
        editTextList.get(3).setText(strings.get(3));

        progressDialog.dismiss();

    }

    /**
     * Uploads and edits the music for the users
     *
     * @param music          music object
     * @param imagePath      image path
     * @param musicPath      music path
     * @param imageView      image view
     * @param context        current context
     * @param progressDialog progress dialog
     */
    @Override
    public void uploadAndUpdateMusic(Music music, String imagePath, String musicPath, CircleImageView imageView, Context context, ProgressDialog progressDialog) {
        try {
            if (imagePath != null && musicPath == null) {
                //updates image path
                String finalPath = Utils.decodeFile(imagePath);
                File f = new File(finalPath);
                //gets the edit image multipart entity
                MultipartEntity entity = this.getEditImageMultipartEntity(f, music);
                //uploads the image
                this.uploadImageEditFromVolley(music, context, entity, progressDialog);

            } else if (imagePath == null && musicPath != null) {
                //updates the music
                File f = new File(musicPath);
                //gets the edit music multipart entity
                MultipartEntity entity = this.getEditMusicMultipartEntity(f, music);
                //uploads the music
                this.uploadMusicEditFromVolley(music, context, entity, progressDialog);

            } else if (imagePath != null && musicPath != null) {
                //updates music and image
                String finalPath = Utils.decodeFile(imagePath);
                File f = new File(finalPath);
                //gets the image multipart entity
                MultipartEntity entity = this.getEditImageMultipartEntity(f, music);
                //uploads image and music
                this.uploadImageAndMusicEditFromVolley(musicPath, music, context, entity, progressDialog);

            } else {
                //updates the music
                this.updateMusic(music, "", "", context.getApplicationContext(), progressDialog);
            }

        } catch (NoSuchMethodError e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Upload image and music for the edit
     *
     * @param musicPath      music path
     * @param music          music object
     * @param context        current context
     * @param entity         multipart entity
     * @param progressDialog progress dialog
     */
    private void uploadImageAndMusicEditFromVolley(final String musicPath, final Music music, final Context context, MultipartEntity entity, final ProgressDialog progressDialog) {
        final CustomResponse[] finalResponse = {new CustomResponse()};
        CustomMultipartRequest req = new CustomMultipartRequest(
                IMAGE_FILE_UPLOAD_URL,  //url where you want to send the multipart request
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers));

                            } catch (UnsupportedEncodingException e1) {
                                // Couldn't properly decode data to string
                                e1.printStackTrace();
                            }
                        }
                    }
                }, //instance of onErrorResponse Listener
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        imagePath = response;
                        File musicFile = new File(musicPath);
                        MultipartEntity musicEntity = null;
                        try {
                            musicEntity = getEditMusicMultipartEntity(musicFile, music);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        uploadOnlyMusicEditFromVolley(music, context, musicEntity, progressDialog);

                    }

                },  //instance of onResponse Listener
                entity);
        VolleyUtil.setRetryPolicyForVolley(req);

        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * upload only music for edit music
     *
     * @param music          music object
     * @param context        current context
     * @param entity         multipart entity
     * @param progressDialog progress dialog
     */
    private void uploadOnlyMusicEditFromVolley(final Music music, final Context context, MultipartEntity entity, final ProgressDialog progressDialog) {
        final CustomResponse[] finalResponse = {new CustomResponse()};
        CustomMultipartRequest req = new CustomMultipartRequest(
                MUSIC_FILE_UPLOAD_URL,  //url where you want to send the multipart request
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers));

                            } catch (UnsupportedEncodingException e1) {
                                // Couldn't properly decode data to string
                                e1.printStackTrace();
                            }
                        }
                    }
                }, //instance of onErrorResponse Listener
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        musicPath = response;
                        updateMusic(music, imagePath, musicPath, context, progressDialog);
                    }
                },  //instance of onResponse Listener
                entity);
        VolleyUtil.setRetryPolicyForVolley(req);

        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * uploads image from the edit view
     *
     * @param music          music object
     * @param context        current context
     * @param entity         multipart entity
     * @param progressDialog progress dialog
     */
    private void uploadImageEditFromVolley(final Music music, final Context context, MultipartEntity entity, final ProgressDialog progressDialog) {
        final CustomResponse[] finalResponse = {new CustomResponse()};
        CustomMultipartRequest req = new CustomMultipartRequest(
                IMAGE_FILE_UPLOAD_URL,  //url where you want to send the multipart request
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers));

                            } catch (UnsupportedEncodingException e1) {
                                // Couldn't properly decode data to string
                                e1.printStackTrace();
                            }
                        }
                    }
                }, //instance of onErrorResponse Listener
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        imagePath = response;
                        updateMusic(music, imagePath, "", context, progressDialog);
                    }


                },  //instance of onResponse Listener
                entity);
        VolleyUtil.setRetryPolicyForVolley(req);

        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * uploads music using custom volley request
     *
     * @param music          music object
     * @param context        current context
     * @param entity         multipart entity
     * @param progressDialog progress dialog
     */
    private void uploadMusicEditFromVolley(final Music music, final Context context, MultipartEntity entity, final ProgressDialog progressDialog) {
        final CustomResponse[] finalResponse = {new CustomResponse()};
        CustomMultipartRequest req = new CustomMultipartRequest(
                MUSIC_FILE_UPLOAD_URL,  //url where you want to send the multipart request
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers));

                            } catch (UnsupportedEncodingException e1) {
                                // Couldn't properly decode data to string
                                e1.printStackTrace();
                            }
                        }
                    }
                }, //instance of onErrorResponse Listener
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        musicPath = response;
                        updateMusic(music, "", musicPath, context, progressDialog);
                    }
                },  //instance of onResponse Listener
                entity);
        VolleyUtil.setRetryPolicyForVolley(req);

        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * Updates the music
     *
     * @param music          music object
     * @param imagePath      image path
     * @param musicPath      music path
     * @param context        current context
     * @param progressDialog progress dialog
     * @author Akash Rajbanshi
     * @since 1.0
     */
    private void updateMusic(final Music music, final String imagePath, final String musicPath, final Context context, final ProgressDialog progressDialog) {
        JSONObject params = new JSONObject();
        try {
            params.put("id", music.getId());
            params.put("title", music.getTitle());
            params.put("album_art_path", imagePath);
            params.put("genre", music.getGenre());
            params.put("music_path", musicPath);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.PUT, UPDATE_MUSIC_URL, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String code = response.getString("code").toString();
                            String message = response.getString("message").toString();

                            if (code.equals("update_success")) {
                                Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            }

                            progressDialog.dismiss();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()

                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        VolleyUtil.setRetryPolicyForVolley(postRequest);
        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(postRequest);
    }

    /**
     * Deletes the music from the user's profile
     *
     * @param id                  music id
     * @param context             current context
     * @param fragmentManager     fragment manager
     * @param profileMusicAdapter profile music adapter
     */
    @Override
    public void deleteMusic(int id, final Context context, final FragmentManager fragmentManager, final ProfileMusicAdapter profileMusicAdapter) {
        final int musicId = id;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete the post?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final StringRequest stringRequest = new StringRequest(Request.Method.PUT, DELETE_MUSIC_URL + musicId, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String code = jsonObject.getString("code");
                            String message = jsonObject.getString("message");
                            if (code.equals("delete_success")) {
                                if (fragmentManager != null) {
                                    Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                    //refresh the fragments after deletion
                                    ProfileFragment profileFragment = new ProfileFragment();
                                    FragmentManager fm = fragmentManager;
                                    fm.beginTransaction().detach(profileFragment).attach(profileFragment).commit();
                                } else {
                                    profileMusicAdapter.notifyDataSetChanged();
                                }
                            } else {
                                Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/json; charset=utf-8");
                        return headers;
                    }
                };
                VolleyUtil.setRetryPolicyForVolley(stringRequest);
                Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(stringRequest);
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    /**
     * Creates the like for the music posts in the profile view of the user
     *
     * @param music   music object
     * @param holder  profile music adapter holder
     * @param context current context
     */
    @Override
    public void createMusicLike(final Music music, final ProfileMusicAdapter.MyViewHolder holder, final Context context) {

        String url = MUSIC_URL + music.getId() + "/likes/createLike";


        JSONObject params = new JSONObject();
        try {
            params.put("music_id", music.getId());
            params.put("liked", music.isLiked());
            JSONObject userParam = new JSONObject();
            userParam.put("id", music.getUserId());
            params.put("user", userParam);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject jsonObject = response;
                            JSONArray jsonArray = jsonObject.getJSONArray("likes");
                            //sets the like image view after like has been created
                            holder.like.setText(jsonArray.length() + " person like(s) your music.");
                            notificationService = new NotificationServiceImpl();
                            //sends the notification if the music is from other user and if it is liked
                            if (music.getUserId() != music.getUser().getId() && music.isLiked())
                                notificationService.sendNotificationToUser(music.getUserId(), 0, music.getId(), NOTIFICATION_URL, "like", context);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                },
                new Response.ErrorListener()

                {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));

                    JSONObject result = null;

                    if (jsonString != null && jsonString.length() > 0)
                        result = new JSONObject(jsonString);

                    return Response.success(result,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };
        VolleyUtil.setRetryPolicyForVolley(postRequest);
        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(postRequest);
    }

    /**
     * Creates the music like for the posts in the single music post view
     *
     * @param music music object
     * @param holder music post adapter holder
     * @param context current context
     */
    @Override
    public void createMusicLike(final Music music, final MusicPostAdapter.MyViewHolder holder, final Context context) {

        String url = MUSIC_URL + music.getId() + "/likes/createLike";

        JSONObject params = new JSONObject();
        try {
            params.put("music_id", music.getId());
            params.put("liked", music.isLiked());
            JSONObject userParam = new JSONObject();
            userParam.put("id", music.getUserId());
            params.put("user", userParam);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject jsonObject = response;
                            JSONArray jsonArray = jsonObject.getJSONArray("likes");
                            //sets the like image view after like has been created
                            holder.like.setText(jsonArray.length() + " person like(s) your music.");
                            notificationService = new NotificationServiceImpl();
                            //sends the notification if the music is from other user and if it is liked
                            if (music.getUserId() != music.getUser().getId() && music.isLiked())
                                notificationService.sendNotificationToUser(music.getUserId(), 0, music.getId(), NOTIFICATION_URL, "like", context);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()

                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));

                    JSONObject result = null;

                    if (jsonString != null && jsonString.length() > 0)
                        result = new JSONObject(jsonString);

                    return Response.success(result,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };
        VolleyUtil.setRetryPolicyForVolley(postRequest);
        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(postRequest);
    }

    /**
     * Create music like for the music adpater
     * @param music music object
     * @param holder music adapter's holder
     * @param context current context
     */
    public void createMusicLike(final Music music, final MusicAdapter.MyViewHolder holder, final Context context) {

        String url = MUSIC_URL + music.getId() + "/likes/createLike";

        JSONObject params = new JSONObject();
        try {
            params.put("music_id", music.getId());
            params.put("liked", music.isLiked());
            JSONObject userParam = new JSONObject();
            userParam.put("id", music.getUserId());
            params.put("user", userParam);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject jsonObject = response;
                            JSONArray jsonArray = jsonObject.getJSONArray("likes");
                            //sets the like image view after like has been created
                            holder.like.setText(jsonArray.length() + " person like(s) your music.");
                            notificationService = new NotificationServiceImpl();
                            //sends the notification if the music is from other user and if it is liked
                            if (music.getUserId() != music.getUser().getId() && music.isLiked())
                                notificationService.sendNotificationToUser(music.getUserId(), 0, music.getId(), NOTIFICATION_URL, "like", context);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()

                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));

                    JSONObject result = null;

                    if (jsonString != null && jsonString.length() > 0)
                        result = new JSONObject(jsonString);

                    return Response.success(result,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };
        VolleyUtil.setRetryPolicyForVolley(postRequest);
        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(postRequest);
    }

    /**
     * loads the music information for the single post music view
     *
     * @param id music id
     * @param musicPostActivity music post activity object
     * @param musicList music list
     * @param musicPostAdapter music post adapter
     */
    @Override
    public void loadMusicInfo(int id, final MusicPostActivity musicPostActivity, final List<Music> musicList, final MusicPostAdapter musicPostAdapter) {
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, MUSIC_URL + String.valueOf(id), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //gets the music object
                    JSONObject jsonObject = new JSONObject(response);
                    int id = jsonObject.getInt("id");
                    String title = jsonObject.getString("title");
                    String genre = jsonObject.getString("genre");

                    //gets the user object from the music object
                    JSONObject jObject = jsonObject.getJSONObject("user");
                    String name = jObject.getString("name");
                    String userImagePath = jObject.getString("user_img_path");
                    String albumArt = jsonObject.getString("album_art_path");
                    String musicPath = jsonObject.getString("music_path");
                    String date = jsonObject.getString("published_date");

                    String[] splitDate = date.split("T");

                    //gets the comment list array
                    JSONArray comments = jsonObject.getJSONArray("comments");
                    //gets the like list array
                    JSONArray likes = jsonObject.getJSONArray("likes");

                    int userid = jObject.getInt("id");

                    //gets the session data
                    SessionManager session = new SessionManager(musicPostActivity.getApplicationContext());
                    final HashMap<String, Integer> sessionUserId = session.getUserDetails();
                    final int sesUserId = sessionUserId.get(SessionManager.KEY_ID);


                    Music music = new Music(id, title, genre, name, albumArt, musicPath, 0, splitDate[0], likes.length(), comments.length(), sesUserId);
                    music.getUser().setUser_img_path(userImagePath);
                    music.getUser().setId(userid);
                    for (int j = 0; j < likes.length(); j++) {
                        JSONObject likeObject = likes.getJSONObject(j);
                        JSONObject userObject = likeObject.getJSONObject("user");
                        JSONObject musicObject = likeObject.getJSONObject("music");

                        int likeUserId = userObject.getInt("id");
                        int musicId = musicObject.getInt("id");

                        if (likeUserId == sesUserId && musicId == id) {
                            //gets the music like boolean value if the liked user id and
                            // session user and music id and current music id is same
                            music.setLiked(likeObject.getBoolean("liked"));
                        }
                    }
                    musicList.add(music);
                    musicPostAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        VolleyUtil.setRetryPolicyForVolley(stringRequest);
        Singleton.getmInstance(musicPostActivity.getApplicationContext()).addToRequestQueue(stringRequest);
    }

    /**
     * Load music for the home fragments
     * @param id user id
     * @param musicList music list
     * @param musicAdapter music adapter
     * @param context current context
     * @param emptyHome empty home text view
     * @param recyclerView recycler view object
     */
    public void loadMusicInfoForHome(int id, final List<Music> musicList, final MusicAdapter musicAdapter, final Context context, final TextView emptyHome, final RecyclerView recyclerView) {

        JSONObject finalParams = new JSONObject();
        try {
            JSONArray musics = new JSONArray();
            for (Music music : musicList) {
                if (music != null) {
                    JSONObject params = new JSONObject();
                    params.put("id", music.getId());
                    JSONObject userParam = new JSONObject();
                    userParam.put("id", music.getUser().getId());
                    params.put("user", userParam);
                    musics.put(params);
                }
            }
            finalParams.put("musics", musics);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, MUSIC_URL + "followingMusic/" + String.valueOf(id) + "/" + musicList.size(), finalParams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = new JSONObject(response.toString());
                    JSONArray jsonArray = obj.getJSONArray("musics");
                    int length = jsonArray.length();
                    if (length != 0) {
                        if (musicList != null) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                //gets the music object
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int id = jsonObject.getInt("id");
                                String title = jsonObject.getString("title");
                                String genre = jsonObject.getString("genre");

                                //gets the user object from the music object
                                JSONObject jObject = jsonObject.getJSONObject("user");
                                String name = jObject.getString("name");
                                String userImagePath = jObject.getString("user_img_path");
                                String albumArt = jsonObject.getString("album_art_path");
                                String musicPath = jsonObject.getString("music_path");
                                String date = jsonObject.getString("published_date");

                                String[] splitDate = date.split("T");

                                //gets the comment list array
                                JSONArray comments = jsonObject.getJSONArray("comments");
                                //gets the like list array
                                JSONArray likes = jsonObject.getJSONArray("likes");

                                int userid = jObject.getInt("id");
                                String serverPath = jObject.getString("commentUserImagePath");

                                //gets the session data
                                SessionManager session = new SessionManager(context);
                                final HashMap<String, Integer> sessionUserId = session.getUserDetails();
                                final int sesUserId = sessionUserId.get(SessionManager.KEY_ID);


                                Music music = new Music(id, title, genre, name, albumArt, musicPath, 0, splitDate[0], likes.length(), comments.length(), sesUserId);
                                music.getUser().setUser_img_path(serverPath.concat(File.separator).concat(userImagePath));
                                music.getUser().setId(userid);
                                for (int j = 0; j < likes.length(); j++) {
                                    JSONObject likeObject = likes.getJSONObject(j);
                                    JSONObject userObject = likeObject.getJSONObject("user");
                                    JSONObject musicObject = likeObject.getJSONObject("music");

                                    int likeUserId = userObject.getInt("id");
                                    int musicId = musicObject.getInt("id");

                                    if (likeUserId == sesUserId && musicId == id) {
                                        //gets the music like boolean value if the liked user id and
                                        // session user and music id and current music id is same
                                        music.setLiked(likeObject.getBoolean("liked"));
                                    }
                                }

                                musicList.add(music);
                                musicAdapter.notifyDataSetChanged();
                                musicAdapter.setMoreLoading(false);
                            }

                        }
                    } else {
                        if (musicList != null && musicList.size() == 0)
                            musicList.clear();
                        musicAdapter.notifyDataSetChanged();
                    }

                    if (musicList.size() > 0) {
                        emptyHome.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        emptyHome.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));

                    JSONObject result = null;

                    if (jsonString != null && jsonString.length() > 0)
                        result = new JSONObject(jsonString);

                    return Response.success(result,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };
        VolleyUtil.setRetryPolicyForVolley(stringRequest);
        Singleton.getmInstance(context).addToRequestQueue(stringRequest);
    }


}
