package com.project.musicianhub;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.project.musicianhub.model.User;
import com.project.musicianhub.service.RegisterServiceImpl;
import com.project.musicianhub.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Register Activity which controls the registration of the users
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    //registration button
    Button regBtn;
    //edit text for register user activity
    EditText name, email, username, password, confirmPassword, phonenumber;
    //gender spinner
    Spinner gender;
    //user image path
    String imagePath;
    //user image view
    CircleImageView imageView;
    //alert dialog
    AlertDialog.Builder builder;
    //progress dialog
    ProgressDialog progressDialog;
    //unique code for picture
    private static final int SELECT_PICTURE = 100;


    //register service
    RegisterServiceImpl registerService = new RegisterServiceImpl();

    /**
     * Check if the permission is need to be asked if the build version is below lollipop
     *
     * @return yes or no
     */
    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    /**
     * Asks the permission of the user
     */
    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //UI component initialization
        regBtn = (Button) findViewById(R.id.register_button);
        name = (EditText) findViewById(R.id.reg_name);
        email = (EditText) findViewById(R.id.reg_email);
        username = (EditText) findViewById(R.id.reg_username);
        password = (EditText) findViewById(R.id.reg_password);
        confirmPassword = (EditText) findViewById(R.id.confirm_password);
        gender = (Spinner) findViewById(R.id.reg_gender);
        phonenumber = (EditText) findViewById(R.id.reg_phoneno);
        imageView = (CircleImageView) findViewById(R.id.reg_img_view);
        imageView.setImageResource(R.drawable.user);


        imageView.setOnClickListener(this);

        //progress dialog initialization
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setIndeterminate(true);
        progressDialog.setIcon(R.drawable.logo);


        //adds items to the spinner
        addItemsOnGenderSpinner();

        builder = new AlertDialog.Builder(RegisterActivity.this);
        regBtn.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          final User user = new User(name.getText().toString(), username.getText().toString(), email.getText().toString(), gender.getSelectedItem().toString(), password.getText().toString(), confirmPassword.getText().toString(), phonenumber.getText().toString());
                                          boolean isFormEmpty = ValidationUtil.validateEmptyRegistrationForm(name, username, email, gender, password, confirmPassword, phonenumber);
                                          boolean checkEmailFormat = ValidationUtil.checkEmailFormat(email);
                                          boolean checkPasswordFormat = ValidationUtil.checkPasswordFormat(password);
                                          //checks for the empty form, email format and password format for the registration
                                          if (!isFormEmpty && !checkEmailFormat && !checkPasswordFormat) {
                                              if (!(user.getPassword().equals(user.getConfirmPassword()))) {
                                                  builder.setTitle("Something went wrong");
                                                  builder.setMessage("Password do not match!");
                                                  displayAlert("input_error");
                                              } else {
                                                  progressDialog.setMessage("Registration in Progress...");
                                                  progressDialog.setCancelable(false);
                                                  progressDialog.show();
                                                  registerService.uploadImageAndRegister(user, imagePath, imageView, RegisterActivity.this.getApplicationContext(), builder, progressDialog);
                                              }
                                          }
                                      }

                                  }
        );
    }

    /**
     * displays the alert for the registration activity
     *
     * @param code
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void displayAlert(final String code) {
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (code.equals("input_error")) {
                    password.setText("");
                    confirmPassword.setText("");
                } else if (code.equals("reg_success")) {
                    finish();
                } else if (code.equals("reg_failed")) {
                    username.getText().clear();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Starts the image chooser intents
     *
     * @author Akash Rajbanshi
     * @since 1.0
     */
    void openImageChooser() {
        if (shouldAskPermissions()) {
            askPermissions();
        }
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Get the path from the Uri
                    String path = getPathFromURI(selectedImageUri);
                    imagePath = path;
                    imageView.setImageURI(selectedImageUri);
                }
            }
        }
    }

    /**
     * Get the image path from the URI
     *
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public String getPathFromURI(Uri contentUri) {
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }


    @Override
    public void onClick(View v) {
        openImageChooser();
    }

    /**
     * sets the data to the gender spinner
     *
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void addItemsOnGenderSpinner() {
        List<String> list = new ArrayList<String>();
        list.add("MALE");
        list.add("FEMALE");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list) {
            //customizes the spinner view
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                Typeface tfavv = Typeface.create("sans-serif-condensed", Typeface.BOLD);
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setPadding(0, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                view.setTypeface(tfavv);
                view.setTextSize(16);
                return view;
            }

            //customizes the drop down view of the spinner
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                Typeface tfavv = Typeface.create("sans-serif-condensed", Typeface.BOLD);

                view.setTextColor(Color.GRAY);
                view.setTypeface(tfavv);
                view.setTextSize(30);

                return view;
            }

        };
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(dataAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }
}
