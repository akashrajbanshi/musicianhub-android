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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Edit Music Activity class
 * Controls the editing of the music and related information for the music post
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class EditMusicActivity extends AppCompatActivity {

    //album art image path
    String imagePath;
    //music path
    String musicPath;
    //user's id
    int userId;
    //activity toolbar
    Toolbar toolbar;
    //select music button
    Button selectMusicBtn;
    //edit text for the music file, id, title and genre
    EditText musicFilePathEditText, musicIdEditText, musicTitleEditText, musicGenreEditText;
    //progress dialog
    ProgressDialog progressDialog;
    //music service initialization
    MusicServiceImpl musicService = new MusicServiceImpl();
    //circle image view
    CircleImageView imageView;
    //unique code for picture
    private static final int SELECT_PICTURE = 100;
    //unique code for music
    private static final int SELECT_MUSIC = 101;
    //from main or post activity
    String from;

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
        setContentView(R.layout.activity_edit_music);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (shouldAskPermissions()) {
            askPermissions();
        }

        //UI element initialization
        toolbar = (Toolbar) findViewById(R.id.edit_music_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        musicFilePathEditText = (EditText) findViewById(R.id.edit_music_file_path);
        musicGenreEditText = (EditText) findViewById(R.id.edit_music_genre);
        musicTitleEditText = (EditText) findViewById(R.id.edit_music_title);
        musicIdEditText = (EditText) findViewById(R.id.edit_music_id);
        selectMusicBtn = (Button) findViewById(R.id.edit_music_find_button);

        imageView = (CircleImageView) findViewById(R.id.edit_music_album_art_img_view);
        imageView.setImageResource(R.drawable.logo);

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

        selectMusicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMusicChooser();
            }
        });

        //gets the intent's extra data
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String genre = intent.getStringExtra("genre");
        String imagePath = intent.getStringExtra("imagePath");
        String musicPath = intent.getStringExtra("musicPath");
        //music id
        int id = intent.getIntExtra("id", 0);
        int userId = intent.getIntExtra("userId", 0);
        this.userId = userId;
        final String from = intent.getStringExtra("from");
        this.from = from;

        //sets the value for music in an string array
        List<String> strings = new ArrayList<String>();
        strings.add(title);
        strings.add(genre);
        strings.add(imagePath);
        strings.add(musicPath);
        //sets the value for edit text in an edit text array
        List<EditText> editTextList = new ArrayList<>();
        editTextList.add(musicIdEditText);
        editTextList.add(musicTitleEditText);
        editTextList.add(musicGenreEditText);
        editTextList.add(musicFilePathEditText);

        //gets the edit music info
        getEditMusicInfo(id, imageView, editTextList, strings);

    }

    /**
     * gets the music info for its editing
     *
     * @param id
     * @param imageView
     * @param editTextList
     * @param strings
     * @author Akash Rajbanshi
     * @since 1.0
     */
    private void getEditMusicInfo(int id, CircleImageView imageView, List<EditText> editTextList, List<String> strings) {
        progressDialog.setMessage("Loading your awesome music info...");
        progressDialog.show();
        musicService.getEditMusicInto(id, imageView, editTextList, strings, this.getApplicationContext(), progressDialog);
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
                progressDialog.setMessage("Editing your awesome music...");
                progressDialog.setCancelable(false);
                Music music = new Music();
                music.setUserId(userId);
                music.setId(Integer.parseInt(musicIdEditText.getText().toString()));
                music.setTitle(musicTitleEditText.getText().toString());
                music.setGenre(musicGenreEditText.getText().toString());
                boolean checkEmptyEditMusicForm = ValidationUtil.validateEmptyAddEditMusicForm(musicTitleEditText, musicGenreEditText, musicFilePathEditText);
                if (!checkEmptyEditMusicForm) {
                    progressDialog.show();
                    musicService.uploadAndUpdateMusic(music, imagePath, musicPath, imageView, EditMusicActivity.this.getApplicationContext(), progressDialog);
                    item.setEnabled(false);
                }
                break;
            case android.R.id.home:
                if (from.equals("profileMusic")) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            default:
                break;
        }
        return true;
    }
}
