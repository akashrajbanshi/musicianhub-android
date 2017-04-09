package com.project.musicianhub.model;

/**
 * Model class for the follow
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */
public class Follow {

    private int userId;
    private String name;
    private String imagePath;

    public Follow() {
    }

    public Follow(String name, String imagePath, int userId) {
        this.name = name;
        this.imagePath = imagePath;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
