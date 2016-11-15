package com.tokenlab.guinb.desafio_tokenlab;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;


public class StartActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    final static int ANIMATION_TIME = 1500;
    final static int RC_SIGN_IN = 007;

    private GoogleApiClient mGoogleApiClient;

    private ImageView tokenLogo;
    private SignInButton signInButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Installing a Shortcut
        installShortcut();

        // Setting Logo Animation
        setInicialAnimation();

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        // Setting Goolge plus SignIn Options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Setting Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton.setSize(SignInButton.SIZE_STANDARD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
    }

    private void signIn() {
        // Always ask which google account the app should use;
        mGoogleApiClient.clearDefaultAccountAndReconnect();
        // Start Google Login intent
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Istalling shortcut if the app is not installed
    private void installShortcut() {

        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAppInstalled = appPreferences.getBoolean("isAppInstalled", false);

        // if the app is not installed, do it
        if(!isAppInstalled){

            Intent HomeScreenShortCut= new Intent(getApplicationContext(), StartActivity.class);

            HomeScreenShortCut.setAction(Intent.ACTION_MAIN);
            HomeScreenShortCut.putExtra("duplicate", false);

            // setting intent for create new shortcut
            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, HomeScreenShortCut);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.app_icon));
            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            getApplicationContext().sendBroadcast(addIntent);


            //Make preference true
            SharedPreferences.Editor editor = appPreferences.edit();
            editor.putBoolean("isAppInstalled", true);
            editor.putString("appName", getString(R.string.app_name));
            editor.commit();
        }
    }

    // Setting start animation
    private void setInicialAnimation() {

        final AnimatorSet mAnimationSet = new AnimatorSet();
        ImageView logotypeImage = (ImageView)findViewById(R.id.am_token_logo);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(logotypeImage, "alpha", 0f, 1f);

        // set Animation Listener
        mAnimationSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                // User is able to do the login now
                signInButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        // setAnimation Duration
        fadeIn.setDuration(ANIMATION_TIME);

        // starting Animation
        mAnimationSet.play(fadeIn);
        mAnimationSet.start();
    }

    // function that call the Game List Activity
    public void callNextActivity(){
        Intent nextActivity = new Intent(getApplicationContext(), GamesListActivity.class);
        startActivity(nextActivity);
        finish();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // defining App's User
            CustomApplication app = (CustomApplication)getApplicationContext();
            GoogleSignInAccount account = result.getSignInAccount();
            app.setUser(account.getDisplayName());

            // Loggin successfull msg
            String msg = getString(R.string.login_successfull) + account.getEmail();
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

            // Calling next activity
            callNextActivity();
        }
    }

}
