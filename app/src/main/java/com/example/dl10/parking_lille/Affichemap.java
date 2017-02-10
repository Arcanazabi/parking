package com.example.dl10.parking_lille;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Affichemap extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    LatLng camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affichemap);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        Intent intent=getIntent();
        String latitude = intent.getStringExtra("latitude");
        String longitude = intent.getStringExtra("longitude");
        double[] coordonnes = intent.getDoubleArrayExtra("coordonnes");

        Double recuplatitude = Double.parseDouble(latitude);
        Double recuplongitude = Double.parseDouble(longitude);
        camera = new LatLng(recuplatitude, recuplongitude);


        //On ajoute tout les marqueurs pour les autres parcking en vert
        for (int i =0; i<coordonnes.length; i=i+2)
        {
            mMap.addMarker(new MarkerOptions().position(new LatLng(coordonnes[i], coordonnes[i+1])).title("Parking").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }

        //On ajoute le marqueur du parcking demandé en vert
        mMap.addMarker(new MarkerOptions().position(camera).title("Votre Parking"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(camera,15));        ;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }




    }
    @Override
    public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double altitude = location.getAltitude();
        float accuracy = location.getAccuracy();

       /* String msg = String.format(
                getResources().getString(R.string.new_location), String.valueOf(latitude),
                String.valueOf(longitude), String.valueOf(altitude), String.valueOf(accuracy));
        Toast.makeText(getParent().getBaseContext(), msg, Toast.LENGTH_LONG).show();  */

    }


}
