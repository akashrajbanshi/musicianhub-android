package com.project.musicianhub;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.project.musicianhub.adapter.MusicAdapter;
import com.project.musicianhub.model.Music;
import com.project.musicianhub.service.MusicServiceImpl;
import com.project.musicianhub.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Home Fragment which controls the home which is included inside the main activity
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class HomeFragment extends Fragment implements MusicAdapter.OnLoadMoreListener {
    //session
    SessionManager session;
    //recyclerView inside home fragment
    RecyclerView recyclerView;
    //floating action button of the home view
    FloatingActionButton floatingActionButton;
    //current music adapter
    private MusicAdapter musicAdapter;
    //music list
    private List<Music> musicList;
    //user id
    int id;
    //user image path
    String imagePath;
    //service for music
    MusicServiceImpl musicService;
    //swipe refresh layout
    SwipeRefreshLayout swipeRefreshLayout;
    //empty home text view
    TextView emptyHome;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        musicService = new MusicServiceImpl();

        //initializes the session and gets the session data
        session = new SessionManager(getActivity().getApplicationContext());
        HashMap<String, Integer> userId = session.getUserDetails();
        HashMap<String, String> userImage = session.getUserPathDetails();
        HashMap<String, String> username = session.getUsernameDetails();
        final int id = userId.get(SessionManager.KEY_ID);
        final String imagePath = userImage.get(SessionManager.KEY_USER_IMAGE);
        final String userName = username.get(SessionManager.KEY_USERNAME);
        this.id = id;
        this.imagePath = imagePath;

        //UI initialization
        emptyHome = (TextView) view.findViewById(R.id.empty_home);
        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), AddMusicActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("userId", id);
                view.getContext().startActivity(intent);
            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        musicList = new ArrayList<>();
        musicAdapter = new MusicAdapter(view.getContext(), musicList, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getContext());
        musicAdapter.setLinearLayoutManager(mLayoutManager);
        musicAdapter.setRecyclerView(recyclerView);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(musicAdapter);

        //gets the session data
        SessionManager session = new SessionManager(view.getContext());
        final HashMap<String, Integer> sessionUserId = session.getUserDetails();
        final int sesUserId = sessionUserId.get(SessionManager.KEY_ID);
        this.id = sesUserId;

        //swipe refresh layout initialization
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                musicList.clear();
                recyclerView.getRecycledViewPool().clear();
                recyclerView.stopScroll();
                musicService.loadMusicInfoForHome(sesUserId, musicList, musicAdapter, view.getContext(), emptyHome, recyclerView);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //on scroll listener to show/hide floating action button
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && floatingActionButton.isShown()) {
                    floatingActionButton.hide();
                } else if (dy < 0 || !floatingActionButton.isShown()) {
                    floatingActionButton.show();
                }
            }


        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    /**
     * Triggers the loading of data according to need in the recycler view
     */
    @Override
    public void onLoadMore() {
        musicAdapter.setProgressMore(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                musicAdapter.setProgressMore(false);
                loadData();
            }
        }, 2000);
    }

    /**
     * Loads music for the recylcer view
     */
    private void loadData() {
        recyclerView.getRecycledViewPool().clear();
        recyclerView.stopScroll();
        musicService.loadMusicInfoForHome(id, musicList, musicAdapter, this.getContext(), emptyHome, recyclerView);
    }


}
