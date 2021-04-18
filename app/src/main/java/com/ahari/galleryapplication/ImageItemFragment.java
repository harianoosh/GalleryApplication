package com.ahari.galleryapplication;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/*
    HW07
    ImageItemFragment
    Anoosh Hari, Dayakar Ravuri - Group 29
 */

public class ImageItemFragment extends Fragment {

    private static final String URI = "URI";

    private Uri uri;

    ImageView imageView;

    public ImageItemFragment(Uri uri) {
        this.uri = uri;
    }

    public static ImageItemFragment newInstance(Uri uri) {
        ImageItemFragment fragment = new ImageItemFragment(uri);
        Bundle args = new Bundle();
        args.putParcelable(URI, uri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uri = getArguments().getParcelable(URI);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_item, container, false);
        getActivity().setTitle(getString(R.string.preview_title));
        imageView = view.findViewById(R.id.imageView);
        imageView.setImageURI(uri);
        return view;
    }
}