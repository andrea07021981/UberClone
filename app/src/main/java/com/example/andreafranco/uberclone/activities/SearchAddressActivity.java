package com.example.andreafranco.uberclone.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Log;

import com.example.andreafranco.uberclone.BuildConfig;
import com.example.andreafranco.uberclone.R;
import com.example.andreafranco.uberclone.utils.GeoUtils;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import static com.example.andreafranco.uberclone.BuildConfig.DEBUG;

public class SearchAddressActivity extends BaseActivity {

    private static final String TAG = SearchAddressActivity.class.getSimpleName();
    public static final String DESTINATION = "destination";
    public static final String ORIGIN = "origin";

    private LocationManager mLocationManager;
    private PlaceAutocompleteFragment mOriginPlaceAutoCompleteFragment;
    private PlaceAutocompleteFragment mDestinationPlaceAutoCompleteFragment;
    private AppCompatEditText mOriginEditText;
    private Address mOriginAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_address);
        setupToolbar();

        //TODO add check for permissions or pass the current position from map
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location lastKnownPosition = GeoUtils.getLastKnownPosition(mLocationManager);
        GeoUtils.getAddressFromLocation(
                lastKnownPosition.getLatitude(),
                lastKnownPosition.getLongitude(),
                this,
                new GeocoderHandler());

        mOriginPlaceAutoCompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.origin_place_autocomplete_fragment);
        mOriginPlaceAutoCompleteFragment.setHint("Origin");
        mOriginEditText = (AppCompatEditText) mOriginPlaceAutoCompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input);

        mOriginPlaceAutoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        mDestinationPlaceAutoCompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.destination_place_autocomplete_fragment);

        mDestinationPlaceAutoCompleteFragment.setHint("Destination");
        mDestinationPlaceAutoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
                if (!TextUtils.isEmpty(mOriginEditText.getText().toString().trim())) {
                    //We have two positions, go back
                    Intent intentResult = new Intent();
                    intentResult.putExtra(DESTINATION, place.getLatLng());
                    intentResult.putExtra(ORIGIN, new LatLng(mOriginAddress.getLatitude(), mOriginAddress.getLongitude()));
                    setResult(RESULT_OK, intentResult);
                    finish();
                }
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    mOriginAddress = bundle.getParcelable("address");
                    mOriginEditText.setText(getFormattedAddress(mOriginAddress));
                    mOriginEditText.setEnabled(false);
                    if (DEBUG) {
                        Intent intentResult = new Intent();
                        intentResult.putExtra(DESTINATION, new LatLng(45.406374,11.884550));
                        intentResult.putExtra(ORIGIN, new LatLng(mOriginAddress.getLatitude(), mOriginAddress.getLongitude()));
                        setResult(RESULT_OK, intentResult);
                        finish();
                    }
                    break;
                default:
                    mOriginAddress = null;
            }
            Log.e("location Address=", mOriginAddress.getLocality());
        }
    }

    private String getFormattedAddress(Address locationAddress) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < locationAddress.getMaxAddressLineIndex(); i++) {
            sb.append(locationAddress.getAddressLine(i)); //.append("\n");
        }
        sb.append(locationAddress.getLocality()).append("\n");
        sb.append(locationAddress.getPostalCode()).append("\n");
        sb.append(locationAddress.getCountryName());
        return sb.toString();
    }
}
