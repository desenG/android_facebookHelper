package com.getdive.facebookhelper.facebook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.getdive.facebookhelper.utilities.DoneCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by desenguo on 2015-11-27.
 */
public class FacebookUtility {

    //TAG
    private static String TAG = "FacebookUtility";

    private LoginManager fbLoginManager;
    private CallbackManager fbCallbackManager;
    private AccessTokenTracker fbAccessTokenTracker;

    //Activity
    private Activity activity;

    private OnLoginListener onLoginListener;
    private OnTokenChangeListener onTokenChangeListener;

    public void setOnLoginListener(OnLoginListener onLoginListener) {
        this.onLoginListener = onLoginListener;
    }

    public void setOnTokenChangeListener(OnTokenChangeListener onTokenChangeListener) {
        this.onTokenChangeListener = onTokenChangeListener;
    }

    public static interface OnLoginListener {
        boolean onLoginFail();
        boolean onLoginSucceed(HashMap<String, String> facebookInfo);
    }

    public static interface OnTokenChangeListener {
        boolean onTokenUpdate(AccessToken oldAccessToken, AccessToken newAccessToken);
    }

    public FacebookUtility(final Activity activity) {
        this.activity = activity;
        fbLoginManager = LoginManager.getInstance();
        fbCallbackManager = CallbackManager.Factory.create();
        fbLoginManager.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken fbAccessToken = loginResult.getAccessToken();
                if (fbAccessToken != null)
                    loginFBProfileOfToken(fbAccessToken);
                else
                    loginFailed();
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException e) {
                e.printStackTrace();
                if (activity != null) {
                    new AlertDialog.Builder(activity)
                            .setTitle("Facebook")
                            .setMessage("Facebook connection problem. Please try again.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                }
            }
        });
    }

    public void startTrackingToken() {
        fbAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                onTokenChangeListener.onTokenUpdate(oldAccessToken,newAccessToken);
            }
        };
    }

    public void stopTrackingToken() {
        fbAccessTokenTracker.stopTracking();
        fbAccessTokenTracker = null;
    }

    private void loginFailed() {
        if (onLoginListener != null) {
            //todo might execute this in uithread
            onLoginListener.onLoginFail();
        }
    }

    private void loginSucceed(HashMap<String, String> facebookInfo){
        if (onLoginListener != null) {
            //todo might execute this in uithread
            onLoginListener.onLoginSucceed(facebookInfo);
        }
    }

    private void loginFBProfileOfToken(AccessToken fbAccessToken){
        GraphRequest meRequest= GraphRequest.newMeRequest(
                fbAccessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        HashMap<String,String> facebookInfo;

                        if(response.getError() == null){
                            String id = null;
                            try{
                                id = object.getString("id");
                            }catch (JSONException e){
                                e.printStackTrace();
                            }

                            if(id == null){
                                loginFailed();
                                return;
                            }
                            facebookInfo = new HashMap<>();
                            facebookInfo.put("id",id);

                            String name;
                            try{
                                name = object.getString("name");
                            }catch (JSONException e){
                                e.printStackTrace();
                                name = "";
                            }
                            facebookInfo.put("name",name);

                            String email = null;
                            try{
                                email = object.getString("email");
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                            facebookInfo.put("email",email);

                            String gender;
                            try{
                                gender = object.getString("gender");
                            }catch (JSONException e){
                                e.printStackTrace();
                                gender = "";
                            }
                            facebookInfo.put("gender", gender);
                            if(facebookInfo != null){
                                loginSucceed(facebookInfo);
                            }
                            else {
                                loginFailed();
                            }
                        }
                        else {
                            loginFailed();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,email,gender");    //TODO picture maybe
        meRequest.setParameters(parameters);
        meRequest.executeAsync();
    }

    public void logout()
    {
        fbLoginManager.logOut();
    }

    public void login() {
        AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();
        if(fbAccessToken != null && !fbAccessToken.isExpired()) {
            loginFBProfileOfToken(fbAccessToken);
        }
        else {
            fbLoginManager.logInWithReadPermissions(activity, Arrays.asList("public_profile", "email"));
        }
    }

    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        fbCallbackManager.onActivityResult(requestCode, responseCode, intent);
    }

    public void requestFaceboookFriendList(final DoneCallback<JSONArray> doneCallback) {
        /* make the API call */
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        /* handle the result */
                        Log.i(TAG, "Facebook list response: " + response.getRawResponse());
                        try {
                            doneCallback.done((JSONArray) response.getJSONObject().get("data"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

} //end