package com.ahari.galleryapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import com.ahari.galleryapplication.pojos.Image;
import com.ahari.galleryapplication.pojos.UserProfile;
import com.google.firebase.auth.FirebaseAuth;

/*
    HW07
    MainActivity
    Anoosh Hari, Dayakar Ravuri - Group 29
 */

public class MainActivity extends AppCompatActivity implements ImagesFragment.IImagesFragmentListener, LoginFragment.ILoginListener, CreateNewAccountFragment.ICreateNewAccountListener, AllUsersFragment.IAllUsersListener, UserProfileFragment.IUserProfileListener {

    private static final String TAG = "mainActivity";

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (mAuth.getCurrentUser() == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.mainContainer, LoginFragment.newInstance())
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.mainContainer, AllUsersFragment.newInstance())
                    .commit();
        }
    }

    @Override
    public void launchImages(Uri uri) {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.mainContainer, ImageItemFragment.newInstance(uri))
                .commit();
    }

    @Override
    public void gotoLogin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainContainer, LoginFragment.newInstance())
                .commit();
    }

    @Override
    public void launchUserProfileFragment(UserProfile userProfile) {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.mainContainer, UserProfileFragment.newInstance(userProfile))
                .commit();
    }

    @Override
    public void launchAllUsersFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainContainer, AllUsersFragment.newInstance())
                .commit();
    }

    @Override
    public void launchCreateNewAccountFragment() {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.mainContainer, CreateNewAccountFragment.newInstance())
                .commit();
    }

    @Override
    public void launchUploadImagesFragment(UserProfile userProfile) {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.mainContainer, ImagesFragment.newInstance(userProfile))
                .commit();
    }

    @Override
    public void launchPostExtendedFragment(Image image, UserProfile userProfile) {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.mainContainer, PostExtendedFragment.newInstance(image, userProfile))
                .commit();
    }

    @Override
    public void launchUserProfileFragment(UserProfile userProfile, boolean addToBackStack) {
        if (addToBackStack){
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.mainContainer, UserProfileFragment.newInstance(userProfile))
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainContainer, UserProfileFragment.newInstance(userProfile))
                    .commit();
        }
    }

    @Override
    public void popFromBackStack() {
        getSupportFragmentManager().popBackStack();
    }
}