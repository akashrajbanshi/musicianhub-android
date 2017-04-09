package com.project.musicianhub.service;

import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

/**
 * Follow Service Interface
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public interface IFollowService {
    public void createFollowUser(int id, int userIdFromPost, Button followButton, final Context context,final TextView followersTextView);
}
