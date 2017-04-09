package com.project.musicianhub.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.project.musicianhub.CommentActivity;
import com.project.musicianhub.EditMusicActivity;
import com.project.musicianhub.MainActivity;
import com.project.musicianhub.ProfileActivity;
import com.project.musicianhub.ProfileFragment;
import com.project.musicianhub.R;
import com.project.musicianhub.model.Music;
import com.project.musicianhub.service.MusicServiceImpl;
import com.project.musicianhub.service.ProfileServiceImpl;
import com.project.musicianhub.util.SessionManager;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

/**
 * Recycler view adapter class for profile music
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class ProfileMusicAdapter extends RecyclerView.Adapter<ProfileMusicAdapter.MyViewHolder> {

    private Context mContext;
    private List<Music> musicList;
    String imgPath;
    private MusicServiceImpl musicService;
    FragmentManager fragmentManager;
    SessionManager session;

    /**
     * View holder class for profile music
     *
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, name, postedOn, like, comment, musicId, likeTextView, commentCountTxtView, shareTextView;
        public ImageView thumbnail, overflow, likeImageView, shareImageView;
        public LinearLayout commentLayout;

        /**
         * Constructor of MyViewHolder
         *
         * @param view current view
         */
        public MyViewHolder(final View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.profile_title);
            name = (TextView) view.findViewById(R.id.profile_name);
            postedOn = (TextView) view.findViewById(R.id.profile_posted_on);
            thumbnail = (ImageView) view.findViewById(R.id.profile_thumbnail);
            overflow = (ImageView) view.findViewById(R.id.profile_overflow);
            like = (TextView) view.findViewById(R.id.profile_likes_count);
            comment = (TextView) view.findViewById(R.id.profile_comments_count);
            musicId = (TextView) view.findViewById(R.id.profile_music_id);
            likeImageView = (ImageView) view.findViewById(R.id.profile_likeImageView);
            likeTextView = (TextView) view.findViewById(R.id.profile_likeTextView);
            commentLayout = (LinearLayout) view.findViewById(R.id.profile_comment_linear_layout);
            commentCountTxtView = (TextView) view.findViewById(R.id.profile_comments_count);
            shareImageView = (ImageView) view.findViewById(R.id.profile_shareImageView);
            shareTextView = (TextView) view.findViewById(R.id.profile_shareTextView);
        }
    }

    /**
     * Constructor for music adapter
     *
     * @param mContext        current context
     * @param musicList       music list
     * @param fragmentManager current fragment manager
     */
    public ProfileMusicAdapter(Context mContext, List<Music> musicList, FragmentManager fragmentManager) {
        this.mContext = mContext;
        this.musicList = musicList;
        this.fragmentManager = fragmentManager;

    }

    /**
     * Creates the view holder for the adapter
     *
     * @param parent   parent view group
     * @param viewType type of a view
     * @return MyViewHolder class
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_card_profile, parent, false);

        return new MyViewHolder(itemView);
    }

    /**
     * Creates the view items for the adapter
     *
     * @param holder   ViewHolder
     * @param position current position on the recycler view
     */
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        musicService = new MusicServiceImpl();
        final Music music = musicList.get(position);

        session = new SessionManager(mContext);
        HashMap<String, Integer> userId = session.getUserDetails();
        HashMap<String, String> userImage = session.getUserPathDetails();
        String imgPath = userImage.get(SessionManager.KEY_USER_IMAGE);
        this.imgPath = imgPath;
        final int sessionUserId = userId.get(SessionManager.KEY_ID);
        if (sessionUserId == Integer.valueOf(music.getUserId()) && music.isLiked()) {
            holder.likeImageView.setTag(R.drawable.liked);
            holder.likeImageView.setImageResource(R.drawable.liked);
        } else {
            holder.likeImageView.setTag(R.drawable.like);
            holder.likeImageView.setImageResource(R.drawable.like);
        }

        if (sessionUserId != music.getUser().getId()) {
            holder.overflow.setVisibility(View.GONE);
        }

        holder.title.setText(music.getTitle());
        holder.name.setText("Artist: " + music.getName());
        holder.postedOn.setText("Posted On: " + music.getPostedOn());
        holder.musicId.setText(String.valueOf(music.getId()));
        holder.like.setText(music.getLike() + " person like(s) your music.");
        holder.comment.setText("View all " + music.getComment() + " comment(s)");


        String imagePath = music.getAlbumArtPath();
        if (imagePath.equalsIgnoreCase("")) {
            Glide.with(mContext).load(R.drawable.logo).placeholder(R.drawable.logo).override(125, 125).fitCenter().into(holder.thumbnail);
        } else {
            String replaceSlash = imagePath.replace("\\", "/");
            imagePath = "http:" + "//" + replaceSlash;
            music.setAlbumArtPath(imagePath);
            String musicPath = music.getMusicPath();
            String replaceMusicSlash = musicPath.replace("\\", "/");
            musicPath = "http:" + "//" + replaceMusicSlash;
            music.setMusicPath(musicPath);

            Glide.with(mContext).load(imagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.drawable.logo).override(125, 125).fitCenter().into(holder.thumbnail);
        }

        holder.likeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = (int) holder.likeImageView.getTag();
                if (id == R.drawable.like) {
                    holder.likeImageView.setTag(R.drawable.liked);
                    holder.likeImageView.setImageResource(R.drawable.liked);
                    music.setLiked(true);
                    music.setUserId(sessionUserId);
                    musicService.createMusicLike(music, holder, mContext);
                } else {
                    holder.likeImageView.setTag(R.drawable.like);
                    holder.likeImageView.setImageResource(R.drawable.like);
                    music.setLiked(false);
                    music.setUserId(sessionUserId);
                    musicService.createMusicLike(music, holder, mContext);
                }
            }
        });
        holder.likeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = (int) holder.likeImageView.getTag();
                if (id == R.drawable.like) {
                    holder.likeImageView.setTag(R.drawable.liked);
                    holder.likeImageView.setImageResource(R.drawable.liked);
                    music.setLiked(true);
                    music.setUserId(sessionUserId);
                    musicService.createMusicLike(music, holder, mContext);
                } else {
                    holder.likeImageView.setTag(R.drawable.like);
                    holder.likeImageView.setImageResource(R.drawable.like);
                    music.setLiked(false);
                    music.setUserId(sessionUserId);
                    musicService.createMusicLike(music, holder, mContext);
                }
            }
        });


        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow, music);
            }
        });

        holder.commentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentActivity(holder.commentLayout, music);
            }
        });

        holder.commentCountTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentActivity(holder.commentLayout, music);
            }
        });
        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusicForSelectedPost(music, mContext, position, musicList);
            }
        });

        holder.shareTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToFacebook(music, mContext);
            }
        });

        holder.shareImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToFacebook(music, mContext);
            }
        });
    }

    /**
     * Plays the music for the selected post
     *
     * @param music     music object
     * @param context   current context
     * @param position  current position in the list
     * @param musicList music list
     */
    private void playMusicForSelectedPost(Music music, Context context, int position, List<Music> musicList) {

        if (context instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) context;
            mainActivity.playAudio(music, position, musicList);
        } else if (context instanceof ProfileActivity) {
            ProfileActivity profileActivity = (ProfileActivity) context;
            profileActivity.playAudio(music, position, musicList);
        }

    }

    /**
     * Shares the post to the facebook
     *
     * @param music   music object
     * @param context current context
     */
    private void shareToFacebook(Music music, Context context) {

        if (context instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) context;
            mainActivity.shareToFacebook(music, context);
        } else if (context instanceof ProfileActivity) {
            ProfileActivity profileActivity = (ProfileActivity) context;
            profileActivity.shareToFacebook(music, context);
        }

    }

    /**
     * Shows the comment of the current music post
     *
     * @param view  current view
     * @param music music object
     */
    private void showCommentActivity(View view, Music music) {
        Intent intent = new Intent(mContext, CommentActivity.class);
        intent.putExtra("userId", music.getUserId());
        intent.putExtra("id", music.getId());
        intent.putExtra("musicUserId", music.getUser().getId());
        intent.putExtra("from", music.getFrom());
        mContext.startActivity(intent);
    }

    /**
     * Shows the pop up menu for editing and deleting the music post
     *
     * @param view  current view
     * @param music music object
     */
    private void showPopupMenu(View view, Music music) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.profile_menu_music, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(music));
        popup.show();
    }

    /**
     * Pop up menu click listener class
     *
     * @author Akash Rajbanshi
     * @since 1.0
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        private Music music;

        public MyMenuItemClickListener(Music music) {
            this.music = music;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_edit_music:
                    Intent intent = new Intent(mContext, EditMusicActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("userId", music.getUserId());
                    intent.putExtra("id", music.getId());
                    intent.putExtra("title", music.getTitle());
                    intent.putExtra("genre", music.getGenre());
                    intent.putExtra("imagePath", music.getAlbumArtPath());
                    intent.putExtra("musicPath", music.getMusicPath());
                    intent.putExtra("from", "profileMusic");
                    mContext.startActivity(intent);
                    return true;
                case R.id.action_delete_music:
                    musicService = new MusicServiceImpl();
                    musicService.deleteMusic(music.getId(), mContext, fragmentManager, ProfileMusicAdapter.this);
                    return true;
                default:
            }
            return false;
        }
    }

    /**
     * Gets the item count of the recycler view
     *
     * @return item count of the recycler view
     */
    @Override
    public int getItemCount() {
        return musicList.size();
    }
}


