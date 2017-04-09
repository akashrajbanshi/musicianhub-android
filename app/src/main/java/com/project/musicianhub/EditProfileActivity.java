package com.project.musicianhub;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.project.musicianhub.model.User;
import com.project.musicianhub.service.ProfileServiceImpl;
import com.project.musicianhub.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Edit Profile Activity class
 * Controls the editing of the profile and related information
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class EditProfileActivity extends AppCompatActivity {

    //logout button
    Button logOutBtn;
    //profile image path
    String imagePath;
    //toolbar for the edit profile's activity
    Toolbar toolbar;
    //image view for profile image
    CircleImageView imageView;
    //gender spinner
    Spinner genderSpinner;
    //edit text for name, username, email and phone
    EditText nameEditTxt, usernameEditTxt, emailEditTxt, phoneNoEditTxt;
    //user id text view
    TextView idTextView;
    //unique id dor picture
    private static final int SELECT_PICTURE = 100;
    //progress dialog
    ProgressDialog progressDialog;
    //profile service initialization
    ProfileServiceImpl profileService = new ProfileServiceImpl();
    //session manager
    SessionManager session;

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
        setContentView(R.layout.activity_edit_profile);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (shouldAskPermissions()) {
            askPermissions();
        }

        session = new SessionManager(getApplicationContext());


        //UI element initialization
        toolbar = (Toolbar) findViewById(R.id.edit_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        logOutBtn = (Button) findViewById(R.id.logout_button);

        nameEditTxt = (EditText) findViewById(R.id.edit_profile_name);
        usernameEditTxt = (EditText) findViewById(R.id.edit_profile_username);
        emailEditTxt = (EditText) findViewById(R.id.edit_profile_email);
        phoneNoEditTxt = (EditText) findViewById(R.id.edit_profile_phoneno);
        idTextView = (TextView) findViewById(R.id.edit_profile_id);


        imageView = (CircleImageView) findViewById(R.id.edit_profile_img_view);
        imageView.setImageResource(R.drawable.user);

        genderSpinner = (Spinner) findViewById(R.id.edit_profile_gender);
        addItemsOnGenderSpinner();

        //initialization of progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setIndeterminate(false);
        progressDialog.setIcon(R.drawable.logo);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        //gets the intent's extra data
        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        String imagePath = intent.getStringExtra("imagePath");

        //sets the value for edit text elements in an array list
        List<EditText> editTextList = new ArrayList<>();
        editTextList.add(nameEditTxt);
        editTextList.add(usernameEditTxt);
        editTextList.add(emailEditTxt);
        editTextList.add(phoneNoEditTxt);

        idTextView.setText(String.valueOf(id));

        //gets the profile info
        getEditProfileInfo(id, imageView, editTextList, genderSpinner, imagePath);

        logOutBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Clear the session data
                // This will clear all session data and
                // redirect user to LoginActivity
                session.logoutUser();
            }
        });
    }

    /**
     * Gets the profile information for edit
     *
     * @param id
     * @param imageView
     * @param editTextList
     * @param genderSpinner
     * @param imagePath
     * @author Akash Rajbanshi
     * @since 1.0
     */
    private void getEditProfileInfo(int id, CircleImageView imageView, List<EditText> editTextList, Spinner genderSpinner, String imagePath) {
        progressDialog.setMessage("Loading your profile info...");
        progressDialog.show();
        profileService.getEditProfileInfo(id, imageView, editTextList, genderSpinner, this.getApplicationContext(), imagePath, progressDialog);
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
        genderSpinner.setAdapter(dataAdapter);
    }

    /**
     * Starts the image chooser intents
     *
     * @author Akash Rajbanshi
     * @since 1.0
     */
    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK)

        {
            if (requestCode == SELECT_PICTURE) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
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
        String path = "";
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        cursor.moveToFirst();
        if (cursor != null && cursor.moveToFirst()) {
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor.close();


            cursor = getContentResolver().query(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }

        return path;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    /**
     * Based on the context the back button triggers the activity
     *
     * @param item current clicked menu item
     * @return true or false if the back button is successfully trigged
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                progressDialog.setMessage("Updating your profile...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                User user = new User();
                user.setId(Integer.parseInt(idTextView.getText().toString()));
                user.setName(nameEditTxt.getText().toString());
                user.setUsername(usernameEditTxt.getText().toString());
                user.setEmail(emailEditTxt.getText().toString());
                user.setGender(genderSpinner.getSelectedItem().toString());
                user.setPhone_no(phoneNoEditTxt.getText().toString());
                profileService.uploadImageAndUpdateProfile(user, imagePath, imageView, EditProfileActivity.this.getApplicationContext(), progressDialog);
                break;
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                break;
        }
        return true;
    }

}
