package com.example.popularmovies.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.popularmovies.Movie;
import com.example.popularmovies.R;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.ViewHolder> {

    private Context mContext;
    private Movie[] mMovies;
    private OnItemClickListener itemClickListener;

    public MoviesAdapter(Movie[] mMovies, Context mContext, OnItemClickListener itemClickListener) {
        this.mMovies = mMovies;
        if (mContext == null) {
            try{
                Thread.sleep(1000);
            } catch(InterruptedException exception){
                exception.printStackTrace();
            }
        } else {
            this.mContext = mContext;
        }
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.movie_grid_item, parent, false);

        return new ViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get()
                .load(mMovies[position].getPosterPath())
                .fit()
                .error(R.mipmap.ic_launcher_round)
                .placeholder(R.drawable.loading)// using a gif to show the loading
                .into(holder.ivMovieImage);

    }

    @Override
    public int getItemCount() {
        return mMovies != null ? mMovies.length : 0;
    }

    public void setMovies(Movie[] movies) {
        mMovies = movies;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivMovieImage;
        OnItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            ivMovieImage = itemView.findViewById(R.id.ivMovieImage);
            itemClickListener = onItemClickListener;

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            itemClickListener.onItemClick(getAdapterPosition());
        }
    }
}
