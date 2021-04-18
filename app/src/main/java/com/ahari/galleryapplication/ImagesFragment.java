package com.ahari.galleryapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ahari.galleryapplication.pojos.Image;
import com.ahari.galleryapplication.pojos.UserProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/*
    HW07
    ImagesFragment
    Anoosh Hari, Dayakar Ravuri - Group 29
 */

public class ImagesFragment extends Fragment {

    private static final String USER_PROFILE = "USER_PROFILE";
    List<ClipData.Item> images = new ArrayList<ClipData.Item>();
    ImageView imageView;
    TextView imageLabel;
    GridView gridView;
    GridViewAdapter adapter;

    FirebaseFirestore db;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    Resources resources;

    AlertDialog.Builder builder;
    int nImages = 0;

    private UserProfile userProfile;

    public ImagesFragment(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public static ImagesFragment newInstance(UserProfile userProfile) {
        ImagesFragment fragment = new ImagesFragment(userProfile);
        Bundle args = new Bundle();
        args.putSerializable(USER_PROFILE, userProfile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userProfile = (UserProfile) getArguments().getSerializable(USER_PROFILE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_images, container, false);

        getActivity().setTitle(getString(R.string.upload_images_title));

        imageView = view.findViewById(R.id.image);
        imageLabel = view.findViewById(R.id.previewText);
        gridView = view.findViewById(R.id.gridView);
        adapter = new GridViewAdapter();
        gridView.setAdapter(adapter);
        imageLabel.setVisibility(View.INVISIBLE);

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle(getString(R.string.uploading_label));

        builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton(getString(R.string.error_dialogue_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        resources = getResources();


        view.findViewById(R.id.buttonSelectImages).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_images_label)), 1);
            }
        });

        view.findViewById(R.id.buttonUpload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (images.size() > 0) {
                    progressDialog.show();
                    uploadImageToFirebaseStorage(images.get(nImages), progressDialog);
                } else {
                    builder.setTitle(getString(R.string.error_dialogue_title));
                    builder.setMessage(getString(R.string.no_images_to_upload));
                    builder.show();
                }
            }
        });

        return view;
    }

    private void uploadImageToFirebaseStorage(ClipData.Item image, ProgressDialog progressDialog) {
        String imageId = UUID.randomUUID().toString();
        storage.getReference().child(mAuth.getUid()).child(imageId + getString(R.string.image_extension))
                .putFile(image.getUri())
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        progressDialog.setTitle(getString(R.string.upload_images_title)+" " + (nImages + 1) + "/" + images.size());
                        progressDialog.setMessage(getString(R.string.upload_progress_title)+" " + snapshot.getBytesTransferred() + "/" + snapshot.getTotalByteCount());
                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Image currentImage = new Image();
                currentImage.setImageId(imageId);
                currentImage.setPostTime(new Date());
                currentImage.setUserId(mAuth.getUid());
                currentImage.setUserName(mAuth.getCurrentUser().getDisplayName());
                currentImage.setAccount(userProfile.getAccount());
                db.collection(resources.getString(R.string.profiles_collection)).document(mAuth.getUid())
                        .collection(resources.getString(R.string.images_collection)).document(imageId)
                        .set(currentImage)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                nImages++;
                                if (nImages < images.size()) {
                                    uploadImageToFirebaseStorage(images.get(nImages), progressDialog);
                                } else {
                                    progressDialog.dismiss();
                                    listener.popFromBackStack();
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        images.clear();
        if (data != null && data.getData() != null) {
            images.add(new ClipData.Item(data.getData()));
            imageLabel.setVisibility(View.VISIBLE);
        } else if (data != null && data.getClipData() != null) {
            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                images.add(data.getClipData().getItemAt(i));
            }
            imageLabel.setVisibility(View.VISIBLE);
        } else {
            imageLabel.setVisibility(View.INVISIBLE);
            Toast.makeText(getContext(), getString(R.string.import_images_0), Toast.LENGTH_SHORT).show();
        }

        if (images.size() > 0) {
            db = FirebaseFirestore.getInstance();
        }
        adapter.notifyDataSetChanged();
    }

    class GridViewAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object getItem(int position) {
            return images.get(position).getUri();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.images_item, parent, false);
            }
            ImageView imageItem = convertView.findViewById(R.id.imageItem);
            ConstraintLayout background = convertView.findViewById(R.id.background);
            background.setBackground(getResources().getDrawable(R.drawable.border));
            imageItem.setImageURI(images.get(position).getUri());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.launchImages(images.get(position).getUri());
                }
            });
            return convertView;
        }

    }

    IImagesFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IImagesFragmentListener) {
            listener = (IImagesFragmentListener) context;
        }
    }

    interface IImagesFragmentListener {
        void launchImages(Uri uri);

        void popFromBackStack();
    }
}