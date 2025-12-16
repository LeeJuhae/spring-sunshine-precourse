package sunshine.weather.dto;

public record ForecastResponse(Current current) {
    public record Current(
            double temperature_2m,
            double apparent_temperature,
            int weather_code,
            int relative_humidity_2m,
            double wind_speed_10m
    ) {}
}