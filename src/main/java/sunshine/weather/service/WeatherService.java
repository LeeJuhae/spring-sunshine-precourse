package sunshine.weather.service;

import org.springframework.stereotype.Service;
import sunshine.weather.model.City;
import sunshine.weather.dto.ForecastResponse;
import sunshine.weather.model.WeatherCode;
import java.util.Map;

@Service
public class WeatherService {
    private final OpenMeteo openMeteo;
    private final Map<String, City> cities;

    public WeatherService(OpenMeteo openMeteo) {
        this.openMeteo = openMeteo;
        this.cities = initializeCities();
    }

    private Map<String, City> initializeCities() {
        return Map.of(
            "seoul", new City("Seoul", 37.5665, 126.9780),
            "tokyo", new City("Tokyo", 35.6762, 139.6503),
            "newyork", new City("New York", 40.7128, -74.0060),
            "paris", new City("Paris", 48.8566, 2.3522),
            "london", new City("London", 51.5074, -0.1278)
        );
    }

    public String getWeatherSummary(String cityName) {
        City city = findCity(cityName);
        ForecastResponse.Current weather = openMeteo.fetchCurrent(city);
        return generateSummary(city, weather);
    }

    private City findCity(String cityName) {
        String normalizedCityName = cityName.toLowerCase();
        if (!cities.containsKey(normalizedCityName)) {
            throw new IllegalArgumentException("지원하지 않는 도시입니다: " + cityName);
        }
        return cities.get(normalizedCityName);
    }

    private String generateSummary(City city, ForecastResponse.Current weather) {
        return String.format(
            "현재 %s의 기온은 %.1f°C이며, 체감온도는 %.1f°C입니다. 습도는 %d%%이고, 풍속은 %.1fm/s입니다. 날씨는 %s입니다.",
            city.getName(),
            weather.temperature_2m(),
            weather.apparent_temperature(),
            weather.relative_humidity_2m(),
            weather.wind_speed_10m(),
            WeatherCode.getDescription(weather.weather_code())
        );
    }
}