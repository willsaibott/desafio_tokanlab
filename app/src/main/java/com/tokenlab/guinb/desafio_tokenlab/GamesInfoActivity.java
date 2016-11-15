package com.tokenlab.guinb.desafio_tokenlab;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;

import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GamesInfoActivity extends YouTubeBaseActivity implements AppCompatCallback,   YouTubePlayer.OnInitializedListener {

    private static final int RECOVERY_DIALOG_REQUEST = 1;

    private ImageView gameLogo;
    private TextView gameNameView;
    private TextView releaseDateView;
    private TextView platformView;
    private Toolbar toolbar;
    private RatingBar ratingBar;
    private YouTubePlayerView trailerView;
    private AppCompatDelegate delegate;


    private String gameName;
    private String trailerUrl;
    private String releaseDate;
    private String platforms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting Youtube Window Parameters
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        delegate = AppCompatDelegate.create(this, this);

        //we need to call the onCreate() of the AppCompatDelegate
        delegate.onCreate(savedInstanceState);

        //we use the delegate to inflate the layout
        delegate.setContentView(R.layout.activity_games_info);

        //Finally, let's add the Toolbar
        toolbar= (Toolbar) findViewById(R.id.gi_toolBar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setDisplayShowTitleEnabled(true);
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // setting View's Ids
        setViewId();

        // setting Info
        setInfo();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_game_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        finish();
    }


    private void setViewId() {
        gameLogo = (ImageView) findViewById(R.id.gi_gameLogo);
        gameNameView = (TextView) findViewById(R.id.gi_gameName);
        releaseDateView = (TextView) findViewById(R.id.gi_releaseDataView);
        platformView = (TextView) findViewById(R.id.gi_platformsView);
        ratingBar = (RatingBar) findViewById(R.id.gi_ratingBar);
        trailerView = (YouTubePlayerView) findViewById(R.id.gi_trailerView);

    }

    private void setInfo() {
        CustomApplication app = (CustomApplication) getApplicationContext();
        gameName    = getIntent().getExtras().getString(GamesListActivity.GAME_NAME);
        releaseDate = app.getGame(gameName).getRelease_date();
        platforms   = app.getConcatenatedPlatforms(gameName);
        trailerUrl  = app.getGame(gameName).getTrailer();

        trailerView.initialize(getResources().getString(R.string.google_api_key), this);
        setGameImage(app.getGameImage(gameName));
        gameNameView.setText(gameName);
        releaseDateView.setText(releaseDate);
        platformView.setText(platforms);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

            }
        });

        EditText ratingValue = (EditText) findViewById(R.id.gi_ratingValue);
        ratingValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0) {
                    float rate = Float.valueOf(s.toString());
                    rate = (rate > 10.0f) ? 5.0f : ((rate < 0.0f) ? 0.0f : rate / 2);
                    ratingBar.setRating(rate);
                }

            }
        });

        delegate.getSupportActionBar().setTitle(gameName);
    }

    private void setGameImage(Bitmap bitmap){
        gameLogo.setImageBitmap(bitmap);
        gameLogo.setScaleType(ImageView.ScaleType.FIT_XY);

    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        CustomApplication app = (CustomApplication) getApplicationContext();
        if (!wasRestored) {

            // loadVideo() will wait for user
            // Use cueVideo() method, if you don't want to play it automatically
            youTubePlayer.cueVideo(app.getYoutubeVideoId(trailerUrl));

            // Hiding player controls
            //youTubePlayer.setPlayerStyle(PlayerStyle.CHROMELESS);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format(
                    getString(R.string.error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(getResources().getString(R.string.google_api_key_youtube), this);
        }
    }

    private YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerView) findViewById(R.id.gi_trailerView);
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {

    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {

    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    private class DonloadDataTask extends AsyncTask<String, Integer, Double> {
        Bitmap image;

        @Override
        protected Double doInBackground(String... urlString) {
            try {
                getBitmapData(urlString[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }


        protected void onPostExecute(Double result) {
            if(image!=null)
                setGameImage(image);
            Toast.makeText(getApplicationContext(), "Download Finished with Success", Toast.LENGTH_LONG).show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }


        private void getBitmapData(String urlString) throws MalformedURLException {

            if (isOnline()) {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = null;
                try {
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    image = Bitmap.createScaledBitmap(bitmap, GamesListActivity.IMAGE_WIDTH, GamesListActivity.IMAGE_HEIGHT, false);
                    bitmap.recycle();

                } catch (IOException e) {
                    e.printStackTrace();
                    Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.undefined);
                    image = Bitmap.createScaledBitmap(bitmap, GamesListActivity.IMAGE_WIDTH, GamesListActivity.IMAGE_HEIGHT, false);
                    bitmap.recycle();
                } finally {
                    urlConnection.disconnect();
                }
            }
        }


    }

    public void onPause ()
    {
        super.onPause();
    }

}
