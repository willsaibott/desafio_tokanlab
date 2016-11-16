package com.tokenlab.guinb.desafio_tokenlab;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by guinb on 11/15/2016.
 *
 * Review List Adapter
 *
 *
 */

public class ReviewsListAdapter extends BaseAdapter {

    ArrayList<Reviews> reviews = new ArrayList<>();
    CustomApplication app;
    HashMap<String, Bitmap> userPhotos;

    public ReviewsListAdapter(Context context, ArrayList<Reviews> gameReview, HashMap<String, Bitmap> photos){
        app = (CustomApplication) context;
        reviews = gameReview;
        userPhotos = photos;
    }

    @Override
    public int getCount() {
        return reviews.size();
    }

    @Override
    public Object getItem(int position) {
        return reviews.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;

        // Completing the row Data
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) app.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.review_list_row_layout, parent, false);
            holder = new ViewHolder();

            holder.userName = (TextView) view.findViewById(R.id.gi_userName);
            holder.userPhoto = (ImageView) view.findViewById(R.id.gi_userIcon);
            holder.reviewLabel = (TextView) view.findViewById(R.id.gi_reviewLabel);
            holder.reviewRate = (RatingBar) view.findViewById(R.id.gi_rate);
            holder.commentView = (TextView) view.findViewById(R.id.gi_reviewCommentView);
            holder.date = (TextView) view.findViewById(R.id.gi_datetime);

            Reviews review = reviews.get(position);
            holder.userName.setText(review.getUserName());
            holder.commentView.setText(review.getComment());
            holder.reviewRate.setRating(review.getRate());
            holder.date.setText(review.getDate());
            view.setTag(holder);
        } else {
            // get view holder back
            holder = (ViewHolder) view.getTag();
        }
        holder.userPhoto.setImageBitmap(userPhotos.get(reviews.get(position).getUserName()));
        view.setBackgroundColor(Color.WHITE);
        return view;
    }

    // Class that contains the Children Views of a Review Row.
    class ViewHolder{

        ImageView userPhoto;
        TextView userName;
        TextView reviewLabel;
        RatingBar reviewRate;
        TextView commentView;
        TextView date;
    }
}
