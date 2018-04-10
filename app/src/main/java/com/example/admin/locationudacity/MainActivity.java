package com.example.admin.locationudacity;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.admin.locationudacity.databinding.ActivityMainBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap m_map;
    private boolean mapReady = false;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
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

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapReady = true;
        m_map = googleMap;
        LatLng weFit = new LatLng(21.0031415, 105.834901);
        CameraPosition target = CameraPosition.builder().target(weFit).zoom(15).build();
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));
    }
}
