package arnes.respati.friendsintheworld.Activities;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import arnes.respati.friendsintheworld.Communications;
import arnes.respati.friendsintheworld.Controller;
import arnes.respati.friendsintheworld.Network.ReceiveListener;
import arnes.respati.friendsintheworld.R;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String PREF_NAME = "prefs";
    private static final String KEY_NAME = "name";

    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 16f;

    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private Marker mCurrLocationMarker;
    private Location mLastLocation;
    private double latitude, longitude;

    private Controller controller;

    private LatLng userLatlng;
    private String username;
    private boolean joined_a_group = false;
    private boolean english = true;

    ArrayList<String> membernames = new ArrayList<>();
    ArrayList<Double> memberLat = new ArrayList<>();
    ArrayList<Double> memberLong = new ArrayList<>();

    ArrayList <Marker> markers = new ArrayList<>();
    private Marker userMark;

    Observer<Boolean> joined = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable final Boolean joined) {
            // Update the UI
            joined_a_group = joined;
        }
    };

    Observer<Boolean> isEnglish = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable final Boolean isEnglish) {
            // Update the UI
            english = isEnglish;
        }
    };

    Observer<ArrayList<String>> updateNames = new Observer<ArrayList<String>>() {
        @Override
        public void onChanged(@Nullable final ArrayList<String> newNames) {
            // Update the UI

            Log.d(TAG, "onChanged: member names " + newNames);
            Log.d(TAG, "onChanged: lat " + controller.getCurrentMemberLat().getValue());
            Log.d(TAG, "onChanged: long " + controller.getCurrentMemberLong().getValue());

            membernames = newNames;
            memberLat = controller.getCurrentMemberLat().getValue();
            memberLong = controller.getCurrentMemberLong().getValue();

            Log.d(TAG, "memberLat: " + memberLat);
            Log.d(TAG, "memberLong: " + memberLong);


            if (membernames.size() > 0) {
                addMarkers(membernames, memberLat, memberLong);
            }
//
//            else return;
//            if (newNames != null && markers.size()>0) {
//                for (int i = 0; i < newNames.size(); i++) {
//                    markers.get(i).setTitle(newNames.get(i));
//                }
//            }

        }
    };

    Observer<ArrayList<Double>> updateLat = new Observer<ArrayList<Double>>() {
        @Override
        public void onChanged(@Nullable final ArrayList<Double> newlat) {
            // Update the UI
            membernames = controller.getCurrentMemberNames().getValue();
            memberLat = newlat;
            memberLong = controller.getCurrentMemberLong().getValue();

            if (memberLat.size() > 0) {
                addMarkers(membernames, memberLat, memberLong);
            }
//
//            else return;

//            for (int i = 0; i < markers.size() ; i++) {
//                markers.get(i).setPosition(new LatLng(newlat.get(i), controller.getCurrentMemberLong().getValue().get(i)));
//            }
        }
    };

    Observer<ArrayList<Double>> updateLong = new Observer<ArrayList<Double>>() {
        @Override
        public void onChanged(@Nullable final ArrayList<Double> newlong) {
            // Update the UI
            membernames = controller.getCurrentMemberNames().getValue();
            memberLat = controller.getCurrentMemberLat().getValue();
            memberLong = newlong;

            if (memberLong.size() > 0) {
                addMarkers(membernames, memberLat, memberLong);
            }
////
////            else return;
//            for (int i = 0; i < markers.size() ; i++) {
//                markers.get(i).setPosition(new LatLng(controller.getCurrentMemberLat().getValue().get(i), newlong.get(i)));
//            }
        }
    };

    Observer<String> updateGroup = new Observer<String>() {
        @Override
        public void onChanged(@Nullable final String currentGroup) {
            // Update the UI
            addMarkers(controller.getCurrentMemberNames().getValue(),
                    controller.getCurrentMemberLat().getValue(), controller.getCurrentMemberLong().getValue());
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        launchOnBoarding();

        controller = (Controller) getApplication();
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        getLocationPermission();

        SharedPreferences sharedPreferences = Objects.requireNonNull(this).getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String tempname = sharedPreferences.getString(KEY_NAME,"");
        controller.getCurrentUserName().postValue(tempname);

//        controller.getCurrentGroupName().observe(this, updateGroup);
        controller.isEnglish().observe(this, isEnglish);
        controller.getCurrentStatus().observe(this, joined);
        username = controller.getCurrentUserName().getValue();
    }

    public void setUserPosition (String groupID, String latitude, String longitude) {
        controller.connection.send(Communications.setPosition(groupID, latitude, longitude));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initializeMap();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
//            controller.getCurrentMemberLat().observe(this, updateLat);
//            controller.getCurrentMemberLong().observe(this, updateLong);
//            controller.getCurrentMemberNames().observe(this, updateNames);
        }
    }

    private void initializeMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initializeMap();
            }
            else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else {
            ActivityCompat.requestPermissions(this,
                    permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000); // twice per minute interval
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        //looper loops message
        try{
            if(mLocationPermissionsGranted){
                mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
        }
        catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);

                Toast.makeText(MapsActivity.this, "Getting user locations", Toast.LENGTH_SHORT).show();

                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                moveCamera(new LatLng(location.getLatitude(), location.getLongitude()));
                setUserLatlng(new LatLng(location.getLatitude(), location.getLongitude()));
//
                setUserPosition(controller.getCurrentGroupID().getValue(), Double.toString(location.getLatitude()) , Double.toString(location.getLongitude()));
                controller.getCurrentUserLat().postValue(location.getLatitude());
                controller.getCurrentUserLong().postValue(location.getLongitude());

                if (joined_a_group) {
                    if (userMark != null) {
                        userMark.remove();
                    }
                    userMark = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title(username)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                }

                addMarkers(controller.getCurrentMemberNames().getValue(),
                        controller.getCurrentMemberLat().getValue(), controller.getCurrentMemberLong().getValue());
            }
            else{
                Log.d(TAG, "onComplete: current location is null");
                Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
            }
        }
    };


    private void addMarkers (ArrayList<String> user, ArrayList<Double> lat, ArrayList<Double> lng) {
        LatLng latLng;
        if (markers != null) {
            for (int i = 0; i < markers.size() ; i++) {
                markers.get(i).remove();
            }
            markers.clear();
            Log.d(TAG, "addMarkers: markers removed");
        }

        if (lat!= null && !lat.isEmpty()){
            Log.d(TAG, "addMarkers: adding marker");
            Log.d("Markers", user + " " + String.valueOf(lat) + " " + String.valueOf(lng));

            for (int i = 0; i < user.size(); i++) {
                Log.d(TAG, "addMarkers: index" + i);
                    
                if (lat.get(i) < 0 && lng.get(i) < 0){
                    break;
                }

//                if (user.get(i).equals(this.username)) {
//                    break;
//                }

                if (!user.get(i).equals(this.username)){
                    latLng = new LatLng(lat.get(i), lng.get(i));

                    markers.add(mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(user.get(i))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
                    Toast.makeText(MapsActivity.this, "Adding Markers", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "addMarkers: markers added");
                    moveCamera(latLng);
                }
            }
        }
    }

    private void moveCamera(LatLng latLng){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    public void setUserLatlng(LatLng latlng) {
        this.userLatlng = latlng;
    }

    public LatLng getUserLatlng () {
        return this.userLatlng;
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.btnHome) {
            if (english) {
                Toast.makeText(MapsActivity.this,
                        getString(R.string.home),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MapsActivity.this,
                        getString(R.string.home2),
                        Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
            MapsActivity.this.startActivity(intent);
        }

        if (id == R.id.btnMap) {
            View popupMap = findViewById(R.id.btnMap); // SAME ID AS MENU ID
            PopupMenu popupMenu = new PopupMenu(this, popupMap);
            popupMenu.inflate(R.menu.popmenu_map);

            if (english) {
                popupMenu.getMenu().getItem(0).setTitle(R.string.show_map);
            }
            else {
                popupMenu.getMenu().getItem(0).setTitle(R.string.show_map2);
            }

            //registering popup with OnMenuItemClickListener
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if (english){
                        Toast.makeText(MapsActivity.this,
                                getString(R.string.map),
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MapsActivity.this,
                                getString(R.string.map2),
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });

            popupMenu.show();

            return true;
        }

        if (id == R.id.btnGroups) {
            View popupGroups = findViewById(R.id.btnGroups); // SAME ID AS MENU ID
            PopupMenu popupMenu = new PopupMenu(this, popupGroups);
            popupMenu.inflate(R.menu.popmenu_group);

            if (english) {
                popupMenu.getMenu().getItem(0).setTitle(R.string.show_mygroup);
                popupMenu.getMenu().getItem(1).setTitle(R.string.show_allgroup);
            }
            else {
                popupMenu.getMenu().getItem(0).setTitle(R.string.show_mygroup2);
                popupMenu.getMenu().getItem(1).setTitle(R.string.show_allgroup2);
            }

            //registering popup with OnMenuItemClickListener
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.pop_myGroup) {
                        if (english){
                            Toast.makeText(MapsActivity.this,
                                    getString(R.string.mygroup),
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(MapsActivity.this,
                                    getString(R.string.mygroup2),
                                    Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(MapsActivity.this, MyGroupActivity.class);
                        MapsActivity.this.startActivity(intent);
                    }
                    if (id == R.id.pop_allGroups) {
                        if (english){
                            Toast.makeText(MapsActivity.this,
                                    getString(R.string.allgroup),
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(MapsActivity.this,
                                    getString(R.string.allgroup2),
                                    Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(MapsActivity.this, AllGroupsActivity.class);
                        MapsActivity.this.startActivity(intent);
                    }
                    return true;
                }
            });

            popupMenu.show();
        }

        if (id == R.id.btnSettings) {
            View popupSettings = findViewById(R.id.btnSettings); // SAME ID AS MENU ID
            PopupMenu popupMenu = new PopupMenu(this, popupSettings);
            popupMenu.inflate(R.menu.popmenu_setting);

            if (english) {
                popupMenu.getMenu().getItem(0).setTitle(R.string.swedish);
                popupMenu.getMenu().getItem(1).setTitle(R.string.english);
            }
            else {
                popupMenu.getMenu().getItem(0).setTitle(R.string.swedish2);
                popupMenu.getMenu().getItem(1).setTitle(R.string.english2);
            }

            //registering popup with OnMenuItemClickListener
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.pop_changeSwedish) {
                        controller.isEnglish().setValue(false);
                        Toast.makeText(MapsActivity.this,
                                getString(R.string.changeSwedish),
                                Toast.LENGTH_SHORT).show();
                    }
                    if (id == R.id.pop_changeEnglish) {
                        controller.isEnglish().setValue(true);
                        Toast.makeText(MapsActivity.this,
                                getString(R.string.changeEnglish),
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });

            popupMenu.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void launchOnBoarding() {
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);

        if (isFirstRun) {
            //show onBoardingActivity
            startActivity(new Intent(MapsActivity.this, OnBoardingActivity.class));
        }
    }

}

