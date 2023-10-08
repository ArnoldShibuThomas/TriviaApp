package com.arnold.triviaapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextView usernameHere;
    private TextView scoreHere;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button logoutBtn;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private LoadingDialog loadingDialog = null;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(loadingDialog == null) {
            loadingDialog = new LoadingDialog(getActivity());
        }
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(loadingDialog == null) {
            loadingDialog = new LoadingDialog(getActivity());
        }
        // start loading
        loadingDialog.startLoadingDialog();
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        scoreHere = view.findViewById(R.id.scoreHere);
        usernameHere = view.findViewById(R.id.userNameDisplay);
        logoutBtn = view.findViewById(R.id.logoutBtn);
        // get the instance of the auth
        firebaseAuth = FirebaseAuth.getInstance();
        // This will update everything based on score
        // Get the current user's uid
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        // get firebase instance
        rootNode = FirebaseDatabase.getInstance();
        // get reference
        reference = rootNode.getReference(uid);
        reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    UserHelperClass userhere = task.getResult().getValue(UserHelperClass.class);
                    // set the score and user name
                    usernameHere.setText(userhere.getUsername());
                    scoreHere.setText("Score: " + userhere.getScore());
                }
                else{
                    Toast.makeText(getActivity(), "Internet unavailable", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // add the logout button functionality to sign out and go back to login page
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(getActivity(),Login.class));
            }
        });
        // create a delay
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.dismissDialog();
            }
        }, 600);
        // Inflate the layout for this fragment
        return view;
    }
}