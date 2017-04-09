package com.project.musicianhub.service;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.EditText;

import com.project.musicianhub.util.SessionManager;

/**
 * Login Service Interface
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public interface ILoginService {
    /**
     * Identifies the user credentials for the login using usernamme and password
     *
     * @param builder
     * @param username
     * @param password
     * @param uname
     * @param pssword
     * @param session
     * @param context
     * @param progressDialog
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void userLogin(final AlertDialog.Builder builder, final EditText username, final EditText password, String uname, String pssword, final SessionManager session, final Context context, ProgressDialog progressDialog);

}
