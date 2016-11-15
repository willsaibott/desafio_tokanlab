package com.tokenlab.guinb.desafio_tokenlab;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by guinb on 11/13/2016.
 */

public class PlatformListAdapter extends BaseAdapter {

    ArrayList<String> platforms = new ArrayList<>();
    ArrayList<Games> games;
    CustomApplication app;

    public PlatformListAdapter(Context context){
        app = (CustomApplication) context;
        games = app.getJsonData().getGames();
        setPlatforms();
    }

    @Override
    public int getCount() {
        return platforms.size();
    }

    @Override
    public Object getItem(int position) {
        return platforms.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;


        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) app.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.platforms_list_row_layout, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) view.findViewById(R.id.glr_platformNameView);
            holder.name.setText(platforms.get(position));
            view.setTag(holder);
        } else {
            // get view holder back
            holder = (ViewHolder) view.getTag();
        }

        view.setBackgroundColor(Color.WHITE);
        return view;
    }

    private void setPlatforms(){
        for (Games game: games){
            for (String str:game.getPlatforms()){
                if (!platforms.contains(str))
                    platforms.add(str);
            }
        }
    }


    static class ViewHolder{
        TextView name;
    }
}
