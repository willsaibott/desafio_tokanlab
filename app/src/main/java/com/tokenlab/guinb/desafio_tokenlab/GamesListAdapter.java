package com.tokenlab.guinb.desafio_tokenlab;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by guinb on 11/11/2016.
 */

public class GamesListAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private GamesFilter gamesFilter;
    private Typeface typeface;
    private ArrayList<Games> gamesList;
    private ArrayList<Games> filteredList;
    private CustomApplication app;

    public GamesListAdapter(Context context, ArrayList<Games> gamesList) {
        this.context = context;
        this.gamesList = gamesList;
        this.filteredList = gamesList;
        app = (CustomApplication) context;
        getFilter();
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public Object getItem(int i) {
        return filteredList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        final ViewHolder holder;
        final Games game = (Games) getItem(position);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.games_list_row_layout_name, parent, false);
            holder = new ViewHolder();
            holder.gameIcon = (ImageView) view.findViewById(R.id.glr_gameIcon);
            holder.gamePlatform = (TextView) view.findViewById(R.id.glr_gamePlatform);
            holder.gameNameLayout = (LinearLayout) view.findViewById(R.id.glr_linearLayout);
            holder.gameName = (TextView) view.findViewById(R.id.glr_gameName);
            view.setTag(holder);
        } else {
            // get view holder back
            holder = (ViewHolder) view.getTag();
        }

        // bind text with view holder content view for efficient use
        holder.gameName.setText(game.getName());
        holder.gameIcon.setImageBitmap(app.getIconBitmap(game.getName()));
        holder.gameIcon.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.gamePlatform.setText(app.getConcatenatedPlatforms(game.getName()));
        view.setBackgroundColor(Color.WHITE);
        return view;
    }

    @Override
    public Filter getFilter() {
        if (gamesFilter == null) {
            gamesFilter = new GamesFilter();
        }
        return gamesFilter;
    }

    public ArrayList<Games> getGamesList(){
        return filteredList;
    }

    // Class that contains the Children Views of a row int the game ListView
    static class ViewHolder {
        ImageView gameIcon;
        LinearLayout gameNameLayout;
        TextView  gameName;
        TextView  gamePlatform;
    }

    // Filter Class that will do search and filtrate the original game list when matches with the key
    private class GamesFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence filteredString) {
            FilterResults filterResults = new FilterResults();
            if (filteredString!=null && filteredString.length()>0) {
                ArrayList<Games> gamesToShow = new ArrayList<Games>();

                // search content in Games list
                for (Games game : gamesList) {
                    if (game.getName().toUpperCase().contains(filteredString.toString().toUpperCase()))
                        gamesToShow.add(game);
                    else {
                        for (String str : game.getPlatforms())
                            if (str.toUpperCase().contains(filteredString.toString().toUpperCase())) {
                                gamesToShow.add(game);
                                break;
                            }
                    }
                }
                filterResults.count = gamesToShow.size();
                filterResults.values = gamesToShow;
            } else {
                filterResults.count = gamesList.size();
                filterResults.values = gamesList;
            }
            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<Games>) results.values;
            notifyDataSetChanged();
        }
    }
}
