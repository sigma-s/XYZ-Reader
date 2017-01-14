package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class StoryDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor cursor;
    private long startId;

    private ViewPager pager;
    private MyPagerAdapter pagerAdapter;
    private FloatingActionButton shareFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_story_detail);

        getLoaderManager().initLoader(0, null, this);

        pagerAdapter = new MyPagerAdapter(getFragmentManager());
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        pager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        pager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (cursor != null) {
                    cursor.moveToPosition(position);
                }
            }
        });

        shareFab = (FloatingActionButton) findViewById(R.id.share_fab);
        shareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(StoryDetailActivity.this)
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                startId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        this.cursor = cursor;
        pagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (startId > 0) {
            this.cursor.moveToFirst();
            // TODO: optimize
            while (!this.cursor.isAfterLast()) {
                if (this.cursor.getLong(ArticleLoader.Query._ID) == startId) {
                    final int position = this.cursor.getPosition();
                    pager.setCurrentItem(position, false);
                    break;
                }
                this.cursor.moveToNext();
            }
            startId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        cursor = null;
        pagerAdapter.notifyDataSetChanged();
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            cursor.moveToPosition(position);
            return StoryDetailFragment.newInstance(cursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (cursor != null) ? cursor.getCount() : 0;
        }
    }
}
