package com.project.musicianhub.service;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.project.musicianhub.adapter.CommentAdapter;
import com.project.musicianhub.model.Comment;
import com.project.musicianhub.singleton.Singleton;
import com.project.musicianhub.util.VolleyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Comment Service implementation class
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */


public class CommentServiceImpl implements ICommentService {


    private static final String COMMENT_URL = "http://192.168.0.13:8080/musicianhub/webapi/music/";
    private static final String NOTIFICATION_URL = "http://192.168.0.13:8080/musicianhub/webapi/users/";

    NotificationServiceImpl notificationService;

    /**
     * Creates the comment for the music post
     *
     * @param musicId            music id
     * @param userId             user id
     * @param comment            comment object
     * @param commentList        comment list
     * @param commentAdapter     comment adapter
     * @param applicationContext application context
     * @param emptyComment       empty comment text view
     * @param recyclerView       recycler text view
     */
    @Override
    public void createComment(final int musicId, final int userId, final int musicUserId, String comment, final List<Comment> commentList, final CommentAdapter commentAdapter, final Context applicationContext, final ProgressBar progressBar, final TextView emptyComment, final RecyclerView recyclerView) {
        //gets the RestAPI url for creating comment
        String url = COMMENT_URL + musicId + "/comments/createComment";

        //passes the required parameters as a json object
        JSONObject params = new JSONObject();
        try {
            params.put("comment", comment);
            JSONObject userParam = new JSONObject();
            userParam.put("id", userId);
            params.put("user", userParam);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //gets all the comments
                        getAllComments(musicId, commentList, commentAdapter, applicationContext, progressBar, emptyComment, recyclerView);
                        notificationService = new NotificationServiceImpl();
                        if (userId != musicUserId)
                            notificationService.sendNotificationToUser(userId, 0, musicId, NOTIFICATION_URL, "comment", applicationContext);

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
        Singleton.getmInstance(applicationContext.getApplicationContext()).addToRequestQueue(postRequest);
    }

    /**
     * Gets all the comments for the music post
     *
     * @param musicId            music id
     * @param commentList        comment list
     * @param commentAdapter     comment adapter
     * @param applicationContext current context
     * @param emptyComment       emptu comment text view
     * @param recyclerView       recycler view object
     */
    @Override
    public void getAllComments(int musicId, final List<Comment> commentList, final CommentAdapter commentAdapter, final Context applicationContext, final ProgressBar progressBar, final TextView emptyComment, final RecyclerView recyclerView) {
        //gets the RestAPI url for the getting all the comments
        String url = COMMENT_URL + musicId + "/comments/allComments";
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //gets the json array response from the rest api call
                    JSONArray jsonArray = new JSONArray(response);
                    int length = jsonArray.length();
                    if (length != 0) {
                        //clears the comment list before setting the json array to comment list
                        commentList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {

                            //gets the comment object from the json object
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            String comment = jsonObject.getString("comment");


                            //gets the user object
                            JSONObject userObj = jsonObject.getJSONObject("user");


                            String userName = userObj.getString("name");
                            int userId = userObj.getInt("id");
                            String imagePath = userObj.getString("commentUserImagePath") + userObj.getString("user_img_path");

                            //sets the comment objects and updates the comment adapter
                            Comment commentObj = new Comment(id, comment, userName, imagePath, userId);
                            commentList.add(commentObj);
                            commentAdapter.notifyDataSetChanged();
                        }
                    }

                    if (commentList.size() > 0) {
                        emptyComment.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        emptyComment.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                    progressBar.setVisibility(View.GONE);
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
        Singleton.getmInstance(applicationContext.getApplicationContext()).addToRequestQueue(stringRequest);
    }

}
