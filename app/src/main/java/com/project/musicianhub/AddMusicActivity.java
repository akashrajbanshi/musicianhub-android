package com.project.musicianhub;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
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
import android.widget.Button;
import android.widget.EditText;

import com.project.musicianhub.model.Music;
import com.project.musicianhub.service.MusicServiceImpl;
import com.project.musicianhub.util.ValidationUtil;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Add Music Activity class
 * Controls the creating and uploading of the music for the users
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class AddMusicActivity extends AppCompatActivity {

    //album art image path
    String imagePath;
    //music path for the new music
    String musicPath;
    //toolbar element of the activity
    Toolbar toolbar;
    //select music selectMusicBtn
    Button selectMusicBtn;
    //edit text for music path, title and genre
    EditText musicFilePathEditText, musicTitle, musicGenre;

    //user's id
    int userId;

    //Album art circle image view
    CircleImageView imageView;
    //unique code for the picture
    private static final int SELECT_PICTURE = 100;
    //unique code for the music
    private static final int SELECT_MUSIC = 101;

    //progress dialog
    ProgressDialog progressDialog;

    //initalizing musicService
    MusicServiceImpl musicService = new MusicServiceImpl();

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
        setContentView(R.layout.activity_add_music);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (shouldAskPermissions()) {
            askPermissions();
        }

        //UI element initalization
        toolbar = (Toolbar) findViewById(R.id.add_music_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        musicFilePathEditText = (EditText) findViewById(R.id.add_music_file_path);
        musicGenre = (EditText) findViewById(R.id.add_music_genre);
        musicTitle = (EditText) findViewById(R.id.add_music_title);

        selectMusicBtn = (Button) findViewById(R.id.add_music_find_button);

        imageView = (CircleImageView) findViewById(R.id.add_music_album_art_img_view);
        imageView.setImageResource(R.drawable.logo);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        selectMusicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMusicChooser();
            }
        });

        //getting the user's id from the activity intent extra
        Intent intent = getIntent();
        int id = intent.getIntExtra("userId", 0);
        userId = id;

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        int id = intent.getIntExtra("userId", 0);
        userId = id;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
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

    /**
     * Starts the music chooser intents
     *
     * @author Akash Rajbanshi
     * @since 1.0
     */
    void openMusicChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_MUSIC);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    String path = getImagePathFromURI(selectedImageUri);
                    imagePath = path;
                    imageView.setImageURI(selectedImageUri);
                }
            } else if (requestCode == SELECT_MUSIC) {
                // Get the url from data
                Uri selectedMusicUri = data.getData();
                if (null != selectedMusicUri) {
                    String path = getMusicPathFromURI(selectedMusicUri);
                    musicPath = path;
                    musicFilePathEditText.setText(musicPath);
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
    public String getImagePathFromURI(Uri contentUri) {
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

    /**
     * Get the music path from the URI
     *
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public String getMusicPathFromURI(Uri contentUri) {
        String path = "";
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        cursor.moveToFirst();
        if (cursor != null && cursor.moveToFirst()) {
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor.close();


            cursor = getContentResolver().query(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Audio.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            cursor.close();
        }

        return path;
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
                progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Please Wait");
                progressDialog.setMessage("Saving your awesome music...");
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(false);
                progressDialog.setIcon(R.drawable.logo);
                //sets the properties for the music object
                Music music = new Music();
                music.setTitle(musicTitle.getText().toString());
                music.setGenre(musicGenre.getText().toString());
                music.getUser().setId(userId);
                //upload and creates the music for the user
                boolean checkEmptyAddMusicForm = ValidationUtil.validateEmptyAddEditMusicForm(musicTitle, musicGenre, musicFilePathEditText);
                if (!checkEmptyAddMusicForm) {
                    progressDialog.show();
                    musicService.uploadAndCreateMusic(music, imagePath, musicPath, imageView, AddMusicActivity.this, progressDialog);
                    item.setEnabled(false);
                }
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
