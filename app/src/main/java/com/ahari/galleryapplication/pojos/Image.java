package com.ahari.galleryapplication.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/*
    HW07
    Image
    Anoosh Hari, Dayakar Ravuri - Group 29
 */

public class Image implements Serializable, Comparable<Image> {
    private String imageId, userName, userId;
    private Date postTime;
    private Account account;
    private ArrayList<String> likedBy = new ArrayList<>();
    private ArrayList<Comment> comments = new ArrayList<>();

    @Override
    public String toString() {
        return "Image{" +
                "imageId='" + imageId + '\'' +
                ", userName='" + userName + '\'' +
                ", userId='" + userId + '\'' +
                ", postTime=" + postTime +
                ", account=" + account +
                ", likedBy=" + likedBy +
                ", comments=" + comments +
                '}';
    }

    public ArrayList<String> getLikedBy() {
        return likedBy;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getPostTime() {
        return postTime;
    }

    public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public int compareTo(Image o) {
        return o.getPostTime().compareTo(this.postTime);
    }
}
