package com.android.example.popularmovie;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.example.popularmovie.entity.ResultsBean;
import com.android.example.popularmovie.netutil.DownloadCallback;
import com.android.example.popularmovie.netutil.DownloadUtil;
import com.android.example.popularmovie.netutil.Result;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            FilmDisplayFragment filmDisplayFragment = FilmDisplayFragment.newInstance();
            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            //firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.container, filmDisplayFragment).commit();
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null)
            return;
        toolbar.setTitle(getTitle());

        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.action_settings) {
            //startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class FilmDisplayFragment extends Fragment implements DownloadCallback<String> {

        public static final String TAG = FilmDisplayFragment.class.getSimpleName();

        public static FilmDisplayFragment newInstance() {
            return new FilmDisplayFragment();
        }

        /**
         * Boolean telling us whether a download is in progress
         */
        private boolean mDownloading = false;

        private final Gson gson = new Gson();

        private String mUrlString;

        private FetchMovieTask mTask;

        private Activity mActivity;

        private ArrayAdapter<ResultsBean.FilmBean> mAdapter;

        public FilmDisplayFragment() {
        }


        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mActivity = getActivity();

            //TODO test
            mUrlString = Constant.TOP_RATED;

            // Add this line in order for this fragment to handle menu events.
            setHasOptionsMenu(true);

            // Retain this Fragment across configuration changes in the host Activity.
            setRetainInstance(true);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_film_display, container, false);
            GridView gridView = (GridView) rootView;

            mAdapter = new InnerGridViewAdapter(mActivity);

            gridView.setAdapter(mAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    DetailActivity.startActivity(mActivity, mAdapter.getItem(position));
                }
            });
            gridView.setOnScrollListener(new PicassoScrollListener(mActivity));
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            this.updateMovieInfo();
        }

        @Override
        public void onStart() {
            super.onStart();
            //this.updateMovieInfo();
        }

        @Override
        public void onDestroy() {
            this.cancelDownload();
            super.onDestroy();
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.refresh, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int id = item.getItemId();

            if (id == R.id.action_refresh) {
                this.updateMovieInfo();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }


        @Override
        public void onSuccess(String result) {
            // Update your UI here based on result of download.
            // New data is back from the server.
            if (result != null) {
                ResultsBean resultsBean = gson.fromJson(result, ResultsBean.class);

                List<ResultsBean.FilmBean> filmList = resultsBean.getResults();
                mAdapter.clear();
                mAdapter.addAll(filmList);
            }
        }

        @Override
        public void onError(String errorString) {
            Toast.makeText(mActivity, errorString, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void finishDownloading() {
            mDownloading = false;
            this.cancelDownload();

        }

        public void updateMovieInfo() {
            if (!mDownloading) {
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
            mTask.execute(mUrlString);
        }

        /**
         * Cancel (and interrupt if necessary) any ongoing DownloadTask execution.
         */
        private void cancelDownload() {
            if (mTask != null) {
                mTask.cancel(true);
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
                ;
                if (convertView == null) {
                    // if it's not recycled, initialize some attributes
                    imageView = new ImageView(context);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }

                // Get the image URL for the current position.
                final ResultsBean.FilmBean item = getItem(position);

                // Trigger the download of the URL asynchronously into the image view.
                Picasso.with(mActivity)
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



