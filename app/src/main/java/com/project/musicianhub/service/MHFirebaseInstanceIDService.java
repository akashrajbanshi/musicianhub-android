package com.project.musicianhub.service;

import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Jerry on 3/2/2017.
 */

public class MHFirebaseInstanceIDService extends FirebaseInstanceIdService {

    LoginServiceImpl loginService = new LoginServiceImpl();
    private String refreshedToken;

    @Override
    public void onTokenRefresh() {

    }

    private void getRefreshedTokenId(String refreshedToken) {
    }


}
