package com.project.musicianhub;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.project.musicianhub.adapter.FollowAdapter;
import com.project.musicianhub.model.Follow;
import com.project.musicianhub.service.FollowServiceImpl;
import com.project.musicianhub.util.FollowRecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Follow Activity class
 * Controls the follow options provided in the application
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class FollowActivity extends AppCompatActivity {
    //toolbar for the follow activity
    Toolbar toolbar;
    //recyclerview for follow activity
    RecyclerView recyclerView;
    //adapter for follow
    FollowAdapter followAdapter;
    //follow list
    private List<Follow> followList;
    //service for follow
    FollowServiceImpl followService;
    //text view of follow activity
    TextView toolbarText, emptyFollower, emptyFollowing;
    //from main or post activity
    String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        followService = new FollowServiceImpl();

        //UI Initialization

        emptyFollower = (TextView) findViewById(R.id.empty_follower);
        emptyFollowing = (TextView) findViewById(R.id.empty_following);


        toolbar = (Toolbar) findViewById(R.id.follow_user_toolbar);
        toolbarText = (TextView) toolbar.findViewById(R.id.follow_toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.follow_recycler_view);
        followList = new ArrayList<>();
        followAdapter = new FollowAdapter(this, followList);


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(followAdapter);
        getFollow();

        recyclerView.addOnItemTouchListener(new FollowRecyclerTouchListener(this, recyclerView, new FollowActivity.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Follow follow = followList.get(position);
                int userId = follow.getUserId();
                Intent intent = new Intent(view.getContext(), ProfileActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("from", "follow");
                intent.putExtra("username", follow.getName());
                intent.putExtra("userImage", follow.getImagePath());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


    }

    /**
     * Gets the follow data based on the following or followers activity
     */
    private void getFollow() {
        //gets the intent's extras
        Intent intent = getIntent();
        final int userId = intent.getIntExtra("id", 0);
        final String from = intent.getStringExtra("from");
        this.from = from;


        if (intent.getStringExtra("from").equalsIgnoreCase("follower") || intent.getStringExtra("from").equalsIgnoreCase("profileTabfollower")) {
            toolbarText.setText("Followers");
            followService.getAllFollowers(userId, followList, followAdapter, this.getApplicationContext(), emptyFollower, recyclerView);
        } else if (intent.getStringExtra("from").equalsIgnoreCase("following") || intent.getStringExtra("from").equalsIgnoreCase("profileTabfollowing")) {
            toolbarText.setText("Following");
            followService.getAllFollowing(userId, followList, followAdapter, this.getApplicationContext(), emptyFollowing, recyclerView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFollow();
    }

    /**
     * Based on the context the back button triggers the activity
     *
     * @param item current clicked menu item
     * @return true or false if the back button is successfully trigged
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (from.equals("follower") || from.equals("following"))
                    NavUtils.navigateUpFromSameTask(this);
                else {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                return true;
            default:
                break;
        }
        return true;
    }

    /**
     * Interface defining the click listener for recycler view
     */
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

}
