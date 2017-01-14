package com.example.xyzreader.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link StoryDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class StoryListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(refreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(refreshingReceiver);
    }

    private BroadcastReceiver refreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                isRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        swipeRefreshLayout.setRefreshing(isRefreshing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(sglm);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor cursor;

        public Adapter(Cursor cursor) {
            this.cursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            cursor.moveToPosition(position);
            return cursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_story, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()))));
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            cursor.moveToPosition(position);
            holder.mTitleView.setText(cursor.getString(ArticleLoader.Query.TITLE));
            holder.mDateView.setText(
                    DateUtils.getRelativeTimeSpanString(
                            cursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString());
            holder.mAuthorView.setText(
                            cursor.getString(ArticleLoader.Query.AUTHOR));
            holder.mThumbnailView.setResponseObserver(new DynamicHeightNetworkImageView.ResponseObserver() {

                @Override
                public void onSuccess(Bitmap bmp) {
                    if (bmp != null) {
                        Palette p = Palette.generate(bmp);
                        Palette.Swatch vibrant = p.getVibrantSwatch();
                        if(vibrant!=null) {
                            int mColor = vibrant.getRgb();
                            holder.mStoryView.setCardBackgroundColor(mColor);
                        }
                    }
                }
            });
            holder.mThumbnailView.setImageUrl(
                    cursor.getString(ArticleLoader.Query.THUMB_URL),
                    ImageLoaderHelper.getInstance(StoryListActivity.this).getImageLoader());
            holder.mThumbnailView.setAspectRatio(cursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        }

        @Override
        public int getItemCount() {
            return cursor.getCount();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public DynamicHeightNetworkImageView mThumbnailView;
        public TextView mTitleView;
        public CardView mStoryView;
        public TextView mDateView;
        public TextView mAuthorView;

        public ViewHolder(View view) {
            super(view);
            mStoryView = (CardView) view.findViewById(R.id.story_view);
            mThumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail_view);
            mTitleView = (TextView) view.findViewById(R.id.story_title);
            mDateView = (TextView) view.findViewById(R.id.story_date);
            mAuthorView = (TextView) view.findViewById(R.id.story_author);
        }
    }
}
