package com.project.musicianhub.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.project.musicianhub.R;
import com.project.musicianhub.adapter.ProfileMusicAdapter;
import com.project.musicianhub.model.CustomResponse;
import com.project.musicianhub.model.Music;
import com.project.musicianhub.model.User;
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
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Jerry on 2/9/2017.
 */

public class ProfileServiceImpl implements IProfileService {

    private static final String MUSIC_URL = "http://192.168.0.13:8080/musicianhub/webapi/music/usermusic/";
    private static final String USER_INFO_URL = "http://192.168.0.13:8080/musicianhub/webapi/users/";
    private static final String UPDATE_PROFILE_URL = "http://192.168.0.13:8080/musicianhub/webapi/users/update";
    private static final String FILE_UPLOAD_URL = "http://192.168.0.13:8080/musicianhub/webapi/users/upload";

    List<Music> musicList;
    ProfileMusicAdapter profileMusicAdapter;
    SessionManager session;
    CircleImageView circleImageView;
    TextView toolbarText;

    /**
     * Gets the user information
     *
     * @param id           user if
     * @param imageView    image view
     * @param imagePath    image path
     * @param context      current context
     * @param musicList    music list
     * @param musicAdapter music adapter
     * @param textViewList text view list
     * @param emptyProfile empty profile text view
     * @param recyclerView recyclerview object
     */
    @Override
    public void getUserInfo(int id, CircleImageView imageView, String imagePath, Context context, List<Music> musicList, ProfileMusicAdapter musicAdapter, List<TextView> textViewList, ProgressBar progressBar, String from, TextView emptyProfile, RecyclerView recyclerView) {
        String followersUrl = "http://192.168.0.13:8080/musicianhub/webapi/users/" + id + "/follow/followers";
        String followingUrl = "http://192.168.0.13:8080/musicianhub/webapi/users/" + id + "/follow/following";

        getFollowAndPostNumber(followersUrl, context, textViewList.get(1), progressBar, from, emptyProfile, recyclerView);
        getFollowAndPostNumber(followingUrl, context, textViewList.get(2), progressBar, from, emptyProfile, recyclerView);
        this.profileMusicAdapter = musicAdapter;
        this.musicList = musicList;
        this.circleImageView = imageView;


        //gets the toolbar text view for the profile view of a user
        if (textViewList.size() > 3)
            this.toolbarText = textViewList.get(3);

        getFollowAndPostNumber(MUSIC_URL + id + "/" + 0, context, textViewList.get(0), progressBar, from, emptyProfile, recyclerView);


        if (imagePath.equalsIgnoreCase("")) {
            Glide.with(context.getApplicationContext()).load(R.drawable.user).placeholder(R.drawable.logo).override(80, 80).fitCenter().into(imageView);
        } else {
            String replaceSlash = imagePath.replace("\\", "/");
            imagePath = "http:" + "//" + replaceSlash;
            Glide.with(context.getApplicationContext()).load(imagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).override(80, 80).into(imageView);
        }
    }

