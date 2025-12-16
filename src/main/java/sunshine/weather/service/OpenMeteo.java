package sunshine.weather.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import sunshine.weather.dto.ForecastResponse;
import sunshine.weather.model.City;

@Component
public class OpenMeteo {
    private final RestClient client;

    public OpenMeteo(RestClient.Builder builder) {
        this.client = builder.build();
    }

    public ForecastResponse.Current fetchCurrent(City city) {
        var uri = UriComponentsBuilder.fromUriString("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", city.getLatitude())
                .queryParam("longitude", city.getLongitude())
                .queryParam("current", "temperature_2m", "weather_code", "relative_humidity_2m", "wind_speed_10m", "apparent_temperature")
                .toUriString();

        try {
            var response = client.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ForecastResponse.class);

            if (response == null || response.current() == null) {
                throw new IllegalStateException("response is null");
            }
            return response.current();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}