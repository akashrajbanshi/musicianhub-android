package com.project.musicianhub.model;

import java.io.Serializable;

/**
 * Model class for the music
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class Music implements Serializable {
    private int id;
    private String title;
    private String genre;
    private String name;
    private String albumArtPath;
    private String musicPath;
    private int thumbnail;
    private String postedOn;
    private int userId;
    private boolean liked;
    private String from;

    private User user = new User();


    private int like;
    private int comment;

    public Music() {
    }


    public Music(int id, String title, String genre, String name, String albumArtPath, String musicPath, int thumbnail, String postedOn, int like, int comment, int userId) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.name = name;
        this.albumArtPath = albumArtPath;
        this.musicPath = musicPath;
        this.thumbnail = thumbnail;
        this.postedOn = postedOn;
        this.like = like;
        this.comment = comment;
        this.userId = userId;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getComment() {
        return comment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getPostedOn() {
        return postedOn;
    }

    public void setPostedOn(String postedOn) {
        this.postedOn = postedOn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getAlbumArtPath() {
        return albumArtPath;
    }

    public void setAlbumArtPath(String albumArtPath) {
        this.albumArtPath = albumArtPath;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}

