package com.ahari.galleryapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.ahari.galleryapplication.pojos.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

/*
    HW07
    AllUsersFragment
    Anoosh Hari, Dayakar Ravuri - Group 29
 */

public class AllUsersFragment extends Fragment {

    private boolean doUIChange;

    ListView listView;
    ArrayAdapter arrayAdapter;
    Button myProfile;
    Button signOut;

    Activity activity;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ArrayList<UserProfile> users = new ArrayList<>();
    ArrayList<String> userNames = new ArrayList<>();

    UserProfile currentUser;

    public AllUsersFragment() {

    }

    public static AllUsersFragment newInstance() {
        AllUsersFragment fragment = new AllUsersFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_users, container, false);

        signOut = view.findViewById(R.id.buttonSignOut);
        myProfile = view.findViewById(R.id.buttonMyProfile);
        listView = view.findViewById(R.id.listViewAllUsers);
        arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_expandable_list_item_1,
                android.R.id.text1, userNames);

        listView.setAdapter(arrayAdapter);

        activity = getActivity();
        activity.setTitle(getString(R.string.all_users_title));

        myProfile.setClickable(false);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.launchUserProfileFragment(users.get(position));
            }
        });

        getAllUsers();

        myProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.launchUserProfileFragment(currentUser);
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                listener.gotoLogin();
            }
        });

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void updateUserNames(){
        userNames.clear();
        users.sort(new Comparator<UserProfile>() {
            @Override
            public int compare(UserProfile o1, UserProfile o2) {
                return o1.getUserName().compareTo(o2.getUserName());
            }
        });
        userNames.addAll(users.stream().map(e -> e.getUserName()).collect(Collectors.toList()));
    }

    private void getAllUsers() {
        db.collection(getString(R.string.profiles_collection))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        users.clear();
                        for (QueryDocumentSnapshot account : value) {
                            UserProfile userProfile = account.toObject(UserProfile.class);
                            users.add(userProfile);
                        }
                        if (doUIChange){
                            updateUserNames();
                            arrayAdapter.notifyDataSetChanged();
                        }
                    }
                });

        db.collection(getString(R.string.profiles_collection))
                .document(mAuth.getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (doUIChange){
                            currentUser = value.toObject(UserProfile.class);
                            myProfile.setClickable(true);
                        }
                    }
                });
    }

    IAllUsersListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IAllUsersListener) {
            listener = (IAllUsersListener) context;
        }
        doUIChange = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        doUIChange = false;
    }

    interface IAllUsersListener {
        void gotoLogin();

        void launchUserProfileFragment(UserProfile userProfile);
    }
}