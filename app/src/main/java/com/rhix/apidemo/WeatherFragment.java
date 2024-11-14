package com.rhix.apidemo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherFragment extends Fragment {

    private TextView cityNameTextView;
    private TextView temperatureTextView;
    private TextView descriptionTextView;

    private final String API_KEY = BuildConfig.API_KEY;
    private final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        cityNameTextView = view.findViewById(R.id.tv_city_name);
        temperatureTextView = view.findViewById(R.id.tv_temperature);
        descriptionTextView = view.findViewById(R.id.tv_description);

        getWeatherData("Bacolod");

        return view;
    }

    private void getWeatherData(String city) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApiService apiService = retrofit.create(WeatherApiService.class);

        Call<WeatherResponse> call = apiService.getWeather(city, API_KEY, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("Result: ", "Loading...");
                    WeatherResponse weatherData = response.body();
                    cityNameTextView.setText(weatherData.getName());
                    temperatureTextView.setText(weatherData.getMain().getTemp() + "°C");
                    descriptionTextView.setText(weatherData.getWeather()[0].getDescription());
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                // Handle the error
                cityNameTextView.setText("Error loading data");
            }
        });
    }
}
