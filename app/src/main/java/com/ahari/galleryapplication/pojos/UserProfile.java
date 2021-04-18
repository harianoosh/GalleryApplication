package com.ahari.galleryapplication.pojos;

import java.io.Serializable;

/*
    HW07
    UserProfile
    Anoosh Hari, Dayakar Ravuri - Group 29
 */

public class UserProfile implements Serializable {
    String userName,  userId;
    Account account;

    @Override
    public String toString() {
        return "UserProfile{" +
                "userName='" + userName + '\'' +
                ", userId='" + userId + '\'' +
                ", account=" + account +
                '}';
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
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
}
