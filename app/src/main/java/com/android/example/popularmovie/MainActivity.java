package com.android.example.popularmovie;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.android.example.popularmovie.util.DisplayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, FilmDisplayFragment.newInstance()).commit();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class FilmDisplayFragment extends Fragment {

        public static FilmDisplayFragment newInstance() {
            return new FilmDisplayFragment();
        }

        private Activity mActivity;

        public FilmDisplayFragment() {
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mActivity = getActivity();

            // Add this line in order for this fragment to handle menu events.
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_film_display, container, false);
            GridView gridView = (GridView) rootView;

            final Integer[] data = {R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher
                    , R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher
                    , R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher};
            final ArrayAdapter<Integer> adapter = new FilmAdapter(mActivity, new ArrayList<>(Arrays.asList(data)));

            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(null);
            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.refresh, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int id = item.getItemId();

            if (id == R.id.action_refresh) {
                this.updateFilmInfo();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void updateFilmInfo() {

        }

        class FilmAdapter extends ArrayAdapter<Integer> {

            private final int IMAGE_WIDTH;

            FilmAdapter(Context context, List<Integer> list) {
                super(context, 0, list);

                //获取屏幕宽度
                IMAGE_WIDTH = DisplayUtil.getScreenWidth(mActivity) / 2;
            }

            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                final Integer item = getItem(position);
                ImageView imageView;
                if (convertView == null) {
                    // if it's not recycled, initialize some attributes
                    imageView = new ImageView(mActivity);

                    //获取原始图片的宽和高
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    Bitmap bitmap = BitmapFactory.decodeResource(mActivity.getResources(), item);
                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();

                    final int realHeight = IMAGE_WIDTH * h / w;

                    imageView.setLayoutParams(new GridView.LayoutParams(IMAGE_WIDTH, realHeight));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    imageView = (ImageView) convertView;
                }

                if (item != null)
                    imageView.setImageResource(item);
                return imageView;
            }

        }
    }


}



