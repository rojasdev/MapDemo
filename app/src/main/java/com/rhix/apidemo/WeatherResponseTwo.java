package com.rhix.apidemo;

public class WeatherResponseTwo {
    private Main main;

    public Main getMain() {
        return main;
    }

    public class Main {
        private double temp;

        public double getTemp() {
            return temp;
        }
    }
}
