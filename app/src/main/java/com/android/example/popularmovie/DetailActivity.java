package com.android.example.popularmovie;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.example.popularmovie.entity.ResultsBean;
import com.squareup.picasso.Picasso;

import static com.android.example.popularmovie.R.id.overview;


public class DetailActivity extends AppCompatActivity {

    public static Intent newIntent(Context packageContext, Parcelable value){
        Intent intent = new Intent(packageContext, DetailActivity.class);
        intent.putExtra(DetailFragment.EXTRA_FILM, value);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (findViewById(R.id.container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            Parcelable p = getIntent().getParcelableExtra(DetailFragment.EXTRA_FILM);
            DetailFragment detailFragment = DetailFragment.newInstance(p);

            getFragmentManager().beginTransaction()
                    .add(R.id.container, detailFragment)
                    .commit();
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null)
            return;
        toolbar.setTitle(getTitle());

        setSupportActionBar(toolbar);


        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            return;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        public static final String EXTRA_FILM = "EXTRA_FILM";

        private Activity mActivity;

        public static DetailFragment newInstance(@Nullable Parcelable value){
            DetailFragment detailFragment = new DetailFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(EXTRA_FILM, value);
            detailFragment.setArguments(bundle);
            return detailFragment;
        }

        public DetailFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mActivity = getActivity();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_detail, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            ResultsBean.FilmBean item = getArguments().getParcelable(EXTRA_FILM);
            if(item != null){
                TextView detail_text = (TextView) view.findViewById(R.id.detail_text);
                detail_text.setText(item.title);

                ImageView backdrop_iv = (ImageView) view.findViewById(R.id.backdrop_iv);
                Picasso.with(mActivity)
                        .load(Constant.IMAGE_BASE_URL.concat(item.backdropPath))
                        .into(backdrop_iv);

                ((TextView) view.findViewById(R.id.release_date_text)).setText(item.releaseDate);
                ((TextView) view.findViewById(R.id.vote_average_text)).setText(String.valueOf(item.voteAverage));

                String overviewDesc = item.overview;
                if(TextUtils.isEmpty(overviewDesc)){
                    overviewDesc = getString(R.string.prompt_noFilm);
                }
                ((TextView) view.findViewById(overview)).setText(overviewDesc);
            }
        }

        @Override
        public void onDestroy() {
            mActivity = null;
            super.onDestroy();
        }
    }

}