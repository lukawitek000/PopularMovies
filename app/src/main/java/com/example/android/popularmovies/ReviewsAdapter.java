package com.example.android.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder> {

    private static Review[] reviews;

    @NonNull
    @Override
    public ReviewsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.reviews_list_item, parent, false);

        return new ReviewsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewsAdapterViewHolder holder, int position) {
        Review singleReview = reviews[position];
        holder.content.setText(singleReview.getContent());
        holder.author.setText(singleReview.getAuthor());


    }

    @Override
    public int getItemCount() {
        if(reviews != null)
            return reviews.length;
        return 0;
    }

    public void setReviews(Review[] reviews) {
        this.reviews = reviews;
    }



    class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder{

        TextView author;
        TextView content;


        public ReviewsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.authorValue);
            content = itemView.findViewById(R.id.content);

        }
    }



}