package com.ahari.galleryapplication.pojos;

import java.io.Serializable;
import java.util.Date;

/*
    HW07
    Comment
    Anoosh Hari, Dayakar Ravuri - Group 29
 */

public class Comment implements Serializable, Comparable<Comment> {
    private String commentText, commentId, imageId;
    private Date commentTime;
    private Account commentedBy;

    @Override
    public String toString() {
        return "Comment{" +
                "commentText='" + commentText + '\'' +
                ", commentId='" + commentId + '\'' +
                ", imageId='" + imageId + '\'' +
                ", commentTime=" + commentTime +
                ", commentedBy=" + commentedBy +
                '}';
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public Date getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(Date commentTime) {
        this.commentTime = commentTime;
    }

    public Account getCommentedBy() {
        return commentedBy;
    }

    public void setCommentedBy(Account commentedBy) {
        this.commentedBy = commentedBy;
    }

    @Override
    public int compareTo(Comment o) {
        return o.getCommentTime().compareTo(this.commentTime);
    }
}
