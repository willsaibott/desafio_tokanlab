package com.tokenlab.guinb.desafio_tokenlab;

import java.util.ArrayList;

/**
 * Created by guinb on 11/16/2016.
 *
 * Class that contains the Games Data according to the json input file
 */

public class Games {

    private String name;
    private String image;
    private String release_date;
    private String trailer;
    private ArrayList<String> platforms;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public ArrayList<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(ArrayList<String> platforms) {
        this.platforms = platforms;
    }
}