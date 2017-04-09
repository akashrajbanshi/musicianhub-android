package com.project.musicianhub.service;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.ImageView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.project.musicianhub.LoginActivity;
import com.project.musicianhub.model.CustomResponse;
import com.project.musicianhub.model.User;
import com.project.musicianhub.singleton.Singleton;
import com.project.musicianhub.util.CustomMultipartRequest;
import com.project.musicianhub.util.Utils;
import com.project.musicianhub.util.VolleyUtil;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * Register Service Implementation class
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class RegisterServiceImpl implements IRegisterService {

    private static final String REG_URL = "http://192.168.137.192:8080/musicianhub/webapi/users/create";
    private static final String FILE_UPLOAD_URL = "http://192.168.137.192:8080/musicianhub/webapi/users/upload";

    /**
     * creates the user
     *
     * @param user           user object
     * @param imagePath      image path
     * @param context        current context
     * @param builder        alert dialog builder
     * @param progressDialog progress dialog
     */
    @Override
    public void createUser(User user, String imagePath, final Context context, final AlertDialog.Builder builder, final ProgressDialog progressDialog) {
        JSONObject params = new JSONObject();
        try {
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


        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, REG_URL, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String code = response.getString("code").toString();
                            String message = response.getString("message").toString();
                            builder.setTitle("Server Response");
                            builder.setMessage(message);
                            progressDialog.dismiss();

                            displayAlert(code, builder, context);

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
     * uploads image and register/create user
     *
     * @param user           user object
     * @param imagePath      image path
     * @param imageView      image view
     * @param context        current context
     * @param builder        alert dialog builder
     * @param progressDialog progress dialog
     */
    @Override
    public void uploadImageAndRegister(final User user, final String imagePath, ImageView imageView, final Context context, AlertDialog.Builder builder, ProgressDialog progressDialog) {
        try {
            if (imagePath != null) {
                String finalPath = Utils.decodeFile(imagePath);
                File f = new File(finalPath);
                MultipartEntity entity = getMultipartEntity(f);
                this.uploadImageAndRegisterFromVolley(user, context, entity, builder, progressDialog);
            } else {
                this.createUser(user, "", context.getApplicationContext(), builder, progressDialog);
            }

        } catch (NoSuchMethodError e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    /**
     * Uploads and registers user using custom volley request
     *
     * @param user           user object
     * @param context        current context
     * @param entity         multipart entity
     * @param builder        alert dialog builder
     * @param progressDialog progress dialog
     * @return CustomResponse custom response
     */
    private CustomResponse uploadImageAndRegisterFromVolley(final User user, final Context context, MultipartEntity entity, final AlertDialog.Builder builder, final ProgressDialog progressDialog) {
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

                        createUser(user, response, context.getApplicationContext(), builder, progressDialog);
                    }
                },  //instance of onResponse Listener
                entity);
        VolleyUtil.setRetryPolicyForVolley(req);

        Singleton.getmInstance(context.getApplicationContext()).addToRequestQueue(req);
        return finalResponse[0];
    }

    /**
     * Gets and sets multipart entity
     *
     * @param f file object
     * @return multipart entity
     * @throws UnsupportedEncodingException unsupported encoding
     */
    private MultipartEntity getMultipartEntity(File f) throws UnsupportedEncodingException {
        MultipartEntity entity = new MultipartEntity();
        FileBody fileBody = new FileBody(f);
        entity.addPart("photo_file", fileBody);
        entity.addPart("file_name", new StringBody(fileBody.getFilename()));
        entity.addPart("from", new StringBody("register"));
        return entity;
    }


    /**
     * Display the alert box after registration sucess
     *
     * @param code    alert code
     * @param builder alert dialog builder
     * @param context current context
     */
    public void displayAlert(final String code, AlertDialog.Builder builder, final Context context) {
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (code.equals("reg_success")) {
                    Intent intent = new Intent(context.getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.getApplicationContext().startActivity(intent);
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


}
