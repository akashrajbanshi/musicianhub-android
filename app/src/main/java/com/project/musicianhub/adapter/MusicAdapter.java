package com.project.musicianhub.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.project.musicianhub.CommentActivity;
import com.project.musicianhub.MainActivity;
import com.project.musicianhub.ProfileActivity;
import com.project.musicianhub.R;
import com.project.musicianhub.model.Music;
import com.project.musicianhub.service.MusicServiceImpl;
import com.project.musicianhub.util.SessionManager;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Recycler view adapter class for music
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */


public class MusicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Music> musicList;
    //item view
    private final int VIEW_ITEM = 1;
    //progress bar view
    private final int VIEW_PROG = 0;
    //listens on load more
    private OnLoadMoreListener onLoadMoreListener;
    //linear layout for recylcer view
    private LinearLayoutManager mLinearLayoutManager;

    private boolean isMoreLoading = false;
    private int visibleThreshold = 1;
    int firstVisibleItem, visibleItemCount, totalItemCount;


    public interface OnLoadMoreListener {
        void onLoadMore();
    }


    String imgPath;
    private MusicServiceImpl musicService;


    SessionManager session;

    /**
     * View holder class for follow
     *
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, name, postedOn, like, comment, musicId, likeTextView, commentCountTxtView, userTextView, shareTextView;
        public ImageView thumbnail, overflow, likeImageView, shareImageView;
        public LinearLayout commentLayout;
        CircleImageView circleImageView;

        /**
         * Constructor of MyViewHolder
         *
         * @param view current view
         */
        public MyViewHolder(final View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.home_title);
            name = (TextView) view.findViewById(R.id.home_name);
            postedOn = (TextView) view.findViewById(R.id.home_posted_on);
            thumbnail = (ImageView) view.findViewById(R.id.home_thumbnail);
            overflow = (ImageView) view.findViewById(R.id.home_overflow);
            like = (TextView) view.findViewById(R.id.home_likes_count);
            comment = (TextView) view.findViewById(R.id.home_comments_count);
            musicId = (TextView) view.findViewById(R.id.home_music_id);
            likeImageView = (ImageView) view.findViewById(R.id.home_likeImageView);
            likeTextView = (TextView) view.findViewById(R.id.home_likeTextView);
            commentLayout = (LinearLayout) view.findViewById(R.id.home_comment_linear_layout);
            commentCountTxtView = (TextView) view.findViewById(R.id.home_comments_count);
            circleImageView = (CircleImageView) view.findViewById(R.id.home_user_imageView);
            userTextView = (TextView) view.findViewById(R.id.home_user_textview);
            shareImageView = (ImageView) view.findViewById(R.id.home_shareImageView);
            shareTextView = (TextView) view.findViewById(R.id.home_shareTextView);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar pBar;

        public ProgressViewHolder(View v) {
            super(v);
            pBar = (ProgressBar) v.findViewById(R.id.progressBar);
        }
    }

    /**
     * Constructor for music adapter
     *
     * @param mContext           current context
     * @param musicList          musicList list
     * @param onLoadMoreListener on load more listenter
     */
    public MusicAdapter(Context mContext, List<Music> musicList, OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
        this.mContext = mContext;
        this.musicList = musicList;
    }

    public void setLinearLayoutManager(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public int getItemViewType(int position) {
        return musicList.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    /**
     * Sets the recylcer view items
     *
     * @param mView recycler view
     */
    public void setRecyclerView(RecyclerView mView) {
        mView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = mLinearLayoutManager.getItemCount();
                firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
                if (!isMoreLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                    }
                    isMoreLoading = true;
                }
            }
        });
    }

    /**
     * Creates the view holder for the adapter
     *
     * @param parent   parent view group
     * @param viewType type of a view
     * @return MyViewHolder class
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.music_card_home, parent, false));
        } else {
            return new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_view, parent, false));
        }
    }


    /**
     * Sets load more or not
     *
     * @param isMoreLoading
     */
    public void setMoreLoading(boolean isMoreLoading) {
        this.isMoreLoading = isMoreLoading;
    }

    /**
     * Sets the progress bar to the recycler view
     *
     * @param isProgress sets the boolean value
     */
    public void setProgressMore(final boolean isProgress) {
        if (isProgress) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    musicList.add(null);
                    notifyItemInserted(musicList.size() - 1);
                }
            });
        } else {
            if (musicList.size() != 0) {
                musicList.remove(musicList.size() - 1);
                notifyItemRemoved(musicList.size());
            }
        }
    }

    /**
     * Creates the view items for the adapter
     *
     * @param holder   ViewHolder
     * @param position current position on the recycler view
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof MyViewHolder) {
            musicService = new MusicServiceImpl();
            final Music music = musicList.get(position);

            String imagePath = music.getAlbumArtPath();
            if (imagePath.equalsIgnoreCase("")) {
                Glide.with(mContext).load(R.drawable.logo).placeholder(R.drawable.logo).override(125, 125).fitCenter().into(((MyViewHolder) holder).thumbnail);
            } else if (imagePath.contains("http")) {
                Glide.with(mContext).load(imagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.drawable.logo).override(125, 125).fitCenter().into(((MyViewHolder) holder).thumbnail);
            } else {
                String replaceSlash = imagePath.replace("\\", "/");
                imagePath = "http:" + "//" + replaceSlash;
                music.setAlbumArtPath(imagePath);
                Glide.with(mContext).load(imagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.drawable.logo).override(40, 40).fitCenter().into(((MyViewHolder) holder).thumbnail);
            }

            String musicPath = music.getMusicPath();
            String replaceMusicSlash = musicPath.replace("\\", "/");
            if (!musicPath.contains("http"))
                musicPath = "http:" + "//" + replaceMusicSlash;
            music.setMusicPath(musicPath);

            String userImagePath = music.getUser().getUser_img_path();
            if (userImagePath.equalsIgnoreCase("") || userImagePath.contains("http")) {
                Glide.with(mContext).load(R.drawable.user).placeholder(R.drawable.logo).override(40, 40).fitCenter().into(((MyViewHolder) holder).thumbnail);
            } else if (userImagePath.contains("http")) {
                Glide.with(mContext).load(imagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).override(40, 40).fitCenter().into(((MyViewHolder) holder).thumbnail);
            } else {
                String replacedSlash = userImagePath.replace("\\", "/");
                userImagePath = "http:" + "//" + replacedSlash;
                Glide.with(mContext).load(userImagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).override(40, 40).into(((MyViewHolder) holder).circleImageView);
            }


            ((MyViewHolder) (holder)).userTextView.setText(music.getName());

            ((MyViewHolder) (holder)).userTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int userId = music.getUser().getId();
                    Intent intent = new Intent(mContext, ProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("userId", userId);
                    intent.putExtra("userImage", music.getUser().getUser_img_path());
                    intent.putExtra("username", music.getName());
                    intent.putExtra("from", "home");
                    mContext.startActivity(intent);
                }
            });


            session = new SessionManager(mContext);
            final HashMap<String, Integer> userId = session.getUserDetails();
            HashMap<String, String> userImage = session.getUserPathDetails();
            String imgPath = userImage.get(SessionManager.KEY_USER_IMAGE);
            this.imgPath = imgPath;
            final int sessionUserId = userId.get(SessionManager.KEY_ID);
            if (sessionUserId == Integer.valueOf(music.getUserId()) && music.isLiked()) {
                ((MyViewHolder) holder).likeImageView.setTag(R.drawable.liked);
                ((MyViewHolder) holder).likeImageView.setImageResource(R.drawable.liked);
            } else {
                ((MyViewHolder) holder).likeImageView.setTag(R.drawable.like);
                ((MyViewHolder) holder).likeImageView.setImageResource(R.drawable.like);
            }

            ((MyViewHolder) (holder)).title.setText(music.getTitle());
            ((MyViewHolder) (holder)).name.setText("Artist: " + music.getName());
            ((MyViewHolder) (holder)).postedOn.setText("Posted On: " + music.getPostedOn());
            ((MyViewHolder) (holder)).musicId.setText(String.valueOf(music.getId()));
            ((MyViewHolder) (holder)).like.setText(music.getLike() + " person like(s) your music.");
            ((MyViewHolder) (holder)).comment.setText("View all " + music.getComment() + " comment(s)");


            ((MyViewHolder) (holder)).likeImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = (int) ((MyViewHolder) (holder)).likeImageView.getTag();
                    if (id == R.drawable.like) {
                        ((MyViewHolder) holder).likeImageView.setTag(R.drawable.liked);
                        ((MyViewHolder) holder).likeImageView.setImageResource(R.drawable.liked);
                        music.setLiked(true);
                        music.setUserId(sessionUserId);
                        musicService.createMusicLike(music, ((MyViewHolder) holder), mContext);
                    } else {
                        ((MyViewHolder) holder).likeImageView.setTag(R.drawable.like);
                        ((MyViewHolder) holder).likeImageView.setImageResource(R.drawable.like);
                        music.setLiked(false);
                        music.setUserId(sessionUserId);
                        musicService.createMusicLike(music, ((MyViewHolder) holder), mContext);
                    }
                }
            });
            ((MyViewHolder) holder).likeTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = (int) ((MyViewHolder) holder).likeImageView.getTag();
                    if (id == R.drawable.like) {
                        ((MyViewHolder) holder).likeImageView.setTag(R.drawable.liked);
                        ((MyViewHolder) holder).likeImageView.setImageResource(R.drawable.liked);
                        music.setLiked(true);
                        music.setUserId(sessionUserId);
                        musicService.createMusicLike(music, ((MyViewHolder) holder), mContext);
                    } else {
                        ((MyViewHolder) holder).likeImageView.setTag(R.drawable.like);
                        ((MyViewHolder) holder).likeImageView.setImageResource(R.drawable.like);
                        music.setLiked(false);
                        music.setUserId(sessionUserId);
                        musicService.createMusicLike(music, ((MyViewHolder) holder), mContext);
                    }
                }
            });


            ((MyViewHolder) holder).overflow.setVisibility(View.GONE);

            ((MyViewHolder) holder).overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(((MyViewHolder) holder).overflow, music);
                }
            });

            ((MyViewHolder) holder).commentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCommentActivity(((MyViewHolder) holder).commentLayout, music);
                }
            });

            ((MyViewHolder) holder).commentCountTxtView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCommentActivity(((MyViewHolder) holder).commentLayout, music);
                }
            });
            ((MyViewHolder) holder).thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playMusicForSelectedPost(music, mContext, position, musicList);
                }
            });

            ((MyViewHolder) holder).shareTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareToFacebook(music, mContext);
                }
            });

            ((MyViewHolder) holder).shareImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareToFacebook(music, mContext);
                }
            });
        }
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
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("userId", music.getUserId());
        intent.putExtra("id", music.getId());
        intent.putExtra("musicUserId", music.getUser().getId());
        intent.putExtra("from", "home");
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
        popup.setOnMenuItemClickListener(new MusicAdapter.MyMenuItemClickListener(music));
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
                case R.id.action_add_favourite:
                    Toast.makeText(mContext, "Add to favourite", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_play_next:
                    Toast.makeText(mContext, "Play next", Toast.LENGTH_SHORT).show();
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


