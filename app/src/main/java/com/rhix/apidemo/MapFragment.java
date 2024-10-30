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
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapFragment extends Fragment {
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;

    // Declare ActivityResultLauncher for location permission
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
       // Initialize osmdroid configuration with SharedPreferences directly
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE);
        Configuration.getInstance().load(requireContext(), sharedPreferences);
        Configuration.getInstance().setUserAgentValue("com.rhix.apidemo");

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Set up the MapView
        mapView = view.findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK); // Use MAPNIK for OSM tiles
        mapView.setMultiTouchControls(true);

        // Set initial map position and zoom level
        mapView.getController().setZoom(18.0);
        GeoPoint startPoint = new GeoPoint(40.748817, -73.985428);  // Example: New York City
        mapView.getController().setCenter(startPoint);

        // Add MyLocation overlay for showing user location
        locationOverlay = new MyLocationNewOverlay(mapView);
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
