package com.ahari.galleryapplication;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahari.galleryapplication.pojos.Image;
import com.ahari.galleryapplication.pojos.UserProfile;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

/*
    HW07
    UserProfileFragment
    Anoosh Hari, Dayakar Ravuri - Group 29
 */

public class UserProfileFragment extends Fragment {

    private static final String USER_PROFILE = "USER_PROFILE";

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

    private UserProfile userProfile;
    private boolean doUIChange;

    ImageView userDP;
    RecyclerView recyclerView;
    UserProfileAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    Button allUsers, upload;

    ArrayList<Image> images = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private Activity activity;

    public UserProfileFragment(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public static UserProfileFragment newInstance(UserProfile userProfile) {
        UserProfileFragment fragment = new UserProfileFragment(userProfile);
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
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        recyclerView = view.findViewById(R.id.userProfileRecyclerView);
        adapter = new UserProfileAdapter();
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        userDP = view.findViewById(R.id.userDP);

        activity = getActivity();

        allUsers = view.findViewById(R.id.buttonAllUsers);
        upload = view.findViewById(R.id.buttonUploadImages);

        String docId = userProfile.getUserId();

        activity.setTitle(userProfile.getUserName() + getString(R.string.profile_tag));
        if (userProfile.getAccount().getGender().equalsIgnoreCase(getString(R.string.male_label))) {
            userDP.setImageDrawable(getResources().getDrawable(R.drawable.male_img));
        } else {
            userDP.setImageDrawable(getResources().getDrawable(R.drawable.female_img));
        }

        if (userProfile.getAccount().getEmail().equalsIgnoreCase(mAuth.getCurrentUser().getEmail())) {
            upload.setVisibility(View.VISIBLE);
        } else {
            upload.setVisibility(View.INVISIBLE);
        }

        db.collection(getString(R.string.profiles_collection))
                .document(docId)
                .collection(getString(R.string.images_collection))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        images.clear();
                        for (QueryDocumentSnapshot image : value) {
                            Image currentImage = image.toObject(Image.class);
                            currentImage.setImageId(image.getId());
                            images.add(currentImage);
                        }
                        images.sort(new Comparator<Image>() {
                            @Override
                            public int compare(Image o1, Image o2) {
                                return o1.compareTo(o2);
                            }
                        });
                        if (doUIChange) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.launchUploadImagesFragment(userProfile);
            }
        });

        allUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    listener.popFromBackStack();
                } else {
                    listener.launchAllUsersFragment();
                }
            }
        });

        return view;
    }

    class UserProfileAdapter extends RecyclerView.Adapter<UserProfileViewHolder> {

        @NonNull
        @Override
        public UserProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.profile_image_item, parent, false);
            UserProfileViewHolder holder = new UserProfileViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull UserProfileViewHolder holder, int position) {
            Image image = images.get(position);
            holder.postTime.setText(dateFormat.format(image.getPostTime()));
            holder.image = image;
            holder.postBoxContainer.setBackground(getResources().getDrawable(R.drawable.border));

            if (image.getAccount().getEmail().equalsIgnoreCase(mAuth.getCurrentUser().getEmail())) {
                holder.postBinIcon.setVisibility(View.VISIBLE);
            } else {
                holder.postBinIcon.setVisibility(View.INVISIBLE);
            }

            holder.likedUsers = new HashSet<>(image.getLikedBy());
            holder.userPostItemComments.setText(image.getComments().size() + " "+getString(R.string.comments_label));

            holder.userPostItemLikes.setText(image.getLikedBy().size() + " "+getString(R.string.likes_label));
            if (holder.likedUsers.contains(mAuth.getCurrentUser().getEmail())) {
                holder.heartIconPostItem.setImageDrawable(getResources().getDrawable(R.drawable.like_favorite));
                holder.isLiked = true;
            } else {
                holder.heartIconPostItem.setImageDrawable(getResources().getDrawable(R.drawable.like_not_favorite));
                holder.isLiked = false;
            }

            Glide.with(getContext())
                    .load(storage.getReference().child(userProfile.getUserId())
                            .child(images.get(position).getImageId() + getString(R.string.image_extension)))
                    .into(holder.post);
        }

        @Override
        public int getItemCount() {
            return images.size();
        }
    }

    class UserProfileViewHolder extends RecyclerView.ViewHolder {
        ImageView post, postBinIcon, heartIconPostItem;
        TextView postTime;
        TextView userPostItemComments, userPostItemLikes;
        ConstraintLayout postBoxContainer;
        Image image;
        boolean isLiked;
        HashSet<String> likedUsers = new HashSet<>();

        public UserProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            postTime = itemView.findViewById(R.id.imageUploadTime);
            postBinIcon = itemView.findViewById(R.id.postBinIcon);
            heartIconPostItem = itemView.findViewById(R.id.heartIconPostItem);
            post = itemView.findViewById(R.id.userPostItem);
            postBoxContainer = itemView.findViewById(R.id.postBoxContainer);
            userPostItemComments = itemView.findViewById(R.id.userPostItemComments);
            userPostItemLikes = itemView.findViewById(R.id.userPostItemLikes);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.launchPostExtendedFragment(image, userProfile);
                }
            });

            postBinIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.collection(getString(R.string.profiles_collection))
                            .document(image.getUserId())
                            .collection(getString(R.string.images_collection))
                            .document(image.getImageId())
                            .delete();
                    storage.getReference().child(image.getUserId())
                            .child(image.getImageId() + getString(R.string.image_extension))
                            .delete();
                }
            });

            heartIconPostItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isLiked) {
                        likedUsers.remove(mAuth.getCurrentUser().getEmail());
                    } else {
                        likedUsers.add(mAuth.getCurrentUser().getEmail());
                    }
                    db.collection(getString(R.string.profiles_collection))
                            .document(image.getUserId())
                            .collection(getString(R.string.images_collection))
                            .document(image.getImageId())
                            .update("likedBy", new ArrayList<>(likedUsers));
                }
            });
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IUserProfileListener) {
            listener = (IUserProfileListener) context;
        }
        doUIChange = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        doUIChange = false;
    }

    IUserProfileListener listener;

    interface IUserProfileListener {
        void launchUploadImagesFragment(UserProfile userProfile);

        void launchPostExtendedFragment(Image image, UserProfile userProfile);

        void launchAllUsersFragment();

        void popFromBackStack();
    }
}