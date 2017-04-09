package com.project.musicianhub.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.project.musicianhub.ProfileActivity;
import com.project.musicianhub.R;
import com.project.musicianhub.model.Follow;
import com.project.musicianhub.model.Search;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Recycler view adapter class for follow
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class FollowAdapter extends RecyclerView.Adapter<FollowAdapter.MyViewHolder> {

    private Context mContext;
    private List<Follow> followList;

    /**
     * View holder class for follow
     *
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public CircleImageView thumbnail;

        /**
         * Constructor of MyViewHolder
         *
         * @param view current view
         */
        public MyViewHolder(View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.follow_name);
            thumbnail = (CircleImageView) view.findViewById(R.id.follow_user_image);

        }
    }

    /**
     * Constructor for follow adapter
     *
     * @param mContext    current context
     * @param followList follow list
     */
    public FollowAdapter(Context mContext, List<Follow> followList) {
        this.mContext = mContext;
        this.followList = followList;
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
                .inflate(R.layout.follow_list_view, parent, false);

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
        final Follow follow = followList.get(position);
        holder.name.setText(follow.getName());


        String imagePath = follow.getImagePath();
        if (imagePath.equalsIgnoreCase("")) {
            Glide.with(mContext).load(R.drawable.user).placeholder(R.drawable.logo).override(40, 40).into(holder.thumbnail);
        } else {
            String replaceSlash = imagePath.replace("\\", "/");
            imagePath = "http:" + "//" + replaceSlash;
            Glide.with(mContext).load(imagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).override(40, 40).into(holder.thumbnail);
        }
        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userId = follow.getUserId();
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("from", "follow");
                intent.putExtra("username", follow.getName());
                intent.putExtra("userImage", follow.getImagePath());
                mContext.startActivity(intent);
            }
        });


    }
    /**
     * Gets the item count of the recycler view
     *
     * @return item count of the recycler view
     */
    @Override
    public int getItemCount() {
        return followList.size();
    }


}


