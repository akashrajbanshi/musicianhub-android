package com.project.musicianhub.service;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.project.musicianhub.R;
import com.project.musicianhub.adapter.FollowAdapter;
import com.project.musicianhub.model.Follow;
import com.project.musicianhub.singleton.Singleton;
import com.project.musicianhub.util.VolleyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Follow Service Implementation class
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class FollowServiceImpl implements IFollowService {

    private static final String FOLLOW_URL = "http://192.168.137.192:8080/musicianhub/webapi/users/";


    ProfileServiceImpl profileService;
    NotificationServiceImpl notificationService;


    /**
     * creates or updates the followers and following
     *
     * @param id             user id
     * @param userIdFromPost user's id from the post
     * @param followButton   follow button
     * @param context        current context
     */
    @Override
    public void createFollowUser(final int id, final int userIdFromPost, final Button followButton, final Context context, final TextView followersTextView) {
        String url = FOLLOW_URL + id + "/follow/createFollow";
        JSONObject params = new JSONObject();
        try {
            JSONObject followUserParam = new JSONObject();
            followUserParam.put("id", userIdFromPost);
            params.put("followingUser", followUserParam);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (followButton.getText().toString().equalsIgnoreCase("follow")) {
                            followButton.setText("FOLLOWING");
                            followButton.setBackgroundResource(R.drawable.button_style_following);
                            followButton.setTextColor(R.color.album_title);
                            notificationService = new NotificationServiceImpl();
                            notificationService.sendNotificationToUser(id, userIdFromPost, FOLLOW_URL, "follow", context);
                        } else {
                            followButton.setText("FOLLOW");
                            followButton.setBackgroundResource(R.drawable.button_style);
                            followButton.setTextColor(R.color.album_title);
                        }
                        String url = "http://192.168.137.192:8080/musicianhub/webapi/users/" + userIdFromPost + "/follow/followers";
                        profileService = new ProfileServiceImpl();
                        profileService.getFollowAndPostNumber(url, context, followersTextView, null, "", null, null);

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
     * Checks if user is being followed
     *
     * @param id              user id
     * @param userIdFromPost  user's id from post
     * @param followButton    follow button
     * @param context         current context
     * @param followerTxtView followers text view
     */
    public void checkIfFollowed(final int id, final int userIdFromPost, final Button followButton, final Context context, final TextView followerTxtView) {
        final String url = FOLLOW_URL + id + "/follow/followCheck";
        JSONObject params = new JSONObject();
        try {
            JSONObject followUserParam = new JSONObject();
            followUserParam.put("id", userIdFromPost);
            params.put("followingUser", followUserParam);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean isFollowing = response.getBoolean("follow_status");

                            if (isFollowing) {
                                followButton.setText("FOLLOWING");
                                followButton.setBackgroundResource(R.drawable.button_style_following);
                                followButton.setTextColor(R.color.album_title);
                            } else {
                                followButton.setText("FOLLOW");
                                followButton.setBackgroundResource(R.drawable.button_style);
                                followButton.setTextColor(R.color.album_title);
                            }

                            String url = "http://192.168.137.192:8080/musicianhub/webapi/users/" + userIdFromPost + "/follow/followers";
                            profileService = new ProfileServiceImpl();
                            profileService.getFollowAndPostNumber(url, context, followerTxtView, null, "", null, null);

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
     * Gets all the followers for the users
     *
     * @param id            user id
     * @param followList    follow list
     * @param followAdapter follow adapter
     * @param context       current context
     * @param emptyFollower empty follower text view
     * @param recyclerView  recycler view object
     */
    public void getAllFollowers(int id, final List<Follow> followList, final FollowAdapter followAdapter, final Context context, final TextView emptyFollower, final RecyclerView recyclerView) {
        //gets the RestAPI url for the getting all the followers
        String followersUrl = "http://192.168.137.192:8080/musicianhub/webapi/users/" + id + "/follow/followers";
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, followersUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //gets the json array response from the rest api call
                    JSONArray jsonArray = new JSONArray(response);
                    int length = jsonArray.length();
                    if (length != 0) {
                        //clears the follow list before setting the json array to comment list
                        followList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {

                            //gets the follow object from the json object
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            //gets the user object
                            JSONObject userObj = jsonObject.getJSONObject("followerUser");


                            String userName = userObj.getString("name");
                            int userId = userObj.getInt("id");
                            String imagePath = userObj.getString("commentUserImagePath") + userObj.getString("user_img_path");

                            //sets the follow objects and updates the follow adapter
                            Follow follow = new Follow(userName, imagePath, userId);
                            followList.add(follow);
                            followAdapter.notifyDataSetChanged();
                        }
                    }

                    if (followList.size() > 0) {
                        emptyFollower.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        emptyFollower.setVisibility(View.VISIBLE);
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
        });
        VolleyUtil.setRetryPolicyForVolley(stringRequest);
        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(stringRequest);
    }

    /**
     * Get all the following user
     *
     * @param id             user id
     * @param followList     following list
     * @param followAdapter  following adapter
     * @param context        current context
     * @param emptyFollowing empty following text view
     * @param recyclerView   recycler view
     */
    public void getAllFollowing(int id, final List<Follow> followList, final FollowAdapter followAdapter, final Context context, final TextView emptyFollowing, final RecyclerView recyclerView) {
        //gets the RestAPI url for the getting all the followers
        String followersUrl = "http://192.168.137.192:8080/musicianhub/webapi/users/" + id + "/follow/following";
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, followersUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //gets the json array response from the rest api call
                    JSONArray jsonArray = new JSONArray(response);
                    followList.clear();
                    int length = jsonArray.length();
                    if (length != 0) {
                        //clears the follow list before setting the json array to comment list
                        for (int i = 0; i < jsonArray.length(); i++) {

                            //gets the follow object from the json object
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            //gets the user object
                            JSONObject userObj = jsonObject.getJSONObject("followingUser");


                            String userName = userObj.getString("name");
                            int userId = userObj.getInt("id");
                            String imagePath = userObj.getString("commentUserImagePath") + userObj.getString("user_img_path");

                            //sets the follow objects and updates the follow adapter
                            Follow follow = new Follow(userName, imagePath, userId);
                            followList.add(follow);
                            followAdapter.notifyDataSetChanged();
                        }
                    } else {
                        if (followList != null)
                            followList.clear();
                        followAdapter.notifyDataSetChanged();
                    }

                    if (followList.size() > 0) {
                        emptyFollowing.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        emptyFollowing.setVisibility(View.VISIBLE);
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
        });
        VolleyUtil.setRetryPolicyForVolley(stringRequest);
        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(stringRequest);
    }


}
