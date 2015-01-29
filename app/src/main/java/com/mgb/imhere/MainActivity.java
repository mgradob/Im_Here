package com.mgb.imhere;

import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        ObservableScrollViewCallbacks {

    private View mMapView, mToolbarView;
    private TextView mNextLocationView;
    private ObservableScrollView mScrollView;

    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private Location nextLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapView = findViewById(R.id.main_map);
        mToolbarView = findViewById(R.id.main_toolbar);
        mNextLocationView = (TextView) findViewById(R.id.main_next_location);

        mScrollView = (ObservableScrollView) findViewById(R.id.main_scroll);
        mScrollView.setScrollViewCallbacks(this);

        initMap();

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                nextLocation = new Location("NEXT_LOCATION");
                nextLocation.setLatitude(latLng.latitude);
                nextLocation.setLongitude(latLng.longitude);

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title("Next Location");
                mGoogleMap.addMarker(markerOptions);
                mNextLocationView.setText(String.format("%.4f, %.4f", latLng.latitude, latLng.longitude));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initMap();
    }

    //region App methods.
    private void enableGps() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private void initMap() {
        if (mGoogleMap == null) {
            mGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.main_map)).getMap();

            if (mGoogleMap == null) {
                Toast.makeText(this, "Unable to get map", Toast.LENGTH_SHORT).show();
            }
        }

        mGoogleMap.setMyLocationEnabled(true);

        enableGps();

        buildGoogleApiClient();

        createLocationRequest();

        setCurrentLocation();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void updateCameraPosition(Location location) {
        if (location != null) {
            CameraPosition mCameraPosition = new CameraPosition
                    .Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .build();
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(16), 1000, null);
        } else {
            Toast.makeText(this, "Unable to set location", Toast.LENGTH_SHORT).show();
        }
    }

    private void setCurrentLocation() {
        Location mLastLocation = mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(new Criteria(), true));
        updateCameraPosition(mLastLocation);
    }

    //region GoogleMapsApi methods.
    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        setCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        updateCameraPosition(location);
    }
    //endregion

    //region ObservableScrollView methods.
    @Override
    public void onScrollChanged(int i, boolean b, boolean b2) {
        int baseColor = getResources().getColor(R.color.background);
        float alpha = 1 - (float) Math.max(0, 70 - i) / 70;
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(alpha, baseColor));
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }
    //endregion


}
