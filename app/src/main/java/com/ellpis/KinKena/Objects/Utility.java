package com.ellpis.KinKena.Objects;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.InputType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.ellpis.KinKena.Adapters.PlaylistTabAdapter;
import com.ellpis.KinKena.MainActivity;
import com.ellpis.KinKena.R;
import com.ellpis.KinKena.Repository.PlaylistRepository;
import com.ellpis.KinKena.Repository.UserRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;


public class Utility {

    public static int manipulateColor(int color, float whiteAmount) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * 0.6f);
        int g = Math.round(Color.green(color) * 0.6f);
        int b = Math.round(Color.blue(color) * 0.6f);
        return Color.argb(a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255));
    }

    private interface onButtonClick{
        void onclick();
    }
    public static void createPlaylist(Context context, Song song) {
        TextInputLayout textInputLayout = textInputLayoutBuilder(context, "Playlist Name");
        TextInputEditText textInputEditText = textInputBuilder(context, "");
        textInputLayout.addView(textInputEditText);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle("Create playlist")
                .setView(textInputLayout)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (textInputEditText.getText().toString().trim().isEmpty()) {
                            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_LONG).show();
                            createPlaylist(context, song);
                            return;
                        }
                        String currentUserID = FirebaseAuth.getInstance().getUid();
                        Playlist playlist = new Playlist();
                        List<Song> songs = new ArrayList<>();
                        if (song != null) {
                            songs.add(song);
                        }
                        playlist.setSongs(songs);
                        playlist.setOwnerID(currentUserID);
                        playlist.setTitle(textInputEditText.getText().toString().trim());
                        playlist.setOwnerUsername(MainActivity.username);
                        playlist.setPrivate(false);
                        PlaylistRepository.createPlaylist(playlist,context);
                        Toast.makeText(context,"Playlist Created",Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setBackground(context.getDrawable(R.drawable.dialog_backgound));
        builder.show();
    }

    public static void createPlaylist(Context context, PlaylistTabAdapter adapter) {

        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(ll);
        TextInputLayout textInputLayout = textInputLayoutBuilder(context, "Playlist Name");
        TextInputEditText textInputEditText = textInputBuilder(context, "");
        textInputLayout.addView(textInputEditText);
        linearLayout.addView(textInputLayout);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle("Create playlist")
                .setView(linearLayout)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (textInputEditText.getText().toString().trim().isEmpty()) {
                            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_LONG).show();
                            createPlaylist(context, adapter);
                            return;
                        }
                        String currentUserID = FirebaseAuth.getInstance().getUid();
                        Playlist playlist = new Playlist();
                        List<Song> songs = new ArrayList<>();
                        playlist.setSongs(songs);
                        playlist.setTitle(textInputEditText.getText().toString().trim());
                        playlist.setOwnerID(currentUserID);
                        playlist.setOwnerUsername(MainActivity.username);
                        playlist.setPrivate(false);
                        PlaylistRepository.createPlaylist(playlist,context);
                    }
                });
        builder.setBackground(context.getDrawable(R.drawable.dialog_backgound));
        builder.show();
    }


    public static void renamePlaylist(Fragment fragment, Playlist playlist) {

        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout linearLayout = new LinearLayout(fragment.getContext());
        linearLayout.setLayoutParams(ll);
        TextInputLayout textInputLayout = textInputLayoutBuilder(fragment.getContext(), "New Name");
        TextInputEditText textInputEditText = textInputBuilder(fragment.getContext(), "");
        textInputLayout.addView(textInputEditText);
        linearLayout.addView(textInputLayout);
        linearLayout.setBackgroundColor(fragment.getContext().getResources().getColor(R.color.Transparent));
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(fragment.getContext())
                .setTitle("Rename Playlist")
                .setView(linearLayout)
                .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlaylistRepository.renamePlaylist(textInputEditText.getText().toString().trim(), playlist, fragment);
                    }
                });
        builder.setBackground(fragment.getContext().getDrawable(R.drawable.dialog_backgound));
        builder.show();
    }

    public static void deletePlaylist(Fragment fragment, Playlist playlist) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(fragment.getContext())
                .setTitle("Are you sure you want to delete your playlist \"" + playlist.getTitle() + "\"")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlaylistRepository.deletePlaylist(playlist.getId(),fragment.getContext(), () -> fragment.getFragmentManager().popBackStackImmediate());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.setBackground(fragment.getContext().

                getDrawable(R.drawable.dialog_backgound));
        builder.show();
    }

    public static void changeUsername(Fragment fragment) {
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout linearLayout = new LinearLayout(fragment.getContext());
        linearLayout.setLayoutParams(ll);
        TextInputLayout textInputLayout = textInputLayoutBuilder(fragment.getContext(), "New Username");
        TextInputEditText textInputEditText = textInputBuilder(fragment.getContext(), MainActivity.username);
        textInputLayout.addView(textInputEditText);
        linearLayout.addView(textInputLayout);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(fragment.getContext())
                .setTitle("Change Username\"")
                .setView(linearLayout)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserRepository.renameUsername(textInputEditText.getText().toString().trim(),fragment.getContext(), () -> {
                            Toast.makeText(fragment.getContext(), "Name cannot be empty", Toast.LENGTH_LONG).show();
                            if (textInputEditText.getText().toString().trim().isEmpty()) {
                                changeUsername(fragment);
                                return;
                            }
                            ((TextView) fragment.getView().findViewById(R.id.account_setting_username)).setText(textInputEditText.getText().toString().trim());
                            MainActivity.username = textInputEditText.getText().toString().trim();
                        });
                    }
                });
        builder.setBackground(fragment.getContext().getDrawable(R.drawable.dialog_backgound));
        builder.show();
    }

    private static TextInputLayout textInputLayoutBuilder(Context context, String hint) {
        LinearLayout.LayoutParams tilLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        tilLP.leftMargin = 50;
        tilLP.rightMargin = 50;
        tilLP.topMargin = 50;
        TextInputLayout textInputLayout = new TextInputLayout(context);
        textInputLayout.setLayoutParams(tilLP);
        textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.White)));
        textInputLayout.setHintTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.OffWhite)));
        textInputLayout.setBoxBackgroundColor(context.getResources().getColor(R.color.White));
        textInputLayout.setBoxStrokeColor(context.getResources().getColor(R.color.White));
        textInputLayout.setHint(hint);
        return textInputLayout;
    }

    private static TextInputEditText textInputBuilder(Context context, String hint) {
        TextInputEditText textInputEditText = new TextInputEditText(context);
        LinearLayout.LayoutParams tilLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textInputEditText.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.White)));
        textInputEditText.setLayoutParams(tilLP);
        textInputEditText.setTextColor(context.getResources().getColor(R.color.White));
        textInputEditText.setText(hint);
        textInputEditText.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        return textInputEditText;
    }


    public static Intent getPickImageIntent(Fragment parentFragment) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
//        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(context)));
        intentList = addIntentsToList(parentFragment.getContext(), intentList, pickIntent);
        intentList = addIntentsToList(parentFragment.getContext(), intentList, takePhotoIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    "choose a Playlist cover Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }
        parentFragment.startActivityForResult(chooserIntent, 0);
        return chooserIntent;
    }

    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }
}
