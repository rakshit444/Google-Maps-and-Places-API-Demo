package com.api.uniconn.Activites;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.api.uniconn.Models.MessageEvent;
import com.api.uniconn.Rest.ApiService;
import com.api.uniconn.Rest.Models.NearbySearch;
import com.api.uniconn.Rest.Models.Result;
import com.api.uniconn.Rest.RestClient;
import com.api.uniconn.java.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

public class activity_details extends AppCompatActivity implements OnMapReadyCallback {

    private static final String api_key = "";
    private static final String TAG = "Activity Details";
    private List<Result> result;
    private int CardSelected;
    private GoogleMap map;
    private ImageView imageView;
    private ProgressBar spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        savedInstanceState = getIntent().getExtras();
        ActionBar ab = getActionBar();
        spinner = (ProgressBar)findViewById(R.id.Progress1);
        imageView = (ImageView) findViewById(R.id.image1);
        CardSelected = savedInstanceState.getInt("position");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {

        return super.onCreateView(parent, name, context, attrs);

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        getSupportActionBar().setTitle(result.get(CardSelected).getName());
        spinner.setVisibility(View.VISIBLE);
        Log.i(TAG,"START");


        if(isNetworkAvailable() == false){
            Toast.makeText(getApplicationContext(),"Check Network Connection",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Picasso.with(getApplicationContext()).load("https://maps.googleapis.com/maps/api/place/photo?photoreference=" + result.get(CardSelected).getPhotos().get(0).getPhotoReference() + "&maxheight=800&maxwidth=800&key=" + api_key).error(R.drawable.error).into(imageView);
        }catch (Exception e){
            Picasso.with(getApplicationContext()).load(R.drawable.error).into(imageView);
        }

    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        spinner.setVisibility(View.GONE);
        super.onPause();

    }

    @Subscribe(sticky = true)
    public void onMessageEvent(MessageEvent event) {
        result = event.getResult();
        Toast.makeText(getApplicationContext(), result.get(1).getName(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Double lat = result.get(CardSelected).getGeometry().getLocation().getLat();
        Double lng = result.get(CardSelected).getGeometry().getLocation().getLng();
        CameraPosition googlePlex = CameraPosition.builder()
                .target(new LatLng(lat,lng))
                .zoom(16)
                .bearing(0)
                .tilt(45)
                .build();

        map.moveCamera(CameraUpdateFactory.newCameraPosition(googlePlex));
        map.addMarker(new MarkerOptions()
                .position(new LatLng(result.get(CardSelected).getGeometry().getLocation().getLat(),result.get(CardSelected).getGeometry().getLocation().getLng())
                        ))
                .setTitle(result.get(CardSelected).getName());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}
