package com.ellpis.KinKena.Repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.ellpis.KinKena.Objects.Dialogs;
import com.ellpis.KinKena.Objects.Playlist;
import com.ellpis.KinKena.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class PlaylistRepository {
    public static final String PLAYLIST_DOWNLOAD_ID="PLAYLIST_DOWNLOAD";
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    public interface FirebaseFunctionOnCompleteDocumentTask {
        void onCompleteFunction(DocumentSnapshot task);
    }

    public interface FirebaseFunctionOnCompleteCollectionTask {
        void onCompleteFunction(QuerySnapshot task);
    }

    public interface FirebaseFunctionOnComplete {
        void onCompleteFunction();
    }

    public static void deletePlaylist(String playlistId, Context context, FirebaseFunctionOnComplete firebaseFunctionOnComplete) {
        String currentUserID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users").document(currentUserID).collection("Playlists")
                .document(playlistId).delete();
        firebaseFunctionOnComplete.onCompleteFunction();

        deleteSearchPlaylistIndex(playlistId, context);
    }

    public static void renamePlaylist(String newName, Playlist playlist, Fragment fragment) {
        String currentUserID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (newName.isEmpty()) {
            Toast.makeText(fragment.getContext(), "Name cannot be empty", Toast.LENGTH_LONG).show();
            Dialogs.renamePlaylist(fragment, playlist);
            return;
        }
        db.collection("Users").document(currentUserID).collection("Playlists")
                .document(playlist.getId()).update("title", newName);
        renameSearchPlaylistIndex(newName, playlist.getId(), fragment.getContext());

    }

    public static void setPrivacyPlaylist(String playlistID, boolean newPrivacy, Fragment fragment) {
        String currentUserID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(currentUserID).collection("Playlists")
                .document(playlistID).update("privacy", newPrivacy);
        updatePrivacySearchPlaylistIndex(newPrivacy, playlistID, fragment.getContext());

    }


    public static void createPlaylist(Playlist playlist, Context context) {
        String currentUserID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(currentUserID).collection("Playlists")
                .add(playlist).addOnSuccessListener(task -> {
            createSearchPlaylistIndex(playlist, task.getId(), context);
        });
    }

    public static void followPlaylist(String ownerID, Playlist playlist) {
        if (!playlist.isFromFirebase()) {
            String currentUserID = FirebaseAuth.getInstance().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(currentUserID).collection("Playlists")
                    .document(ownerID.substring(ownerID.lastIndexOf('/') + 1)).set(playlist);
        } else {
            String currentUserID = FirebaseAuth.getInstance().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(currentUserID).collection("Playlists")
                    .document(playlist.getId()).set(playlist);
        }
    }
    public static void unFollowPlaylist(String ownerID, Playlist playlist) {
        if (!playlist.isFromFirebase()) {
            String currentUserID = FirebaseAuth.getInstance().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(currentUserID).collection("Playlists")
                    .document(ownerID.substring(ownerID.lastIndexOf('/') + 1)).delete();
        } else {
            String currentUserID = FirebaseAuth.getInstance().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(currentUserID).collection("Playlists")
                    .document(playlist.getId()).delete();
        }
    }

    public static void getPlaylist(String ownerID, String playlistID, FirebaseFunctionOnCompleteDocumentTask firebaseFunctionOnCompleteDocumentTask) {
        String currentUserID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(ownerID).collection("Playlists").document(playlistID)
                .get().addOnSuccessListener(firebaseFunctionOnCompleteDocumentTask::onCompleteFunction);
    }

    public static void getAllPlaylists(String userID, FirebaseFunctionOnCompleteCollectionTask firebaseFunctionOnCompleteCollectionTask) {
        String currentUserID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(userID).collection("Playlists")
                .get().addOnSuccessListener(firebaseFunctionOnCompleteCollectionTask::onCompleteFunction);
    }

    private static void createSearchPlaylistIndex(Playlist playlist, String id, Context context) {
        Client client = new Client(context.getString(R.string.algolia_application_id), context.getString(R.string.algolia_api_key));
        playlist.setOwnerID(FirebaseAuth.getInstance().getUid());
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(new Gson().toJson(playlist));
            jsonObject.put("objectID", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Index index = client.getIndex("Playlists");
        index.addObjectAsync(jsonObject, (jsonObject1, e) -> {
        });
    }

    private static void renameSearchPlaylistIndex(String newName, String id, Context context) {
        Client client = new Client(context.getString(R.string.algolia_application_id), context.getString(R.string.algolia_api_key));

        Index index = client.getIndex("Playlists");
        try {
            index.partialUpdateObjectAsync(
                    new JSONObject("{\"title\": \"" + newName + "\"}"),
                    id,
                    null
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void updatePrivacySearchPlaylistIndex(boolean isPrivate, String id, Context context) {
        Client client = new Client(context.getString(R.string.algolia_application_id), context.getString(R.string.algolia_api_key));

        Index index = client.getIndex("Playlists");
        try {
            index.partialUpdateObjectAsync(
                    new JSONObject("{\"privacy\": " + isPrivate + "}"),
                    id,
                    null
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void deleteSearchPlaylistIndex(String id, Context context) {
        Client client = new Client(context.getString(R.string.algolia_application_id), context.getString(R.string.algolia_api_key));
        Index index = client.getIndex("Playlists");
        index.deleteObjectAsync(id, (jsonObject, e) -> {
        });
    }


}
