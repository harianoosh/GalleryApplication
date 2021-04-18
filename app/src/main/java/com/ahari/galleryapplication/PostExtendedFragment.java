package com.ahari.galleryapplication;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahari.galleryapplication.pojos.Account;
import com.ahari.galleryapplication.pojos.Comment;
import com.ahari.galleryapplication.pojos.Image;
import com.ahari.galleryapplication.pojos.UserProfile;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

/*
    HW07
    PostExtendedFragment
    Anoosh Hari, Dayakar Ravuri - Group 29
 */

public class PostExtendedFragment extends Fragment {

    private static final String IMAGE = "IMAGE";
    private static final String USER_PROFILE = "USER_PROFILE";
    private static final String TAG = "threadId test";
    private UserProfile userProfile;

    private Image image;
    private boolean doUIChange;

    private ArrayList<Comment> comments = new ArrayList<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

    Account loggedInUserAccount;

    Activity activity;

    ExtendedViewAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    RecyclerView recyclerView;
    ImageView postPicture;
    TextView commentsCount;
    EditText commentInput;
    Button postComment;

    public PostExtendedFragment(Image image, UserProfile userProfile) {
        this.image = image;
        this.userProfile = userProfile;
    }

    public static PostExtendedFragment newInstance(Image image, UserProfile userProfile) {
        PostExtendedFragment fragment = new PostExtendedFragment(image, userProfile);
        Bundle args = new Bundle();
        args.putSerializable(IMAGE, image);
        args.putSerializable(USER_PROFILE, userProfile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            image = (Image) getArguments().getSerializable(IMAGE);
            userProfile = (UserProfile) getArguments().getSerializable(USER_PROFILE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_extended, container, false);

        recyclerView = view.findViewById(R.id.commentsRecyclerView);
        postPicture = view.findViewById(R.id.postImageExtended);
        commentsCount = view.findViewById(R.id.commentsCount);
        commentInput = view.findViewById(R.id.commentTextInput);
        postComment = view.findViewById(R.id.buttonCommentPost);

        activity = getActivity();
        activity.setTitle(image.getAccount().getName() + getString(R.string.post_tag));

        getCurrentlyLoggedInUserAccount();

        Glide.with(activity)
                .load(storage.getReference().child(image.getUserId())
                        .child(image.getImageId() + getString(R.string.image_extension)))
                .into(postPicture);

        adapter = new ExtendedViewAdapter();
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        db.collection(getString(R.string.profiles_collection))
                .document(image.getUserId())
                .collection(getString(R.string.images_collection))
                .document(image.getImageId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        comments.clear();
                        Image image = value.toObject(Image.class);
                        if (image != null) {
                            comments.addAll(image.getComments());
                        }
                        comments.sort(new Comparator<Comment>() {
                            @Override
                            public int compare(Comment o1, Comment o2) {
                                return o1.compareTo(o2);
                            }
                        });
                        if (doUIChange) {
                            commentsCount.setText(getString(R.string.comments_label) + " " + comments.size());
                            adapter.notifyDataSetChanged();
                        }
                    }
                });

        postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Comment comment = new Comment();
                comment.setCommentedBy(loggedInUserAccount);
                comment.setCommentText(commentInput.getText().toString());
                comment.setCommentTime(new Date());
                comment.setImageId(image.getImageId());
                comments.add(comment);
                db.collection(getString(R.string.profiles_collection))
                        .document(image.getUserId())
                        .collection(getString(R.string.images_collection))
                        .document(image.getImageId())
                        .update("comments", comments).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        commentInput.setText("");
                    }
                });

            }
        });

        return view;
    }

    private void getCurrentlyLoggedInUserAccount() {
        postComment.setClickable(false);
        db.collection(getString(R.string.accounts_collection))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot account : task.getResult()) {
                                Account temp = account.toObject(Account.class);
                                if (temp.getEmail().equalsIgnoreCase(mAuth.getCurrentUser().getEmail())) {
                                    loggedInUserAccount = temp;
                                }
                            }
                        }
                        postComment.setClickable(true);
                    }
                });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        doUIChange = false;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        doUIChange = true;
    }

    class ExtendedViewAdapter extends RecyclerView.Adapter<ExtendedViewHolder> {

        @NonNull
        @Override
        public ExtendedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.comment_item, parent, false);
            ExtendedViewHolder holder = new ExtendedViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ExtendedViewHolder holder, int position) {
            Comment comment = comments.get(position);
            holder.userName.setText(comment.getCommentedBy().getName());
            holder.commentText.setText(comment.getCommentText());
            holder.commentTime.setText(dateFormat.format(comment.getCommentTime()));
            if (comment.getCommentedBy().getEmail().equalsIgnoreCase(mAuth.getCurrentUser().getEmail())) {
                holder.commentBin.setVisibility(View.VISIBLE);
            } else {
                holder.commentBin.setVisibility(View.INVISIBLE);
            }
            holder.comment = comments.get(position);

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }
    }

    class ExtendedViewHolder extends RecyclerView.ViewHolder {

        TextView userName, commentText, commentTime;
        ImageView commentBin;
        Comment comment;

        public ExtendedViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.commentUserName);
            commentText = itemView.findViewById(R.id.commentText);
            commentTime = itemView.findViewById(R.id.commentTime);
            commentBin = itemView.findViewById(R.id.commentBin);

            itemView.findViewById(R.id.commentBoxContainer).setBackground(getResources().getDrawable(R.drawable.border));

            commentBin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    comments.remove(comment);
                    db.collection(getString(R.string.profiles_collection))
                            .document(image.getUserId())
                            .collection(getString(R.string.images_collection))
                            .document(comment.getImageId())
                            .update("comments", comments);
                }
            });
        }
    }
}