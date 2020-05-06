package com.ellpis.KinKena;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ellpis.KinKena.Objects.Utility;
import com.ellpis.KinKena.Repository.SearchRepository;
import com.ellpis.KinKena.Repository.StorageRepository;
import com.ellpis.KinKena.Repository.UserRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.MalformedURLException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;


public class AccountSettings extends Fragment {

    @BindView(R.id.account_setting_username)
    TextView username;
    @BindView(R.id.account_setting_email)
    TextView email;
    @BindView(R.id.account_setting_change_username)
    TextView changeUsername;
    @BindView(R.id.account_setting_change_image)
    TextView changeImage;
    @BindView(R.id.account_setting_logout)
    Button logout;
    @BindView(R.id.account_setting_image)
    ImageView cover;

    // TODO: Rename and change types and number of parameters
    public static AccountSettings newInstance(String param1, String param2) {
        AccountSettings fragment = new AccountSettings();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment'
        return inflater.inflate(R.layout.fragment_account_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        username.setText(MainActivity.username);
        setProfileImage();
        email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        changeUsername.setOnClickListener(changeUsernameOnClick());
        changeImage.setOnClickListener(changeImageOnClick());
        logout.setOnClickListener(logoutOnClick());
    }

    private void setProfileImage(){
        UserRepository.getUser(FirebaseAuth.getInstance().getUid(),task->{
            if(task.getResult().get("profileImage")!=null && task.getResult().get("profileImage").toString().length()>4){
                Picasso.get().load("https://firebasestorage.googleapis.com"+task.getResult().get("profileImage").toString())
                        .placeholder(R.drawable.ic_profile)
                        .into(cover);
            }
        });

    }

    private View.OnClickListener changeUsernameOnClick() {
        return v -> {
            Utility.changeUsername(this);
        };
    }
    private View.OnClickListener changeImageOnClick() {
        return v -> {
            Utility.getPickImageIntent(this);
        };
    }

    private View.OnClickListener logoutOnClick() {
        return v -> {
            new MaterialAlertDialogBuilder(getContext())
                    .setTitle("Are you sure you want to log out?")
                    .setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SearchRepository searchRepository = new SearchRepository(getActivity());
                            searchRepository.clearSearchPref();
                            FirebaseAuth.getInstance().signOut();
                            getActivity().startActivity(new Intent(getContext(), LaunchPage.class));
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setBackground(getResources().getDrawable(R.drawable.dialog_backgound))
                    .show();

        };

    }
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Bundle extras = imageReturnedIntent.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    //fromGallery
                    if (imageBitmap == null) {
                        Picasso.get().load(imageReturnedIntent.getData()).into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                cover.setImageBitmap(bitmap);
                                StorageRepository.saveProfileImageToFirebase(bitmap,task->{
                                    Uri downloadUri = task.getResult();
                                    try {
                                        String url = (new URL(downloadUri.toString())).getPath() + "?" + downloadUri.getQuery();
                                        UserRepository.updateProfileImage(url,getContext(),()->{});

                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        });
                        return;
                    }else{
                        //from Camera
                        cover.setImageBitmap(imageBitmap);
                        StorageRepository.saveProfileImageToFirebase(imageBitmap,task->{
                            Uri downloadUri = task.getResult();
                            try {
                                String url = (new URL(downloadUri.toString())).getPath() + "?" + downloadUri.getQuery();
                                UserRepository.updateProfileImage(url,getContext(), ()->{});

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
                break;

        }
    }

}
