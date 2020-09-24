package com.ellpis.KinKena.Objects;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Parcelable;
import android.provider.MediaStore;

import androidx.fragment.app.Fragment;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class Utility {

    public static int manipulateColor(int color, float whiteAmount) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * 1f);
        int g = Math.round(Color.green(color) *1f);
        int b = Math.round(Color.blue(color) * 1f);
        return Color.argb(a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255));
    }

    public interface onButtonClick {
        void onclick();
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
    public static void getImageLinkMini(String link, ImageLinkRetrieved imageLinkRetrieved){
        if(link.equals("/images/default/album_cover_120x120.png")) {
            imageLinkRetrieved.OnImageLickRetrieved("https://firebasestorage.googleapis.com/v0/b/ethiopian-music-app.appspot.com/o/Assets%2Fkinkena_album_art_mini.png?alt=media&token=2e79300e-17f4-4fad-bd27-d3a494e63f24");
        }else{
            imageLinkRetrieved.OnImageLickRetrieved("http://www.arifzefen.com" + link);
        }
    }
    public static void getImageLinkLarge(String link, ImageLinkRetrieved imageLinkRetrieved){
        if(link.equals("/images/default/album_cover_120x120.png")) {
            imageLinkRetrieved.OnImageLickRetrieved("https://firebasestorage.googleapis.com/v0/b/ethiopian-music-app.appspot.com/o/Assets%2Fkinkena_album_art.png?alt=media&token=1eb2dd07-a164-4293-97df-1d873de731bf");
        }else{
            imageLinkRetrieved.OnImageLickRetrieved("http://www.arifzefen.com" + link);
        }
    }
    public interface ImageLinkRetrieved {
        void OnImageLickRetrieved(String link);
    }
}
