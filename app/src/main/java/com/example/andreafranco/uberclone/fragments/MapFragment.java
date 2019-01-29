package com.example.andreafranco.uberclone.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.andreafranco.uberclone.BuildConfig;
import com.example.andreafranco.uberclone.R;
import com.example.andreafranco.uberclone.activities.SearchAddressActivity;
import com.example.andreafranco.uberclone.loaders.RouteLoader;
import com.example.andreafranco.uberclone.models.LoggedUser;
import com.example.andreafranco.uberclone.models.Request;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, LoaderManager.LoaderCallbacks<PolylineOptions> {
    private static final int PERMISSION_CODE = 0;
    private static final float DEFAULT_ZOOM_LEVEL = 15;
    private static final String CONFIG_ZOOM_LEVEL_KEY = "config_zoom_level_key";
    private static final int ROUTES_LOADER_ID = 1;

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private FloatingActionButton mDriverFab;

    private HashMap<String, Marker> mMarkersHash;

    // the fragment initialization parameters
    private static final String ARG_PARAM = "arg_param";

    private LoggedUser mCurrentUser;
    private float mZoomLevel;
    private Marker mUserPositionMarker;
    LoaderManager mLoaderManager = null;

    //Firebase Variables
    private MapFragment.OnFragmentInteractionListener mListener;
    private DatabaseReference mRequestsDatabaseReference;
    private FirebaseDatabase mDataBase;
    private ChildEventListener mChildEventListener;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapFragment.
     * @param user
     */
    public static MapFragment newInstance(LoggedUser user) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrentUser = getArguments().getParcelable(ARG_PARAM);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final EditText driverFab =view.findViewById(R.id.search_edittext);
        driverFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onStartNewActivity(new Intent(getActivity(), SearchAddressActivity.class));
            }
        });

        mMarkersHash = new HashMap<>();
        mLoaderManager = getLoaderManager();
        return view;
    }

    private void updateRequests(final Location userLocation) {
        attachDatabaseReadListener();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        //Prepare Database
        mDataBase = FirebaseDatabase.getInstance();
        mRequestsDatabaseReference = mDataBase.getReference().child("requests");
        activateFirebaseComponents();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null && mCurrentUser.getUserType() == LoggedUser.DRIVER) {
            mChildEventListener = new ChildEventListener() {

                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Request request = dataSnapshot.getValue(Request.class);

                    //mMap.clear();
                    LatLng userLocation = new LatLng(request.getLatitude(), request.getLongitude());
                    String rider = request.getRiderUuid();

                    MarkerOptions riderRequestMarker = new MarkerOptions();
                    riderRequestMarker.position(userLocation);
                    riderRequestMarker.title(rider);
                    riderRequestMarker.anchor(0.5f, 0.5f);
                    riderRequestMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    Marker marker = mMap.addMarker(riderRequestMarker);
                    marker.setTag(dataSnapshot.getKey());
                    mMarkersHash.put(dataSnapshot.getKey(),marker);
                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), mZoomLevel);
                    mMap.animateCamera(cu);

                    /*//Add the current driver position
                    MarkerOptions driverRequestMarker = new MarkerOptions();
                    Location lastKnownPosition = getLastKnownPosition();
                    LatLng driverLocation = new LatLng(lastKnownPosition.getLatitude(), lastKnownPosition.getLongitude());
                    driverRequestMarker.position(driverLocation);
                    driverRequestMarker.title(request.getDriverUuid());
                    driverRequestMarker.anchor(0.5f, 0.5f);
                    driverRequestMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    Marker markerDriver = mMap.addMarker(driverRequestMarker);
                    markerDriver.setTag("driver");
                    if (!mMarkersHash.containsKey("driver")) {
                        mMarkersHash.put("driver",marker);
                    }

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Object iterator : mMarkersHash.entrySet()) {
                        Map.Entry pair = (Map.Entry) iterator;
                        Marker markerValue = (Marker) pair.getValue();
                        builder.include(markerValue.getPosition());
                    }

                    LatLngBounds bounds = builder.build();
                    int padding = 10; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.animateCamera(cu);*/

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    //TODO check if marker already exist, otherwise add it
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    //TODO check if marker already exist, otherwise add it
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mRequestsDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mRequestsDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                    Location lastKnownLocation = getLastKnownPosition();
                    if (lastKnownLocation != null) {
                        updateMap(lastKnownLocation);
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private Location getLastKnownPosition() {
        Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation == null) {
            lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return lastKnownLocation;
    }

    private void setUpLocationManager() {
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                addUserMarker(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        } else {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                Location lastKnownLocation = getLastKnownPosition();
                if (lastKnownLocation != null) {
                    updateMap(lastKnownLocation);
                }
            }
        }
    }

    private void addUserMarker(Location location) {
        if (mUserPositionMarker != null && mUserPositionMarker.getId() != null) {
            mUserPositionMarker.remove();
        }
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions riderRequestMarker = new MarkerOptions();
        riderRequestMarker.position(userLocation);
        riderRequestMarker.title(mCurrentUser.getName());
        riderRequestMarker.anchor(0.5f, 0.5f);
        riderRequestMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.rider_marker));
        mUserPositionMarker = mMap.addMarker(riderRequestMarker);
                /*marker.setTag(mc);
                mMarkersHash.put(dataSnapshot.getKey(),marker);*/
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(mUserPositionMarker.getPosition(), mZoomLevel);
        mMap.animateCamera(cu);
    }

    private void activateFirebaseComponents() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(CONFIG_ZOOM_LEVEL_KEY, DEFAULT_ZOOM_LEVEL);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);
        fetchConfig();
    }

    private void fetchConfig() {
        long cacheExpiration = 3600;
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        //Add listeners
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFirebaseRemoteConfig.activateFetched();
                        applyRetrieveZoomLimit();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void applyRetrieveZoomLimit() {
        mZoomLevel = ((Double) mFirebaseRemoteConfig.getDouble(CONFIG_ZOOM_LEVEL_KEY)).floatValue();
        setUpLocationManager();
    }

    /**
     * Add a marker to user position
     * @param location
     */
    private void updateMap(Location location) {
        updateRequests(location);
        addUserMarker(location);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(new UserWindowAdapter());
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    /**
     * Add a request e draw the path on map
     * @param data
     */
    public void addRequestOnMap(Intent data) {
        if (data != null) {
            LatLng originPlaceParcelableExtra = data.getParcelableExtra(SearchAddressActivity.ORIGIN);
            LatLng destinationPlaceParcelableExtra = data.getParcelableExtra(SearchAddressActivity.DESTINATION);
            Bundle bundle = new Bundle();
            bundle.putParcelable(SearchAddressActivity.ORIGIN, originPlaceParcelableExtra);
            bundle.putParcelable(SearchAddressActivity.DESTINATION, destinationPlaceParcelableExtra);
            if (mLoaderManager.getLoader(ROUTES_LOADER_ID) == null) {
                mLoaderManager.initLoader(ROUTES_LOADER_ID, bundle, MapFragment.this);
            } else {
                mLoaderManager.restartLoader(ROUTES_LOADER_ID, bundle, MapFragment.this);
            }
        }
    }

    @NonNull
    @Override
    public Loader<PolylineOptions> onCreateLoader(int i, @NonNull Bundle bundle) {
        LatLng originPlaceParcelableExtra = bundle.getParcelable(SearchAddressActivity.ORIGIN);
        LatLng destinationPlaceParcelableExtra = bundle.getParcelable(SearchAddressActivity.DESTINATION);
        return new RouteLoader(getActivity(), originPlaceParcelableExtra, destinationPlaceParcelableExtra);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<PolylineOptions> loader, PolylineOptions polylineOptions) {
        if (mMap != null && polylineOptions != null) {
            mMap.addPolyline(polylineOptions);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<PolylineOptions> loader) {

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void onStartNewActivity(Intent intent);
    }

    class UserWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        UserWindowAdapter(){
            myContentsView = getLayoutInflater().inflate(R.layout.custom_info_contents_rider, null);
        }

        @Override
        public View getInfoContents(Marker marker) {

            TextView nameTextView = ((TextView)myContentsView.findViewById(R.id.name_textview));
            nameTextView.setText(marker.getTitle());
            ImageView userProfileImageView = myContentsView.findViewById(R.id.user_profile_mageview);
            //userProfileImageView.setImageBitmap(User profile image saved on server);
            Button acceptRequestButton = myContentsView.findViewById(R.id.accept_button);
            acceptRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO send the accept message to the server
                }
            });
            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
