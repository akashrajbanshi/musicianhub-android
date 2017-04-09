package com.project.musicianhub.service;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.project.musicianhub.singleton.Singleton;
import com.project.musicianhub.util.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Notification service class
 * Enables user to send notification to user
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class NotificationServiceImpl {

    /**
     * Sends the notification to the user
     *
     * @param fromId  from user user id
     * @param toId    to user id
     * @param url     url to send the notification
     * @param type    notification type
     * @param context current context
     */
    public void sendNotificationToUser(int fromId, int toId, String url, String type, Context context) {

        String finalUrl = url + fromId + "/notification/sendNotification/" + toId;
        JSONObject params = new JSONObject();
        try {
            params.put("type", type);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, finalUrl, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

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
     * Send notification to the user
     *
     * @param fromId  from user id
     * @param toId    to user id
     * @param musicId music id
     * @param url     url to send the notificaion
     * @param type    notification type
     * @param context current context
     */
    public void sendNotificationToUser(int fromId, int toId, int musicId, String url, String type, Context context) {

        String finalUrl = url + fromId + "/notification/sendNotification/" + toId;
        JSONObject params = new JSONObject();
        try {
            params.put("type", type);
            JSONObject userParam = new JSONObject();
            userParam.put("id", musicId);
            params.put("music", userParam);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, finalUrl, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

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
     * Gets all the notification
     *
     * @param url     url to get the notification
     * @param context current context
     */
    public void getAllNotification(String url, final Context context) {
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


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
