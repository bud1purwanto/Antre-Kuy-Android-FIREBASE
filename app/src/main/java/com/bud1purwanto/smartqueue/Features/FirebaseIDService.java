package com.bud1purwanto.smartqueue.Features;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by root on 1/27/18.
 */

public class FirebaseIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseIDService";
    private DatabaseReference root;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseLogin;

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
//        firebaseAuth = FirebaseAuth.getInstance();
//        firebaseLogin = firebaseAuth.getCurrentUser();
//        root = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseLogin.getUid());
//
//        Map<String,Object> map2 = new HashMap<String, Object>();
//        map2.put("token", token);
//        root.updateChildren(map2);
    }
}