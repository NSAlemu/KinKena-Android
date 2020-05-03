package com.ellpis.KinKena.Repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class BrowseRepository {
    public interface FirebaseFunctionOnCompleteDocumentTask {
        void onCompleteFunction(Task<QuerySnapshot> task);
    }
    public static void getBrowseLinks(FirebaseFunctionOnCompleteDocumentTask firebaseFunctionOnCompleteDocumentTask) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Browse").get().addOnCompleteListener(firebaseFunctionOnCompleteDocumentTask::onCompleteFunction);

    }

}
