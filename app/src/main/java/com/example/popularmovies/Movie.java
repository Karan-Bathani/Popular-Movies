package com.example.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "movie")
public class Movie implements Parcelable {

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @PrimaryKey(autoGenerate = true)
    private int dbMovieId;
    private int movieId;
    private String originalTitle;
    private String posterPath;
    private String overview;
    private String releaseDate;
    private Double voterAverage;
    private String trailerPath;
    private String reviewAuthor;
    private String reviewContents;
    private String reviewUrl;
    @Ignore
    private boolean isFavoriteMovie = false;

    public Movie() {
    }

    protected Movie(Parcel in) {
        dbMovieId = in.readInt();
        movieId = in.readInt();
        originalTitle = in.readString();
        posterPath = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        if (in.readByte() == 0) {
            voterAverage = null;
        } else {
            voterAverage = in.readDouble();
        }
        trailerPath = in.readString();
        reviewAuthor = in.readString();
        reviewContents = in.readString();
        reviewUrl = in.readString();
        isFavoriteMovie = in.readByte() != 0;
    }

    public int getDbMovieId() {
        return dbMovieId;
    }

    public void setDbMovieId(int dbMovieId) {
        this.dbMovieId = dbMovieId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Double getVoterAverage() {
        return voterAverage;
    }

    public void setVoterAverage(Double voterAverage) {
        this.voterAverage = voterAverage;
    }

    public String getTrailerPath() {
        return trailerPath;
    }

    public void setTrailerPath(String trailerPath) {
        this.trailerPath = trailerPath;
    }

    public String getReviewAuthor() {
        return reviewAuthor;
    }

    public void setReviewAuthor(String reviewAuthor) {
        this.reviewAuthor = reviewAuthor;
    }

    public String getReviewContents() {
        return reviewContents;
    }

    public void setReviewContents(String reviewContents) {
        this.reviewContents = reviewContents;
    }

    public String getReviewUrl() {
        return reviewUrl;
    }

    public void setReviewUrl(String reviewUrl) {
        this.reviewUrl = reviewUrl;
    }

    public boolean isFavoriteMovie() {
        return isFavoriteMovie;
    }

    public void setFavoriteMovie(boolean favoriteMovie) {
        isFavoriteMovie = favoriteMovie;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(dbMovieId);
        dest.writeInt(movieId);
        dest.writeString(originalTitle);
        dest.writeString(posterPath);
        dest.writeString(overview);
        dest.writeString(releaseDate);
        if (voterAverage == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(voterAverage);
        }
        dest.writeString(trailerPath);
        dest.writeString(reviewAuthor);
        dest.writeString(reviewContents);
        dest.writeString(reviewUrl);
        dest.writeByte((byte) (isFavoriteMovie ? 1 : 0));
    }
}