    public void getFollowAndPostNumber(final String url, final Context context, final TextView textView, final ProgressBar progressBar, final String from, final TextView emptyProfile, final RecyclerView recyclerView) {
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    int length = jsonArray.length();
                    if (length != 0) {
                        //sets the number of posts
                        textView.setText(String.valueOf(length));
                        if (musicList != null) {
                            if (!url.contains("follow")) {
                                musicList.clear();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    //gets the music object
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    int id = jsonObject.getInt("id");
                                    String title = jsonObject.getString("title");
                                    String genre = jsonObject.getString("genre");

                                    JSONObject jObject = jsonObject.getJSONObject("user");

                                    int userId = jObject.getInt("id");

                                    String name = jObject.getString("name");
                                    //sets the toolbar text view text
                                    if (toolbarText != null)
                                        toolbarText.setText(name);
                                    String userImagePath = jObject.getString("user_img_path");
                                    String serverPath = jObject.getString("commentUserImagePath");

                                    String imagePath = serverPath.concat(File.separator).concat(userImagePath);

                                    String replaceSlash = imagePath.replace("\\", "/");
                                    imagePath = "http:" + "//" + replaceSlash;

                                    Glide.with(context.getApplicationContext()).load(imagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).override(80, 80).into(circleImageView);

                                    String albumArt = jsonObject.getString("album_art_path");
                                    String musicPath = jsonObject.getString("music_path");
                                    String date = jsonObject.getString("published_date");

                                    String[] splitDate = date.split("T");

                                    JSONArray comments = jsonObject.getJSONArray("comments");
                                    JSONArray likes = jsonObject.getJSONArray("likes");


                                    SessionManager session = new SessionManager(context.getApplicationContext());
                                    final HashMap<String, Integer> sessionUserId = session.getUserDetails();
                                    final int sesUserId = sessionUserId.get(SessionManager.KEY_ID);


                                    Music music = new Music(id, title, genre, name, albumArt, musicPath, 0, splitDate[0], likes.length(), comments.length(), sesUserId);

                                    for (int j = 0; j < likes.length(); j++) {
                                        JSONObject likeObject = likes.getJSONObject(j);
                                        JSONObject userObject = likeObject.getJSONObject("user");
                                        JSONObject musicObject = likeObject.getJSONObject("music");

                                        int likeUserId = userObject.getInt("id");
                                        int musicId = musicObject.getInt("id");

                                        if (likeUserId == sesUserId && musicId == id) {
                                            music.setLiked(likeObject.getBoolean("liked"));
                                        }
                                    }

                                    music.getUser().setId(userId);
                                    music.setFrom(from);

                                    musicList.add(music);
                                    profileMusicAdapter.notifyDataSetChanged();

                                }
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                    } else {
                        if (musicList != null) {
                            musicList.clear();
                            profileMusicAdapter.notifyDataSetChanged();
                        }
                        textView.setText(String.valueOf(length));
                    }
                    if (musicList != null) {
                        if (musicList.size() > 0) {
                            emptyProfile.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            emptyProfile.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
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
        });
        VolleyUtil.setRetryPolicyForVolley(stringRequest);
        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(stringRequest);
    }

    /**
     * gets the edit profile information
     *
     * @param id             user id
     * @param imageView      image view
     * @param editTextList   edit text list
     * @param genderSpinner  gender spinner
     * @param context        current context
     * @param imagePath      image path
     * @param progressDialog progress dialog
     */
    @Override
    public void getEditProfileInfo(int id, CircleImageView imageView, List<EditText> editTextList, Spinner genderSpinner, Context context, String imagePath, ProgressDialog progressDialog) {
        String replaceSlash = imagePath.replace("\\", "/");
        imagePath = "http:" + "//" + replaceSlash;

        Glide.with(context.getApplicationContext()).load(imagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).override(128, 128).into(imageView);
        getProfileInfoForEdit(USER_INFO_URL + id, context, editTextList, genderSpinner, progressDialog);

    }


    /**
     * Gets the user info for the edit
     *
     * @param userInfoUrl    user information url
     * @param context        current context
     * @param editTextList   edit text list
     * @param genderSpinner  gender spinner
     * @param progressDialog progress dialog
     */
    private void getProfileInfoForEdit(String userInfoUrl, final Context context, final List<EditText> editTextList, final Spinner genderSpinner, final ProgressDialog progressDialog) {

        final StringRequest stringRequest = new StringRequest(Request.Method.GET, userInfoUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String name = jsonObject.getString("name");
                    editTextList.get(0).setText(name);
                    String username = jsonObject.getString("username");
                    editTextList.get(1).setText(username);
                    String email = jsonObject.getString("email");
                    editTextList.get(2).setText(email);
                    String gender = jsonObject.getString("gender");
                    selectSpinnerValue(genderSpinner, gender);
                    long phoneno = jsonObject.getLong("phone_no");
                    editTextList.get(3).setText(String.valueOf(phoneno));
                    progressDialog.dismiss();
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
        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(stringRequest);
    }

    /**
     * Selects the spinner value
     *
     * @param spinner  gender spinner
     * @param myString string value for the spinner
     */
    private void selectSpinnerValue(Spinner spinner, String myString) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(myString)) {
                spinner.setSelection(i);
                break;
            }
        }
    }


    /**
     * Uploads the user images and updates the profile
     *
     * @param user           user object
     * @param imagePath      image path
     * @param imageView      image view
     * @param context        current context
     * @param progressDialog progress dialog
     */
    @Override
    public void uploadImageAndUpdateProfile(User user, String imagePath, CircleImageView imageView, Context context, ProgressDialog progressDialog) {
        try {
            if (imagePath != null) {
                String finalPath = Utils.decodeFile(imagePath);
                File f = new File(finalPath);
                MultipartEntity entity = getMultipartEntity(f, user);
                this.uploadImageAndUpdateProfileFromVolley(user, context, entity, progressDialog);
            } else {
                this.updateProfile(user, "", context.getApplicationContext(), progressDialog);
            }

        } catch (NoSuchMethodError e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the profile for the user
     *
     * @param user           user object
     * @param imagePath      image path
     * @param context        current context
     * @param progressDialog progress dialog
     */
    private void updateProfile(final User user, final String imagePath, final Context context, final ProgressDialog progressDialog) {
        JSONObject params = new JSONObject();
        try {
            params.put("id", user.getId());
            params.put("phone_no", Long.parseLong(user.getPhone_no()));
            params.put("name", user.getName());
            params.put("username", user.getUsername());
            params.put("gender", user.getGender());
            params.put("email", user.getEmail());
            params.put("user_img_path", imagePath);
            params.put("password", user.getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.PUT, UPDATE_PROFILE_URL, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String code = response.getString("code").toString();
                            String message = response.getString("message").toString();
                            String imagePath = response.getString("userImagePath").toString();
                            if (code.equals("update_success")) {
                                session = new SessionManager(context.getApplicationContext());
                                session.createUserLoginSession(user.getId(), imagePath, user.getUsername());
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
     * Uploads the image and update the user profile
     *
     * @param user           user profile
     * @param context        current context
     * @param entity         multipart entity
     * @param progressDialog progress dialog
     */
    private void uploadImageAndUpdateProfileFromVolley(final User user, final Context context, MultipartEntity entity, final ProgressDialog progressDialog) {
        final CustomResponse[] finalResponse = {new CustomResponse()};
        CustomMultipartRequest req = new CustomMultipartRequest(
                FILE_UPLOAD_URL,  //url where you want to send the multipart request
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

                        updateProfile(user, response, context.getApplicationContext(), progressDialog);
                    }
                },  //instance of onResponse Listener
                entity);
        VolleyUtil.setRetryPolicyForVolley(req);

        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * Gets the multipart entity from the file
     *
     * @param f    file object
     * @param user user object
     * @return multipart entity
     * @throws UnsupportedEncodingException unsupported encoding
     */
    @NonNull
    private MultipartEntity getMultipartEntity(File f, User user) throws UnsupportedEncodingException {
        MultipartEntity entity = new MultipartEntity();
        FileBody fileBody = new FileBody(f);
        entity.addPart("photo_file", fileBody);
        entity.addPart("file_name", new StringBody(fileBody.getFilename()));
        entity.addPart("from", new StringBody("update"));
        entity.addPart("id", new StringBody(String.valueOf(user.getId())));
        return entity;
    }


}
