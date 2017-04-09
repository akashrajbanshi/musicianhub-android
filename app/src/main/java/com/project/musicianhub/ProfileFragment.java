package com.project.musicianhub;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.project.musicianhub.adapter.ProfileMusicAdapter;
import com.project.musicianhub.model.Music;
import com.project.musicianhub.service.ProfileServiceImpl;
import com.project.musicianhub.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Profile Fragment which controls the profile which is included inside the main activity
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class ProfileFragment extends Fragment {

    //session
    SessionManager session;
    //profile image view
    CircleImageView imageView;
    //profile recyclerview
    RecyclerView recyclerView;
    //floating action button
    FloatingActionButton floatingActionButton;
    //profile music adapter
    private ProfileMusicAdapter musicAdapter;
    //music list
    private List<Music> musicList;
    //swipe refresh layout
    SwipeRefreshLayout swipeRefreshLayout;
    //text view components of the profile fragment
    TextView editProfileTxtView, followerTxtView, followingTxtView, numberOfPostTxtView, numberOfFollowerTxtView, numberOfFollowingTxtView, profileTextView;
    //linear layout components of the profile fragment
    LinearLayout followersLinearLayout, followingLinearLayout;
    //follow button
    Button followButton;
    //user id
    int id;
    //user image path
    String imagePath;
    //profile service
    ProfileServiceImpl profileService;
    //progress bar
    ProgressBar progressBar;
    //empty profile
    TextView emptyProfile;


    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Check if the permission is need to be asked if the build version is below lollipop
     *
     * @return yes or no
     */
    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    /**
     * Asks the permission of the user
     */
    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*if (shouldAskPermissions()) {
            askPermissions();
        }*/
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        emptyProfile = (TextView) view.findViewById(R.id.empty_profile);


        progressBar = new ProgressBar(view.getContext(), null, android.R.attr.progressBarStyleSmall);

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


        //UI components initialization
        profileTextView = (TextView) view.findViewById(R.id.profile_name);
        profileTextView.setText(userName);
        numberOfPostTxtView = (TextView) view.findViewById(R.id.post_number);
        numberOfFollowerTxtView = (TextView) view.findViewById(R.id.followers_number);
        numberOfFollowingTxtView = (TextView) view.findViewById(R.id.following_number);
        followButton = (Button) view.findViewById(R.id.follow_user_button);
        followButton.setVisibility(View.GONE);
        editProfileTxtView = (TextView) view.findViewById(R.id.profile_edit_text_view);
        editProfileTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), EditProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("id", id);
                intent.putExtra("imagePath", imagePath);
                startActivity(intent);
            }
        });
        imageView = (CircleImageView) view.findViewById(R.id.profile_user_image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), EditProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("id", id);
                intent.putExtra("imagePath", imagePath);
                startActivity(intent);
            }
        });
        followerTxtView = (TextView) view.findViewById(R.id.followers_number_title);
        followerTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), FollowActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("id", id);
                intent.putExtra("from", "profileTabfollower");
                startActivity(intent);
            }
        });
        followersLinearLayout = (LinearLayout) view.findViewById(R.id.followers_linear_layout);
        followersLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), FollowActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("id", id);
                intent.putExtra("from", "profileTabfollower");
                startActivity(intent);
            }
        });
        followingTxtView = (TextView) view.findViewById(R.id.following_number_title);
        followingTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), FollowActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("id", id);
                intent.putExtra("from", "profileTabfollowing");
                startActivity(intent);
            }
        });
        followingLinearLayout = (LinearLayout) view.findViewById(R.id.following_linear_layout);
        followingLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), FollowActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("id", id);
                intent.putExtra("from", "profileTabfollowing");
                startActivity(intent);
            }
        });

        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.profile_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), AddMusicActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("userId", id);
                startActivity(intent);
            }
        });

        //recylerview initialization
        recyclerView = (RecyclerView) view.findViewById(R.id.profile_recycler_view);
        musicList = new ArrayList<>();
        musicAdapter = new ProfileMusicAdapter(getContext(), musicList, getFragmentManager());
        final GridLayoutManager mLayoutManager = new GridLayoutManager(view.getContext(), 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(musicAdapter);

        //swipe refresh layout initialization
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeProfileRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                musicList.clear();
                recyclerView.getRecycledViewPool().clear();
                recyclerView.stopScroll();
                loadUserInfo(id, imagePath);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        //recylcer view initialization
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


        //load user information
        loadUserInfo(id, imagePath);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo(id, imagePath);
    }

    /**
     * loads the user information either from the session's user id or from the music post
     *
     * @param id
     * @param imagepath
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void loadUserInfo(int id, String imagepath) {
        progressBar.setVisibility(View.VISIBLE);
        profileService = new ProfileServiceImpl();
        List<TextView> textViewList = new ArrayList<>();
        textViewList.add(numberOfPostTxtView);
        textViewList.add(numberOfFollowerTxtView);
        textViewList.add(numberOfFollowingTxtView);
        recyclerView.getRecycledViewPool().clear();
        recyclerView.stopScroll();
        String from = "profileMusic";
        profileService.getUserInfo(id, imageView, imagepath, this.getContext(), musicList, musicAdapter, textViewList, progressBar, from, emptyProfile, recyclerView);
    }


}
