package com.project.musicianhub.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.project.musicianhub.MainActivity;
import com.project.musicianhub.R;
import com.project.musicianhub.model.Music;
import com.project.musicianhub.util.PlaybackStatus;
import com.project.musicianhub.util.Utils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Handles all the services related to music player
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class MediaPlayerService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {

    //thread handler
    final Handler handler = new Handler();

    //instance of media player
    private MediaPlayer mediaPlayer;
    //current playing track
    private Music currentlyPlayingMusic;
    //Music instance
    Music music;
    //used for pausing or resuming of the media player
    private int resumePosition;
    //manages the different events regarding the whole android OS
    private AudioManager audioManager;
    //handles the incoming phone calls
    private boolean onGoingcall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    //sets the broadcast message for various music player action
    public static final String ACTION_PLAY = "com.project.musicianhub.audioplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.project.musicianhub.audioplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.project.musicianhub.audioplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.project.musicianhub.audioplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.project.musicianhub.audioplayer.ACTION_STOP";

    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;

    //initializes the main activity and view from this service
    MainActivity mainActivity;
    ImageView playerStatus;
    TextView artistName;
    TextView titleName;
    ImageView albumArt;
    int musicPosition;
    boolean resetPlayList;
    List<Music> musicList;


    @Override
    public void onCreate() {
        super.onCreate();
        // Perform one-time setup procedures

        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
    }

    private void initMusicPLayer() {
        mediaPlayer = new MediaPlayer();
        //set up mediaplayer event listener
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);

        //reset the media player so that it takes the current broadcasted media player
        mediaPlayer.reset();

        //allows the media player to stream music
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            //sets the url path of the music for media player
            mediaPlayer.setDataSource(music.getMusicPath());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }


    //binder given to the client
    private final IBinder iBinder = new LocalBinder();


    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed.
        stopMusic();


        //stop the service
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation.
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        playMusic();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        //Invoked when the audio focus of the system is updated.
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                //resume playback
                if (mediaPlayer == null) initMusicPLayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                //lost the media audio focust
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                //lost the media audio focus for a short period of time
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                //lost focus but only lower volume due to other activities such as arrival of notification
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    /**
     * requests for the audio focus permission
     *
     * @return audio focus permission
     */
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }
        return false;
    }

    /**
     * removes the audio focus
     *
     * @return audio focus permission
     */
    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //An audio file is passed to the service through putExtra();
            music = (Music) intent.getSerializableExtra("Music");
            resetPlayList = intent.getBooleanExtra("resetPlayList", false);
            if (musicList == null || resetPlayList) {
                musicPosition = intent.getIntExtra("musicPosition", 0);
                musicList = (List<Music>) intent.getSerializableExtra("musicList");
                resetPlayList = false;
            }
            if (music != null)
                currentlyPlayingMusic = music;
        } catch (NullPointerException e) {
            e.printStackTrace();
            stopSelf();
        }

        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf();
        }

        if (music != null) {
            if (music.getMusicPath() != null && music.getMusicPath() != "") {
                stopMusic();
                initMusicPLayer();
            }
        }


        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initMusicPLayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMusic();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);
    }


    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMusic();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    /**
     * Handles the incoming phone calls
     */
    private void callStateListener() {
        //gets the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //starting the phone state changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMusic();
                            onGoingcall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (onGoingcall) {
                                onGoingcall = false;
                                resumeMusic();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * Plays the new music from the broad cast received from the activity
     */
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get the data from the broadcast intent
            String uiUpdate = intent.getStringExtra("actionValue");
            String playingStatus = intent.getStringExtra("fromPlayer");
            resetPlayList = intent.getBooleanExtra("resetPlayList", false);
            //if reset player list is true
            if (resetPlayList) {
                //update the music list in the player
                musicPosition = intent.getIntExtra("musicPosition", 0);
                musicList = (List<Music>) intent.getSerializableExtra("musicList");
                resetPlayList = false;
            }
            //if the music player is updated from the music player
            if (uiUpdate != null && uiUpdate.equalsIgnoreCase("updateMusicPlayerUI")) {
                if (playingStatus.equals("playing")) {
                    buildNotification(PlaybackStatus.PAUSED);
                    pauseMusic();
                } else if (playingStatus.equals("paused")) {
                    buildNotification(PlaybackStatus.PLAYING);
                    //if the music played from the session music
                    if (playingStatus.equals("pausedSession")) {
                        playSessionMusic();
                    } else {
                        playMusic();
                    }
                }
            } else {
                //if music player is played from other than music player
                music = (Music) intent.getSerializableExtra("Music");
                //if the music played is different from the current playing music
                if (music != null && music.getId() != currentlyPlayingMusic.getId()) {
                    //set the music as current playing
                    currentlyPlayingMusic = music;
                    //if music path is available
                    if (music.getMusicPath() != null && music.getMusicPath() != "") {
                        //A PLAY_NEW_AUDIO action received
                        //reset mediaPlayer to play the new Audio

                        musicPosition = intent.getIntExtra("musicPosition", 0);

                        resetPlayList = intent.getBooleanExtra("resetPlayList", false);

                        stopMusic();
                        mediaPlayer.reset();
                        initMusicPLayer();
                        buildNotification(PlaybackStatus.PLAYING);
                    } else {
                        stopSelf();
                    }
                    //if the current music is same as the played music
                } else if (playingStatus.equals("playing")) {
                    buildNotification(PlaybackStatus.PAUSED);
                    pauseMusic();
                } else if (playingStatus.equals("paused")) {
                    buildNotification(PlaybackStatus.PLAYING);
                    buildNotification(PlaybackStatus.PLAYING);
                    if (playingStatus.equals("pausedSession")) {
                        playSessionMusic();
                    } else {
                        playMusic();
                    }
                }
            }
        }
    };

    /**
     * Play music that is currently available in the session
     */
    private void playSessionMusic() {
        if (!mediaPlayer.isPlaying()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(getApplicationContext()).load(android.R.drawable.ic_media_pause).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(playerStatus);
                    mediaPlayer.start();
                }
            });
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(getApplicationContext()).load(android.R.drawable.ic_media_play).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(playerStatus);
                }
            });

        }
    }

    /**
     * Register the new music
     */
    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }


    /**
     * plays the selected music
     */
    private void playMusic() {
        if (!mediaPlayer.isPlaying()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (artistName != null) {
                        artistName.setText(music.getName());
                        titleName.setText(music.getTitle());
                        Glide.with(getApplicationContext()).load(music.getAlbumArtPath()).diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(albumArt);
                        Glide.with(getApplicationContext()).load(android.R.drawable.ic_media_pause).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(playerStatus);
                    }
                    mediaPlayer.start();
                }
            });


        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(getApplicationContext()).load(android.R.drawable.ic_media_play).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(playerStatus);
                }
            });

        }
    }

    /**
     * stops the currently playing music
     */
    private void stopMusic() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(getApplicationContext()).load(android.R.drawable.ic_media_pause).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(playerStatus);
                    buildNotification(PlaybackStatus.PLAYING);
                }
            });
        }
    }

    /**
     * pauses the playing music
     */
    private void pauseMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
            Glide.with(this).load(android.R.drawable.ic_media_play).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(playerStatus);
        }
    }

    /**
     * resumes playing of the music from the paused position
     */
    private void resumeMusic() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
            Glide.with(this).load(android.R.drawable.ic_media_pause).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(playerStatus);
        }
    }

    /**
     * Initialize the media players session
     *
     * @throws RemoteException
     */
    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMusic();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMusic();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                if (music != null) {
                    updateMetaData();
                    buildNotification(PlaybackStatus.PLAYING);
                }
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                if (music != null) {
                    updateMetaData();
                    buildNotification(PlaybackStatus.PLAYING);
                }
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    /**
     * Updates the metadata for the notification bar
     */
    private void updateMetaData() {
        Bitmap albumArt = null;
        try {
            albumArt = new Utils().new Connection().execute(music.getAlbumArtPath()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        // Update the current metadata
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, music.getName())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, music.getTitle())
                .build());
    }

    /**
     * Skips the music to next available music
     */
    private void skipToNext() {
        //music is not null and the current music is not last in the position
        if (musicList != null && (musicList.size() - 1) > musicPosition) {
            musicPosition++;
            final Music music = musicList.get(musicPosition);
            if (music != null)
                this.music = music;


            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!music.getMusicPath().contains("http") && !music.getAlbumArtPath().contains("http")) {
                        String musicPath = music.getMusicPath();
                        String replaceMusicSlash = musicPath.replace("\\", "/");
                        musicPath = "http:" + "//" + replaceMusicSlash;
                        music.setMusicPath(musicPath);

                        String imagePath = music.getAlbumArtPath();
                        String replaceSlash = imagePath.replace("\\", "/");
                        imagePath = "http:" + "//" + replaceSlash;
                        music.setAlbumArtPath(imagePath);
                    }
                    titleName.setText(music.getTitle());
                    artistName.setText(music.getName());
                    Glide.with(getApplicationContext()).load(music.getAlbumArtPath()).diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(albumArt);
                }
            });


            currentlyPlayingMusic = this.music;


            stopMusic();
            //reset mediaPlayer
            mediaPlayer.reset();
            initMusicPLayer();
        }


    }

    /**
     * Skips the music to previous available music
     */
    private void skipToPrevious() {

        //if music is not null and music position is always greater than zero
        if (musicList != null && musicPosition > 0) {
            final Music music = musicList.get(musicPosition - 1);
            musicPosition--;
            if (music != null)
                this.music = music;


            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!music.getMusicPath().contains("http") && !music.getAlbumArtPath().contains("http")) {
                        String musicPath = music.getMusicPath();
                        String replaceMusicSlash = musicPath.replace("\\", "/");
                        musicPath = "http:" + "//" + replaceMusicSlash;
                        music.setMusicPath(musicPath);

                        String imagePath = music.getAlbumArtPath();
                        String replaceSlash = imagePath.replace("\\", "/");
                        imagePath = "http:" + "//" + replaceSlash;
                        music.setAlbumArtPath(imagePath);
                    }
                    titleName.setText(music.getTitle());
                    artistName.setText(music.getName());
                    Glide.with(getApplicationContext()).load(music.getAlbumArtPath()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.drawable.logo).override(70, 70).fitCenter().into(albumArt);
                }
            });


            currentlyPlayingMusic = this.music;

            stopMusic();
            //reset mediaPlayer
            mediaPlayer.reset();
            initMusicPLayer();
        }


    }

    /**
     * Build the notification tray for the music player
     *
     * @param playbackStatus update the play/pause
     */
    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

        if (music != null) {
            createOrUpdateNotification(music, notificationAction, play_pauseAction, notificationBuilder);
        } else {
            createOrUpdateNotification(currentlyPlayingMusic, notificationAction, play_pauseAction, notificationBuilder);
        }

    }

    /**
     * Create and update the notification tray
     *
     * @param music               music object
     * @param notificationAction  get the notification action
     * @param play_pauseAction    play/pause actions
     * @param notificationBuilder notification builder
     */
    private void createOrUpdateNotification(Music music, int notificationAction, PendingIntent play_pauseAction, NotificationCompat.Builder notificationBuilder) {
        Bitmap albumArt = null;
        try {
            albumArt = new Utils().new Connection().execute(music.getAlbumArtPath()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // Create a new Notification
        notificationBuilder
                .setShowWhen(false)
                .setOngoing(false)
                // Set the Notification style
                .setStyle(new NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                        // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(getResources().getColor(R.color.cardview_dark_background))
                // Set the large and small icons
                .setLargeIcon(albumArt)
                .setSmallIcon(R.drawable.logo)
                // Set Notification content information
                .setContentText(music.getName())
                .setContentTitle(music.getTitle())
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * Removes the notification
     */
    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    /**
     * Set the playback actions
     *
     * @param actionNumber action number such as play/pause/next/previous
     * @return pending intent object
     */
    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    /**
     * Handles the playback intent
     *
     * @param playbackAction playback action intent
     */
    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }


    /**
     * Binding class for Media Player Service
     *
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    public ImageView getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(ImageView playerStatus) {
        this.playerStatus = playerStatus;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public TextView getArtistName() {
        return artistName;
    }

    public void setArtistName(TextView artistName) {
        this.artistName = artistName;
    }

    public TextView getTitleName() {
        return titleName;
    }

    public void setTitleName(TextView titleName) {
        this.titleName = titleName;
    }

    public ImageView getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(ImageView albumArt) {
        this.albumArt = albumArt;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}
