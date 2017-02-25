package com.android.example.popularmovie;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.example.popularmovie.entity.ResultsBean;


public class DetailActivity extends AppCompatActivity {

    private static final String EXTRA_PARCE = "EXTRA_PARCE";

    public static void startActivity(Context context, Parcelable value){
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_PARCE, value);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (findViewById(R.id.container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
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

        public DetailFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_detail, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            Intent intent = getActivity().getIntent();
            ResultsBean.FilmBean item = intent.getParcelableExtra(DetailActivity.EXTRA_PARCE);
            TextView detail_text = (TextView) view.findViewById(R.id.detail_text);
            detail_text.setText(item.title);
        }

    }

}