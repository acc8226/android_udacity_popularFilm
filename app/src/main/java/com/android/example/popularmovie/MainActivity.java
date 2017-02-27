package com.android.example.popularmovie;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.example.popularmovie.Util.SharedPreferencesUtil;
import com.android.example.popularmovie.entity.ResultsBean;
import com.android.example.popularmovie.netutil.DownloadCallback;
import com.android.example.popularmovie.netutil.DownloadUtil;
import com.android.example.popularmovie.netutil.Result;
import com.android.example.popularmovie.view.PicassoScrollListener;
import com.android.example.popularmovie.view.ProgressFragment;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG_FRAGMENT_FILM_LIST = "tag_fragment_film_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final CharSequence[] MOVIE_SORT_ARRAY = getResources().getTextArray(R.array.movie_sort_array);
        final String[] FILM_URL = getResources().getStringArray(R.array.movie_sort_url_array);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null)
            return;
        toolbar.setTitle(getTitle());
        setSupportActionBar(toolbar);

        Spinner spinner = (Spinner) toolbar.findViewById(R.id.movie_sort_array);
        // Create an ArrayAdapter using the string array and a default spinner layout
        final ArrayAdapter<CharSequence> adapter =  new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MOVIE_SORT_ARRAY);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        final int selection = SharedPreferencesUtil.getMovieSort(this);
        spinner.setSelection(selection);

        // Check that the activity is using the layout version with the fragment_container FrameLayout
        if (findViewById(R.id.container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            FilmDisplayFragment filmDisplayFragment = FilmDisplayFragment.newInstance(FILM_URL);
            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.container, filmDisplayFragment, TAG_FRAGMENT_FILM_LIST).commit();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Toast.makeText(this, "write to sp" + position, Toast.LENGTH_SHORT).show();
        // An item was selected. You can retrieve the selected item using
        SharedPreferencesUtil.putMovieSort(this, position);

        //更改url, 并使fragment去刷新
        FilmDisplayFragment fragment = (FilmDisplayFragment) getFragmentManager().findFragmentByTag(TAG_FRAGMENT_FILM_LIST);
        if(fragment != null && fragment.isVisible()){
            fragment.updateMovieInfo(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public static class FilmDisplayFragment extends Fragment implements DownloadCallback<String>{

        private static final String FILM_URL_KEY = "film_url_key";
        private static final String TAG_DIALOG = "dialog";

        private final Gson gson = new Gson();
        private int mFilmIndex = 0;
        private String[] mFilmUrl;
        /**
         * Boolean telling us whether a download is in progress
         */
        private boolean mDownloading = false;
        private FetchMovieTask mTask;
        private Activity mActivity;
        private ArrayAdapter<ResultsBean.FilmBean> mAdapter;
        private FragmentManager mFragmentManager;

        private SwipeRefreshLayout mSwipeRefreshLayout;
        private GridView mGridView;

        public FilmDisplayFragment() {}

        public static FilmDisplayFragment newInstance(@Nullable String[] sequences) {
            FilmDisplayFragment fragment = new FilmDisplayFragment();
            Bundle bundle = new Bundle();
            bundle.putStringArray(FILM_URL_KEY, sequences);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mActivity = getActivity();
            mFragmentManager = getFragmentManager();
            mFilmUrl = getArguments().getStringArray(FILM_URL_KEY);
            // Retain this Fragment across configuration changes in the host Activity.
            setRetainInstance(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_film_display, container, false);
            mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout);
            mGridView = (GridView) rootView.findViewById(R.id.gridView);

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    FilmDisplayFragment.this.updateMovieInfo();
                }
            });

            mAdapter = new InnerGridViewAdapter(mActivity);

            mGridView.setAdapter(mAdapter);
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = DetailActivity.newIntent(mActivity, mAdapter.getItem(position));
                    startActivity(intent);
                }
            });
            mGridView.setOnScrollListener(new PicassoScrollListener(mActivity));
            return rootView;
        }

        @Override
        public void onDestroy() {
            this.cancelDownload();
            mActivity = null;
            super.onDestroy();
        }

        @Override
        public void onSuccess(String result) {
            // Update your UI here based on result of download. New data is back from the server.
            if (result != null) {
                try {
                    ResultsBean resultsBean = gson.fromJson(result, ResultsBean.class);
                    List<ResultsBean.FilmBean> filmList = resultsBean.getResults();
                    mAdapter.clear();
                    mAdapter.addAll(filmList);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    if(mGridView != null){
                        mGridView.smoothScrollToPositionFromTop(0, 700);
                    }
                    this.dismissLoadingDialog();
                }
            }
        }

        @Override
        public void onError(String errorString) {
            Toast.makeText(mActivity, errorString, Toast.LENGTH_SHORT).show();
            if(mSwipeRefreshLayout != null){
                mSwipeRefreshLayout.setRefreshing(false);
            }
            this.dismissLoadingDialog();
        }

        @Override
        public void finishDownloading() {
            mDownloading = false;
            this.cancelDownload();

        }

        /**
         * 直接刷新
         */
        public void updateMovieInfo() {
            if (!mDownloading) {
                // Execute the async download.
                this.startDownload();
                mDownloading = true;
            }
        }

        /**
         * 更改url导致的刷新
         * @param index
         */
        public void updateMovieInfo(int index) {
            if (!mDownloading) {
                mFilmIndex = index;
                this.showLoadingDialog();
                // Execute the async download.
                this.startDownload();
                mDownloading = true;
            }
        }

        /**
         * Start non-blocking execution of DownloadTask.
         */
        private void startDownload() {
            this.cancelDownload();
            mTask = new FetchMovieTask();
            mTask.execute(mFilmUrl[mFilmIndex]);
        }

        /**
         * Cancel (and interrupt if necessary) any ongoing DownloadTask execution.
         */
        private void cancelDownload() {
            if (mTask != null) {
                mTask.cancel(true);
            }
        }

        private void showLoadingDialog(){
            if(mFragmentManager.findFragmentByTag(TAG_DIALOG) == null && this.isResumed()){
                new ProgressFragment().show(mFragmentManager, TAG_DIALOG);
            }
        }

        private void dismissLoadingDialog(){
            DialogFragment dg;
            if((dg = (DialogFragment) mFragmentManager.findFragmentByTag(TAG_DIALOG)) != null){
                //fixed "Can not perform this action after onSaveInstanceState"
                dg.dismissAllowingStateLoss();
            }
        }

        class InnerGridViewAdapter extends ArrayAdapter<ResultsBean.FilmBean> {

            final Context context;

            InnerGridViewAdapter(Context context) {
                super(context, 0, new ArrayList<ResultsBean.FilmBean>(20));
                this.context = context;
            }

            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                ImageView imageView = (ImageView) convertView;
                if (convertView == null) {
                    // if it's not recycled, initialize some attributes
                    imageView = new ImageView(context);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }

                // Get the image URL for the current position.
                final ResultsBean.FilmBean item = getItem(position);

                // Trigger the download of the URL asynchronously into the image view.
                Picasso.with(context)
                        .load(Constant.IMAGE_BASE_URL.concat(item.backdropPath))
                        .placeholder(R.drawable.placeholder)
                        .tag(context)
                        .into(imageView);
                return imageView;
            }

        }

        class FetchMovieTask extends AsyncTask<String, Void, Result> {

            /**
             * Defines work to perform on the background thread.
             */
            @Override
            protected Result doInBackground(String... urls) {
                Result result = null;
                if (!super.isCancelled() && urls != null && urls.length > 0) {
                    final String urlString = urls[0];

                    Uri builtUri = Uri.parse(urlString)
                            .buildUpon()
                            .appendQueryParameter("api_key", BuildConfig.THEMOVIEDB_API_KEY)
                            .build();

                    try {
                        final URL url = new URL(builtUri.toString());
                        String resultString = DownloadUtil.download(url);
                        if (resultString != null) {
                            result = new Result(resultString);
                        } else {
                            //重新包装后抛出
                            throw new IOException("No response received.");
                        }
                    } catch (IOException e) {
                        result = new Result(e);
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(Result result) {
                if (result != null) {
                    if (result.mException != null) {
                        final String errorMsg;
                        if (result.mException instanceof UnknownHostException) {
                            errorMsg = "与主机失联, 请检查网络";
                        } else if (result.mException instanceof SocketTimeoutException) {
                            errorMsg = "连接超时, 请检查网络";
                        } else if (result.mException instanceof ConnectException) {
                            errorMsg = "没有联网";
                        } else {
                            errorMsg = "网络连接失败, 请检查网络";
                        }
                        FilmDisplayFragment.this.onError(errorMsg);
                    } else if (result.mResultValue != null) {
                        FilmDisplayFragment.this.onSuccess(result.mResultValue);
                    }
                    FilmDisplayFragment.this.finishDownloading();
                }
            }

        }
    }

}



