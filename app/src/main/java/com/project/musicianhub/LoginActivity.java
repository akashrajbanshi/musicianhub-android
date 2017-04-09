package com.project.musicianhub;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.project.musicianhub.service.LoginServiceImpl;
import com.project.musicianhub.util.SessionManager;
import com.project.musicianhub.util.ValidationUtil;


/**
 * A login screen that offers login via username/password.
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class LoginActivity extends AppCompatActivity {

    //progress dialog
    ProgressDialog progressDialog;
    //session element
    SessionManager session;
    //initializing the login service
    LoginServiceImpl loginService = new LoginServiceImpl();
    //textview for registration action
    TextView registerTextView;
    //login button
    Button loginBtn;
    //user name edit text
    EditText username;
    //password edit text
    EditText password;
    //variable for username and password
    String uname, pssword;
    //alert dialog
    AlertDialog.Builder builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //initializing the UI element
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        loginBtn = (Button) findViewById(R.id.login_button);
        registerTextView = (TextView) findViewById(R.id.registerTextView);
        builder = new AlertDialog.Builder(LoginActivity.this);

        //initializing the progress dialog
        progressDialog = new ProgressDialog(this);


        //initialize the register activity
        registerTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        //initialize the session object
        session = new SessionManager(getApplicationContext());

        //login button triggered
        loginBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                uname = username.getText().toString();
                pssword = password.getText().toString();
                boolean isFormEmpty = ValidationUtil.validateEmptyLoginForm(username, password);
                if (!isFormEmpty) {
                    progressDialog.setTitle("Please Wait");
                    progressDialog.setMessage("Logging in...");
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    progressDialog.setIcon(R.drawable.logo);
                    progressDialog.show();
                    //logins the user
                    loginService.userLogin(builder, username, password, uname, pssword, session, LoginActivity.this, progressDialog);
                }
            }
        });

    }
}

