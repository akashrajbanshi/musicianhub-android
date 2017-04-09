package com.project.musicianhub;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.project.musicianhub.adapter.CommentAdapter;
import com.project.musicianhub.adapter.SearchAdapter;
import com.project.musicianhub.model.Comment;
import com.project.musicianhub.model.Search;
import com.project.musicianhub.service.CommentServiceImpl;
import com.project.musicianhub.util.SessionManager;
import com.project.musicianhub.util.ValidationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Comment Activity class
 * Controls the commenting in the user's music post
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class CommentActivity extends AppCompatActivity {
    //activity's toolbar element
    Toolbar toolbar;
    //session element
    SessionManager session;
    //recylerview for the comment activity
    RecyclerView recyclerView;
    //comment adapter for the recycler view
    CommentAdapter commentAdapter;
    //comment list
    private List<Comment> commentList;
    //comment selectMusicBtn
    Button commentButton;
    //edit text for commenting
    EditText commentEditText;
    //comment service class
    CommentServiceImpl commentService;

    //from main or post activity
    String from;

    //comment TextView
    TextView emptyComment;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        emptyComment = (TextView) findViewById(R.id.empty_comment);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        //UI initialization of the comment activity
        toolbar = (Toolbar) findViewById(R.id.comment_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        commentButton = (Button) findViewById(R.id.commentButton);
        commentEditText = (EditText) findViewById(R.id.commentEditText);


        //gets the intent's extras
        Intent intent = getIntent();
        final int musicId = intent.getIntExtra("id", 0);
        final int musicUserId = intent.getIntExtra("musicUserId", 0);
        final String from = intent.getStringExtra("from");
        this.from = from;

        session = new SessionManager(this);

        //gets the user id from the session
        HashMap<String, Integer> userId = session.getUserDetails();
        final int id = userId.get(SessionManager.KEY_ID);

        //creates the comment for the post
        commentService = new CommentServiceImpl();
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!ValidationUtil.validateEmptyComments(commentEditText)) {
                    progressBar.setVisibility(View.VISIBLE);
                    commentList.clear();
                    commentService.createComment(musicId, id, musicUserId, commentEditText.getText().toString(), commentList, commentAdapter, CommentActivity.this.getApplicationContext(), progressBar, emptyComment, recyclerView);
                }
                commentEditText.setText("");

            }
        });

        //initializes the recyclerview for the comment activity
        recyclerView = (RecyclerView) findViewById(R.id.comment_recycler_view);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(commentAdapter);

        //gets all the comment for the related music post
        commentList.clear();
        progressBar.setVisibility(View.VISIBLE);
        commentService.getAllComments(musicId, commentList, commentAdapter, this.getApplicationContext(), progressBar, emptyComment, recyclerView);


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
                if (from.equals("home") || from.equals("profileMusic")) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            default:
                break;
        }
        return true;
    }
}
