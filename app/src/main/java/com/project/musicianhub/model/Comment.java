package com.project.musicianhub.model;

/**
 * Model class for the comment
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class Comment {

    private int id;
    private String comment;
    private String username;
    private String imagePath;
    private int userId;


    public Comment() {
    }

    public Comment(int id, String comment, String username, String imagePath, int userId) {
        this.id = id;
        this.comment = comment;
        this.username = username;
        this.imagePath = imagePath;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
