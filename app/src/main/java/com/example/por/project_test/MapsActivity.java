package com.example.por.project_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, HttpRequestCallback, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private double lat, lon;
    private String id, token;
    private boolean status = true, click = false;
    private double longmark, latmark;
    AESEncryption aesEncryption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SharedPreferences sp = getSharedPreferences("MySetting", MODE_PRIVATE);
        id = sp.getString("user_id_current", "-1");
        token = sp.getString("token", "-1");

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)//ถ้าต่อสำเร็จบอกด้วย
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)//ใช้serviceอะไร
                    .build();
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {//พร้อมใช้งาน
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);//มหุดบนแผนที่

        // Add a marker in Sydney and move the camera

        LatLng sydney = new LatLng(13.722403, 100.529343);//ละติจูดลองจิจูด
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setOnMarkerDragListener(this);
        if (getIntent().hasExtra("lat") && getIntent().hasExtra("lon")) {
            LatLng bangkok = new LatLng(getIntent().getDoubleExtra("lat", 0), getIntent().getDoubleExtra("lon", 0));
            MarkerOptions marker = new MarkerOptions().position(bangkok).title("Your location");
            mMap.addMarker(marker);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bangkok, 18));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 999);
            return;
        } else {
            requestLocation();
        }
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Need permission", Toast.LENGTH_SHORT).show();
            return;
        }//ถ้ากดไม่ให้


        if ((getIntent().hasExtra("friendid")) && (status)) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);//ขอตำแนหน่ง่าสุด
            if (mLastLocation != null) {//มีค่า
                lat = mLastLocation.getLatitude();
                lon = mLastLocation.getLongitude();
                longmark = lon;
                latmark = lat;
                LatLng bangkok = new LatLng(lat, lon);
                MarkerOptions marker = new MarkerOptions().position(bangkok).title("Your location").draggable(true);
                mMap.addMarker(marker);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bangkok, 18));
                status = false;
            } else {
                Toast.makeText(this, "Cann't find location", Toast.LENGTH_SHORT).show();
            }
        } else if ((getIntent().hasExtra("groupid")) && (status)) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);//ขอตำแนหน่ง่าสุด
            if (mLastLocation != null) {//มีค่า
                lat = mLastLocation.getLatitude();
                lon = mLastLocation.getLongitude();
                longmark = lon;
                latmark = lat;
                LatLng bangkok = new LatLng(lat, lon);
                MarkerOptions marker = new MarkerOptions().position(bangkok).title("Your location").draggable(true);
                mMap.addMarker(marker);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bangkok, 18));
                status = false;
            } else {
                Toast.makeText(this, "Cann't find location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        requestLocation();//กดallowสำหรับครั้งแรก
    }

    @Override
    public void onConnectionSuspended(int i) {//ถูกขัดจังวะ

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {


        if (getIntent().hasExtra("friendid") && getIntent().hasExtra("sharedkey") && (!click)) {

            Intent intent = getIntent();
            String friendid = intent.getStringExtra("friendid");

            aesEncryption = new AESEncryption(intent.getStringExtra("sharedkey"));

//            MessageActivity messageActivity = new MessageActivity();
            String lonEncrypt = aesEncryption.encrypt(longmark + "");
            String latEncrypt = aesEncryption.encrypt(latmark + "");

            BackgoundWorker backgoundWorker = new BackgoundWorker(MapsActivity.this);
            backgoundWorker.execute("sendmessage", id, friendid, "", "map", "", latEncrypt, lonEncrypt, token, "");
            click = true;
            return true;//ไม่ต้องการให้bahivior defaultของแมพ
        } else if (getIntent().hasExtra("groupid") && getIntent().hasExtra("sharedkey") && (!click)) {


            Intent intent = getIntent();
            String groupid = intent.getStringExtra("groupid");

            aesEncryption = new AESEncryption(intent.getStringExtra("sharedkey"));
//            GroupMessageActivity groupMessageActivity = new GroupMessageActivity();
            String lonEncrypt = aesEncryption.encrypt(longmark + "");
            String latEncrypt = aesEncryption.encrypt(latmark + "");

            BackgoundWorker backgoundWorker = new BackgoundWorker(MapsActivity.this);
            backgoundWorker.execute("sendmessagegroup", id, groupid, "", "map", "", latEncrypt, lonEncrypt, token);
            click = true;
            return true;//ไม่ต้องการให้bahivior defaultของแมพ
        }
        //Toast.makeText(this, marker.getPosition().toString(), Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onResult(String[] result, ArrayList<Object> objectses) {
        finish();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        longmark = marker.getPosition().longitude;
        latmark = marker.getPosition().latitude;
    }


}