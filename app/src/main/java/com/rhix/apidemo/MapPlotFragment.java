package com.rhix.apidemo;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import java.util.Arrays;
import java.util.List;

public class MapPlotFragment extends Fragment {
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

        // Add markers for the points and draw a line between them
        addMarkersToMap();
        drawLineBetweenPoints();

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
                new GeoPoint(10.679661456360577, 122.96088446926933),
                new GeoPoint(10.680528171358237, 122.96263178591225),
                new GeoPoint(10.678683980411808, 122.96473349189357),
                new GeoPoint(10.677468595948623, 122.96316727417242),
                new GeoPoint(10.677434749405863, 122.96284656472892),
                new GeoPoint(10.677507066175139, 122.96217089109979),
                new GeoPoint(10.67777500620726, 122.96120092033692),
                new GeoPoint(10.678230880119218, 122.96054658923967),
                new GeoPoint(10.679661456360577, 122.96088446926933)
                /*
                new GeoPoint(10.668328, 122.958444),
                new GeoPoint(10.674155, 122.961091),
                new GeoPoint(10.675039, 122.961242),
                new GeoPoint(10.675847, 122.961027),
                new GeoPoint(10.683639, 122.957072),
                new GeoPoint(10.683985, 122.956504)*/
        };

        for (GeoPoint point : points) {
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pin));
            marker.setTitle("Point: " + point.getLatitude() + ", " + point.getLongitude());
            mapView.getOverlays().add(marker);
        }
    }

    private void drawLineBetweenPoints() {
        List<GeoPoint> points = Arrays.asList(
                new GeoPoint(10.679661456360577, 122.96088446926933),
                new GeoPoint(10.680528171358237, 122.96263178591225),
                new GeoPoint(10.678683980411808, 122.96473349189357),
                new GeoPoint(10.677468595948623, 122.96316727417242),
                new GeoPoint(10.677434749405863, 122.96284656472892),
                new GeoPoint(10.677507066175139, 122.96217089109979),
                new GeoPoint(10.67777500620726, 122.96120092033692),
                new GeoPoint(10.678230880119218, 122.96054658923967),
                new GeoPoint(10.679661456360577, 122.96088446926933)
        );

        // Create a Polyline and set the points
        Polyline line = new Polyline();
        line.setPoints(points);

        // Set color and width for the polyline
        line.setColor(ContextCompat.getColor(requireContext(), R.color.red)); // Set color from resources
        line.setWidth(10f); // Line width in pixels (using deprecated method)

        // Add the line to the map
        mapView.getOverlays().add(line);
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