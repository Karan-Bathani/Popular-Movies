package com.example.popularmovies;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.SyncStateContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.popularmovies.Adapters.MovieReviewAdapter;
import com.example.popularmovies.database.AppDatabase;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MovieDetails extends AppCompatActivity {

    private ImageView ivMovieImage;
    private TextView tvTitle, tvUserRating, tvReleaseDate, tvOverview, tvReviewLabel;
    private Button btnTrailer;
    private ToggleButton toggleFavourite;
    private MovieReviewAdapter movieReviewAdapter;
    private AppDatabase mDb;
    private RecyclerView reviewRecyclerView;
    private Movie movie;
//    private Movie[] movies;

    public static void watchYoutubeVideo(Context context, String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        // If youtube is not installed, plays from web
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();

        movie = intent.getParcelableExtra("movie");
        mDb = AppDatabase.getInstance(getApplicationContext());
        setupUi(movie);

        setUpFavoriteMovieButton();

        toggleFavourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggleFavourite.getTextOn();
                    onFavoriteButtonClicked();
                } else {
                    toggleFavourite.setTextColor(Color.parseColor("#000000"));
                    toggleFavourite.getTextOff();

                    AppExecutor.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            MovieDetails.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mDb.movieDao().deleteMovie(movie.getMovieId());
                                }
                            });
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState (outState);
    }

    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState (savedState);
    }

    private void setupUi(Movie movie) {

        ivMovieImage = (ImageView) findViewById(R.id.ivMovieImage);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvUserRating = (TextView) findViewById(R.id.tvUserRating);
        tvReleaseDate = (TextView) findViewById(R.id.tvReleaseDate);
        tvOverview = (TextView) findViewById(R.id.tvOverview);
        reviewRecyclerView = (RecyclerView) findViewById(R.id.review_recycler_view);
        btnTrailer = (Button) findViewById(R.id.btn_Trailer);
        toggleFavourite = (ToggleButton) findViewById(R.id.toggle_Favourite);

        tvTitle.setText(movie.getOriginalTitle());
        tvUserRating.setText(String.valueOf(movie.getVoterAverage()) + " / 10");
        Picasso.get()
                .load(movie.getPosterPath())
                .fit()
                .error(R.mipmap.ic_launcher_round)
                .placeholder(R.drawable.loading)
                .into(ivMovieImage);

        tvOverview.setText(movie.getOverview());

        tvReleaseDate.setText(movie.getReleaseDate());

        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        new TrailerButtonAsyncTask(btnTrailer).execute(String.valueOf(movie.getMovieId()), "videos");

        new ReviewsAsyncTask().execute(String.valueOf(movie.getMovieId()), "reviews");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    public Movie[] setMovieDataToArray(String jsonResults) throws JSONException {
        JSONObject root = new JSONObject(jsonResults);
        JSONArray resultsArray = root.getJSONArray("results");
        Movie[] movies = new Movie[resultsArray.length()];

        for (int i = 0; i < resultsArray.length(); i++) {
            // Initialize each object before it can be used
            movies[i] = new Movie();

            // Object contains all tags we're looking for
            JSONObject movieInfo = resultsArray.getJSONObject(i);

            // Store data in movie object
            movies[i].setReviewAuthor(movieInfo.getString("author"));
            movies[i].setReviewContents(movieInfo.getString("content"));
            movies[i].setReviewUrl(movieInfo.getString("url"));
        }
        return movies;
    }

    public void onFavoriteButtonClicked() {
        AppExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.movieDao().insertMovie(movie);
            }
        });
    }

    private void setUpFavoriteMovieButton() {
        MovieDetailsViewModelFactory factory =
                new MovieDetailsViewModelFactory(mDb, movie.getMovieId());
        final MovieDetailsViewModel viewModel =
                ViewModelProviders.of(this, factory).get(MovieDetailsViewModel.class);

        viewModel.getMovie().observe(this, new Observer<Movie>() {
            @Override
            public void onChanged(@Nullable Movie movieInDb) {
                viewModel.getMovie().removeObserver(this);

                if (movieInDb == null) {
                    toggleFavourite.setTextColor(Color.parseColor("#000000"));
                    toggleFavourite.setChecked(false);
                    toggleFavourite.getTextOff();
                } else if ((movie.getMovieId() == movieInDb.getMovieId()) && !toggleFavourite.isChecked()) {
                    toggleFavourite.setChecked(true);
                    toggleFavourite.setText("Favourited");
                    toggleFavourite.setTextColor(Color.parseColor("#0D22D3"));
                } else {
                    toggleFavourite.setTextColor(Color.parseColor("#000000"));
                    toggleFavourite.setChecked(false);
                    toggleFavourite.getTextOff();
                }
            }
        });
    }

    private class TrailerButtonAsyncTask extends AsyncTask<String, Void, String> {
        private final Button button;
        String trailerKey = null;

        public TrailerButtonAsyncTask(Button button) {
            this.button = button;
        }

        @Override
        protected String doInBackground(String... strings) {
            Movie[] movies;
            try {
                URL url = JsonUtils.buildMovieIdUrl(strings[0], strings[1]);
                String movieSearchResults = JsonUtils.getResponseFromHttpUrl(url);

                JSONObject root = new JSONObject(movieSearchResults);
                JSONArray resultsArray = root.getJSONArray("results");

                if (resultsArray.length() == 0) {
                    trailerKey = null;
                } else {
                    movies = new Movie[resultsArray.length()];
                    for (int i = 0; i < resultsArray.length(); i++) {
                        movies[i] = new Movie();

                        JSONObject movieInfo = resultsArray.getJSONObject(i);

                        movies[i].setTrailerPath(movieInfo.getString("key"));
                    }
                    return movies[0].getTrailerPath();
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return trailerKey;
        }

        protected void onPostExecute(final String temp) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (temp == null) {
                        Toast.makeText(getApplicationContext(), "Sorry, no trailers here.", Toast.LENGTH_SHORT).show();
                    } else {
                        watchYoutubeVideo(MovieDetails.this, temp);
                    }
                }
            });
        }
    }

    private class ReviewsAsyncTask extends AsyncTask<String, Void, Movie[]> {
        @Override
        protected Movie[] doInBackground(String... strings) {
            try {
                URL url = JsonUtils.buildMovieIdUrl(strings[0], strings[1]);
                String movieSearchResults = JsonUtils.getResponseFromHttpUrl(url);
                return setMovieDataToArray(movieSearchResults);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Movie[] movies) {
            movieReviewAdapter = new MovieReviewAdapter(movies, MovieDetails.this);

            if (movieReviewAdapter.getItemCount() == -1) {
                tvReviewLabel = findViewById(R.id.tvReviewLabel);
                tvReviewLabel.setVisibility(TextView.GONE);
            } else {
                reviewRecyclerView.setAdapter(movieReviewAdapter);
                reviewRecyclerView.setNestedScrollingEnabled(false);
            }

        }
    }
}
