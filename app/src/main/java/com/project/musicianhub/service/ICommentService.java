package com.project.musicianhub.service;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.project.musicianhub.adapter.CommentAdapter;
import com.project.musicianhub.model.Comment;

import java.util.List;

/**
 * Comment Service interface
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public interface ICommentService {
    /**
     * Creates the comment for the music post
     *
     * @param musicId
     * @param userId
     * @param musicUserId
     * @param comment
     * @param commentList
     * @param commentAdapter
     * @param applicationContext
     * @param emptyComment
     *@param recyclerView @author Akash Rajbanhi
     * @since 1.0
     */
    public void createComment(int musicId, int userId, int musicUserId, String comment, List<Comment> commentList, CommentAdapter commentAdapter, Context applicationContext, ProgressBar progressBar, TextView emptyComment, RecyclerView recyclerView);

    /**
     * Gets all the comments for the music post
     *
     * @param musicId
     * @param commentList
     * @param commentAdapter
     * @param applicationContext
     * @param emptyComment
     * @param recyclerView
     * @author Akash Rajbanshi
     * @since 1.0
     */
    public void getAllComments(int musicId, List<Comment> commentList, CommentAdapter commentAdapter, Context applicationContext, ProgressBar progressBar, TextView emptyComment, RecyclerView recyclerView);
}
