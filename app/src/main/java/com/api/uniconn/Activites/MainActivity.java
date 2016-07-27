package com.api.uniconn.Activites;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.api.uniconn.Adapters.CustomAdapter;
import com.api.uniconn.Adapters.EndlessRecyclerViewScrollListener;
import com.api.uniconn.Models.CardViewItems;
import com.api.uniconn.Models.MessageEvent;
import com.api.uniconn.java.R;
import com.api.uniconn.Rest.ApiService;
import com.api.uniconn.Rest.Models.NearbySearch;
import com.api.uniconn.Rest.Models.Result;
import com.api.uniconn.Rest.RestClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "Main Activity";
    private static final String api_key = "";
    private GoogleApiClient mGoogleApiClient;
    private RestClient mRestClient;
    private Location mLastLocation;
    private ApiService mApiService;
    private RecyclerView mRecycleView;
    private CustomAdapter mCustomAdaptor;
    private ArrayList<CardViewItems> mCardViewItems;
    private List<Result> result;
    private List<String> distance;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static String types = "airport|amusement_part|food|beauty_salon|bar|book_store|bus_station|cafe|church|clothing_store|dentist|doctor|department_store|police|school|shopping_mall|store|university|park|hospital";
    private ProgressBar spinner;
    private ProgressBar spinner1;
    private String nextPageToken = null;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = (ProgressBar) findViewById(R.id.Progress);
        spinner1 = (ProgressBar) findViewById(R.id.Progress2);
        spinner1.setVisibility(View.GONE);
        mRestClient = new RestClient();
        mApiService = mRestClient.getApiService();
        mRecycleView = (RecyclerView) findViewById(R.id.my_recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mRecycleView.setHasFixedSize(true);
        mRecycleView.setLayoutManager(layoutManager);
        mCardViewItems = new ArrayList<CardViewItems>();
        mRecycleView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                 getNextPage(totalItemsCount);

            }
        });
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }
    }

    private void displayLocation() {
        spinner.setVisibility(View.VISIBLE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //       startLocationUpdates();
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            Log.i(TAG, latitude + ", " + longitude);

            getNearbyPlaces();

        } else {

            Toast.makeText(getApplicationContext(), "Couldn't get the location. Make sure location is enabled on the device", Toast.LENGTH_LONG).show();
        }
    }

    private void getNearbyPlaces() {
        Call<NearbySearch> call = mApiService.getGetOutputFormats(mLastLocation.getLatitude() + "," + mLastLocation.getLongitude(), types, "distance", api_key);
        call.enqueue(new Callback<NearbySearch>() {
            @Override
            public void onResponse(Call<NearbySearch> call, Response<NearbySearch> response) {
                Log.i(TAG, response.message() + "");
                NearbySearch nearbySearch = response.body();
                nextPageToken = nearbySearch.getNextPageToken();
                result = nearbySearch.getResults();
                List<String> type;
                Log.i(TAG, result.size() + "");
                EventBus.getDefault().postSticky(new MessageEvent(result));
                for (int i = 0; i < result.size(); i++) {
                    type = result.get(i).getTypes();

                    Log.i(TAG, result.get(i).getName());
                    mCardViewItems.add(new CardViewItems(result.get(i).getName(), type.get(0), distFrom(mLastLocation.getLatitude(), mLastLocation.getLongitude(), result.get(i).getGeometry().getLocation().getLat(), result.get(i).getGeometry().getLocation().getLng()) + " km", result.get(i).getVicinity()));
                }
                Log.i(TAG, mCardViewItems.size() + "");
                mCustomAdaptor = new CustomAdapter(mCardViewItems);
                mRecycleView.setAdapter(mCustomAdaptor);
                spinner.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<NearbySearch> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Check Network Connection", Toast.LENGTH_LONG).show();
                spinner.setVisibility(View.GONE);
            }
        });
    }

    private void getNextPage(final int totlaItems) {
        spinner1.setVisibility(View.VISIBLE);
        final int currSize = mCustomAdaptor.getItemCount();
        Call<NearbySearch> call = mApiService.getNextPage(mLastLocation.getLatitude() + "," + mLastLocation.getLongitude(), types, "distance", api_key, nextPageToken);
        call.enqueue(new Callback<NearbySearch>() {
            @Override
            public void onResponse(Call<NearbySearch> call, Response<NearbySearch> response) {
                Log.i(TAG, response.message() + "");
                NearbySearch nearbySearch1 = response.body();
                result.addAll(nearbySearch1.getResults());
                List<String> type;
                Log.i(TAG, result.size() + "");
                EventBus.getDefault().postSticky(new MessageEvent(result));
                for (int i = currSize; i < result.size(); i++) {
                    type = result.get(i).getTypes();

                    Log.i(TAG, result.get(i).getName());
                    mCardViewItems.add(new CardViewItems(result.get(i).getName(), type.get(0), distFrom(mLastLocation.getLatitude(), mLastLocation.getLongitude(), result.get(i).getGeometry().getLocation().getLat(), result.get(i).getGeometry().getLocation().getLng()) + " km", result.get(i).getVicinity()));
                }
                Log.i(TAG, mCardViewItems.size() + "");
                mCustomAdaptor.notifyItemRangeInserted(currSize, mCardViewItems.size());
                spinner1.setVisibility(View.GONE);
                /*while (!nearbySearch1.getNextPageToken().equals(null)){
                    getNextPage(nearbySearch1.getNextPageToken());
                }*/

            }

            @Override
            public void onFailure(Call<NearbySearch> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Check Network Connection", Toast.LENGTH_LONG).show();
                spinner1.setVisibility(View.GONE);

            }
        });

    }

    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;
        double roundOff = Math.round(dist * 100.0) / 100.0;
        System.out.println(roundOff);
        return roundOff;
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        if (isNetworkAvailable() == false) {
            spinner.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Check Network Connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search1:
                displayLocation();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onStop() {
        spinner.setVisibility(View.GONE);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
