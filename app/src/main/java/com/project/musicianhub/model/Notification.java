package com.project.musicianhub.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class for the notification
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class Notification implements Parcelable {

    private String name;
    private String music;
    private String imagePath;
    private int musicId;
    private String type;
    private int userId;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Notification) {
            Notification temp = (Notification) obj;
            if (this.name == temp.name && this.music == temp.music && this.imagePath == temp.imagePath && this.musicId == temp.musicId && this.type == temp.type)
                return true;
        }
        return false;

    }

    @Override
    public int hashCode() {
        return (this.name.hashCode() + this.music.hashCode() + this.imagePath.hashCode() + String.valueOf(this.musicId).hashCode() + this.type.hashCode());
    }

    public Notification(String name, String music, String imagePath, int musicId, String type) {
        this.name = name;
        this.music = music;
        this.imagePath = imagePath;
        this.musicId = musicId;
        this.type = type;
    }

    protected Notification(Parcel in) {
        name = in.readString();
        music = in.readString();
        imagePath = in.readString();
        musicId = in.readInt();
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

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

    public Notification() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(music);
        dest.writeString(imagePath);
        dest.writeInt(musicId);
    }


}
