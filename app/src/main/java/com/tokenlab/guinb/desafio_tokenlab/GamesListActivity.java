package com.tokenlab.guinb.desafio_tokenlab;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.app.SearchManager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class GamesListActivity extends AppCompatActivity {

    final static String GAME_LIST_URL = "https://dl.dropboxusercontent.com/u/34048947/games";
    final static String GAME_NAME = "game_name";

    static int ICON_HEIGHT;
    static int ICON_WIDTH;
    static int IMAGE_WIDTH;
    static int IMAGE_HEIGHT;

    private ProgressBar loadingBar;
    private SearchView searchBar;
    private ListView gamesListView;
    private ListView platformListView;
    private Toolbar menuToolBar;
    private DrawerLayout platformDrawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private TextView percentageView;

    private JsonData jsonData;
    private HashMap<String, Bitmap> GameIcons  = new HashMap<>();
    private HashMap<String, Bitmap> GameImages = new HashMap<>();
    private CustomApplication app;
    private GamesListAdapter gameListAdapter = null;
    private PlatformListAdapter platformListAdapter = null;


    //private List<>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_list);
        app = (CustomApplication) getApplicationContext();

        ICON_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
        ICON_WIDTH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
        IMAGE_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());
        IMAGE_WIDTH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());

        menuToolBar = (Toolbar) findViewById(R.id.gl_menuToolBar);
        setSupportActionBar(menuToolBar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);

        // setting View Id's and click listeners
        setViewId();
        // download gamelist from url
        getGameList();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_games_list, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.searchItem);

        // setting SearchView SearchBar with a searchManager
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchBar = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchBar.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchBar.setSubmitButtonEnabled(true);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (gameListAdapter!=null)
                    gameListAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (gameListAdapter!=null)
                    gameListAdapter.getFilter().filter(newText);
                return true;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) { // going out of SearchView
                if (gameListAdapter!=null)
                    gameListAdapter.getFilter().filter("");
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reloadItem:
                getGameList();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, "Searching by: "+ query, Toast.LENGTH_SHORT).show();

        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String uri = intent.getDataString();
            Toast.makeText(this, "Suggestion: "+ uri, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){

                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                }).create().show();
    }

    // Setting View Ids
    private void setViewId() {
        loadingBar = (ProgressBar)findViewById(R.id.gl_loadingBar);
        percentageView = (TextView) findViewById(R.id.gl_percentageView);
        gamesListView = (ListView) findViewById(R.id.gl_listView);
    }

    // setting game ListView with gameListAdapter
    private void setGamesListView(){
        gameListAdapter = new GamesListAdapter(getApplicationContext(), jsonData.getGames());

        gamesListView.setAdapter(gameListAdapter);
        gamesListView.setTextFilterEnabled(false);

        // showing progress percentage and hiding the game ListView while it is not ready to show
        percentageView.setVisibility(View.GONE);
        gamesListView.setVisibility(View.VISIBLE);
        // set up click listener
        gamesListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position < jsonData.getGames().size()) {
                    handleGamesListItemClick((Games)gameListAdapter.getItem(position));
                }
            }
        });
    }

    // A simle Comparation Class to use in the sort algorithm
    class CompareByName implements Comparator<Games>{
        @Override
        public int compare(Games o1, Games o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    // start the Game Info Activity of the game chosen
    private void handleGamesListItemClick(Games game) {
        callNextActivity(game);
    }

    private void getGameList() {
        boolean isConnectedToInternet = isOnline();

        if(!isConnectedToInternet){
            // out of connection
            Toast.makeText(this, "Device is not connected to Internet./nPlease Check your Internet Connection.", Toast.LENGTH_LONG).show();
        }else{ // start games list download
            gamesListView.setVisibility(View.GONE);
            loadingBar.setVisibility(View.VISIBLE);
            percentageView.setVisibility(View.VISIBLE);
            new DonloadDataTask().execute(GAME_LIST_URL);
        }
    }

    // return status of Internet Connection
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    // calling Game Info Activity of a chosen game
    protected void callNextActivity(Games game){
        Intent nextActivity = new Intent(getApplicationContext(), GamesInfoActivity.class);
        nextActivity.putExtra(GAME_NAME, game.getName());
        startActivity(nextActivity);
    }

    public JsonData getJsonData() {
        return jsonData;
    }

    public void setJsonData(JsonData jsonData) {
        this.jsonData = jsonData;
    }

    // Doanload of the meta-data
    private class DonloadDataTask extends AsyncTask<String, Integer, Double> {

        @Override
        protected Double doInBackground(String... urlString) {
            try {
                getJsonData(urlString[0]);
                if (jsonData!=null && jsonData.getGames().size() > 0)
                    getBitmapData();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }

        // when download finishes, store data at my CustomApplication app and show game list
        protected void onPostExecute(Double result){
            loadingBar.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Download Finished with Success", Toast.LENGTH_LONG).show();
            app.setGameIcon(GameIcons);
            app.setGameImages(GameImages);
            app.setJsonData(jsonData);
            setGamesListView();
        }

        // Show Download Progress on loadingBar and the percentage of the Donload into the TexView percentageView
        protected void onProgressUpdate(Integer... progress){
            String percetage = new String("");
            percetage = String.valueOf(progress[0]) + "%";
            loadingBar.setProgress(progress[0]);
            percentageView.setText(percetage);
        }

        // Request the json file
        public void getJsonData(String urlString) throws MalformedURLException {

            URL url = new URL(urlString);
            HttpURLConnection urlConnection = null;

            publishProgress(0); // reset the progress
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                // storing Json Data Structure
                setJsonData(JsonDataFactory.getData(inputStream));
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(e.toString(), e.getMessage());
            } finally {
                urlConnection.disconnect();
                if (jsonData!=null)
                    Collections.sort(jsonData.getGames(), new CompareByName() );
                else{
                    getJsonData(urlString);
                }
                publishProgress(100/(jsonData.getGames().size()+1)); // updating progress after download the json file
            }
        }

        private void getBitmapData() throws MalformedURLException {
            if (!GameIcons.isEmpty())  GameIcons.clear();
            if (!GameImages.isEmpty()) GameImages.clear();
            int i = 1;

            // Download all the Games' Logos
            for (Games game : jsonData.getGames()) {
                if (isOnline()) {
                    URL url = new URL(game.getImage());
                    HttpURLConnection urlConnection = null;
                    try { // try Download the original Game Logo
                        urlConnection =  (HttpURLConnection) url.openConnection();
                        InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        Bitmap image = Bitmap.createScaledBitmap(bitmap, IMAGE_WIDTH, IMAGE_HEIGHT, false);
                        Bitmap icon = createSquareBitmap(bitmap);
                        GameIcons.put(game.getName(), icon);
                        GameImages.put(game.getName(), image);
                        bitmap.recycle();
                    }
                    catch (IOException e) { // if you cannot, use a default image
                        e.printStackTrace();
                        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.undefined);
                        Bitmap image = Bitmap.createScaledBitmap(bitmap, IMAGE_WIDTH, IMAGE_HEIGHT, false);
                        Bitmap icon = createSquareBitmap(bitmap);
                        GameIcons.put(game.getName(), icon);
                        GameImages.put(game.getName(), image);
                        bitmap.recycle();
                    }finally {
                        urlConnection.disconnect();
                    }
                    i++;
                    publishProgress(100*i/(jsonData.getGames().size()+1)); // Showing download progress
                }
            }
        }

        // this function creates an icon from the original Game logo.
        private Bitmap createSquareBitmap(Bitmap bitmap) {

            Bitmap resizedBitmap = null;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix m = new Matrix();
            float width_scale = (float)ICON_WIDTH/width;
            float height_scale = (float)ICON_HEIGHT/height;

            m.postScale(width_scale, height_scale);

            if (width >= height){
                resizedBitmap = Bitmap.createBitmap(bitmap, width/2 - height/4, height/4, height/2, height/2, m, false);

            }else{
                resizedBitmap = Bitmap.createBitmap(bitmap, width/4, height/2 - width/4, width/2, width/2, m, false);
            }
            return resizedBitmap;
        }

    }


}
