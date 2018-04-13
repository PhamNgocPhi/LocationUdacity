package com.example.admin.locationudacity;

import android.databinding.DataBindingUtil;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.admin.locationudacity.databinding.ActivityMainBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap m_map;
    private boolean mapReady = false;
    private ActivityMainBinding binding;
    private LatLng polyline = new LatLng(21.028801,105.8321062);

    private MarkerOptions markerOption1, markerOption2;

    private static final  CameraPosition HANOI = CameraPosition.builder()
            .target(new LatLng(21.0279784,105.8274621))
            .zoom(15)
            .build();
    private static final  CameraPosition TOKYO = CameraPosition.builder()
            .target(new LatLng(35.6691074,139.6012945))
            .zoom(15)
            .build();
    private static final  CameraPosition BACKINH = CameraPosition.builder()
            .target(new LatLng(39.9040612,116.3778722))
            .zoom(15)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);

        markerOption1 = new MarkerOptions()
                .position(new LatLng(21.0264927,105.8319627))
                .title("Truong cao dang y te ha noi");

        markerOption2 = new MarkerOptions()
                .position(new LatLng(21.028801,105.8321062))
                .title("So ke hoach va dau tu tp ha noi");

        binding.btnMap.setOnClickListener(view -> {
            if(mapReady)
                m_map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        });
        binding.btnHybrid.setOnClickListener(view -> {
            if(mapReady)
                m_map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        });
        binding.btnSatellite.setOnClickListener(view -> {
            if(mapReady)
                m_map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        });
        binding.btnHaNoi.setOnClickListener(v -> {
            if(mapReady)
                flyTo(HANOI);
        });

        binding.btnBacKinh.setOnClickListener(v -> {
            if(mapReady)
                flyTo(BACKINH);
        });

        binding.btnTokyo.setOnClickListener(v -> {
            if(mapReady)
                flyTo(TOKYO);
        });

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapReady = true;
        m_map = googleMap;
        m_map.addMarker(markerOption1);
        m_map.addMarker(markerOption2);
        m_map.addCircle(new CircleOptions().center(polyline)
                .radius(500)
                .strokeColor(Color.GREEN)
                .fillColor(Color.argb(64,0,255,0)));
        flyTo(HANOI);
    }

    private void flyTo(CameraPosition target) {
        m_map.animateCamera(CameraUpdateFactory.newCameraPosition(target), 10000, null);
    }
}
