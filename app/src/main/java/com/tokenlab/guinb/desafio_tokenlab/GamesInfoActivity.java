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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class GamesInfoActivity extends YouTubeBaseActivity implements AppCompatCallback,   YouTubePlayer.OnInitializedListener {

    private static final int RECOVERY_DIALOG_REQUEST = 1;
    public static final int MAX_STARS = 5;

    private ImageView gameLogo;
    private TextView gameNameView;
    private TextView releaseDateView;
    private TextView platformView;
    private Toolbar toolbar;
    private RatingBar ratingBar;
    private YouTubePlayerView trailerView;
    private AppCompatDelegate delegate;
    private ListView reviewsListView;
    private EditText reviewRateEdit;
    private EditText commentEdit;
    private ImageButton sendButton;
    private ViewGroup footer;
    private ViewGroup header;

    private String gameName;
    private String trailerUrl;
    private String releaseDate;
    private String platforms;

    private ArrayList<Reviews> reviews = new ArrayList<>();
    private HashMap<String, Bitmap> userPhotos  = new HashMap<>();
    private ReviewsListAdapter reviewListAdapter;
    SqlDataModel db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // opening connection
        db = new SqlDataModel(getApplicationContext());

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

        // read database
        readDataBase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_game_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // on Back button pressed, finishes this activity
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (db!=null)
            db.close();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        CustomApplication app = (CustomApplication) getApplicationContext();
        if (!wasRestored) {
            // waiting for user to press play
            youTubePlayer.cueVideo(app.getYoutubeVideoId(trailerUrl));
        }
    }

    // on error initialization, show error message
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
    public void onSupportActionModeStarted(ActionMode mode) {}

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {}

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {return null;}

    // this function gets the View Ids and add the header and the footer into the review List
    private void setViewId() {

        // setting the list View adding the footer and the header
        reviewsListView = (ListView) findViewById(R.id.gi_reviewListView);
        LayoutInflater inflaterHeader = getLayoutInflater();
        LayoutInflater inflaterFooter = getLayoutInflater();
        header = (ViewGroup) inflaterHeader.inflate(R.layout.game_info_content_header, null);
        footer = (ViewGroup) inflaterFooter.inflate(R.layout.game_info_content_footer, null);
        reviewsListView.addHeaderView(header);
        reviewsListView.addHeaderView(footer);
        reviewsListView.setScrollingCacheEnabled(false);
        reviewsListView.setFriction(ViewConfiguration.getScrollFriction()*100);

        gameLogo = (ImageView) findViewById(R.id.gi_gameLogo);
        gameNameView = (TextView) findViewById(R.id.gi_gameName);
        releaseDateView = (TextView) findViewById(R.id.gi_releaseDataView);
        platformView = (TextView) findViewById(R.id.gi_platformsView);
        ratingBar = (RatingBar) findViewById(R.id.gi_ratingBar);
        trailerView = (YouTubePlayerView) findViewById(R.id.gi_trailerView);
        reviewRateEdit = (EditText) findViewById(R.id.gi_reviewRateEdit);
        commentEdit = (EditText) findViewById(R.id.gi_CommentEdit);
        sendButton = (ImageButton) findViewById(R.id.gi_sendButton);
    }

    // Setting Info in Intern Views
    private void setInfo() {

        // getting stored data
        CustomApplication app = (CustomApplication) getApplicationContext();
        gameName    = getIntent().getExtras().getString(GamesListActivity.GAME_NAME);
        releaseDate = app.getGame(gameName).getRelease_date();
        platforms   = app.getConcatenatedPlatforms(gameName);
        trailerUrl  = app.getGame(gameName).getTrailer();

        // try to initialize youtube API
        trailerView.initialize(getResources().getString(R.string.google_api_key), this);

        // Update Game Logo image and the game information
        setGameImage(app.getGameImage(gameName));
        gameNameView.setText(gameName);
        releaseDateView.setText(releaseDate);
        platformView.setText(platforms);

        // setting send button click listener,
        // this button , when pressed, add a review to the database and update the listView
        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                CustomApplication app = (CustomApplication) getApplicationContext();
                Reviews review = new Reviews();

                // getting current default timezone
                TimeZone tz = TimeZone.getDefault();
                Calendar currentDate = Calendar.getInstance(tz); //Get the current date
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy HH:mm"); //format it as per your requirement
                String dateNow = formatter.format(currentDate.getTime()); // store the formatted string with the datetime

                // setting review atributes
                review.setUserName(app.getAccount().getDisplayName());
                review.setUserPhotoUrl(app.getAccount().getPhotoUrl().toString());
                review.setComment(commentEdit.getText().toString());
                review.setDate(dateNow);

                 // if the user gives a valid rate. set this new rate
                if (reviewRateEdit.getText().length()>0 && Integer.valueOf(reviewRateEdit.getText().toString()) < 5)
                    review.setRate(Integer.valueOf(reviewRateEdit.getText().toString()));
                else // if the user forgot to rate the game, or the user gave a invalid rate, set the rate as the maximun
                    review.setRate(MAX_STARS);

                reviews.add(review);
                new DonloadDataTask().execute(gameName);

                // store data into the database
                db.insertIntoTable(review, gameName, reviews.size());
            }
        });

        // setting the title as the game Name
        delegate.getSupportActionBar().setTitle(gameName);
    }

    // this function read the databse and start the download of the url images of the users
    private void readDataBase() {
        reviews = db.getAllReviews(gameName);
        new DonloadDataTask().execute(gameName);
    }

    // setting the game logo
    private void setGameImage(Bitmap bitmap){
        gameLogo.setImageBitmap(bitmap);
        gameLogo.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void onPause ()
    {
        super.onPause();
    }

    // this function sets up the necessary information of the listView
    private void setReviewList() {
        reviewListAdapter = new ReviewsListAdapter(getApplicationContext(), reviews, userPhotos);
        reviewsListView.setAdapter(reviewListAdapter);
        reviewsListView.setTextFilterEnabled(false);

        float sumRate = 0, mediaRate;
        for (Reviews review: reviews)
            sumRate += review.getRate();
        mediaRate = (sumRate)/(reviews.size());

        // rating is the average of the rate values
        ratingBar.setRating(mediaRate);
    }

    private class DonloadDataTask extends AsyncTask<String, Integer, Double> {

        @Override
        protected Double doInBackground(String... gameName) {
            try {
                if (reviews!=null)
                    getBitmapData();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void getReviewsData(String s) {}

        protected void onPostExecute(Double result){
            setReviewList();
        }

        protected void onProgressUpdate(Integer... progress){}

        private void getBitmapData() throws MalformedURLException {
            int IMAGE_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
            int IMAGE_WIDTH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());

            if (!userPhotos.isEmpty()) userPhotos.clear();

            for (Reviews review: reviews) {
                if (isOnline()) { // url connection attempt
                    URL url = new URL(review.getUserPhotoUrl());
                    HttpURLConnection urlConnection = null;
                    try { // try to download the User photos
                        urlConnection =  (HttpURLConnection) url.openConnection();
                        InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        Bitmap image = Bitmap.createScaledBitmap(bitmap, IMAGE_WIDTH, IMAGE_HEIGHT, false);
                        userPhotos.put(review.getUserName(), image);
                        bitmap.recycle();
                    }
                    catch (IOException e) { // on error, use default image
                        e.printStackTrace();
                        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.undefined);
                        Bitmap image = Bitmap.createScaledBitmap(bitmap, IMAGE_WIDTH, IMAGE_HEIGHT, false);
                        userPhotos.put(review.getUserName(), image);
                        bitmap.recycle();
                    }finally {
                        urlConnection.disconnect();
                    }
                }
            }
        }
    }
}
