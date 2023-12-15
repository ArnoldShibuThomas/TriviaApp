package com.arnold.triviaapp;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

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

    private CircleImageView profilePicture = null;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;

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
        // instantiate the profile picture
        profilePicture = view.findViewById(R.id.profilePic);
        // get the instance of the auth
        firebaseAuth = FirebaseAuth.getInstance();
        // get the instance of the firebase storage
        storage = FirebaseStorage.getInstance();
        // get the reference from the Firebase storage
        storageReference = storage.getReference();
        // set an on click listener for the application
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // allow the user to select a photo from their device
                selectPictureFromDevice();
            }
        });
        // This will update everything based on score
        // Get the current user's uid
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        try {
            // Create a reference with an initial file path and name
            StorageReference pathReference = storageReference.child("images/" + uid);

            pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // This will use glide to get the image and show it on the image view
                    Glide.with(getContext())
                            .load(uri)
                            .placeholder(R.drawable.baseline_person_24)
                            .error(R.drawable.baseline_person_24)
                            .into(profilePicture);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Image failed to display", Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (Exception e){
            // no photo file provided
            e.printStackTrace();
        }
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

    private void selectPictureFromDevice() {
        // create an intent to open the gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    // This will be the onActivityResult method we are overriding
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code was 1 and that the user has selected an image
        if(requestCode == 1 && resultCode==RESULT_OK && data != null && data.getData()!= null){
            imageUri = data.getData();
            // set the picture to the one that the user selected
            profilePicture.setImageURI(imageUri);
            // lets go ahead and let the user upload their picture
            uploadPicture();
        }
    }

    // This will go ahead and upload the user selected picture
    private void uploadPicture() {
        // create a progress dialog
        final ProgressDialog uploadProgress = new ProgressDialog(getContext());
        // set the title of the progress
        uploadProgress.setTitle("Image uploading");
        // show the progress
        uploadProgress.show();
        // get the uuid associate with the current user since this will be unique to each
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        // Create a reference to this
        StorageReference profilePictureRef = storageReference.child("images/" + uid);
        // go ahead and upload the profile picture
        profilePictureRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // dismiss the progress bar
                uploadProgress.dismiss();
                // show the users we were able to upload the image successfully
                Toast.makeText(getContext(), "Image uploaded!", Toast.LENGTH_SHORT).show();
            }
        }).removeOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // dismiss the progress bar
                uploadProgress.dismiss();
                Toast.makeText(getContext(), "Failed to upload!", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                // get the upload percentage
                double progressMade = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                // this will show the upload progress
                uploadProgress.setMessage("Percentage Complete: " + (int) progressMade + "%");
            }
        });

    }
}