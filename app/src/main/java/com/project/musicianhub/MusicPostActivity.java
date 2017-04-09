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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import com.project.musicianhub.adapter.MusicPostAdapter;
import com.project.musicianhub.model.Music;
import com.project.musicianhub.service.MediaPlayerService;
import com.project.musicianhub.service.MusicServiceImpl;
import com.project.musicianhub.util.SessionManager;
import com.project.musicianhub.util.Utils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Music Post Activity class which controls the single post view of the application
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class MusicPostActivity extends AppCompatActivity {

    //recyclerview for the single post
    RecyclerView recyclerView;
    //music post adapter
    private MusicPostAdapter musicPostAdapter;
    //music list arraylist
    private List<Music> musicList;
    //swipe refresh layout
    SwipeRefreshLayout swipeRefreshLayout;
    //music service
    MusicServiceImpl musicService;


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
    //session
    SessionManager session;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_post);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //UI component initialization
        Toolbar toolbar = (Toolbar) findViewById(R.id.music_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        session = new SessionManager(getApplicationContext());

        View view = View.inflate(this, R.layout.activity_main, null);

        slidingUpPanelLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
        playerStatus = (ImageView) slidingUpPanelLayout.findViewById(R.id.player_status);
        playerStatus.setTag(android.R.drawable.ic_media_play);


        //gets the intent data
        Intent intent = getIntent();
        final int musicId = intent.getIntExtra("musicId", 0);


        //gets and initializes the recylcer view for the single post
        recyclerView = (RecyclerView) findViewById(R.id.post_recycler_view);
        musicList = new ArrayList<>();
        musicPostAdapter = new MusicPostAdapter(this, musicList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(musicPostAdapter);


        //initializes the swipe refresh layout for the single post
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipePostRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMusicInfo(musicId);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //loads the music info to the single post
        loadMusicInfo(musicId);
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Intent intent = getIntent();
        final int musicId = intent.getIntExtra("musicId", 0);

        loadMusicInfo(musicId);
    }

    /**
     * loads the music info to the single post
     *
     * @param musicId
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void loadMusicInfo(int musicId) {
        musicService = new MusicServiceImpl();
        musicList.clear();
        musicPostAdapter.notifyDataSetChanged();
        musicService.loadMusicInfo(musicId, this, musicList, musicPostAdapter);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
