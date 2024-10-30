package com.rhix.apidemo;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapPointsFragment extends Fragment {
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (fineLocationGranted != null && fineLocationGranted) {
                    enableLocationOverlay();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    enableLocationOverlay();
                } else {
                    // Handle denied permissions as needed
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE);
        Configuration.getInstance().load(requireContext(), sharedPreferences);
        Configuration.getInstance().setUserAgentValue("com.yourapp.package");

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Set up the MapView
        mapView = view.findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Set initial map position and zoom level
        mapView.getController().setZoom(16.0);
        GeoPoint startPoint = new GeoPoint(10.668328, 122.958444);
        mapView.getController().setCenter(startPoint);

        // Add markers for the points
        addMarkersToMap();

        // Initialize location overlay with GpsMyLocationProvider
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()), mapView);
        mapView.getOverlays().add(locationOverlay);

        // Request location permission
        requestLocationPermission();

        // Set up custom zoom buttons
        Button zoomInButton = view.findViewById(R.id.zoom_in_button);
        Button zoomOutButton = view.findViewById(R.id.zoom_out_button);

        zoomInButton.setOnClickListener(v -> mapView.getController().zoomIn());
        zoomOutButton.setOnClickListener(v -> mapView.getController().zoomOut());

        return view;
    }

    private void addMarkersToMap() {
        GeoPoint[] points = new GeoPoint[]{
                new GeoPoint(10.668328, 122.958444),
                new GeoPoint(10.674155, 122.961091),
                new GeoPoint(10.675039, 122.961242),
                new GeoPoint(10.675847, 122.961027),
                new GeoPoint(10.683639, 122.957072),
                new GeoPoint(10.683985, 122.956504)
        };

        for (GeoPoint point : points) {
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle("Point: " + point.getLatitude() + ", " + point.getLongitude());
            mapView.getOverlays().add(marker);
        }
    }

    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            locationPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    private void enableLocationOverlay() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationOverlay.enableMyLocation();
            locationOverlay.enableFollowLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDetach();
    }
}
