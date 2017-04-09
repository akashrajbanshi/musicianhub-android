package com.project.musicianhub;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.project.musicianhub.adapter.ProfileMusicAdapter;
import com.project.musicianhub.model.Music;
import com.project.musicianhub.service.FollowServiceImpl;
import com.project.musicianhub.service.MediaPlayerService;
import com.project.musicianhub.service.ProfileServiceImpl;
import com.project.musicianhub.util.SessionManager;
import com.project.musicianhub.util.Utils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Profile Activity for the app which controls all the major function for the user's profille view
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class ProfileActivity extends AppCompatActivity {

    //session
    SessionManager session;
    //circle image view
    CircleImageView imageView;
    //recycler view
    RecyclerView recyclerView;
    //profile music adapter
    private ProfileMusicAdapter musicAdapter;
    //music list
    private List<Music> musicList;
    //swipe refresh layout
    SwipeRefreshLayout swipeRefreshLayout;
    //text view for the UI components of profile activity
    TextView editProfileTxtView, followerTxtView, followingTxtView, numberOfPostTxtView, numberOfFollowerTxtView, numberOfFollowingTxtView, toolbarText, profileTextView, emptyProfile;
    //linear layout for followers and following UI component
    LinearLayout followersLinearLayout, followingLinearLayout;
    //follow button
    Button followButton;

    ProgressBar progressBar;

    //profile service
    ProfileServiceImpl profileService;
    //follow service
    FollowServiceImpl followService;
    //from main or post activity
    String from;

    //gets the music player service
    private MediaPlayerService player;
    //indicates if the service is bounded to activity
    boolean serviceBound = false;
    //new audio play broadcast message
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.project.musicianhub.musicplayer.PlayNewAudio";

    //slide up panel
    SlidingUpPanelLayout slidingUpPanelLayout;
    //play/pause status button image
    ImageView playerStatus;
    //artist name text view
    TextView artistName;
    //music title text view
    TextView titleName;
    //album art image view
    ImageView albumArt;
    //handler for thread
    final Handler handler = new Handler();
    //music object
    Music musicObj;
    //facebook share manager and share dialog
    private CallbackManager callbackManager;
    private LoginManager manager;
    ShareDialog shareDialog;

    /**
     * binding the client to the media player service
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            player.setPlayerStatus(playerStatus);
            player.setAlbumArt(albumArt);
            player.setArtistName(artistName);
            player.setTitleName(titleName);
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public void playAudio(final Music music, final int position, final List<Music> musicList) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //there's no music
                if (music != null) {
                    musicObj = music;
                    artistName = (TextView) slidingUpPanelLayout.findViewById(R.id.player_artist);
                    titleName = (TextView) slidingUpPanelLayout.findViewById(R.id.player_song_title);
                    albumArt = (ImageView) slidingUpPanelLayout.findViewById(R.id.player_album_art);
                    //no service is bound
                    if (!serviceBound) {
                        Intent playerIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
                        playerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        int playerStat = (int) playerStatus.getTag();
                        if (playerStat == android.R.drawable.ic_media_pause) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playerStatus.setTag(android.R.drawable.ic_media_play);
                                    Glide.with(getApplicationContext()).load(android.R.drawable.ic_media_play).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(playerStatus);
                                }
                            });
                            playerIntent.putExtra("fromPlayer", "playing");
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playerStatus.setTag(android.R.drawable.ic_media_pause);
                                    Glide.with(getApplicationContext()).load(android.R.drawable.ic_media_pause).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(playerStatus);
                                }
                            });
                            playerIntent.putExtra("fromPlayer", "paused");
                        }
                        playerIntent.putExtra("Music", musicObj);
                        playerIntent.putExtra("musicPosition", position);
                        playerIntent.putExtra("musicList", (Serializable) musicList);
                        playerIntent.putExtra("resetPlayList", true);
                        startService(playerIntent);
                        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);


                    } else {
                        //Service is active
                        //Send a broadcast to the service -> PLAY_NEW_AUDIO

                        Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
                        broadcastIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        int playerStat = (int) playerStatus.getTag();
                        if (playerStat == android.R.drawable.ic_media_pause) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    playerStatus.setTag(android.R.drawable.ic_media_play);
                                    Glide.with(getApplicationContext()).load(android.R.drawable.ic_media_play).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(playerStatus);
                                }
                            });


                            broadcastIntent.putExtra("fromPlayer", "playing");

                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    playerStatus.setTag(android.R.drawable.ic_media_pause);
                                    Glide.with(getApplicationContext()).load(android.R.drawable.ic_media_pause).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(playerStatus);
                                }
                            });

                            broadcastIntent.putExtra("fromPlayer", "paused");
                        }
                        broadcastIntent.putExtra("musicPosition", position);
                        broadcastIntent.putExtra("Music", musicObj);
                        broadcastIntent.putExtra("resetPlayList", true);
                        broadcastIntent.putExtra("musicList", (Serializable) musicList);
                        sendBroadcast(broadcastIntent);
                    }
                    session.saveMusicInformation(musicObj.getTitle(), musicObj.getName(), musicObj.getAlbumArtPath(), musicObj.getMusicPath());
                } else {
                    //there's music
                    Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
                    broadcastIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    int playerStat = (int) playerStatus.getTag();
                    if (playerStat == android.R.drawable.ic_media_pause) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                playerStatus.setTag(android.R.drawable.ic_media_play);
                                Glide.with(getApplicationContext()).load(android.R.drawable.ic_media_play).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(playerStatus);
                            }
                        });

                        broadcastIntent.putExtra("fromPlayer", "playing");

                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                playerStatus.setTag(android.R.drawable.ic_media_pause);
                                Glide.with(getApplicationContext()).load(android.R.drawable.ic_media_pause).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(playerStatus);
                            }
                        });
                        broadcastIntent.putExtra("fromPlayer", "paused");
                    }
                    broadcastIntent.putExtra("actionValue", "updateMusicPlayerUI");
                    sendBroadcast(broadcastIntent);


                }
            }
        }

        );
        thread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            player.stopSelf();
        }

    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        View view = View.inflate(this, R.layout.activity_main, null);

        emptyProfile = (TextView) findViewById(R.id.empty_profile_activity);

        slidingUpPanelLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
        playerStatus = (ImageView) slidingUpPanelLayout.findViewById(R.id.player_status);
        playerStatus.setTag(android.R.drawable.ic_media_play);

        //UI initialization of the profile activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbarText = (TextView) toolbar.findViewById(R.id.toolbar_title);
        followButton = (Button) findViewById(R.id.user_follow_user_button);
        followButton.setVisibility(View.GONE);
        imageView = (CircleImageView) findViewById(R.id.user_profile_user_image);
        editProfileTxtView = (TextView) findViewById(R.id.user_profile_edit_text_view);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeUserProfileRefreshLayout);
        numberOfPostTxtView = (TextView) findViewById(R.id.user_post_number);
        numberOfFollowerTxtView = (TextView) findViewById(R.id.user_followers_number);
        numberOfFollowingTxtView = (TextView) findViewById(R.id.user_following_number);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);


        //gets the extra data from the intent
        Intent intent = getIntent();
        final int userIdFromPost = intent.getIntExtra("userId", 0);
        final String username = intent.getStringExtra("username");
        final String from = intent.getStringExtra("from");
        final String userImagePath = intent.getStringExtra("userImage");
        this.from = from;


        profileTextView = (TextView) findViewById(R.id.user_profile_name);
        profileTextView.setText(username);


        followService = new FollowServiceImpl();

        followerTxtView = (TextView) findViewById(R.id.user_followers_number_title);
        followerTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FollowActivity.class);
                intent.putExtra("id", userIdFromPost);
                intent.putExtra("from", "follower");
                startActivity(intent);
            }
        });

        followersLinearLayout = (LinearLayout) findViewById(R.id.user_followers_linear_layout);
        followersLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FollowActivity.class);
                intent.putExtra("id", userIdFromPost);
                intent.putExtra("from", "follower");
                startActivity(intent);
            }
        });

        followingTxtView = (TextView) findViewById(R.id.user_following_number_title);
        followingTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FollowActivity.class);
                intent.putExtra("id", userIdFromPost);
                intent.putExtra("from", "following");
                startActivity(intent);
            }
        });

        followingLinearLayout = (LinearLayout) findViewById(R.id.user_following_linear_layout);
        followingLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FollowActivity.class);
                intent.putExtra("id", userIdFromPost);
                intent.putExtra("from", "following");
                startActivity(intent);
            }
        });

        //recylcer view initialization
        recyclerView = (RecyclerView) findViewById(R.id.user_profile_recycler_view);
        musicList = new ArrayList<>();
        musicAdapter = new ProfileMusicAdapter(this, musicList, null);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(ProfileActivity.this, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(musicAdapter);


        //gets the session information
        session = new SessionManager(this);
        HashMap<String, Integer> userId = session.getUserDetails();
        HashMap<String, String> userImage = session.getUserPathDetails();
        final int id = userId.get(SessionManager.KEY_ID);
        final String imagePath = userImage.get(SessionManager.KEY_USER_IMAGE);

        //starts edit profile activity
        editProfileTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("imagePath", userImagePath);
                startActivity(intent);
            }
        });


        //action when users click the follow button
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                followService.createFollowUser(id, userIdFromPost, followButton, ProfileActivity.this.getApplicationContext(), numberOfFollowerTxtView);
            }
        });


        //checks if the session id is equals to the post's user id
        if (userIdFromPost != id) {
            //if session user id is not same as post's user id all the editing components will be hidden
            editProfileTxtView.setVisibility(View.GONE);
            followButton.setVisibility(View.VISIBLE);
            loadUserInfoForProfile(userIdFromPost, userImagePath);

            followService.checkIfFollowed(id, userIdFromPost, followButton, ProfileActivity.this.getApplicationContext(), numberOfFollowerTxtView);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {


                    //loads user info using user's id from the post
                    loadUserInfoForProfile(userIdFromPost, userImagePath);

                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        } else {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("id", id);
                    intent.putExtra("imagePath", imagePath);
                    startActivity(intent);
                }
            });
            loadUserInfoForProfile(id, imagePath);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    //loads user info using user's id from the session
                    loadUserInfoForProfile(id, imagePath);
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    /**
     * loads the user information either from the session's user id or from the music post
     *
     * @param id
     * @param imagepath
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void loadUserInfoForProfile(int id, String imagepath) {
        progressBar.setVisibility(View.VISIBLE);
        profileService = new ProfileServiceImpl();
        List<TextView> textViewList = new ArrayList<>();
        textViewList.add(numberOfPostTxtView);
        textViewList.add(numberOfFollowerTxtView);
        textViewList.add(numberOfFollowingTxtView);
        textViewList.add(toolbarText);
        String from = "profileActivity";
        profileService.getUserInfo(id, imageView, imagepath, this.getApplicationContext(), musicList, musicAdapter, textViewList, progressBar, from, emptyProfile, recyclerView);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (from.equals("musicPost") || from.equals("follow") || from.equals("comment"))
                    NavUtils.navigateUpFromSameTask(this);
                else {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                return true;
            default:
                break;
        }
        return true;
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Profile Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    /**
     * Shares the music link to the facebook
     *
     * @param music   music object
     * @param context current context
     */
    public void shareToFacebook(final Music music, Context context) {
        FacebookSdk.sdkInitialize(context.getApplicationContext());

        shareDialog = new ShareDialog(this);

        callbackManager = CallbackManager.Factory.create();

        List<String> permissionNeeds = Arrays.asList("publish_actions");

        manager = LoginManager.getInstance();

        manager.logInWithPublishPermissions(this, permissionNeeds);

        manager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                publishMusicPost(music);
            }

            @Override
            public void onCancel() {
                System.out.println("onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                System.out.println("onError");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
        callbackManager.onActivityResult(requestCode, responseCode, data);
    }

    /**
     * Publish music post to facebook wall
     *
     * @param music music object
     */
    private void publishMusicPost(Music music) {
        Bitmap image = null;
        try {
            image = new Utils().new Connection().execute(music.getAlbumArtPath()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle(music.getTitle())
                    .setImageUrl(Uri.parse("http://i.imgur.com/KyIf1m0.png"))
                    .setContentDescription(
                            music.getName())
                    .setContentUrl(Uri.parse("MUSICIANHUB.com"))
                    .setShareHashtag(new ShareHashtag.Builder()
                            .setHashtag("#MusicianHub")
                            .build())
                    .build();

            shareDialog.show(linkContent);
        }

    }
}
