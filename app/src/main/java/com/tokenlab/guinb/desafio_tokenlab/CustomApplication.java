package com.tokenlab.guinb.desafio_tokenlab;

import android.app.Application;
import android.graphics.Bitmap;;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by guinb on 11/13/2016.
 *
 * Custom application class that store the most important information
 */

public class CustomApplication extends Application {
    private JsonData jsonData;
    private HashMap<String, Bitmap> gameIcon;
    private HashMap<String, Bitmap> gameImages;
    private GoogleSignInAccount account;

    public JsonData getJsonData() {
        return jsonData;
    }

    public void setJsonData(JsonData jsonData) {
        this.jsonData = jsonData;
    }

    public Bitmap getIconBitmap(String gameName){
        return (gameIcon.get(gameName));
    }

    public void setGameIcon(HashMap<String, Bitmap> gameIcon) {
        this.gameIcon = gameIcon;
    }

    public Games getGame (String gameName){
        for (Games game:jsonData.getGames())
            if (game.getName().equals(gameName))
                return game;
        return null;
    }

    public String getConcatenatedPlatforms(String gameName){
        String str = new String("");
        Games game = getGame(gameName);
        if (game!=null){
            str += game.getPlatforms().get(0);
            for(int i = 1; i < game.getPlatforms().size(); i++){
                str += ", " + game.getPlatforms().get(i);
            }
        }
        return str;
    }

    public Bitmap getGameImage(String gameName) {
        return gameImages.get(gameName) ;
    }

    public void setGameImages(HashMap<String, Bitmap> gameImages) {
        this.gameImages = gameImages;
    }

    String getYoutubeVideoId(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);

        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    public GoogleSignInAccount getAccount() {
        return account;
    }

    public void setAccount(GoogleSignInAccount account) {
        this.account = account;
    }
}
