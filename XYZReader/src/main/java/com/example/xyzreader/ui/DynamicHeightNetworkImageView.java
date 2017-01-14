package com.example.xyzreader.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

public class DynamicHeightNetworkImageView extends NetworkImageView {
    private float mAspectRatio = 1.5f;

    public interface ResponseObserver
    {
        void onSuccess(Bitmap bm);
    }

    private ResponseObserver mObserver;

    public void setResponseObserver(ResponseObserver observer) {
        mObserver = observer;
    }


    public DynamicHeightNetworkImageView(Context context) {
        super(context);
    }

    public DynamicHeightNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicHeightNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAspectRatio(float aspectRatio) {
        mAspectRatio = aspectRatio;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, (int) (measuredWidth / mAspectRatio));
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        mObserver.onSuccess(bm);
        super.setImageBitmap(bm);
    }
}
