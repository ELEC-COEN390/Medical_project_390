package com.example.moodproject;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

import java.util.ArrayList;
import java.util.List;
public class SynchronizedScrollView extends HorizontalScrollView {
    // In SynchronizedScrollView.java
    private List<SynchronizedScrollView> mViews = new ArrayList<>();


    private boolean mIsMaster = true;

    public SynchronizedScrollView(Context context) {
        super(context);
    }

    public SynchronizedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SynchronizedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSynchronizedScrollViews(List<SynchronizedScrollView> views) {
        mViews = views;
    }
    public void setIsMaster(boolean isMaster) {
        mIsMaster = isMaster;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mIsMaster && mViews != null) {
            for (SynchronizedScrollView view : mViews) {
                if (view != this) {
                    view.scrollTo(l, t);
                }
            }
        }
    }
}

