package com.getdive.facebookhelper.facebook;

import android.app.Activity;

import com.facebook.AccessToken;
import com.getdive.facebookhelper.utilities.DoneCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by desenguo on 2015-11-27.
 */
public class FacebookHelper {

    FacebookUtility facebookUtility;
    Activity activity;
    Boolean flagFirstTime = false;

    public FacebookHelper(Activity activity) {
        facebookUtility = new FacebookUtility(activity);
        this.activity = activity;
    }

    public FacebookUtility getFacebookUtility() {
        return facebookUtility;
    }

    public void login(final DoneCallback<String> doneCallback) {
        facebookUtility.setOnLoginListener(new FacebookUtility.OnLoginListener() {
            @Override
            public boolean onLoginFail() {
                return false;
            }
            @Override
            public boolean onLoginSucceed(final HashMap<String, String> facebookInfo) {

                return false;
            }
        });
        facebookUtility.login();
    }

    public void facebookLogout() {
        facebookUtility.logout();
    }

    public void getFacebookFriendList(final DoneCallback<ArrayList<FBContact>> getFacebookFriendsSucceed) {
        DoneCallback <String> loginSucceedCallback = new DoneCallback<String>() {
            @Override
            public void done(String result) {
                DoneCallback<JSONArray> requestFacebookFriendSucceed = new DoneCallback<JSONArray>() {
                    @Override
                    public void done(JSONArray friendDataList) {
                        ArrayList<FBContact> friendList = new ArrayList<>();
                        FBContact facebookAccountInfo;
                        for (int i = 0; i < friendDataList.length(); i++) {
                            try {
                                facebookAccountInfo = new FBContact();
                                facebookAccountInfo.facebookFriendID = (String)friendDataList.getJSONObject(i).get("id");
                                facebookAccountInfo.name = (String) friendDataList.getJSONObject(i).get("name");
                                friendList.add(facebookAccountInfo);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        getFacebookFriendsSucceed.done(friendList);
                    }
                };
                facebookUtility.requestFaceboookFriendList(requestFacebookFriendSucceed);
            }
        };
        if(!isFacebookLoggedIn()){
            login(loginSucceedCallback);
        }
        else {
            loginSucceedCallback.done("COMPLETED");
        }
    }

    public boolean isFacebookLoggedIn() {
        AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();
        return (fbAccessToken!=null && !fbAccessToken.isExpired());
    }
} //end