package com.example.andreafranco.uberclone.fragments;

import android.Manifest;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.andreafranco.uberclone.R;
import com.example.andreafranco.uberclone.models.LoggedUser;
import com.example.andreafranco.uberclone.models.Request;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.LOCATION_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DriverFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DriverFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private static final int PERMISSION_CODE = 0;
    private static final float ZOOM_LEVEL = 15;

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private FloatingActionButton mDriverFab;

    // the fragment initialization parameters
    private static final String ARG_PARAM = "arg_param";
    private LoggedUser mParam;

    private RiderFragment.OnFragmentInteractionListener mListener;

    private DatabaseReference mRequestsDatabaseReference;
    private FirebaseDatabase mDataBase;
    private ChildEventListener mChildEventListener;

    public DriverFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DriverFragment.
     * @param user
     */
    public static DriverFragment newInstance(LoggedUser user) {
        DriverFragment fragment = new DriverFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam = getArguments().getParcelable(ARG_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_driver, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    private void updateRequests(final Location userLocation) {
        attachDatabaseReadListener();

        /*ParseQuery<ParseObject> requestQuery = new ParseQuery<ParseObject>("request");
        requestQuery.whereNotEqualTo("rider", ParseUser.getCurrentUser().getUsername());
        requestQuery.whereNear("location", new ParseGeoPoint(lastKnownPosition.getLatitude(), lastKnownPosition.getLongitude()));
        requestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        if (mMap != null) {
                            List<Marker> markers = new ArrayList<>();
                            mMap.clear();
                            for (ParseObject object : objects) {
                                ParseGeoPoint parseGeoPoint = (ParseGeoPoint) object.get("location");
                                LatLng userLocation = new LatLng(parseGeoPoint.getLatitude(), parseGeoPoint.getLongitude());
                                String rider = object.getString("rider");

                                MarkerOptions riderRequestMarker = new MarkerOptions();
                                riderRequestMarker.position(userLocation);
                                riderRequestMarker.title(rider);
                                riderRequestMarker.anchor(0.5f, 0.5f);
                                riderRequestMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.rider_marker));

                                markers.add(mMap.addMarker(riderRequestMarker));
                            }

                            //Add the current driver position
                            MarkerOptions riderRequestMarker = new MarkerOptions();
                            LatLng driverLocation = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                            riderRequestMarker.position(driverLocation);
                            riderRequestMarker.title(ParseUser.getCurrentUser().getUsername());
                            riderRequestMarker.anchor(0.5f, 0.5f);
                            riderRequestMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            markers.add(mMap.addMarker(riderRequestMarker));

                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            for (Marker marker : markers) {
                                builder.include(marker.getPosition());
                            }

                            LatLngBounds bounds = builder.build();
                            int padding = 0; // offset from edges of the map in pixels
                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                            mMap.animateCamera(cu);
                        }
                    }
                }
            }
        });*/
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
        mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        //Prepare Database
        mDataBase = FirebaseDatabase.getInstance();
        mRequestsDatabaseReference = mDataBase.getReference().child("requests");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {

                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Request request = dataSnapshot.getValue(Request.class);
                    //TODO check if marker already exist, otherwise add it
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
                updateMap(location);
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

    /**
     * Add a marker to user position
     * @param location
     */
    private void updateMap(Location location) {
        //TODO: Add live query here and on server configuration. We need to update map everytime Requests change
        updateRequests(location);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(new UserWindowAdapter());
        setUpLocationManager();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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
