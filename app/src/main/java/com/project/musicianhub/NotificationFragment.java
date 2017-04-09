package com.project.musicianhub;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.project.musicianhub.adapter.NotificationAdapter;
import com.project.musicianhub.model.Notification;
import com.project.musicianhub.model.Search;
import com.project.musicianhub.util.NotificationCustomTouchListener;
import com.project.musicianhub.util.SearchCustomTouchListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Notification Fragment which controls the notification which is included inside the main activity
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class NotificationFragment extends Fragment {

    //recylcer view
    RecyclerView recyclerView;
    //adpater for notification
    NotificationAdapter notificationAdapter;
    //notification list
    private List<Notification> notifications;
    //user id
    int id;
    //current view
    View view;
    //empty notification text view
    TextView emptyNotification;


    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        emptyNotification = (TextView) view.findViewById(R.id.empty_notification);
        this.view = view;
        refreshRecyclerViewer(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshRecyclerViewer(view);
    }

    /**
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            refreshRecyclerViewer(view);
        }
    }

    /**
     * Refresh recylcer view according to the notification received
     *
     * @param view current view
     */
    private void refreshRecyclerViewer(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.notification_recycler_view);

        final List<Notification> notificationList = this.getArguments().getParcelableArrayList("notificationList");
        if (notificationList != null) {
            notifications = notificationList;

            notificationAdapter = new NotificationAdapter(view.getContext(), notifications);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            recyclerView.setAdapter(notificationAdapter);

            notificationAdapter.notifyDataSetChanged();
            if (notificationList.size() > 0) {
                emptyNotification.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                emptyNotification.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }

        //sets the custom on item touch listener on recycler view
        recyclerView.addOnItemTouchListener(new NotificationCustomTouchListener(view.getContext(), recyclerView, new NotificationFragment.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Notification notification = notifications.get(position);

                if (notification.getType().equalsIgnoreCase("like")) {
                    Intent resultIntent = new Intent(view.getContext(), MusicPostActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    resultIntent.putExtra("musicId", notification.getMusicId());
                    view.getContext().startActivity(resultIntent);

                } else if (notification.getType().equalsIgnoreCase("comment")) {

                    Intent resultIntent = new Intent(view.getContext(), MusicPostActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    resultIntent.putExtra("musicId", notification.getMusicId());
                    view.getContext().startActivity(resultIntent);
                } else {
                    int userId = notification.getUserId();
                    Intent intent = new Intent(view.getContext(), ProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("userId", userId);
                    intent.putExtra("from", "notification");
                    intent.putExtra("username", notification.getName());
                    intent.putExtra("userImage", notification.getImagePath());
                    view.getContext().startActivity(intent);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    /**
     * Interface for the reclycler view touch events
     *
     * @author Akash Rajbashi
     * @since 1.0
     */
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }
}
