package com.ellpis.KinKena.Repository;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

public class CloudStorageRepository {
    public interface FirebaseFunctionOnCompleteTask {
        void onCompleteFunction(Uri task);
    }
    public interface FirebaseFunctionOnComplete {
        void onCompleteFunction();
    }
    public static void saveProfileImageToFirebase(Bitmap imageBitmap, FirebaseFunctionOnCompleteTask firebaseFunctionOnCompleteTask) {
        String currentUserID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getWidth());
        } catch (IllegalArgumentException ex) {
            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getHeight(), imageBitmap.getHeight());
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        final StorageReference firebaseImageFolder = FirebaseStorage.getInstance()
                .getReference()
                .child(currentUserID + "/profileImage.jpg");

        firebaseImageFolder.putBytes(byteArray).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            // Continue with the task to get the download URL
            return firebaseImageFolder.getDownloadUrl();
        }).addOnSuccessListener(task -> {
                firebaseFunctionOnCompleteTask.onCompleteFunction(task);

        });
    }
    public static void savePlaylistImageToFirebase(Bitmap imageBitmap, String playlistID, FirebaseFunctionOnCompleteTask firebaseFunctionOnCompleteTask) {
        String currentUserID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getWidth());
        } catch (IllegalArgumentException ex) {
            imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getHeight(), imageBitmap.getHeight());
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        final StorageReference firebaseImageFolder = FirebaseStorage.getInstance()
                .getReference()
                .child(currentUserID + "/" + playlistID + "/playlistCover.jpg");

        firebaseImageFolder.putBytes(byteArray).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            // Continue with the task to get the download URL
            return firebaseImageFolder.getDownloadUrl();
        }).addOnSuccessListener(task -> {
                firebaseFunctionOnCompleteTask.onCompleteFunction(task);

        });
    }
}
