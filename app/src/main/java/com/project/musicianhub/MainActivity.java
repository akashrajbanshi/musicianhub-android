package com.project.musicianhub;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.iid.FirebaseInstanceId;
import com.project.musicianhub.adapter.ViewPagerAdapter;
import com.project.musicianhub.model.Music;
import com.project.musicianhub.model.Notification;
import com.project.musicianhub.service.LoginServiceImpl;
import com.project.musicianhub.service.MediaPlayerService;
import com.project.musicianhub.util.SessionManager;
import com.project.musicianhub.util.Utils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.fabric.sdk.android.Fabric;

/**
 * Main Activity class for the Musician Hub Application
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class MainActivity extends AppCompatActivity {

    //session
    SessionManager session;
    //tab layout for the fragments
    TabLayout tabLayout;
    //view the page
    ViewPager viewPager;
    //adpater for the page viewer
    ViewPagerAdapter viewPagerAdapter;

    //login service
    LoginServiceImpl loginService;
    //get the final notification count
    int finalNotificationCount;
    //broadcast receiver for notification registration
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    //list of notifications
    private ArrayList<Notification> notifications = new ArrayList<>();

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

    //music object
    Music musicObj;


    //facebook share manager and share dialog
    private CallbackManager callbackManager;
    private LoginManager manager;
    ShareDialog shareDialog;

    //handler for thread
    final Handler handler = new Handler();


    /**
     * binding the client to the media player service
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();

            player.setMainActivity(MainActivity.this);
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

    /**
     * Plays the required audio
     *
     * @param music     music list
     * @param position  current music position in the list
     * @param musicList current music list
     */
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
                        player.setPlayerStatus(playerStatus);
                        player.setAlbumArt(albumArt);
                        player.setArtistName(artistName);
                        player.setTitleName(titleName);

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
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            player.stopSelf();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //UI initialization
        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        artistName = (TextView) slidingUpPanelLayout.findViewById(R.id.player_artist);
        titleName = (TextView) slidingUpPanelLayout.findViewById(R.id.player_song_title);
        albumArt = (ImageView) slidingUpPanelLayout.findViewById(R.id.player_album_art);

        playerStatus = (ImageView) slidingUpPanelLayout.findViewById(R.id.player_status);
        playerStatus.setTag(android.R.drawable.ic_media_play);


        session = new SessionManager(getApplicationContext());

        if (session.checkLogin())
            finish();

        //initializing the music player after the application is re opened
        final HashMap<String, String> songUrl = session.getSongUrl();
        final String[] songURL = {songUrl.get(SessionManager.KEY_SONG_URL)};

        final HashMap<String, String> albumsArt = session.getAlbumArt();
        final HashMap<String, String> title = session.getTitle();
        final HashMap<String, String> artistNames = session.getArtistName();


        final String albumArtURL = albumsArt.get(SessionManager.KEY_URL);
        final String songTitle = title.get(SessionManager.KEY_TITLE);
        final String artistsName = artistNames.get(SessionManager.KEY_ARTIST_NAME);

        playerStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songURL[0].length() == 0)
                    playAudio(null, 0, null);
                else {
                    if (!serviceBound) {
                        Music musicPlayer = new Music();
                        musicPlayer.setMusicPath(songURL[0]);
                        musicPlayer.setAlbumArtPath(albumArtURL);
                        musicPlayer.setTitle(songTitle);
                        musicPlayer.setName(artistsName);
                        Intent playerIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
                        playerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        int playerStat = (int) playerStatus.getTag();
                        if (playerStat == android.R.drawable.ic_media_pause) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Glide.with(getApplicationContext()).load(android.R.drawable.ic_media_play).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(playerStatus);
                                }
                            });
                            playerIntent.putExtra("fromPlayer", "playingSession");
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playerStatus.setTag(android.R.drawable.ic_media_pause);
                                    Glide.with(getApplicationContext()).load(android.R.drawable.ic_media_pause).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(playerStatus);
                                }
                            });
                            playerIntent.putExtra("fromPlayer", "pausedSession");
                        }
                        playerIntent.putExtra("Music", musicPlayer);
                        playerIntent.putExtra("resetPlayList", true);
                        startService(playerIntent);
                        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                        songURL[0] = "";
                    }
                }
            }
        });

        //gets session data for data loading
        HashMap<String, Integer> userId = session.getUserDetails();
        HashMap<String, String> userImage = session.getUserPathDetails();

        // user id and getting image path
        int id = userId.get(SessionManager.KEY_ID);
        String imagePath = userImage.get(SessionManager.KEY_USER_IMAGE);
        //get the firebase registration token and save to server
        loginService = new LoginServiceImpl();
        String tkn = FirebaseInstanceId.getInstance().getToken();
        loginService.registerTokenToServer(tkn, id, this);


        //initialize the tab layout
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        HomeFragment homeFragment = new HomeFragment();
        SearchFragment searchFragment = new SearchFragment();
        final NotificationFragment notificationFragment = new NotificationFragment();
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle args = new Bundle();

        args.putInt("id", id);
        args.putString("userImage", imagePath);
        homeFragment.setArguments(args);
        searchFragment.setArguments(args);
        notificationFragment.setArguments(args);
        profileFragment.setArguments(args);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(homeFragment, "");
        viewPagerAdapter.addFragments(searchFragment, "");
        viewPagerAdapter.addFragments(notificationFragment, "");
        viewPagerAdapter.addFragments(profileFragment, "");


        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(3);


        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.home);

        tabLayout.getTabAt(1).setIcon(R.drawable.search);


        //initialize the notification tablayout
        tabLayout.getTabAt(2).setCustomView(R.layout.tab_layout_badge);
        final TextView notificationTextView = (TextView) tabLayout.getTabAt(2).getCustomView().findViewById(R.id.badge);
        final View notificationContainer = tabLayout.getTabAt(2).getCustomView().findViewById(R.id.badgeContainer);


        tabLayout.getTabAt(2).setIcon(R.drawable.notification);
        tabLayout.getTabAt(3).setIcon(R.drawable.profile);

        //sends the broadcast for the push notification
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("pushNotification")) {
                    int notificationCount = intent.getIntExtra("notificationCount", 0);
                    String senderUserName = intent.getStringExtra("senderUserName");
                    String musicTitle = intent.getStringExtra("musicTitle");
                    String type = intent.getStringExtra("type");
                    int musicId = intent.getIntExtra("musicId", 0);
                    int userId = intent.getIntExtra("userId", 0);
                    String userImagePath = intent.getStringExtra("userImagePath");

                    //sets the notification object
                    Notification notification = new Notification(senderUserName, musicTitle, userImagePath, musicId, type);
                    notification.setUserId(userId);

                    //no notification available
                    if (notifications.size() == 0)
                        notifications.add(0, notification);
                    else {
                        //notification available
                        for (int i = 0; i < notifications.size(); i++) {
                            //notification for like
                            if (notifications.get(i).getName() != null && type.equals("like") && !notifications.get(i).getType().equals("follow")) {
                                if (notifications.get(i).getName().equals(senderUserName) && notifications.get(i).getMusic().equals(musicTitle) && notifications.get(i).getImagePath().equals(userImagePath) && notifications.get(i).getMusicId() == musicId && notifications.get(i).getType().equals(type)) {
                                    //if notification already available do nothing
                                    return;
                                }
                                //add to the notification list
                                notifications.add(0, notification);
                                break;

                            } else {
                                //for every other type of notification
                                if (notifications.get(i).getName() != null && !notifications.get(i).getType().equals("follow")) {
                                    //adds the notification
                                    notifications.add(0, notification);
                                    break;
                                }
                            }
                        }
                    }

                    //sends the notification to the notification fragment
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("notificationList", notifications);
                    notificationFragment.getArguments().putAll(bundle);

                    //sets the notification count to the notification text view
                    finalNotificationCount = notificationCount + finalNotificationCount;
                    notificationTextView.setText(String.valueOf(finalNotificationCount));
                    notificationContainer.setVisibility(View.VISIBLE);


                }
            }
        };

        //sets the tab click listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //if the notification tab is selected
                if (tab.getPosition() == 2) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("notificationList", notifications);
                    notificationFragment.getArguments().putAll(bundle);
                    notificationContainer.setVisibility(View.GONE);
                    finalNotificationCount = 0;
                }
                viewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //if home tab is reselected
                Fragment f = viewPagerAdapter.getItem(tab.getPosition());
                if (f != null) {
                    if (tab.getPosition() == 0) {
                        View fragmentView = f.getView();
                        if (fragmentView != null) {
                            RecyclerView mRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_view);
                            if (mRecyclerView != null)
                                mRecyclerView.smoothScrollToPosition(0);
                        }
                    }

                }

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("pushNotification"));

        final HashMap<String, String> albumsArt = session.getAlbumArt();
        final HashMap<String, String> title = session.getTitle();
        final HashMap<String, String> artistNames = session.getArtistName();


        final String albumArtURL = albumsArt.get(SessionManager.KEY_URL);
        final String songTitle = title.get(SessionManager.KEY_TITLE);
        final String artistsName = artistNames.get(SessionManager.KEY_ARTIST_NAME);

        handler.post(new Runnable() {
            @Override
            public void run() {
                titleName.setText(songTitle);
                artistName.setText(artistsName);
                Glide.with(getApplicationContext()).load(albumArtURL).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(albumArt);
            }
        });

    }


    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * Hide the soft keyboard according to the click
     *
     * @param ev motion event
     * @return true or false
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        boolean handleReturn = super.dispatchTouchEvent(ev);

        View view = getCurrentFocus();

        int x = (int) ev.getX();
        int y = (int) ev.getY();

        if (view instanceof EditText) {
            View innerView = getCurrentFocus();

            if (ev.getAction() == MotionEvent.ACTION_UP &&
                    !getLocationOnScreen((EditText) innerView).contains(x, y)) {

                InputMethodManager input = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                input.hideSoftInputFromWindow(getWindow().getCurrentFocus()
                        .getWindowToken(), 0);
            }
        }

        return handleReturn;
    }

    protected Rect getLocationOnScreen(EditText mEditText) {
        Rect mRect = new Rect();
        int[] location = new int[2];

        mEditText.getLocationOnScreen(location);

        mRect.left = location[0];
        mRect.top = location[1];
        mRect.right = location[0] + mEditText.getWidth();
        mRect.bottom = location[1] + mEditText.getHeight();

        return mRect;
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }
}
