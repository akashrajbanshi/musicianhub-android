package com.project.musicianhub.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.project.musicianhub.R;
import com.project.musicianhub.model.Search;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Recycler view adapter class for search
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {

    private Context mContext;
    private List<Search> searchList;

    /**
     * View holder class for search
     *
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, name, musicId;
        public CircleImageView thumbnail;

        /**
         * Constructor of MyViewHolder
         *
         * @param view current view
         */
        public MyViewHolder(View view) {
            super(view);
            musicId = (TextView) view.findViewById(R.id.search_music_id);
            title = (TextView) view.findViewById(R.id.search_title);
            name = (TextView) view.findViewById(R.id.search_name);
            thumbnail = (CircleImageView) view.findViewById(R.id.search_album_image);

        }
    }

    /**
     * Constructor for search adapter
     *
     * @param mContext   current context
     * @param searchList search list
     */
    public SearchAdapter(Context mContext, List<Search> searchList) {
        this.mContext = mContext;
        this.searchList = searchList;
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
                .inflate(R.layout.search_list_view, parent, false);

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
        Search search = searchList.get(position);
        holder.title.setText(search.getTitle());
        holder.name.setText(search.getName());
        holder.musicId.setText(String.valueOf(search.getMusicId()));

        String imagePath = search.getImagePath();

        if (imagePath.equalsIgnoreCase("")) {
            Glide.with(mContext).load(R.drawable.user).placeholder(R.drawable.logo).override(40, 40).fitCenter().into(holder.thumbnail);
        } else {
            String replaceSlash = imagePath.replace("\\", "/");
            imagePath = "http:" + "//" + replaceSlash;
            Glide.with(mContext).load(imagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).override(40, 40).into(holder.thumbnail);
        }


    }

    /**
     * Gets the item count of the recycler view
     *
     * @return item count of the recycler view
     */
    @Override
    public int getItemCount() {
        return searchList.size();
    }


}


