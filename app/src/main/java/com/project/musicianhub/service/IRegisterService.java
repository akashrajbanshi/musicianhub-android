package com.project.musicianhub.service;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ImageView;

import com.project.musicianhub.model.User;

/**
 * Register Service interface
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public interface IRegisterService {

    /**
     * creates the user
     *
     * @param user
     * @param imagePath
     * @param context
     * @param builder
     * @param progressDialog
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void createUser(User user, String imagePath, Context context, final AlertDialog.Builder builder, ProgressDialog progressDialog);

    /**
     * uploads image and register/create user
     *
     * @param user
     * @param imagePath
     * @param imageView
     * @param context
     * @param builder
     * @param progressDialog
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void uploadImageAndRegister(final User user, final String imagePath, ImageView imageView, final Context context, final AlertDialog.Builder builder, ProgressDialog progressDialog);
}
