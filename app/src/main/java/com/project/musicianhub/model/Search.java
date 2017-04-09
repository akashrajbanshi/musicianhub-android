package com.project.musicianhub.model;

/**
 * Model class for the search
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class Search {


    private String title;
    private String name;
    private String imagePath;
    private int musicId;
    private int userId;

    public Search() {
    }

    public Search(String title, String name, String imagePath, int musicId) {
        this.title = title;
        this.name = name;
        this.imagePath = imagePath;
        this.musicId = musicId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getMusicId() {
        return musicId;
    }

    public void setMusicId(int musicId) {
        this.musicId = musicId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
