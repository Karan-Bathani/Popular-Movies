package com.example.popularmovies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.popularmovies.Adapters.MoviesAdapter;
import com.example.popularmovies.database.AppDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.OnItemClickListener {

    Movie[] movies;
    MoviesAdapter moviesAdapter;

    private AppDatabase mDb;
    private int selectedItem;
    private Parcelable mListState;
    private MenuItem menuItem;
    private RecyclerView.LayoutManager gridLayoutManager;

    private ProgressBar loading;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loading = (ProgressBar) findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        recyclerView = (RecyclerView) findViewById(R.id.moviesGrid);
        recyclerView.getRecycledViewPool().clear();
        gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        mDb = AppDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null) {
            selectedItem = savedInstanceState.getInt("OPTION");
        }

        if (isNetworkConnected()) {
            new FectchMovieAsync().execute("popular");
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
            loading.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("OPTION", selectedItem);
        // Save list state
        mListState = gridLayoutManager.onSaveInstanceState();
        outState.putParcelable("LIST_STATE_KEY", mListState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        selectedItem = outState.getInt("OPTION");

        // Retrieve list state and list/item positions
        if (outState != null)
            mListState = outState.getParcelable("LIST_STATE_KEY");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mListState != null) {
            gridLayoutManager.onRestoreInstanceState(mListState);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sort, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_most_popular) {
            selectedItem = R.id.menu_most_popular;
            if (isNetworkConnected()) {
                new FectchMovieAsync().execute("popular");
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
                loading.setVisibility(View.GONE);
            }
        } else if (item.getItemId() == R.id.menu_top_rated) {
            selectedItem = R.id.menu_top_rated;
            if (isNetworkConnected()) {
                new FectchMovieAsync().execute("top_rated");
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
                loading.setVisibility(View.GONE);
            }
        } else if (item.getItemId() == R.id.menu_favourite) {
            selectedItem = R.id.menu_favourite;
            loading.setVisibility(View.GONE);
            setUpViewModel();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setUpViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getMovies().observe(this, new Observer<Movie[]>() {
            @Override
            public void onChanged(Movie[] movies1) {
                if (moviesAdapter!=null) {
                    moviesAdapter.notifyDataSetChanged();
                    moviesAdapter.setMovies(movies1);
                }
            }
        });
    }

    //    To create an array of Movie class from the fetched json data
    public Movie[] makeMoviesDataToArray(String moviesJsonResults) throws JSONException {

        String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w185";

        // JSON filters
        final String RESULTS = "results";
        final String ORIGINAL_TITLE = "original_title";
        final String POSTER_PATH = "poster_path";
        final String OVERVIEW = "overview";
        final String VOTER_AVERAGE = "vote_average";
        final String RELEASE_DATE = "release_date";
        final String MOVIE_ID_QUERY_PARAM = "id";

        JSONObject moviesJson = new JSONObject(moviesJsonResults);
        JSONArray resultsArray = moviesJson.getJSONArray(RESULTS);

        movies = new Movie[resultsArray.length()];

        for (int i = 0; i < resultsArray.length(); i++) {

            movies[i] = new Movie();

            // Object contains all tags we're looking for
            JSONObject movieInfo = resultsArray.getJSONObject(i);

            movies[i].setOriginalTitle(movieInfo.getString(ORIGINAL_TITLE));
            movies[i].setPosterPath(IMAGE_BASE_URL + movieInfo.getString(POSTER_PATH));
            movies[i].setOverview(movieInfo.getString(OVERVIEW));
            movies[i].setVoterAverage(movieInfo.getDouble(VOTER_AVERAGE));
            movies[i].setReleaseDate(movieInfo.getString(RELEASE_DATE));
            movies[i].setMovieId(movieInfo.getInt(MOVIE_ID_QUERY_PARAM));
        }
        return movies;
    }

    //To handle click events on the recycler item
    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(MainActivity.this, MovieDetails.class);
        intent.putExtra("movie", movies[position]);
        startActivity(intent);
    }

    //    To check if device is connected to internet
    public boolean isNetworkConnected() {
        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            if (Build.VERSION.SDK_INT < 23) {
                final NetworkInfo ni = cm.getActiveNetworkInfo();

                if (ni != null) {
                    return (ni.isConnected() && (ni.getType() == ConnectivityManager.TYPE_WIFI || ni.getType() == ConnectivityManager.TYPE_MOBILE));
                }
            } else {
                final Network n = cm.getActiveNetwork();

                if (n != null) {
                    final NetworkCapabilities nc = cm.getNetworkCapabilities(n);

                    return (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
                }
            }
        }

        return false;
    }
/*
//    Not Recommended
    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }
*/

    public class FectchMovieAsync extends AsyncTask<String, Void, Movie[]> {

        public FectchMovieAsync() {
        }

        @Override
        protected Movie[] doInBackground(String... strings) {

            String movieSearchResults = null;

            try {
                URL url = JsonUtils.buildUrl(strings);
                movieSearchResults = JsonUtils.getResponseFromHttpUrl(url);

                if (movieSearchResults == null) {
                    return null;
                }
            } catch (IOException e) {
                return null;
            }

            try {
                return makeMoviesDataToArray(movieSearchResults);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Movie[] movies) {
//            Update the recycler view  after the data has been fetched in the background
            moviesAdapter = new MoviesAdapter(movies, MainActivity.this, MainActivity.this);
            recyclerView.setAdapter(moviesAdapter);
            loading.setVisibility(View.GONE);
        }
    }
}
