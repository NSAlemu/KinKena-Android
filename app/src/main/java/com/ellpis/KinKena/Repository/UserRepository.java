package com.ellpis.KinKena.Repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.ellpis.KinKena.Objects.Playlist;
import com.ellpis.KinKena.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONException;
import org.json.JSONObject;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class UserRepository {

    public interface FirebaseFunctionOnComplete {
        void onCompleteFunction();
    }

    public interface FirebaseFunctionOnCompleteTask {
        void onCompleteFunction(Task<DocumentSnapshot> task);
    }
    public interface FirebaseFunctionOnAuthTask {
        void onCompleteFunction(Task<AuthResult> task);
    }

    public static void renameUsername(String newName, Context context, FirebaseFunctionOnComplete firebaseFunctionOnComplete) {
        String currentUserID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(currentUserID).update("username", newName)
                .addOnCompleteListener(task -> {
                    firebaseFunctionOnComplete.onCompleteFunction();
                });
        renameSearchUsernameIndex(newName, currentUserID,context);
        PlaylistRepository.getAllPlaylists(currentUserID, task->{
            // Get a new write batch
            WriteBatch batch = db.batch();
            for (QueryDocumentSnapshot document : task.getResult()) {
                Playlist playlist = document.toObject(Playlist.class);
                if(playlist.getOwnerID().equals(currentUserID)){
                    DocumentReference sfRef = db.collection("Users").document(currentUserID)
                            .collection("Playlists").document(playlist.getId());
                    batch.update(sfRef, "ownerUsername", newName);
                }
            }
            batch.commit();
        });
    }

    public static void updateProfileImage(String profileImage, Context context, FirebaseFunctionOnComplete firebaseFunctionOnComplete) {
        String currentUserID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(currentUserID).update("profileImage", profileImage)
                .addOnCompleteListener(task -> {
                    firebaseFunctionOnComplete.onCompleteFunction();
                });
        updateSearchProfileImageIndex(profileImage, currentUserID,context);
    }

    public static void getUser(String userId, FirebaseFunctionOnCompleteTask firebaseFunctionOnCompleteTask) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(userId).get()
                .addOnCompleteListener(task -> firebaseFunctionOnCompleteTask.onCompleteFunction(task));
    }

    public static void createUser(String Username, String email, String password, Context context, FirebaseFunctionOnAuthTask firebaseFunctionOnAuthTask) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    Log.e(TAG, "createUser: savng to algolia");
                    createSearchUserIndex(Username, FirebaseAuth.getInstance().getUid(),context);
                    Log.e(TAG, "createUser: saving to firebase" );
                    firebaseFunctionOnAuthTask.onCompleteFunction(task);
                    Log.e(TAG, "createUser: done" );
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: "+e.toString() );
            }
        });


    }
    private static void createSearchUserIndex(String username, String id, Context context){
        Client client = new Client(context.getString(R.string.algolia_application_id), context.getString(R.string.algolia_api_key));

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{\"username\": \""+username+"\"}, "
                    +"{\"objectID\": \""+id+"\"}"
                    +"{\"profileImage\": \"\"}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Index index = client.getIndex("Users");
        index.addObjectAsync(jsonObject, (jsonObject1, e) -> { });
    }

    private static void renameSearchUsernameIndex(String newName, String id, Context context){
        Client client = new Client(context.getString(R.string.algolia_application_id), context.getString(R.string.algolia_api_key));

        Index index = client.getIndex("Users");
        try {
            index.partialUpdateObjectAsync(
                    new JSONObject("{\"username\": \""+newName+"\"}"),
                    id,
                    null
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private static void updateSearchProfileImageIndex(String profileImage, String id, Context context){
        Client client = new Client(context.getString(R.string.algolia_application_id), context.getString(R.string.algolia_api_key));

        Index index = client.getIndex("Users");
        try {
            index.partialUpdateObjectAsync(
                    new JSONObject("{\"profileImage\": \""+profileImage+"\"}"),
                    id,
                    null
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private static void deleteSearchPlaylistIndex(String id, Context context){
        Client client = new Client(context.getString(R.string.algolia_application_id), context.getString(R.string.algolia_api_key));
        Index index = client.getIndex("Users");
        index.deleteObjectAsync(id, (jsonObject, e) -> {});

    }
}

