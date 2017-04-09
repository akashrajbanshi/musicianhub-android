package com.project.musicianhub.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.project.musicianhub.MusicPostActivity;
import com.project.musicianhub.ProfileActivity;
import com.project.musicianhub.R;
import com.project.musicianhub.model.Notification;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Recycler view adapter class for notification
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {

    private Context mContext;
    private List<Notification> notifications;

    /**
     * View holder class for notification
     *
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView notification;
        public CircleImageView thumbnail;

        /**
         * Constructor of MyViewHolder
         *
         * @param view current view
         */
        public MyViewHolder(View view) {
            super(view);
            notification = (TextView) view.findViewById(R.id.notification_textView);
            thumbnail = (CircleImageView) view.findViewById(R.id.notification_user_image);

        }
    }

    /**
     * Constructor for notification adapter
     *
     * @param mContext         current context
     * @param notificationList notification list
     */
    public NotificationAdapter(Context mContext, List<Notification> notificationList) {
        this.mContext = mContext;
        this.notifications = notificationList;
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
                .inflate(R.layout.notification_list_view, parent, false);

        return new MyViewHolder(itemView);
    }

    /**
     * Creates the view items for the adapter
     *
     * @param holder   ViewHolder
     * @param position current position on the recycler view
     */
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Notification notification = notifications.get(position);

        if (notification.getType().equalsIgnoreCase("like")) {
            holder.notification.setText(Html.fromHtml("<b>" + notification.getName() + "</b>" + " liked your music " + "<b>" + notification.getMusic() + ".</b>"));
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent resultIntent = new Intent(mContext, MusicPostActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    resultIntent.putExtra("musicId", notification.getMusicId());
                    mContext.startActivity(resultIntent);
                }
            });
        } else if (notification.getType().equalsIgnoreCase("comment")) {
            holder.notification.setText(Html.fromHtml("<b>" + notification.getName() + "</b>" + " commented your music " + "<b>" + notification.getMusic() + "</b>."));
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent resultIntent = new Intent(mContext, MusicPostActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    resultIntent.putExtra("musicId", notification.getMusicId());
                    mContext.startActivity(resultIntent);
                }
            });
        } else {
            holder.notification.setText(Html.fromHtml("<b>" + notification.getName() + "</b>" + " started following you!"));
            holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int userId = notification.getUserId();
                    Intent intent = new Intent(mContext, ProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("userId", userId);
                    intent.putExtra("from", "notification");
                    intent.putExtra("username", notification.getName());
                    intent.putExtra("userImage", notification.getImagePath());
                    mContext.startActivity(intent);
                }
            });
        }


        if (notification.getImagePath().equals("")) {
            Glide.with(mContext).load(R.drawable.user).placeholder(R.drawable.logo).override(125, 125).into(holder.thumbnail);
        } else {
            Glide.with(mContext).load(notification.getImagePath()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.drawable.logo).override(125, 125).into(holder.thumbnail);
        }


    }

    /**
     * Gets the item count of the recycler view
     *
     * @return item count of the recycler view
     */
    @Override
    public int getItemCount() {
        return notifications.size();
    }


}


