package com.example.popularmovies.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.popularmovies.Movie;
import com.example.popularmovies.R;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class MovieReviewAdapter extends RecyclerView.Adapter<MovieReviewAdapter.ViewHolder> {
    private Movie[] movies;
    private Context context;

    public MovieReviewAdapter(Movie[] movies, Context context) {
        this.movies = movies;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_review_item, parent, false);
        return new ViewHolder(constraintLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.tvAuthor.setText(String.valueOf(movies[position].getReviewAuthor()));
        holder.tvReview.setText(String.valueOf(movies[position].getReviewContents()));
        holder.btnFullReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(movies[position].getReviewUrl()));
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (movies == null || movies.length == 0) {
            return -1;
        }
        return movies.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor;
        TextView tvReview;
        Button btnFullReview;

        public ViewHolder(ConstraintLayout itemView) {
            super(itemView);

            tvAuthor = (TextView) itemView.findViewById(R.id.tv_Author);
            tvReview = (TextView) itemView.findViewById(R.id.tv_Review);
            btnFullReview = (Button) itemView.findViewById(R.id.btn_FullReview);
        }
    }
}
