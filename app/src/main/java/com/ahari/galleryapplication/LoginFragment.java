package com.ahari.galleryapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ahari.galleryapplication.pojos.UserProfile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/*
    HW07
    LoginFragment
    Anoosh Hari, Dayakar Ravuri - Group 29
 */

public class LoginFragment extends Fragment {

    private static final String TAG = "demo";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public LoginFragment() {

    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    EditText email;
    EditText password;
    Button login;
    TextView createNewAccount;
    ILoginListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        email = view.findViewById(R.id.loginFragmentEmail);
        password = view.findViewById(R.id.loginFragmentPassword);
        login = view.findViewById(R.id.loginFragmentLogin);
        createNewAccount = view.findViewById(R.id.loginFragmentCreateNewAccount);

        getActivity().setTitle(getString(R.string.login_title));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.error_dialogue_title));
        builder.setPositiveButton(getString(R.string.error_dialogue_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String emailId = email.getText().toString();
                    String passwordVal = password.getText().toString();
                    if (emailId.isEmpty()) {
                        throw new Exception(getString(R.string.email_error));
                    }

                    if (passwordVal.isEmpty()) {
                        throw new Exception(getString(R.string.password_error));
                    }
                    mAuth.signInWithEmailAndPassword(emailId, passwordVal)
                            .addOnSuccessListener(getActivity(), new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Toast.makeText(getActivity(), getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
                                    listener.launchAllUsersFragment();
                                }
                            }).addOnFailureListener(getActivity(), new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            builder.setMessage(e.getMessage());
                            builder.show();
                        }
                    });
                } catch (Exception e) {
                    builder.setMessage(e.getMessage());
                    builder.show();
                }
            }
        });

        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.launchCreateNewAccountFragment();
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ILoginListener) {
            listener = (ILoginListener) context;
        }
    }

    interface ILoginListener {
        void launchAllUsersFragment();

        void launchCreateNewAccountFragment();
    }
}