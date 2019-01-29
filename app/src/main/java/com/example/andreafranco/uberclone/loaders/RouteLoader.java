package com.example.andreafranco.uberclone.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import com.example.andreafranco.uberclone.utils.HttpUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class RouteLoader extends AsyncTaskLoader<PolylineOptions> {

    private final LatLng mOrigin;
    private final LatLng mDestination;

    public RouteLoader(@NonNull Context context, LatLng origin, LatLng destination) {
        super(context);
        mOrigin = origin;
        mDestination = destination;
    }

    @Nullable
    @Override
    public PolylineOptions loadInBackground() {
        if (mOrigin == null || mDestination == null) {
            return null;
        }
        return HttpUtils.fetchImageListData(mOrigin, mDestination, getContext());
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}
