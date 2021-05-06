package com.example.popularmovies;

import android.app.Application;

import com.example.popularmovies.database.AppDatabase;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MainViewModel extends AndroidViewModel {

    private LiveData<Movie[]> movies;

    public MainViewModel(@NonNull Application application) {
        super (application);
        AppDatabase database = AppDatabase.getInstance (this.getApplication ());
        movies = database.movieDao ().loadAllMovies ();
    }

    public LiveData<Movie[]> getMovies() {
        return movies;
    }
}
