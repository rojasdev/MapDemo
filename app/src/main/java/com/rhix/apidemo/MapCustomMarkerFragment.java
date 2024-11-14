package com.rhix.apidemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapCustomMarkerFragment extends Fragment {
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private Marker customMarker; // Declare the custom marker here

    // Declare ActivityResultLauncher for location permission
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false);

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
        GeoPoint startPoint = new GeoPoint(10.53724, 122.83202);  // Example: New York City
        mapView.getController().setCenter(startPoint);
        mapView.getController().setZoom(18.0);

        // Add MyLocation overlay for showing user location
        locationOverlay = new MyLocationNewOverlay(mapView);
        locationOverlay.setPersonIcon(getBitmapFromDrawable(R.drawable.ic_center)); // Set custom location marker
        locationOverlay.setDrawAccuracyEnabled(true); // Optional: draw accuracy circle
        mapView.getOverlays().add(locationOverlay);

        // Initialize and add the custom marker
        customMarker = addCustomMarker(startPoint); // Add a custom marker at the starting point

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
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            locationPermissionLauncher.launch(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    private void enableLocationOverlay() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationOverlay.enableMyLocation();
            locationOverlay.enableFollowLocation();
            locationOverlay.runOnFirstFix(() -> {
                requireActivity().runOnUiThread(() -> {
                    // Optionally set the center of the map on first fix
                    GeoPoint userLocation = locationOverlay.getMyLocation();
                    if (userLocation != null) {
                        mapView.getController().animateTo(userLocation);
                    }
                });
            });
        }
    }

    private Marker addCustomMarker(GeoPoint position) {
        // Create a new marker
        Marker marker = new Marker(mapView);
        marker.setPosition(position);
        marker.setTitle("New York City"); // Set title for the marker

        // Set custom icon for the marker
        Bitmap scaledMarker = getScaledBitmap(R.drawable.ic_center, 100, 100); // Replace with your custom marker drawable
        marker.setIcon(new BitmapDrawable(getResources(), scaledMarker));

        // Set anchor for the marker
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Add the marker to the map
        mapView.getOverlays().add(marker);

        return marker;
    }

    private Bitmap getBitmapFromDrawable(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(requireContext(), drawableId);
        if (drawable == null) return null;

        // Create a Bitmap from the drawable
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private Bitmap getScaledBitmap(int drawableId, int width, int height) {
        Drawable drawable = ContextCompat.getDrawable(requireContext(), drawableId);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
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