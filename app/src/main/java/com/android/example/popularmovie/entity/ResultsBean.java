package com.android.example.popularmovie.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by hp on 2017/2/25.
 */

public class ResultsBean{

    /**
     * page : 1
     * results : [{"poster_path":"/AmbtHzH5kGt4dPTw2E4tBZQcLjz.jpg","adult":false,"overview":"故事设定在《美版午夜凶铃2》结尾的13年后，玛蒂尔达·鲁茨（Matilda Lutz）和阿历克斯·罗（Alex Roe）将在片中饰演一对情侣，后者因为看了录像带而开始疏远女友。","release_date":"2017-02-01","genre_ids":[27],....
     * total_results : 19627
     * total_pages : 982
     */

    private int page;

    private int total_results;

    private int total_pages;

    private List<FilmBean> results;


    public List<FilmBean> getResults() {
        return results;
    }

    public void setResults(List<FilmBean> results) {
        this.results = results;
    }

    public static class FilmBean implements Parcelable {

        /**
         * poster_path : /AmbtHzH5kGt4dPTw2E4tBZQcLjz.jpg
         * adult : false
         * overview : 故事设定在...
         * release_date : 2017-02-01
         * genre_ids : [27]
         * id : 14564
         * original_title : Rings
         * original_language : en
         * title : 午夜凶铃3(美版)
         * backdrop_path : /biN2sqExViEh8IYSJrXlNKjpjxx.jpg
         * popularity : 185.121625
         * vote_count : 296
         * video : false
         * vote_average : 5.1
         */

        public String overview;

        @SerializedName("release_date")
        public String releaseDate;

        public String title;

        @SerializedName("backdrop_path")
        public String backdropPath;

        @SerializedName("vote_average")
        public double voteAverage;

        FilmBean(Parcel in) {
            overview = in.readString();
            releaseDate = in.readString();
            title = in.readString();
            backdropPath = in.readString();
            voteAverage = in.readDouble();
        }

        public static final Creator<FilmBean> CREATOR = new Creator<FilmBean>() {
            @Override
            public FilmBean createFromParcel(Parcel in) {
                return new FilmBean(in);
            }

            @Override
            public FilmBean[] newArray(int size) {
                return new FilmBean[size];
            }
        };

        /**
         * Parcelable interface methods
         */
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(overview);
            dest.writeString(releaseDate);
            dest.writeString(title);
            dest.writeString(backdropPath);
            dest.writeDouble(voteAverage);

        }
    }

}
