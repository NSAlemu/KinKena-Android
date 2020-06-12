package com.ellpis.KinKena.Repository;

import android.content.Context;

import com.ellpis.KinKena.Objects.BrowseItems;
import com.ellpis.KinKena.Objects.Playlist;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class BrowseRepository {
    public interface FirebaseFunctionOnCompleteDocumentTask {
        void onCompleteFunction(QuerySnapshot task);
    }
    public static void getBrowseLinks(FirebaseFunctionOnCompleteDocumentTask firebaseFunctionOnCompleteDocumentTask) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Browse").get().addOnSuccessListener(firebaseFunctionOnCompleteDocumentTask::onCompleteFunction);
    }
    public static void createBrowseLinks(List<BrowseItems> browseItemsList) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for(BrowseItems browseItems: browseItemsList){
            db.collection("Browse").document(browseItems.getTitle()).set(browseItems);
        }
    }

}
