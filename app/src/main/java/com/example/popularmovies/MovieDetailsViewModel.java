package com.example.popularmovies;

import com.example.popularmovies.database.AppDatabase;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class MovieDetailsViewModel extends ViewModel {

    private LiveData<Movie> movie;

    public MovieDetailsViewModel(AppDatabase database, int movieId) {
        movie = database.movieDao ().loadMovieById (movieId);
    }

    public LiveData<Movie> getMovie() {
        return movie;
    }
}