package com.project.musicianhub.service;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.project.musicianhub.MainActivity;
import com.project.musicianhub.model.User;
import com.project.musicianhub.singleton.Singleton;
import com.project.musicianhub.util.SessionManager;
import com.project.musicianhub.util.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Login Service implementation class
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class LoginServiceImpl implements ILoginService {

    private static final String LOGIN_URL = "http://192.168.137.192:8080/musicianhub/webapi/users/login";
    private static final String REGISTER_TOKEN_URL = "http://192.168.137.192:8080/musicianhub/webapi/users/";

    /**
     * Identifies the user credentials for the login using usernamme and password
     *
     * @param builder        alert dialog builder
     * @param username       user name edit text
     * @param password       password edit text
     * @param uname          username text
     * @param pssword        password text
     * @param session        current session
     * @param context        current context
     * @param progressDialog progress dialog
     */
    @Override
    public void userLogin(final AlertDialog.Builder builder, final EditText username, final EditText password, String uname, String pssword, final SessionManager session, final Context context, final ProgressDialog progressDialog) {
        //sets the parameters as the json object
        JSONObject params = new JSONObject();
        try {
            params.put("username", uname);
            params.put("password", pssword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, LOGIN_URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String code = response.getString("code");
                    if (code.equals("login_failed")) {
                        builder.setTitle("Server Response...");
                        displayAlert(response.getString("message"), builder, username, password);
                        progressDialog.dismiss();
                    } else {
                        JSONObject object = response.getJSONObject("user");
                        Gson gson = new Gson();
                        //converts the json to user objecy
                        User user = gson.fromJson(object.toString(), User.class);
                        session.createUserLoginSession(object.getInt("id"), user.getName(), user.getUser_img_path());
                        //login success lands to the main activity class
                        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
                        intent.putExtra("EXIT", true);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY);
                        context.getApplicationContext().startActivity(intent);
                        progressDialog.dismiss();


                        user.setId(object.getInt("id"));
                        //get the firebase registration token and save to server
                        String tkn = FirebaseInstanceId.getInstance().getToken();
                        registerTokenToServer(tkn, object.getInt("id"), context);


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
        VolleyUtil.setRetryPolicyForVolley(jsonObjectRequest);
        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * register the unique token of the device to the server
     *
     * @param tkn     unique notification token
     * @param userId  user id
     * @param context current context
     */
    public void registerTokenToServer(String tkn, int userId, final Context context) {
        String url = REGISTER_TOKEN_URL + userId + "/notification/registerUserToken";
        //sets the parameters as the json object
        JSONObject params = new JSONObject();
        try {
            params.put("token", tkn);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        VolleyUtil.setRetryPolicyForVolley(jsonObjectRequest);
        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Display the alert message on login fail
     *
     * @param message alert message
     * @param builder alert dialog builder
     * @param username user name edit text
     * @param password password edit text
     */
    public void displayAlert(final String message, AlertDialog.Builder builder, final EditText username, final EditText password) {
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                username.setText("");
                password.setText("");
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
