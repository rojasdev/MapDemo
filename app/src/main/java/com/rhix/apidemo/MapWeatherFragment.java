package com.rhix.apidemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapWeatherFragment extends Fragment {
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private Marker weatherMarker;
    private final String WEATHER_API_KEY = BuildConfig.API_KEY;
    private final String WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/";

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
        Configuration.getInstance().setUserAgentValue("com.rhix.apidemo");

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(18.0);
        GeoPoint startPoint = new GeoPoint(10.53724, 122.83202);
        mapController.setCenter(startPoint);

        locationOverlay = new MyLocationNewOverlay(mapView);
        mapView.getOverlays().add(locationOverlay);

        weatherMarker = new Marker(mapView);
        mapView.getOverlays().add(weatherMarker);

        requestLocationPermission();

        Button zoomInButton = view.findViewById(R.id.zoom_in_button);
        Button zoomOutButton = view.findViewById(R.id.zoom_out_button);

        zoomInButton.setOnClickListener(v -> mapView.getController().zoomIn());
        zoomOutButton.setOnClickListener(v -> mapView.getController().zoomOut());

        return view;
    }

    private void requestLocationPermission() {
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void enableLocationOverlay() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationOverlay.enableMyLocation();
            locationOverlay.enableFollowLocation();
            locationOverlay.runOnFirstFix(() -> {
                GeoPoint myLocation = locationOverlay.getMyLocation();
                if (myLocation != null) {
                    fetchWeatherData(myLocation.getLatitude(), myLocation.getLongitude());
                }
            });
        }
    }

    private void fetchWeatherData(double latitude, double longitude) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WEATHER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApiServiceTwo apiService = retrofit.create(WeatherApiServiceTwo.class);

        Call<WeatherResponseTwo> call = apiService.getWeather(latitude, longitude, WEATHER_API_KEY, "metric");

        call.enqueue(new Callback<WeatherResponseTwo>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponseTwo> call, @NonNull Response<WeatherResponseTwo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String temperature = response.body().getMain().getTemp() + "°C";
                    fetchLocationNameWithNominatim(latitude, longitude, temperature);
                    //updateWeatherMarker(latitude, longitude, temperature);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponseTwo> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Failed to load weather data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLocationNameWithNominatim(double latitude, double longitude, String temperature) {
        Log.v("Location: ", "Retrieving........");
        Retrofit nominatimRetrofit = new Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApiServiceTwo apiService = nominatimRetrofit.create(WeatherApiServiceTwo.class);

        Call<NominatimResponse> call = apiService.reverseGeocodeWithNominatim(latitude, longitude, "json");

        call.enqueue(new Callback<NominatimResponse>() {
            @Override
            public void onResponse(@NonNull Call<NominatimResponse> call, @NonNull Response<NominatimResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String locationText = response.body().getDisplayName() + ": " + temperature;
                    //Log.v("Location: ", "---------------------------");
                    //Log.v("Location: ", locationText);

                    updateWeatherMarker(latitude, longitude, locationText);
                }
            }

            @Override
            public void onFailure(@NonNull Call<NominatimResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Failed to load location name with Nominatim", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWeatherMarker(double latitude, double longitude, String locationText) {
        GeoPoint location = new GeoPoint(latitude, longitude);
        weatherMarker.setPosition(location);
        weatherMarker.setTitle(locationText);
        weatherMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getController().animateTo(location);
        mapView.invalidate();
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
