package com.getdive.facebookhelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.getdive.facebookhelper.facebook.FacebookHelper;
import com.getdive.facebookhelper.facebook.FacebookUtility;
import com.getdive.facebookhelper.utilities.DoneCallback;

public class MainActivity extends AppCompatActivity {
    private TextView facebookLogin;
    private TextView facebookLogout;
    private FacebookHelper facebookHelper;
    //context
    private MainActivity context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        facebookHelper=new FacebookHelper(context);
        linkToUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        if (FacebookSdk.isFacebookRequestCode(requestCode)) {
            facebookHelper.getFacebookUtility().onActivityResult(requestCode, responseCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initElements();
    }

    private void linkToUI() {
        facebookLogin = (TextView) findViewById(R.id.facebooklogin);
        facebookLogout = (TextView) findViewById(R.id.facebooklogout);
    }

    private void initElements() {
        initFacebook();
    }

    private void initFacebook() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken != null && !accessToken.isExpired()){
            facebookLogout.setVisibility(View.VISIBLE);
            facebookLogin.setVisibility(View.GONE);
        }
        facebookHelper.getFacebookUtility().setOnTokenChangeListener(new FacebookUtility.OnTokenChangeListener() {
            @Override
            public boolean onTokenUpdate(AccessToken oldAccessToken, AccessToken newAccessToken) {
                if (newAccessToken == null) {
                    facebookLogin.setVisibility(View.VISIBLE);
                    facebookLogout.setVisibility(View.GONE);
                } else {
                    facebookLogout.setVisibility(View.VISIBLE);
                    facebookLogin.setVisibility(View.GONE);
                }
                return false;
            }
        });
        facebookHelper.getFacebookUtility().startTrackingToken();
    }

    public void loginFacebook(View view) {
        DoneCallback<String> doneCallback=new DoneCallback<String>() {
            @Override
            public void done(final String result) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.equals("FAIL")) {
                        }
                    }
                });
            }
        };
        facebookHelper.login(doneCallback);
    }

    public void logoutFacebook(View v) {
        facebookHelper.facebookLogout();
    }

}
