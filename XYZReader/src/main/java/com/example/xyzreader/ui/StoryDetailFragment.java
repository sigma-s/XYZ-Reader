package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link StoryListActivity} in two-pane mode (on
 * tablets) or a {@link StoryDetailActivity} on handsets.
 */
public class StoryDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "StoryDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private Cursor cursor;
    private long itemId;
    private View rootView;
    private int color = 0xFF333333;
    private ColorDrawable statusBarColorDrawable;
    private Toolbar toolbar;

    private ImageView photoView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StoryDetailFragment() {
    }

    public static StoryDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        StoryDetailFragment fragment = new StoryDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            itemId = getArguments().getLong(ARG_ITEM_ID);
        }

        setHasOptionsMenu(true);
    }

    public StoryDetailActivity getActivityCast() {
        return (StoryDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_story_detail, container, false);

        photoView = (ImageView) rootView.findViewById(R.id.story_photo);

        statusBarColorDrawable = new ColorDrawable(0);

        toolbar = (Toolbar) rootView.findViewById(R.id.detail_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        bindViews();
        updateStatusBar();
        return rootView;
    }

    private void updateStatusBar() {
        int color = Color.argb(255,
                (int) (Color.red(this.color) * 0.9),
                (int) (Color.green(this.color) * 0.9),
                (int) (Color.blue(this.color) * 0.9));
        statusBarColorDrawable.setColor(color);
    }

    private void bindViews() {
        if (rootView == null) {
            return;
        }

        TextView titleView = (TextView) rootView.findViewById(R.id.story_title);
        TextView bylineView = (TextView) rootView.findViewById(R.id.story_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) rootView.findViewById(R.id.story_body);
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (cursor != null) {
            rootView.setAlpha(0);
            rootView.setVisibility(View.VISIBLE);
            rootView.animate().alpha(1);
            titleView.setText(cursor.getString(ArticleLoader.Query.TITLE));
            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            cursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by "
                            + cursor.getString(ArticleLoader.Query.AUTHOR)));
            bodyView.setText(Html.fromHtml(cursor.getString(ArticleLoader.Query.BODY)));
            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(cursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                Palette.Swatch vibrant = p.getVibrantSwatch();
                                if(vibrant!=null) {
                                    color = vibrant.getRgb();
                                }
                                photoView.setImageBitmap(imageContainer.getBitmap());
                                rootView.findViewById(R.id.metabar)
                                        .setBackgroundColor(color);
                                updateStatusBar();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        } else {
            rootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A" );
            bodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), itemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        this.cursor = cursor;
        if (this.cursor != null && !this.cursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            this.cursor.close();
            this.cursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        cursor = null;
        bindViews();
    }
    
}
